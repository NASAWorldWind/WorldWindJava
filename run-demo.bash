#!/bin/bash

#
# Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
# Administrator of the National Aeronautics and Space Administration.
# All rights reserved.
# 
# The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
# Version 2.0 (the "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
# 
# NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
# software:
# 
#     Jackson Parser – Licensed under Apache 2.0
#     GDAL – Licensed under MIT
#     JOGL – Licensed under  Berkeley Software Distribution (BSD)
#     Gluegen – Licensed under Berkeley Software Distribution (BSD)
# 
# A complete listing of 3rd Party software notices and licenses included in
# NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
# notices and licenses PDF found in code directory.
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
# Run a WorldWind Demo via Maven.
#
# Maven resolves the full classpath (JOGL 2.6.0, GlueGen 2.6.0, GDAL, etc.)
# from the project dependencies declared in pom.xml, so no manual classpath
# management is required here.
#
# The --add-opens flags are declared in the exec-maven-plugin configuration
# in pom.xml and are forwarded automatically to the forked JVM.
#
echo "Running ${WWDEMO}"
mvn --quiet exec:exec -Dexec.mainClass="${WWDEMO}"