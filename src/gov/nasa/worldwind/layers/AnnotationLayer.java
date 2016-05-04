/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.render.*;

/**
 * The <code>AnnotationLayer</code> class manages a collection of {@link gov.nasa.worldwind.render.Annotation} objects
 * for rendering and picking. <code>AnnotationLayer</code> delegates to its internal {@link
 * gov.nasa.worldwind.render.AnnotationRenderer} for rendering and picking operations. The
 * <code>AnnotationRenderer</code> is specified by calling {@link #setAnnotationRenderer}.
 *
 * @author Patrick Murris
 * @version $Id: AnnotationLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.render.Annotation
 * @see gov.nasa.worldwind.render.AnnotationRenderer
 */
public class AnnotationLayer extends AbstractLayer
{
    protected final java.util.Collection<Annotation> annotations =
        new java.util.concurrent.ConcurrentLinkedQueue<Annotation>();
    protected Iterable<Annotation> annotationsOverride;
    private AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();

    /** Creates a new <code>AnnotationLayer</code> with an empty collection of Annotations. */
    public AnnotationLayer()
    {
    }

    /**
     * Adds the specified <code>annotation</code> to this layer's internal collection. If this layer's internal
     * collection has been overriden with a call to {@link #setAnnotations}, this will throw an exception.
     *
     * @param annotation Annotation to add.
     *
     * @throws IllegalArgumentException If <code>annotation</code> is null.
     * @throws IllegalStateException    If a custom Iterable has been specified by a call to <code>setAnnotations</code>.
     */
    public void addAnnotation(Annotation annotation)
    {
        if (annotation == null)
        {
            throw new IllegalArgumentException();
        }
        if (this.annotationsOverride != null)
        {
            throw new IllegalStateException();
        }

        this.annotations.add(annotation);
    }

    /**
     * Adds the contents of the specified <code>annotations</code> to this layer's internal collection. If this layer's
     * internal collection has been overriden with a call to {@link #setAnnotations}, this will throw an exception.
     *
     * @param annotations Annotations to add.
     *
     * @throws IllegalArgumentException If <code>annotations</code> is null.
     * @throws IllegalStateException    If a custom Iterable has been specified by a call to <code>setAnnotations</code>.
     */
    public void addAnnotations(Iterable<Annotation> annotations)
    {
        if (annotations == null)
        {
            throw new IllegalArgumentException();
        }
        if (this.annotationsOverride != null)
        {
            throw new IllegalStateException();
        }

        for (Annotation annotation : annotations)
        {
            // Internal list of annotations does not accept null values.
            if (annotation != null)
                this.annotations.add(annotation);
        }
    }

    /**
     * Removes the specified <code>annotation</code> from this layer's internal collection, if it exists. If this
     * layer's internal collection has been overriden with a call to {@link #setAnnotations}, this will throw an
     * exception.
     *
     * @param annotation Annotation to remove.
     *
     * @throws IllegalArgumentException If <code>annotation</code> is null.
     * @throws IllegalStateException    If a custom Iterable has been specified by a call to <code>setAnnotations</code>.
     */
    public void removeAnnotation(Annotation annotation)
    {
        if (annotation == null)
        {
            throw new IllegalArgumentException();
        }
        if (this.annotationsOverride != null)
        {
            throw new IllegalStateException();
        }

        this.annotations.remove(annotation);
    }

    /**
     * Clears the contents of this layer's internal Annotation collection. If this layer's internal collection has been
     * overriden with a call to {@link #setAnnotations}, this will throw an exception.
     *
     * @throws IllegalStateException If a custom Iterable has been specified by a call to <code>setAnnotations</code>.
     */
    public void removeAllAnnotations()
    {
        if (this.annotationsOverride != null)
        {
            throw new IllegalStateException();
        }

        clearAnnotations();
    }

    protected void clearAnnotations()
    {
        if (this.annotations != null && this.annotations.size() > 0)
            this.annotations.clear();
    }

