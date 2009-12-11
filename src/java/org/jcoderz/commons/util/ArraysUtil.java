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
package org.jcoderz.commons.util;

/**
 * This class contains a static factory that allows arrays to be viewed as
 * lists.
 * 
 * @author Michael Griffel
 */
public final class ArraysUtil
{
   /**
    * No constructor for util class.
    */
   private ArraysUtil ()
   {
      // No instances allowed - contains only static utility functions.
   }

   /**
    * Returns a string representation of the contents of given
    * object with special care for potential arrays.
    * For none array objects the toString method is invoked, for
    * arrays the content of the array is converted.
    *  
    * If the array contains other arrays as elements, they are converted 
    * to strings by the {@link Object#toString}method inherited 
    * from <tt>Object</tt>, which describes their <i>identities</i> 
    * rather than their contents.
    * <p>
    * The value returned by this method is equal to the value that would be
    * returned by <tt>Arrays.asList(a).toString()</tt>, unless <tt>array</tt>
    * is <tt>null</tt>, in which case <tt>"null"</tt> is returned.
    * <p>
    * This method is useful to dump the content of the array in a tracing 
    * method, e.g.:
    * <pre>
    * logger.entering(
    *       CLASSNAME, "foo(Object[])", ArraysUtil.toString(array));
    * </pre>
    * @param array The array whose string representation to return.  
    * @return A string representation of <tt>array</tt>.
    */
   public static String toString (Object array)
   {
       return toString(array, 0);
   }

   /**
    * Returns a string representation of the contents of given
    * object with special care for potential arrays, the output is
    * cut - for each potentially contained array - to maxSize.
    * 
    * <p>
    * If one of the contained arrays contains more than maxSize 
    * elements, the dump is stopped and the number of total elements 
    * is printed in the output, for the particular array.
    * </p> 
    * 
    * @param array The array whose string representation to return.  
    * @return A string representation of <tt>array</tt>.
    * @see #toString(Object)
    */
   public static String toString (Object array, int maxSize)
   {
      final String result;
      
      if (array == null)
      {
         result = "null";
      }
      else if (!array.getClass().isArray())
      {
          result = String.valueOf(array);
      }
      else if (array instanceof Object[])
      {
          result = ArraysUtil.toString((Object[]) array, maxSize);
      }
      else if (array instanceof boolean[])
      {
          result = ArraysUtil.toString((boolean[]) array, maxSize);
      }
      else if (array instanceof byte[])
      {
          result = ArraysUtil.toString((byte[]) array, maxSize);
      }
      else if (array instanceof short[])
      {
          result = ArraysUtil.toString((short[]) array, maxSize);
      }
      else if (array instanceof char[])
      {
          result = ArraysUtil.toString((char[]) array, maxSize);
      }
      else if (array instanceof int[])
      {
          result = ArraysUtil.toString((int[]) array, maxSize);
      }
      else if (array instanceof long[])
      {
          result = ArraysUtil.toString((long[]) array, maxSize);
      }
      else // no float/double
      {
          result = String.valueOf(array);
      }
      return result;
   }

   /**
    * Returns a string representation of the contents of the 
    * specified array. 
    * If the array contains other arrays as elements, they are converted 
    * to strings by the {@link Object#toString}method inherited 
    * from <tt>Object</tt>, which describes their <i>identities</i> 
    * rather than their contents.
    * <p>
    * The value returned by this method is equal to the value that would be
    * returned by <tt>Arrays.asList(a).toString()</tt>, unless <tt>array</tt>
    * is <tt>null</tt>, in which case <tt>"null"</tt> is returned.
    * <p>
    * This method is useful to dump the content of the array in a tracing 
    * method, e.g.:
    * <pre>
    * logger.entering(
    *     CLASSNAME, "foo(Object[])", ArraysUtil.toString(array));
    * </pre>
    * @param array The array whose string representation to return.  
    * @return A string representation of <tt>array</tt>.
    */
   public static String toString (Object[] array)
   {
       return toString(array, 0);
   }

