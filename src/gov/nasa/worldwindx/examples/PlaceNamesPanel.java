/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.placename.PlaceNameService;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

/**
 * @author jparsons
 * @version $Id: PlaceNamesPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PlaceNamesPanel extends JPanel implements ItemListener
{
    List<PlaceNameService> nameServices;
    PlaceNameLayer nameLayer;
    WorldWindow wwd;
    ArrayList<JCheckBox> cbList = new ArrayList<JCheckBox>();

    public PlaceNamesPanel(WorldWindow wwd)
    {
        super(new BorderLayout());
        this.wwd=wwd;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Object layer : layers)
        {
            if (layer instanceof PlaceNameLayer)
            {
                nameLayer = (PlaceNameLayer) layer;
                break;
            }
        }

        if (nameLayer !=null)
        {
            nameServices = nameLayer.getPlaceNameServiceSet().getServices();
            this.makePanel();
        }
    }
   
    private void makePanel()
    {
        JPanel namesPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        namesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel comboPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        
        for (PlaceNameService s: nameServices)
        {
          JCheckBox cb=new JCheckBox(s.getDataset(),true);
          cb.addItemListener(this);
          comboPanel.add(cb);
          cbList.add(cb);
        }

        namesPanel.add(comboPanel);
        this.add(namesPanel, BorderLayout.CENTER);
    }

    public void itemStateChanged(ItemEvent e)
    {

        for (PlaceNameService s: nameServices)
        {
            if (s.getDataset().equalsIgnoreCase(((JCheckBox)e.getSource()).getText()))
            {
                s.setEnabled(!s.isEnabled());
                break;
            }
        }


        update();
    }

    // Update worldwind
    private void update()
    {
        wwd.redraw();
    }
}
