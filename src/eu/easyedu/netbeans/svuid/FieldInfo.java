/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import java.lang.reflect.Modifier;
import java.util.Set;
import javax.lang.model.element.Name;

/**
 *
 * @author hlavki
 */
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
        return access & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED |
                Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE | Modifier.TRANSIENT);
    }


    @Override
    public String getSortingName() {
        return name;
    }
}
