/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

/**
 * Unit tests for {@link WWUtil}.
 *
 * @author pabercrombie
 * @version $Id: WWUtilTest.java 697 2012-07-13 17:14:03Z pabercrombie $
 */
public class WWUtilTest
{
    /** Test parsing time strings. */
    @Test
    public void testParseTimeString()
    {
        Calendar expected = Calendar.getInstance();
        expected.set(Calendar.MILLISECOND, 0);

        Long time = WWUtil.parseTimeString("1997");
        expected.set(1997, Calendar.JANUARY, 1, 0, 0, 0);
        TestCase.assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07");
        expected.set(1997, Calendar.JULY, 1, 0, 0, 0);
        TestCase.assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07-01");
        expected.set(1997, Calendar.JULY, 1, 0, 0, 0);
        TestCase.assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07-01T07:30:15Z");
        expected.set(1997, Calendar.JULY, 1, 7, 30, 15);
        TestCase.assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07-01T07:30:15+03:00");
        expected.set(1997, Calendar.JULY, 1, 7, 30, 15);
        expected.setTimeZone(TimeZone.getTimeZone("EAT"));
        TestCase.assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("invalid time");
        TestCase.assertNull(time);
    }
}
