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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.tyrannyofheaven.bukkit.util.ToHUtils.colorize;

import org.bukkit.ChatColor;
import org.junit.Test;

public class ToHUtilsTest {

    @Test
    public void testColorizeBasics() {
        // null
        assertNull(colorize(null));

        // no substitutions
        assertEquals("this is a test", colorize("this is a test"));

        // basic substitions
        assertEquals(ChatColor.BLUE + "hello world", colorize("{BLUE}hello world"));
        assertEquals("hello world" + ChatColor.DARK_AQUA, colorize("hello world{DARK_AQUA}"));
        assertEquals("hello" + ChatColor.BLACK + " world", colorize("hello{BLACK} world"));
    }
     
    @Test
    public void testColorizeEscape() {
        // escape
        assertEquals("hello {world}", colorize("hello {{world}"));
        assertEquals("he{llo wo}rld", colorize("he{{llo wo}rld"));
        assertEquals("he{llo wo}rld", colorize("he{{llo wo}}rld"));
        assertEquals("hell{" + ChatColor.RED + "o world", colorize("hell{{{RED}o world"));
    }

    @Test
    public void testColorizeInvalid1() {
        // invalid
        try {
            colorize("hello {blah}world");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

    @Test
    public void testColorizeInvalid2() {
        try {
            colorize("hello world{WHITE");
        }
        catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

    @Test
    public void testColorizeInvalid3() {
        try {
            colorize("h{_BLUE}ello world");
        }
        catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

}
