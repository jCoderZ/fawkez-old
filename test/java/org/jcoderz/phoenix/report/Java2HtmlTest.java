/**
 * 
 */
package org.jcoderz.phoenix.report;

import java.text.ParseException;

import org.jcoderz.commons.types.Date;

import junit.framework.TestCase;

/**
 * @author amandel
 *
 */
public class Java2HtmlTest
    extends TestCase
{
    /**
     * Test method for {@link org.jcoderz.phoenix.report.Java2Html#getPeriodStart(org.jcoderz.phoenix.report.ReportInterval, org.jcoderz.commons.types.Date)}.
     */
    public void testGetPeriodStart () 
        throws ParseException
    {
        assertEquals("Unexpected start for week.", 
            Date.fromString("2009-05-18T00:00:00.000Z"),
            Java2Html.getPeriodStart(ReportInterval.WEEK, 
                Date.fromString("2009-05-22T20:20:20.200Z")));
        assertEquals("Unexpected start for week.", 
            Date.fromString("2009-05-18T00:00:00.000Z"),
            Java2Html.getPeriodStart(ReportInterval.WEEK, 
                Date.fromString("2009-05-21T20:20:20.200Z")));
        assertEquals("Unexpected start for week.", 
            Date.fromString("2009-05-18T00:00:00.000Z"),
            Java2Html.getPeriodStart(ReportInterval.WEEK, 
                Date.fromString("2009-05-20T20:20:20.200Z")));
        assertEquals("Unexpected start for week.", 
            Date.fromString("2009-05-18T00:00:00.000Z"),
            Java2Html.getPeriodStart(ReportInterval.WEEK, 
                Date.fromString("2009-05-19T20:20:20.200Z")));
        assertEquals("Unexpected start for week.", 
            Date.fromString("2009-05-18T00:00:00.000Z"),
            Java2Html.getPeriodStart(ReportInterval.WEEK, 
                Date.fromString("2009-05-18T20:20:20.200Z")));
    }

    public void testGetPeriodEnd () 
        throws ParseException
    {
        assertEquals("Unexpected end for week.", 
            Date.fromString("2009-05-25T00:00:00.000Z"),
            Java2Html.getPeriodEnd(ReportInterval.WEEK, 
                Date.fromString("2009-05-22T20:20:20.200Z")));
        assertEquals("Unexpected end for week.", 
            Date.fromString("2009-05-25T00:00:00.000Z"),
            Java2Html.getPeriodEnd(ReportInterval.WEEK, 
                Date.fromString("2009-05-23T20:20:20.200Z")));
        assertEquals("Unexpected end for week.", 
            Date.fromString("2009-05-25T00:00:00.000Z"),
            Java2Html.getPeriodEnd(ReportInterval.WEEK, 
                Date.fromString("2009-05-24T20:20:20.200Z")));
        assertEquals("Unexpected end for week.", 
            Date.fromString("2009-06-01T00:00:00.000Z"),
            Java2Html.getPeriodEnd(ReportInterval.WEEK, 
                Date.fromString("2009-05-25T20:20:20.200Z")));
        assertEquals("Unexpected end for week.", 
            Date.fromString("2009-06-01T00:00:00.000Z"),
            Java2Html.getPeriodEnd(ReportInterval.WEEK, 
                Date.fromString("2009-05-31T23:20:20.200Z")));
    }
}
