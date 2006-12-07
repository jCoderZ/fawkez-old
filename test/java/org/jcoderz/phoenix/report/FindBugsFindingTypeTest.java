/*
 * $Id: TokenType.java 97 2006-12-05 16:44:05Z amandel $
 *
 * Copyright 2006, The jCoderZ.org Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *    * Neither the name of the jCoderZ.org Project nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
    
    /**
     * Tests if finding type initialisation for fb-conrib works.
     */
    public void testFbContribFindingType ()
    {
       FindBugsFindingType.initialize();
       assertNotNull("Message must be defined.", 
             FindBugsFindingType.fromString(
                   "LEST_LOST_EXCEPTION_STACK_TRACE").getDescription());
    }
    
}
