# GeoTrellis

[![Build Status](https://api.travis-ci.org/geotrellis/geotrellis.svg)](http://travis-ci.org/geotrellis/geotrellis) [![Join the chat at https://gitter.im/geotrellis/geotrellis](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/geotrellis/geotrellis?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

*GeoTrellis* is a Scala library and framework that uses
Spark to work with raster data.  It is released under
the Apache 2 License.

GeoTrellis reads, writes, and operates on raster data
as fast as possible. It implements many
[Map Algebra](http://en.wikipedia.org/wiki/Map_algebra)
operations as well as vector to raster or raster to
vector operations.

GeoTrellis also provides tools to render rasters into
PNGs or to store metadata about raster files as JSON.
It aims to provide raster processing at web speeds (sub-second
or less) with RESTful endpoints as well as provide
fast batch processing of large raster data sets.

Please visit the **[project site](http://geotrellis.io)**
for more information as well as some interactive demos.


##Contact and Support
You can find more information and talk to developers
(let us know what you're working on!) at:

  - [Gitter](https://gitter.im/geotrellis/geotrellis)
  - [GeoTrellis mailing list](https://groups.google.com/group/geotrellis-user)


## Hello Raster

```scala
scala> import geotrellis.raster._
import geotrellis.raster._

scala> import geotrellis.raster.op.focal._
import geotrellis.raster.op.focal._

scala> val nd = NODATA
nd: Int = -2147483648

scala> val input = Array[Int](
     |         nd, 7, 1, 1, 3, 5, 9, 8, 2,
     |         9, 1, 1, 2, 2, 2, 4, 3, 5,
     |
     |         3, 8, 1, 3, 3, 3, 1, 2, 2,
     |         2, 4, 7, 1, nd, 1, 8, 4, 3)
input: Array[Int] = Array(-2147483648, 7, 1, 1, 3, 5, 9, 8, 2, 9, 1, 1, 2, 
2, 2, 4, 3, 5, 3, 8, 1, 3, 3, 3, 1, 2, 2, 2, 4, 7, 1, -2147483648, 1, 8, 4, 3)

scala> val iat = IntArrayTile(input, 9, 4)  // 9 and 4 here specify columns and rows
iat: geotrellis.raster.IntArrayTile = IntArrayTile([I@278434d0,9,4)

// The asciiDraw method is mostly useful when you're working with small tiles
// which can be taken in at a glance
scala> iat.asciiDraw()
res0: String =
"    ND     7     1     1     3     5     9     8     2
     9     1     1     2     2     2     4     3     5
     3     8     1     3     3     3     1     2     2
     2     4     7     1    ND     1     8     4     3
"

scala> val focalNeighborhood = Square(1)  // a 3x3 square neighborhood
focalNeighborhood: geotrellis.raster.op.focal.Square =
 O  O  O
 O  O  O
 O  O  O

scala> val meanTile = iat.focalMean(focalNeighborhood)
meanTile: geotrellis.raster.Tile = DoubleArrayTile([D@7e31c125,9,4)

scala> meanTile.getDouble(0, 0)  // Should equal (1 + 7 + 9) / 3
res1: Double = 5.666666666666667
```

## Documentation

- Further examples and documentation of GeoTrellis use-cases can be found in the [docs/](./docs) folder
- *Scaladocs* for the latest version of the project can be found here:

[http://geotrellis.github.com/scaladocs/latest/#geotrellis.package](http://geotrellis.github.com/scaladocs/latest/#geotrellis.package)


## Contributors

 - Josh Marcus
 - Erik Osheim
 - Rob Emanuele
 - Adam Hinz
 - Michael Tedeschi
 - Robert Cheetham
 - Justin Walgran
 - Eric J. Christeson
 - Ameet Kini
 - Mark Landry
 - Walt Chen
 - Eugene Cheipesh

## Contributing

Feedback and contributions to the project, no matter what kind,
are always very welcome. A CLA is required for contribution, see
the [CLA FAQ](https://github.com/geotrellis/geotrellis/wiki/Contributor-license-agreement-FAQ)
on the wiki for more information. Please refer to the
[Scala style guide](http://docs.scala-lang.org/style/) for
formatting patches to the codebase.
