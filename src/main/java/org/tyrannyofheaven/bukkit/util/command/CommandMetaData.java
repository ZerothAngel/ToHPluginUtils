package org.tyrannyofheaven.bukkit.util.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandMetaData {

    private final Object handler;
    
    private final Method method;

    private final List<MethodParameter> parameters;
    
    public CommandMetaData(Object handler, Method method, List<MethodParameter> options) {
        this.handler = handler;
        this.method = method;
        this.parameters = new ArrayList<MethodParameter>(options);
    }

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

}
