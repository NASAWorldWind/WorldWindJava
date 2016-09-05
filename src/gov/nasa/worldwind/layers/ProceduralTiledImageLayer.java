/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;


import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Patrick Murris
 * @version $Id:$
 */
public abstract class ProceduralTiledImageLayer extends BasicTiledImageLayer
{
    public ProceduralTiledImageLayer(LevelSet levelSet)
    {
        super(levelSet);
    }

    public ProceduralTiledImageLayer(AVList params)
    {
        super(params);
    }

    abstract BufferedImage createTileImage(TextureTile tile, BufferedImage image);

    @Override
    protected void retrieveTexture(final TextureTile tile, DownloadPostProcessor postProcessor)
    {
        final File outFile = WorldWind.getDataFileStore().newFile(tile.getPath());
        if (outFile == null || outFile.exists())
            return;

        // Create and save tile texture image.
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        image = createTileImage(tile, image);
        try
        {
            ImageIO.write(image, "png", outFile);
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("layers.TextureLayer.ExceptionSavingRetrievedTextureFile", outFile.getPath());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
        }
    }
}