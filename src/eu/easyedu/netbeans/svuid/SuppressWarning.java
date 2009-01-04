/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.easyedu.netbeans.svuid;

/**
 *
 * @author hlavki
 */
public enum SuppressWarning {

    ALL("all"),
    BOXING("boxing"),
    CAST("cast"),
    DEP_ANN("dep-ann"),
    DEPRECATION("deprecation"),
    FALLTHROUGH("fallthrough"),
    FINALLY("finally"),
    HIDING("hiding"),
    INCOMPLETE_SWITCH("incomplete-switch"),
    NLS("nls"),
    NULL("null"),
    RESTRICTION("restriction"),
    SERIAL("serial"),
    STATIC_ACCESS("static-access"),
    SYNTHETIC_ACCESS("synthetic-access"),
    UNCHECKED("unchecked"),
    UNQUALIFIED_FIELD_ACCESS("unqualified-field-access"),
    UNUSED("unused");
    private final String code;

    private SuppressWarning(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
