/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 *
 * @author hlavki
 */
public abstract class ElementInfo implements Comparable<ElementInfo> {

    protected String name;
    protected int access = 0;
    protected String descriptor;

    public ElementInfo(Name name, Set<Modifier> modifiers, String descriptor) {
        this.name = new StringBuilder(name).toString();
        this.access = getAccessFlag(modifiers);
        this.descriptor = descriptor;
    }

    public ElementInfo(Name name, String description) {
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
        int result = 0;
        switch (modifier) {
            case ABSTRACT:
                result = java.lang.reflect.Modifier.ABSTRACT;
                break;
            case FINAL:
                result = java.lang.reflect.Modifier.FINAL;
                break;
            case NATIVE:
                result = java.lang.reflect.Modifier.NATIVE;
                break;
            case PRIVATE:
                result = java.lang.reflect.Modifier.PRIVATE;
                break;
            case PROTECTED:
                result = java.lang.reflect.Modifier.PROTECTED;
                break;
            case PUBLIC:
                result = java.lang.reflect.Modifier.PUBLIC;
                break;
            case STATIC:
                result = java.lang.reflect.Modifier.STATIC;
                break;
            case STRICTFP:
                result = java.lang.reflect.Modifier.STRICT;
                break;
            case SYNCHRONIZED:
                result = java.lang.reflect.Modifier.SYNCHRONIZED;
                break;
            case TRANSIENT:
                result = java.lang.reflect.Modifier.TRANSIENT;
                break;
            case VOLATILE:
                result = java.lang.reflect.Modifier.VOLATILE;
                break;
        }
        return result;
    }
}