    /**
     * Returns the Iterable of Annotations currently in use by this layer. If the caller has specified a custom Iterable
     * via {@link #setAnnotations}, this will returns a reference to that Iterable. If the caller passed
     * <code>setAnnotations</code> a null parameter, or if <code>setAnnotations</code> has not been called, this returns
     * a view of this layer's internal collection of Annotations.
     *
     * @return Iterable of currently active Annotations.
     */
    public Iterable<Annotation> getAnnotations()
    {
        return getActiveAnnotations();
    }

    /**
     * Returns the Iterable of currently active Annotations. If the caller has specified a custom Iterable via {@link
     * #setAnnotations}, this will returns a reference to that Iterable. If the caller passed
     * <code>setAnnotations</code> a null parameter, or if <code>setAnnotations</code> has not been called, this returns
     * a view of this layer's internal collection of Annotations.
     *
     * @return Iterable of currently active Annotations.
     */
    protected Iterable<Annotation> getActiveAnnotations()
    {
        if (this.annotationsOverride != null)
        {
            return this.annotationsOverride;
        }
        else
        {
            // Return an unmodifiable reference to the internal list of annotations.
            // This prevents callers from changing this list and invalidating any invariants we have established.
            return java.util.Collections.unmodifiableCollection(this.annotations);
        }
    }

    /**
     * Overrides the collection of currently active Annotations with the specified <code>annotationIterable</code>. This
     * layer will maintain a reference to <code>annotationIterable</code> strictly for picking and rendering. This layer
     * will not modify the Iterable reference. However, this will clear the internal collection of Annotations, and will
     * prevent any modification to its contents via <code>addAnnotation, addAnnotations, or removeAnnotations</code>.
     * <p/>
     * If the specified <code>annotationIterable</code> is null, this layer will revert to maintaining its internal
     * collection.
     *
     * @param annotationIterable Iterable to use instead of this layer's internal collection, or null to use this
     *                           layer's internal collection.
     */
    public void setAnnotations(Iterable<Annotation> annotationIterable)
    {
        this.annotationsOverride = annotationIterable;
        // Clear the internal collection of Annotations.
        clearAnnotations();
    }

    /**
     * Opacity is not applied to layers of this type because each annotation has an attribute set with opacity control.
     *
     * @param opacity the current opacity value, which is ignored by this layer.
     */
    @Override
    public void setOpacity(double opacity)
    {
        super.setOpacity(opacity);
    }

    /**
     * Returns the layer's opacity value, which is ignored by this layer because each of its annotations has an
     * attribute with its own opacity control.
     *
     * @return The layer opacity, a value between 0 and 1.
     */
    @Override
    public double getOpacity()
    {
        return super.getOpacity();
    }

    /**
     * Returns the <code>AnnotationRenderer</code> this layer delegates to during picking and rendering.
     *
     * @return <code>AnnotationRenderer</code> used to pick and render <code>Annotations</code>.
     */
    public AnnotationRenderer getAnnotationRenderer()
    {
        return this.annotationRenderer;
    }

    /**
     * Sets the <code>AnnotationRenderer</code> this layer delegates to during picking and rendering.
     *
     * @param annotationRenderer <code>AnnotationRenderer</code> used to pick and render <code>Annotations</code>.
     *
     * @throws IllegalArgumentException If <code>annotationRenderer</code> is null.
     */
    public void setAnnotationRenderer(AnnotationRenderer annotationRenderer)
    {
        if (annotationRenderer == null)
        {
            throw new IllegalArgumentException();
        }

        this.annotationRenderer = annotationRenderer;
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.annotationRenderer.pick(dc, getActiveAnnotations(), pickPoint, this);
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        this.annotationRenderer.render(dc, getActiveAnnotations(), this);
    }

    @Override
    public String toString()
    {
        return null;
    }
}
