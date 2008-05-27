/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import com.sun.source.tree.Tree;
import java.util.List;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

/**
 *
 * @author hlavki
 */
public class Descriptor {

    /**
     * Converts a class name into the internal representation used in
     * the JVM.
     *
     * <p>Note that <code>toJvmName(toJvmName(s))</code> is equivalent
     * to <code>toJvmName(s)</code>.
     */
    public static String toJvmName(String classname) {
        return classname.replace('.', '/');
    }

    /**
     * Converts a class name into the internal representation used in
     * the JVM.
     *
     * <p>Note that <code>toJvmName(toJvmName(s))</code> is equivalent
     * to <code>toJvmName(s)</code>.
     */
    public static StringBuffer toJvmName(StringBuffer sb, Name clazzName) {
        for (int idx = 0; idx < clazzName.length(); idx++) {
            char ch = clazzName.charAt(idx);
            sb.append(ch == '.' ? '/' : ch);
        }
        return sb;
    }

    /**
     * Converts a class name from the internal representation used in
     * the JVM to the normal one used in Java.
     */
    public static String toJavaName(String classname) {
        return classname.replace('/', '.');
    }

    public static final String of(Tree type) {
        StringBuffer sb = new StringBuffer();
//        descriptor(sb, type);
        return sb.toString();
    }

    public static final String of(TypeMirror type) {
        StringBuffer sb = new StringBuffer();
        descriptor(sb, type);
        return sb.toString();
    }

    private static final void descriptor(StringBuffer sb, TypeMirror type) {
        switch (type.getKind()) {
            case ARRAY:
                sb.append("[");
                descriptor(sb, ((ArrayType) type).getComponentType());
                break;
            case DECLARED:
                DeclaredType declaredType = (DeclaredType) type;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                sb.append('L');
                toJvmName(sb, typeElement.getQualifiedName());
                List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                if (!typeArgs.isEmpty()) {
                    sb.append('<');
                    for (TypeMirror typeArg : typeArgs) {
                        descriptor(sb, typeArg);
                    }
                    sb.append('>');
                }
                sb.append(';');
                break;
            case TYPEVAR:
                TypeVariable typeVar = (TypeVariable) type;
                sb.append('T').append(typeVar.asElement().getSimpleName()).append(';');
                break;
            case INT:
                sb.append('I');
                break;
            case BYTE:
                sb.append('B');
                break;
            case LONG:
                sb.append('J');
                break;
            case DOUBLE:
                sb.append('D');
                break;
            case FLOAT:
                sb.append('F');
                break;
            case CHAR:
                sb.append('C');
                break;
            case SHORT:
                sb.append('S');
                break;
            case BOOLEAN:
                sb.append('Z');
                break;
            case VOID:
                sb.append('V');
                break;
            case EXECUTABLE:
                sb.append('(');
                ExecutableType execType = (ExecutableType) type;
                List<? extends TypeMirror> paramTypes = execType.getParameterTypes();
                for (TypeMirror paramType : paramTypes) {
                    descriptor(sb, paramType);
                }
                sb.append(')');
                descriptor(sb, execType.getReturnType());
                break;
            default:
                System.out.println("UNKNOWN: " + type.getKind());
        }
    }
}
