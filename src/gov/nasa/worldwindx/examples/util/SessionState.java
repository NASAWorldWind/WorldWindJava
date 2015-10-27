/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: SessionState.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SessionState
{
    protected static class LayerStateFilename
    {
        protected String className;
        protected int index;

        public LayerStateFilename(String className, int index)
        {
            this.className = className;
            this.index = index;
        }
    }

    protected static final String VIEW_STATE_PATH = "SessionState/ViewState.xml";
    protected static final String LAYER_STATE_PATH = "SessionState/LayerState";
    protected static final String LAYER_STATE_FILENAME_DELIMITER = "-";
    protected static final Comparator<String> LAYER_STATE_FILENAME_COMPARATOR = new Comparator<String>()
    {
        @Override
        public int compare(String a, String b)
        {
            LayerStateFilename fna = parseLayerStateFilename(a);
            LayerStateFilename fnb = parseLayerStateFilename(b);

            if (fna == null || fnb == null)
                return fnb != null ? -1 : (fna != null ? 1 : 0);
            else
                return fna.index < fnb.index ? -1 : (fna.index > fnb.index ? 1 : 0);
        }
    };

    protected String sessionKey;

    public SessionState(String sessionKey)
    {
        if (WWUtil.isEmpty(sessionKey))
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sessionKey = sessionKey;
    }

    public String getSessionKey()
    {
        return this.sessionKey;
    }

    public void saveSessionState(WorldWindow worldWindow)
    {
        if (worldWindow == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.saveViewState(worldWindow);
        this.saveLayerListState(worldWindow);
    }

    public void restoreSessionState(WorldWindow worldWindow)
    {
        if (worldWindow == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.restoreViewState(worldWindow);
        this.restoreLayerListState(worldWindow);
    }

    protected void saveViewState(WorldWindow worldWindow)
    {
        // There's nothing to save if the view is null. We treat this as an exceptional condition rather than a
        // supported case and leave the existing state file, if one exists.
        View view = worldWindow.getView();
        if (view == null)
            return;

        try
        {
            // There's nothing to do if the view does not provide restorable state. A null return value from
            // getRestorableState is valid, and indicates that the view does not support save/restore. We treat this as
            // an exceptional condition rather than a standard case and leave the existing state file, if one exists.
            String stateInXml = view.getRestorableState();
            if (WWUtil.isEmpty(stateInXml))
                return;

            // Save the View's restorable state XML to the view state path determined by this SessionState's session key
            // and the view state sub-path. This overwrites the contents of an existing state file, if any exists.
            WWIO.makeParentDirs(this.getViewStatePath());
            WWIO.writeTextFile(stateInXml, new File(this.getViewStatePath()));
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "Unable to save view state: " + view, e);
        }
    }

    protected void restoreViewState(WorldWindow worldWindow)
    {
        // There's nothing to restore if the view is null. We treat this as an exceptional condition rather than a
        // supported case and leave the existing state file, if one exists.
        View view = worldWindow.getView();
        if (view == null)
            return;

        // There's nothing to restore if the view state file does not exist.
        File stateFile = new File(this.getViewStatePath());
        if (!stateFile.exists())
            return;

        try
        {
            // Read the View's restorable state XML from the view state path determined by this SessionState's session
            // key and the view state sub-path.
            String stateInXml = WWIO.readTextFile(stateFile);
            if (WWUtil.isEmpty(stateInXml))
            {
                String msg = Logging.getMessage("nullValue.RestorableStateIsNull");
                Logging.logger().warning(msg);
                return;
            }

            worldWindow.getView().restoreState(stateInXml);
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "Unable to restore view state: " + stateFile, e);
        }
    }

    protected void saveLayerListState(WorldWindow worldWindow)
    {
        // There's nothing to save if the Model or the LayerList is null. We treat this as an exceptional condition
        // rather than a supported case and leave the existing state files, if any exist.
        if (worldWindow.getModel() == null || worldWindow.getModel().getLayers() == null)
            return;

        // Delete the contents of the layer state path directory, but not the directory itself.
        File stateFile = new File(this.getLayerStatePath());

        try
        {
            if (stateFile.exists())
                WWIO.deleteDirectory(stateFile);
            else
                stateFile.mkdirs();

            int index = 0;
            for (Layer layer : worldWindow.getModel().getLayers())
            {
                if (layer == null)
                    continue; // There's nothing to save if the Layer is null.

                String filename = composeLayerStateFilename(layer.getClass().getName(), index);
                this.saveLayerState(layer, new File(stateFile, filename));
                index++;
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "Unable to save layer list state: " + stateFile, e);
        }
    }

    protected void restoreLayerListState(WorldWindow worldWindow)
    {
        // There's nothing to save if the Model or the LayerList is null. We treat this as an exceptional condition
        // rather than a supported case and leave the existing state files, if any exist.
        if (worldWindow.getModel() == null || worldWindow.getModel().getLayers() == null)
            return;

        File stateFile = new File(this.getLayerStatePath());

        String[] filenames = stateFile.list();
        if (filenames == null || filenames.length == 0)
            return;

        try
        {
            Arrays.sort(filenames, LAYER_STATE_FILENAME_COMPARATOR);
            LayerList layers = worldWindow.getModel().getLayers();

            for (String filename : filenames)
            {
                Layer layer = this.restoreLayerState(new File(stateFile, filename));
                if (layer == null)
                    continue;

                Layer existingLayer = this.findLayer(layers, layer.getName());
                if (existingLayer != null)
                    layers.remove(existingLayer);

                LayerStateFilename fn = parseLayerStateFilename(filename);
                if (fn != null && fn.index < layers.size())
                    layers.add(fn.index, layer);
                else
                    layers.add(layer);
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "Unable to restore layer list state: " + stateFile, e);
        }
    }

    protected void saveLayerState(Layer layer, File stateFile)
    {
        try
        {
            // There's nothing to do if the Layer cannot be restored.
            if (!this.isLayerRestorable(layer))
                return;

            // There's nothing to do if the Layer does not provide restorable state. A null return value from
            // getRestorableState is valid, and indicates that the Layer does not support save/restore.
            String stateInXml = layer.getRestorableState();
            if (WWUtil.isEmpty(stateInXml))
                return;

            // Save the Layer's restorable state XML to the view state path determined by this SessionState's session
            // key and the layer state sub-path.
            WWIO.writeTextFile(stateInXml, stateFile);
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "Unable to save layer state: " + layer, e);
        }
    }

    protected Layer restoreLayerState(File stateFile)
    {
        try
        {
            // Read the Layer's restorable state XML from the layer state path determined by this SessionState's session
            // key and the layer state sub-path.
            String stateInXml = WWIO.readTextFile(stateFile);
            if (WWUtil.isEmpty(stateInXml))
            {
                String msg = Logging.getMessage("nullValue.RestorableStateIsNull");
                Logging.logger().warning(msg);
                return null;
            }

            // Get the Layer's class name fom its encoded filename.
            LayerStateFilename fn = parseLayerStateFilename(stateFile.getName());
            if (fn == null)
            {
                Logging.logger().warning("Invalid layer state filename: " + stateFile.getName());
                return null;
            }

            // Restore the Layer using a constructor that takes a restorable state XML string as its sole argument.
            Class<?> c = Class.forName(fn.className);
            Constructor constructor = c.getConstructor(String.class);
            Object o = constructor.newInstance(stateInXml);

            return (o != null && o instanceof Layer) ? (Layer) o : null;
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "Unable to restore layer state: " + stateFile, e);
            return null;
        }
    }

    protected boolean isLayerRestorable(Layer layer)
    {
        try
        {
            if (layer.getClass().getConstructor(String.class) != null)
                return true;
        }
        catch (Exception e)
        {
            // Intentionally left blank. Class.getConstructor throws a NoSuchMethodError if the constructor does not
            // exist, in which case the return statement below is executed.
        }

        return false;
    }

    protected Layer findLayer(LayerList layers, String layerName)
    {
        if (layerName == null)
            return null;

        for (Layer layer : layers)
        {
            if (layer.getName() != null && layer.getName().equals(layerName))
                return layer;
        }

        return null;
    }

    protected String getSessionStatePath()
    {
        String appDataPath = Configuration.getCurrentUserAppDataDirectory();
        String sessionDataPath = this.getSessionKey();

        // The pattern for storing application data on all unix variants is to put those files in a hidden folder in the
        // current user's application data directory. Since the session key is our application data's folder name, we
        // ensure that it is hidden by inserting "." character at the beginning of the session key if one does not
        // already exist.
        if ((Configuration.isLinuxOS() || Configuration.isUnixOS() || Configuration.isSolarisOS())
            && !sessionDataPath.startsWith("."))
        {
            sessionDataPath = "." + sessionDataPath;
        }

        return WWIO.appendPathPart(appDataPath, sessionDataPath);
    }

    protected String getViewStatePath()
    {
        return WWIO.appendPathPart(this.getSessionStatePath(), VIEW_STATE_PATH);
    }

    protected String getLayerStatePath()
    {
        return WWIO.appendPathPart(this.getSessionStatePath(), LAYER_STATE_PATH);
    }

    protected static LayerStateFilename parseLayerStateFilename(String filename)
    {
        String filenameWithoutSuffix = WWIO.replaceSuffix(filename, "");
        String[] tokens = filenameWithoutSuffix.split(LAYER_STATE_FILENAME_DELIMITER);

        if (tokens == null || tokens.length < 2)
            return null;

        Integer index = WWUtil.makeInteger(tokens[0]);
        if (index == null)
            return null;

        String className = tokens[1].trim();

        return new LayerStateFilename(className, index);
    }

    protected static String composeLayerStateFilename(String className, int index)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(index);
        sb.append(LAYER_STATE_FILENAME_DELIMITER);
        sb.append(className);
        sb.append(".xml");

        return sb.toString();
    }
}
