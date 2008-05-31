/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author hlavki
 */
public class ClassInfo extends ElementInfo {

    public ClassInfo(TypeElement el) {
        super(el.getQualifiedName(), null);
        this.access = getAccessFlag(el.getModifiers()) | getInitialAccessFlag(el);
    }

    private final int getInitialAccessFlag(TypeElement el) {
        int accessFlag = 0;
        if (el.getKind().equals(ElementKind.INTERFACE)) {
            accessFlag = OpCodes.ACC_INTERFACE | OpCodes.ACC_ABSTRACT;
        } else {
            accessFlag = OpCodes.ACC_SUPER;
        }
        return accessFlag;
    }

    @Override
    public int getSvuidAccess() {
        return access & (OpCodes.ACC_PUBLIC | OpCodes.ACC_FINAL | OpCodes.ACC_INTERFACE | OpCodes.ACC_ABSTRACT);
    }
}
