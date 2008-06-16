package org.jcoderz.phoenix.report;

import junit.framework.TestCase;

/**
 * Test the FileSummary class.
 * @author Andreas Mandel
 */
public class FileSummaryTest
    extends TestCase
{
    /** Test the {@link FileSummary#getNumberOfFindings()} method. */
    public void testGetNumberOfFindings ()
    {
        final FileSummary testSummary = new FileSummary();
        
        assertEquals("No violation at all.", 0, 
                testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.FILTERED);
        assertEquals("Filtered should not be count.", 0, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.OK);
        assertEquals("OK should not be count.", 0, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.COVERAGE);
        assertEquals("COVERAGE is not expected to be count.", 0, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.INFO);
        assertEquals("INFO is expected to be count.", 1, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.CODE_STYLE);
        assertEquals("CODE_STYLE is expected to be count.", 2, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.CPD);
        assertEquals("CPD is expected to be count.", 3, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.DESIGN);
        assertEquals("DESIGN is expected to be count.", 4, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.ERROR);
        assertEquals("ERROR is expected to be count.", 5, 
            testSummary.getNumberOfFindings());
        testSummary.addViolation(Severity.WARNING);
        assertEquals("WARNING is expected to be count.", 6, 
            testSummary.getNumberOfFindings());
        
    }
}
