package org.jcoderz.fawkez.test;

import java.util.HashMap;

public class Test 
{
    static final String TEST = "Test String Bää! ß";
    
    private HashMap test ()
    {
        String test = null;
	System.out.println("Buggy test:" + test.toString());
        return null;
    }
}