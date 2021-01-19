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

import java.lang.reflect.Modifier;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author hlavki &lt;iso@hlavki.eu&gt;
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
