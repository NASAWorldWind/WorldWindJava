/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tiff;

import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOInvalidTreeException;

/**
 * @author brownrigg
 * @version $Id: GeotiffMetaData.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class GeotiffMetaData extends IIOMetadata {

    
    public boolean isReadOnly() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getAsTree(String formatName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
