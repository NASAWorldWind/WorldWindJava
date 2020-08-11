/*
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

package gov.nasa.worldwindx.applications.glider;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;

import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * View class to support GLIDER program. This class is internal to the Eclipse RCP implementation of EarthView in the
 * GLIDER source.
 *
 * @author tag
 * @version $Id: GliderWorldWindow.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GliderWorldWindow extends WorldWindowGLCanvas
{
    protected CopyOnWriteArraySet<GliderImage> imageTable = new CopyOnWriteArraySet<GliderImage>();
    protected LayerListener layerListener = new LayerListener();

    /**
     * Adds an image to display on the globe.
     *
     * @param image the image to display
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     * @throws IOException              if the image cannot be opened.
     */
    public void addImage(GliderImage image) throws IOException
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.imageTable.contains(image))
            this.removeImage(image);

        GliderImageLayer layer = new GliderImageLayer();

        image.setValue(AVKey.LAYER, layer);
        layer.setImage(image);
        ApplicationTemplate.insertBeforeCompass(this, layer);

        layer.addPropertyChangeListener(this.layerListener);

        this.imageTable.add(image);

        this.firePropertyChange(GliderImage.GLIDER_IMAGE_SOURCE, null, image);

        this.repaint();
    }

    /**
     * Removes a specified image from the globe.
     *
     * @param image the image to remove.
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     */
    public void removeImage(GliderImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageTable.remove(image);

        Layer layer = (Layer) image.getValue(AVKey.LAYER);
        if (layer != null)
        {
            image.removeKey(AVKey.LAYER);
            layer.removePropertyChangeListener(this.layerListener);
            this.getModel().getLayers().remove(layer);
            layer.dispose();
        }

        this.firePropertyChange(GliderImage.GLIDER_IMAGE_SOURCE, null, image);

        this.repaint();
    }

    /**
     * Returns the set of currently associated images.
     *
     * @return the set of currently associated images. The returned set is not modifiable and cannot be used to
     *         associate or disassociate an image.
     */
    public Set<GliderImage> getImages()
    {
        return Collections.unmodifiableSet(this.imageTable);
    }

    /**
     * Indicates whether a specified image is associated.
     *
     * @param image the image in question.
     *
     * @return <code>true</code> if the image is associated, <code>false</code> if not or if <code>image</code> is
     *         null.
     */
    public boolean containsImage(GliderImage image)
    {
        return image != null && this.imageTable.contains(image);
    }

    protected class LayerListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            GliderWorldWindow.this.firePropertyChange(GliderImage.GLIDER_IMAGE_SOURCE, null, this);
            GliderWorldWindow.this.repaint();
        }
    }
}
