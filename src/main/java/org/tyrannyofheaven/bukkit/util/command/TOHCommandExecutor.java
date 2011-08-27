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

import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class TOHCommandExecutor implements CommandExecutor {

    private final Plugin plugin;

    private final List<Object> handlers = new ArrayList<Object>();

    private final Map<String, CommandMetaData> commandMap = new HashMap<String, CommandMetaData>();

    private static final Set<Class<?>> supportedParameterTypes;
    
    static {
        // Build set of supported parameter types
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(String.class);
        types.add(Boolean.class);
        types.add(Boolean.TYPE);
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
    
    public TOHCommandExecutor(Plugin plugin, Object... handlers) {
        this.plugin = plugin;
        this.handlers.addAll(Arrays.asList(handlers));
        processHandlers();
    }

    private void processHandlers() {
        for (Object handler : handlers) {
            Class<?> clazz = handler.getClass();
            // Scan each method
            for (Method method : clazz.getMethods()) {
                // @Command present?
                Command command = method.getAnnotation(Command.class);
                if (command != null) {
                    List<MethodParameter> options = new ArrayList<MethodParameter>();

                    boolean hasRest = false;

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

                            if (optAnn == null && restAnn == null) {
                                throw new RuntimeException(); // FIXME placeholder
                            }

                            // Is it @Rest?
                            if (restAnn != null) {
                                if (hasRest) {
                                    throw new RuntimeException(); // FIXME
                                }

                                // Is it an array of Strings?
                                if (!paramType.isArray() || paramType.getComponentType() != String.class) {
                                    throw new RuntimeException(); // FIXME
                                }

                                ma = new SpecialParameter(SpecialParameter.Type.REST);
                                hasRest = true;
                            }
                            else {
                                // Must be @Option

                                // Supported parameter type?
                                if (!supportedParameterTypes.contains(paramType)) {
                                    throw new RuntimeException(); // FIXME placeholder
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
                                throw new RuntimeException(); // FIXME placeholder
                            }
                        }
                    }

                    CommandMetaData cmd = new CommandMetaData(handler, method, options);
                    for (String commandName : command.value()) {
                        if (commandMap.put(commandName, cmd) != null) {
                            // TODO warn about dupe
                        }
                    }
                }
            }
        }
    }

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
                // If Boolean or boolean, treat specially
                if (omd.getType() == Boolean.class || omd.getType() == Boolean.TYPE) {
                    if (omd.isArgument()) {
                        if (pa.hasOption(omd.getName())) {
                            result.add(Boolean.valueOf(pa.getOption(omd.getName())));
                        }
                        else if (!omd.isOptional()) {
                            // Missing positional argument
                            throw new RuntimeException(); // FIXME
                        }
                        else {
                            // Flag not specified
                            result.add(Boolean.FALSE);
                        }
                    }
                    else {
                        // Flag
                        result.add(Boolean.valueOf(pa.hasOption(omd.getName())));
                    }
                }
                else if (pa.hasOption(omd.getName())) {
                    String text = pa.getOption(omd.getName());
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    if (omd.isArgument() && !omd.isOptional()) {
                        // Missing positional argument
                        throw new RuntimeException(); // FIXME
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

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        CommandMetaData cmd = commandMap.get(label);
        if (cmd != null) {
            ParsedArgs pa = ParsedArgs.parse(cmd, args);
            if (pa != null) {
                Object[] methodArgs = buildMethodArgs(cmd, sender, cmd.getMethod(), pa);
                try {
                    cmd.getMethod().invoke(cmd.getHandler(), methodArgs);
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

}
