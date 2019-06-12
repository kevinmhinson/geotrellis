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

package geotrellis.raster.summary.types

import cats.Monoid
import cats.syntax.monoid._
import geotrellis.raster._

/**
  * Typesafe wrapper for a Double value that stores the Sum for a summary computation.
  *
  * The provided monoids ignore NaN values when combining.
  *
  * @param value
  */
case class SumValue(val value: Double) extends AnyVal {
  def toOption: Option[Double] = if (isData(value)) Some(value) else None
}

object SumValue {
  implicit val sumValueMonoid: Monoid[SumValue] = new Monoid[SumValue] {
    override def empty: SumValue = SumValue(Double.NaN)

    override def combine(x: SumValue, y: SumValue): SumValue = {
      if (isData(x.value) && isData(y.value)) {
        SumValue(x.value + y.value)
      } else if (isData(x.value)) {
        x
      } else if (isData(y.value)) {
        y
      } else {
        empty
      }
    }
  }

  implicit val sumValueArrayMonoid: Monoid[Array[SumValue]] = new Monoid[Array[SumValue]] {
    override def empty: Array[SumValue] = Array[SumValue]()

    override def combine(x: Array[SumValue], y: Array[SumValue]): Array[SumValue] = {
      x.zipAll(y, Monoid[SumValue].empty, Monoid[SumValue].empty).map { case (x, y) => x.combine(y) }
    }
  }
}
