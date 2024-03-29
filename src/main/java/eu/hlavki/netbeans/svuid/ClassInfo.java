package eu.hlavki.netbeans.svuid;

import java.lang.reflect.Modifier;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

public class ClassInfo extends ElementInfo {

    public ClassInfo(TypeElement el) {
        super(el.getQualifiedName(), null);
        this.access = getAccessFlag(el.getModifiers()) | getInitialAccessFlag(el);
    }

    private int getInitialAccessFlag(TypeElement el) {
        int accessFlag = 0;
        if (el.getKind().equals(ElementKind.INTERFACE)) {
            accessFlag = Modifier.INTERFACE | Modifier.ABSTRACT;
        }
        return accessFlag;
    }

    @Override
    public int getSvuidAccess() {
        int modifier = access & (Modifier.PUBLIC | Modifier.FINAL | Modifier.INTERFACE | Modifier.ABSTRACT);
        return modifier;
    }

    @Override
    public String getSortingName() {
        return name + descriptor;
    }
}
