/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_LifeCycle_Status
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
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Name"))  {
    setAttributeType("String");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("NAME");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("MODIFIED");
  }
  with (addAttribute("PolicyLink"))  {
    setAttributeType("Link");
    setTypeLink("Admin_LifeCycle_Policy");
    setSQLTable("Admin_LifeCycle_StatusTable");
    setSQLColumn("LCPOLICY");
  }
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=eFapsAdminLifeCycleStatus");
  addProperty("Tree",                   "Admin_LifeCycle_StatusTree");
}
