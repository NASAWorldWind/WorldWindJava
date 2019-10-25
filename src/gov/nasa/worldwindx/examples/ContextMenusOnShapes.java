/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Illustrates how to attach context (popup) menus to shapes. The example creates several <code>{@link
 * PointPlacemark}s</code> and assigns each of them a context-menu definition. When the user presses the right mouse
 * button while the cursor is on a placemark, the placemark's context menu is shown and the user may select an item in
 * it.
 *
 * @author tag
 * @version $Id: ContextMenusOnShapes.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ContextMenusOnShapes extends ApplicationTemplate {

    /**
     * The Controller listens for selection events and either highlights a selected item or shows its context menu.
     */
    protected static class ContextMenuController implements SelectListener {

        protected PointPlacemark lastPickedPlacemark = null;

        @Override
        public void selected(SelectEvent event) {
            try {
                if (event.getEventAction().equals(SelectEvent.ROLLOVER)) {
                    highlight(event, event.getTopObject());
                } else if (event.getEventAction().equals(SelectEvent.RIGHT_PRESS)) // Could do RIGHT_CLICK instead
                {
                    showContextMenu(event);
                }
            } catch (Exception e) {
                Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
            }
        }

        @SuppressWarnings({"UnusedDeclaration"})
        protected void highlight(SelectEvent event, Object o) {
            if (this.lastPickedPlacemark == o) {
                return; // same thing selected
            }
            // Turn off highlight if on.
            if (this.lastPickedPlacemark != null) {
                this.lastPickedPlacemark.setHighlighted(false);
                this.lastPickedPlacemark = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof PointPlacemark) {
                this.lastPickedPlacemark = (PointPlacemark) o;
                this.lastPickedPlacemark.setHighlighted(true);
            }
        }

        protected void showContextMenu(SelectEvent event) {
            if (!(event.getTopObject() instanceof PointPlacemark)) {
                return;
            }

            // See if the top picked object has context-menu info defined. Show the menu if it does.
            Object o = event.getTopObject();
            if (o instanceof AVList) // Uses an AVList in order to be applicable to all shapes.
            {
                AVList params = (AVList) o;
                ContextMenuInfo menuInfo = (ContextMenuInfo) params.getValue(ContextMenu.CONTEXT_MENU_INFO);
                if (menuInfo == null) {
                    return;
                }

                if (!(event.getSource() instanceof Component)) {
                    return;
                }

                ContextMenu menu = new ContextMenu((Component) event.getSource(), menuInfo);
                menu.show(event.getMouseEvent());
            }
        }
    }

    /**
     * The ContextMenu class implements the context menu.
     */
    protected static class ContextMenu {

        public static final String CONTEXT_MENU_INFO = "ContextMenuInfo";

        protected ContextMenuInfo ctxMenuInfo;
        protected Component sourceComponent;
        protected JMenuItem menuTitleItem;
        protected ArrayList<JMenuItem> menuItems = new ArrayList<>();

        public ContextMenu(Component sourceComponent, ContextMenuInfo contextMenuInfo) {
            this.sourceComponent = sourceComponent;
            this.ctxMenuInfo = contextMenuInfo;

            this.makeMenuTitle();
            this.makeMenuItems();
        }

        protected void makeMenuTitle() {
            this.menuTitleItem = new JMenuItem(this.ctxMenuInfo.menuTitle);
        }

        protected void makeMenuItems() {
            for (ContextMenuItemInfo itemInfo : this.ctxMenuInfo.menuItems) {
                this.menuItems.add(new JMenuItem(new ContextMenuItemAction(itemInfo)));
            }
        }

        public void show(final MouseEvent event) {
            JPopupMenu popup = new JPopupMenu();

            popup.add(this.menuTitleItem);

            popup.addSeparator();

            for (JMenuItem subMenu : this.menuItems) {
                popup.add(subMenu);
            }

            popup.show(sourceComponent, event.getX(), event.getY());
        }
    }

    /**
     * The ContextMenuInfo class specifies the contents of the context menu.
     */
    protected static class ContextMenuInfo {

        protected String menuTitle;
        protected ContextMenuItemInfo[] menuItems;

        public ContextMenuInfo(String title, ContextMenuItemInfo[] menuItems) {
            this.menuTitle = title;
            this.menuItems = menuItems;
        }
    }

    /**
     * The ContextMenuItemInfo class specifies the contents of one entry in the context menu.
     */
    protected static class ContextMenuItemInfo {

        protected String displayString;

        public ContextMenuItemInfo(String displayString) {
            this.displayString = displayString;
        }
    }

    /**
     * The ContextMenuItemAction responds to user selection of a context menu item.
     */
    public static class ContextMenuItemAction extends AbstractAction {

        protected ContextMenuItemInfo itemInfo;

        public ContextMenuItemAction(ContextMenuItemInfo itemInfo) {
            super(itemInfo.displayString);

            this.itemInfo = itemInfo;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            System.out.println(this.itemInfo.displayString); // Replace with application's menu-item response.
        }
    }

    // The code below makes and displays some placemarks. The context menu info for each placemark is also specified.
    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setAntiAliasHint(WorldWind.ANTIALIAS_FASTEST);
            attrs.setLineMaterial(Material.WHITE);
            attrs.setLineWidth(2d);
            attrs.setImageAddress("images/pushpins/push-pin-yellow.png");
            attrs.setScale(0.6);
            attrs.setImageOffset(new Offset(19d, 11d, AVKey.PIXELS, AVKey.PIXELS));

            PointPlacemarkAttributes highlightAttrs = new PointPlacemarkAttributes(attrs);
            highlightAttrs.setScale(0.7);

            ContextMenuItemInfo[] itemActionNames = new ContextMenuItemInfo[]{
                new ContextMenuItemInfo("Do This"),
                new ContextMenuItemInfo("Do That"),
                new ContextMenuItemInfo("Do the Other Thing"),};

            PointPlacemark pp = new PointPlacemark(Position.fromDegrees(28, -102, 1e4));
            pp.setAttributes(attrs);
            pp.setHighlightAttributes(highlightAttrs);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pp.setValue(ContextMenu.CONTEXT_MENU_INFO, new ContextMenuInfo("Placemark A", itemActionNames));
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(29, -104, 2e4));
            pp.setAttributes(attrs);
            pp.setHighlightAttributes(highlightAttrs);
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pp.setValue(ContextMenu.CONTEXT_MENU_INFO, new ContextMenuInfo("Placemark B", itemActionNames));
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(30, -104.5, 2e4));
            pp.setAttributes(attrs);
            pp.setHighlightAttributes(highlightAttrs);
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pp.setValue(ContextMenu.CONTEXT_MENU_INFO, new ContextMenuInfo("Placemark C", itemActionNames));
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(28, -104.5, 2e4));
            pp.setAttributes(attrs);
            pp.setHighlightAttributes(highlightAttrs);
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pp.setValue(ContextMenu.CONTEXT_MENU_INFO, new ContextMenuInfo("Placemark D", itemActionNames));
            layer.addRenderable(pp);

            // Create a placemark that uses all default values.
            pp = new PointPlacemark(Position.fromDegrees(30, -103.5, 2e3));
            pp.setValue(ContextMenu.CONTEXT_MENU_INFO, new ContextMenuInfo("Placemark E", itemActionNames));
            layer.addRenderable(pp);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Set up the context menu
            ContextMenuController contextMenuController = new ContextMenuController();
            getWwd().addSelectListener(contextMenuController);
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Context Menus on Shapes", AppFrame.class);
    }
}
