/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.collada.impl;

import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.collada.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.meshes.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

/**
 * Shape to render a COLLADA line or triangle mesh. An instance of this shape can render any number of {@link
 * ColladaLines} or {@link ColladaTriangles}, but a single instance cannot render both lines and triangles. New
 * instances are created by {@link #createTriangleMesh(java.util.List, gov.nasa.worldwind.ogc.collada.ColladaBindMaterial)
 * createTriangleMesh} and {@link #createLineMesh(java.util.List, gov.nasa.worldwind.ogc.collada.ColladaBindMaterial)
 * createLineMesh}.
 * <p>
 * This shape supports only COLLADA line and triangle geometries.
 *
 * @author pabercrombie
 * @version $Id: ColladaMeshShape.java 2216 2014-08-11 20:29:24Z tgaskins $
 */
public class ColladaMeshShape extends Mesh3D {
    protected ColladaBindMaterial bindMaterial;

    public static ColladaMeshShape createTriangleMesh(List<ColladaTriangles> geometries, ColladaBindMaterial bindMaterial) {
        ColladaMeshShape shape = new ColladaMeshShape(geometries);

        geometries.forEach((geometry) -> {
            geometry.setParentMesh(shape);
        });

        shape.bindMaterial = bindMaterial;
        shape.setElementType(GL.GL_TRIANGLES);
        shape.setVertsPerShape(3);

        return shape;
    }

    /**
     * Create a line mesh shape.
     *
     * @param geometries COLLADA elements that defines geometry for this shape. Must contain at least one element.
     * @param bindMaterial Material applied to the mesh. May be null.
     * @return The resulting shape.
     */
    public static ColladaMeshShape createLineMesh(List<ColladaLines> geometries, ColladaBindMaterial bindMaterial) {
        ColladaMeshShape shape = new ColladaMeshShape(geometries);

        geometries.forEach((geometry) -> {
            geometry.setParentMesh(shape);
        });

        shape.bindMaterial = bindMaterial;
        shape.setElementType(GL.GL_LINES);
        shape.setVertsPerShape(2);

        return shape;
    }

