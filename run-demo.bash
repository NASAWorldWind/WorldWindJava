#!/bin/bash

#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

#
# Run a WorldWind Demo
#

echo Running $1
java -Xmx1024m -Dsun.java2d.noddraw=true -classpath ./src:./classes:./worldwind.jar:./worldwindx.jar:./jogl-all.jar:./gluegen-rt.jar:./gdal.jar $*