   /**
    * Returns a string representation of the contents of the specified 
    * array with a maxSize limitation.
    * If the array contains other arrays as elements, they are converted 
    * to strings by the {@link Object#toString}method inherited 
    * from <tt>Object</tt>, which describes their <i>identities</i> 
    * rather than their contents.
    * <p>
    * The value returned by this method is equal to the value that would be
    * returned by <tt>Arrays.asList(a).toString()</tt>, unless <tt>array</tt>
    * is <tt>null</tt>, in which case <tt>"null"</tt> is returned.
    * <p>
    * This method is useful to dump the content of the array in a tracing 
    * method, e.g.:
    * <pre>
    * logger.entering(
    *     CLASSNAME, "foo(Object[])", ArraysUtil.toString(array));
    * </pre>
    * @param array The array whose string representation to return.  
    * @param maxSize the maximum number of elements to print, will
    *   be ignored if set to 0 or below.
    * @return A string representation of <tt>array</tt>.
    */
   public static String toString (Object[] array, int maxSize)
   {
      final String result;
      
      if (array == null)
      {
         result = "null";
      }
      else if (array.length == 0)
      {
         result = "[]";
      }
      else
      {
         final StringBuffer buf = new StringBuffer();
         for (int i = 0; i < array.length; i++)
         {
            if (i == 0)
            {
               buf.append('[');
            }
            else if (i == maxSize)
            {
                buf.append(",... in total ");
                buf.append(array.length);
                buf.append("Elements");
                break;
            }
            else
            {
               buf.append(", ");
            }
            buf.append(toString(array[i]));
         }
         buf.append(']');
         result = buf.toString();
      }
      return result;
   }

   /**
    * Returns a string representation of the contents of the specified 
    * array. 
    * <p>
    * The value dumps all int stored in the given array.
    * <p>
    * This method is useful to dump the content of the array in a tracing 
    * method, e.g.:
    * <pre>
    * logger.entering(
    *     CLASSNAME, "foo(int[])", ArraysUtil.toString(array));
    * </pre>
    * @param array The array whose string representation to return.  
    * @param maxSize the maximum number of elements to print, will
    *   be ignored if set to 0.
    * @return A string representation of <tt>array</tt>.
    */
    public static String toString (int[] array)
    {
        return toString(array, 0);
    }
   

   /**
    * Returns a string representation of the contents of the specified 
    * array with a maxSize limitation.
    * <p>
    * The value dumps up to maxSize ints stored in the given array.
    * <p>
    * This method is useful to dump the content of the array in a tracing 
    * method, e.g.:
    * <pre>
    * logger.entering(
    *     CLASSNAME, "foo(int[])", ArraysUtil.toString(array, 20));
    * </pre>
    * @param array The array whose string representation to return.  
    * @param maxSize the maximum number of elements to print, will
    *   be ignored if set to 0 or below.
    * @return A string representation of <tt>array</tt>.
    */
    public static String toString (int[] array, int maxSize)
    {
        final String result;
        
        if (array == null)
        {
           result = "null";
        }
        else if (array.length == 0)
        {
           result = "[]";
        }
        else
        {
           final StringBuffer buf = new StringBuffer();
           for (int i = 0; i < array.length; i++)
           {
              if (i == 0)
              {
                 buf.append('[');
              }
              else if (i == maxSize)
              {
                  buf.append(",... in total ");
                  buf.append(array.length);
                  buf.append("Elements");
                  break;
              }
              else
              {
                 buf.append(", ");
              }
              buf.append(String.valueOf(array[i]));
           }
           buf.append(']');
           result = buf.toString();
        }
        return result;
    }

    /**
     * Returns a string representation of the contents of the specified 
     * array. 
     * <p>
     * The value dumps all short stored in the given array.
     * <p>
     * This method is useful to dump the content of the array in a tracing 
     * method, e.g.:
     * <pre>
     * logger.entering(
     *     CLASSNAME, "foo(short[])", ArraysUtil.toString(array));
     * </pre>
     * @param array The array whose string representation to return.  
     * @return A string representation of <tt>array</tt>.
     */
     public static String toString (short[] array)
     {
         return toString(array, 0);
     }
    
