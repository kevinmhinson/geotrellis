/*
 * Copyright 2016 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.spark.store.s3

import geotrellis.layer._
import geotrellis.store._
import geotrellis.store.util._
import geotrellis.store.avro._
import geotrellis.store.avro.codecs._
import geotrellis.store.index._
import geotrellis.store.s3._
import geotrellis.spark.store._
import geotrellis.util._

import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.S3Client
import org.apache.spark.rdd.RDD
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import cats.Semigroup

import scala.concurrent.ExecutionContext
import scala.reflect._

/**
  * Handles writing Raster RDDs and their metadata to S3.
  *
  * @param bucket              S3 bucket to be written to
  * @param keyPrefix           S3 prefix to write the raster to
  * @param attributeStore      AttributeStore to be used for storing raster metadata
  * @param putObjectModifier   Function that will be applied ot S3 PutObjectRequests, so that they can be modified (e.g. to change the ACL settings)
  * @param s3Client           A function which returns an S3 Client (real or mock) into-which to save the data
  * @param executionContext A function to get execution context
 */
class S3LayerWriter(
  val attributeStore: AttributeStore,
  bucket: String,
  keyPrefix: String,
  putObjectModifier: PutObjectRequest => PutObjectRequest = identity,
  s3Client: => S3Client = S3ClientProducer.get(),
  executionContext: => ExecutionContext = BlockingThreadPool.executionContext
) extends LayerWriter[LayerId] with LazyLogging {

  def rddWriter: S3RDDWriter = new S3RDDWriter(s3Client, executionContext)

  // Layer Updating
  def overwrite[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]: Semigroup
  ](
    id: LayerId,
    rdd: RDD[(K, V)] with Metadata[M]
  ): Unit = {
    update(id, rdd, None)
  }

  def update[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]: Semigroup
  ](id: LayerId, rdd: RDD[(K, V)] with Metadata[M], mergeFunc: (V, V) => V): Unit = {
    update(id, rdd, Some(mergeFunc))
  }

  private def update[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]: Semigroup
  ](
    id: LayerId,
    rdd: RDD[(K, V)] with Metadata[M],
    mergeFunc: Option[(V, V) => V]
  ) = {
    validateUpdate[S3LayerHeader, K, V, M](id, rdd.metadata) match {
      case Some(LayerAttributes(header, metadata, keyIndex, writerSchema)) =>
        val prefix = header.key
        val bucket = header.bucket
        val maxWidth = Index.digits(keyIndex.toIndex(keyIndex.keyBounds.maxKey))
        val keyPath = (key: K) => makePath(prefix, Index.encode(keyIndex.toIndex(key), maxWidth))

        logger.info(s"Writing update for layer ${id} to $bucket $prefix")
        attributeStore.writeLayerAttributes(id, header, metadata, keyIndex, writerSchema)
        rddWriter.update(rdd, bucket, keyPath, Some(writerSchema), mergeFunc)

      case None =>
        logger.warn(s"Skipping update with empty bounds for $id.")
    }
  }

  // Layer Writing
  protected def _write[
    K: AvroRecordCodec: Encoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Component[?, Bounds[K]]
  ](id: LayerId, rdd: RDD[(K, V)] with Metadata[M], keyIndex: KeyIndex[K]): Unit = {
    require(!attributeStore.layerExists(id), s"$id already exists")
    implicit val sc = rdd.sparkContext
    val prefix = makePath(keyPrefix, s"${id.name}/${id.zoom}")
    val metadata = rdd.metadata
    val header = S3LayerHeader(
      keyClass = classTag[K].toString(),
      valueClass = classTag[V].toString(),
      bucket = bucket,
      key = prefix)

    val maxWidth = Index.digits(keyIndex.toIndex(keyIndex.keyBounds.maxKey))
    val keyPath = (key: K) => makePath(prefix, Index.encode(keyIndex.toIndex(key), maxWidth))
    val schema = KeyValueRecordCodec[K, V].schema

    try {
      attributeStore.writeLayerAttributes(id, header, metadata, keyIndex, schema)

      logger.info(s"Saving RDD ${id.name} to $bucket  $prefix")
      rddWriter.write(rdd, bucket, keyPath, putObjectModifier)
    } catch {
      case e: Exception => throw new LayerWriteError(id).initCause(e)
    }
  }
}

object S3LayerWriter {
  def apply(
    attributeStore: AttributeStore,
    bucket: String,
    prefix: String,
    putObjectModifier: PutObjectRequest => PutObjectRequest,
    getClient: => S3Client = S3ClientProducer.get()
  ): S3LayerWriter =
    new S3LayerWriter(attributeStore, bucket, prefix, putObjectModifier)

  def apply(attributeStore: AttributeStore, bucket: String, prefix: String, s3Client: => S3Client): S3LayerWriter =
    new S3LayerWriter(attributeStore, bucket, prefix, identity, s3Client)

  def apply(attributeStore: S3AttributeStore): S3LayerWriter =
    apply(attributeStore, attributeStore.bucket, attributeStore.prefix, attributeStore.client)

  def apply(attributeStore: S3AttributeStore, putObjectModifier: PutObjectRequest => PutObjectRequest): S3LayerWriter =
    apply(attributeStore, attributeStore.bucket, attributeStore.prefix, putObjectModifier, attributeStore.client)

  def apply(bucket: String, prefix: String, s3Client: => S3Client): S3LayerWriter = {
    val attStore = S3AttributeStore(bucket, prefix, s3Client)
    apply(attStore)
  }

  def apply(
    bucket: String,
    prefix: String,
    putObjectModifier: PutObjectRequest => PutObjectRequest,
    s3Client: => S3Client
  ): S3LayerWriter = {
    val attStore = S3AttributeStore(bucket, prefix, s3Client)
    apply(attStore, putObjectModifier)
  }

}
