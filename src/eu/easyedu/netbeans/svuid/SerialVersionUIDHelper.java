/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.lang.annotation.IncompleteAnnotationException;
import java.util.Collection;
import java.util.HashSet;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;

/**
 *
 * @author hlavki
 */
public class SerialVersionUIDHelper {

    private static final Logger log = Logger.getLogger(SerialVersionUIDHelper.class.getName());
    private static final String ERROR = "<error>"; //NOI18N

    private SerialVersionUIDHelper() {
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

    private static boolean containsSerialVersionField(TypeElement typeElement) {
        boolean result = false;
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
        Iterator<VariableElement> it = fields.iterator();
        while (it.hasNext() && !result) {
            VariableElement elem = it.next();
            Set<Modifier> modifiers = elem.getModifiers();
            TypeMirror typeMirror = elem.asType();
            StringBuffer sb = new StringBuffer(elem.getSimpleName());
            result = Constants.SERIAL_VERSION_FIELD.equals(sb.toString()) && modifiers.contains(Modifier.FINAL) &&
                    modifiers.contains(Modifier.STATIC) && typeMirror.getKind().isPrimitive() &&
                    typeMirror.getKind().equals(TypeKind.LONG);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Class " + typeElement.asType().toString() + (result ? "" : " does not") + " contain serialVersionUID field");
        }
        return result;
    }

    public static boolean needsSerialVersionUID(TypeElement typeElement) {
        return isSerializable(typeElement) && !containsSerialVersionField(typeElement) &&
                !containsSuppressWarning(typeElement, SuppressWarning.SERIAL) &&
                !typeElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    private static boolean isSerializable(TypeElement typeElement) {
        boolean result = false;
        Collection<TypeElement> parents = GeneratorUtils.getAllParents(typeElement);
        Iterator<TypeElement> it = parents.iterator();
        while (it.hasNext() && !result) {
            TypeElement type = it.next();
            StringBuffer qualifiedName = new StringBuffer(type.getQualifiedName());
            result = (type.getKind().equals(ElementKind.INTERFACE) &&
                    java.io.Serializable.class.getName().equals(qualifiedName.toString()));
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Class " + typeElement.asType().toString() + (result ? " is" : " is not") + " serializable");
        }
        return result;
    }

    /**
     * Creates the <code>serialVersionUID</code> field with
     * value of <code>serialVersion</code>.
     * 
     * @return the created field.
     */
    public static VariableTree createSerialVersionUID(WorkingCopy copy, Long serialVersion) {
        Set<Modifier> serialVersionUIDModifiers = new HashSet<Modifier>();
        serialVersionUIDModifiers.add(Modifier.PRIVATE);
        serialVersionUIDModifiers.add(Modifier.STATIC);
        serialVersionUIDModifiers.add(Modifier.FINAL);
        TreeMaker make = copy.getTreeMaker();
        VariableTree serialVersionUID = make.Variable(make.Modifiers(serialVersionUIDModifiers),
                Constants.SERIAL_VERSION_FIELD, make.Identifier("long"), make.Literal(Long.valueOf(serialVersion))); //NO18N

        return serialVersionUID;
    }

    /**
     * check if contains SuppressWarnings with value serial
     * @param typeElement
     * @return
     */
    private static boolean containsSuppressWarning(TypeElement typeElement, SuppressWarning suppressWarning) {
        boolean result = false;
        SuppressWarnings suppressAnnotation = typeElement.getAnnotation(SuppressWarnings.class);
        try {
            if (suppressAnnotation != null) {
                String[] values = suppressAnnotation.value();
                int idx = 0;
                while (idx < values.length && result == false) {
                    result = suppressWarning.getCode().equals(values[idx++]);
                }
            }
        } catch (IncompleteAnnotationException e) {
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Class " + typeElement.asType().toString() + (result ? "" : " does not") +
                    " contain SuppressWarnings(serial) annotation");
        }
        return result;
    }
}