/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import eu.easyedu.netbeans.svuid.resources.BundleHelper;
import eu.easyedu.netbeans.svuid.service.SerialVersionUIDService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class SerialVersionUidHint extends AbstractHint {

    private static final Logger log = Logger.getLogger(SerialVersionUidHint.class.getName());
    private static final Set<Tree.Kind> TREE_KINDS = EnumSet.<Tree.Kind>of(Tree.Kind.CLASS);
    protected final WorkingCopy copy = null;


    public SerialVersionUidHint() {
        super(true, true, AbstractHint.HintSeverity.WARNING);
    }


    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }


    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        try {
            treePath = Utilities.getPathElementOfKind(Tree.Kind.CLASS, treePath);
            TypeElement typeElement = (TypeElement) info.getTrees().getElement(treePath);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Type of " + typeElement.asType().toString() + " is " + typeElement.getNestingKind());
            }
            if (typeElement.getKind().equals(ElementKind.CLASS)) {
                if (!SvuidHelper.needsSerialVersionUID(typeElement)) {
                    return Collections.emptyList();
                }
                List<Fix> fixes = new ArrayList<Fix>();
                TreePathHandle handle = TreePathHandle.create(treePath, info);
                fixes.add(new FixImpl(info.getFileObject(), handle, SvuidType.DEFAULT));
                fixes.add(new FixImpl(info.getFileObject(), handle, SvuidType.GENERATED));
                fixes.addAll(FixFactory.createSuppressWarnings(info, treePath, SvuidHelper.SUPPRESS_WARNING_SERIAL));

                int[] span = info.getTreeUtilities().findNameSpan((ClassTree) treePath.getLeaf());
                return Collections.<ErrorDescription>singletonList(
                        ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(),
                        getDisplayName(),
                        fixes,
                        info.getFileObject(),
                        span[0],
                        span[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void cancel() {
        // Do nothing
    }


    public String getId() {
        return NbBundle.getMessage(BundleHelper.class, "serial-version-hint-id"); // NOI18N
    }


    public String getDisplayName() {
        return NbBundle.getMessage(BundleHelper.class, "serial-version-hint-display-name");
    }


    public String getDescription() {
        return NbBundle.getMessage(BundleHelper.class, "serial-version-hint-description");
    }

    private static final class FixImpl implements Fix {

        private FileObject file;
        private TreePathHandle handle;
        private SvuidType type;
        private SerialVersionUIDService svuidService;


        public FixImpl(FileObject file, TreePathHandle handle, SvuidType type) {
            this.file = file;
            this.handle = handle;
            this.type = type;
            svuidService = Lookup.getDefault().lookup(SerialVersionUIDService.class);
        }


        public String getText() {
            String msg = type.equals(SvuidType.DEFAULT)
                    ? Constants.SVUID_DEFAULT_LABEL : Constants.SVUID_GENERATED_LABEL;
            return NbBundle.getMessage(BundleHelper.class, msg);
        }


        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);
            js.runModificationTask(new Task<WorkingCopy>() {

                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TreePath path = handle.resolve(copy);
                    path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                    long svuid = 1L;
                    if (type.equals(SvuidType.GENERATED)) {
                        TypeElement typeElement = (TypeElement) copy.getTrees().getElement(path);
                        svuid = svuidService.generate(typeElement);
                    }
                    ClassTree classTree = (ClassTree) path.getLeaf();
                    VariableTree varTree = SvuidHelper.createSerialVersionUID(copy, svuid);
                    ClassTree decl = GeneratorUtilities.get(copy).insertClassMember(classTree, varTree);
                    copy.rewrite(classTree, decl);
                }
            }).commit();
            return null;
        }
    }


    @Override
    public String toString() {
        return "Fix";
    }
}

