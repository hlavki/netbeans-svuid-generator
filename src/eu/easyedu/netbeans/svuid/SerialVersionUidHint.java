/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import eu.easyedu.netbeans.svuid.service.SerialVersionUIDService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class SerialVersionUidHint extends AbstractHint {

    private static final Set<Tree.Kind> TREE_KINDS = EnumSet.<Tree.Kind>of(Tree.Kind.CLASS);
    private static final String SVUID = "serialVersionUID";
    protected final WorkingCopy copy = null;
    private AtomicBoolean cancel = new AtomicBoolean();

    public SerialVersionUidHint() {
        super(true, true, AbstractHint.HintSeverity.WARNING);
    }

    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        if (treePath == null || treePath.getLeaf().getKind() != Kind.CLASS) {
            return null;
        }
        cancel.set(false);
        TypeElement type = (TypeElement) info.getTrees().getElement(treePath);
        if (!SvuidHelper.needsSerialVersionUID(type)) {
            return null;
        }
        // Contrary to popular belief, abstract classes *should* define serialVersionUID,
        // according to the documentation of Serializable. It refers to "all classes".
        List<Fix> fixes = new ArrayList<Fix>();
        fixes.add(new FixImpl(TreePathHandle.create(treePath, info), SvuidType.DEFAULT, type));
        fixes.add(new FixImpl(TreePathHandle.create(treePath, info), SvuidType.GENERATED, type));
        fixes.addAll(FixFactory.createSuppressWarnings(info, treePath, SvuidHelper.SUPPRESS_WARNING_SERIAL));

        String desc = NbBundle.getMessage(getClass(), "ERR_SerialVersionUID"); //NOI18N
        ErrorDescription ed = null;
        Severity severity = getSeverity().toEditorSeverity();
        FileObject fo = info.getFileObject();
        if (type.getNestingKind().equals(NestingKind.ANONYMOUS)) {
            SourcePositions pos = info.getTrees().getSourcePositions();
            Iterator<Tree> trees = treePath.iterator();
            Tree clazzTree = null;
            while (trees.hasNext() && clazzTree == null) {
                Tree tree = trees.next();
                if (tree.getKind().equals(Tree.Kind.NEW_CLASS)) {
                    clazzTree = ((NewClassTree) tree).getIdentifier();
                }
            }
            if (clazzTree == null) clazzTree = treePath.getParentPath().getLeaf(); // mark all implementation!
            long start = pos.getStartPosition(info.getCompilationUnit(), clazzTree);
            long end = pos.getEndPosition(info.getCompilationUnit(), clazzTree);
            ed = ErrorDescriptionFactory.createErrorDescription(severity, desc, fixes, fo, (int) start, (int) end);
        } else {
            int[] span = info.getTreeUtilities().findNameSpan((ClassTree) treePath.getLeaf());
            ed = ErrorDescriptionFactory.createErrorDescription(severity, desc, fixes, fo, span[0], span[1]);
        }
        if (cancel.get()) {
            return null;
        }
        return ed == null ? Collections.<ErrorDescription>emptyList() : Collections.singletonList(ed);
    }

    public void cancel() {
        cancel.set(true);
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DN_SerialVersionUID");//NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(getClass(), "DSC_SerialVersionUID"); //NOI18N
    }

    private static final class FixImpl implements Fix, Task<WorkingCopy> {

        private TreePathHandle handle;
        private SvuidType type;
        private TypeElement classType;

        public FixImpl(TreePathHandle handle, SvuidType type, TypeElement classType) {
            this.handle = handle;
            this.type = type;
            this.classType = classType;
        }

        public String getText() {
            switch (type) {
                case GENERATED:
                    return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID_Generated");//NOI18N
                default:
                    return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID");//NOI18N
            }
        }

        public ChangeInfo implement() throws Exception {
            JavaSource js = JavaSource.forFileObject(handle.getFileObject());
            js.runModificationTask(this).commit();
            return null;
        }

        public void run(WorkingCopy copy) throws Exception {
            if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                return;
            }
            TreePath treePath = handle.resolve(copy);
            if (treePath == null || treePath.getLeaf().getKind() != Kind.CLASS) {
                return;
            }
            ClassTree classTree = (ClassTree) treePath.getLeaf();
            TreeMaker make = copy.getTreeMaker();

            // documentation recommends private
            Set<Modifier> modifiers = EnumSet.of(PRIVATE, STATIC, FINAL);
            Long svuid = 1L;
            if (type.equals(SvuidType.GENERATED)) {
                SerialVersionUIDService svuidService = Lookup.getDefault().lookup(SerialVersionUIDService.class);
                svuid = svuidService.generate(classType);
            }
            VariableTree svuidTree = make.Variable(make.Modifiers(modifiers), SVUID,
                    make.Identifier("long"), make.Literal(svuid)); //NO18N

            ClassTree decl = GeneratorUtilities.get(copy).insertClassMember(classTree, svuidTree);
            copy.rewrite(classTree, decl);
        }
    }

    @Override
    public String toString() {
        return "Fix";
    }
}

