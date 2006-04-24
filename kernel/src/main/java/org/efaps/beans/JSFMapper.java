/*
 * Copyright 2006 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.beans;

import java.util.List;
import java.util.Vector;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.beans.MenuAbstractBean.MenuHolder;
import org.efaps.beans.MenuAbstractBean.CommandHolder;

public class JSFMapper {

    public static List getJSFNavigationMenuItems(final MenuHolder _menuHolder) {
        List<NavigationMenuItem> jsfNavigationMenuItems = new Vector<NavigationMenuItem>();
        
        List<CommandHolder> menuElements = _menuHolder.getSubs();

        for (CommandHolder menuElement : menuElements) {
            jsfNavigationMenuItems.add(getJSFNavigationMenuItem(menuElement));
        }

        return jsfNavigationMenuItems;
    }

    public static NavigationMenuItem getJSFNavigationMenuItem(final CommandHolder _commandHolder) {
        CommandAbstract    command            = _commandHolder.getSub();
        NavigationMenuItem navigationMenuItem = new NavigationMenuItem(command.getLabel(),
                                                                       String.valueOf(command.getAction()));

        if (_commandHolder.getIcon() != null)  {
            navigationMenuItem.setIcon("/.."+_commandHolder.getIcon());
        }
        
        String sURL = getTargetURL(_commandHolder);
        if (sURL != null && sURL.length() > 0)  {
            if (!sURL.matches("^\\w{1,5}://.*"))
            {
                sURL = (new StringBuilder("relative:")).append(sURL).toString();
            }
            navigationMenuItem.setAction(sURL);
        }

//        String sTarget = getTargetTarget(_commandHolder);
        String sTarget = "Content";
        if (sTarget != null)  {
            navigationMenuItem.setTarget(sTarget);
        }

        if (_commandHolder.isMenu()) {
            navigationMenuItem.setNavigationMenuItems(getJSFNavigationMenuItems((MenuHolder) _commandHolder));
        }
        else {
        }

        return navigationMenuItem;
    }

    public static String getTargetURL(final CommandHolder _commandHolder)
    {
        StringBuilder sbURL = new StringBuilder();
        String        sURI  = _commandHolder.getSub().getReference();

        if (sURI == null && 
                (_commandHolder.getSub().getTargetTable()      != null || 
                 _commandHolder.getSub().getTargetForm()       != null ||
                 _commandHolder.getSub().getTargetCreateType() != null))
        {
            sURI = "Link.jsp?";
        }
        if (sURI == null && _commandHolder.getSub().isSubmit())
        {
        // TODO set submitUrl
//            sbURL = new StringBuilder();
        }
        if (sURI != null)
        {
            // TODO append oid and/or nodeId 

            // add command name string to url
            sbURL.append("&command=").append(_commandHolder.getSub().getName());

            sbURL.deleteCharAt(0)
                 .insert(0, sURI);
        }

        return sbURL.toString();
    }
}
