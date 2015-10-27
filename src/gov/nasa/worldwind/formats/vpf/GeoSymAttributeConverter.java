/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;

/**
 * The GeoSymAttributeConverter application converts GeoSym line attributes, area attributes, and area patterns into a
 * form usable by World Wind VPF shapes. Outputs to "gsac-out" a comma-separated-value (CSV) file containing line and
 * area attributes for VPF line and area shapes, and PNG images containing area patterns for VPF area shapes.
 * <p/>
 * GeoSymAttributeConverter is used to build the VPF symbol JAR file <code>vpf-symbols.jar</code>. For instructions on
 * how to build <code>vpf-symbols.jar</code>, see the file <code>VPF_README.txt</code>, which is distributed in each
 * World Wind release.
 * <p/>
 * <code> Usage: java -cp worldwind.jar gov.nasa.worldwind.formats.vpf.GeoSymAttributeConverter [full path to
 * "GeoSymEd2Final/GRAPHICS/CTEXT"] </code>
 *
 * @author dcollins
 * @version $Id: GeoSymAttributeConverter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymAttributeConverter
{
    protected static String TYPE_POINT = "Point";
    protected static String TYPE_LINE_PLAIN = "LinePlain";
    protected static String TYPE_LINE_COMPLEX = "LineComplex";
    protected static String TYPE_AREA_PLAIN = "AreaPlain";
    protected static String TYPE_AREA_PATTERN = "AreaPattern";

    protected static class CGMFile
    {
        protected File file;
        protected String content;
        protected String type;
        protected int[] colorTable;
        protected int[] patternTable;

        protected int lineCount = 0;
        protected int polylineCount = 0;
        protected int shapeCount = 0;
        protected int polygonCount = 0;
        protected int patternCount = 0;
        protected int pentagonCount = 0;
        protected int lineElementCount = 0;

        protected int lineColorIndex = -1;
        protected int fillColorIndex = -1;
        protected int lineWidth = -1;
        protected double scale = 1;
        protected short stipplePattern;
        protected int stippleFactor = 0;
        protected String edgeVis = "";

        public CGMFile(File file)
        {
            this.file = file;
            processCGMFile(file);

            // TODO: process line stipple pattern
            if (this.type.equals(TYPE_LINE_COMPLEX))
                this.processStipplePattern();
        }

        protected void processCGMFile(File file)
        {
            this.content = readTextFile(file).replaceAll("\r", "");
            String[] lines = this.content.split("\n");
            String CGMLine = "";
            for (String line : lines)
            {
                if (!line.endsWith(";"))
                    line = line.trim() + " ";
                CGMLine += line;
                if (CGMLine.endsWith(";"))
                {
                    this.processCGMLine(CGMLine);
                    CGMLine = "";
                }
            }

            this.type = getType();
        }

        protected void processCGMLine(String line)
        {
            if (line.startsWith("LINE "))
            {
                if (getNumValues(line) == 4)
                    this.lineCount++;
                else
                    this.polylineCount++;
            }
            else if (line.startsWith("CIRCLE "))
                this.shapeCount++;
            else if (line.startsWith("ELLIPARC "))
                this.shapeCount++;
            else if (line.startsWith("POLYGONSET "))
                this.shapeCount++;
            else if (line.startsWith("POLYGON "))
            {
                this.polygonCount++;
                if (getNumValues(line) == 12)
                    this.pentagonCount++;
            }
            else if (line.startsWith("PATTABLE "))
            {
                this.patternCount++;
                this.patternTable = getIntegerValues(line);
            }
            else if (line.startsWith("LINECOLR "))
                this.lineColorIndex = getIntegerValue(line);
            else if (line.startsWith("FILLCOLR "))
                this.fillColorIndex = getIntegerValue(line);
            else if (line.startsWith("LINEWIDTH "))
                this.lineWidth = getIntegerValue(line);
            else if (line.contains("LineComponentElement"))
                this.lineElementCount++;
            else if (line.startsWith("EDGEVIS "))
                this.edgeVis = getStringValue(line);
            else if (line.startsWith("COLRTABLE "))
                this.colorTable = getIntegerValues(line);
            else if (line.startsWith("COLRTABLE "))
                this.colorTable = getIntegerValues(line);
            else if (line.startsWith("SCALEMODE "))
                this.scale = getScaleValue(line);
        }

        protected int getIntegerValue(String line)
        {
            return Integer.parseInt(getStringValue(line));
        }

        protected int[] getIntegerValues(String line)
        {
            line = line.substring(0, line.length() - 1); // remove end line ";"
            String[] values = line.split(" ");
            int[] ints = new int[values.length - 1];
            int idx = 0;
            for (int i = 1; i < values.length; i++)
            {
                if (values[i].length() > 0)
                    ints[idx++] = Integer.parseInt(values[i]);
            }
            return ints;
        }

        protected String getStringValue(String line)
        {
            line = line.substring(0, line.length() - 1); // remove end line ";"
            return line.split(" ")[1];
        }

        protected double getScaleValue(String line)
        {
            line = line.substring(0, line.length() - 1); // remove end line ";"
            String[] values = line.split(" ");
            return Double.parseDouble(values[2]);
        }

        protected int getNumValues(String line)
        {
            return line.split(" ").length - 1;
        }

        protected String getColor(int idx)
        {
            if (idx < 0 || idx * 3 + 3 > this.colorTable.length - 1)
                return "#FFFFFF";
            String color = String.format("#%02x%02x%02x", this.colorTable[idx * 3 + 1], this.colorTable[idx * 3 + 2],
                this.colorTable[idx * 3 + 3]);
            if (color.length() > 7)
                System.out.println(
                    "Color error: idx: " + idx + ", color: " + color + ", components: " + String.format("%d %d %d",
                        this.colorTable[idx * 3 + 1], this.colorTable[idx * 3 + 2], this.colorTable[idx * 3 + 3]));
            return color;
        }

        protected String getType()
        {
            if (this.patternCount > 0)
                return TYPE_AREA_PATTERN;
            else if (this.polygonCount == 1 && this.pentagonCount == 1 && this.edgeVis.equals("OFF")
                && this.lineCount == 0 && this.polylineCount == 0 && this.shapeCount == 0)
                return TYPE_AREA_PLAIN;
            else if (this.lineElementCount > 0)
                return TYPE_LINE_COMPLEX;
            else if (this.lineCount == 1 && this.polylineCount == 0 && this.shapeCount == 0 && this.polygonCount == 0)
                return TYPE_LINE_PLAIN;
            else
                return TYPE_POINT;
        }

        protected void processStipplePattern()
        {
            //String elementType = "";
            double elementLength = 0;
            double totalLength = 0;
            ArrayList<Double> lengths = new ArrayList<Double>();
            String[] lines = this.content.split("\n");
            // Gather pattern segments
            for (String line : lines)
            {
                if (line.contains("Component.1.Element"))
                {
                    if (elementLength > 0)
                    {
                        lengths.add(elementLength);
                        totalLength += elementLength;
                        elementLength = 0;
                    }
                }
                else if (line.startsWith("APSATTR \"ElementType\""))
                {
                    //elementType = getLastValue(line);
                }
                else if (line.startsWith("APSATTR \"ElementLength\""))
                {
                    elementLength = Double.parseDouble(getLastValue(line));
                }
                else if (line.contains("Component.2") || line.startsWith("BEGAPS \"IC_ViewportTable\""))
                {
                    break;
                }
            }
            // Gather last pattern portion if any
            if (elementLength > 0)
            {
                lengths.add(elementLength);
                totalLength += elementLength;
            }
            // Determine the stipple 16 bit pattern out of the length sequence
            // This assumes the sequence starts with a dash and follows with gaps/dashes
            double bitLength = totalLength / 16;
            String bitMask = "";
            char bit = '1';
            for (Double d : lengths)
            {
                int bits = (int) Math.round(d / bitLength);
                for (int i = 0; i < bits; i++)
                {
                    bitMask += bit;
                }
                bit = bit == '1' ? '0' : '1';
            }
            // Padd or trim the bit mask to 16 bits
            if (bitMask.length() == 0 || !bitMask.contains("0") || !bitMask.contains("1"))
                bitMask = "1111110001111000"; // default mask
            while (bitMask.length() < 16)
            {
                bitMask += bitMask.substring(bitMask.length() - 1);
            }
            bitMask = bitMask.substring(0, 16);
            // Set stipple pattern and factor
            this.stipplePattern = (short) Integer.parseInt(bitMask, 2);
            this.stippleFactor = (int) Math.max(1, Math.min(3, Math.ceil(totalLength / 6)));
            // Trace
            //System.out.print(this.file.getName() + ", bitmask: " + bitMask + " (" + bitMask.length() + ")" + ", pattern length: " + totalLength + ", num segements: " + lengths.size() + " - ");
            //for (Double d : lengths)
            //    System.out.print(d + ", ");
            //System.out.println("");
        }

        protected String getLastValue(String line)
        {
            String[] tokens = line.replaceAll("\"", "").replaceAll(";", "").split("\\s");
            return tokens[tokens.length - 1];
        }

        protected BufferedImage getPattern()
        {
            if (this.patternTable == null)
                return null;

            Color color = Color.decode(getColor(this.fillColorIndex));
            int width = this.patternTable[1];
            int height = this.patternTable[2];
            int idx = 4; // bit data start
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    if (this.patternTable[idx++] > 0)
                        image.setRGB(x, y, color.getRGB());
                }
            }

            return image;
        }

        public String toRecordString()
        {
            String code = this.file.getName().split("\\.")[0];
            String type = this.type;
            String lineWidth = "";
            String lineColor = "";
            String stipplePattern = "";
            String stippleFactor = "";
            String fillColor = "";

            if (this.type.equals(TYPE_AREA_PATTERN) || this.type.equals(TYPE_AREA_PLAIN))
            {
                // Set fill color for patterns too as a backup
                fillColor = getColor(this.fillColorIndex);
            }
            else if (this.type.equals(TYPE_LINE_PLAIN) || this.type.equals(TYPE_LINE_COMPLEX))
            {
                // Use .5 if no line width found
                lineWidth = this.lineWidth > 0 ? String.format("%.1f", this.lineWidth * this.scale).replace(',', '.')
                    : ".5";
                // Use fill color if no line color found
                lineColor = this.lineColorIndex >= 0 ? getColor(this.lineColorIndex) : getColor(this.fillColorIndex);
                if (this.type.equals(TYPE_LINE_COMPLEX))
                {
                    stipplePattern = String.format("#%04x", this.stipplePattern);
                    stippleFactor = "" + this.stippleFactor;
                }
            }

            return String.format("%s,%s,%s,%s,%s,%s,%s", code, type, lineWidth, lineColor, stipplePattern,
                stippleFactor, fillColor);
        }

        public String readTextFile(File file)
        {
            if (file == null)
            {
                String msg = Logging.getMessage("nullValue.FileIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            StringBuilder sb = new StringBuilder();

            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator")); // add back line separator
                }
            }
            catch (IOException e)
            {
                String msg = Logging.getMessage("generic.ExceptionAttemptingToReadFile", file.getPath());
                Logging.logger().log(java.util.logging.Level.SEVERE, msg);
                return null;
            }
            finally
            {
                WWIO.closeStream(reader, file.getPath());
            }

            return sb.toString();
        }
    }

    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
        {
            printUsage();
            return;
        }

        File[] cgmFiles = new File(args[0]).listFiles();

        PrintStream outAttrs = null;
        try
        {
            File outFile = new File(OUT_ATTRS_PATH);
            //noinspection ResultOfMethodCallIgnored
            outFile.getParentFile().mkdirs();

            outAttrs = new PrintStream(new FileOutputStream(outFile)); // CSV Output
            outAttrs.println("# GeoSym line and area attributes");
            outAttrs.println(
                "# GeoSym code, Feature type, Line width, Line color, Stipple pattern, Stipple factor, Fill color");
            outAttrs.println("#");

            for (File file : cgmFiles)
            {
                if (file.getName().toUpperCase().endsWith(".CGM"))
                {
                    CGMFile cgmf = new CGMFile(file);

                    if (!cgmf.type.equals(TYPE_POINT))
                        outAttrs.println(cgmf.toRecordString());

                    if (cgmf.type.equals(TYPE_AREA_PATTERN))
                        writeAreaPattern(cgmf);
                }
            }

            System.out.println("Done.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            WWIO.closeStream(outAttrs, OUT_ATTRS_PATH);
        }
    }

    protected static final String OUT_DIR = "gsac-out";
    protected static final String OUT_ATTRS_PATH = OUT_DIR + "/geosym/symasgn/ascii/geosym-line-area-attr.csv";
    protected static final String OUT_PATTERNS_PATH = OUT_DIR + "/geosym/graphics/bin";

    protected static void printUsage()
    {
        System.out.println("GeoSymAttributeConverter");
        System.out.println();
        System.out.println("Converts GeoSym line attributes, area attributes, and area patterns into a form usable by");
        System.out.println("World Wind VPF shapes. Outputs to \"" + OUT_DIR + "\" a comma-separated-value file");
        System.out.println("containing line and area attributes for VPF line and area shapes, and PNG files");
        System.out.println("containing area patterns for VPF area shapes.");
        System.out.println();
        System.out.println(
            "Usage: java -cp worldwind.jar gov.nasa.worldwind.formats.vpf.GeoSymAttributeConverter [full path to \"GeoSymEd2Final/GRAPHICS/CTEXT\"]");
        System.out.println();
    }

    protected static void writeAreaPattern(CGMFile cgmf)
    {
        String fileName = cgmf.file.getName().toUpperCase().replace(".CGM", ".png");
        File outFile = new File(OUT_PATTERNS_PATH, fileName);

        try
        {
            //noinspection ResultOfMethodCallIgnored
            outFile.getParentFile().mkdirs();
            ImageIO.write(cgmf.getPattern(), "png", outFile);
        }
        catch (Exception e)
        {
            System.out.println("Could not save pattern " + fileName);
        }
    }
}
