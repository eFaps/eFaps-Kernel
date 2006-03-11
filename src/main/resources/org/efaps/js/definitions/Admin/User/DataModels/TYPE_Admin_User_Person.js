/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_User_Person
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

with (TYPE)  {
  setParentType(new Type("Admin_User_Abstract"));
  with (addAttribute("FirstName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("FIRSTNAME");
  }
  with (addAttribute("LastName"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("LASTNAME");
  }
  with (addAttribute("Email"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("EMAIL");
  }
  with (addAttribute("Organisation"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("ORG");
  }
  with (addAttribute("Url"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("URL");
  }
  with (addAttribute("Phone"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("PHONE");
  }
  with (addAttribute("Mobile"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("MOBILE");
  }
  with (addAttribute("Fax"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("FAX");
  }
  with (addAttribute("Password"))  {
    setAttributeType("Password");
    setSQLTable("Admin_User_PersonTable");
    setSQLColumn("PASSWORD");
  }
  addProperty("Form",                   "Admin_User_PersonForm");
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=eFapsAdminUserPerson");
  addProperty("Tree",                   "Admin_User_PersonTree");
}
