@echo off
REM Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
REM Administrator of the National Aeronautics and Space Administration.
REM All rights reserved.
REM 
REM The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
REM Version 2.0 (the "License"); you may not use this file except in compliance
REM with the License. You may obtain a copy of the License at
REM http://www.apache.org/licenses/LICENSE-2.0
REM 
REM Unless required by applicable law or agreed to in writing, software distributed
REM under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
REM CONDITIONS OF ANY KIND, either express or implied. See the License for the
REM specific language governing permissions and limitations under the License.
REM 
REM NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
REM software:
REM 
REM     Jackson Parser – Licensed under Apache 2.0
REM     GDAL – Licensed under MIT
REM     JOGL – Licensed under  Berkeley Software Distribution (BSD)
REM     Gluegen – Licensed under Berkeley Software Distribution (BSD)
REM 
REM A complete listing of 3rd Party software notices and licenses included in
REM NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
REM notices and licenses PDF found in code directory.

REM Default to the ApplicationTemplate example if a class name is not provided
IF "%1"=="" (SET WWDEMO=gov.nasa.worldwindx.examples.ApplicationTemplate) ELSE (SET WWDEMO=%*)

REM Run a WorldWind Demo via Maven.
REM
REM Maven resolves the full classpath (JOGL 2.6.0, GlueGen 2.6.0, GDAL, etc.)
REM from the project dependencies declared in pom.xml, so no manual classpath
REM management is required here.
REM
REM The --add-opens flags are declared in the exec-maven-plugin configuration
REM in pom.xml and are forwarded automatically to the forked JVM.
@echo Running %WWDEMO%
mvn --quiet exec:exec "-Dexec.mainClass=%WWDEMO%"
