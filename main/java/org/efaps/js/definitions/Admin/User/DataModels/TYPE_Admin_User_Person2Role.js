/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_User_Person2Role
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
  setParentType(new Type("Admin_User_Abstract2Abstract"));
  with (addAttribute("UserFromLink"))  {
    setAttributeType("Link");
    setTypeLink("Admin_User_Person");
    setSQLTable("Admin_User_Abstract2AbstractTable");
    setSQLColumn("USERABSTRACTFROM");
  }
  with (addAttribute("UserToLink"))  {
    setAttributeType("Link");
    setTypeLink("Admin_User_Role");
    setSQLTable("Admin_User_Abstract2AbstractTable");
    setSQLColumn("USERABSTRACTTO");
  }
}
