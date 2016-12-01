/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: RecordPanel.java 1582 2013-09-05 17:14:08Z dcollins $
 */
public class RecordPanel extends JPanel implements ActionListener
{
    protected Record record;
    protected WorldWindow wwd;
    protected List<Layer> layerList;
    protected Layer activeLayer;
    protected JCheckBox layerCheckBox;
    protected JComboBox layerComboBox;
    protected int statusCode;

    protected static final ExecutorService layerExecutor = Executors.newFixedThreadPool(4); // shared thread pool
    protected static final ImageIcon activityIcon = new ImageIcon(
        RecordPanel.class.getResource("/gov/nasa/worldwindx/applications/eurogeoss/images/activity-indicator-16.gif"));
    protected static final ImageIcon errorIcon = new ImageIcon(
        RecordPanel.class.getResource("/gov/nasa/worldwindx/applications/eurogeoss/images/error.gif"));
    protected static final int PANEL_PREFERRED_HEIGHT = 25;
    protected static final int COMBO_BOX_PREFERRED_WIDTH = 350;
    protected static final int STATUS_NO_ONLINE_RESOURCES = 0x1;
    protected static final int STATUS_INVALID_LAYER_NAME = 0x1 << 1;
    protected static final int STATUS_EXCEPTION_CREATING_LAYER = 0x1 << 2;

    public RecordPanel(Record record, WorldWindow wwd)
    {
        if (record == null)
        {
            String msg = Logging.getMessage("nullValue.RecordIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.record = record;
        this.wwd = wwd;

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.assembleLayers();
        this.layoutComponents();
    }

    protected void layoutComponents()
    {
        this.removeAll();

        String title = this.makeTitle();
        String toolTipText = this.makeToolTipText();

        if (this.layerList == null) // layer list is being retrieved
        {
            JLabel label = new JLabel(title, activityIcon, SwingConstants.LEFT);
            label.setToolTipText(toolTipText);
            label.setPreferredSize(new JCheckBox(title).getPreferredSize());
            this.add(label);
        }
        else if (this.layerList.size() == 0) // either no layers or no valid layers
        {
            JLabel label = new JLabel(title, errorIcon, SwingConstants.LEFT);
            label.setToolTipText(toolTipText);
            label.setPreferredSize(new JCheckBox(title).getPreferredSize());
            this.add(label);
        }
        else if (this.layerList.size() == 1) // one or more valid layers
        {
            this.activeLayer = layerList.get(0);
            this.layerCheckBox = new JCheckBox(title);
            this.layerCheckBox.setToolTipText(toolTipText);
            this.layerCheckBox.addActionListener(this);
            this.add(this.layerCheckBox);
        }
        else // more than one valid layer
        {
            this.activeLayer = layerList.get(0);
            this.layerCheckBox = new JCheckBox();
            this.layerCheckBox.addActionListener(this);
            this.layerComboBox = new JComboBox(this.layerList.toArray());
            this.layerComboBox.setPreferredSize(new Dimension(COMBO_BOX_PREFERRED_WIDTH, this.layerComboBox.getPreferredSize().height));
            this.layerComboBox.setMaximumSize(new Dimension(COMBO_BOX_PREFERRED_WIDTH, Integer.MAX_VALUE));
            this.layerComboBox.setToolTipText(toolTipText);
            this.layerComboBox.addActionListener(this);
            this.add(this.layerCheckBox);
            this.add(this.layerComboBox);
        }

        this.setPreferredSize(new Dimension(this.getPreferredSize().width, PANEL_PREFERRED_HEIGHT));
        this.validate();
    }

    protected String makeTitle()
    {
        return WWUtil.isEmpty(this.record.getTitle()) ? "Untitled" : this.record.getTitle();
    }

    protected String makeToolTipText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(this.makeTitle());

        if (this.statusCode != 0)
        {
            sb.append("<font color=\"red\">");

            if ((this.statusCode & STATUS_NO_ONLINE_RESOURCES) != 0)
            {
                sb.append("<br/>");
                sb.append("No layers");
            }

            if ((this.statusCode & STATUS_INVALID_LAYER_NAME) != 0
                || (this.statusCode & STATUS_EXCEPTION_CREATING_LAYER) != 0)
            {
                sb.append("<br/>");
                sb.append("Invalid layers");
            }

            sb.append("</font>");
        }

        sb.append("</html>");

        return sb.toString();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getSource().equals(this.layerCheckBox))
        {
            if (this.layerCheckBox.isSelected())
            {
                this.wwd.getModel().getLayers().add(this.activeLayer);
            }
            else
            {
                this.wwd.getModel().getLayers().remove(this.activeLayer);
            }
        }
        else if (actionEvent.getSource().equals(this.layerComboBox))
        {
            if (this.layerCheckBox.isSelected())
            {
                this.wwd.getModel().getLayers().remove(this.activeLayer);
            }

            this.activeLayer = (Layer) this.layerComboBox.getSelectedItem();

            if (this.layerCheckBox.isSelected())
            {
                this.wwd.getModel().getLayers().add(this.activeLayer);
            }
        }
    }

