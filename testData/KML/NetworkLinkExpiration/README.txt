$Id: README.txt 691 2012-07-12 19:17:17Z pabercrombie $

Test suite for KML NetworkLink expiration
-----------------------------------------

This directory contains a KML file of network links, and PHP scripts to generate HTTP responses that include KML
documents with expiration headers.

How to install
--------------

Install the PHP scripts in this directory to a web server capable of serving PHP. Set the links in
NetworkLinkExpiration.kml to point to this server.

How to test
-----------

Open NetworkLinkExpiration.kml in KMLViewer. Each link loads one placemark. The label on each placemark should change
every 5 seconds.