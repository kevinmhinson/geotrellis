/*
 * Copyright 2019 Azavea
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

package geotrellis.spark.store.http.util

import geotrellis.util.RangeReader

import org.scalatest._

import java.net.URI

class HttpRangeReaderProviderSpec extends FunSpec with Matchers {
  describe("HttpRangeReaderProviderSpec") {
    it("should create a HttpRangeReader from a URI") {
      val path = "http://localhost:8081/all-ones.tif"
      val reader = RangeReader(new URI(path))

      assert(reader.isInstanceOf[HttpRangeReader])
    }

    it("should dectect a bad URL") {
      val path = "httpa://localhost:8081/!!!!/all-ones.tif"
      val result = new HttpRangeReaderProvider().canProcess(new URI(path))

      result should be (false)
    }
  }
}
