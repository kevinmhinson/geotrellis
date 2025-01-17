/*
 * Copyright 2018 Azavea
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

package geotrellis.spark.store.hadoop.geotiff

import geotrellis.store.hadoop.util.{HdfsUtils, HdfsRangeReader}
import geotrellis.raster.io.geotiff.reader.TiffTagsReader
import geotrellis.util.annotations.experimental

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import java.net.URI

@experimental object HadoopGeoTiffInput {
  /**
    *
    * Returns a list of URIs matching given regexp
    *
    * @name - group name
    * @uri - regexp
    * @conf - hadoopConfiguration
    */
  @experimental def list(name: String, uri: URI, conf: Configuration): List[GeoTiffMetadata] = {
    HdfsUtils
      .listFiles(new Path(uri), conf)
      .map { p =>
        val tiffTags = TiffTagsReader.read(HdfsRangeReader(p, conf))
        GeoTiffMetadata(tiffTags.extent, tiffTags.crs, name, uri)
      }
  }
}
