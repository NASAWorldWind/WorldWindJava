/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Illustrates how to display related geometry in an ESRI Shapefile as groups with shared attributes. This example loads
 * a Shapefile defining Earth's political boundaries, then groups those boundaries by continent: Africa, Europe, Asia,
 * Americas, Oceania and Antarctica. The outline color for each continent group can be set to either the default color
 * or the group's color by toggling a check box.
 *
 * @author dcollins
 * @version $Id: ShapefileAttributeGroups.java 2348 2014-09-25 23:35:46Z dcollins $
 */
public class ShapefileAttributeGroups extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
        implements ActionListener, ShapefileRenderable.AttributeDelegate
    {
        protected static String SHAPEFILE_PATH = "gov/nasa/worldwindx/examples/data/ShapefileAttributeGroups.xml";
        protected Map<Integer, AttributeGroup> groups = new LinkedHashMap<Integer, AttributeGroup>();

        public AppFrame()
        {
            this.setupGroups();
            this.loadShapefile();
        }

        protected void setupGroups()
        {
            // Create the mapping from region key to group name and color.
            // Continent codes are documented at http://unstats.un.org/unsd/methods/m49/m49regin.htm
            // Continent names and colors are based on https://en.wikipedia.org/wiki/Continent

            this.groups.put(2, new AttributeGroup("Africa", new Color(255, 198, 0)));
            this.groups.put(150, new AttributeGroup("Europe", new Color(255, 9, 84)));
            this.groups.put(142, new AttributeGroup("Asia", new Color(255, 133, 0)));
            this.groups.put(19, new AttributeGroup("Americas", new Color(79, 213, 33)));
            this.groups.put(9, new AttributeGroup("Oceania", new Color(193, 83, 220)));
            this.groups.put(0, new AttributeGroup("Antarctica", new Color(7, 152, 249)));

            // Setup the group control panel.

            JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 10));
            titlePanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Continents")));
            titlePanel.setToolTipText("Continents to highlight");
            this.getControlPanel().add(titlePanel, BorderLayout.SOUTH);

            JPanel groupPanel = new JPanel(new GridLayout(0, 1, 0, 5));
            groupPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            titlePanel.add(groupPanel);

            for (AttributeGroup group : this.groups.values())
            {
                JCheckBox jcb = new JCheckBox(group.getDisplayName(), group.isUseGroupColor());
                jcb.putClientProperty("group", group);
                jcb.addActionListener(this); // call actionPerformed when the check box selection state changes
                groupPanel.add(jcb);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            JCheckBox jcb = (JCheckBox) e.getSource();
            AttributeGroup group = (AttributeGroup) jcb.getClientProperty("group");
            group.setUseGroupColor(jcb.isSelected());
            this.getWwd().redraw();
        }

        protected void loadShapefile()
        {
            ShapefileLayerFactory factory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(
                AVKey.SHAPEFILE_LAYER_FACTORY);
            factory.setAttributeDelegate(this); // call assignAttributes for each shapefile record

            Layer layer = (Layer) factory.createFromConfigSource(SHAPEFILE_PATH, null);
            this.getWwd().getModel().getLayers().add(layer);
        }

        @Override
        public void assignAttributes(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord)
        {
            Number region = (Number) shapefileRecord.getAttributes().getValue("REGION");
            AttributeGroup group = this.groups.get(region.intValue());
            if (group != null)
            {
                group.addRecord(renderableRecord);
            }
        }
    }

    public static class AttributeGroup
    {
        protected String displayName;
        protected Material groupMaterial;
        protected Material defaultMaterial;
        protected boolean useGroupColor;
        protected ShapeAttributes attributes;

        public AttributeGroup(String displayName, Color color)
        {
            this.displayName = displayName;
            this.groupMaterial = new Material(color); // specifies the diffuse color
            this.useGroupColor = true;
        }

        public String getDisplayName()
        {
            return this.displayName;
        }

        public Color getColor()
        {
            return this.groupMaterial.getDiffuse();
        }

        public boolean isUseGroupColor()
        {
            return this.useGroupColor;
        }

        public void setUseGroupColor(boolean useGroupColor)
        {
            this.useGroupColor = useGroupColor;
            this.updateAttributes();
        }

        public void addRecord(ShapefileRenderable.Record record)
        {
            if (this.attributes == null) // use the first record to access the default attributes
            {
                this.attributes = record.getAttributes().copy(); // use default attrs as a template
                this.defaultMaterial = record.getAttributes().getOutlineMaterial(); // save the default material
                this.updateAttributes();
            }

            record.setAttributes(this.attributes); // use the group's attributes as the record's normal attributes
        }

        protected void updateAttributes()
        {
            if (this.attributes != null)
            {
                this.attributes.setOutlineMaterial(this.useGroupColor ? this.groupMaterial : this.defaultMaterial);
            }
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 30);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 30);
        start("WorldWind Shapefile Attribute Groups", AppFrame.class);
    }
}
