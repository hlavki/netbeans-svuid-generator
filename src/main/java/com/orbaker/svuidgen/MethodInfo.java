/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orbaker.svuidgen;

import java.lang.reflect.Modifier;
import java.util.Set;
import javax.lang.model.element.Name;

/**
 *
 * @author hlavki
 */
public class MethodInfo extends ElementInfo
{
    public MethodInfo( Name name, Set<javax.lang.model.element.Modifier> modifiers, String descriptor )
    {
        super( name, modifiers, descriptor );
    }

    @Override
    public boolean includeInSerialVersionUID()
    {
        return (this.access & Modifier.PRIVATE) == 0;
    }

    @Override
    public int getSvuidAccess()
    {
        return this.access;
    }

    @Override
    public String getSortingName()
    {
        return this.name + this.descriptor;
    }
}
