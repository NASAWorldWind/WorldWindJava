#!/bin/bash

#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

#
# Run a WorldWind Demo
#
# $Id: run-demo.bash 1171 2013-02-11 21:45:02Z dcollins $
#

echo Running $1
java -Xmx512m -Dsun.java2d.noddraw=true -classpath ./src:./classes:./worldwind.jar:./worldwindx.jar:./jogl-all.jar:./gluegen-rt.jar:./gdal.jar $*
