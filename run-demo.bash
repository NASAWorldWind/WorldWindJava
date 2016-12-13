#!/bin/bash

#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

#
# Default to the ApplicationTemplate example if no arguments are provided
#
if [ $# -lt 1 ]
then
  WWDEMO=gov.nasa.worldwindx.examples.ApplicationTemplate
else
  WWDEMO=$*
fi

#
# Run a WorldWind Demo
#
echo Running ${WWDEMO}
java -Xmx1024m -classpath ./worldwind.jar:./worldwindx.jar:./gdal.jar:./jogl-all.jar:./gluegen-rt.jar ${WWDEMO}