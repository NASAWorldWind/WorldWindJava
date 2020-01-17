package gov.nasa.worldwind.animation;

import java.util.*;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;

public class AnimatedObjectController implements RenderingListener, SelectListener {

    private final WorldWindow wwd;
    private final ArrayList<AnimatedObject> animObjects;
    private Globe globe;
    private boolean started = false;
    private Animatable selectedObject;
    private AnnotationAttributes metaAttrs;
    private RenderableLayer objectLayer;
    private RenderableLayer annotationLayer;

    public AnimatedObjectController(WorldWindow wwd, RenderableLayer objectLayer, RenderableLayer annotationLayer) {
        this.wwd = wwd;
        this.animObjects = new ArrayList<>();
        this.objectLayer = objectLayer;
        this.annotationLayer = annotationLayer;
        metaAttrs = new AnnotationAttributes();
        metaAttrs.setCornerRadius(0);
        metaAttrs.setInsets(new Insets(4, 4, 4, 4));
        metaAttrs.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        metaAttrs.setTextColor(Color.WHITE);
        metaAttrs.setBorderColor(Color.yellow);
        metaAttrs.setBorderWidth(1);
        metaAttrs.setLeaderGapWidth(4);
        metaAttrs.setDrawOffset(new Point(0, 40));
    }

    @Override
    public void stageChanged(RenderingEvent event) {
        if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING)) {
            if (globe == null) {
                globe = this.wwd.getView().getGlobe();
            }
            if (globe != null) {
                if (started) {
                    animObjects.forEach((ao) -> {
                        ao.stepAnimation(wwd.getView().getGlobe());
                    });
                } else {
                    animObjects.forEach((ao) -> {
                        ao.startAnimation(wwd.getView().getGlobe());
                    });
                    started = true;
                }
            }
        }
    }

    public void addObject(AnimatedObject ao) {
        this.animObjects.add(ao);
    }

    public void startAnimations() {
        this.wwd.addRenderingListener(this);
        this.wwd.addSelectListener(this);
    }

    private void showMetadata(Object o) {
        if (this.selectedObject == o) {
            return; // same thing selected
        }

        if (o instanceof Animatable) {
            this.selectedObject = (Animatable) o;
            Object prevNote = this.selectedObject.getField(AVKey.ANIMATION_ANNOTATION);
            if (prevNote != null) {
                ((GlobeAnnotation) prevNote).getAttributes().setVisible(true);
            } else {
                String metadata = this.selectedObject.getField(AVKey.ANIMATION_META_DATA).toString();
                if (metadata != null) {
                    GlobeAnnotation note = new GlobeAnnotation(metadata, this.selectedObject.getPosition(), this.metaAttrs);
                    this.annotationLayer.addRenderable(note);
                    note.getAttributes().setVisible(true);
                    this.selectedObject.setField(AVKey.ANIMATION_ANNOTATION, note);
                }
            }
        }
    }

    @Override
    public void selected(SelectEvent event) {
        // System.out.println(event,event.);
        if (event.getEventAction().equals(SelectEvent.ROLLOVER)) {
            showMetadata(event.getTopObject());
        }
    }
}
