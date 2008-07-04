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
public final class FieldInfo extends ElementInfo {

    public FieldInfo(Name name, Set<Modifier> modifiers, String description) {
        super(name, modifiers, description);
    }

    @Override
    public boolean includeInSerialVersionUID() {
        return (access & OpCodes.ACC_PRIVATE) == 0 || (access & (OpCodes.ACC_STATIC | OpCodes.ACC_TRANSIENT)) == 0;
    }

    @Override
    public int getSvuidAccess() {
        return access & (OpCodes.ACC_PUBLIC | OpCodes.ACC_PRIVATE | OpCodes.ACC_PROTECTED |
                OpCodes.ACC_STATIC | OpCodes.ACC_FINAL | OpCodes.ACC_VOLATILE | OpCodes.ACC_TRANSIENT);
    }

    @Override
    public String getSortingName() {
        return name;
    }
}
