package org.tyrannyofheaven.bukkit.util.command;

import java.lang.reflect.Method;

class SubCommandMetaData {

    protected final Object handler;

    protected final Method method;

    protected SubCommandMetaData(Object handler, Method method) {
        this.handler = handler;
        this.method = method;
    }

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }

}
