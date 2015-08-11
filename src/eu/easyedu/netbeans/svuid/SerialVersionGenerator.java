/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import eu.easyedu.netbeans.svuid.service.SerialVersionUIDService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.*;
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

    private static final String SVUID_DEFAULT_LABEL = "LBL_SerialVersionGenerator_default";
    private static final String SVUID_GENERATED_LABEL = "LBL_SerialVersionGenerator_generated";
    private static final String SVUID_FIELD = "serialVersionUID";
    private final SvuidType type;
    private final JTextComponent component;

    public static class Factory implements CodeGenerator.Factory {

        public Factory() {
        }

        @Override
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
            if (typeElement == null || typeElement.getKind().isInterface()) {
                return Collections.emptyList();
            }
            if (!SvuidHelper.needsSerialVersionUID(typeElement)) {
                return Collections.emptyList();
            }
            List<CodeGenerator> result = new ArrayList<>();
            result.add(new SerialVersionGenerator(component, SvuidType.DEFAULT));
            result.add(new SerialVersionGenerator(component, SvuidType.GENERATED));
            return result;
        }
    }

    public SerialVersionGenerator(JTextComponent component, SvuidType type) {
        this.component = component;
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        String msg = type == SvuidType.DEFAULT ? SVUID_DEFAULT_LABEL : SVUID_GENERATED_LABEL;
        return NbBundle.getMessage(getClass(), msg);
    }

    @Override
    public void invoke() {
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                final int caretOffset = component.getCaretPosition();
                ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {

                    @Override
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
                        path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                        ClassTree clazz = (ClassTree) path.getLeaf();
                        long svuid = 1L;
                        if (type.equals(SvuidType.GENERATED)) {
                            TypeElement typeElement = (TypeElement) copy.getTrees().getElement(path);
                            SerialVersionUIDService svuidService
                                    = Lookup.getDefault().lookup(SerialVersionUIDService.class);
                            svuid = svuidService.generate(typeElement);
                        }
//                        int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree) path.getLeaf(), caretOffset);

                        Set<Modifier> modifiers = EnumSet.of(PRIVATE, STATIC, FINAL);
                        TreeMaker make = copy.getTreeMaker();
                        VariableTree var = make.Variable(make.Modifiers(modifiers),
                                SVUID_FIELD, make.Identifier("long"), make.Literal(Long.valueOf(svuid))); //NO18N

                        copy.rewrite(clazz, GeneratorUtils.insertClassMembers(copy, clazz, Collections.singletonList(var), caretOffset));
                    }
                });
                GeneratorUtils.guardedCommit(component, mr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
