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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@Hint(displayName = "#DN_SerialVersionUID", description = "#DESC_SerialVersionUID", id = "eu.easyedu.netbeans.svuid.SerialVersionUidHint", category = "general", enabled = true, options = Options.QUERY, suppressWarnings = SvuidHelper.SUPPRESS_WARNING_SERIAL)
public class SerialVersionUidHint {

    private static final String SVUID = "serialVersionUID";
    static final String WARN_FOR_INCORRECT_VALUE_KEY = "warn-for-incorrect-value";
    static final String IGNORED_VALUES_KEY = "wfiv-ingored-values";
    static final String IGNORED_VALUES_DEFAULT = "0L,1L";
    static final boolean WARN_FOR_INCORRECT_VALUE_DEFAULT = false;
    protected final WorkingCopy copy = null;

    public SerialVersionUidHint() {
    }

    @TriggerTreeKind(Kind.CLASS)
    public static List<ErrorDescription> run(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        if (treePath == null || treePath.getLeaf().getKind() != Kind.CLASS) {
            return null;
        }
        TypeElement type = (TypeElement) compilationInfo.getTrees().getElement(treePath);
        if (!SvuidHelper.needsSerialVersionUID(type)) {
            return null;
        }

        // Contrary to popular belief, abstract classes *should* define serialVersionUID,
        // according to the documentation of Serializable. It refers to "all classes".
        Fix[] fixes = new Fix[2];
        ElementHandle<TypeElement> elementHandle = ElementHandle.create(type);
        TreePathHandle tpHandle = TreePathHandle.create(treePath, compilationInfo);
//        fixes[0] = new FixImpl(TreePathHandle.create(treePath, compilationInfo), SvuidType.DEFAULT, elementHandle);
//        fixes[1] = new FixImpl(TreePathHandle.create(treePath, compilationInfo), SvuidType.GENERATED, elementHandle);
        fixes[0] = new JavaFixImpl(tpHandle, SvuidType.DEFAULT, elementHandle).toEditorFix();
        fixes[1] = new JavaFixImpl(tpHandle, SvuidType.GENERATED, elementHandle).toEditorFix();

        String desc = NbBundle.getMessage(SerialVersionUidHint.class, "ERR_SerialVersionUID"); //NOI18N
        int[] span;
        if (type.getNestingKind().equals(NestingKind.ANONYMOUS)) {
            SourcePositions pos = compilationInfo.getTrees().getSourcePositions();
            Iterator<Tree> trees = treePath.iterator();
            Tree clazzTree = null;
            while (trees.hasNext() && clazzTree == null) {
                Tree tree = trees.next();
                if (tree.getKind().equals(Tree.Kind.NEW_CLASS)) {
                    clazzTree = ((NewClassTree) tree).getIdentifier();
                }
            }
            if (clazzTree == null) clazzTree = treePath.getParentPath().getLeaf(); // mark all implementation!
            long start = pos.getStartPosition(compilationInfo.getCompilationUnit(), clazzTree);
            long end = pos.getEndPosition(compilationInfo.getCompilationUnit(), clazzTree);
            span = new int[]{(int) start, (int) end};
        } else {
//            fixes.addAll(FixFactory.createSuppressWarnings(compilationInfo, treePath, SvuidHelper.SUPPRESS_WARNING_SERIAL));
            span = compilationInfo.getTreeUtilities().findNameSpan((ClassTree) treePath.getLeaf());
        }
        ErrorDescription ed = ErrorDescriptionFactory.forSpan(ctx, span[0], span[1], desc, fixes);

        return Collections.singletonList(ed);
    }

    private static final class JavaFixImpl extends JavaFix {

        private final SvuidType svuidType;
        private final ElementHandle<TypeElement> classType;

        public JavaFixImpl(TreePathHandle tpHandle, SvuidType type,
                ElementHandle<TypeElement> classType) {
            super(tpHandle);
            this.svuidType = type;
            this.classType = classType;
        }

        @Override
        protected String getText() {
            switch (svuidType) {
                case GENERATED:
                    return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID_Generated");//NOI18N
                default:
                    return NbBundle.getMessage(getClass(), "HINT_SerialVersionUID");//NOI18N
            }
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {
            WorkingCopy copy = tc.getWorkingCopy();
            if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                return;
            }
            TreePath treePath = tc.getPath();
            if (!TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) {
                return;
            }
            ClassTree classTree = (ClassTree) treePath.getLeaf();
            TreeMaker make = copy.getTreeMaker();

            // documentation recommends private
            Set<Modifier> modifiers = EnumSet.of(PRIVATE, STATIC, FINAL);
            Long svuid = 1L;
            if (svuidType == SvuidType.GENERATED) {
                SerialVersionUIDService svuidService = Lookup.getDefault().lookup(SerialVersionUIDService.class);
                TypeElement typeEl = classType.resolve(copy);
                svuid = svuidService.generate(typeEl);
            }
            VariableTree svuidTree = make.Variable(make.Modifiers(modifiers), SVUID,
                    make.Identifier("long"), make.Literal(svuid)); //NO18N

            ClassTree decl = GeneratorUtilities.get(copy).insertClassMember(classTree, svuidTree);
            copy.rewrite(classTree, decl);
        }
    }
}
