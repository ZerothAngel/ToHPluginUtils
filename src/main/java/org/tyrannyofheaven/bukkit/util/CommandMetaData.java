package org.tyrannyofheaven.bukkit.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandMetaData {

    private final Object handler;
    
    private final Method method;

    private final List<OptionMetaData> options;
    
    public CommandMetaData(Object handler, Method method, List<OptionMetaData> options) {
        this.handler = handler;
        this.method = method;
        this.options = new ArrayList<OptionMetaData>(options);
    }

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }

    public List<OptionMetaData> getOptions() {
        return options;
    }

}
