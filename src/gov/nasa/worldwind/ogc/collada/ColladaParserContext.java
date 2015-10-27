/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

/**
 * The parser context for Collada documents.
 *
 * @author jfb
 * @version $Id: ColladaParserContext.java 644 2012-06-14 20:07:17Z pabercrombie $
 */
public class ColladaParserContext extends BasicXMLEventParserContext
{
    /** The key used to identify the coordinates parser in the parser context's parser map. */
    protected static QName COORDINATES = new QName("Coordinates");

    /** The names of elements that contain merely string data and can be parsed by a generic string parser. */
    protected static final String[] StringFields = new String[]
        {
            "author",
            "name",
            "authoring_tool",
            "source_data",
            "init_from",
            "created",
            "modified",
            "up_axis",
        };

    /** The names of elements that contain merely double data and can be parsed by a generic double parser. */
    protected static final String[] DoubleFields = new String[]
        {
            "revision",
            "float"
        };

    /** The names of elements that contain merely integer data and can be parsed by a generic integer parser. */
    protected static final String[] IntegerFields = new String[]
        {
            "drawOrder",
            "meter",
            "double_sided" // Not part of core COLLADA spec, but included in most SketchUp models.
        };

    /**
     * The names of elements that contain merely boolean integer (0 or 1) data and can be parsed by a generic boolean
     * integer parser.
     */
    protected static final String[] BooleanFields = new String[]
        {
            "extrude",
        };

    /**
     * Creates a parser context instance.
     *
     * @param eventReader      the event reader from which to read events.
     * @param defaultNamespace the default namespace. If null, {@link gov.nasa.worldwind.ogc.collada.ColladaConstants#COLLADA_NAMESPACE}
     */
    public ColladaParserContext(XMLEventReader eventReader, String defaultNamespace)
    {
        super(eventReader, defaultNamespace != null ? defaultNamespace : ColladaConstants.COLLADA_NAMESPACE);
    }

    /**
     * Creates a parser context instance.
     *
     * @param defaultNamespace the default namespace. If null, {@link gov.nasa.worldwind.ogc.collada.ColladaConstants#COLLADA_NAMESPACE}
     */
    public ColladaParserContext(String defaultNamespace)
    {
        this(null, defaultNamespace);
    }

    public ColladaParserContext(ColladaParserContext ctx)
    {
        super(ctx);
    }

    /**
     * Loads the parser map with the parser to use for each element type. The parser may be changed by calling {@link
     * #registerParser(javax.xml.namespace.QName, gov.nasa.worldwind.util.xml.XMLEventParser)}.
     */
    @Override
    protected void initializeParsers()
    {
        super.initializeParsers();

        this.initializeParsers(ColladaConstants.COLLADA_NAMESPACE);
    }

