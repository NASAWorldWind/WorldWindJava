set -x

cd jogl/make
ant clean -Dtarget.sourcelevel=1.8 -Dtarget.targetlevel=1.8 -Dtarget.rt.jar=/home/mpeterson/d/jopengl/dummy.jar

