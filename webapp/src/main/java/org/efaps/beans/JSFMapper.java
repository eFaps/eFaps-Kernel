/*
 * Copyright 2003-2007 The eFaps Team
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

    public static List getJSFNavigationMenuItems(final ResourceBundleBean _i18nBean,
                                                 final MenuHolder         _menuHolder) {
      List<NavigationMenuItem> jsfNavigationMenuItems = new Vector<NavigationMenuItem>();

      List<CommandHolder> menuElements = _menuHolder.getSubs();

      for (CommandHolder menuElement : menuElements) {
        jsfNavigationMenuItems.add(getJSFNavigationMenuItem(_i18nBean, menuElement));
      }

      return jsfNavigationMenuItems;
    }

    public static NavigationMenuItem getJSFNavigationMenuItem(final ResourceBundleBean _i18nBean,
                                                              final CommandHolder      _commandHolder) {
      CommandAbstract    command            = _commandHolder.getSub();

      // initalise the NavigationMenuItem instance with mapped Label and raw
      // action
      NavigationMenuItem navigationMenuItem =
              new NavigationMenuItem(_i18nBean.translate(command.getLabel()),
                                     String.valueOf(command.getAction()));
      // map the icon path
      if (_commandHolder.getIcon() != null)  {
        navigationMenuItem.setIcon("/.." + _commandHolder.getIcon());
      }

      // map the target url
      navigationMenuItem.setAction(getTargetURL(_commandHolder));

      if (_commandHolder.isMenu()) {
        // map the sub items
        navigationMenuItem.setNavigationMenuItems(
                getJSFNavigationMenuItems(_i18nBean, (MenuHolder) _commandHolder));
      }

      return navigationMenuItem;
    }

    public static String getTargetURL(final CommandHolder _commandHolder)  {
      StringBuilder url = new StringBuilder();

// TODO set submitUrl _commandHolder.getSub().isSubmit()
      // always javascript (needed for faces..)
      url.append("javascript:eFapsCommonOpenUrl(\"");

      // add link
      if ((_commandHolder.getSub().getReference() != null) &&
          (_commandHolder.getSub().getReference().length() > 0))  {
        url.append(_commandHolder.getSub().getReference());
      } else if ((_commandHolder.getSub().getTargetTable() != null) ||
                 (_commandHolder.getSub().getTargetForm() != null) ||
                 (_commandHolder.getSub().getTargetSearch() != null) ||
                 (_commandHolder.getSub().hasTrigger()))  {
        url.append("Link.jsp?");
// hack (no url found!)
} else  {
return null;
      }

      // append always the command name
      url.append("command=")
         .append(_commandHolder.getSub().getName());

// TODO append oid and/or nodeId

      // append target
      url.append("\",\""); 
      switch (_commandHolder.getSub().getTarget())  {
        case CommandAbstract.TARGET_CONTENT:
          url.append("Content");
          break;
        case CommandAbstract.TARGET_HIDDEN:
          url.append("eFapsFrameHidden");
          break;
        case CommandAbstract.TARGET_POPUP:
          url.append("popup");
          if ((_commandHolder.getSub().getWindowWidth() > 0) &&
             (_commandHolder.getSub().getWindowHeight() > 0))  {
            url.append("\",\"")
               .append(_commandHolder.getSub().getWindowWidth())
               .append("\",\"")
               .append(_commandHolder.getSub().getWindowHeight());
          }
          break;
        default:
          if (_commandHolder.getSub().hasTrigger())  {
            url.append("eFapsFrameHidden");
          } else  {
            url.append("Content");
          }
      }
      url.append("\")"); 

      return url.toString();
    }
}
