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
package org.jcoderz.commons.connector.file;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;

/**
 * Tests File System connections.
 *
 */
public class FsConnectionTest
      extends FsTestCase
{
   /** The full qualified name of this class. */
   private static final transient String CLASSNAME = FsConnectionTest.class
         .getName();
   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   /** Tests the connector interfaces. */
   public void testInterfaces ()
   {
      try
      {
         getConnection().close();
      }
      catch (ResourceException e)
      {
         fail("Got an unexpected resource exception while testing File "
               + "Connector interfaces. " + e.getMessage());
      }
   }

   /**
    * Tests whether the methods of the FsConnection throw a ResourceException
    * after the connection has been closed. A client should not use a closed
    * connection.
    */
   public void testClosed ()
   {
      final String msg = "closed.";
      final FsConnection c = getConnection();
      closeConnection(c);

      mustBeNotAvailable(msg, c);
   }

   /**
    * Tests whether the methods of the FsConnection throw a ResourceException
    * after the underlying managed connection has been cleaned up.
    */
   public void testCleanedUp ()
   {
      final String msg = "cleaned up.";
      final FsConnection c = getConnection();

      cleanUpManagedConnection();

      mustBeNotAvailable(msg, c);
   }

   /**
    * Tests whether the methods of the FsConnection throw a ResourceException
    * after the underlying managed connection has been destroyed.
    */
   public void testDestroyed ()
   {
      final String msg = "destroyed.";
      final FsConnection c = getConnection();

      destroyManagedConnection();

      mustBeNotAvailable(msg, c);
   }

   /**
    * Tests close method on multiply connection instances created by the same
    * managed connection factory.
    */
   public void testMultiplyConnectionsClose ()
   {
      final String msg = "closed.";
      final FsConnection c1 = getConnection();
      final FsConnection c2 = getConnection();
      try
      {
         c1.close();
         c2.close();
      }
      catch (ResourceException re)
      {
         fail("MultiplyConnectionClose failed due to a ResourceException "
               + re.getMessage());
      }

      mustBeNotAvailable(msg, c1);
      mustBeNotAvailable(msg, c2);
   }

   /**
    * Tests close method on multiply connection instances created by the same
    * managed connection factory.
    */
   public void testMultiplyConnectionsClosed ()
   {
      final FsConnection c1 = getConnection();
      final FsConnection c2 = getConnection();
      try
      {
         c1.close();
         c2.close();
      }
      catch (ResourceException re)
      {
         fail("MultiplyConnectionClose failed due to a ResourceException "
               + re.getMessage());
      }
   }

   /**
    * Tests whether the methods of the FsConnection throw a ResourceException
    * after the underlying managed connection has been destroyed.
    */
   public void testMultiplyConnectionsDestroyed ()
   {
      final String msg = "destroyed.";

      final FsConnection c1 = getConnection();
      final FsConnection c2 = getConnection();

      destroyManagedConnection();

      mustBeNotAvailable(msg, c1);
      mustBeNotAvailable(msg, c2);
   }

   /**
    * Tests whether the methods of the FsConnection throw a ResourceException
    * after the underlying managed connection has been destroyed.
    */
   public void testMultiplyConnectionsCleanedUp ()
   {
      final String msg = "cleaned up.";

      final FsConnection c1 = getConnection();
      final FsConnection c2 = getConnection();

      cleanUpManagedConnection();

      mustBeNotAvailable(msg, c1);
      mustBeNotAvailable(msg, c2);
   }

   /** Calls destroy() on the Connection Manager. */
   private void destroyManagedConnection ()
   {
      try
      {
         getConnectionManager().destroy();
      }
      catch (ResourceException e)
      {
         fail("Got an unexpected resource exception while testing destroyed "
               + "connections. " + e.getMessage());
      }
   }

   /** Calls cleanUp() on the Connection Manager. */
   private void cleanUpManagedConnection ()
   {
      try
      {
         getConnectionManager().cleanUp();
      }
      catch (ResourceException e)
      {
         fail("Got an unexpected resource exception while testing cleaned "
               + "connections. " + e.getMessage());
      }
   }


   /**
    * Checks whether the given connection is not available
    * @param msg1 message suffix
    * @param c connection to be test.
    */
   private void mustBeNotAvailable (final String msg1, final FsConnection c)
   {
      final Method [] m = FsConnection.class.getMethods();
      for (int i = 0; i < m.length; i++)
      {
         final Class [] cl = m[i].getParameterTypes();
         final Object [] objs = new Object [cl.length];
         for (int j = 0; j < objs.length; j++)
         {
            try
            {
               objs[j] = cl[j].newInstance();
            }
            catch (Exception e1)
            {
               fail("Failed to create a new instance of the class "
                     + cl[j].getName());
            }
         }

         try
         {
            //System.out.println("Invoking method " + m[i].getName());
            m[i].invoke(c, objs);
            fail("Method '" + m[i].getName()
                  + "' did not throw a ResourceException after the "
                  + "underlying connection had been " + msg1);
         }
         catch (IllegalArgumentException e1)
         {
            fail("Method " + m[i].getName()
                  + " throw a IllegalArgumentException.");
         }
         catch (IllegalAccessException e1)
         {
            fail("Method " + m[i].getName()
                  + " threw a IllegalAccessException.");
         }
         catch (InvocationTargetException e1)
         {
            assertTrue("Method '" + m[i].getName()
                  + "' must throw a ResourceException after the "
                  + "underlying connection has been " + msg1,
                  (e1.getTargetException()
                        instanceof javax.resource.spi.IllegalStateException));
         }
      }
   }

   private void closeConnection (final FsConnection c)
   {
      try
      {
         c.close();
      }
      catch (ResourceException e)
      {
         logger.log(Level.SEVERE,
               "Unexcpected error while connection's closing.", e);
         fail("Unexcpected error while  connection's closing. "
               + e.getMessage());
      }
   }
}
