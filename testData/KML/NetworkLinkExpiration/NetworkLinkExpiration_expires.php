<?php
 /**
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
