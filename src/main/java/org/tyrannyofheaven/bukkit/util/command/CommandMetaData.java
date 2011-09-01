package org.tyrannyofheaven.bukkit.util.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CommandMetaData extends SubCommandMetaData {

    private final List<MethodParameter> parameters;

    private final Set<OptionMetaData> flagOptions;
    
    private final List<OptionMetaData> positionalArguments;
    
    public CommandMetaData(Object handler, Method method, List<MethodParameter> options, String[] permissions, boolean requireAll) {
        super(handler, method, permissions, requireAll);
        this.parameters = Collections.unmodifiableList(new ArrayList<MethodParameter>(options));
        
        Set<OptionMetaData> flagOptions = new HashSet<OptionMetaData>();
        List<OptionMetaData> positionalArguments = new ArrayList<OptionMetaData>();
        for (MethodParameter mp : this.parameters) {
            if (mp instanceof OptionMetaData) {
                OptionMetaData omd = (OptionMetaData)mp;
                if (omd.isArgument()) {
                    positionalArguments.add(omd);
                }
                else {
                    flagOptions.add(omd);
                }
            }
        }
        
        this.flagOptions = Collections.unmodifiableSet(flagOptions);
        this.positionalArguments = Collections.unmodifiableList(positionalArguments);
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public Set<OptionMetaData> getFlagOptions() {
        return flagOptions;
    }

    public List<OptionMetaData> getPositionalArguments() {
        return positionalArguments;
    }

}
