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

    private final static String[] TARGETS = {"","Content","popup","eFapsFrameHidden"};

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
        navigationMenuItem.setIcon("/.."+_commandHolder.getIcon());
      }

      // map the target url
      String sURL = getTargetURL(_commandHolder);
      if (sURL != null && sURL.length() > 0)  {
        if (!sURL.matches("^\\w{1,5}://.*"))  {
          sURL = (new StringBuilder("relative:")).append(sURL).toString();
        }
        navigationMenuItem.setAction(sURL);
      }

      String sTarget = getTargetTarget(_commandHolder);
      if (sTarget != null)  {
        navigationMenuItem.setTarget(sTarget);
      }

      if (_commandHolder.isMenu()) {
        // map the sub items
        navigationMenuItem.setNavigationMenuItems(
                getJSFNavigationMenuItems(_i18nBean, (MenuHolder) _commandHolder));
      } else  {
// TODO: what happens if it is not a menu?
      }

      return navigationMenuItem;
    }

    public static String getTargetURL(final CommandHolder _commandHolder)  {
        StringBuilder sbURL = new StringBuilder();
        String        sURI  = _commandHolder.getSub().getReference();

      if (sURI == null &&
              (_commandHolder.getSub().getTargetTable()  != null ||
               _commandHolder.getSub().getTargetForm()   != null ||
               _commandHolder.getSub().getTargetSearch() != null))  {

        sURI = "Link.jsp?";
      }
      if (sURI == null && _commandHolder.getSub().isSubmit())  {
        // TODO set submitUrl
//            sbURL = new StringBuilder();
      }
      if (sURI != null)  {
          // TODO append oid and/or nodeId

          // add command name string to url
          sbURL.append("&command=").append(_commandHolder.getSub().getName());

          sbURL.deleteCharAt(0)
               .insert(0, sURI);
      }

      return sbURL.toString();
    }

    public static String getTargetTarget(final CommandHolder _commandHolder)  {
      int iTargetId = _commandHolder.getSub().getTarget();

      String sTarget = TARGETS[iTargetId];
      if ("popup".equals(sTarget))  {
        int iHeight = _commandHolder.getSub().getWindowHeight();
        int iWidth  = _commandHolder.getSub().getWindowWidth();

        StringBuilder sbTarget = new StringBuilder(sTarget);
        sbTarget.append(iHeight).append("x").append(iWidth);

        sTarget = sbTarget.toString();
      }

      return sTarget;
    }
}
