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
