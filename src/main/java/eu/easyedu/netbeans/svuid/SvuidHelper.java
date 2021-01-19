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

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author hlavki
 */
public class SvuidHelper
{
    private static final Logger LOG = Logger.getLogger( SvuidHelper.class.getName() );
    private static final String ERROR                   = "<error>"; //NOI18N
    private static final String SERIALIZABLE_CLASS      = "java.io.Serializable";
    public  static final String SUPPRESS_WARNING_SERIAL = "serial";

    private SvuidHelper() {}

    public static void scanForStaticFields( CompilationInfo info, final TreePath clsPath, final Set<VariableElement> staticFields )
    {
        final Trees trees = info.getTrees();

        new TreePathScanner<Void, Boolean>() {
            @Override
            public Void visitVariable( VariableTree node, Boolean p ) {
                if ( SvuidHelper.ERROR.contentEquals( node.getName() ) ) return null;

                Element el = trees.getElement( this.getCurrentPath() );

                if ( el != null && el.getKind() == ElementKind.FIELD && el.getModifiers().contains( Modifier.STATIC ) ) {
                    staticFields.add( (VariableElement) el );
                }
                return null;
            }
        }.scan( clsPath, Boolean.FALSE );
    }

    private static boolean containsSerialVersionField( TypeElement type )
    {
        List<VariableElement>   fields  = ElementFilter.fieldsIn(type.getEnclosedElements());

        for ( VariableElement element : fields ) {
            Set<Modifier>   modifiers = element.getModifiers();

            // documentation says ANY-ACCESS-MODIFIER static final long serialVersionUID
            if ( modifiers.containsAll( EnumSet.of( Modifier.STATIC, Modifier.FINAL ) ) ) {
                TypeMirror mirror = element.asType();

                if ( mirror.getKind() != null && mirror.getKind() == TypeKind.LONG ) {
                    SvuidHelper.LOG.fine( "Class " + type.asType().toString() + " contains serialVersionUID field" );

                    return true;
                }
            }
        }

        SvuidHelper.LOG.fine("Class " + type.asType().toString() + " does not contain serialVersionUID field" );

        return false;
    }

    public static boolean needsSerialVersionUID( TypeElement type )
    {
        return SvuidHelper.isSerializable( type )               &&
               type.getKind() != ElementKind.ENUM               &&
             ! SvuidHelper.containsSerialVersionField( type )   &&
             ! SvuidHelper.hasSuppressWarning( type, SvuidHelper.SUPPRESS_WARNING_SERIAL );
    }

    private static boolean isSerializable( TypeElement type )
    {
        boolean                 result = false;
        Collection<TypeElement> parents = SvuidHelper.getAllParents( type );

        for ( TypeElement parent : parents ) {
            StringBuilder qualifiedName = new StringBuilder( parent.getQualifiedName() );

            result = (parent.getKind() == ElementKind.INTERFACE && SvuidHelper.SERIALIZABLE_CLASS.equals( qualifiedName.toString( ) ));

            if ( result ) break;
        }

        SvuidHelper.LOG.fine( "Class " + type.asType().toString() + (result ? " is" : " is not") + " serializable" );

        return result;
    }

    private static Collection<TypeElement> getAllParents( TypeElement ofType )
    {
        Set<TypeElement> result = new HashSet<>();

        for ( TypeMirror iface : ofType.getInterfaces() ) {
            TypeElement element = (TypeElement) ((DeclaredType) iface).asElement();

            if ( element != null ) {
                result.add( element );
                result.addAll( SvuidHelper.getAllParents( element ) );
            } else {
                SvuidHelper.LOG.log( Level.FINER, "element=null, iface={0}", iface );
            }
        }

        TypeMirror  parent  = ofType.getSuperclass();
        TypeElement element = (parent.getKind() == TypeKind.DECLARED) ? (TypeElement) ((DeclaredType) parent).asElement() : null;

        if ( element != null ) {
            result.add( element );
            result.addAll( SvuidHelper.getAllParents( element ) );
        } else {
            SvuidHelper.LOG.log(Level.FINER, "element=null, ofType={0}", ofType);
        }

        return result;
    }

    /**
     * check if contains SuppressWarnings with value serial
     *
     * @param typeElement
     * @return
     */
    private static boolean hasSuppressWarning( TypeElement type, String warning )
    {
        boolean             result      = false;
        SuppressWarnings    annotation  = type.getAnnotation( SuppressWarnings.class );

        if ( annotation != null ) {
            String[] values = annotation.value();

            result = Arrays.stream( values )
                           .anyMatch( value -> value.equals( warning ) );
        }

        if ( result ) {
            SvuidHelper.LOG.log( Level.FINE, "Class {0} contains SuppressWarnings(serial) annotation", type );
        } else {
            SvuidHelper.LOG.log( Level.FINE, "Class {0} does not contain SuppressWarnings(serial) annotation", type );
        }

        return result;
    }
}
