/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;

/**
 *
 * @author hlavki
 */
public class SvuidHelper {

    private static final Logger log = Logger.getLogger(SvuidHelper.class.getName());
    private static final String ERROR = "<error>"; //NOI18N
    private static final String SERIALIZABLE_CLASS = "java.io.Serializable";
    public static String SUPPRESS_WARNING_SERIAL = "serial";

    private SvuidHelper() {
    }

    public static void scanForStaticFields(CompilationInfo info, final TreePath clsPath,
            final Set<VariableElement> staticFields) {
        final Trees trees = info.getTrees();
        new TreePathScanner<Void, Boolean>() {

            @Override
            public Void visitVariable(VariableTree node, Boolean p) {
                if (ERROR.contentEquals(node.getName())) {
                    return null;
                }
                Element el = trees.getElement(getCurrentPath());
                if (el != null && el.getKind() == ElementKind.FIELD && el.getModifiers().contains(Modifier.STATIC)) {
                    staticFields.add((VariableElement) el);
                }
                return null;
            }
        }.scan(clsPath, Boolean.FALSE);
    }

    private static boolean containsSerialVersionField(TypeElement type) {
        boolean result = false;
        List<VariableElement> fields = ElementFilter.fieldsIn(type.getEnclosedElements());
        Iterator<VariableElement> it = fields.iterator();
        while (it.hasNext() && !result) {
            VariableElement e = it.next();
            Set<Modifier> modifiers = e.getModifiers();
            // documentation says ANY-ACCESS-MODIFIER static final long serialVersionUID
            if (modifiers.containsAll(EnumSet.of(STATIC, FINAL))) {
                TypeMirror t = e.asType();
                if (t.getKind() != null && t.getKind() == TypeKind.LONG) {
                    return true;
                }
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Class " + type.asType().toString() + (result ? "" : " does not") + " contain serialVersionUID field");
        }
        return result;
    }

    public static boolean needsSerialVersionUID(TypeElement type) {
        return isSerializable(type) && !containsSerialVersionField(type) && type.getKind() != ElementKind.ENUM
                && !hasSuppressWarning(type, SvuidHelper.SUPPRESS_WARNING_SERIAL);
    }

    private static boolean isSerializable(TypeElement type) {
        boolean result = false;
        Collection<TypeElement> parents = GeneratorUtils.getAllParents(type);
        Iterator<TypeElement> it = parents.iterator();
        while (it.hasNext() && !result) {
            TypeElement parent = it.next();
            StringBuilder qualifiedName = new StringBuilder(parent.getQualifiedName());
            result = (parent.getKind().equals(ElementKind.INTERFACE) && SERIALIZABLE_CLASS.equals(qualifiedName.toString()));
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Class " + type.asType().toString() + (result ? " is" : " is not") + " serializable");
        }
        return result;
    }

    /**
     * check if contains SuppressWarnings with value serial
     * @param typeElement
     * @return
     */
    private static boolean hasSuppressWarning(TypeElement type, String warning) {
        boolean result = false;
        SuppressWarnings annotation = type.getAnnotation(SuppressWarnings.class);
        if (annotation != null) {
            String[] values = annotation.value();
            int idx = 0;
            while (idx < values.length && result == false) {
                result = warning.equals(values[idx++]);
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Class " + type.asType().toString() + (result ? "" : " does not")
                    + " contain SuppressWarnings(serial) annotation");
        }
        return result;
    }
}
