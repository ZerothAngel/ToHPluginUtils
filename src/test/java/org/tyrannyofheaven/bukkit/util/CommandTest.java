package org.tyrannyofheaven.bukkit.util;

import org.junit.Test;

public class CommandTest {

    @Test
    public void testMetaData() {
        TOHCommandExecutor ce = new TOHCommandExecutor(null, new TestHandler());
    }

}
