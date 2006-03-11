/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_User_Abstract
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
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("TYPEID");
  }
  with (addAttribute("OID"))  {
    setAttributeType("OID");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("TYPEID,ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Name"))  {
    setAttributeType("String");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("NAME");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_User_AbstractTable");
    setSQLColumn("MODIFIED");
  }
}
