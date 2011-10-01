/*
 * Copyright 2011 ZerothAngel <zerothangel@tyrannyofheaven.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tyrannyofheaven.bukkit.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utility methods I miss from Spring StringUtils and/or Commons-Lang.
 * 
 * @author zerothangel
 */
public class ToHStringUtils {

    private ToHStringUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    /**
     * Returns true if text is non-null and contains non-whitespace characters.
     * 
     * @param text the string to test
     * @return true if text is non-null and contains non-whitespace characters
     */
    public static boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    /**
     * Returns a string representation of each object, with each object delimited
     * by the given delimiter. Similar to "string join" in other languages.
     * 
     * @param delimiter the string delimiter
     * @param coll a collection of objects
     * @return the delimited string
     */
    public static String delimitedString(String delimiter, Collection<?> coll) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> i = coll.iterator(); i.hasNext();) {
            sb.append(i.next());
            if (i.hasNext())
                sb.append(delimiter);
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of each object, with each object delimited
     * by the given delimiter. Similar to "string join" in other languages.
     * 
     * @param delimiter the string delimiter
     * @param objs an array of objects
     * @return the delimited string
     */
    public static String delimitedString(String delimiter, Object... objs) {
        return delimitedString(delimiter, Arrays.asList(objs));
    }

}
