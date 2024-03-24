package eu.hlavki.netbeans.svuid.model;

import eu.hlavki.netbeans.svuid.model.ElementInfo;
import java.lang.reflect.Modifier;
import java.util.Set;
import javax.lang.model.element.Name;

public class MethodInfo extends ElementInfo {

    public MethodInfo(Name name, Set<javax.lang.model.element.Modifier> modifiers, String descriptor) {
        super(name, modifiers, descriptor);
    }

    @Override
    public boolean includeInSerialVersionUID() {
        return (access & Modifier.PRIVATE) == 0;
    }

    @Override
    public int getSvuidAccess() {
        return access;
    }

    @Override
    public String getSortingName() {
        return name + descriptor;
    }
}
