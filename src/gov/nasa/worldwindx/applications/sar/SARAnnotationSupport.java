/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 * Handles SAR annotations
 * @author Patrick Murris
 * @version $Id: SARAnnotationSupport.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARAnnotationSupport
{
    private AnnotationLayer annotationLayer;
    private AnnotationAttributes defaults;
    private WorldWindow wwd;
    private SARAnnotation lastPickedObject;
    private SARAnnotation currentAnnotation;
    private ScreenAnnotation helpMessageAnnotation;
    private Color savedBorderColor;
    private String angleFormat;


    public SARAnnotationSupport()
    {
        this.annotationLayer = new AnnotationLayer();

        this.helpMessageAnnotation = new ScreenAnnotation("", new Point(0, 0));
        this.helpMessageAnnotation.getAttributes().setFrameShape(AVKey.SHAPE_NONE);
        this.helpMessageAnnotation.getAttributes().setInsets(new Insets(0, 0, 0, 0));
        this.helpMessageAnnotation.getAttributes().setDrawOffset(new Point(0, -20));
        this.helpMessageAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
        this.helpMessageAnnotation.getAttributes().setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        this.helpMessageAnnotation.getAttributes().setFont(Font.decode("Arial-Bold-14"));
        this.helpMessageAnnotation.getAttributes().setTextColor(Color.YELLOW);
        this.helpMessageAnnotation.getAttributes().setBackgroundColor(Color.BLACK);
        this.helpMessageAnnotation.getAttributes().setSize(new Dimension(220, 0));
        this.annotationLayer.addAnnotation(this.helpMessageAnnotation);

        this.defaults = new AnnotationAttributes();
        this.defaults.setHighlightScale(1.1);

    }

    /**
     * Set the WorldWindow reference. Adds an annotation layer and a SelectListener to WW.
     * @param wwd the WorldWindow reference.
     */
    public void setWwd(WorldWindow wwd)
    {
        this.wwd = wwd;
        // Add annotation layer
        this.wwd.getModel().getLayers().add(this.annotationLayer);

        // Add a select listener to select or highlight annotations on rollover
        this.wwd.addSelectListener(new SelectListener()
        {
            private BasicDragger dragger = new BasicDragger(SARAnnotationSupport.this.wwd);

            public void selected(SelectEvent event)
            {
                // Select/unselect on left click on annotations
                if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                {
                    if (event.hasObjects())
                    {
                        if (event.getTopObject() instanceof Annotation)
                        {
                            // Check for text or url
                            PickedObject po = event.getTopPickedObject();
                            if(po.getValue(AVKey.TEXT) != null)
                            {
                                //System.out.println("Text: \"" + po.getValue(AVKey.TEXT) + "\" Hyperlink: "  + po.getValue(AVKey.URL));
                                //if(SARAnnotationSupport.this.currentAnnotation == event.getTopObject())
                                //    return;
                            }
                            // Left click on an annotation - select
                            select(event.getTopObject());

                        }
                    }
                }
                // Edit annotation on double click
                else if (event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK))
                {
                    if (event.hasObjects())
                    {
                        if (event.getTopObject() instanceof Annotation)
                        {
                            edit((SARAnnotation)event.getTopObject());
                        }
                    }
                }
                // Highlight on rollover
                else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
                {
                    highlight(event.getTopObject());
                }
                // Have drag events drag the selected object.
                else if (event.getEventAction().equals(SelectEvent.DRAG_END)
                        || event.getEventAction().equals(SelectEvent.DRAG))
                {
                    if (event.hasObjects())
                    {
                        // If selected annotation delegate dragging computations to a dragger.
                        if(event.getTopObject() == SARAnnotationSupport.this.currentAnnotation)
                        {
                            this.dragger.selected(event);
                            // Update help text when dragging
                            updateHelpMessage(SARAnnotationSupport.this.currentAnnotation);
                            // Mark the owner track as dirty.
                            if (SARAnnotationSupport.this.currentAnnotation.getOwner() != null)
                                SARAnnotationSupport.this.currentAnnotation.getOwner().markDirty();
                        }
                    }

                    // We missed any roll-over events while dragging, so highlight any under the cursor now,
                    // or de-highlight the dragged shape if it's no longer under the cursor.
                    if (event.getEventAction().equals(SelectEvent.DRAG_END))
                    {
                        PickedObjectList pol = SARAnnotationSupport.this.wwd.getObjectsAtCurrentPosition();
                        if (pol != null)
                        {
                            highlight(pol.getTopObject());
                            SARAnnotationSupport.this.wwd.redraw();
                        }
                    }
                }

            }
        });

        // Add a mouse listener to deselect annotation on click anywhere - including terrain
        this.wwd.getInputHandler().addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent mouseEvent)
            {
                if (currentAnnotation != null && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    select(null);
                    mouseEvent.consume();
                    getWwd().redraw();
                }
            }
        });

        // Listen for angle format change
        this.wwd.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                if (propertyChangeEvent.getPropertyName() == SARKey.ANGLE_FORMAT)
                    setAngleFormat((String)propertyChangeEvent.getNewValue());
            }
        });
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public String getAngleFormat()
    {
        return this.angleFormat;
    }

    public void setAngleFormat(String format)
    {
        this.angleFormat = format;
        updateHelpMessage(this.lastPickedObject);
    }
    
    private void select(Object o)
    {
        if(this.currentAnnotation != null)
        {
            // Unselect current
            //this.currentAnnotation.getAttributes().setHighlighted(false);
            this.currentAnnotation.getAttributes().setBorderColor(SARAnnotationSupport.this.savedBorderColor);
        }
        if(o != null && o instanceof SARAnnotation && this.currentAnnotation != o)
        {
            // Select new one if not current one already
            this.currentAnnotation = (SARAnnotation)o;
            //this.currentAnnotation.getAttributes().setHighlighted(true);
            this.savedBorderColor = this.currentAnnotation.getAttributes().getBorderColor();
            this.currentAnnotation.getAttributes().setBorderColor(Color.YELLOW);
        }
        else
        {
            // Clear current annotation
            this.currentAnnotation = null; // switch off
        }

    }

    private void highlight(Object o)
    {
        // Manage highlighting of Annotations.
        if (this.lastPickedObject == o)
            return; // same thing selected

        // Turn off highlight if on.
        if (this.lastPickedObject != null) // && this.lastPickedObject != this.currentAnnotation)
        {
            this.lastPickedObject.getAttributes().setHighlighted(false);
            this.lastPickedObject = null;
            updateHelpMessage(null);  // Clear help text
        }

        // Turn on highlight if object selected.
        if (o != null && o instanceof SARAnnotation)
        {
            this.lastPickedObject = (SARAnnotation) o;
            this.lastPickedObject.getAttributes().setHighlighted(true);
            updateHelpMessage(this.lastPickedObject);  // Update help text
        }
    }

    private void updateHelpMessage(SARAnnotation annotation)
    {
        if (annotation != null)
        {
            Position pos = annotation.getPosition();
            this.helpMessageAnnotation.getAttributes().setVisible(true);
            this.helpMessageAnnotation.setText(String.format("Lat %s Lon %s",
                    SAR2.formatAngle(angleFormat, pos.getLatitude()),
                    SAR2.formatAngle(angleFormat, pos.getLongitude())));
            // set help message screen position - follow annotation
            Vec4 surfacePoint = this.getWwd().getSceneController().getTerrain().getSurfacePoint(
                    pos.getLatitude(), pos.getLongitude());
            if (surfacePoint == null)
            {
                Globe globe = this.getWwd().getModel().getGlobe();
                surfacePoint = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                        globe.getElevation(pos.getLatitude(), pos.getLongitude()));
            }
            Vec4 screenPoint = this.getWwd().getView().project(surfacePoint);
            if (screenPoint != null)
                this.helpMessageAnnotation.setScreenPoint(new Point((int)screenPoint.x,  (int)screenPoint.y));
        }
        else
        {
            this.helpMessageAnnotation.getAttributes().setVisible(false);
        }
    }

    /**
     * Add a new annotation in the screen center.
     * @param text the <code>Annotation</code> text.
     * @param owner if not null, the SARTrack to add the annotation to.
     *              annotation's border and text will be colored according to the owner SARTrack.
     */
    public void addNew(String text, SARTrack owner)
    {
        if (text == null)
            text = showAnnotationDialog("Add New Annotation", null);

        OrbitView view = (OrbitView)this.wwd.getView();
        if(text != null && text.length() > 0 && view != null)
        {
            Position centerPosition = new Position(view.getCenterPosition(), 0);
            SARAnnotation annotation = new SARAnnotation(text, centerPosition);
            addNew(annotation, owner);
            select(annotation);
        }
    }

    public void addNew(SARAnnotation annotation, SARTrack owner)
    {
        if (annotation != null)
        {
            annotation.getAttributes().setDefaults(this.defaults);
            // Reduce annotation distance scaling effect
            annotation.getAttributes().setDistanceMinScale(0.7);
            annotation.getAttributes().setDistanceMaxScale(1.3);
            if (owner != null)
            {
                annotation.setOwner(owner);
                annotation.getAttributes().setTextColor(owner.getColor());
                //annotation.getAttributes().setBorderColor(color);
            }
            add(annotation);
        }
    }

    /**
     * Multi line input dialog. Returns the input text or null if canceled.
     * @param title the dialog window title.
     * @param text the initial text.
     * @return the input text or null if the dialog was canceled.
     */
    private String showAnnotationDialog(String title, String text)
    {
        final JTextArea textArea = new JTextArea(5, 10);
        if (text != null)
            textArea.setText(text);
        // Focus to text area from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6420212
        textArea.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent he) {
                if ((he.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (textArea.isShowing()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                textArea.requestFocus();
                            }
                        });
                    }
                }
            }
        });
        int dialogResult = -1;
        JScrollPane pane = new JScrollPane(textArea);
        if (text != null && text.length() > 0)
        {
            Object[] options = {"Save", "Delete", "Cancel"};
            dialogResult = JOptionPane.showOptionDialog(null, new Object[] {"Enter text", pane}, title,
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        else
        {
            dialogResult = JOptionPane.showOptionDialog(null, new Object[] {"Enter text", pane}, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        }
        String newText = null;
        if (dialogResult == JOptionPane.OK_OPTION || dialogResult == JOptionPane.YES_OPTION)
            newText = textArea.getText();
        if (dialogResult == JOptionPane.NO_OPTION)
            newText = "";
        return newText;
    }

    /**
     * Add an annotation.
     * @param annotation the annotation to add.
     */
    public void add(SARAnnotation annotation)
    {
        if(annotation != null)
        {
            annotationLayer.addAnnotation(annotation);
            // Mark the SARTrack as dirty.
            if (annotation.getOwner() != null)
                annotation.getOwner().markDirty();
        }
    }

    /**
     * Edit an annotation.
     * @param annotation the Annotation to be edited.
     */
    public void edit(SARAnnotation annotation)
    {
        if(annotation != null)
        {
            String text = showAnnotationDialog("Edit Annotation", annotation.getText());
            if(text != null)
            {
                if (text.length() > 0)
                {
                    annotation.setText(text);
                    // Mark the owner track as dirty.
                    if (annotation.getOwner() != null)
                        annotation.getOwner().markDirty();
                }
                else
                {
                    // The remove operation will mark the
                    // owner track as tirty.
                    this.remove(annotation);
                }
            }
            this.wwd.redraw();
        }
    }

    /**
     * Remove an annotation.
     * @param annotation the annotation to be removed.
     */
    public void remove(SARAnnotation annotation)
    {
        if (annotation != null)
        {
            annotationLayer.removeAnnotation(annotation);
            if (currentAnnotation == annotation)
                currentAnnotation = null;
            // Mark the SARTrack as dirty.
            if (annotation.getOwner() != null)
                annotation.getOwner().markDirty();
        }

    }

    public void removeAnnotationsForTrack(SARTrack owner)
    {
        for (SARAnnotation sa : getAnnotationsForTrack(owner))
        {
            if (sa != null)
                remove(sa);
        }
    }

    /**
     * Get current annotation.
     * @return current annotation.
     */
    public SARAnnotation getCurrent()
    {
        return currentAnnotation;
    }

    /**
     * Get the annotation collection from the RenderableLayer
     * @return an Annotation collection.
     */
    public Iterable<Annotation> getAnnotations()
    {
       return annotationLayer.getAnnotations();
    }

    public Iterable<SARAnnotation> getAnnotationsForTrack(SARTrack owner)
    {
        java.util.ArrayList<SARAnnotation> result = new java.util.ArrayList<SARAnnotation>();
        for (Annotation a : this.annotationLayer.getAnnotations())
        {
            if (a != null && a instanceof SARAnnotation)
            {
                if (owner == ((SARAnnotation) a).getOwner())
                    result.add((SARAnnotation) a);
            }
        }
        return result;
    }

    /**
     * Set annotations enabled state
     * @param state true if annotations should be enabled.
     */
    public void setEnabled(boolean state)
    {
        annotationLayer.setEnabled(state);
    }

    /**
     * Get the annotations enabled state.
     * @return true if annotations are enabled.
     */
    public boolean getEnabled()
    {
        return annotationLayer.isEnabled();
    }

    /**
     * Get the default attribute set used for all annotations.
     * @return the default attribute set used for all annotations.
     */
    public AnnotationAttributes getDefaults()
    {
        return this.defaults;
    }

    public void writeAnnotations(String filePath, SARTrack trackOwner) throws IOException
    {
        try
        {
            if (filePath != null)
            {
                SARAnnotationWriter writer = new SARAnnotationWriter(filePath);
                writer.writeAnnotations(getAnnotationsForTrack(trackOwner));
                writer.close();
            }
        }
        catch (javax.xml.parsers.ParserConfigurationException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (javax.xml.transform.TransformerException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public void readAnnotations(String filePath, SARTrack trackOwner) throws IOException
    {
        try
        {
            if (filePath != null)
            {
                SARAnnotationReader reader = new SARAnnotationReader();
                reader.readFile(filePath);
                for (SARAnnotation sa : reader.getSARAnnotations())
                    addNew(sa, trackOwner);
            }
        }
        catch (javax.xml.parsers.ParserConfigurationException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (org.xml.sax.SAXException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
