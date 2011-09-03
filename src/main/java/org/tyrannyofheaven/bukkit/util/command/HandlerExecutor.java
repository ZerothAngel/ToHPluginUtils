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
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils;

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
                        else if (paramType.isArray() && paramType.getComponentType() == String.class) {
                            if (hasRest) {
                                throw new CommandException("Method already has a String[] parameter");
                            }

                            ma = new SpecialParameter(SpecialParameter.Type.REST);
                            hasRest = true;
                        }
                        else {
                            // Grab the @Option annotations
                            Option optAnn = null;
                            for (Annotation ann : anns) {
                                if (ann instanceof Option) {
                                    optAnn = (Option)ann;
                                    break;
                                }
                            }

                            // Must be present
                            if (optAnn == null) {
                                // Though is it a String parameter?
                                if (paramType == String.class) {
                                    if (hasLabel) {
                                        throw new CommandException("Method already has an unannotated String parameter");
                                    }
                                    
                                    ma = new SpecialParameter(SpecialParameter.Type.LABEL);
                                    hasLabel = true;
                                }
                                else
                                    throw new CommandException("Non-special parameters must be annotated with @Option");
                            }
                            else {
                                // Supported parameter type?
                                if (!supportedParameterTypes.contains(paramType)) {
                                    throw new CommandException("Unsupported parameter type: " + paramType);
                                }

                                ma = new OptionMetaData(optAnn.value(), optAnn.valueName(), paramType, optAnn.optional());
                            }
                        }
                        
                        options.add(ma);
                    }

                    // Some validation of option ordering
                    // Flags (-f, --flag) can appear anywhere.
                    // Optional arguments must follow positional ones.
                    List<MethodParameter> reversed = new ArrayList<MethodParameter>(options);
                    Collections.reverse(reversed); // easier to do this in reverse
                    boolean positional = false; // true if positional arguments have started
                    for (MethodParameter ma : reversed) {
                        if (!(ma instanceof OptionMetaData)) continue;
                        OptionMetaData omd = (OptionMetaData)ma;
                        if (omd.isArgument()) {
                            if (!omd.isOptional()) {
                                positional = true;
                            }
                            else if (positional) {
                                throw new CommandException("Optional parameters must follow all non-optional ones");
                            }
                        }
                    }

                    CommandMetaData cmd = new CommandMetaData(handler, method, options, permissions, requireAll, command.description());
                    for (String commandName : command.value()) {
                        if (commandMap.put(commandName, cmd) != null) {
                            throw new CommandException("Duplicate command: " + commandName);
                        }
                    }
                }
            }
        }
    }

    // Given parsed arguments and metadata, create an argument list suitable
    // for reflective invoke.
    private Object[] buildMethodArgs(CommandMetaData cmd, CommandSender sender, Method method, ParsedArgs pa, String label, InvocationChain invChain) {
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
                    result.add(getHelpBuilder(invChain));
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
                            result.add(Boolean.valueOf(text));
                        }
                        else if (!omd.isOptional()) {
                            // Missing positional argument
                            throw new ParseException(ChatColor.RED + "Missing argument: " + omd.getName());
                        }
                        else {
                            // Flag not specified
                            result.add(Boolean.FALSE);
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
                        catch (SecurityException e) {
                            throw new CommandException(e);
                        }
                        catch (IllegalArgumentException e) {
                            throw new CommandException(e);
                        }
                        catch (NoSuchMethodException e) {
                            throw new CommandException(e);
                        }
                        catch (IllegalAccessException e) {
                            throw new CommandException(e);
                        }
                        catch (InvocationTargetException e) {
                            // Unwrap, see if it's a NumberFormatException
                            if (e.getCause() instanceof NumberFormatException) {
                                // Complain
                                throw new ParseException(ChatColor.RED + "Invalid number: " + omd.getName());
                            }
                            else
                                throw new CommandException(e.getCause());
                        }
                    }
                }
                else {
                    if (omd.isArgument() && !omd.isOptional()) {
                        // Missing positional argument
                        throw new ParseException(ChatColor.RED + "Missing argument: " + omd.getName());
                    }
                    
                    result.add(null);
                }
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
     * @param cmdChain TODO
     */
    void execute(CommandSender sender, String name, String label, String[] args, InvocationChain invChain) {
        CommandMetaData cmd = commandMap.get(name);
        if (cmd == null)
            throw new ParseException(ChatColor.RED + "Unknown command: " + name);

        // Check permissions
        if (cmd.isRequireAll()) {
            PermissionUtils.requireAllPermissions(sender, cmd.getPermissions());
        }
        else {
            PermissionUtils.requireOnePermission(sender, cmd.getPermissions());
        }

        // Save into chain
        if (invChain != null)
            invChain.addInvocation(label, cmd);

        ParsedArgs pa = ParsedArgs.parse(cmd, args);
        Object[] methodArgs = buildMethodArgs(cmd, sender, cmd.getMethod(), pa, label, invChain);
        Object nextHandler = null;
        try {
            nextHandler = cmd.getMethod().invoke(cmd.getHandler(), methodArgs);
        }
        catch (IllegalArgumentException e) {
            throw new CommandException(e);
        }
        catch (IllegalAccessException e) {
            throw new CommandException(e);
        }
        catch (InvocationTargetException e) {
            // Unwrap exception, re-wrap with CommandException, re-throw
            if (e.getCause() instanceof ParseException) {
                // Unless it's a ParseException, then don't wrap
                throw (ParseException)e.getCause();
            }
            else {
                throw new CommandException(e.getCause());
            }
        }

        if (nextHandler != null) {
            // Handle a sub-command
            // Note: Original handler method is responsible for throwing
            // ParseException to display usage (if needed).
            if (args.length >= 1) {
                // Check HandlerExecutor cache
                HandlerExecutor<T> he = handlerExecutorFor(nextHandler);

                // Chain to next handler
                String subName = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);

                he.execute(sender, subName, subName, args, invChain);
            }
        }
    }

    // Add the named CommandMetaData to an InvocationChain, returning a copy
    InvocationChain fillInvocationChain(InvocationChain invChain, String label) {
        CommandMetaData cmd = commandMap.get(label);
        if (cmd == null)
            throw new IllegalArgumentException("Unknown command: " + label);
        invChain = invChain.copy();
        invChain.addInvocation(label, cmd);
        return invChain;
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
    HelpBuilder getHelpBuilder(InvocationChain rootInvocationChain) {
        return new HelpBuilder(this, rootInvocationChain, usageOptions);
    }

}
