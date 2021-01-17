/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orbaker.svuidgen;

import java.lang.reflect.Modifier;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author hlavki
 */
public class ClassInfo extends ElementInfo
{
    public ClassInfo( TypeElement element )
    {
        super( element.getQualifiedName(), null );

        this.access = this.getAccessFlag( element.getModifiers() ) | this.getInitialAccessFlag( element );
    }

    private int getInitialAccessFlag( TypeElement element )
    {
        int accessFlag = 0;

        if ( element.getKind().equals( ElementKind.INTERFACE ) ) {
            accessFlag = Modifier.INTERFACE | Modifier.ABSTRACT;
        }

        return accessFlag;
    }

    @Override
    public int getSvuidAccess()
    {
        int modifier = this.access & (Modifier.PUBLIC | Modifier.FINAL | Modifier.INTERFACE | Modifier.ABSTRACT);

        return modifier;
    }

    @Override
    public String getSortingName()
    {
        return this.name + this.descriptor;
    }
}
