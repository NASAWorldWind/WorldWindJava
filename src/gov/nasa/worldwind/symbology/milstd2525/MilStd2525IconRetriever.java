/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

/**
 * Retriever to retrieve icons for symbols in the MIL-STD-2525 symbol set. The retriever can retrieve icons from either
 * a local or remote symbol store. See the <a href="http://goworldwind.org/developers-guide/symbology/tactical-symbols/#offline-use">Symbology
 * Usage Guide</a> for details on how to configure a local symbol repository. For more information on how to use this
 * class see the IconRetriever Usage Guide and the {@link gov.nasa.worldwindx.examples.symbology.IconRetrieverUsage}
 * example.
 * <p/>
 * <h2><a name="parameters">Retrieval parameters</a></h2>
 * <p/>
 * Table IX (pg. 35) of MIL-STD-2525C defines a hierarchy for simplifying tactical symbols. This hierarchy is
 * implemented using retrieval parameters SHOW_FILL, SHOW_FRAME, and SHOW_ICON. By default, all three elements are
 * displayed, and they can be turned off by setting the appropriate parameter. If frame and icon are turned off the
 * retriever will return an image that contains a circle, either black or filled with the icon fill color (depending on
 * the state of SHOW_FILL).
 * <p/>
 * {@link #createIcon(String, gov.nasa.worldwind.avlist.AVList) createIcon} accepts the following parameters:
 * <p/>
 * <table> <tr><th>Key</th><th>Type</th><td>Description</th></tr> <tr><td>SymbologyConstants.SHOW_ICON</td><td>Boolean</td><td>Determines
 * if the symbol will be created with an icon.</td></tr> <tr><td>SymbologyConstants.SHOW_FRAME</td><td>Boolean</td><td>Determines
 * if the symbol will be created with a frame.</td></tr> <tr><td>SymbologyConstants.SHOW_FILL</td><td>Boolean</td><td>Determines
 * if the symbol will be created with a fill color.</td></tr><tr><td valign="top">AVKey.COLOR</td><td
 * valign="top">java.awt.Color</td><td valign="top">Fill color applied to the symbol. If the symbol is drawn with a
 * frame, then this color will be used to fill the frame. If the symbol is not drawn with a frame, then the fill will be
 * applied to the icon itself. The fill color has no effect if Show Fill is False.</td></tr> </table>
 *
 * @author ccrick
 * @version $Id: MilStd2525IconRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MilStd2525IconRetriever extends AbstractIconRetriever
{
    protected static final String FILLS_PATH = "fills";
    protected static final String FRAMES_PATH = "frames";
    protected static final String ICONS_PATH = "icons";
    protected static final String TACTICAL_SYMBOLS_PATH = "tacsym";
    protected static final String UNKNOWN_PATH = "unk";

    protected static final Color FILL_COLOR_LIGHT_RED = new Color(255, 128, 128);
    protected static final Color FILL_COLOR_LIGHT_BLUE = new Color(128, 224, 255);
    protected static final Color FILL_COLOR_LIGHT_GREEN = new Color(170, 255, 170);
    protected static final Color FILL_COLOR_LIGHT_YELLOW = new Color(255, 255, 128);
    protected static final Color FILL_COLOR_LIGHT_PURPLE = new Color(255, 161, 255);

    protected static final Color FRAME_COLOR_RED = new Color(255, 0, 0);
    protected static final Color FRAME_COLOR_BLUE = new Color(0, 255, 255);
    protected static final Color FRAME_COLOR_GREEN = new Color(0, 255, 0);
    protected static final Color FRAME_COLOR_YELLOW = new Color(255, 255, 0);
    protected static final Color FRAME_COLOR_PURPLE = new Color(255, 0, 255);

    protected static final Color ICON_COLOR_RED = new Color(255, 0, 0);
    protected static final Color ICON_COLOR_ORANGE = new Color(255, 140, 0);
    protected static final Color ICON_COLOR_GREEN = new Color(0, 255, 0);
    protected static final Color ICON_COLOR_DARK_GREEN = new Color(0, 128, 0);
    protected static final Color ICON_COLOR_YELLOW = new Color(255, 255, 0);

    protected static final Color DEFAULT_FRAME_COLOR = Color.BLACK;
    protected static final Color DEFAULT_ICON_COLOR = Color.BLACK;
    protected static final String DEFAULT_IMAGE_FORMAT = "image/png";

    /** Radius (in pixels) of circle that is drawn to the represent the symbol when both frame and icon are off. */
    protected static final int CIRCLE_RADIUS = 16;
    /** Line width used to stroke circle when fill is turned off. */
    protected static final int CIRCLE_LINE_WIDTH = 2;

    // Static maps and sets providing fast access to attributes about a symbol ID. These data structures are populated
    // in a static block at the bottom of this class.
    protected static final Map<String, String> schemePathMap = new HashMap<String, String>();
    protected static final Map<String, Color> fillColorMap = new HashMap<String, Color>();
    protected static final Map<String, Color> frameColorMap = new HashMap<String, Color>();
    protected static final Map<String, Color> iconColorMap = new HashMap<String, Color>();
    protected static final Set<String> unfilledIconMap = new HashSet<String>();
    protected static final Set<String> unframedIconMap = new HashSet<String>();
    protected static final Set<String> emsEquipment = new HashSet<String>();

    /**
     * Create a new retriever that will retrieve icons from the specified location. The retrieval path may be a file URL
     * to a directory on the local file system (for example, file:///symbols/mil-std-2525). A URL to a network resource
     * (http://myserver.com/milstd2525/), or a URL to a JAR or ZIP file (jar:file:milstd2525-symbols.zip!).
     *
     * @param retrieverPath File path or URL to the symbol directory, for example "http://myserver.com/milstd2525/".
     */
    public MilStd2525IconRetriever(String retrieverPath)
    {
        super(retrieverPath);
    }

    /**
     * Create an icon for a MIL-STD-2525C symbol. By default the symbol will include a filled frame and an icon. The
     * fill, frame, and icon can be turned off by setting retrieval parameters. If both frame and icon are turned off
     * then this method will return an image containing a circle.
     *
     * @param sidc   SIDC identifier for the symbol.
     * @param params Parameters that affect icon retrieval. See <a href="#parameters">Parameters</a> in class
     *               documentation.
     *
     * @return An BufferedImage containing the icon for the requested symbol, or null if the icon cannot be retrieved.
     */
    public BufferedImage createIcon(String sidc, AVList params)
    {
        if (sidc == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        SymbolCode symbolCode = new SymbolCode(sidc);
        BufferedImage image = null;

        boolean mustDrawFill = this.mustDrawFill(symbolCode, params);
        boolean mustDrawIcon = this.mustDrawIcon(symbolCode, params);
        boolean mustDrawFrame = this.mustDrawFrame(symbolCode, params);

        if (mustDrawFrame || mustDrawIcon)
        {
            if (mustDrawFill && mustDrawFrame)
                image = this.drawFill(symbolCode, params, null);

            if (mustDrawFrame)
                image = this.drawFrame(symbolCode, params, image);

            if (mustDrawIcon)
                image = this.drawIcon(symbolCode, params, image);
        }

        // Draw a dot if both frame and icon are turned off
        if (image == null)
            image = this.drawCircle(symbolCode, params, image);

        return image;
    }

    protected boolean mustDrawFill(SymbolCode symbolCode, AVList params)
    {
        String maskedCode = symbolCode.toMaskedString().toLowerCase();
        if (unfilledIconMap.contains(maskedCode))
            return false;

        Object o = params != null ? params.getValue(SymbologyConstants.SHOW_FILL) : null;
        return o == null || o.equals(Boolean.TRUE);
    }

    protected boolean mustDrawFrame(SymbolCode symbolCode, AVList params)
    {
        String maskedCode = symbolCode.toMaskedString().toLowerCase();
        if (unframedIconMap.contains(maskedCode))
            return false;

        Object o = params != null ? params.getValue(SymbologyConstants.SHOW_FRAME) : null;
        return o == null || o.equals(Boolean.TRUE);
    }

    @SuppressWarnings({"UnusedParameters"})
    protected boolean mustDrawIcon(SymbolCode symbolCode, AVList params)
    {
        Object o = params != null ? params.getValue(SymbologyConstants.SHOW_ICON) : null;
        return o == null || o.equals(Boolean.TRUE);
    }

    protected BufferedImage drawFill(SymbolCode symbolCode, AVList params, BufferedImage dest)
    {
        String path = this.composeFillPath(symbolCode);
        Color color = this.getFillColor(symbolCode, params);

        return path != null ? this.drawIconComponent(path, color, dest) : dest;
    }

    protected BufferedImage drawFrame(SymbolCode symbolCode, AVList params, BufferedImage dest)
    {
        String path = this.composeFramePath(symbolCode);
        Color color = this.getFrameColor(symbolCode, params);

        return path != null ? this.drawIconComponent(path, color, dest) : dest;
    }

    protected BufferedImage drawIcon(SymbolCode symbolCode, AVList params, BufferedImage dest)
    {
        String path = this.composeIconPath(symbolCode, params);
        Color color = this.getIconColor(symbolCode, params);

        return path != null ? this.drawIconComponent(path, color, dest) : dest;
    }

    protected BufferedImage drawCircle(SymbolCode symbolCode, AVList params, BufferedImage dest)
    {
        Color frameColor = DEFAULT_FRAME_COLOR;
        Color fillColor = this.mustDrawFill(symbolCode, params) ? this.getFillColor(symbolCode, params)
            : DEFAULT_ICON_COLOR;

        if (dest == null)
        {
            int diameter = CIRCLE_RADIUS * 2;
            dest = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g = null;
        try
        {
            g = dest.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int upperLeft = (int) (dest.getWidth() / 2.0 - CIRCLE_RADIUS + CIRCLE_LINE_WIDTH);
            int width = CIRCLE_RADIUS * 2 - CIRCLE_LINE_WIDTH * 2;
            @SuppressWarnings({"SuspiciousNameCombination"})
            Ellipse2D circle = new Ellipse2D.Double(upperLeft, upperLeft, width, width);

            // Draw filled circle
            g.setColor(fillColor);
            g.fill(circle);

            // Draw the circle's border. Always draw the circle with a solid border, even if the status is not Present.
            // MIL-STD-2525C section 5.3.1.4 (pg. 18) states: "Planned status cannot be shown if the symbol is [...]
            // displayed as a dot."
            g.setColor(frameColor);
            g.setStroke(new BasicStroke(CIRCLE_LINE_WIDTH));
            g.draw(circle);
        }
        finally
        {
            if (g != null)
                g.dispose();
        }

        return dest;
    }

    protected BufferedImage drawIconComponent(String path, Color color, BufferedImage dest)
    {
        BufferedImage image = this.readImage(path);
        if (image == null)
        {
            String msg = Logging.getMessage("Symbology.MissingIconComponent", path);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (color != null)
            this.multiply(image, color);

        if (dest != null)
            image = this.drawImage(image, dest);

        return image;
    }

    protected String composeFillPath(SymbolCode symbolCode)
    {
        String maskedCode = this.getMaskedFillCode(symbolCode);

        StringBuilder sb = new StringBuilder();
        sb.append(FILLS_PATH).append("/");
        sb.append(TACTICAL_SYMBOLS_PATH).append("/");
        sb.append(maskedCode.toLowerCase());
        sb.append(WWIO.makeSuffixForMimeType(DEFAULT_IMAGE_FORMAT));

        return sb.toString();
    }

    protected String composeFramePath(SymbolCode symbolCode)
    {
        String maskedCode = this.getMaskedFrameCode(symbolCode);

        StringBuilder sb = new StringBuilder();
        sb.append(FRAMES_PATH).append("/");
        sb.append(TACTICAL_SYMBOLS_PATH).append("/");
        sb.append(maskedCode.toLowerCase());
        sb.append(WWIO.makeSuffixForMimeType(DEFAULT_IMAGE_FORMAT));

        return sb.toString();
    }

    protected String composeIconPath(SymbolCode symbolCode, AVList params)
    {
        String scheme = symbolCode.getScheme();
        String bd = symbolCode.getBattleDimension();

        if (bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_UNKNOWN))
        {
            String maskedCode = this.getMaskedUnknownIconCode(symbolCode, params);
            StringBuilder sb = new StringBuilder();
            sb.append(ICONS_PATH).append("/");
            sb.append(UNKNOWN_PATH).append("/");
            sb.append(maskedCode.toLowerCase());
            sb.append(WWIO.makeSuffixForMimeType(DEFAULT_IMAGE_FORMAT));
            return sb.toString();
        }
        else
        {
            if (SymbolCode.isFieldEmpty(symbolCode.getFunctionId()))
                return null; // Don't draw an icon if the function ID is empty.

            String maskedCode = this.getMaskedIconCode(symbolCode, params);
            StringBuilder sb = new StringBuilder();
            sb.append(ICONS_PATH).append("/");
            sb.append(schemePathMap.get(scheme.toLowerCase())).append("/");
            sb.append(maskedCode.toLowerCase());
            sb.append(WWIO.makeSuffixForMimeType(DEFAULT_IMAGE_FORMAT));
            return sb.toString();
        }
    }

    protected Color getFillColor(SymbolCode symbolCode, AVList params)
    {
        Color color = this.getColorFromParams(params);
        return color != null ? color : fillColorMap.get(symbolCode.getStandardIdentity().toLowerCase());
    }

    protected Color getFrameColor(SymbolCode symbolCode, AVList params)
    {
        if (this.isDashedFrame(symbolCode))
            return null; // Dashed pending or exercise frames are not colored.

        if (this.mustDrawFill(symbolCode, params))
            return DEFAULT_FRAME_COLOR; // Use the default color if the fill is on.

        Color color = this.getColorFromParams(params);
        return color != null ? color : frameColorMap.get(symbolCode.getStandardIdentity().toLowerCase());
    }

    protected Color getIconColor(SymbolCode symbolCode, AVList params)
    {
        String maskedCode = symbolCode.toMaskedString().toLowerCase();

        if (this.mustDrawFrame(symbolCode, params))
        {
            // When the frame is enabled, we draw the icon in either its specified custom color or the default color. In
            // this case the app-specified color override (if any) is applied to the frame, and does apply to the icon.
            return iconColorMap.containsKey(maskedCode) ? iconColorMap.get(maskedCode) : DEFAULT_ICON_COLOR;
        }
        else if (this.mustDrawFill(symbolCode, params))
        {
            // When the frame is disabled and the fill is enabled, we draw the icon in its corresponding standard
            // identity color (or app-specified color override).
            Color color = this.getColorFromParams(params);
            return color != null ? color : fillColorMap.get(symbolCode.getStandardIdentity().toLowerCase());
        }
        else
        {
            // When the frame is disabled and the fill is disabled, we draw the icon in either its specified custom
            // color or the default color. In this case the app-specified color override (if any) is ignored.
            return iconColorMap.containsKey(maskedCode) ? iconColorMap.get(maskedCode) : DEFAULT_ICON_COLOR;
        }
    }

    /**
     * Retrieves the value of the AVKey.COLOR parameter.
     *
     * @param params Parameter list.
     *
     * @return The value of the AVKey.COLOR parameter, if such a parameter exists and is of type java.awt.Color. Returns
     *         null if the parameter list is null, if there is no value for key AVKey.COLOR, or if the value is not a
     *         Color.
     */
    protected Color getColorFromParams(AVList params)
    {
        if (params == null)
            return null;

        Object o = params.getValue(AVKey.COLOR);
        return (o instanceof Color) ? (Color) o : null;
    }

    protected String getMaskedFillCode(SymbolCode symbolCode)
    {
        // Transform the symbol code to its equivalent code in the Warfighting scheme. This ensures that we can use
        // the generic fill shape lookup logic used by Warfighting symbols.
        symbolCode = this.transformToWarfightingScheme(symbolCode);

        String si = this.getSimpleStandardIdentity(symbolCode); // Either Unknown, Friend, Neutral, or Hostile
        String bd = symbolCode.getBattleDimension();
        String fid = this.getGroundFunctionId(symbolCode);

        StringBuilder sb = new StringBuilder();
        SymbolCode.appendFieldValue(sb, null, 1); // Scheme
        SymbolCode.appendFieldValue(sb, si, 1); // Standard Identity
        SymbolCode.appendFieldValue(sb, bd, 1); // Battle Dimension
        SymbolCode.appendFieldValue(sb, null, 1); // Status
        SymbolCode.appendFieldValue(sb, fid, 6); // Function ID
        SymbolCode.appendFieldValue(sb, null, 2); // Symbol Modifier
        SymbolCode.appendFieldValue(sb, null, 2); // Country Code
        SymbolCode.appendFieldValue(sb, null, 1); // Order of Battle

        return sb.toString();
    }

    protected String getMaskedFrameCode(SymbolCode symbolCode)
    {
        // Transform the symbol code to its equivalent code in the Warfighting scheme. This ensures that we can use
        // the generic fill shape lookup logic used by Warfighting symbols.
        symbolCode = this.transformToWarfightingScheme(symbolCode);

        String si = symbolCode.getStandardIdentity();
        String bd = symbolCode.getBattleDimension();
        String status = this.getSimpleStatus(symbolCode); // Either Present or Anticipated
        String fid = this.getGroundFunctionId(symbolCode); // Either "U-----", "E-----", "I-----", or null

        StringBuilder sb = new StringBuilder();
        SymbolCode.appendFieldValue(sb, null, 1); // Scheme
        SymbolCode.appendFieldValue(sb, si, 1); // Standard Identity
        SymbolCode.appendFieldValue(sb, bd, 1); // Battle Dimension
        SymbolCode.appendFieldValue(sb, status, 1); // Status
        SymbolCode.appendFieldValue(sb, fid, 6); // Function ID
        SymbolCode.appendFieldValue(sb, null, 2); // Symbol Modifier
        SymbolCode.appendFieldValue(sb, null, 2); // Country Code
        SymbolCode.appendFieldValue(sb, null, 1); // Order of Battle

        return sb.toString();
    }

    protected SymbolCode transformToWarfightingScheme(SymbolCode symbolCode)
    {
        String maskedCode = symbolCode.toMaskedString().toLowerCase();
        String scheme = symbolCode.getScheme();
        String bd = symbolCode.getBattleDimension();

        SymbolCode newCode = new SymbolCode();
        newCode.setScheme(SymbologyConstants.SCHEME_WARFIGHTING);
        newCode.setStandardIdentity(symbolCode.getStandardIdentity());
        newCode.setStatus(symbolCode.getStatus());

        if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_INTELLIGENCE))
        {
            newCode.setBattleDimension(bd);

            // Signals Intelligence ground symbols are equivalent to Warfighting ground equipment.
            if (bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND))
                newCode.setFunctionId("E-----");

            return newCode;
        }
        else if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_STABILITY_OPERATIONS))
        {
            // Stability Operations symbols frames are equivalent to Warfighting ground units.
            newCode.setBattleDimension(SymbologyConstants.BATTLE_DIMENSION_GROUND);
            newCode.setFunctionId("U-----");

            return newCode;
        }
        else if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT))
        {
            // Emergency Management symbol frames are equivalent to either Warfighting ground units or ground equipment.
            newCode.setBattleDimension(SymbologyConstants.BATTLE_DIMENSION_GROUND);
            newCode.setFunctionId(emsEquipment.contains(maskedCode) ? "E-----" : "U-----");

            return newCode;
        }
        else
        {
            return symbolCode;
        }
    }

    protected String getMaskedIconCode(SymbolCode symbolCode, AVList params)
    {
        String si = this.getSimpleStandardIdentity(symbolCode); // Either Unknown, Friend, Neutral, or Hostile.
        String status = this.getSimpleStatus(symbolCode); // Either Present or Anticipated.

        if (this.mustDrawFrame(symbolCode, params))
            status = SymbologyConstants.STATUS_PRESENT;

        SymbolCode maskedCode = new SymbolCode(symbolCode.toString());
        maskedCode.setStandardIdentity(si);
        maskedCode.setStatus(status);
        maskedCode.setSymbolModifier(null); // Ignore the Symbol Modifier field.
        maskedCode.setCountryCode(null); // Ignore the Country Code field.
        maskedCode.setOrderOfBattle(null); // Ignore the Order of Battle field.

        return maskedCode.toString();
    }

    protected String getMaskedUnknownIconCode(SymbolCode symbolCode, AVList params)
    {
        String si = this.getSimpleStandardIdentity(symbolCode); // Either Unknown, Friend, Neutral, or Hostile.
        String bd = symbolCode.getBattleDimension();
        String status = this.getSimpleStatus(symbolCode); // Either Present or Anticipated.

        if (this.mustDrawFrame(symbolCode, params))
            status = SymbologyConstants.STATUS_PRESENT;

        StringBuilder sb = new StringBuilder();
        SymbolCode.appendFieldValue(sb, null, 1); // Scheme
        SymbolCode.appendFieldValue(sb, si, 1); // Standard Identity
        SymbolCode.appendFieldValue(sb, bd, 1); // Battle Dimension
        SymbolCode.appendFieldValue(sb, status, 1); // Status
        SymbolCode.appendFieldValue(sb, null, 6); // Function ID
        SymbolCode.appendFieldValue(sb, null, 2); // Symbol Modifier
        SymbolCode.appendFieldValue(sb, null, 2); // Country Code
        SymbolCode.appendFieldValue(sb, null, 1); // Order of Battle

        return sb.toString();
    }

    protected boolean isDashedFrame(SymbolCode symbolCode)
    {
        String si = symbolCode.getStandardIdentity();
        return si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_PENDING)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_ASSUMED_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_SUSPECT)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_PENDING)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_ASSUMED_FRIEND));
    }

    protected String getSimpleStandardIdentity(SymbolCode symbolCode)
    {
        String si = symbolCode.getStandardIdentity();
        if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_PENDING)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_UNKNOWN)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_PENDING)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_UNKNOWN)))
        {
            return SymbologyConstants.STANDARD_IDENTITY_UNKNOWN;
        }
        else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_ASSUMED_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_ASSUMED_FRIEND)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_JOKER)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_FAKER)))
        {
            return SymbologyConstants.STANDARD_IDENTITY_FRIEND;
        }
        else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_NEUTRAL)
            || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_NEUTRAL)))
        {
            return SymbologyConstants.STANDARD_IDENTITY_NEUTRAL;
        }
        else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_HOSTILE) ||
            si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_SUSPECT)))
        {
            return SymbologyConstants.STANDARD_IDENTITY_HOSTILE;
        }

        return si;
    }

    protected String getSimpleStatus(SymbolCode symbolCode)
    {
        String status = symbolCode.getStatus();

        if (status != null && status.equalsIgnoreCase(SymbologyConstants.STATUS_ANTICIPATED))
            return SymbologyConstants.STATUS_ANTICIPATED;
        else
            return SymbologyConstants.STATUS_PRESENT;
    }

    protected String getGroundFunctionId(SymbolCode symbolCode)
    {
        String scheme = symbolCode.getScheme();
        String bd = symbolCode.getBattleDimension();
        String fid = symbolCode.getFunctionId();

        if (scheme != null && scheme.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING)
            && bd != null && bd.equalsIgnoreCase(SymbologyConstants.BATTLE_DIMENSION_GROUND))
        {
            if (fid != null && fid.toLowerCase().startsWith("u"))
                return "u-----";
            else if (fid != null && fid.toLowerCase().startsWith("e"))
                return "e-----";
            else if (fid != null && fid.toLowerCase().startsWith("i"))
                return "i-----";
        }

        return null;
    }

    static
    {
        schemePathMap.put("s", "war"); // Scheme Warfighting
        schemePathMap.put("i", "sigint"); // Scheme Signals Intelligence
        schemePathMap.put("o", "stbops"); // Scheme Stability Operations
        schemePathMap.put("e", "ems"); // Scheme Emergency Management

        // The MIL-STD-2525 symbol fill colors for each Standard Identity.
        fillColorMap.put("p", FILL_COLOR_LIGHT_YELLOW); // Standard Identity Pending
        fillColorMap.put("u", FILL_COLOR_LIGHT_YELLOW); // Standard Identity Unknown
        fillColorMap.put("f", FILL_COLOR_LIGHT_BLUE); // Standard Identity Friend
        fillColorMap.put("n", FILL_COLOR_LIGHT_GREEN); // Standard Identity Neutral
        fillColorMap.put("h", FILL_COLOR_LIGHT_RED); // Standard Identity Hostile
        fillColorMap.put("a", FILL_COLOR_LIGHT_BLUE); // Standard Identity Assumed Friend
        fillColorMap.put("s", FILL_COLOR_LIGHT_RED); // Standard Identity Suspect
        fillColorMap.put("g", FILL_COLOR_LIGHT_YELLOW); // Standard Identity Exercise Pending
        fillColorMap.put("w", FILL_COLOR_LIGHT_YELLOW); // Standard Identity Exercise Unknown
        fillColorMap.put("d", FILL_COLOR_LIGHT_BLUE); // Standard Identity Exercise Friend
        fillColorMap.put("l", FILL_COLOR_LIGHT_GREEN); // Standard Identity Exercise Neutral
        fillColorMap.put("m", FILL_COLOR_LIGHT_BLUE); // Standard Identity Exercise Assumed Friend
        fillColorMap.put("j", FILL_COLOR_LIGHT_RED); // Standard Identity Joker
        fillColorMap.put("k", FILL_COLOR_LIGHT_RED); // Standard Identity Faker

        // The MIL-STD-2525 symbol frame colors for each Standard Identity.
        frameColorMap.put("p", FRAME_COLOR_YELLOW); // Standard Identity Pending
        frameColorMap.put("u", FRAME_COLOR_YELLOW); // Standard Identity Unknown
        frameColorMap.put("f", FRAME_COLOR_BLUE); // Standard Identity Friend
        frameColorMap.put("n", FRAME_COLOR_GREEN); // Standard Identity Neutral
        frameColorMap.put("h", FRAME_COLOR_RED); // Standard Identity Hostile
        frameColorMap.put("a", FRAME_COLOR_BLUE); // Standard Identity Assumed Friend
        frameColorMap.put("s", FRAME_COLOR_RED); // Standard Identity Suspect
        frameColorMap.put("g", FRAME_COLOR_YELLOW); // Standard Identity Exercise Pending
        frameColorMap.put("w", FRAME_COLOR_YELLOW); // Standard Identity Exercise Unknown
        frameColorMap.put("d", FRAME_COLOR_BLUE); // Standard Identity Exercise Friend
        frameColorMap.put("l", FRAME_COLOR_GREEN); // Standard Identity Exercise Neutral
        frameColorMap.put("m", FRAME_COLOR_BLUE); // Standard Identity Exercise Assumed Friend
        frameColorMap.put("j", FRAME_COLOR_RED); // Standard Identity Joker
        frameColorMap.put("k", FRAME_COLOR_RED); // Standard Identity Faker

        // The MIL-STD-2525 symbol icon colors for each icon that has either a white or colored fill. White is denoted
        // as a null value.
        iconColorMap.put("e-f-a----------", null);
        iconColorMap.put("e-f-aa---------", null);
        iconColorMap.put("e-f-ab---------", null);
        iconColorMap.put("e-f-ad---------", null);
        iconColorMap.put("e-f-ag---------", null);
        iconColorMap.put("e-f-ba---------", null);
        iconColorMap.put("e-f-bb---------", null);
        iconColorMap.put("e-f-bc---------", null);
        iconColorMap.put("e-f-bd---------", null);
        iconColorMap.put("e-f-c----------", null);
        iconColorMap.put("e-f-ca---------", null);
        iconColorMap.put("e-f-cb---------", null);
        iconColorMap.put("e-f-cc---------", null);
        iconColorMap.put("e-f-cd---------", null);
        iconColorMap.put("e-f-ce---------", null);
        iconColorMap.put("e-f-cf---------", null);
        iconColorMap.put("e-f-cg---------", null);
        iconColorMap.put("e-f-ch---------", null);
        iconColorMap.put("e-f-ci---------", null);
        iconColorMap.put("e-f-cj---------", null);
        iconColorMap.put("e-f-ee---------", null);
        iconColorMap.put("e-f-f----------", null);
        iconColorMap.put("e-f-g----------", null);
        iconColorMap.put("e-f-h----------", null);
        iconColorMap.put("e-f-ha---------", null);
        iconColorMap.put("e-f-hb---------", null);
        iconColorMap.put("e-f-ia---------", null);
        iconColorMap.put("e-f-id---------", null);
        iconColorMap.put("e-f-jb---------", null);
        iconColorMap.put("e-f-ld---------", null);
        iconColorMap.put("e-f-le---------", null);
        iconColorMap.put("e-f-lf---------", null);
        iconColorMap.put("e-f-lm---------", null);
        iconColorMap.put("e-f-lo---------", null);
        iconColorMap.put("e-f-lp---------", null);
        iconColorMap.put("e-f-me---------", null);
        iconColorMap.put("e-f-mf---------", null);
        iconColorMap.put("e-f-mg---------", null);
        iconColorMap.put("e-f-mh---------", null);
        iconColorMap.put("e-f-mi---------", null);
        iconColorMap.put("e-i-b----------", null);
        iconColorMap.put("e-i-ca---------", null);
        iconColorMap.put("e-i-cc---------", null);
        iconColorMap.put("e-i-d----------", null);
        iconColorMap.put("e-i-da---------", new Color(255, 254, 111));
        iconColorMap.put("e-i-dc---------", null);
        iconColorMap.put("e-i-dd---------", null);
        iconColorMap.put("e-i-de---------", null);
        iconColorMap.put("e-i-df---------", null);
        iconColorMap.put("e-i-dg---------", null);
        iconColorMap.put("e-i-dh---------", null);
        iconColorMap.put("e-i-di---------", null);
        iconColorMap.put("e-i-dj---------", null);
        iconColorMap.put("e-i-dm---------", null);
        iconColorMap.put("e-i-e----------", null);
        iconColorMap.put("e-i-ea---------", null);
        iconColorMap.put("e-i-f----------", null);
        iconColorMap.put("e-i-fa---------", null);
        iconColorMap.put("e-o-ae---------", null);
        iconColorMap.put("e-o-af---------", null);
        iconColorMap.put("e-o-aj---------", null);
        iconColorMap.put("e-o-ak---------", null);
        iconColorMap.put("e-o-am---------", null);
        iconColorMap.put("e-o-b----------", null);
        iconColorMap.put("e-o-ba---------", null);
        iconColorMap.put("e-o-bb---------", null);
        iconColorMap.put("e-o-bc---------", null);
        iconColorMap.put("e-o-bd---------", null);
        iconColorMap.put("e-o-be---------", null);
        iconColorMap.put("e-o-bf---------", null);
        iconColorMap.put("e-o-bg---------", null);
        iconColorMap.put("e-o-bh---------", null);
        iconColorMap.put("e-o-bi---------", null);
        iconColorMap.put("e-o-bj---------", null);
        iconColorMap.put("e-o-cc---------", null);
        iconColorMap.put("e-o-cd---------", null);
        iconColorMap.put("e-o-de---------", null);
        iconColorMap.put("e-o-dea--------", null);
        iconColorMap.put("e-o-deb--------", null);
        iconColorMap.put("e-o-dec--------", null);
        iconColorMap.put("e-o-df---------", null);
        iconColorMap.put("e-o-dfa--------", null);
        iconColorMap.put("e-o-dfb--------", null);
        iconColorMap.put("e-o-dfc--------", null);
        iconColorMap.put("e-o-dk---------", null);
        iconColorMap.put("e-o-dn---------", null);
        iconColorMap.put("e-o-dna--------", null);
        iconColorMap.put("e-o-dnc--------", null);
        iconColorMap.put("e-o-do---------", null);
        iconColorMap.put("e-o-doa--------", null);
        iconColorMap.put("e-o-dob--------", null);
        iconColorMap.put("e-o-doc--------", null);
        iconColorMap.put("o-o-ha---------", null);
        iconColorMap.put("o-o-hv---------", null);
        iconColorMap.put("o-o-y----------", null);
        iconColorMap.put("o-o-yh---------", null);
        iconColorMap.put("o-o-yt---------", null);
        iconColorMap.put("o-o-yw---------", null);
        iconColorMap.put("s-a-cf---------", null);
        iconColorMap.put("s-a-ch---------", null);
        iconColorMap.put("s-a-cl---------", null);
        iconColorMap.put("s-a-w----------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wm---------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wma--------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmaa-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmap-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmas-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmb--------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmcm-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wms--------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmsa-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmsb-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmss-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmsu-------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-a-wmu--------", FILL_COLOR_LIGHT_YELLOW);
        iconColorMap.put("s-f-gp---------", null);
        iconColorMap.put("s-f-gpa--------", null);
        iconColorMap.put("s-f-nb---------", null);
        iconColorMap.put("s-g-evca-------", null);
        iconColorMap.put("s-g-evcah------", null);
        iconColorMap.put("s-g-evcal------", null);
        iconColorMap.put("s-g-evcam------", null);
        iconColorMap.put("s-g-evcf-------", null);
        iconColorMap.put("s-g-evcfh------", null);
        iconColorMap.put("s-g-evcfl------", null);
        iconColorMap.put("s-g-evcfm------", null);
        iconColorMap.put("s-g-evcj-------", null);
        iconColorMap.put("s-g-evcjh------", null);
        iconColorMap.put("s-g-evcjl------", null);
        iconColorMap.put("s-g-evcjm------", null);
        iconColorMap.put("s-g-evcm-------", null);
        iconColorMap.put("s-g-evcmh------", null);
        iconColorMap.put("s-g-evcml------", null);
        iconColorMap.put("s-g-evcmm------", null);
        iconColorMap.put("s-g-evco-------", null);
        iconColorMap.put("s-g-evcoh------", null);
        iconColorMap.put("s-g-evcol------", null);
        iconColorMap.put("s-g-evcom------", null);
        iconColorMap.put("s-g-evct-------", null);
        iconColorMap.put("s-g-evcth------", null);
        iconColorMap.put("s-g-evctl------", null);
        iconColorMap.put("s-g-evctm------", null);
        iconColorMap.put("s-g-evcu-------", null);
        iconColorMap.put("s-g-evcuh------", null);
        iconColorMap.put("s-g-evcul------", null);
        iconColorMap.put("s-g-evcum------", null);
        iconColorMap.put("s-g-ucfs-------", null);
        iconColorMap.put("s-g-ucfsa------", null);
        iconColorMap.put("s-g-ucfsl------", null);
        iconColorMap.put("s-g-ucfso------", null);
        iconColorMap.put("s-g-ucfss------", null);
        iconColorMap.put("s-g-ucfts------", null);
        iconColorMap.put("s-g-uumrs------", null);
        iconColorMap.put("s-g-uumrss-----", null);
        iconColorMap.put("s-g-uusx-------", null);
        iconColorMap.put("s-p-t----------", null);
        iconColorMap.put("s-s-c----------", null);
        iconColorMap.put("s-s-nh---------", null);
        iconColorMap.put("s-s-xa---------", null);
        iconColorMap.put("s-s-xar--------", null);
        iconColorMap.put("s-s-xas--------", null);
        iconColorMap.put("s-s-xf---------", null);
        iconColorMap.put("s-s-xfdf-------", null);
        iconColorMap.put("s-s-xfdr-------", null);
        iconColorMap.put("s-s-xftr-------", null);
        iconColorMap.put("s-s-xh---------", null);
        iconColorMap.put("s-s-xl---------", null);
        iconColorMap.put("s-s-xm---------", null);
        iconColorMap.put("s-s-xmc--------", null);
        iconColorMap.put("s-s-xmf--------", null);
        iconColorMap.put("s-s-xmh--------", null);
        iconColorMap.put("s-s-xmo--------", null);
        iconColorMap.put("s-s-xmp--------", null);
        iconColorMap.put("s-s-xmr--------", null);
        iconColorMap.put("s-s-xmto-------", null);
        iconColorMap.put("s-s-xmtu-------", null);
        iconColorMap.put("s-s-xp---------", null);
        iconColorMap.put("s-s-xr---------", null);
        iconColorMap.put("s-u-e----------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-nd---------", null);
        iconColorMap.put("s-u-sca--------", null);
        iconColorMap.put("s-u-scb--------", null);
        iconColorMap.put("s-u-scg--------", null);
        iconColorMap.put("s-u-scm--------", null);
        iconColorMap.put("s-u-sna--------", null);
        iconColorMap.put("s-u-snb--------", null);
        iconColorMap.put("s-u-sng--------", null);
        iconColorMap.put("s-u-snm--------", null);
        iconColorMap.put("s-u-v----------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wdm--------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wdmg-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wdmm-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wm---------", ICON_COLOR_RED);
        iconColorMap.put("s-u-wma--------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmb--------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmbd-------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wmc--------", ICON_COLOR_ORANGE);
        iconColorMap.put("s-u-wmd--------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wme--------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmf--------", ICON_COLOR_RED);
        iconColorMap.put("s-u-wmfc-------", ICON_COLOR_ORANGE);
        iconColorMap.put("s-u-wmfd-------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wmfe-------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmfo-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmfr-------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmfx-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmg--------", ICON_COLOR_RED);
        iconColorMap.put("s-u-wmgc-------", ICON_COLOR_ORANGE);
        iconColorMap.put("s-u-wmgd-------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wmge-------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmgo-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmgr-------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmgx-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmm--------", ICON_COLOR_RED);
        iconColorMap.put("s-u-wmmc-------", ICON_COLOR_ORANGE);
        iconColorMap.put("s-u-wmmd-------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wmme-------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmmo-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmmr-------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wmmx-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmn--------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmo--------", ICON_COLOR_RED);
        iconColorMap.put("s-u-wmod-------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wmr--------", ICON_COLOR_YELLOW);
        iconColorMap.put("s-u-wms--------", ICON_COLOR_RED);
        iconColorMap.put("s-u-wmsd-------", ICON_COLOR_GREEN);
        iconColorMap.put("s-u-wmsx-------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-wmx--------", ICON_COLOR_DARK_GREEN);
        iconColorMap.put("s-u-x----------", ICON_COLOR_RED);

        // The MIL-STD-2525 symbol icons that are implicitly unfilled.
        unfilledIconMap.add("s-u-wm---------");
        unfilledIconMap.add("s-u-wmd--------");
        unfilledIconMap.add("s-u-wmg--------");
        unfilledIconMap.add("s-u-wmgd-------");
        unfilledIconMap.add("s-u-wmgx-------");
        unfilledIconMap.add("s-u-wmge-------");
        unfilledIconMap.add("s-u-wmgc-------");
        unfilledIconMap.add("s-u-wmgr-------");
        unfilledIconMap.add("s-u-wmgo-------");
        unfilledIconMap.add("s-u-wmm--------");
        unfilledIconMap.add("s-u-wmmd-------");
        unfilledIconMap.add("s-u-wmmx-------");
        unfilledIconMap.add("s-u-wmme-------");
        unfilledIconMap.add("s-u-wmmc-------");
        unfilledIconMap.add("s-u-wmmr-------");
        unfilledIconMap.add("s-u-wmmo-------");
        unfilledIconMap.add("s-u-wmf--------");
        unfilledIconMap.add("s-u-wmfd-------");
        unfilledIconMap.add("s-u-wmfx-------");
        unfilledIconMap.add("s-u-wmfe-------");
        unfilledIconMap.add("s-u-wmfc-------");
        unfilledIconMap.add("s-u-wmfr-------");
        unfilledIconMap.add("s-u-wmfo-------");
        unfilledIconMap.add("s-u-wmo--------");
        unfilledIconMap.add("s-u-wmod-------");
        unfilledIconMap.add("s-u-wmx--------");
        unfilledIconMap.add("s-u-wme--------");
        unfilledIconMap.add("s-u-wma--------");
        unfilledIconMap.add("s-u-wmc--------");
        unfilledIconMap.add("s-u-wmr--------");
        unfilledIconMap.add("s-u-wmb--------");
        unfilledIconMap.add("s-u-wmbd-------");
        unfilledIconMap.add("s-u-wmn--------");
        unfilledIconMap.add("s-u-wms--------");
        unfilledIconMap.add("s-u-wmsx-------");
        unfilledIconMap.add("s-u-wmsd-------");
        unfilledIconMap.add("s-u-wdm--------");
        unfilledIconMap.add("s-u-wdmg-------");
        unfilledIconMap.add("s-u-wdmm-------");
        unfilledIconMap.add("s-u-e----------");
        unfilledIconMap.add("s-u-v----------");
        unfilledIconMap.add("s-u-x----------");

        // The MIL-STD-2525 symbol icons that are implicitly unframed.
        unframedIconMap.add("s-s-o----------");
        unframedIconMap.add("s-u-nd---------");

        // The MIL-STD-2525 Emergency Management symbols representing units.
        emsEquipment.add("e-o-ab---------");
        emsEquipment.add("e-o-ae---------");
        emsEquipment.add("e-o-af---------");
        emsEquipment.add("e-o-bb---------");
        emsEquipment.add("e-o-cb---------");
        emsEquipment.add("e-o-cc---------");
        emsEquipment.add("e-o-db---------");
        emsEquipment.add("e-o-ddb--------");
        emsEquipment.add("e-o-deb--------");
        emsEquipment.add("e-o-dfb--------");
        emsEquipment.add("e-o-dgb--------");
        emsEquipment.add("e-o-dhb--------");
        emsEquipment.add("e-o-dib--------");
        emsEquipment.add("e-o-djb--------");
        emsEquipment.add("e-o-dlb--------");
        emsEquipment.add("e-o-dmb--------");
        emsEquipment.add("e-o-dob--------");
        emsEquipment.add("e-o-pea--------");
        emsEquipment.add("e-o-peb--------");
        emsEquipment.add("e-o-pec--------");
        emsEquipment.add("e-o-ped--------");
        emsEquipment.add("e-o-pee--------");
        emsEquipment.add("e-f-ba---------");
        emsEquipment.add("e-f-ma---------");
        emsEquipment.add("e-f-mc---------");
    }
}

