/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

/**
 * Illustrates how to display an icon with an alarm state using a WorldWind <code>{@link WWIcon}</code>. This applies a
 * background image to an icon indicating a warning or an urgent condition, then varies the background image's scale
 * factor over time to make it flash or pulse.
 * <p>
 * This applies the background image by calling <code>{@link WWIcon#setBackgroundImage(Object)}</code>, and varies its
 * scale factor by calling <code>{@link WWIcon#setBackgroundScale(double)}</code>.
 *
 * @author Tom Gaskins
 * @version $Id: AlarmIcons.java 2139 2014-07-10 18:52:17Z tgaskins $
 */
public class AlarmIcons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private UserFacingIcon icon;
        private ArrayList<Action> alarmTypes = new ArrayList<Action>();

        public AppFrame()
        {
            super(true, true, false);

            IconLayer layer = new IconLayer();
            icon = new UserFacingIcon("src/images/32x32-icon-nasa.png",
                new Position(Angle.fromDegrees(38), Angle.fromDegrees(-116), 0));
            icon.setSize(new Dimension(64, 64));
            layer.addIcon(icon);
            ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);

            // Create bitmaps
            BufferedImage circleYellow = createBitmap(PatternFactory.PATTERN_CIRCLE, Color.YELLOW);
            BufferedImage circleRed = createBitmap(PatternFactory.PATTERN_CIRCLE, Color.RED);
            BufferedImage triangleYellow = createBitmap(PatternFactory.PATTERN_TRIANGLE_UP, Color.YELLOW);
            BufferedImage triangleRed = createBitmap(PatternFactory.PATTERN_TRIANGLE_UP, Color.RED);
            BufferedImage squareYellow = createBitmap(PatternFactory.PATTERN_SQUARE, Color.YELLOW);
            BufferedImage squareRed = createBitmap(PatternFactory.PATTERN_SQUARE, Color.RED);

            // Set up alarm types
            alarmTypes.add(new StaticAlarmAction("Static Yellow Circle", circleYellow, 2));
            alarmTypes.add(new StaticAlarmAction("Static Smaller Red Circle", circleRed, 1.5));
            alarmTypes.add(new StaticAlarmAction("Static Yellow Triangle", triangleYellow, 2.0));
            alarmTypes.add(new StaticAlarmAction("Static Bigger Red Triangle", triangleRed, 2.5));
            alarmTypes.add(new PulsingAlarmAction("Pulsing Yellow Circle", circleYellow, 100));
            alarmTypes.add(new PulsingAlarmAction("Pulsing Red Circle", circleRed, 100));
            alarmTypes.add(new PulsingAlarmAction("Rapidly Pulsing Yellow Square", squareYellow, 50));
            alarmTypes.add(new PulsingAlarmAction("Rapidly Pulsing Red Square", squareRed, 50));
            alarmTypes.add(new FlashingAlarmAction("Flashing Red Square", squareRed, 200));
            alarmTypes.add(new FlashingAlarmAction("Flashing Yellow Triangle", triangleYellow, 200));

            this.getControlPanel().add(this.makeControlPanel(), BorderLayout.SOUTH);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel(new GridLayout(0, 1, 10, 10));

            ButtonGroup bg = new ButtonGroup();
            for (Action a : alarmTypes)
            {
                final JRadioButton b = new JRadioButton(a);
                bg.add(b);
                controlPanel.add(b);
            }

            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.add(controlPanel, BorderLayout.NORTH);
            p.setBorder(new CompoundBorder(new TitledBorder("Alarm States"), new EmptyBorder(20, 10, 20, 10)));
            JPanel p2 = new JPanel(new BorderLayout(10, 10));
            p2.add(p);
            p2.setBorder(new EmptyBorder(10, 10, 10, 10));
            return p2;
        }

        // Create a blurred pattern bitmap
        private BufferedImage createBitmap(String pattern, Color color)
        {
            // Create bitmap with pattern
            BufferedImage image = PatternFactory.createPattern(pattern, new Dimension(128, 128), 0.7f,
                color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
            // Blur a lot to get a fuzzy edge
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            return image;
        }

        private class StaticAlarmAction extends AbstractAction
        {
            private Object bgIconPath;
            private double bgScale;

            private StaticAlarmAction(String name, Object bgIconPath, double bgScale)
            {
                super(name);
                this.bgIconPath = bgIconPath;
                this.bgScale = bgScale;
            }

            public void actionPerformed(ActionEvent e)
            {
                icon.setBackgroundImage(bgIconPath);
                icon.setBackgroundScale(bgScale);
                getWwd().redraw();
            }
        }

        private class PulsingAlarmAction extends AbstractAction
        {
            protected final Object bgIconPath;
            protected int frequency;
            protected int scaleIndex = 0;
            protected double[] scales = new double[] {1.25, 1.5, 1.75, 2, 2.25, 2.5, 2.75, 3, 3.25, 3.5, 3.25, 3,
                2.75, 2.5, 2.25, 2, 1.75, 1.5};
            protected Timer timer;

            private PulsingAlarmAction(String name, Object bgp, int frequency)
            {
                super(name);
                this.bgIconPath = bgp;
                this.frequency = frequency;
            }

            private PulsingAlarmAction(String name, Object bgp, int frequency, double[] scales)
            {
                this(name, bgp, frequency);
                this.scales = scales;
            }

            public void actionPerformed(ActionEvent e)
            {
                if (timer == null)
                {
                    timer = new Timer(frequency, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            icon.setBackgroundScale(scales[++scaleIndex % scales.length]);
                            getWwd().redraw();
                        }
                    });

                    ((JRadioButton) e.getSource()).addItemListener(new ItemListener()
                    {
                        public void itemStateChanged(ItemEvent e)
                        {
                            if (e.getStateChange() == ItemEvent.DESELECTED)
                                timer.stop();
                        }
                    });
                }
                icon.setBackgroundImage(bgIconPath);
                scaleIndex = 0;
                timer.start();
            }
        }

        private class FlashingAlarmAction extends PulsingAlarmAction
        {
            private FlashingAlarmAction(String name, Object bgp, int frequency)
            {
                super(name, bgp, frequency, new double[] {2, 0.5});
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Alarm Icons", AppFrame.class);
    }
}