    protected void initializeParsers(String ns)
    {
        this.parsers.put(new QName(ns, "unit"), new ColladaUnit(ns));
        this.parsers.put(new QName(ns, "material"), new ColladaMaterial(ns));
        this.parsers.put(new QName(ns, "technique"), new ColladaTechnique(ns));
        this.parsers.put(new QName(ns, "surface"), new ColladaSurface(ns));
        this.parsers.put(new QName(ns, "format"), new ColladaFormat(ns));
        this.parsers.put(new QName(ns, "sampler2D"), new ColladaSampler2D(ns));
        this.parsers.put(new QName(ns, "source"), new ColladaSource(ns));
        this.parsers.put(new QName(ns, "param"), new ColladaParam(ns));
        this.parsers.put(new QName(ns, "float_array"), new ColladaFloatArray(ns));
        this.parsers.put(new QName(ns, "input"), new ColladaInput(ns));
        this.parsers.put(new QName(ns, "effect"), new ColladaEffect(ns));
        this.parsers.put(new QName(ns, "profile_COMMON"), new ColladaProfileCommon(ns));
        this.parsers.put(new QName(ns, "newparam"), new ColladaNewParam(ns));
        this.parsers.put(new QName(ns, "phong"), new ColladaPhong(ns));
        this.parsers.put(new QName(ns, "lambert"), new ColladaLambert(ns));
        this.parsers.put(new QName(ns, "mesh"), new ColladaMesh(ns));
        this.parsers.put(new QName(ns, "technique_COMMON"), new ColladaTechniqueCommon(ns));
        this.parsers.put(new QName(ns, "technique_common"), new ColladaTechniqueCommon(ns));
        this.parsers.put(new QName(ns, "accessor"), new ColladaAccessor(ns));
        this.parsers.put(new QName(ns, "p"), new ColladaP(ns));
        this.parsers.put(new QName(ns, "texture"), new ColladaTexture(ns));
        this.parsers.put(new QName(ns, "color"), new ColladaColor(ns));
        this.parsers.put(new QName(ns, "geometry"), new ColladaGeometry(ns));
        this.parsers.put(new QName(ns, "vertices"), new ColladaVertices(ns));
        this.parsers.put(new QName(ns, "bind"), new ColladaBind(ns));
        this.parsers.put(new QName(ns, "node"), new ColladaNode(ns));
        this.parsers.put(new QName(ns, "matrix"), new ColladaMatrix(ns));
        this.parsers.put(new QName(ns, "bind_material"), new ColladaBindMaterial(ns));
        this.parsers.put(new QName(ns, "bind_vertex_input"), new ColladaBindVertexInput(ns));
        this.parsers.put(new QName(ns, "scene"), new ColladaScene(ns));

        XMLEventParser parser = new ColladaTextureOrColor(ns);
        this.parsers.put(new QName(ns, "emission"), parser);
        this.parsers.put(new QName(ns, "ambient"), parser);
        this.parsers.put(new QName(ns, "diffuse"), parser);
        this.parsers.put(new QName(ns, "specular"), parser);
        this.parsers.put(new QName(ns, "transparent"), parser);

        parser = new ColladaFloatOrParam(ns);
        this.parsers.put(new QName(ns, "shininess"), parser);
        this.parsers.put(new QName(ns, "reflectivity"), parser);
        this.parsers.put(new QName(ns, "transparency"), parser);
        this.parsers.put(new QName(ns, "index_of_refraction"), parser);

        this.parsers.put(new QName(ns, "image"), new ColladaImage(ns));
        this.parsers.put(new QName(ns, "asset"), new ColladaAsset(ns));
        this.parsers.put(new QName(ns, "contributor"), new ColladaContributor(ns));

        this.parsers.put(new QName(ns, "library_geometries"), new ColladaLibrary<ColladaGeometry>(ns));
        this.parsers.put(new QName(ns, "library_effects"), new ColladaLibrary<ColladaEffect>(ns));
        this.parsers.put(new QName(ns, "library_images"), new ColladaLibrary<ColladaImage>(ns));
        this.parsers.put(new QName(ns, "library_materials"), new ColladaLibrary<ColladaMaterial>(ns));
        this.parsers.put(new QName(ns, "library_visual_scenes"), new ColladaLibrary<ColladaVisualScene>(ns));
        this.parsers.put(new QName(ns, "library_nodes"), new ColladaLibrary<ColladaNode>(ns));

        this.parsers.put(new QName(ns, "instance_visual_scene"), new ColladaInstanceVisualScene(ns));
        this.parsers.put(new QName(ns, "instance_geometry"), new ColladaInstanceGeometry(ns));
        this.parsers.put(new QName(ns, "instance_material"), new ColladaInstanceMaterial(ns));
        this.parsers.put(new QName(ns, "instance_effect"), new ColladaInstanceEffect(ns));
        this.parsers.put(new QName(ns, "instance_node"), new ColladaInstanceNode(ns));

        this.parsers.put(new QName(ns, "visual_scene"), new ColladaVisualScene(ns));
        this.parsers.put(new QName(ns, "triangles"), new ColladaTriangles(ns));
        this.parsers.put(new QName(ns, "lines"), new ColladaLines(ns));
        this.parsers.put(new QName(ns, "extra"), new ColladaExtra(ns));

        // The following elements are valid COLLADA markup, but are not used by World Wind.
        parser = new ColladaUnsupported(ns);
        this.parsers.put(new QName(ns, "library_cameras"), parser);
        this.parsers.put(new QName(ns, "instance_camera"), parser);
        this.parsers.put(new QName(ns, "camera"), parser);
        this.parsers.put(new QName(ns, "lookat"), parser);
        this.parsers.put(new QName(ns, "optics"), parser);
        this.parsers.put(new QName(ns, "perspective"), parser);
        this.parsers.put(new QName(ns, "orthographic"), parser);
        this.parsers.put(new QName(ns, "znear"), parser);
        this.parsers.put(new QName(ns, "zfar"), parser);
        this.parsers.put(new QName(ns, "xfov"), parser);
        this.parsers.put(new QName(ns, "yfov"), parser);
        this.parsers.put(new QName(ns, "aspect_ratio"), parser);

        this.addStringParsers(ns, StringFields);
        this.addDoubleParsers(ns, DoubleFields);
        this.addIntegerParsers(ns, IntegerFields);
        this.addBooleanParsers(ns, BooleanFields);
    }
}
