REM Copyright (C) 2012 United States Government as represented by the Administrator of the
REM National Aeronautics and Space Administration.
REM All Rights Reserved.

REM Windows Batch file for Running a WorldWind Demo
REM $Id: run-demo.bat 1171 2013-02-11 21:45:02Z dcollins $

@echo Running %1
java -Xmx512m -Dsun.java2d.noddraw=true -classpath .\src;.\classes;.\worldwind.jar;.\worldwindx.jar;.\jogl-all.jar;.\gluegen-rt.jar;.\gdal.jar %*
