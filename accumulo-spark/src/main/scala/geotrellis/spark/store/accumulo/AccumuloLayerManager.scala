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

package geotrellis.spark.store.accumulo

import geotrellis.layer._
import geotrellis.store._
import geotrellis.store.AttributeStore.Fields
import geotrellis.store.accumulo._
import geotrellis.store.avro.AvroRecordCodec
import geotrellis.store.index._
import geotrellis.spark._
import geotrellis.spark.store._
import geotrellis.util._

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import io.circe._

import scala.reflect.ClassTag

class AccumuloLayerManager(attributeStore: AccumuloAttributeStore, instance: AccumuloInstance)(implicit sc: SparkContext)
    extends LayerManager[LayerId]{
  def delete(id: LayerId): Unit =
    AccumuloLayerDeleter(attributeStore, instance).delete(id)

  def copy[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]
  ](from: LayerId, to: LayerId): Unit =
    AccumuloLayerCopier(instance).copy[K, V, M](from, to)

  def move[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]
  ](from: LayerId, to: LayerId): Unit =
    AccumuloLayerMover(instance).move[K, V, M](from, to)

  def reindex[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]
  ](id: LayerId, keyIndexMethod: KeyIndexMethod[K]): Unit =
    AccumuloLayerReindexer(instance).reindex[K, V, M](id, keyIndexMethod)

  def reindex[
    K: AvroRecordCodec: Boundable: Encoder: Decoder: ClassTag,
    V: AvroRecordCodec: ClassTag,
    M: Encoder: Decoder: Component[?, Bounds[K]]
  ](id: LayerId, keyIndex: KeyIndex[K]): Unit =
    AccumuloLayerReindexer(instance).reindex[K, V, M](id, keyIndex)
}
