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
package org.jcoderz.commons.logging;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jcoderz.commons.BusinessImpact;


/**
 * This filter is used for filtering log messages according to the business
 * impact.
 *
 */
public class BusinessImpactFilter
      implements Filter
{
   private final Set mAllowedImpacts;

   /**
    * Creates a new instance of this and sets the business impacts, which will
    * pass this filter. All business impacts within the supplied list must be
    * given in their textual representation.
    *
    * @param impacts The list storing passable business impacts in their textual
    * representation.
    */
   public BusinessImpactFilter (final List impacts)
   {
      mAllowedImpacts = new HashSet();
      if (impacts != null)
      {
         for (final Iterator iter = impacts.iterator(); iter.hasNext(); )
         {
            final BusinessImpact impact
                  = BusinessImpact.fromString((String) iter.next());
            mAllowedImpacts.add(impact);
         }
      }
   }

   /** {@inheritDoc} */
   public boolean isPassable (LogItem entry)
   {
      return mAllowedImpacts.contains(entry.getBusinessImpact());
   }
}

