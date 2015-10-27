$Id: README.txt 1171 2013-02-11 21:45:02Z dcollins $

####################################################
# NASA World Wind Java SDK - Build script overview #
####################################################

These are the Build scripts for NASA World Wind Java SDK.

## Intended Audience:
These scripts are intended to be used by developers wishing to update/modify the World Wind source files, and recompile
or re-generate a fresh worldwind.jar

By default, the initial worldwind.jar file should be runnable simply by uzipping worldwind-2.0.0.zip into a
local folder (to be referenced as WORLDWIND_HOME) and launching it with java:

	java -jar WORLDWIND_HOME/worldwind.jar
	
If you wish to make changes to the java source, it can be found in:

	WORLDWIND_HOME/src

In order to make a new jar, simply use the existing ANT target:

	ant worldwind.jarfile

This will recompile source as needed, package the compiled sources into a new worldwind.jar file, and place that JAR file
in WORLDWIND_HOME. Note: this is also the default target for the ANT scripts, so simply entering "ant" from
WORLDWIND_HOME will work as well

Run "ant -p" to see all available ANT targets

##############################
# Note on build dependencies #
##############################

In order for World Wind Java to build and run properly, the jogl libraries MUST be present in the WORLDWIND_JAVA
directory.