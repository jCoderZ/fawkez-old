package org.jcoderz.phoenix.report;

import junit.framework.TestCase;

/**
 * Just a simple init test.
 * 
 * @author Andreas Mandel
 */
public class FindBugsFindingTypeTest
    extends TestCase
{
    /**
     * Yes, this really tests if the find bugs finding type initialisation
     * works.
     */
    public void testFindBugsFindingType ()
    {
       FindBugsFindingType.initialize();
       assertNotNull("Message must be defined.", 
          FindBugsFindingType.fromString("DM_NUMBER_CTOR").getDescription());
    }
    
}
