#!/usr/bin/python3

import sys
from pathlib import Path
import shutil

# Script to update WWJ JOGL libs from a new jogamp-all-platforms.7z unpacked distribution.
# Example: ./refresh-jogl.py ../../downloads/jogamp-all-platforms/jar
if len(sys.argv)<2:
    print('Usage: refresh-jogl.py [path to jars]')
    sys.exit(1)
    
srcPath=sys.argv[1]
jogl_jars=['gluegen-rt.jar','gluegen-rt-natives-linux-amd64.jar','gluegen-rt-natives-macosx-universal.jar','gluegen-rt-natives-windows-amd64.jar', \
            'jogl-all.jar','jogl-all-natives-linux-amd64.jar','jogl-all-natives-macosx-universal.jar','jogl-all-natives-windows-amd64.jar']
for jar in jogl_jars:
    srcJar=srcPath+'/'+jar
    destJar='./'+jar
    print(srcJar, ' => ', destJar)
    shutil.copyfile(srcJar,destJar)
    
jogl_txts=['jogl.README.txt','jogl.LICENSE.txt','gluegen.LICENSE.txt']
folderIdx=srcPath.rfind('/')
txtPath=srcPath[:folderIdx]
for txt in jogl_txts:
    srcTxt=txtPath+'/'+txt
    destTxt='./'+txt
    print(srcTxt, ' => ', destTxt)
    shutil.copyfile(srcTxt,destTxt)
