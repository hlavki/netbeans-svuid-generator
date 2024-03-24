package eu.hlavki.netbeans.svuid.model;

import eu.hlavki.netbeans.svuid.model.ElementInfo;
import java.lang.reflect.Modifier;
import java.util.Set;
import javax.lang.model.element.Name;

@SuppressWarnings("FinalClass")
public final class FieldInfo extends ElementInfo {

    public FieldInfo(Name name, Set<javax.lang.model.element.Modifier> modifiers, String description) {
        super(name, modifiers, description);
    }

    @Override
    public boolean includeInSerialVersionUID() {
        return (access & Modifier.PRIVATE) == 0 || (access & (Modifier.STATIC | Modifier.TRANSIENT)) == 0;
    }

    @Override
    public int getSvuidAccess() {
        return access & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
                | Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE | Modifier.TRANSIENT);
    }

    @Override
    public String getSortingName() {
        return name;
    }
}
