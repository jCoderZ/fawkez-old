/*
 * $Id$
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
package org.jcoderz.guidelines.snippets;


import java.io.FileNotFoundException;
import java.io.Serializable;

import java.util.EventObject;


/**
 * An example class.
 *
 * @author Alexander Bretz
 */
public class IndentationSample
    implements Serializable
{
    private static final long serialVersionUID = -8293451984149714868L;

    // BEGIN SNIPPET: SimpleImplementator.xml
    class SimpleImplementator 
        extends Object 
        implements Serializable
    // END SNIPPET
    {
       private static final long serialVersionUID = -829345198419714868L;
       void doSomthing ()
       {
           // ...
       }
    }

/*
// BEGIN SNIPPET: MultiImplementator1.xml

class MultiImplementator extends SimpleImplementator implements Serializable,
        Cloneable, Comparable, Runnable, EventListener, Observer
// END SNIPPET

// BEGIN SNIPPET: MultiImplementator2.xml
class MultiImplementator2
        extends SimpleImplementator
        implements Serializable, Cloneable, Comparable, Runnable, EventListener
               Observer
// END SNIPPET

// BEGIN SNIPPET: MultiImplementator3.xml
class MultiImplementator3
        extends SimpleImplementator
        implements Serializable,
                   Cloneable,
                   Comparable,
                   Runnable,
                   EventListener,
                   Observer
// END SNIPPET
*/

// BEGIN SNIPPET: MethodParamsStandard.xml
public void severalParameters (String one, int two, String three,
        EventObject four, Integer five)
// END SNIPPET
   {
/*
*/
   }

// BEGIN SNIPPET: MethodParamsNewLine.xml
public void severalParametersNewLine (
        String one, int two, String three, EventObject four,
        Integer five)
// END SNIPPET
   {
      // ...
   }

// BEGIN SNIPPET: MethodParamsDeep.xml
public void severalParametersDeep (
        String one,
        int two,
        String three,
        EventObject four,
        Integer five)
// END SNIPPET
    {
        // ...
/*
// BEGIN SNIPPET: MethodCallDeep.xml
vector.add(lbPunktzahl,
           new GridBagLayout(0, 1, 2, 1, 0.0, 0.0,
                             GribBagConstraints.WEST,
                             GribBagConstraints.NONE,
                             new Insets(0, GribBagConstraints.WEST,
                                        GribBagConstraints.WEST,
                                        GribBagConstraints.WEST), 0, 0));
// END SNIPPET
*/
   }

// BEGIN SNIPPET: MethodThrowsStandard.xml
public void methodThrows (int param) throws IllegalAccessException,
        IllegalStateException, FileNotFoundException
// END SNIPPET
    {
        // ...
    }

// BEGIN SNIPPET: MethodThrowsNewLine.xml
public void methodThrowsNewLine (int param)
           throws IllegalAccessException, IllegalStateException,
               FileNotFoundException
// END SNIPPET
    {
        // ...
    }

// BEGIN SNIPPET: MethodThrowsDeep.xml
public void methodThrowsDeep (int param)
        throws IllegalAccessException,
                IllegalStateException,
                FileNotFoundException
// END SNIPPET
    {
       // ...
    }
}

