/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Menu2Command
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
  with (addAttribute("FromMenu"))  {
    setAttributeType("Link");
    setTypeLink("Admin_UI_Menu");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("FROMID");
  }
  with (addAttribute("ToCommand"))  {
    setAttributeType("Link");
    setTypeLink("Admin_UI_Command");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("TOID");
  }
}
