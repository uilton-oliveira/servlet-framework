/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.uiltonsites.servletframework.utility;


/**
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFStringUtils {
    
    /**
    * Mirror of the unicode table from 00c0 to 017f without diacritics.
    */
   protected static final String tab00c0 = "AAAAAAACEEEEIIII" +
       "DNOOOOO\u00d7\u00d8UUUUYI\u00df" +
       "aaaaaaaceeeeiiii" +
       "\u00f0nooooo\u00f7\u00f8uuuuy\u00fey" +
       "AaAaAaCcCcCcCcDd" +
       "DdEeEeEeEeEeGgGg" +
       "GgGgHhHhIiIiIiIi" +
       "IiJjJjKkkLlLlLlL" +
       "lLlNnNnNnnNnOoOo" +
       "OoOoRrRrRrSsSsSs" +
       "SsTtTtTtUuUuUuUu" +
       "UuUuWwYyYZzZzZzF";

   /**
    * Remove accents from the string
    *
    * @param source string to convert
    * @return corresponding string without diacritics
    */
   public static String clean(String source) {
       char[] vysl = new char[source.length()];
       char one;
       for (int i = 0; i < source.length(); i++) {
           one = source.charAt(i);
           if (one >= '\u00c0' && one <= '\u017f') {
               one = tab00c0.charAt((int) one - '\u00c0');
           }
           vysl[i] = one;
       }
       return new String(vysl);
   }
   
   public static String stripSlashes(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            char next = i!=str.length()-1 ? str.charAt(i+1) : 0x0;
            if (c != '\\') {
                sb.append(c);
            }
            if (c == '\\' && next == '\\') {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
   
   public static String slashesAndAccents(String st) {
       return clean(stripSlashes(st));
   }
   
   public static String slashesAndAccents(String st, int maxLength) {
       return maxLength(clean(stripSlashes(st)), maxLength);
   }
   
   /**
    * Cut an string to the specified size
    * @param text
    * @param maxLength
    * @return 
    */
   public static String maxLength(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() > maxLength) {
            return text.substring(0, maxLength);
        } else {
            return text;
        }
    }
   
}
