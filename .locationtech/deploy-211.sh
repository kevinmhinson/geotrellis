#!/usr/bin/env bash

 set -e
 set -x

 ./sbt "project macros" publish -no-colors \
   && ./sbt "project vector" publish -no-colors \
   && ./sbt "project proj4" publish -no-colors \
   && ./sbt "project raster" publish -no-colors \
   && ./sbt "project spark" publish -no-colors \
   && ./sbt "project spark-pipeline" publish -no-colors \
   && ./sbt "project s3" publish -no-colors \
   && ./sbt "project s3-spark" publish -no-colors \
   && ./sbt "project accumulo" publish -no-colors \
   && ./sbt "project accumulo-spark" publish -no-colors \
   && ./sbt "project hbase" publish -no-colors \
   && ./sbt "project hbase-spark" publish -no-colors \
   && ./sbt "project cassandra" publish -no-colors \
   && ./sbt "project cassandra-spark" publish -no-colors \
   && ./sbt "project geomesa" publish -no-colors \
   && ./sbt "project geotools" publish -no-colors \
   && ./sbt "project shapefile" publish -no-colors \
   && ./sbt "project layer" publish -no-colors \
   && ./sbt "project store" publish -no-colors \
   && ./sbt "project util" publish -no-colors \
   && ./sbt "project vectortile" publish -no-colors \
   && ./sbt "project raster-testkit" publish -no-colors \
   && ./sbt "project vector-testkit" publish -no-colors \
   && ./sbt "project spark-testkit" publish -no-colors