    /**
     * Create an instance of the shape.
     *
     * @param geometries Geometries to render. All geometries must be of the same type (either {@link ColladaTriangles}
     * or {@link ColladaLines}.
     */
    protected ColladaMeshShape(List<? extends ColladaAbstractGeometry> geometries) {
        super(geometries);
        if (WWUtil.isEmpty(geometries)) {
            String message = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
    }

    public Box getLocalExtent(ColladaTraversalContext tc) {
        if (tc == null) {
            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int size = getShapeCount() * getVertsPerShape() * ColladaAbstractGeometry.COORDS_PER_VERTEX;
        FloatBuffer vertexBuffer = WWBufferUtil.newFloatBuffer(size, true);

        ArrayList<Geometry> geometries = getGeometries();

        for (Geometry geometry : geometries) {
            geometry.getNativeGeometry().getVertices(vertexBuffer);
        }

        // Compute a bounding box around the vertices in this shape.
        vertexBuffer.rewind();
        Box box = Box.computeBoundingBox(new BufferWrapper.FloatBufferWrapper(vertexBuffer),
                ColladaAbstractGeometry.COORDS_PER_VERTEX);

        // Compute the corners of the bounding box and transform with the active transform matrix.
        List<Vec4> extrema = new ArrayList<>();
        Vec4[] corners = box.getCorners();
        for (Vec4 corner : corners) {
            extrema.add(corner.transformBy4(tc.peekMatrix()));
        }

        if (extrema.isEmpty()) {
            return null;
        }

        // Compute the bounding box around the transformed corners.
        return Box.computeBoundingBox(extrema);
    }

    //////////////////////////////////////////////////////////////////////
    // Materials and textures
    //////////////////////////////////////////////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public Material getMaterial(AbstractGeometry geometry) {
        ColladaInstanceMaterial myMaterialInstance = this.getInstanceMaterial((ColladaAbstractGeometry) geometry);

        if (myMaterialInstance == null) {
            return DEFAULT_INTERIOR_MATERIAL;
        }

        // Attempt to resolve the instance. The material may not be immediately available.
        ColladaMaterial myMaterial = myMaterialInstance.get();
        if (myMaterial == null) {
            return DEFAULT_INTERIOR_MATERIAL;
        }

        ColladaInstanceEffect myEffectInstance = myMaterial.getInstanceEffect();
        if (myEffectInstance == null) {
            return DEFAULT_INTERIOR_MATERIAL;
        }

        // Attempt to resolve effect. The effect may not be immediately available.
        ColladaEffect myEffect = myEffectInstance.get();
        if (myEffect == null) {
            return DEFAULT_INTERIOR_MATERIAL;
        }

        return myEffect.getMaterial();
    }

    /**
     * Indicates the <i>instance_material</i> element for a geometry.
     *
     * @param colladaGeometry Geometry for which to find material.
     *
     * @return Material for the specified geometry, or null if the material cannot be resolved.
     */
    protected ColladaInstanceMaterial getInstanceMaterial(ColladaAbstractGeometry colladaGeometry) {
        if (this.bindMaterial == null) {
            return null;
        }

        ColladaTechniqueCommon techniqueCommon = this.bindMaterial.getTechniqueCommon();
        if (techniqueCommon == null) {
            return null;
        }

        String materialSource = colladaGeometry.getMaterial();
        if (materialSource == null) {
            return null;
        }

        for (ColladaInstanceMaterial material : techniqueCommon.getMaterials()) {
            if (materialSource.equals(material.getSymbol())) {
                return material;
            }
        }
        return null;
    }

    /**
     * Indicates the semantic that identifies texture coordinates. This may be specified for each material using a
     * <i>bind_vertex_input</i> element.
     *
     * @param colladaGeometry Geometry for which to find semantic.
     *
     * @return The semantic string that identifies the texture coordinates, or null if the geometry does not define the
     * semantic.
     */
    public String getTexCoordSemantic(ColladaAbstractGeometry colladaGeometry) {
        ColladaEffect effect = this.getEffect(colladaGeometry);
        if (effect == null) {
            return null;
        }

        ColladaTexture texture = effect.getTexture();
        if (texture == null) {
            return null;
        }

        String texcoord = texture.getTexCoord();
        if (texcoord == null) {
            return null;
        }

        ColladaInstanceMaterial instanceMaterial = this.getInstanceMaterial(colladaGeometry);
        String inputSemantic = null;

        // Search bind_vertex_input to find the semantic that identifies the texture coords.
        for (ColladaBindVertexInput bind : instanceMaterial.getBindVertexInputs()) {
            if (texcoord.equals(bind.getSemantic())) {
                inputSemantic = bind.getInputSemantic();
            }
        }

        return inputSemantic;
    }

    /**
     * Indicates the source (file path or URL) of the texture applied to a geometry.
     *
     * @param geometry Geometry for which to find texture source.
     *
     * @return The source of the texture, or null if it cannot be resolved.
     */
    protected String getTextureSource(AbstractGeometry geometry) {
        ColladaAbstractGeometry colladaGeometry = (ColladaAbstractGeometry) geometry;
        if (this.bindMaterial == null) {
            return null;
        }
        ColladaTechniqueCommon techniqueCommon = this.bindMaterial.getTechniqueCommon();
        if (techniqueCommon == null) {
            return null;
        }

        String materialSource = colladaGeometry.getMaterial();
        if (materialSource == null) {
            return null;
        }

        ColladaInstanceMaterial myMaterialInstance = null;
        for (ColladaInstanceMaterial material : techniqueCommon.getMaterials()) {
            if (materialSource.equals(material.getSymbol())) {
                myMaterialInstance = material;
                break;
            }
        }

        if (myMaterialInstance == null) {
            return null;
        }

        // Attempt to resolve the instance. The material may not be immediately available.
        ColladaMaterial myMaterial = myMaterialInstance.get();
        if (myMaterial == null) {
            return null;
        }

        ColladaInstanceEffect myEffectInstance = myMaterial.getInstanceEffect();
        if (myEffectInstance == null) {
            return null;
        }

        // Attempt to resolve effect. The effect may not be immediately available.
        ColladaEffect myEffect = myEffectInstance.get();
        if (myEffect == null) {
            return null;
        }

        ColladaTexture texture = myEffect.getTexture();
        if (texture == null) {
            return null;
        }

        String imageRef = this.getImageRef(myEffect, texture);
        if (imageRef == null) {
            return null;
        }

        // imageRef identifiers an <image> element in this or another document. If the string doesn't already contain a
        // # then treat the entire string as a fragment identifier in the current document.
        if (!imageRef.contains("#")) {
            imageRef = "#" + imageRef;
        }

        // imageRef identifiers an <image> element (may be external). This element will give us the filename.
        Object o = colladaGeometry.getRoot().resolveReference(imageRef);
        if (o instanceof ColladaImage) {
            return ((ColladaImage) o).getInitFrom();
        }

        return null;
    }

    /**
     * Indicates the reference string for an image. The image reference identifies an <i>image</i> element in this, or
     * another COLLADA file. For example, "#myImage".
     *
     * @param effect Effect that defines the texture.
     * @param texture Texture for which to find the image reference.
     *
     * @return The image reference, or null if it cannot be resolved.
     */
    protected String getImageRef(ColladaEffect effect, ColladaTexture texture) {
        String sid = texture.getTexture();

        ColladaNewParam param = effect.getParam(sid);
        if (param == null) {
            return null;
        }

        ColladaSampler2D sampler = param.getSampler2D();
        if (sampler == null) {
            return null;
        }

        ColladaSource source = sampler.getSource();
        if (source == null) {
            return null;
        }

        sid = source.getCharacters();
        if (sid == null) {
            return null;
        }

        param = effect.getParam(sid);
        if (param == null) {
            return null;
        }

        ColladaSurface surface = param.getSurface();
        if (surface != null) {
            return surface.getInitFrom();
        }

        return null;
    }

    /**
     * Indicates the effect applied to a geometry.
     *
     * @param geometry Geometry for which to find effect.
     *
     * @return Effect applied to the specified geometry, or null if no effect is defined, or the effect is not
     * available.
     */
    protected ColladaEffect getEffect(ColladaAbstractGeometry geometry) {
        if (this.bindMaterial == null) {
            return null;
        }

        ColladaTechniqueCommon techniqueCommon = this.bindMaterial.getTechniqueCommon();
        if (techniqueCommon == null) {
            return null;
        }

        String materialSource = geometry.getMaterial();
        if (materialSource == null) {
            return null;
        }

        ColladaInstanceMaterial myMaterialInstance = null;
        for (ColladaInstanceMaterial material : techniqueCommon.getMaterials()) {
            if (materialSource.equals(material.getSymbol())) {
                myMaterialInstance = material;
                break;
            }
        }

        if (myMaterialInstance == null) {
            return null;
        }

        // Attempt to resolve the instance. The material may not be immediately available.
        ColladaMaterial myMaterial = myMaterialInstance.get();
        if (myMaterial == null) {
            return null;
        }

        ColladaInstanceEffect myEffectInstance = myMaterial.getInstanceEffect();
        if (myEffectInstance == null) {
            return null;
        }

        // Attempt to resolve effect. The effect may not be immediately available.
        return myEffectInstance.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDoubleSided(AbstractGeometry geometry) {
        ColladaEffect effect = this.getEffect((ColladaAbstractGeometry) geometry);
        if (effect == null) {
            return false;
        }

        ColladaProfileCommon profile = effect.getProfileCommon();
        if (profile == null) {
            return false;
        }

        ColladaExtra extra = profile.getExtra();
        if (extra == null) {
            return false;
        }

        ColladaTechnique technique = (ColladaTechnique) extra.getField("technique");
        if (technique == null || !"GOOGLEEARTH".equals(technique.getProfile())) {
            return false;
        }

        Integer i = (Integer) technique.getField("double_sided");
        return i != null && i == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean mustApplyTexture(Geometry geometry) {
        ColladaAbstractGeometry colladaGeometry = (ColladaAbstractGeometry) geometry.getNativeGeometry();
        String semantic = this.getTexCoordSemantic(colladaGeometry);
        return colladaGeometry.getTexCoordAccessor(semantic) != null && this.getTexture(geometry) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WWTexture getTexture(Geometry geometry) {
        if (geometry.getTexture() != null) {
            return geometry.getTexture();
        }

        ColladaAbstractGeometry colladaGeometry = (ColladaAbstractGeometry) geometry.getNativeGeometry();
        String source = this.getTextureSource(colladaGeometry);
        if (source != null) {
            Object o = colladaGeometry.getRoot().resolveReference(source);
            if (o != null) {
                geometry.setTexture(new LazilyLoadedTexture(o));
            }
        }

        return geometry.getTexture();
    }

}
