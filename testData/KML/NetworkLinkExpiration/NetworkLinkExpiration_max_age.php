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
