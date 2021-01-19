/*
 * Copyright (C) 2021 Michal Hlavac <miso@hlavki.eu>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
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
public class SerialVersionGenerator implements CodeGenerator
{
    private static final String SVUID_DEFAULT_LABEL     = "LBL_SerialVersionGenerator_default";
    private static final String SVUID_GENERATED_LABEL   = "LBL_SerialVersionGenerator_generated";
    private static final String SVUID_FIELD             = "serialVersionUID";

    private final SvuidType         type;
    private final JTextComponent    component;

    public static class Factory implements CodeGenerator.Factory
    {
        public Factory() {}

        @Override
        public List<? extends CodeGenerator> create( Lookup context )
        {
            JTextComponent          component   = context.lookup(JTextComponent.class);
            CompilationController   controller  = context.lookup(CompilationController.class);
            TreePath                path        = context.lookup( TreePath.class );

            path = Utilities.getPathElementOfKind( Tree.Kind.CLASS, path );
            if ( component == null || controller == null || path == null ) {
                return Collections.<CodeGenerator>emptyList();
            }

            try {
                controller.toPhase( JavaSource.Phase.ELEMENTS_RESOLVED );
            } catch ( IOException ex ) {
                return Collections.<CodeGenerator>emptyList();
            }

            TypeElement typeElement = (TypeElement) controller.getTrees().getElement( path );

            if ( typeElement == null || typeElement.getKind().isInterface() ) {
                return Collections.<CodeGenerator>emptyList();
            }

            if ( ! SvuidHelper.needsSerialVersionUID( typeElement ) ) {
                return Collections.<CodeGenerator>emptyList();
            }

            List<CodeGenerator> result = new ArrayList<>();

            result.add(new SerialVersionGenerator( component, SvuidType.DEFAULT   ) );
            result.add(new SerialVersionGenerator( component, SvuidType.GENERATED ) );

            return result;
        }
    }

    public SerialVersionGenerator( JTextComponent component, SvuidType type )
    {
        this.component  = component;
        this.type       = type;
    }

    @Override
    public String getDisplayName()
    {
        String msg = (this.type == SvuidType.DEFAULT) ? SerialVersionGenerator.SVUID_DEFAULT_LABEL : SerialVersionGenerator.SVUID_GENERATED_LABEL;

        return NbBundle.getMessage( this.getClass(), msg );
    }

    @Override
    public void invoke()
    {
        JavaSource js = JavaSource.forDocument( this.component.getDocument() );

        if ( js != null ) try {
            final int           caretOffset     = this.component.getCaretPosition();
            ModificationResult  svuidGenerator  = js.runModificationTask( (copy) ->
                {
                    copy.toPhase( JavaSource.Phase.ELEMENTS_RESOLVED );

                    TreePath path = copy.getTreeUtilities().pathFor( caretOffset );

                    path = Utilities.getPathElementOfKind( Tree.Kind.CLASS, path );

                    ClassTree clazz = (ClassTree) path.getLeaf();

                    long svuid = 1L;

                    if ( this.type.equals( SvuidType.GENERATED ) ) {
                        TypeElement             typeElement     = (TypeElement) copy.getTrees().getElement( path );
                        SerialVersionUIDService svuidService    = Lookup.getDefault().lookup( SerialVersionUIDService.class );

                        svuid = svuidService.generate( typeElement );
                    }

                    Set<Modifier>   modifiers   = EnumSet.of( Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL );
                    TreeMaker       make        = copy.getTreeMaker();
                    VariableTree    var         = make.Variable( make.Modifiers( modifiers ),
                                                                 SerialVersionGenerator.SVUID_FIELD,
                                                                 make.Identifier( long.class.getSimpleName() ),
                                                                 make.Literal( Long.valueOf( svuid ) ) ); //NOI18N

                    copy.rewrite( clazz, GeneratorUtils.insertClassMembers( copy, clazz, Collections.singletonList( var ), caretOffset ) );
                });

            GeneratorUtils.guardedCommit( this.component, svuidGenerator );
        } catch ( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
    }
}
