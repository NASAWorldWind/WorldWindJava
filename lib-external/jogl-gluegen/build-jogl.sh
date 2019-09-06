set -x

cp patch/MacOSXJAWTWindow.java jogl/src/nativewindow/classes/jogamp/nativewindow/jawt/macosx
cp patch/MacOSXCGLContext.java jogl/src/jogl/classes/jogamp/opengl/macosx/cgl
cp patch/GLCanvas.java jogl/src/jogl/classes/com/jogamp/opengl/awt

cd jogl/make
ant -Dtarget.sourcelevel=1.8 -Dtarget.targetlevel=1.8 -Dtarget.rt.jar=dummy.jar

./jogl/src/nativewindow/classes/jogamp/nativewindow/jawt/macosx/MacOSXJAWTWindow.java


