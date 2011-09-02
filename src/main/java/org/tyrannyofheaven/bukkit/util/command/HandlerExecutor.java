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

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.util.permissions.PermissionUtils;

/**
 * The main class that drives annotation-driven command parsing.
 * 
 * @author zerothangel
 */
public class HandlerExecutor {

    private static final Set<Class<?>> supportedParameterTypes;
    
    private final Plugin plugin;

    private final Map<String, SubCommandMetaData> commandMap = new HashMap<String, SubCommandMetaData>();

    private final Map<Object, HandlerExecutor> subCommandMap = new WeakHashMap<Object, HandlerExecutor>();

    static {
        // Build set of supported parameter types
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(String.class);
        types.add(Boolean.class);
        types.add(Boolean.TYPE);
        types.add(Byte.class);
        types.add(Byte.TYPE);
        types.add(Short.class);
        types.add(Short.TYPE);
        types.add(Integer.class);
        types.add(Integer.TYPE);
        types.add(Long.class);
        types.add(Long.TYPE);
        types.add(Float.class);
        types.add(Float.TYPE);
        types.add(Double.class);
        types.add(Double.TYPE);
        supportedParameterTypes = Collections.unmodifiableSet(types);
    }
    
    /**
     * Create a HandlerExecutor instance.
     * 
     * @param plugin the associated plugin
     * @param handlers handler objects
     */
    public HandlerExecutor(Plugin plugin, Object... handlers) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        if (handlers == null)
            handlers = new Object[0];

        this.plugin = plugin;
        processHandlers(handlers);
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
                SubCommand subCommand = method.getAnnotation(SubCommand.class);

                if (command != null && subCommand != null) {
                    throw new CommandException("Methods must not have both the @Command and @SubCommand annotations");
                }
                else if (command != null) {
                    // Handle @Command
                    List<MethodParameter> options = new ArrayList<MethodParameter>();

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
                        else if (paramType.isAssignableFrom(Plugin.class)) {
                            ma = new SpecialParameter(SpecialParameter.Type.PLUGIN);
                        }
                        else if (paramType.isAssignableFrom(CommandSender.class)) {
                            ma = new SpecialParameter(SpecialParameter.Type.COMMAND_SENDER);
                        }
                        else {
                            // Grab the @Option/@Rest annotations
                            Option optAnn = null;
                            Rest restAnn = null;
                            for (Annotation ann : anns) {
                                if (ann instanceof Option) {
                                    optAnn = (Option)ann;
                                    break;
                                }
                                else if (ann instanceof Rest) {
                                    restAnn = (Rest)ann;
                                    break;
                                }
                            }

                            // One or the other must be present
                            if (optAnn == null && restAnn == null) {
                                throw new CommandException("Non-special parameters must be annotated with @Option or @Rest");
                            }

                            // Is it @Rest?
                            if (restAnn != null) {
                                if (hasRest) {
                                    throw new CommandException("Method already has a @Rest annotated parameter");
                                }

                                // Is it an array of Strings?
                                if (!paramType.isArray() || paramType.getComponentType() != String.class) {
                                    throw new CommandException("@Rest annotation must only be used on a String array parameter");
                                }

                                ma = new SpecialParameter(SpecialParameter.Type.REST);
                                hasRest = true;
                            }
                            else {
                                // Must be @Option

                                // Supported parameter type?
                                if (!supportedParameterTypes.contains(paramType)) {
                                    throw new CommandException("Unsupported parameter type: " + paramType);
                                }

                                ma = new OptionMetaData(optAnn.value(), paramType, optAnn.optional());
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

                    CommandMetaData cmd = new CommandMetaData(handler, method, options, permissions, requireAll);
                    for (String commandName : command.value()) {
                        if (commandMap.put(commandName, cmd) != null) {
                            throw new CommandException("Duplicate command: " + commandName);
                        }
                    }
                }
                else if (subCommand != null) {
                    // Handle @SubCommand
                    if (method.getParameterTypes().length != 0) {
                        throw new CommandException("Sub-command methods must take no arguments");
                    }
                    SubCommandMetaData scmd = new SubCommandMetaData(handler, method, permissions, requireAll);
                    for (String commandName : subCommand.value()) {
                        if (commandMap.put(commandName, scmd) != null) {
                            throw new CommandException("Duplicate sub-command: " + commandName);
                        }
                    }
                }
            }
        }
    }

    // Given parsed arguments and metadata, create an argument list suitable
    // for reflective invoke.
    private Object[] buildMethodArgs(CommandMetaData cmd, CommandSender sender, Method method, ParsedArgs pa) {
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
                else if (sp.getType() == SpecialParameter.Type.REST) {
                    result.add(pa.getRest());
                }
                else {
                    throw new AssertionError();
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
                            throw new ParseException("Missing argument: " + omd.getName());
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
                        try {
                            Method valueOf = omd.getType().getMethod("valueOf", String.class);
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
                            throw new CommandException(e);
                        }
                    }
                }
                else {
                    if (omd.isArgument() && !omd.isOptional()) {
                        // Missing positional argument
                        throw new ParseException("Missing argument: " + omd.getName());
                    }
                    
                    result.add(null);
                }
            }
            else {
                throw new AssertionError();
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
     * @return true if successfully handled, false otherwise
     */
    public boolean execute(CommandSender sender, String name, String[] args) {
        SubCommandMetaData scmd = commandMap.get(name);

        // Check permissions
        if (scmd.isRequireAll()) {
            PermissionUtils.requireAllPermissions(sender, scmd.getPermissions());
        }
        else {
            PermissionUtils.requireOnePermission(sender, scmd.getPermissions());
        }

        if (scmd instanceof CommandMetaData) {
            // Handle top-level command
            CommandMetaData cmd = (CommandMetaData)scmd;
            
            ParsedArgs pa = ParsedArgs.parse(cmd, args);
            if (pa != null) {
                Object[] methodArgs = buildMethodArgs(cmd, sender, cmd.getMethod(), pa);
                try {
                    cmd.getMethod().invoke(cmd.getHandler(), methodArgs);
                }
                catch (IllegalArgumentException e) {
                    throw new CommandException(e);
                }
                catch (IllegalAccessException e) {
                    throw new CommandException(e);
                }
                catch (InvocationTargetException e) {
                    throw new CommandException(e);
                }
                return true;
            }
        }
        else if (scmd != null) {
            // Handle a sub-command
            if (args.length >= 1) {
                String subName = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);

                // Execute method to grab handler
                Object handler = null;
                try {
                    handler = scmd.getMethod().invoke(scmd.getHandler(), (Object[])null);
                }
                catch (IllegalArgumentException e) {
                    throw new CommandException(e);
                }
                catch (IllegalAccessException e) {
                    throw new CommandException(e);
                }
                catch (InvocationTargetException e) {
                    throw new CommandException(e);
                }
                if (handler != null) {
                    // Check HandlerExecutor cache
                    HandlerExecutor he;
                    synchronized (this) {
                        he = subCommandMap.get(handler);
                        if (he == null) {
                            // No HandlerExecutor yet, create a new one
                            he = new HandlerExecutor(plugin, handler);
                            subCommandMap.put(handler, he);
                        }
                    }
                    // Chain to next handler
                    return he.execute(sender, subName, args);
                }
                else {
                    throw new CommandException("Sub-command method returned null for sub-command: " + subName);
                }
            }
        }
        return false;
    }

}
