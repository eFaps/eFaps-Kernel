/*******************************************************************************
* Admin_UI_Menu:
* ~~~~~~~~~~~~~~
* TeamCenter_DocumentTree
*
* Description:
* ~~~~~~~~~~~~
*
* History:
* ~~~~~~~~
* Revision: $Rev$
* Date:     $Date$
* By:       $Author$
*
* Author:
* ~~~~~~~
* TMO
*******************************************************************************/

with (MENU)  {
  addProperty("Target",                       "content");
  addProperty("TargetMode",                   "view");
  addTargetForm("TeamCenter_DocumentForm");
  addTargetMenu("TeamCenter_DocumentTree_Menu");
//  addChild(new Command("TeamCenter_DocumentTree_Files"));
}
