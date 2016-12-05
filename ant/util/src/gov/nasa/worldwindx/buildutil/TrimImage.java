/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.buildutil;

import java.awt.image.*;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * @author dcollins
 * @version $Id$
 */
public class TrimImage
{
    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
        {
            System.out.println("No files to trim. Specify files using the following command:\n"
                + "java gov.nasa.worldwindx.buildutil.TrimImage Files...");
            return;
        }

        TrimImage trimImage = new TrimImage();

        for (String path : args)
        {
            trimImage.execute(new File(path));
        }

        System.out.println("Done. " + trimImage.getFileCount() + " files trimmed.");
    }

    protected int fileCount;

    public TrimImage()
    {
    }

    public int getFileCount()
    {
        return this.fileCount;
    }

    public void execute(File file)
    {
        System.out.println("Trimming " + file + ".");

        try
        {
            this.doExecute(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void doExecute(File file) throws Exception
    {
        BufferedImage image = ImageIO.read(file);
        BufferedImage trimmedImage = this.trim(image);
        ImageIO.write(trimmedImage, this.getFileSuffix(file), file);
        this.fileCount++;
    }

    protected String getFileSuffix(File file)
    {
        String name = file.getName();
        int len = name.length();
        int p = name.lastIndexOf(".");
        return (p >= 0 && p + 1 < len) ? name.substring(p + 1, len) : null;
    }

    protected BufferedImage trim(BufferedImage image)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = new int[w];

        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = 0;
        int y2 = 0;

        for (int y = 0; y < h; y++)
        {
            image.getRGB(0, y, w, 1, pixels, 0, w);

            for (int x = 0; x < w; x++)
            {
                int a = ((pixels[x] >> 24) & 0xff);
                if (a <= 0)
                    continue;

                if (x1 > x)
                    x1 = x;
                if (x2 < x)
                    x2 = x;
                if (y1 > y)
                    y1 = y;
                if (y2 < y)
                    y2 = y;
            }
        }

        return (x1 < x2 && y1 < y2) ? image.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1)
            : new BufferedImage(BufferedImage.TYPE_INT_ARGB, 0, 0);
    }
}
