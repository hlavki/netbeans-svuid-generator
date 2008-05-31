/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

import javax.lang.model.element.Modifier;

/**
 *
 * @author hlavki
 */
public class OpCodes {

    public static final int ACC_PUBLIC = 0x0001; // class, field, method

    public static final int ACC_PRIVATE = 0x0002; // class, field, method

    public static final int ACC_PROTECTED = 0x0004; // class, field, method

    public static final int ACC_STATIC = 0x0008; // field, method

    public static final int ACC_FINAL = 0x0010; // class, field, method

    public static final int ACC_SUPER = 0x0020; // class

    public static final int ACC_SYNCHRONIZED = 0x0020; // method

    public static final int ACC_VOLATILE = 0x0040; // field

    public static final int ACC_BRIDGE = 0x0040; // method

    public static final int ACC_VARARGS = 0x0080; // method

    public static final int ACC_TRANSIENT = 0x0080; // field

    public static final int ACC_NATIVE = 0x0100; // method

    public static final int ACC_INTERFACE = 0x0200; // class

    public static final int ACC_ABSTRACT = 0x0400; // class, method

    public static final int ACC_STRICT = 0x0800; // method

    public static final int ACC_SYNTHETIC = 0x1000; // class, field, method

    public static final int ACC_ANNOTATION = 0x2000; // class

    public static final int ACC_ENUM = 0x4000; // class(?) field inner


    public static int fromModifier(Modifier modifier) {
        int result = 0;
        switch (modifier) {
            case ABSTRACT:
                result = OpCodes.ACC_ABSTRACT;
                break;
            case FINAL:
                result = OpCodes.ACC_FINAL;
                break;
            case NATIVE:
                result = OpCodes.ACC_NATIVE;
                break;
            case PRIVATE:
                result = OpCodes.ACC_PRIVATE;
                break;
            case PROTECTED:
                result = OpCodes.ACC_PROTECTED;
                break;
            case PUBLIC:
                result = OpCodes.ACC_PUBLIC;
                break;
            case STATIC:
                result = OpCodes.ACC_STATIC;
                break;
            case STRICTFP:
                result = OpCodes.ACC_STRICT;
                break;
            case SYNCHRONIZED:
                result = OpCodes.ACC_SYNCHRONIZED;
                break;
            case TRANSIENT:
                result = OpCodes.ACC_TRANSIENT;
                break;
            case VOLATILE:
                result = OpCodes.ACC_VOLATILE;
                break;
        }
        return result;
    }
}
