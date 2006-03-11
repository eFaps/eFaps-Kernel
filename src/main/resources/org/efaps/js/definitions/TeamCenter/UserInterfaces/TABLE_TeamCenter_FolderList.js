/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* TeamCenter_FolderList
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

with (TABLE)  {
  with (addField("name"))  {
    addProperty("Expression",                   "Name");
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                        "TeamCenter_Folder/Name.Label");
    addProperty("ShowTypeIcon",                 "true");
  }
  with (addField("buttonOpenInNewWindow"))  {
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                       "popup");
    addIcon("eFapsActionNewWindow");
  }
}
