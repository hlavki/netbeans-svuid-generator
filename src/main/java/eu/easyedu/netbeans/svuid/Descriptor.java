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

import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

/**
 *
 * @author hlavki &lt;iso@hlavki.eu&gt;
 */
public class Descriptor
{
    private static final Logger LOG = Logger.getLogger( Descriptor.class.getName() );

    /**
     * Converts a class name into the internal representation used in the JVM.
     *
     * <p>
     * Note that <code>toJvmName(toJvmName(s))</code> is equivalent to <code>toJvmName(s)</code>.
     */
    private static String toJvmName(String classname)
    {
        return classname.replace('.', '/');
    }

    /**
     * Converts a class name into the internal representation used in the JVM.
     *
     * <p>
     * Note that <code>toJvmName(toJvmName(s))</code> is equivalent to <code>toJvmName(s)</code>.
     */
    private static StringBuffer toJvmName( StringBuffer sb, Name clazzName )
    {
        for (int idx = 0 ; idx < clazzName.length() ; idx++ ) {
            char ch = clazzName.charAt( idx );

            sb.append( (ch == '.') ? '/' : ch );
        }

        return sb;
    }

    public static String of( TypeMirror type )
    {
        StringBuffer sb = new StringBuffer();

        Descriptor.descriptor( sb, type );

        return sb.toString();
    }

    private static void descriptor( StringBuffer sb, TypeMirror type )
    {
        switch ( type.getKind() ) {
            case INT:       sb.append( 'I' );   break;
            case BYTE:      sb.append( 'B' );   break;
            case LONG:      sb.append( 'J' );   break;
            case DOUBLE:    sb.append( 'D' );   break;
            case FLOAT:     sb.append( 'F' );   break;
            case CHAR:      sb.append( 'C' );   break;
            case SHORT:     sb.append( 'S' );   break;
            case BOOLEAN:   sb.append( 'Z' );   break;
            case VOID:      sb.append( 'V' );   break;
            case ARRAY:
                sb.append( "[" );
                Descriptor.descriptor( sb, ((ArrayType) type).getComponentType() );
                break;
            case DECLARED:
                DeclaredType    declaredType    = (DeclaredType) type;
                TypeElement     typeElement     = (TypeElement) declaredType.asElement();

                sb.append('L');
                Descriptor.toJvmName( sb, typeElement.getQualifiedName() );
                sb.append(';');
                break;
            case TYPEVAR:
                TypeVariable typeVar = (TypeVariable) type;

                sb.append('T').append( typeVar.asElement().getSimpleName() ).append( ';');

                break;
            case EXECUTABLE:
                ExecutableType              execType    = (ExecutableType) type;
                List<? extends TypeMirror>  paramTypes  = execType.getParameterTypes();

                sb.append('(');
                for ( TypeMirror paramType : paramTypes ) {
                    Descriptor.descriptor( sb, paramType );
                }
                sb.append(')');

                Descriptor.descriptor( sb, execType.getReturnType() );

                break;
            default:
                Descriptor.LOG.severe( "UNKNOWN TYPE: " + type.getKind() );
        }
    }
}
