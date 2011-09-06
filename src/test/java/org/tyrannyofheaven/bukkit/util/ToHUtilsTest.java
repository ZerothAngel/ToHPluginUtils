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
