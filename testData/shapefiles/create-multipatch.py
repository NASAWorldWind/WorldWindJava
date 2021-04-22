#!/usr/bin/python

# Multipatch shapefiles need a specialized type for each part which can be:

# VALUE  PART TYPE
# 0      Triangle Strip
# 1      Triangle Fan
# 2      Outer Ring
# 3      Inner Ring
# 4      First Ring
# 5      Ring
# 
# In pyshp, this is defined in your parts array using the array "partTypes"
# 
# All polygons must move in clockwise order which I didn't double-check in your example. I created my own coordinates to be sure and used the rest of your code verbatim to create a working cube:

# import shapefile
# 
# w = shapefile.Writer(shapeType=shapefile.MULTIPATCH)
# w.poly(parts=[
# [[0,0,0], [0,1,0], [0,1,1], [0,0,1], [0,0,0]],
# [[0,0,0], [0,0,1], [1,0,1], [1,0,0], [0,0,0]],
# [[1,1,0], [1,1,1], [0,1,1], [0,1,0], [1,1,0]],
# [[1,0,0], [1,0,1], [1,1,1], [1,1,0], [1,0,0]],
# [[0,0,1], [0,1,1], [1,1,1], [1,0,1], [0,0,1]],
# [[0,0,0], [0,1,0], [1,1,0], [1,0,0], [0,0,0]]
# ], 
        # partTypes=[5,5,5,5,5,5],
        # shapeType=31)
# w.field("NAME")
# w.record("PolyZTest")
# w.save("MyPolyS")

import shapefile
from shapefile import TRIANGLE_STRIP, TRIANGLE_FAN, OUTER_RING

w = shapefile.Writer('./multipatch')
w.field('name', 'C')
# lat=40.009993372683
# lon=-105.27284091410579
lat=43
lon=-97
base=0
height=250;
deltaLatLon=0.01
# w.multipatch([
			 # [[lon,lat,base],[lon,lat,base+height],[lon,lat+deltaLatLon,base]]
			 # ],
			 # partTypes=[TRIANGLE_STRIP]) # one type for each part

w.multipatch([
			 [[lon,lat,0],[lon,lat,height],[lon,lat+deltaLatLon,0],[lon,lat+deltaLatLon,height],[lon+deltaLatLon,lat+deltaLatLon,0],[lon+deltaLatLon,lat+deltaLatLon,height],[lon+deltaLatLon,lat,0],[lon+deltaLatLon,lat,height],[lon,lat,0],[lon,lat,height]], # TRIANGLE_STRIP for house walls
			 [[lon+0.005,lat+0.005,height*2],[lon,lat,height],[lon,lat+deltaLatLon,height],[lon+deltaLatLon,lat+deltaLatLon,height],[lon+deltaLatLon,lat,height],[lon,lat,height]], # TRIANGLE_FAN for pointed house roof
			 ],
			 partTypes=[TRIANGLE_STRIP, TRIANGLE_FAN]) # one type for each part

# w.multipatch([
			 # [[lon+0.005,lat+0.005,height*2],[lon,lat,height],[lon,lat+deltaLatLon,height]], # TRIANGLE_FAN for pointed house roof
			 # ],
			 # partTypes=[TRIANGLE_FAN]) # one type for each part

# w.multipatch([
			 # [[0,0,0],[0,0,3],[5,0,0],[5,0,3],[5,5,0],[5,5,3],[0,5,0],[0,5,3],[0,0,0],[0,0,3]], # TRIANGLE_STRIP for house walls
			 # [[2.5,2.5,5],[0,0,3],[5,0,3],[5,5,3],[0,5,3],[0,0,3]], # TRIANGLE_FAN for pointed house roof
			 # ],
			 # partTypes=[TRIANGLE_STRIP, TRIANGLE_FAN]) # one type for each part

# w.multipatch([
			 # [[0,0,0],[0,0,300],[0.005,0,0],[0.005,0,300],[0.005,0.005,0],[0.005,0.005,300],[0,0.005,0],[0,0.005,300],[0,0,0],[0,0,300]], # TRIANGLE_STRIP for house walls
			 # [[0.0025,0.0025,500],[0,0,300],[0.005,0,300],[0.005,0.005,300],[0,0.005,300],[0,0,300]], # TRIANGLE_FAN for pointed house roof
			 # ],
			 # partTypes=[TRIANGLE_STRIP, TRIANGLE_FAN]) # one type for each part
# w.multipatch([
			 # [[0.005, 0.005, 300],[0, 0, 300],[0, 0, 0],[0.005, 0.005, 0],[0.005, 0.005, 300]]
			 # ],
			 # partTypes=[OUTER_RING]) # one type for each part
w.record('house1')

w.close()

prj = open("./multipatch.prj", "w")
epsg = 'GEOGCS["WGS 84",'
epsg += 'DATUM["WGS_1984",'
epsg += 'SPHEROID["WGS 84",6378137,298.257223563]]'
epsg += ',PRIMEM["Greenwich",0],'
epsg += 'UNIT["degree",0.0174532925199433],'
epsg += 'AUTHORITY["EPSG","4326"]]'
prj.write(epsg)
prj.close()