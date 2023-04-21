/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: SARAnnotationWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARAnnotationWriter
{
    private final org.w3c.dom.Document doc;
    private final javax.xml.transform.Result result;

    public SARAnnotationWriter(String path) throws java.io.IOException, javax.xml.parsers.ParserConfigurationException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        this.doc = factory.newDocumentBuilder().newDocument();
        this.result = new javax.xml.transform.stream.StreamResult(new java.io.File(path));
        createAnnotationsDocument(this.doc);
    }

    public SARAnnotationWriter(java.io.OutputStream stream) throws java.io.IOException, javax.xml.parsers.ParserConfigurationException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        this.doc = factory.newDocumentBuilder().newDocument();
        this.result = new javax.xml.transform.stream.StreamResult(stream);
        createAnnotationsDocument(this.doc);
    }

    public void writeAnnotation(SARAnnotation sarAnnotation) throws javax.xml.transform.TransformerException
    {
        if (sarAnnotation == null)
        {
            String msg = "nullValue.SARAnnotationIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        doWriteAnnotation(sarAnnotation, this.doc.getDocumentElement());
    }

    public void writeAnnotations(Iterable<SARAnnotation> sarAnnotations) throws javax.xml.transform.TransformerException
    {
        if (sarAnnotations == null)
        {
            String msg = "nullValue.SARAnnotationIterableIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (SARAnnotation sa : sarAnnotations)
        {
            if (sa != null)
                doWriteAnnotation(sa, this.doc.getDocumentElement());
        }
        doFlush();
    }

    public void close()
    {
        // Intentionally left blank,
        // as a placeholder for future functionality.
    }

    private void createAnnotationsDocument(org.w3c.dom.Document doc)
    {
        // Create the GPX document root when the document
        // doesn't already have a root element.
        if (doc != null)
        {
            if (doc.getDocumentElement() != null)
                doc.removeChild(doc.getDocumentElement());

            org.w3c.dom.Element annotations = doc.createElement("sarTrackAnnotations");
            doc.appendChild(annotations);
        }
    }

    private void doWriteAnnotation(SARAnnotation sarAnnotation, org.w3c.dom.Element elem)
    {
        if (sarAnnotation != null)
        {
            org.w3c.dom.Element anno = this.doc.createElement("sarAnnotation");

            if (sarAnnotation.getPosition() != null)
            {
                org.w3c.dom.Element lat = this.doc.createElement("latitude");
                org.w3c.dom.Text latText = this.doc.createTextNode(
                    Double.toString(sarAnnotation.getPosition().getLatitude().degrees));
                lat.appendChild(latText);
                anno.appendChild(lat);

                org.w3c.dom.Element lon = this.doc.createElement("longitude");
                org.w3c.dom.Text lonText = this.doc.createTextNode(
                    Double.toString(sarAnnotation.getPosition().getLongitude().degrees));
                lon.appendChild(lonText);
                anno.appendChild(lon);
            }

            if (sarAnnotation.getId() != null)
            {
                org.w3c.dom.Element id = this.doc.createElement("id");
                org.w3c.dom.Text idText = this.doc.createTextNode(sarAnnotation.getId());
                id.appendChild(idText);
                anno.appendChild(id);
            }

            if (sarAnnotation.getText() != null)
            {
                org.w3c.dom.Element text = this.doc.createElement("text");
                org.w3c.dom.CDATASection cdata = this.doc.createCDATASection(sarAnnotation.getText());
                text.appendChild(cdata);
                anno.appendChild(text);
            }

            elem.appendChild(anno);
        }
    }

    private void doFlush() throws javax.xml.transform.TransformerException
    {
        javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = factory.newTransformer();
        javax.xml.transform.Source source = new javax.xml.transform.dom.DOMSource(this.doc);
        transformer.transform(source, this.result);
    }
}