    /**
     * Returns a string representation of the contents of the specified 
     * array with a maxSize limitation.
     * <p>
     * The value dumps all short stored in the given array.
     * <p>
     * This method is useful to dump the content of the array in a tracing 
     * method, e.g.:
     * <pre>
     * logger.entering(
     *     CLASSNAME, "foo(short[])", ArraysUtil.toString(array));
     * </pre>
     * @param array The array whose string representation to return.  
     * @param maxSize the maximum number of elements to print, will
     *   be ignored if set to 0 or below.
     * @return A string representation of <tt>array</tt>.
     */
     public static String toString (short[] array, int maxSize)
     {
         final String result;
         
         if (array == null)
         {
            result = "null";
         }
         else if (array.length == 0)
         {
            result = "[]";
         }
         else
         {
            final StringBuffer buf = new StringBuffer();
            for (int i = 0; i < array.length; i++)
            {
               if (i == 0)
               {
                  buf.append('[');
               }
               else if (i == maxSize)
               {
                   buf.append(",... in total ");
                   buf.append(array.length);
                   buf.append("Elements");
                   break;
               }
               else
               {
                  buf.append(", ");
               }
               buf.append(String.valueOf(array[i]));
            }
            buf.append(']');
            result = buf.toString();
         }
         return result;
     }

     /**
      * Returns a string representation of the contents of the 
      * specified array. 
      * <p>
      * The value dumps all long stored in the given array.
      * <p>
      * This method is useful to dump the content of the array in a tracing 
      * method, e.g.:
      * <pre>
      * logger.entering(
      *     CLASSNAME, "foo(long[])", ArraysUtil.toString(array));
      * </pre>
      * @param array The array whose string representation to return.  
      * @return A string representation of <tt>array</tt>.
      */
      public static String toString (long[] array)
      {
          return toString(array, 0);
      }
      
     /**
      * Returns a string representation of the contents of the specified 
      * array with a maxSize limitation.
      * <p>
      * The value dumps all long stored in the given array.
      * <p>
      * This method is useful to dump the content of the array in a tracing 
      * method, e.g.:
      * <pre>
      * logger.entering(
      *     CLASSNAME, "foo(long[])", ArraysUtil.toString(array));
      * </pre>
      * @param array The array whose string representation to return.  
      * @param maxSize the maximum number of elements to print, will
      *   be ignored if set to 0 or below.
      * @return A string representation of <tt>array</tt>.
      */
      public static String toString (long[] array, int maxSize)
      {
          final String result;
          
          if (array == null)
          {
             result = "null";
          }
          else if (array.length == 0)
          {
             result = "[]";
          }
          else
          {
             final StringBuffer buf = new StringBuffer();
             for (int i = 0; i < array.length; i++)
             {
                if (i == 0)
                {
                   buf.append('[');
                }
                else if (i == maxSize)
                {
                    buf.append(",... in total ");
                    buf.append(array.length);
                    buf.append("Elements");
                    break;
                }
                else
                {
                   buf.append(", ");
                }
                buf.append(String.valueOf(array[i]));
             }
             buf.append(']');
             result = buf.toString();
          }
          return result;
      }
      
      /**
       * Returns a string representation of the contents of the specified 
       * array.
       * <p>
       * The value dumps all byte values stored in the given array.
       * <p>
       * This method is useful to dump the content of the array in a tracing 
       * method, e.g.:
       * <pre>
       * logger.entering(
       *     CLASSNAME, "foo(byte[])", ArraysUtil.toString(array));
       * </pre>
       * @param array The array whose string representation to return.  
       * @return A string representation of <tt>array</tt>.
       */
       public static String toString (byte[] array)
       {
           return toString(array, 0);
       }
       
      /**
       * Returns a string representation of the contents of the specified 
       * array with a maxSize limitation.
       * <p>
       * The value dumps all byte values stored in the given array.
       * <p>
       * This method is useful to dump the content of the array in a tracing 
       * method, e.g.:
       * <pre>
       * logger.entering(
       *     CLASSNAME, "foo(byte[])", ArraysUtil.toString(array));
       * </pre>
       * @param array The array whose string representation to return.  
       * @param maxSize the maximum number of elements to print, will
       *   be ignored if set to 0 or below.
       * @return A string representation of <tt>array</tt>.
       */
       public static String toString (byte[] array, int maxSize)
       {
           final String result;
           
           if (array == null)
           {
              result = "null";
           }
           else if (array.length == 0)
           {
              result = "[]";
           }
           else
           {
              final StringBuffer buf = new StringBuffer();
              for (int i = 0; i < array.length; i++)
              {
                 if (i == 0)
                 {
                    buf.append('[');
                 }
                 else if (i == maxSize)
                 {
                     buf.append(",... in total ");
                     buf.append(array.length);
                     buf.append("Elements");
                     break;
                 }
                 else
                 {
                    buf.append(", ");
                 }
                 buf.append(String.valueOf(array[i]));
              }
              buf.append(']');
              result = buf.toString();
           }
           return result;
       }

