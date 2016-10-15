/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.WWUtil;
import junit.framework.TestCase;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

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
    
    @Test
    public void testReplacePropertyReferences()
    {
    	//Test that we can resolve nested property references 
    	//and properties defined at the system level
    	Configuration.setValue("a", "I am a");
        System.setProperty("b", "${a}");
    	Configuration.setValue("c", "${a}, ${b}");
        String expanded = WWUtil.replacePropertyReferences("Prefix_${c}_Suffix");
        TestCase.assertEquals("Prefix_I am a, I am a_Suffix", expanded);
    }    
    
    @Test
    public void testReplacePropertyReferencesLogging()
    {
    	
        //Test that we get a log message when the property doesn't exist
        Logger logger = Logging.logger();
        
        SimpleFormatter formatter = new SimpleFormatter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(out, formatter);
        logger.addHandler(handler);
        try
        {
        	WWUtil.replacePropertyReferences("${property.does.not.exist}");
        	handler.flush();
            String logMsg = out.toString();
     
            assertNotNull(logMsg);
            assertTrue(logMsg.contains("Failed to find property 'property.does.not.exist' for '${property.does.not.exist}'"));
        }
        finally
        {
        	logger.removeHandler(handler);
        }
    }
    
}
