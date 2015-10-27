/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

import java.util.Comparator;

/**
 * From MIL-HDBK-857A, section 6.5: Display Hierarchy.  In order to ensure that features that overlap other are drawn in
 * the correct sequence, the GeoSym application software must allow for the definition of a display order for the
 * features being displayed.  GeoSym uses four "nested" methods to define the order in which symbols should be
 * displayed.
 * <p/>
 * 1. Display priority 2. Feature delineation 3. Order of rows in *sym.txt 4. Symbol type
 * <p/>
 * 6.5.1  Display Priority.  The first criterion to use to determine the order to display features is the display
 * priority.  Each row in the *sym.txt file defines a display priority number to that specific feature/attribute. The
 * display priority is a value between 0 and 9, where 9 identifies the highest priority.  A feature/attribute with a
 * higher priority should be displayed after (on top of) one with a lower priority.  For DNC, the values contained in
 * the display priority field are defined based on the S52 lookup tables.  Each DNC feature/attribute was mapped to a
 * corresponding S57 object class/attribute.  The S52 lookup tables were then utilized to obtain the display priority
 * values assigned to each object class.  For all other products, the display priority values were based on cartographic
 * experience with the corresponding hardcopy map/chart.
 * <p/>
 * 6.5.2  Feature Delineation.  Once the features to be displayed have been sorted based on display priority, they must
 * then be sorted by their delineation (area, line, point).  If the display priority is equal among features, then the
 * "painter's algorithm" should be used to display the area features first, followed by the linear features and then the
 * point features.
 * <p/>
 * 6.5.3  Order of Rows in *sym.txt.  The third criterion that affects the display order is the order of the rows in the
 * fullsym.txt file.  The order will be represented in the id column of each row.  Row ids will always be serial but may
 * not necessarily be consecutive.  Stated another way, the row ID for row N will always be less than the row ID for row
 * N+1.  Symbology being displayed based on this criterion should be displayed based on the least row id to the greatest
 * row id.
 * <p/>
 * 6.5.3.1  Point Features with Components.  For point features (e.g., buoys, beacons, lights) that are composed of
 * several symbol components, displaying the components according to the row ids in the *sym.txt file will result in the
 * properly constructed composite symbol.  Each symbol component is based on the value of a specific attribute and the
 * components will vary in display priority so should already have been sorted according to that value before examining
 * the row ids in the *sym.txt file.
 * <p/>
 * 6.5.3.2  Area Features with Multiple Fills.  There are some area features (e.g., Maritime Areas) that require both a
 * solid fill and one or more pattern fills.  Since the areasym column can only contain a single CGM reference, there is
 * a separate row in the *sym.txt file for each of the area symbols, as well as for the line symbol and/or point symbol
 * that apply to the specific area feature.  These multiple rows will have sequential row ids in the *sym.txt file
 * according to the order in which the symbols are to be displayed on the screen:  solid fill, pattern fill (may be more
 * than one), linear boundary, centered point symbol (may be more than one).
 * <p/>
 * 6.5.3.3  Features with Text Labels  As a general rule, the display priority specified for a feature's text label(s)
 * is the highest value possible, regardless of the display priority assigned to the base feature.  In DNC, all text
 * labels will have a display priority of 8 (per IHO S52).  In all other products, text labels have a display priority
 * of 9.  Therefore, the text labels will by default be displayed on top of all other symbols since the display priority
 * value is the first criterion controlling the order of symbology display on the screen. However, it is possible that a
 * critical feature in a product (e.g., area bridges in DNC) may be assigned the same high priority as the text label
 * for that feature.  Since the row(s) defining the text label(s) will always follow the row(s) defining the CGM symbols
 * for a feature, displaying the symbols and text labels according to their row ids (lowest to highest) in *sym.txt will
 * still result in the correct final symbology for a feature being displayed.
 * <p/>
 * 6.5.4  Symbol Type.  The final criterion for determining the order for symbol display applies only to area features.
 * Area features can be symbolized by any combination of centered point symbol, linear boundary, solid fill and/or
 * pattern fill.  Except for the special case where an area feature's symbology requires both a solid and pattern
 * fill(s) (see section 6.5.3.2), all symbols for an area feature will be specified in the same row of the *sym.txt
 * file.  In other words, any or all of the symbol columns may be populated.  In these cases, the symbols are to be
 * displayed according to the same "painter's algorithm" that applies at the feature level:  the area symbol (from
 * areasym) should be drawn first, followed by the linear symbol (from linesym) followed by the point symbol (from
 * pointsym).
 *
 * @author dcollins
 * @version $Id: VPFSymbolComparator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFSymbolComparator implements Comparator<VPFSymbol>
{
    public VPFSymbolComparator()
    {
    }

    /**
     * @param a
     * @param b
     *
     * @return
     */
    public int compare(VPFSymbol a, VPFSymbol b)
    {
        if (a == null || b == null)
        {
            String message = Logging.getMessage("nullValue.SymbolIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        VPFSymbolAttributes aAttr = a.getAttributes();
        VPFSymbolAttributes bAttr = b.getAttributes();

        if (aAttr == null && bAttr == null)
            return 0;

        if (aAttr == null || bAttr == null)
            return (aAttr != null) ? -1 : 1;

        double aPriority = a.getAttributes().getDisplayPriority();
        double bPriority = b.getAttributes().getDisplayPriority();
        int i = (aPriority < bPriority) ? -1 : (aPriority > bPriority ? 1 : 0);
        if (i != 0)
            return i;

        aPriority = this.getFeatureTypePriority(a.getAttributes().getFeatureType());
        bPriority = this.getFeatureTypePriority(b.getAttributes().getFeatureType());
        i = (aPriority < bPriority) ? -1 : (aPriority > bPriority ? 1 : 0);
        if (i != 0)
            return i;

        VPFSymbolKey aKey = a.getAttributes().getSymbolKey();
        VPFSymbolKey bKey = b.getAttributes().getSymbolKey();
        i = aKey.compareTo(bKey);
        if (i != 0)
            return i;

        return 0;
    }

    protected int getFeatureTypePriority(VPFFeatureType type)
    {
        switch (type)
        {
            case POINT:
                return 3;
            case LINE:
                return 2;
            case AREA:
                return 1;
            default:
                return 0;
        }
    }
}
