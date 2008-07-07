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
        this.name = nameToString(name);
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

    protected int getAccessFlag(Set<Modifier> modifiers) {
        int accessFlag = 0;
        for (Modifier modifier : modifiers) {
            accessFlag |= OpCodes.fromModifier(modifier);
        }
        return accessFlag;
    }

    @Override
    public String toString() {
        return name + "|" + descriptor + "|" + getSvuidAccess();
    }

    public int compareTo(ElementInfo o) {
        return this.getSortingName().compareTo(o.getSortingName());
    }

    protected String nameToString(Name name) {
        return new StringBuffer(name).toString();
    }

    public abstract int getSvuidAccess();

    public abstract String getSortingName();
}
