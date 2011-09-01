package org.tyrannyofheaven.bukkit.util.command;

import java.lang.reflect.Method;
import java.util.Arrays;

class SubCommandMetaData {

    private final Object handler;

    private final Method method;

    private final String[] permissions;
    
    private final boolean requireAll;

    protected SubCommandMetaData(Object handler, Method method, String[] permissions, boolean requireAll) {
        this.handler = handler;
        this.method = method;
        this.permissions = Arrays.copyOf(permissions, permissions.length);
        this.requireAll = requireAll;
    }

    public Object getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public boolean isRequireAll() {
        return requireAll;
    }

}
