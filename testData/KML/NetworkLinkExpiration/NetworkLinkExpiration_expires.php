<?php
 /**
  * Copyright (C) 2012 United States Government as represented by the Administrator of the
  * National Aeronautics and Space Administration.
  * All Rights Reserved.
  */

 /**
  * NetworkLink target that includes an Expires header to cause the link to refresh every 5 seconds.
  * $Id: NetworkLinkExpiration_expires.php 696 2012-07-13 16:47:10Z pabercrombie $
  */

$expire_in = 5; // Expire in 5 seconds
$expires = gmdate("r", time() + $expire_in);

header("Content-Type: application/vnd.google-earth.kml+xml");
header("Expires: $expires");
?>
<kml xmlns="http://www.opengis.net/kml/2.2">
    <!-- Set a minRefreshPeriod to keep the link from updating immediately if the server clock is behind the client clock. -->
    <NetworkLinkControl>
        <minRefreshPeriod>4</minRefreshPeriod>
    </NetworkLinkControl>
    <Document>
        <Placemark>
            <name><?php print date("H:i:s") ?></name>
            <description>Updates every <?php print $expire_in ?> seconds using an Expires header</description>
            <Point>
                <coordinates>-109.2988,26.9340,0</coordinates>
            </Point>
        </Placemark>
    </Document>
</kml>
