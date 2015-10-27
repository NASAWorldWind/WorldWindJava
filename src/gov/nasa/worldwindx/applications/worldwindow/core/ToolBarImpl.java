/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwindx.applications.worldwindow.features.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;

/**
 * @author tag
 * @version $Id: ToolBarImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ToolBarImpl extends AbstractFeature implements ToolBar
{
    private GradientToolBar toolBar;

    public ToolBarImpl(Registry registry)
    {
        super("Tool Bar", Constants.TOOL_BAR, registry);
    }

    public void initialize(Controller controller)
    {
        this.toolBar = new GradientToolBar();
        this.toolBar.setLayout(new GridLayout(1, 0));
        this.toolBar.setRollover(false);
        this.toolBar.setFloatable(false);
        this.toolBar.initialize(controller);

        this.toolBar.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                for (Component c : toolBar.getComponents())
                {
                    if (c instanceof ToolBarButton)
                        ((ToolBarButton) c).updateSize();
                }
            }
        });
    }

    public JToolBar getJToolBar()
    {
        return this.toolBar;
    }

    public void addFeature(Feature feature)
    {
        ToolBarButton btn = new ToolBarButton(feature);
        btn.initialize(controller);
        this.toolBar.add(btn);
    }

    public static class GradientToolBar extends JToolBar implements Initializable
    {
        private ToolBarButton rolloverComponent;
        private MouseListener mouseListener;

        public GradientToolBar()
        {
            setOpaque(false);
            setBorderPainted(false);
        }

        public void initialize(Controller controller)
        {
            this.mouseListener = new MouseListener()
            {
                public void mouseClicked(MouseEvent mouseEvent)
                {
                }

                public void mousePressed(MouseEvent mouseEvent)
                {
                }

                public void mouseReleased(MouseEvent mouseEvent)
                {
                }

                public void mouseEntered(MouseEvent mouseEvent)
                {
                    if (mouseEvent.getSource() instanceof ToolBarButton)
                        rolloverComponent = (ToolBarButton) mouseEvent.getSource();
                    else
                        rolloverComponent = null;
                    repaint();
                }

                public void mouseExited(MouseEvent mouseEvent)
                {
                    rolloverComponent = null;
                    repaint();
                }
            };
        }

        public boolean isInitialized()
        {
            return this.mouseListener != null;
        }

        public void add(ToolBarButton button)
        {
            button.setBorderPainted(false);
            button.setOpaque(false);
            button.setHideActionText(true);
            button.addMouseListener(this.mouseListener);

            super.add(button);
        }

        public void setRolloverComponent(Component c)
        {
            this.rolloverComponent = c != null && c instanceof ToolBarButton ? (ToolBarButton) c : null;
            if (this.rolloverComponent != null)
                this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;

            // Creates a two-stops gradient
            GradientPaint p;
            p = new GradientPaint(0, 0, new Color(0x6b89c8), 0, getHeight(), new Color(0x000000));

            // Saves the state
            Paint oldPaint = g2.getPaint();

            // Paints the background
            g2.setPaint(p);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Restores the state
            g2.setPaint(oldPaint);
        }

        @Override
        protected void paintChildren(Graphics g)
        {
            super.paintChildren(g);

            if (this.rolloverComponent != null)
                this.drawButtonLabel(this.rolloverComponent, (Graphics2D) g);
        }

        public void drawButtonLabel(ToolBarButton c, Graphics2D g)
        {
            Paint oldPaint = g.getPaint();

            Font font = Font.decode("Arial-Bold-14");
            g.setFont(font);
            Rectangle2D r = g.getFontMetrics().getStringBounds(c.getActionCommand(), g);

            double x = c.getLocation().x + 1;
            double y = c.getLocation().y + c.getHeight() - (r.getHeight() + 4);
            double xs = x + r.getX() + 10;
            double ys = y + r.getHeight();

            LinearGradientPaint lg = new LinearGradientPaint(
                new Point2D.Double(x, y),
                new Point2D.Double(x, y + r.getHeight()),
                new float[] {0f, 1f},
                new Color[] {Color.BLACK, new Color(50, 50, 50)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE);
            g.setPaint(lg);

            RoundRectangle2D.Double bg = new RoundRectangle2D.Double(x, y, r.getWidth() + 20, r.getHeight() + 4, 20d,
                20d);
            g.fill(bg);

            g.setPaint(Color.WHITE);
            g.drawString(c.getActionCommand(), (float) xs, (float) ys);

            g.setPaint(oldPaint);
        }
    }

    public static class ToolBarButton extends JButton implements Initializable
    {
        protected boolean initialized = false;
        protected ImageIcon originalIcon;
        protected ImageIcon currentIcon;
        protected int iconSize = Configuration.getIntegerValue(Constants.TOOL_BAR_ICON_SIZE_PROPERTY, 52);

        public ToolBarButton(Feature feature)
        {
            super(feature);

            this.setOpaque(false);
            this.originalIcon = (ImageIcon) feature.getValue(Action.LARGE_ICON_KEY);
            this.setIconSize(iconSize + this.getInsets().left + this.getInsets().right);
            if (feature.getValue(Constants.ACTION_COMMAND) != null)
                this.setActionCommand((String) feature.getValue(Constants.ACTION_COMMAND));
        }

        public void initialize(final Controller controller)
        {
            // Set up to learn of changes to or by the feature
            this.getFeature().addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                {
                    repaint();
                }
            });

            this.initialized = true;
        }

        public boolean isInitialized()
        {
            return this.initialized;
        }

        public Feature getFeature()
        {
            return (Feature) this.getAction();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
//        this.setIconSize(this.getSize().width);
            super.paintComponent(g);

            if (this.getFeature().isOn())
                drawDot(g);
        }

        // Draws a small image above the button to indicate that the button's feature is currently active or selected.
        private void drawDot(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;

            // Saves the state
            Paint oldPaint = ((Graphics2D) g).getPaint();

            float r = 3f;
            float x = this.getWidth() / 2f - r;
            float y = 1f;

            float cx = this.getWidth() / 2f - 1f;
            float cy = y + r - 1f;

            RadialGradientPaint p = new RadialGradientPaint(cx, cy, r,
                new float[] {0f, 1f}, new Color[] {Color.WHITE, Color.GREEN});
            g2.setPaint(p);

            Ellipse2D.Float dot = new Ellipse2D.Float(x, y, 2f * r, 2f * r);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fill(dot);

            // Restores the state
            g2.setPaint(oldPaint);
        }

        // Updates the button's size when the window is resized.
        public void updateSize()
        {
            this.setIconSize(this.getWidth());
        }

        public void setIconSize(int size)
        {
            size -= (this.getInsets().left + this.getInsets().right);
            if (this.currentIcon != null && this.currentIcon.getIconWidth() == size
                && this.currentIcon.getIconHeight() == size)
                return;

            size = Math.min(52, size);
            size = Math.max(16, size);

            this.currentIcon = new ImageIcon(
                this.originalIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            this.getAction().putValue(Action.LARGE_ICON_KEY, this.currentIcon);
        }
    }
}
