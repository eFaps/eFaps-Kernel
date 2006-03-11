/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_User_PersonList
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
    addProperty("Expression",             "Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_User_Person/Name.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("lastname"))  {
    addProperty("Expression",             "LastName");
    addProperty("Label",                  "Admin_User_Person/LastName.Label");
  }
  with (addField("firstname"))  {
    addProperty("Expression",             "FirstName");
    addProperty("Label",                  "Admin_User_Person/FirstName.Label");
  }
  with (addField("organisation"))  {
    addProperty("Expression",             "Organisation");
    addProperty("Label",                  "Admin_User_Person/Organisation.Label");
  }
  with (addField("email"))  {
    addProperty("Expression",             "Email");
    addProperty("Label",                  "Admin_User_Person/Email.Label");
  }
  with (addField("phone"))  {
    addProperty("Expression",             "Phone");
    addProperty("Label",                  "Admin_User_Person/Phone.Label");
  }
  with (addField("mobile"))  {
    addProperty("Expression",             "Mobile");
    addProperty("Label",                  "Admin_User_Person/Mobile.Label");
  }
  with (addField("buttonOpenInNewWindow"))  {
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}
