package eu.hlavki.netbeans.svuid.model;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

public abstract class ElementInfo implements Comparable<ElementInfo> {

    @SuppressWarnings("ProtectedField")
    protected String name;
    @SuppressWarnings("ProtectedField")
    protected int access = 0;
    @SuppressWarnings("ProtectedField")
    protected String descriptor;

    protected ElementInfo(Name name, Set<Modifier> modifiers, String descriptor) {
        this.name = new StringBuilder(name).toString();
        this.access = getAccessFlag(modifiers);
        this.descriptor = descriptor;
    }

    protected ElementInfo(Name name, String description) {
        this(name, Collections.<Modifier>emptySet(), description);
    }

    public int getAccess() {
        return access;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

    public boolean includeInSerialVersionUID() {
        return true;
    }

    @SuppressWarnings("FinalMethod")
    protected final int getAccessFlag(Set<Modifier> modifiers) {
        int accessFlag = 0;
        for (Modifier modifier : modifiers) {
            accessFlag |= toModifier(modifier);
        }
        return accessFlag;
    }

    @Override
    public String toString() {
        return name + "|" + descriptor + "|" + getSvuidAccess();
    }

    @Override
    public int compareTo(ElementInfo o) {
        return this.getSortingName().compareTo(o.getSortingName());
    }

    public abstract int getSvuidAccess();

    public abstract String getSortingName();

    private int toModifier(Modifier modifier) {
        return switch (modifier) {
            case ABSTRACT -> java.lang.reflect.Modifier.ABSTRACT;
            case FINAL -> java.lang.reflect.Modifier.FINAL;
            case NATIVE -> java.lang.reflect.Modifier.NATIVE;
            case PRIVATE -> java.lang.reflect.Modifier.PRIVATE;
            case PROTECTED -> java.lang.reflect.Modifier.PROTECTED;
            case PUBLIC -> java.lang.reflect.Modifier.PUBLIC;
            case STATIC -> java.lang.reflect.Modifier.STATIC;
            case STRICTFP -> java.lang.reflect.Modifier.STRICT;
            case SYNCHRONIZED -> java.lang.reflect.Modifier.SYNCHRONIZED;
            case TRANSIENT -> java.lang.reflect.Modifier.TRANSIENT;
            case VOLATILE -> java.lang.reflect.Modifier.VOLATILE;
            default -> 0;
        };
    }
}
