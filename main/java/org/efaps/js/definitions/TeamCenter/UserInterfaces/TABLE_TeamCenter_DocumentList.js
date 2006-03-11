/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* TeamCenter_DocumentList
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
  with (addField("type"))  {
    addProperty("Expression",                   "Type");
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                        "TeamCenter_Document/Type.Label");
    addProperty("ShowTypeIcon",                 "true");
  }
  with (addField("name"))  {
    addProperty("Expression",                   "Name");
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                        "TeamCenter_Document/Name.Label");
  }
  with (addField("revision"))  {
    addProperty("Expression",                   "Revision");
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                        "TeamCenter_Document/Revision.Label");
  }
  with (addField("fileName"))  {
    addProperty("Expression",                   "FileName");
    addProperty("Label",                        "TeamCenter_Document/FileName.Label");
  }
  with (addField("fileLength"))  {
    addProperty("Expression",                   "FileLength");
    addProperty("Label",                        "TeamCenter_Document/FileLength.Label");
  }
  with (addField("checkout"))  {
    addProperty("HRef",                         "${ROOTURL}/servlet/checkout");
    addProperty("Target",                       "popup");
    addIcon("Action_View");
  }
  with (addField("buttonOpenInNewWindow"))  {
    addProperty("HRef",                         "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                       "popup");
    addIcon("eFapsActionNewWindow");
  }
}
