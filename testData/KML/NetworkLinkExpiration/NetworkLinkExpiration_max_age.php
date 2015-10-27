<?php 
 /**
  * Copyright (C) 2012 United States Government as represented by the Administrator of the
  * National Aeronautics and Space Administration.
  * All Rights Reserved.
  */

 /**
  * NetworkLink target that includes a Cache-Control header to cause the link to refresh every 5 seconds.
  *
  * $Id: NetworkLinkExpiration_max_age.php 691 2012-07-12 19:17:17Z pabercrombie $
  */

$expire_in = 5; // Expire in 5 seconds

header("Content-Type: application/vnd.google-earth.kml+xml");
header("Cache-Control: max-age=$expire_in");
?>
<kml xmlns="http://www.opengis.net/kml/2.2">
    <Document>
        <Placemark>
            <name><?php print date("H:i:s") ?></name>
            <description>Updates every <?php print $expire_in ?> seconds using a Cache-Control header</description>
            <Point>
                <coordinates>-106.9236,42.0667,0</coordinates>
            </Point>
        </Placemark>
    </Document>
</kml>
