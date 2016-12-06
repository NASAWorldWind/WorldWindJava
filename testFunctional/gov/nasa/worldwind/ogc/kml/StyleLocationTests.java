/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.gx.GXConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.xml.atom.AtomConstants;
import gov.nasa.worldwind.util.xml.xal.XALConstants;

import javax.swing.*;
import java.io.*;
import java.util.zip.*;

/**
 * Tests the resolution of Style, StyleMap and styleUrl in a number of situations.
 *
 * @author tag
 * @version $Id: StyleLocationTests.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class StyleLocationTests extends ApplicationTemplate
{
    private static String ICON_LOCATIONS = "http://tomgaskins.net/kmltest/placemarks/";
    protected static Position nextPosition = Position.fromDegrees(0, -151, 1e6);

    protected static TestDoc[] tests = new TestDoc[]
        {
            new TestA(),
            new TestB(),
            new TestC(),
            new TestD(),
            new TestE(),
            new TestF(),
            new TestG(),
            new TestH(),
            new TestI(),
            new TestJ(),
            new TestK(),
            new TestL(),
            new TestM(),
        };

    protected static class TestA extends TestDoc
    {
        // Inline Style

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "a.png";

            StringBuilder sb = newDocument();
            String stylePortion = makeIconStyle(ICON_LOCATIONS + iconFile);
            String feature = makeFeature(getNextPosition(), stylePortion);
            sb.append(feature);
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestB extends TestDoc
    {
        // Inline StyleMap

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "b.png";

            StringBuilder sb = newDocument();
            String stylePortion = makeIconStyleMap(ICON_LOCATIONS + iconFile);
            String feature = makeFeature(getNextPosition(), stylePortion);
            sb.append(feature);
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestC extends TestDoc
    {
        // styleUrl to internal Style

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "c.png";

            StringBuilder sb = newDocument();
            sb.append("<Document>");
            sb.append(makeIconStyle(ICON_LOCATIONS + iconFile));
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(), makeInternalIconStyleUrl());
            sb.append("</Folder>");
            sb.append(feature);
            sb.append("</Document>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestD extends TestDoc
    {
        // styleUrl to internal StyleMap

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "d.png";

            StringBuilder sb = newDocument();
            sb.append("<Document>");
            sb.append(makeIconStyleMap(ICON_LOCATIONS + iconFile));
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(), makeInternalIconStyleMapUrl());
            sb.append("</Folder>");
            sb.append(feature);
            sb.append("</Document>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestE extends TestDoc
    {
        // styleUrl to Style in local KML file

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "e.png";

            StringBuilder sb = newDocument();
            sb.append("<Document>");
            sb.append(makeIconStyle(ICON_LOCATIONS + iconFile));
            sb.append("</Document>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            WWIO.writeTextFile(sb.toString(), file);
            file.deleteOnExit();

            sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(), makeExternalIconStyleUrl(file.getPath()));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestF extends TestDoc
    {
        // styleUrl to StyleMap in local KML file

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "f.png";

            StringBuilder sb = newDocument();
            sb.append("<Document>");
            sb.append(makeIconStyleMap(ICON_LOCATIONS + iconFile));
            sb.append("</Document>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            WWIO.writeTextFile(sb.toString(), file);
            file.deleteOnExit();

            sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(), makeExternalIconStyleUrl(file.getPath()));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestG extends TestDoc
    {
        // styleUrl to Style internal to local KMZ file

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "g.png";

            StringBuilder sbi = newDocument();
            sbi.append("<Document>");
            sbi.append(makeIconStyle(ICON_LOCATIONS + iconFile));
            sbi.append("</Document>");
            endDocument(sbi);

            StringBuilder sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(), makeExternalIconStyleUrl("files/stylesG.kml"));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            File file = makeKMZDoc("doc.kml", sb.toString(), "files/stylesG.kml", sbi.toString());

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestH extends TestDoc
    {
        // styleUrl to StyleMap internal to local KMZ file

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "h.png";

            WorldWind.getSessionCache().clear();

            StringBuilder sbi = newDocument();
            sbi.append("<Document>");
            sbi.append(makeIconStyleMap(ICON_LOCATIONS + iconFile));
            sbi.append("</Document>");
            endDocument(sbi);

            StringBuilder sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(), makeExternalIconStyleUrl("files/stylesH.kml"));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            File file = makeKMZDoc("doc.kml", sb.toString(), "files/stylesH.kml", sbi.toString());

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestI extends TestDoc
    {
        // styleUrl to Style in remote KML file

        protected KMLRoot buildTest() throws Exception
        {
            StringBuilder sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(),
                makeExternalIconStyleUrl("http://tomgaskins.net/kmltest/RemoteStyle.kml"));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestJ extends TestDoc
    {
        // styleUrl to StyleMap in remote KML file

        protected KMLRoot buildTest() throws Exception
        {
            StringBuilder sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(),
                makeExternalIconStyleUrl("http://tomgaskins.net/kmltest/RemoteStyleMap.kml"));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestK extends TestDoc
    {
        // styleUrl to Style in remote KMZ file

        protected KMLRoot buildTest() throws Exception
        {
            StringBuilder sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(),
                makeExternalIconStyleUrl("http://tomgaskins.net/kmltest/RemoteStyle.kmz"));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestL extends TestDoc
    {
        // styleUrl to StyleMap in remote KMZ file

        protected KMLRoot buildTest() throws Exception
        {
            StringBuilder sb = newDocument();
            sb.append("<Folder>");
            String feature = makeFeature(getNextPosition(),
                makeExternalIconStyleUrl("http://tomgaskins.net/kmltest/RemoteStyleMap.kmz"));
            sb.append(feature);
            sb.append("</Folder>");
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static class TestM extends TestDoc
    {
        // Inline Style

        protected KMLRoot buildTest() throws Exception
        {
            String iconFile = "m.png";

            StringBuilder sb = newDocument();
            String stylePortion = makeIconStyle(ICON_LOCATIONS + iconFile);
            String feature = makeFeature(getNextPosition(), stylePortion);
            sb.append(feature);
            endDocument(sb);

            File file = File.createTempFile("wwjKMLStyleLocationTest", ".kml");
            file.deleteOnExit();
            WWIO.writeTextFile(sb.toString(), file);

            KMLRoot kmlRoot = new KMLRoot(file);
            kmlRoot.parse();

            return kmlRoot;
        }
    }

    protected static Position getNextPosition()
    {
        return nextPosition = nextPosition.add(Position.fromDegrees(0, 5));
    }

    protected static StringBuilder newDocument()
    {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<kml:kml");
        sb.append(" xmlns:kml=\"").append(KMLConstants.KML_NAMESPACE).append("\"");
        sb.append(" xmlns:atom=\"").append(AtomConstants.ATOM_NAMESPACE).append("\"");
        sb.append(" xmlns:xal=\"").append(XALConstants.XAL_NAMESPACE).append("\"");
        sb.append(" xmlns:gx=\"").append(GXConstants.GX_NAMESPACE).append("\"");
        sb.append(">");

        return sb;
    }

    protected static void endDocument(StringBuilder sb)
    {
        sb.append("</kml:kml>");
    }

    protected static String makeFeature(Position position, String stylePortion)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<Placemark>");
        if (stylePortion != null)
            sb.append(stylePortion);
        sb.append("<Point>");
        sb.append("<extrude>1</extrude>");
        sb.append("<altitudeMode>relativeToGround</altitudeMode>");
        sb.append("<coordinates>");
        sb.append(position.getLongitude().degrees).append(",");
        sb.append(position.getLatitude().degrees).append(",");
        sb.append(position.getAltitude());
        sb.append("</coordinates>");
        sb.append("</Point>");
        sb.append("</Placemark>");

        return sb.toString();
    }

    protected static String makeIconStyle(String iconFile)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<Style id=\"IconStyle01\">");

        sb.append("<IconStyle>");
        sb.append("<scale>0.5</scale>");
        sb.append("<hotSpot xunits=\"fraction\" x=\"0.5\" yunits=\"pixels\" y=\"30\"/>");
        sb.append("<Icon>");
        sb.append("<href>");
        sb.append(iconFile);
        sb.append("</href>");
        sb.append("</Icon>");
        sb.append("</IconStyle>");

        sb.append("<LineStyle>");
        sb.append("<color>77ffff00</color>"); // aabbggrr -- semi-transparent turquoise
        sb.append("<width>3</width>");
        sb.append("</LineStyle>");

        sb.append("</Style>");

        return sb.toString();
    }

    protected static String makeMultiIconStyle(String iconFile)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<Style id=\"IconStyle01\">");

        sb.append("<IconStyle>");
        sb.append("<hotSpot xunits=\"fraction\" x=\"0.5\" yunits=\"pixels\" y=\"30\"/>");
        sb.append("<Icon>");
        sb.append("<href>");
        sb.append(iconFile);
        sb.append("</href>");
        sb.append("</Icon>");
        sb.append("</IconStyle>");

        sb.append("<LineStyle>");
        sb.append("<color>77ffff00</color>"); // aabbggrr -- semi-transparent turquoise
        sb.append("</LineStyle>");

        sb.append("<IconStyle>");
        sb.append("<scale>0.5</scale>");
        sb.append("</IconStyle>");

        sb.append("<LineStyle>");
        sb.append("<width>3</width>");
        sb.append("</LineStyle>");

        sb.append("</Style>");

        return sb.toString();
    }

    protected static String makeIconStyleMap(String iconFile)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<StyleMap id=\"IconStyle01\">\">");

        sb.append("<Pair>");
        sb.append("<key>normal</key>");
        sb.append(makeIconStyle(iconFile));
        sb.append("</Pair>");
        sb.append("<Pair>");
        sb.append("<key>highlight</key>");
        sb.append(makeIconStyle(iconFile));
        sb.append("</Pair>");

        sb.append("</StyleMap>");

        return sb.toString();
    }

    protected static String makeInternalIconStyleUrl()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<styleUrl>");
        sb.append("#IconStyle01");
        sb.append("</styleUrl>");

        return sb.toString();
    }

    protected static String makeInternalIconStyleMapUrl()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<styleUrl>");
        sb.append("#IconStyle01");
        sb.append("</styleUrl>");

        return sb.toString();
    }

    protected static String makeExternalIconStyleUrl(String filePath)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<styleUrl>");
        sb.append(filePath);
        sb.append("#IconStyle01");
        sb.append("</styleUrl>");

        return sb.toString();
    }

    protected static File makeKMZDoc(String kmlFileName, String kmlContents, Object... auxFiles) throws IOException
    {
        File file = File.createTempFile("wwjKMLStyleLocationTest", ".kmz");
        file.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(file);
        ZipOutputStream zos = new ZipOutputStream(fos);

        ZipEntry entry = new ZipEntry(kmlFileName);
        zos.putNextEntry(entry);
        zos.write(kmlContents.getBytes());

        if (auxFiles.length != 0)
        {
            for (int i = 0; i < auxFiles.length; i += 2)
            {
                entry = new ZipEntry(auxFiles[i].toString());
                zos.putNextEntry(entry);

                if (auxFiles[i + 1] instanceof String)
                    zos.write(auxFiles[i + 1].toString().getBytes());
            }
        }

        zos.close();

        return file;
    }

    abstract protected static class TestDoc extends Thread
    {
        abstract protected KMLRoot buildTest() throws Exception;

        protected AppFrame appFrame;

        public void setAppFrame(AppFrame appFrame)
        {
            this.appFrame = appFrame;
        }

        @Override
        public void run()
        {
            try
            {
                this.add(this.buildTest());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        protected void add(KMLRoot kmlRoot)
        {
            final RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(new KMLController(kmlRoot));

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    appFrame.getWwd().getModel().getLayers().add(layer);
                }
            });
        }
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);
        }
    }

    public static void main(String[] args)
    {
        final AppFrame af = (AppFrame) ApplicationTemplate.start("World Wind KML Style Location Tests", AppFrame.class);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                for (TestDoc td : tests)
                {
                    td.setAppFrame(af);
                    td.run();
                }
            }
        });
    }
}
