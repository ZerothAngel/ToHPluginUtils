package org.tyrannyofheaven.bukkit.util;

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
        types.add(Server.class);
        types.add(Plugin.class);
        types.add(CommandSender.class);
        types.add(String.class);
        types.add(Boolean.class);
        types.add(Short.class);
        types.add(Integer.class);
        types.add(Long.class);
        types.add(Float.class);
        types.add(Double.class);
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
                    List<OptionMetaData> options = new ArrayList<OptionMetaData>();

                    // Scan each parameter
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        Class<?> paramType = method.getParameterTypes()[i];
                        Annotation[] anns = method.getParameterAnnotations()[i];
                        
                        boolean supported = false;
                        for (Class<?> supportedType : supportedParameterTypes) {
                            if (supportedType.isAssignableFrom(paramType)) {
                                supported = true;
                                break;
                            }
                        }
                        
                        if (!supported) {
                            throw new RuntimeException(); // FIXME placeholder
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

    private Object[] buildMethodArgs(CommandSender sender, Method method, ParsedArgs pa) {
        List<Object> result = new ArrayList<Object>(method.getParameterTypes().length);
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> paramType = method.getParameterTypes()[i];
            Annotation[] anns = method.getParameterAnnotations()[i];
            if (Server.class.isAssignableFrom(paramType)) {
                result.add(plugin.getServer());
            }
            else if (Plugin.class.isAssignableFrom(paramType)) {
                result.add(plugin);
            }
            else if (CommandSender.class.isAssignableFrom(paramType)) {
                result.add(sender);
            }
            else {
                Option optAnn = null;
                for (Annotation ann : anns) {
                    if (ann instanceof Option) {
                        optAnn = (Option)ann;
                        break;
                    }
                }
                
                if (optAnn == null) {
                    // Not annotated. Choke.
                    throw new RuntimeException(); // FIXME
                }
            }
        }
        return result.toArray();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        CommandMetaData cmd = commandMap.get(command.getName());
        if (cmd != null) {
            ParsedArgs pa = new ParsedArgs();
            if (pa != null) {
                Object[] methodArgs = buildMethodArgs(sender, cmd.getMethod(), pa);
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
