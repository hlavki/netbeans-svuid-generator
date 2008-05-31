/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import eu.easyedu.netbeans.svuid.resources.BundleHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.CodeGenerator;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author hlavki
 */
public class SerialVersionGenerator implements CodeGenerator {

    private static final String ERROR = "<error>"; //NOI18N
    private SerialVersionUIDType type;

    public static class Factory implements CodeGenerator.Factory {

        public Factory() {
        }

        public Iterable<? extends CodeGenerator> create(CompilationController controller, TreePath path) throws IOException {
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (path == null) {
                return Collections.emptySet();
            }
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass() || NestingKind.ANONYMOUS.equals(typeElement.getNestingKind())) {
                return Collections.emptySet();
            }
//	    final Set<VariableElement> staticFields = new LinkedHashSet<VariableElement>();
//	    scanForStaticFields(controller, path, staticFields);
            Elements elements = controller.getElements();
            List<VariableElement> fields = ElementFilter.fieldsIn(elements.getAllMembers(typeElement));

            Collection<TypeElement> parents = GeneratorUtils.getAllParents(typeElement);
            if (!isSerializable(parents) || containsSerialVersionField(fields)) {
                return Collections.emptySet();
            }

            List<CodeGenerator> result = new ArrayList<CodeGenerator>();
            result.add(new SerialVersionGenerator(SerialVersionUIDType.DEFAULT));
            result.add(new SerialVersionGenerator(SerialVersionUIDType.GENERATED));
            return result;
        }
    }

    public SerialVersionGenerator(SerialVersionUIDType type) {
        this.type = type;
    }

    public String getDisplayName() {
        String msg = type.equals(SerialVersionUIDType.DEFAULT) 
                ? Constants.SVUID_DEFAULT_LABEL : Constants.SVUID_GENERATED_LABEL;
        return NbBundle.getMessage(BundleHelper.class, msg);
    }

    public void invoke(JTextComponent component) {
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                final int caretOffset = component.getCaretPosition();
                ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
                        path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                        ClassTree clazz = (ClassTree) path.getLeaf();
                        long svuid = 1L;
                        if (type.equals(SerialVersionUIDType.GENERATED)) {
                            TypeElement typeElement = (TypeElement) copy.getTrees().getElement(path);
                            svuid = new SerialVersionUID().generate(typeElement);
                        }
                        int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree) path.getLeaf(), caretOffset);
                        VariableTree varTree = createSerialVersionUID(copy, svuid);
                        List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
                        members.add(idx, varTree);
                        TreeMaker make = copy.getTreeMaker();
                        ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(),
                                clazz.getTypeParameters(), clazz.getExtendsClause(),
                                clazz.getImplementsClause(), members);
//			ClassTree decl = GeneratorUtilities.get(copy).insertClassMember(classTree, varTree);
                        copy.rewrite(clazz, nue);

                    }
                });
                GeneratorUtils.guardedCommit(component, mr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public final static void scanForStaticFields(CompilationInfo info, final TreePath clsPath,
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

    public final static boolean containsSerialVersionField(Collection<VariableElement> elements) {
        boolean result = false;
        Iterator<VariableElement> it = elements.iterator();
        while (it.hasNext() && !result) {
            VariableElement elem = it.next();
            Set<Modifier> modifiers = elem.getModifiers();
            TypeMirror typeMirror = elem.asType();
            StringBuffer sb = new StringBuffer(elem.getSimpleName());
            result = Constants.SERIAL_VERSION_FIELD.equals(sb.toString()) && modifiers.contains(Modifier.FINAL) &&
                    modifiers.contains(Modifier.STATIC) && typeMirror.getKind().isPrimitive() &&
                    typeMirror.getKind().equals(TypeKind.LONG);
        }
        return result;
    }

    public final static boolean isSerializable(Collection<TypeElement> parents) {
        boolean result = false;
        Iterator<TypeElement> it = parents.iterator();
        while (it.hasNext() && !result) {
            TypeElement type = it.next();
            StringBuffer qualifiedName = new StringBuffer(type.getQualifiedName());
            result = (type.getKind().equals(ElementKind.INTERFACE) && 
                    Constants.SERIALIZABLE_INTERFACE.equals(qualifiedName.toString()));
        }
        return result;
    }

    /**
     * Creates the <code>serialVersionUID</code> field with
     * value of <code>serialVersion</code>.
     * 
     * @return the created field.
     */
    public static final VariableTree createSerialVersionUID(WorkingCopy copy, Long serialVersion) {
        Set<Modifier> serialVersionUIDModifiers = new HashSet<Modifier>();
        serialVersionUIDModifiers.add(Modifier.PRIVATE);
        serialVersionUIDModifiers.add(Modifier.STATIC);
        serialVersionUIDModifiers.add(Modifier.FINAL);
        TreeMaker make = copy.getTreeMaker();
        VariableTree serialVersionUID = make.Variable(make.Modifiers(serialVersionUIDModifiers),
                Constants.SERIAL_VERSION_FIELD, make.Identifier("long"), make.Literal(Long.valueOf(serialVersion))); //NO18N

        return serialVersionUID;
    }
}

