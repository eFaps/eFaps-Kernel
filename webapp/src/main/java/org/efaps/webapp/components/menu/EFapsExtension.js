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
 * Author:          tmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


//function _cmItemMouseUp = cmItemMouseUp;


//
// action should be taken for mouse button up at a menu item
//
function cmItemMouseUp (obj, isMain, idSub, menuID, index)
{

  if (!_cmItemList[index].isDisabled)  {

    var item = _cmItemList[index];

    if (item.length > 2)  {
      var link = item[2];
      if (item.length > 3 && item[3]){
        target = item[3];
      }
      window.open (link, target);
      var menuInfo = _cmMenuList[menuID];
      var prefix = menuInfo.prefix;
      var thisMenu = cmGetThisMenu (obj, prefix);
      cmHideMenu (thisMenu, null, menuInfo);
    }
	}
}

