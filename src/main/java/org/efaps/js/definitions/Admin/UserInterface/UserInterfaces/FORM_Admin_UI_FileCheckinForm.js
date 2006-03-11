/*******************************************************************************
* Admin_UI_Form:
* ~~~~~~~~~~~~~~
* Admin_UI_FileCheckinForm
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

with (FORM)  {
  with (addField("type"))  {
    addProperty("Editable",               "false");
    addProperty("Expression",             "Type");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("name"))  {
    addProperty("Editable",               "false");
    addProperty("Expression",             "Name");
  }
  with (addField("revision"))  {
    addProperty("Editable",               "false");
    addProperty("Expression",             "Revision");
  }
  with (addField("file"))  {
    addProperty("ClassNameUI",            "org.efaps.admin.datamodel.ui.FileUI");
    addProperty("Editable",               "true");
    addProperty("Label",                  "Admin_UI_File/File.Label");
  }
}
