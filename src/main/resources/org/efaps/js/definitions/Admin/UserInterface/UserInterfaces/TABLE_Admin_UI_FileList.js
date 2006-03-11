/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_UI_FileList
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
    addProperty("Expression",             "Type");
    addProperty("Label",                  "Admin_UI_Abstract/Type.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("name"))  {
    addProperty("Expression",             "Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_UI_Abstract/Name.Label");
  }
  with (addField("revision"))  {
    addProperty("Expression",             "Revision");
    addProperty("Label",                  "Admin_UI_Abstract/Revision.Label");
  }
  with (addField("fileName"))  {
    addProperty("Expression",             "FileName");
    addProperty("Label",                  "Admin_UI_File/FileName.Label");
  }
  with (addField("fileLength"))  {
    addProperty("Expression",             "FileLength");
    addProperty("Label",                  "Admin_UI_File/FileLength.Label");
  }
  with (addField("checkout"))  {
    addProperty("HRef",                   "${ROOTURL}/servlet/checkout");
    addProperty("Target",                 "popup");
    addIcon("Action_View");
  }
  with (addField("openInNewWindow"))  {
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}
