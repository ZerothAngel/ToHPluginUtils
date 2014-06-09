/*
 * Copyright 2012 ZerothAngel <zerothangel@tyrannyofheaven.org>
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

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.Prompt;

class PagerPrompt implements Prompt {

    // Seems saner to keep our state in instance variables rather than a ConversationContext.
    // However as a consequence, ConversationFactories are not reusable.
    // Maybe if the Conversation's first prompt were cloned...

    private final List<String> lines = new LinkedList<>();

    private final int linesPerPage;

    private final int totalPages;

    private int currentLine;

    private int currentPage;

    private boolean shouldBlock;

    private static final Prompt ABORTED_PROMPT = new MessagePrompt() {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Stopping.";
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }
        
    };

    PagerPrompt(List<String> lines, int linesPerPage) {
        if (lines == null || lines.isEmpty())
            throw new IllegalArgumentException("lines cannot be empty");
        this.lines.addAll(lines);
        this.linesPerPage = linesPerPage - 1; // Room for prompt
        
        totalPages = (this.lines.size() + this.linesPerPage - 1) / this.linesPerPage;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (currentLine < linesPerPage) {
            // Next line
            String prompt = lines.remove(0);
            currentLine++;
            shouldBlock = false;
            return prompt;
        }
        else {
            // Next page
            currentLine = 0;
            currentPage++;
            shouldBlock = true;
            return ChatColor.YELLOW + String.format("Page %d of %d. More? y/n", currentPage, totalPages);
        }
    }

    @Override
    public boolean blocksForInput(ConversationContext context) {
        return shouldBlock;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        // Sanitize
        if (input != null) {
            input = input.toLowerCase().trim();
            // Only care about first char
            if (!input.isEmpty())
                input = input.substring(0, 1);
        }

        if ("n".equals(input)) {
            return ABORTED_PROMPT;
        }
        else {
            // Not blocking
            return !lines.isEmpty() ? this : Prompt.END_OF_CONVERSATION;
        }
    }

}
