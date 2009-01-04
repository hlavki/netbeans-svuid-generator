/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 *
 * @author hlavki
 */
public class MethodInfo extends ElementInfo {

    public MethodInfo(Name name, Set<Modifier> modifiers, String descriptor) {
        super(name, modifiers, descriptor);
    }

    @Override
    public boolean includeInSerialVersionUID() {
        return (access & OpCodes.ACC_PRIVATE) == 0;
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
