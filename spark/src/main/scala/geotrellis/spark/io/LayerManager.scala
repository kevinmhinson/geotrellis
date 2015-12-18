package geotrellis.spark.io

import geotrellis.spark._
import geotrellis.spark.io.avro._
import geotrellis.spark.io.index._
import geotrellis.spark.io.hadoop.HadoopFormat
import geotrellis.raster.mosaic.MergeView

import org.apache.spark.rdd._
import spray.json.JsonFormat

import scala.reflect.ClassTag

trait LayerManager[ID] {
  def delete[K: Boundable: AvroRecordCodec: JsonFormat: ClassTag, V: AvroRecordCodec: ClassTag, M: JsonFormat, C <: RDD[(K, V)]](id: ID): Unit

  def copy[K: Boundable: AvroRecordCodec: JsonFormat: ClassTag, V: AvroRecordCodec: MergeView: ClassTag, M: JsonFormat, C <: RDD[(K, V)]]
    (from: ID, to: ID, keyIndexMethod: Option[KeyIndexMethod[K]])(implicit bridge: Bridge[(RDD[(K, V)], M), C]): Unit

  def move[K: Boundable: AvroRecordCodec: JsonFormat: ClassTag, V: AvroRecordCodec: MergeView: ClassTag, M: JsonFormat, C <: RDD[(K, V)]]
    (from: ID, to: ID, keyIndexMethod: Option[KeyIndexMethod[K]])(implicit bridge: Bridge[(RDD[(K, V)], M), C]): Unit

  def reindex[K: Boundable: AvroRecordCodec: JsonFormat: ClassTag, V: AvroRecordCodec: MergeView: ClassTag, M: JsonFormat, C <: RDD[(K, V)]]
    (id: ID, keyIndexMethod: KeyIndexMethod[K])(implicit bridge: Bridge[(RDD[(K, V)], M), C], hadoopFormat: HadoopFormat[K,V]): Unit
}
