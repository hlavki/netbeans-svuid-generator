/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import eu.easyedu.netbeans.svuid.resources.BundleHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author hlavki
 */
public class SerialVersionGenerator implements CodeGenerator {

    private static final String ERROR = "<error>"; //NOI18N
    private SerialVersionUIDType type;
    private JTextComponent component;

    public static class Factory implements CodeGenerator.Factory {

        public Factory() {
        }

        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
            if (component == null || controller == null || path == null) {
                return Collections.emptyList();
            }
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException e) {
                return Collections.emptyList();
            }
            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass() || NestingKind.ANONYMOUS.equals(typeElement.getNestingKind())) {
                return Collections.emptyList();
            }
            if (!SerialVersionUIDHelper.needsSerialVersionUID(typeElement)) {
                return Collections.emptyList();
            }
            List<CodeGenerator> result = new ArrayList<CodeGenerator>();
            result.add(new SerialVersionGenerator(component, SerialVersionUIDType.DEFAULT));
            result.add(new SerialVersionGenerator(component, SerialVersionUIDType.GENERATED));
            return result;
        }
    }

    public SerialVersionGenerator(JTextComponent component, SerialVersionUIDType type) {
        this.component = component;
        this.type = type;
    }

    public String getDisplayName() {
        String msg = type.equals(SerialVersionUIDType.DEFAULT)
                ? Constants.SVUID_DEFAULT_LABEL : Constants.SVUID_GENERATED_LABEL;
        return NbBundle.getMessage(BundleHelper.class, msg);
    }

    public void invoke() {
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
                        VariableTree varTree = SerialVersionUIDHelper.createSerialVersionUID(copy, svuid);
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
}

