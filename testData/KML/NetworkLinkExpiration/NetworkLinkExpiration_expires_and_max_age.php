<?php

 /**
  * Copyright (C) 2012 United States Government as represented by the Administrator of the
  * National Aeronautics and Space Administration.
  * All Rights Reserved.
  */

 /**
  * NetworkLink target that includes a both Expires and Cache-Control headers. The Cache-Control header should take
  * precedence, causing the link to refresh every 5 seconds.
  *
  * $Id: NetworkLinkExpiration_expires_and_max_age.php 691 2012-07-12 19:17:17Z pabercrombie $
  */

$expire_in = 5; // Expire in 5 seconds
$expires = gmdate("r", time() + 3600); // One hour from now

header("Content-Type: application/vnd.google-earth.kml+xml");

// Set Expires header to one hour from now, and Cache-Control header to 5 seconds from now. Cache-Control should have
// priority. See HTTP Spec: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html and KML Spec section 13.1.3.2.1.
header("Expires: $expires");
header("Cache-Control: max-age=$expire_in");
?>
<kml xmlns="http://www.opengis.net/kml/2.2">
    <Document>
        <Placemark>
            <name><?php print date("H:i:s") ?></name>
            <description>Updates every <?php print $expire_in ?> seconds using a Cache-Control header</description>
            <Point>
                <coordinates>-108.74143,35.5973,0</coordinates>
            </Point>
        </Placemark>
    </Document>
</kml>
