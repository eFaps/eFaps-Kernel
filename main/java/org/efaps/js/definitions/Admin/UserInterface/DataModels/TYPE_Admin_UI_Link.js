/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Link
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
  setParentType(new Type("Admin_UI_Abstract2Abstract"));
  with (addAttribute("From"))  {
    setAttributeType("Link");
    setTypeLink("Admin_Abstract");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("FROMID");
  }
  with (addAttribute("To"))  {
    setAttributeType("Link");
    setTypeLink("Admin_UI_Direct");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("TOID");
  }
}