    protected void assembleLayers()
    {
        final Collection<OnlineResource> wmsResources = this.record.getWmsOnlineResources();
        if (wmsResources != null && wmsResources.size() > 0)
        {
            layerExecutor.submit(new Runnable()
            {
                public void run()
                {
                    final ArrayList<Layer> layers = new ArrayList<Layer>();
                    for (OnlineResource onlineResource : wmsResources)
                    {
                        addLayersForOnlineResource(onlineResource, layers);
                    }

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            layerList = layers;
                            layoutComponents();
                        }
                    });
                }
            });
        }
        else
        {
            this.layerList = Collections.emptyList();
            this.statusCode |= STATUS_NO_ONLINE_RESOURCES;
            String msg = "Record has no WMS online resources " + this.record.getTitle();
            Logging.logger().warning(msg);
        }
    }

    protected void addLayersForOnlineResource(OnlineResource onlineResource, Collection<Layer> layerList)
    {
        try
        {
            WMSCapabilities caps = (WMSCapabilities) WorldWind.getSessionCache().get(onlineResource.getLinkage());
            if (caps == null)
            {
                caps = WMSCapabilities.retrieve(new URI(onlineResource.getLinkage()));
                caps.parse();
                WorldWind.getSessionCache().put(onlineResource.getLinkage(), caps);
            }

            if (WWUtil.isEmpty(onlineResource.getName())) // OnlineResource does not name a layer; add all WMS layers.
            {
                for (WMSLayerCapabilities layerCaps : caps.getNamedLayers())
                {
                    layerList.add(this.createLayer(caps, layerCaps.getName()));
                }

                String msg = "Online resource has no name " + onlineResource + ", using all layers";
                Logging.logger().warning(msg);
            }
            else // OnlineResource names one layer; attempt to identify that layer in the WMS capabilities doc.
            {
                // Try using the online resource name as a layer name. If the online resource name does not indicate a
                // layer name, attempt to find a layer who's name or title matches a portion of the online resource
                // name, or vice versa.
                WMSLayerCapabilities layerCaps = caps.getLayerByName(onlineResource.getName());
                if (layerCaps == null)
                {
                    layerCaps = this.findLayerMatchingName(caps, onlineResource.getName());
                }

                if (layerCaps == null)
                {
                    this.statusCode |= STATUS_INVALID_LAYER_NAME;
                    String msg = "Unable to find layer for online resource " + onlineResource;
                    Logging.logger().warning(msg);
                }
                else
                {
                    layerList.add(this.createLayer(caps, layerCaps.getName()));
                }
            }
        }
        catch (Exception e)
        {
            this.statusCode |= STATUS_EXCEPTION_CREATING_LAYER;
            String msg = "Exception creating layer for online resource " + onlineResource;
            Logging.logger().log(Level.WARNING, msg); // Log just the message at the warning level.
            Logging.logger().log(Level.FINEST, msg, e); // Log the message and the exception at the fine level.
        }
    }

    protected Layer createLayer(WMSCapabilities caps, String layerName)
    {
        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        AVList params = new AVListImpl();
        params.setValue(AVKey.LAYER_NAMES, layerName);
        params.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        params.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        params.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
        Layer layer = (Layer) factory.createFromConfigSource(caps, params);
        layer.setOpacity(0.6);

        return layer;
    }

    protected WMSLayerCapabilities findLayerMatchingName(WMSCapabilities caps, String name)
    {
        for (WMSLayerCapabilities layer : caps.getNamedLayers())
        {
            if (layer.getName().contains(name) || name.contains(layer.getName())
                || layer.getTitle().contains(name) || name.contains(layer.getTitle()))
            {
                return layer;
            }
        }

        return null;
    }
}
