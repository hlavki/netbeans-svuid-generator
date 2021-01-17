/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orbaker.svuidgen;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 *
 * @author hlavki
 */
public abstract class ElementInfo implements Comparable<ElementInfo>
{
    protected String    name;
    protected int       access;
    protected String    descriptor;

    public ElementInfo( Name name, Set<Modifier> modifiers, String descriptor )
    {
        this.name       = new StringBuilder( name ).toString();
        this.access     = this.getAccessFlag( modifiers );
        this.descriptor = descriptor;
    }

    public ElementInfo( Name name, String description )
    {
        this( name, Collections.<Modifier>emptySet(), description );
    }

    public int getAccess()
    {
        return this.access;
    }

    public String getDescriptor()
    {
        return this.descriptor;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean includeInSerialVersionUID()
    {
        return true;
    }

    protected final int getAccessFlag( Set<Modifier> modifiers )
    {
        return modifiers.stream()
                        .map( modifier -> this.toModifier( modifier ) )
                        .reduce( 0, (a,b) -> a | b );
    }

    @Override
    public String toString()
    {
        return this.name + "|" + this.descriptor + "|" + getSvuidAccess();
    }

    @Override
    public int compareTo( ElementInfo other )
    {
        if ( other == null ) return 1;

        return this.getSortingName().compareTo( other.getSortingName() );
    }

    private int toModifier( Modifier modifier )
    {
        switch ( modifier ) {
            case ABSTRACT:     return java.lang.reflect.Modifier.ABSTRACT;
            case FINAL:        return java.lang.reflect.Modifier.FINAL;
            case NATIVE:       return java.lang.reflect.Modifier.NATIVE;
            case PRIVATE:      return java.lang.reflect.Modifier.PRIVATE;
            case PROTECTED:    return java.lang.reflect.Modifier.PROTECTED;
            case PUBLIC:       return java.lang.reflect.Modifier.PUBLIC;
            case STATIC:       return java.lang.reflect.Modifier.STATIC;
            case STRICTFP:     return java.lang.reflect.Modifier.STRICT;
            case SYNCHRONIZED: return java.lang.reflect.Modifier.SYNCHRONIZED;
            case TRANSIENT:    return java.lang.reflect.Modifier.TRANSIENT;
            case VOLATILE:     return java.lang.reflect.Modifier.VOLATILE;
        }

        return 0;
    }

    public abstract int getSvuidAccess();
    public abstract String getSortingName();
}