       /**
        * Returns a string representation of the contents of the specified 
        * array.
        * <p>
        * The value dumps all char values stored in the given array.
        * <p>
        * This method is useful to dump the content of the array in a tracing 
        * method, e.g.:
        * <pre>
        * logger.entering(
        *     CLASSNAME, "foo(char[])", ArraysUtil.toString(array));
        * </pre>
        * @param array The array whose string representation to return.  
        * @return A string representation of <tt>array</tt>.
        */
        public static String toString (char[] array)
        {
            return toString(array, 0);
        }
       
       /**
        * Returns a string representation of the contents of the specified 
        * array with a maxSize limitation.
        * <p>
        * The value dumps all char values stored in the given array.
        * <p>
        * This method is useful to dump the content of the array in a tracing 
        * method, e.g.:
        * <pre>
        * logger.entering(
        *     CLASSNAME, "foo(char[])", ArraysUtil.toString(array));
        * </pre>
        * @param array The array whose string representation to return.  
        * @param maxSize the maximum number of elements to print, will
        *   be ignored if set to 0 or below.
        * @return A string representation of <tt>array</tt>.
        */
        public static String toString (char[] array, int maxSize)
        {
            final String result;
            
            if (array == null)
            {
               result = "null";
            }
            else if (array.length == 0)
            {
               result = "[]";
            }
            else
            {
               final StringBuffer buf = new StringBuffer();
               for (int i = 0; i < array.length; i++)
               {
                  if (i == 0)
                  {
                     buf.append('[');
                  }
                  else if (i == maxSize)
                  {
                      buf.append(",... in total ");
                      buf.append(array.length);
                      buf.append("Elements");
                      break;
                  }
                  else
                  {
                     buf.append(", ");
                  }
                  buf.append(String.valueOf(array[i]));
               }
               buf.append(']');
               result = buf.toString();
            }
            return result;
        }

        /**
         * Returns a string representation of the contents of the 
         * specified array. 
         * <p>
         * The value dumps all boolean values stored in the given array.
         * <p>
         * This method is useful to dump the content of the array in a tracing 
         * method, e.g.:
         * <pre>
         * logger.entering(
         *     CLASSNAME, "foo(boolean[])", ArraysUtil.toString(array));
         * </pre>
         * @param array The array whose string representation to return.  
         * @return A string representation of <tt>array</tt>.
         */
         public static String toString (boolean[] array)
         {
             return toString(array, 0);
         }

         /**
         * Returns a string representation of the contents of the specified 
         * array with a maxSize limitation.
         * <p>
         * The value dumps all boolean values stored in the given array.
         * <p>
         * This method is useful to dump the content of the array in a tracing 
         * method, e.g.:
         * <pre>
         * logger.entering(
         *     CLASSNAME, "foo(boolean[])", ArraysUtil.toString(array));
         * </pre>
         * @param array The array whose string representation to return.  
         * @param maxSize the maximum number of elements to print, will
         *   be ignored if set to 0 or below.
         * @return A string representation of <tt>array</tt>.
         */
         public static String toString (boolean[] array, int maxSize)
         {
             final String result;
             
             if (array == null)
             {
                result = "null";
             }
             else if (array.length == 0)
             {
                result = "[]";
             }
             else
             {
                final StringBuffer buf = new StringBuffer();
                for (int i = 0; i < array.length; i++)
                {
                   if (i == 0)
                   {
                      buf.append('[');
                   }
                   else if (i == maxSize)
                   {
                       buf.append(",... in total ");
                       buf.append(array.length);
                       buf.append("Elements");
                       break;
                   }
                   else
                   {
                      buf.append(", ");
                   }
                   buf.append(String.valueOf(array[i]));
                }
                buf.append(']');
                result = buf.toString();
             }
             return result;
         }
}
