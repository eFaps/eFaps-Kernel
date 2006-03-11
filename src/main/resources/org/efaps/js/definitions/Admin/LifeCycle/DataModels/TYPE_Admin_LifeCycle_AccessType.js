/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_LifeCycle_AccessType
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
  with (addAttribute("OID"))  {
    setAttributeType("OID");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Name"))  {
    setAttributeType("String");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("NAME");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_LifeCycle_AccessTypeTable");
    setSQLColumn("MODIFIED");
  }
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=eFapsAdminLifeCycleAccessType");
  addProperty("Tree",                   "Admin_LifeCycle_AccessTypeTree");
}
