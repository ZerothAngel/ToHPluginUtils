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
package org.tyrannyofheaven.bukkit.util.command;

import static org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils.requireAllPermissions;
import static org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils.requireOnePermission;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.tyrannyofheaven.bukkit.util.ToHLoggingUtils;

/**
 * The main class that drives annotation-driven command parsing.
 * 
 * @author zerothangel
 */
final class HandlerExecutor<T extends Plugin> {

    private static final Map<Class<?>, Class<?>> primitiveWrappers;

    private static final Set<Class<?>> supportedParameterTypes;
    
    private final T plugin;

    private final UsageOptions usageOptions;

    private final Map<String, CommandMetaData> commandMap = new HashMap<String, CommandMetaData>();

    private final Map<Object, HandlerExecutor<T>> subCommandMap = new WeakHashMap<Object, HandlerExecutor<T>>();

    private final Set<String> commandList = new TreeSet<String>();

    static {
        // Build map of primitives to primitive wrappers
        Map<Class<?>, Class<?>> wrappers = new HashMap<Class<?>, Class<?>>();
        wrappers.put(Boolean.TYPE, Boolean.class);
        wrappers.put(Byte.TYPE, Byte.class);
        wrappers.put(Short.TYPE, Short.class);
        wrappers.put(Integer.TYPE, Integer.class);
        wrappers.put(Long.TYPE, Long.class);
        wrappers.put(Float.TYPE, Float.class);
        wrappers.put(Double.TYPE, Double.class);
        primitiveWrappers = Collections.unmodifiableMap(wrappers);

        // Build set of supported parameter types
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(String.class);
        for (Map.Entry<Class<?>, Class<?>> me : primitiveWrappers.entrySet()) {
            types.add(me.getKey());
            types.add(me.getValue());
        }
        supportedParameterTypes = Collections.unmodifiableSet(types);
    }
    
    /**
     * Create a HandlerExecutor instance.
     * 
     * @param plugin the associated plugin
     * @param usageOptions UsageOptions to use with the HelpBuilder
     * @param handlers handler objects
     */
    HandlerExecutor(T plugin, UsageOptions usageOptions, Object... handlers) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        if (usageOptions == null)
            throw new IllegalArgumentException("usageOptions cannot be null");
        if (handlers == null)
            handlers = new Object[0];

