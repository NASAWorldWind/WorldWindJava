/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class ImageTrimmer
{
    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
        {
            return;
        }

        for (String path : args)
        {
            try
            {
                trimImageInPlace(new File(path));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void trimImageInPlace(File file) throws IOException
    {
        BufferedImage originalImage = ImageIO.read(file);
        BufferedImage trimmedImage = ImageUtil.trimImage(originalImage);
        ImageIO.write(trimmedImage, WWIO.getSuffix(file.getPath()), file);
    }
}
