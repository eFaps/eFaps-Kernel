/*******************************************************************************
* Admin_UI_Form:
* ~~~~~~~~~~~~~~
* Admin_PropertyForm
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
  with (addField("name"))  {
    addProperty("Columns",                "40");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Name");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
  with (addField("value"))  {
    addProperty("Columns",                "80");
    addProperty("Creatable",              "true");
    addProperty("Editable",               "true");
    addProperty("Expression",             "Value");
    addProperty("Required",               "true");
    addProperty("Searchable",             "true");
  }
}
