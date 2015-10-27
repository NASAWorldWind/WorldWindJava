/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.util.*;

import java.util.*;
import java.util.regex.*;

/**
 * Text decoder that performs entity substitution for KML description balloons. This class is thread safe.
 *
 * @author pabercrombie
 * @version $Id: KMLBalloonTextDecoder.java 1944 2014-04-18 16:50:49Z tgaskins $
 */
public class KMLBalloonTextDecoder extends BasicTextDecoder
{
    /**
     * True if there are entities in the balloon text which may refer to unresolved schema. False if all entities have
     * been resolved, or are known to be unresolvable (because the data they refer to does not exist).
     */
    protected boolean isUnresolved;

    /**
     * Keep a cache of entities that have been resolved so that we don't have to re-resolve them every time the decoded
     * text is requested.
     */
    protected Map<String, String> entityCache = new HashMap<String, String>();

    /** Feature to use as context for entity replacements. */
    protected KMLAbstractFeature feature;

    /**
     * Create a decoder to generate balloon text for a feature. The feature becomes the context of the entity
     * replacements (for example "$[name]" will be replaced with the feature's name.
     *
     * @param feature Feature that is the context of entity replacements.
     */
    public KMLBalloonTextDecoder(KMLAbstractFeature feature)
    {
        if (feature == null)
        {
            String message = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.feature = feature;
    }

    /**
     * Get the balloon text after entity substitutions (for example, $[name], $[description], etc) have been made. See
     * the KML specification for details on entity replacement. If some entities could not be resolved because they
     * refer to unresolved schema, the decoder will try to decode the string again the next time this method is called.
     *
     * @return String after replacements have been made. Returns null if the input text is null.
     */
    @Override
    public synchronized String getDecodedText()
    {
        if (this.decodedText == null)
            this.lastUpdateTime = System.currentTimeMillis();

        if (this.decodedText == null || this.isUnresolved)
            this.decodedText = this.decode(this.text);

        // If the text was fully decoded we can release our reference to the original text.
        if (!this.isUnresolved)
            this.text = null;

        return this.decodedText;
    }

    /** Perform entity substitution. */
    @Override
    protected String decode(String textToDecode)
    {
        if (textToDecode == null)
            return null;

        this.isUnresolved = false;

        Pattern p = Pattern.compile("\\$\\[(.*?)\\]");
        Matcher m = p.matcher(textToDecode);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            String entity = m.group(1);

            // Check the entity cache to see if we've already resolved this entity.
            String r = this.entityCache.get(entity);
            if (r == null)
            {
                // Try to resolve the entity
                r = this.resolveEntityReference(entity);
                if (r != null)
                {
                    // Save the resolved entity in the cache, and set the last update time. Resolving this entity
                    // has changed the decoded string.
                    this.entityCache.put(entity, r);
                    this.lastUpdateTime = System.currentTimeMillis();
                }
            }

            m.appendReplacement(sb, Matcher.quoteReplacement(r != null ? r : ""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setText(String input)
    {
        super.setText(input);
        this.entityCache.clear();
    }

    /**
     * Resolve an entity reference. The pattern can be in one of these forms:
     * <pre>
     *  FeatureField
     *  DataField
     *  DataField/displayName
     *  SchemaName/SchemaField
     *  SchemaName/SchemaField/displayName
     *  </pre>
     * See the KML spec for details.
     *
     * @param pattern Pattern of entity to resolve.
     *
     * @return Return the replacement string for the entity, or null if no replacement can be found.
     */
    protected String resolveEntityReference(String pattern)
    {
        KMLAbstractFeature feature = this.getFeature();

        if ("geDirections".equals(pattern))
            return this.getGeDirectionsText();

        // First look for a field in the Feature
        Object replacement = feature.getField(pattern);
        if (replacement instanceof KMLAbstractObject)
            return ((KMLAbstractObject) replacement).getCharacters();
        else if (replacement != null)
            return replacement.toString();

        // Before searching data fields, split the pattern into name, field, and display name components
        String name;            // Name of data element or schema
        String field = "";      // Schema field (does not apply to data fields)
        boolean isDisplayName;  // Indicates that the replacement is the display name of the data, not the data itself

        isDisplayName = pattern.endsWith("/displayName");

        String[] parts = pattern.split("/");

        name = parts[0];  // Name is always the first field, or the whole pattern if there were no slashes

        if ((parts.length == 2 && !isDisplayName) || (parts.length > 2)) // name/field or name/field/displayName
        {
            field = parts[1];
        }

        // Next look for a data field in the Feature's extended data
        KMLExtendedData extendedData = feature.getExtendedData();
        if (extendedData != null)
        {
            // Search through untyped data first
            for (KMLData data : extendedData.getData())
            {
                if (name.equals(data.getName()))
                {
                    if (isDisplayName)
                    {
                        if (!WWUtil.isEmpty(data.getDisplayName()))
                            return data.getDisplayName();
                        else
                            return data.getName();
                    }
                    else
                    {
                        return data.getValue();
                    }
                }
            }

            // Search through typed schema data fields
            boolean schemaUnresolved = false;
            List<KMLSchemaData> schemaDataList = extendedData.getSchemaData();
            for (KMLSchemaData schemaData : schemaDataList)
            {
                // Try to resolve the schema. The schema may not be available immediately
                final String url = schemaData.getSchemaUrl();
                KMLSchema schema = (KMLSchema) feature.getRoot().resolveReference(url);

                if (schema != null && name.equals(schema.getName()))
                {
                    // We found the schema. Now we are looking for either the display name or the value of
                    // one of the fields.
                    if (isDisplayName)
                    {
                        // Search schema fields
                        for (KMLSimpleField simpleField : schema.getSimpleFields())
                        {
                            if (field.equals(simpleField.getName()))
                            {
                                return simpleField.getDisplayName();
                            }
                        }
                    }
                    else
                    {
                        // Search data fields
                        for (KMLSimpleData simpleData : schemaData.getSimpleData())
                        {
                            if (field.equals(simpleData.getName()))
                            {
                                return simpleData.getCharacters();
                            }
                        }
                    }
                }
                else if (schema == null)
                {
                    schemaUnresolved = true;
                }
            }

            // Set the balloon text to unresolved if we still haven't found a match, and there is at least one
            // unresolved schema, and the pattern could refer to a schema
            if (schemaUnresolved && !WWUtil.isEmpty(name) && !WWUtil.isEmpty(field))
            {
                this.isUnresolved = true;
            }
        }

        return null; // No match found
    }

    /**
     * Get the feature used as context for resolving entity references.
     *
     * @return The context feature.
     */
    public KMLAbstractFeature getFeature()
    {
        return this.feature;
    }

    /**
     * Get the text used to replace the $[geDirections] entity. This implementation returns an empty string. Subclasses
     * can override this method to provide directions text.
     *
     * @return Text to replace the $[geDirections] entity.
     */
    protected String getGeDirectionsText()
    {
        return "";
    }
}
