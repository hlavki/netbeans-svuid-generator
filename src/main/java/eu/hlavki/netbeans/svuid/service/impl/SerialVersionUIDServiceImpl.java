package eu.hlavki.netbeans.svuid.service.impl;

import eu.hlavki.netbeans.svuid.ClassInfo;
import eu.hlavki.netbeans.svuid.Descriptor;
import eu.hlavki.netbeans.svuid.FieldInfo;
import eu.hlavki.netbeans.svuid.MethodInfo;
import eu.hlavki.netbeans.svuid.service.SerialVersionUIDService;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import static javax.lang.model.util.ElementFilter.*;
import javax.lang.model.type.TypeMirror;
import org.openide.util.Exceptions;

/**
 * See: http://java.sun.com/javase/6/docs/platform/serialization/spec/class.html See: ObjectStreamClass
 *
 */
public class SerialVersionUIDServiceImpl implements SerialVersionUIDService {

    private static final Logger log = Logger.getLogger(SerialVersionUIDServiceImpl.class.getName());

    @Override
    public long generate(TypeElement el) {
        long result = 0L;
        ByteArrayOutputStream bout;
        DataOutputStream out = null;
        try {
            bout = new ByteArrayOutputStream();
            out = new DataOutputStream(bout);

            // 1. write class name
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "CLASS: {0}", el.asType().toString());
            }
            ClassInfo clazzInfo = new ClassInfo(el);
            out.writeUTF(clazzInfo.getName());

            // 2. write class access flag
            out.writeInt(clazzInfo.getSvuidAccess());

            // 3. write ordered interfaces
            List<String> interfaces = getInterfaces(el.getInterfaces());
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "INTERFACES: {0}", interfaces);
            }
            for (String interfejz : interfaces) {
                out.writeUTF(interfejz);
            }

            /*
             * 4. For each field of the class sorted by field name (except
             * private static and private transient fields):
             *
             * 1. The name of the field in UTF encoding. 2. The modifiers of the
             * field written as a 32-bit integer. 3. The descriptor of the field
             * in UTF encoding
             *
             * Note that field signatutes are not dot separated. Method and
             * constructor signatures are dot separated. Go figure...
             */
            List<? extends Element> elements = el.getEnclosedElements();
            List<FieldInfo> fields = getFields(elements);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "FIELDS: {0}", fields);
            }
            for (FieldInfo field : fields) {
                out.writeUTF(field.getName());
                out.writeInt(field.getSvuidAccess());
                out.writeUTF(field.getDescriptor());
            }

            /*
             * 5. If a class initializer exists, write out the following: 1. The
             * name of the method, <clinit>, in UTF encoding. 2. The modifier of
             * the method, java.lang.reflect.Modifier.STATIC, written as a
             * 32-bit integer. 3. The descriptor of the method, ()V, in UTF
             * encoding.
             */
            boolean staticInit = hasStaticInit(elements);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Class has {0}static init!", (staticInit ? "" : "not "));
            }
            if (staticInit) {
                out.writeUTF("<clinit>");
                out.writeInt(Modifier.STATIC);
                out.writeUTF("()V");
            }

            /*
             * 6. For each non-private constructor sorted by method name and
             * signature: 1. The name of the method, <init>, in UTF encoding. 2.
             * The modifiers of the method written as a 32-bit integer. 3. The
             * descriptor of the method in UTF encoding.
             */
            List<MethodInfo> constructors = getConstructors(elements);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "CONSTRUCTORS: {0}", constructors);
            }
            for (MethodInfo constructor : constructors) {
                out.writeUTF(constructor.getName());
                out.writeInt(constructor.getSvuidAccess());
                out.writeUTF(constructor.getDescriptor().replace('/', '.'));
            }

            /*
             * 7. For each non-private method sorted by method name and
             * signature: 1. The name of the method in UTF encoding. 2. The
             * modifiers of the method written as a 32-bit integer. 3. The
             * descriptor of the method in UTF encoding.
             */
            List<MethodInfo> methods = getMethods(elements);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "METHODS: {0}", methods);
            }
            for (MethodInfo method : methods) {
                out.writeUTF(method.getName());
                out.writeInt(method.getSvuidAccess());
                out.writeUTF(method.getDescriptor().replace('/', '.'));
            }

            out.flush();

            /*
             * 8. The SHA-1 algorithm is executed on the stream of bytes
             * produced by DataOutputStream and produces five 32-bit values
             * sha[0..4].
             */
            byte[] hashBytes = MessageDigest.getInstance("SHA").digest(bout.toByteArray());

            /*
             * 9. The hash value is assembled from the first and second 32-bit
             * values of the SHA-1 message digest. If the result of the message
             * digest, the five 32-bit words H0 H1 H2 H3 H4, is in an array of
             * five int values named sha, the hash value would be computed as
             * follows:
             *
             * long hash = ((sha[0] >>> 24) & 0xFF) | ((sha[0] >>> 16) & 0xFF) <<
             * 8 | ((sha[0] >>> 8) & 0xFF) << 16 | ((sha[0] >>> 0) & 0xFF) <<
             * 24 | ((sha[1] >>> 24) & 0xFF) << 32 | ((sha[1] >>> 16) & 0xFF) <<
             * 40 | ((sha[1] >>> 8) & 0xFF) << 48 | ((sha[1] >>> 0) & 0xFF) <<
             * 56;
             */
            for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
                result = (result << 8) | (hashBytes[i] & 0xFF);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            Exceptions.printStackTrace(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        return result;
    }

    private List<String> getInterfaces(List<? extends TypeMirror> interfaces) {
        List<String> result = new ArrayList<>();
        for (TypeMirror type : interfaces) {
            result.add(stripGenerics(type.toString()));
        }
        Collections.sort(result);
        return result;
    }

    private List<FieldInfo> getFields(List<? extends Element> elements) {
        List<FieldInfo> result = new ArrayList<>();
        for (VariableElement elem : fieldsIn(elements)) {
            FieldInfo fieldInfo = new FieldInfo(elem.getSimpleName(), elem.getModifiers(), Descriptor.of(elem.asType()));
            if (fieldInfo.includeInSerialVersionUID()) {
                result.add(fieldInfo);
            }
        }
        Collections.sort(result);
        return result;
    }

    private boolean hasStaticInit(List<? extends Element> elements) {
        for (Element e : elements) {
            return STATIC_INIT.equals(e.getKind());
        }
        return false;
    }

    private List<MethodInfo> getConstructors(List<? extends Element> elements) {
        List<MethodInfo> result = new ArrayList<>();
        for (ExecutableElement elem : constructorsIn(elements)) {
            MethodInfo info = new MethodInfo(elem.getSimpleName(), elem.getModifiers(), Descriptor.of(elem.asType()));
            if (info.includeInSerialVersionUID()) {
                result.add(info);
            }
        }
        Collections.sort(result);
        return result;
    }

    private List<MethodInfo> getMethods(List<? extends Element> elements) {
        List<MethodInfo> result = new ArrayList<>();
        for (ExecutableElement elem : methodsIn(elements)) {
            MethodInfo info = new MethodInfo(elem.getSimpleName(), elem.getModifiers(), Descriptor.of(elem.asType()));
            if (info.includeInSerialVersionUID()) {
                result.add(info);
            }
        }
        Collections.sort(result);
        return result;
    }

    private String stripGenerics(String interfejzName) {
        int end = interfejzName.indexOf('<');
        end = end == -1 ? interfejzName.length() : end;
        return interfejzName.substring(0, end);
    }
}
