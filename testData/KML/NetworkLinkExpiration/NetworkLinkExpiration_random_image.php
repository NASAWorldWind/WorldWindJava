<?php
 /**
  * Copyright (C) 2012 United States Government as represented by the Administrator of the
  * National Aeronautics and Space Administration.
  * All Rights Reserved.
  */

 /**
  * Generate an image that contains a random number. This script
  * is intended for testing KML Icon updates. It provides a
  * source of constantly changing images, so that the tester
  * can confirm that the link is actually updating.
  *
  * $Id$
  */

$im = imagecreatetruecolor(128, 128);
$text_color = imagecolorallocate($im, 233, 14, 91);
imagestring($im, 1, 5, 5, 'The number below should', $text_color);
imagestring($im, 1, 5, 15, 'change when the link', $text_color);
imagestring($im, 1, 5, 25, 'updates', $text_color);
imagestring($im, 4, 64, 64,  rand(1,50), $text_color);

// Set the content type header - in this case image/jpeg
header('Content-Type: image/jpeg');
header("Cache-Control: max-age=5");

// Output the image in JPEG format
imagejpeg($im);
imagedestroy($im);
?>
