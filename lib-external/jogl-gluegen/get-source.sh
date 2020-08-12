set -x

git clone --recurse-submodules git://jogamp.org/srv/scm/gluegen.git gluegen
git clone --recurse-submodules git://jogamp.org/srv/scm/jogl.git jogl

cd gluegen
git checkout java11
cd ../jogl
git checkout java11