        this.plugin = plugin;
        this.usageOptions = usageOptions;
        processHandlers(handlers);
    }

    /**
     * Create a HandlerExecutor instance.
     * 
     * @param plugin the associated plugin
     * @param usageOptions UsageOptions to use with the HelpBuilder
     * @param handlers handler objects
     */
    HandlerExecutor(T plugin, Object... handlers) {
        this(plugin, new DefaultUsageOptions(), handlers);
    }

    // Analyze each handler object and create/store the appropriate metadata
    // classes.
    private void processHandlers(Object[] handlers) {
        for (Object handler : handlers) {
            Class<?> clazz = handler.getClass();
            // Scan each method
            for (Method method : clazz.getMethods()) {
                // Handle @Require if present
                Require require = method.getAnnotation(Require.class);
                String[] permissions = new String[0];
                boolean requireAll = false;
                if (require != null) {
                    permissions = require.value();
                    requireAll = require.all();
                }

                // @Command or @SubCommand present?
                Command command = method.getAnnotation(Command.class);

                if (command != null) {
                    // Handle @Command
                    List<MethodParameter> options = new ArrayList<MethodParameter>();

                    boolean hasLabel = false;
                    boolean hasRest = false; // There can be only one!

                    // Scan each parameter
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        Class<?> paramType = method.getParameterTypes()[i];
                        Annotation[] anns = method.getParameterAnnotations()[i];

                        MethodParameter ma = null;
                        
                        // Special parameter type?
                        if (paramType.isAssignableFrom(Server.class)) {
                            ma = new SpecialParameter(SpecialParameter.Type.SERVER);
                        }
                        else if (paramType.isAssignableFrom(plugin.getClass())) {
                            ma = new SpecialParameter(SpecialParameter.Type.PLUGIN);
                        }
                        else if (paramType.isAssignableFrom(CommandSender.class)) {
                            ma = new SpecialParameter(SpecialParameter.Type.COMMAND_SENDER);
                        }
                        else if (paramType.isAssignableFrom(HelpBuilder.class)) {
                            ma = new SpecialParameter(SpecialParameter.Type.USAGE_BUILDER);
                        }
                        else if (paramType.isAssignableFrom(CommandSession.class)) {
                            ma = new SpecialParameter(SpecialParameter.Type.SESSION);
                        }
                        else if (paramType.isArray() && paramType.getComponentType() == String.class) {
                            if (hasRest) {
                                throw new CommandException("Method already has a String[] parameter (%s#%s)", handler.getClass().getName(), method.getName());
                            }

                            ma = new SpecialParameter(SpecialParameter.Type.REST);
                            hasRest = true;
                        }
                        else {
                            // Grab the @Option and @Session annotations
                            Option optAnn = null;
                            Session sessAnn = null;
                            for (Annotation ann : anns) {
                                if (ann instanceof Option) {
                                    optAnn = (Option)ann;
                                }
                                else if (ann instanceof Session) {
                                    sessAnn = (Session)ann;
                                }
                            }

                            // Both must not be present
                            if (optAnn != null && sessAnn != null) {
                                throw new CommandException("Parameter cannot have both @Option and @Session annotations (%s#%s)", handler.getClass().getName(), method.getName());
                            }
                            else if (sessAnn != null) {
                                // @Session
                                ma = new SessionParameter(sessAnn.value(), paramType);
                            }
                            else if (optAnn != null) {
                                // @Option
                                
                                // Supported parameter type?
                                if (!supportedParameterTypes.contains(paramType)) {
                                    throw new CommandException("Unsupported parameter type: %s (%s#%s)", paramType, handler.getClass().getName(), method.getName());
                                }

                                ma = new OptionMetaData(optAnn.value(), optAnn.valueName(), paramType, optAnn.optional(), optAnn.nullable(), optAnn.completer());
                            }
                            else {
                                // Not annotated at all

                                // Is it a String parameter?
                                if (paramType == String.class) {
                                    if (hasLabel) {
                                        throw new CommandException("Method already has an unannotated String parameter (%s#%s)", handler.getClass().getName(), method.getName());
                                    }
                                    
                                    ma = new SpecialParameter(SpecialParameter.Type.LABEL);
                                    hasLabel = true;
                                }
                                else
                                    throw new CommandException("Non-special parameters must be annotated with @Option (%s#%s)", handler.getClass().getName(), method.getName());
                            }
                        }
                        
                        options.add(ma);
                    }

                    // Some validation of option ordering
                    // Flags (-f, --flag) can appear anywhere.
                    // Optional arguments must follow positional ones.
                    // Nullable arguments must follow non-nullable ones.
                    List<MethodParameter> reversed = new ArrayList<MethodParameter>(options);
                    Collections.reverse(reversed); // easier to do this in reverse
                    boolean positional = false; // true if positional arguments have started
                    boolean nonNullable = false; // true if non-nullable arguments have started
                    for (MethodParameter ma : reversed) {
                        if (!(ma instanceof OptionMetaData)) continue;
                        OptionMetaData omd = (OptionMetaData)ma;
                        if (omd.isArgument()) {
                            if (!omd.isOptional()) {
                                positional = true;
                            }
                            else if (positional) {
                                throw new CommandException("Optional parameters must follow all non-optional ones (%s#%s)", handler.getClass().getName(), method.getName());
                            }
                            
                            if (!omd.isNullable()) {
                                nonNullable = true;
                            }
                            else if (nonNullable) {
                                throw new CommandException("Nullable parameters must follow all non-nullable ones (%s#%s)", handler.getClass().getName(), method.getName());
                            }
                        }
                    }

                    CommandMetaData cmd = new CommandMetaData(handler, method, options, permissions, requireAll, command.description(), hasRest ? command.varargs() : null);
                    for (String commandName : command.value()) {
                        if (commandMap.put(commandName, cmd) != null) {
                            throw new CommandException("Duplicate command: %s (%s#%s)", commandName, handler.getClass().getName(), method.getName());
                        }
                    }
                    
                    // Track unaliased name for easy registration
                    // Dupes would have been handled above
                    commandList.add(command.value()[0]);
                }
            }
        }
    }

    // Convert string to boolean (a little more friendlier than Boolean.valueOf(String))
    private boolean toBoolean(String text) {
        text = text.trim().toLowerCase();
        if ("true".equals(text) || "t".equals(text) || "yes".equals(text) || "y".equals(text) || "on".equals(text))
            return true;
        else if ("false".equals(text) || "f".equals(text) || "no".equals(text) || "n".equals(text) || "off".equals(text))
            return false;
        else
            throw new IllegalArgumentException("Cannot convert string to boolean");
    }

    // Given parsed arguments and metadata, create an argument list suitable
    // for reflective invoke.
    private Object[] buildMethodArgs(CommandMetaData cmd, CommandSender sender, ParsedArgs pa, String label, InvocationChain invChain, CommandSession session, Set<String> possibleCommands) throws Throwable {
        List<Object> result = new ArrayList<Object>(cmd.getParameters().size());
        for (MethodParameter mp : cmd.getParameters()) {
            if (mp instanceof SpecialParameter) {
                SpecialParameter sp = (SpecialParameter)mp;
                if (sp.getType() == SpecialParameter.Type.SERVER) {
                    result.add(plugin.getServer());
                }
                else if (sp.getType() == SpecialParameter.Type.PLUGIN) {
                    result.add(plugin);
                }
                else if (sp.getType() == SpecialParameter.Type.COMMAND_SENDER) {
                    result.add(sender);
                }
                else if (sp.getType() == SpecialParameter.Type.LABEL) {
                    result.add(label);
                }
                else if (sp.getType() == SpecialParameter.Type.USAGE_BUILDER) {
                    result.add(getHelpBuilder(invChain, possibleCommands));
                }
                else if (sp.getType() == SpecialParameter.Type.SESSION) {
                    result.add(session);
                }
                else if (sp.getType() == SpecialParameter.Type.REST) {
                    result.add(pa.getRest());
                }
                else {
                    throw new AssertionError("Unknown SpecialParameter type");
                }
            }
            else if (mp instanceof OptionMetaData) {
                OptionMetaData omd = (OptionMetaData)mp;
                String text = pa.getOption(omd.getName());

                // If Boolean or boolean, treat specially
                if (omd.getType() == Boolean.class || omd.getType() == Boolean.TYPE) {
                    if (omd.isArgument()) {
                        if (text != null) {
                            try {
                                result.add(toBoolean(text));
                            }
                            catch (IllegalArgumentException e) {
                                throw new ParseException("Invalid boolean: %s", omd.getName());
                            }
                        }
                        else if (!omd.isOptional()) {
                            if (omd.isNullable()) {
                                result.add(null);
                            }
                            else {
                                // Missing positional argument
                                throw new ParseException("Missing argument: %s", omd.getName());
                            }
                        }
                        else {
                            // Flag not specified
                            // Set to false if primitive, null if wrapper
                            if (omd.getType() == Boolean.TYPE) {
                                result.add(Boolean.FALSE);
                            }
                            else {
                                result.add(null);
                            }
                        }
                    }
                    else {
                        // Flag
                        result.add(Boolean.valueOf(text != null));
                    }
                }
                else if (text != null) {
                    if (omd.getType() == String.class) {
                        // Nothing to convert
                        result.add(text);
                    }
                    else {
                        // Use .valueOf(String) to convert
                        Class<?> paramType = omd.getType();
                        // Primitives don't have .valueOf(String)
                        Class<?> newType = primitiveWrappers.get(paramType);
                        if (newType != null)
                            paramType = newType;
                        try {
                            Method valueOf = paramType.getMethod("valueOf", String.class);
                            Object value = valueOf.invoke(null, text);
                            result.add(value);
                        }
                        catch (InvocationTargetException e) {
                            // Unwrap, see if it's a NumberFormatException
                            if (e.getCause() instanceof NumberFormatException) {
                                // Complain
                                throw new ParseException("Invalid number: %s", omd.getName());
                            }
                            else {
                                // Re-throw
                                throw e.getCause();
                            }
                        }
                    }
                }
                else {
                    if (omd.isArgument() && !omd.isOptional()) {
                        if (!omd.isNullable()) {
                            // Missing positional argument
                            throw new ParseException("Missing argument: %s", omd.getName());
                        }
                    }
                    
                    result.add(null);
                }
            }
            else if (mp instanceof SessionParameter) {
                SessionParameter sp = (SessionParameter)mp;
                result.add(session.getValue(sp.getName(), sp.getType()));
            }
            else {
                throw new AssertionError("Unknown MethodParameter type");
            }
        }
        return result.toArray();
    }

    /**
     * Executes the named command.
     * 
     * @param sender the command sender
     * @param name the name of the command to execute
     * @param args command arguments
     */
    void execute(CommandSender sender, String name, String label, String[] args) throws Throwable {
        execute(sender, name, label, args, null, null);
    }

    /**
     * Executes the named command.
     * 
     * @param sender the command sender
     * @param name the name of the command to execute
     * @param args command arguments
     * @param invChain an InvocationChain or null
     * @param session a CommandSession or null
     */
    void execute(CommandSender sender, String name, String label, String[] args, InvocationChain invChain, CommandSession session) throws Throwable {
        if (invChain == null)
            invChain = new InvocationChain();
        if (session == null)
            session = new CommandSession();

        CommandMetaData cmd = commandMap.get(name);
        if (cmd == null)
            throw new ParseException("Unknown command: %s", name);

        // Check permissions
        if (cmd.isRequireAll()) {
            requireAllPermissions(sender, cmd.getPermissions());
        }
        else {
            requireOnePermission(sender, cmd.getPermissions());
        }

        // Save into chain
        invChain.addInvocation(label, cmd);

        ParsedArgs pa = ParsedArgs.parse(cmd, args);
        Object[] methodArgs = buildMethodArgs(cmd, sender, pa, label, invChain, session, null);
        Object nextHandler = null;
        try {
            nextHandler = cmd.getMethod().invoke(cmd.getHandler(), methodArgs);
        }
        catch (InvocationTargetException e) {
            // Unwrap exception, re-throw
            throw e.getCause();
        }

        if (nextHandler != null) {
            // Handle a sub-command
            args = pa.getRest();
            if (args.length >= 1) {
                // Check HandlerExecutor cache
                HandlerExecutor<T> he = handlerExecutorFor(nextHandler);

                // Chain to next handler
                String subName = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);

                he.execute(sender, subName, subName, args, invChain, session);
            }
        }
    }

    // Add the named CommandMetaData to an InvocationChain
    void fillInvocationChain(InvocationChain invChain, String label) {
        CommandMetaData cmd = commandMap.get(label);
        if (cmd == null)
            throw new IllegalArgumentException("Unknown command: " + label);
        invChain.addInvocation(label, cmd);
    }

    // Retrieve cached HandlerExecutor for given handler object, creating
    // one if it doesn't exist
    synchronized HandlerExecutor<T> handlerExecutorFor(Object handler) {
        // Check HandlerExecutor cache
        HandlerExecutor<T> he = subCommandMap.get(handler);
        if (he == null) {
            // No HandlerExecutor yet, create a new one
            he = new HandlerExecutor<T>(plugin, usageOptions, handler);
            subCommandMap.put(handler, he);
        }
        return he;
    }

    // Create a HelpBuilder associated with this HandlerExecutor
    HelpBuilder getHelpBuilder(InvocationChain rootInvocationChain, Set<String> possibleCommands) {
        return new HelpBuilder(this, rootInvocationChain, usageOptions, possibleCommands);
    }

    // Register top-level commands
    void registerCommands(TabExecutor executor) {
        for (String name : commandList) {
            PluginCommand command = ((JavaPlugin)plugin).getCommand(name);
            if (command == null) {
                ToHLoggingUtils.warn(plugin, "Command '%s' not found in plugin.yml -- ignoring", name);
                continue;
            }
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    /**
     * Determine possible completions for the last argument.
     * 
     * @param sender the command sender
     * @param name the name of the command to execute
     * @param args command arguments
     * @param invChain an InvocationChain or null
     * @param session a CommandSession or null
     * @param typeCompleterRegistry the (global) TypeCompleter registry
     * @return list of possible completions
     */
    List<String> getTabCompletions(CommandSender sender, String name, String label, String[] args, InvocationChain invChain, CommandSession session, Map<String, TypeCompleter> typeCompleterRegistry) throws Throwable {
        if (invChain == null)
            invChain = new InvocationChain();
        if (session == null)
            session = new CommandSession();

        // Isolate query argument (last argument)
        String query;
        String[] argsNoQuery;
        if (args.length > 0) {
            // Have at least one
            query = args[args.length - 1];
            argsNoQuery = Arrays.copyOfRange(args, 0, args.length - 1);
        }
        else {
            query = "";
            argsNoQuery = args;
        }

        CommandMetaData cmd = commandMap.get(name);
        if (cmd == null)
            throw new ParseException("Unknown command: %s", name);

        // Save into chain
        invChain.addInvocation(label, cmd);

        // Tab completion on cmd.getFlagOptions() and cmd.getPositionalArguments()
        OptionMetaData missingValue;
        boolean parsedPositional;
        boolean consumedAll;
        Map<String, String> options;
        ParsedArgs pa = null;
        try {
            pa = ParsedArgs.parse(cmd, argsNoQuery);
            missingValue = pa.getRemainingOptionalArgument(); // possible because of nullable
            parsedPositional = pa.isParsedPositional();
            consumedAll = pa.getRest().length == 0;
            options = pa.getOptions();
        }
        catch (UnknownFlagException e) {
            // Tab-completion ain't gonna help
            return Collections.emptyList();
        }
        catch (MissingValueException e) {
            missingValue = e.getOptionMetaData();
            parsedPositional = e.isParsedPositional();
            consumedAll = true;
            options = e.getOptions();
        }

        // Is it the start of a flag?
        if (consumedAll && !parsedPositional && !OptionMetaData.isArgument(query)) {
            List<String> source = new ArrayList<String>();
            source.add("--"); // explicit end of flags
            for (OptionMetaData omd : cmd.getFlagOptions()) {
                boolean found = false;
                for (String flag : omd.getNames()) {
                    if (options.containsKey(flag)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    // Skip this one (we don't support multiple flags)
                    continue;
                }
                source.addAll(Arrays.asList(omd.getNames()));
            }

            List<String> result = new ArrayList<String>();
            StringUtil.copyPartialMatches(query, source, result);
            return result;
        }

        if (missingValue != null) {
            // Use missing value's type to get candidates
            List<String> result = new ArrayList<String>();
            addCompletions(typeCompleterRegistry, missingValue, query, result);
            return result;
        }

        assert pa != null;

        // Check if sub-command
        if (cmd.getMethod().getReturnType() != Void.TYPE) {
            // Sub-command, attempt to execute it. It better not have side-effects!
            Set<String> possibleCommands = new HashSet<String>();
            Object[] methodArgs = buildMethodArgs(cmd, sender, pa, label, invChain, session, possibleCommands);
            Object nextHandler;
            try {
                nextHandler = cmd.getMethod().invoke(cmd.getHandler(), methodArgs);
            }
            catch (InvocationTargetException e) {
                throw e.getCause();
            }
            
            if (nextHandler != null) {
                args = pa.getRest();

                if (args.length >= 1) {
                    HandlerExecutor<T> he = handlerExecutorFor(nextHandler);

                    // Chain to next
                    String subName = args[0];
                    args = Arrays.copyOfRange(args, 1, args.length + 1); // room for query
                    args[args.length - 1] = query; // stuff query argument back in
                    return he.getTabCompletions(sender, subName, subName, args, invChain, session, typeCompleterRegistry);
                }
            }
            
            // Relying on HelpBuilder to have filled out the blanks
            List<String> result = new ArrayList<String>();
            StringUtil.copyPartialMatches(query, possibleCommands, result);
            return result;
        }

        // Some kind of error
        return Collections.emptyList();
    }

    private void addCompletions(Map<String, TypeCompleter> typeCompleterRegistry, OptionMetaData omd, String partial, List<String> destination) {
        // Determine suitable TypeCompleter
        TypeCompleter typeCompleter = null;
        String arg = null;

        String completerName = omd.getCompleter();
        if (completerName != null) {
            // Split arguments, if present
            String[] parts = completerName.split(":", 2);
            if (parts.length == 2) {
                completerName = parts[0];
                arg = parts[1];
            }
        
            typeCompleter = typeCompleterRegistry.get(completerName);
        }
        
        if (typeCompleter != null) {
            destination.addAll(typeCompleter.complete(omd.getType(), arg, partial));
        }
        else {
            // Use values based on type.
            if (omd.getType() == Boolean.class || omd.getType() == Boolean.TYPE) {
                // Easy one
                List<String> source = new ArrayList<String>();
                source.add("true");
                source.add("false");
                StringUtil.copyPartialMatches(partial, source, destination);
            }
            else if (partial == null || partial.length() == 0){
                // Drop a hint
                destination.add(String.format("<%s>", omd.isArgument() ? omd.getName() : omd.getValueName()));
            }
        }
    }

}
