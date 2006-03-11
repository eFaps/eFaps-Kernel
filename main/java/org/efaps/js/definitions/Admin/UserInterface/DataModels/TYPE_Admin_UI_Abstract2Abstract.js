/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_Abstract2Abstract
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
  with (addAttribute("Type"))  {
    setAttributeType("Type");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("TYPEID");
  }
  with (addAttribute("OID"))  {
    setAttributeType("OID");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("TYPEID,ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_UI_Abstract2AbstractTable");
    setSQLColumn("MODIFIED");
  }
}
