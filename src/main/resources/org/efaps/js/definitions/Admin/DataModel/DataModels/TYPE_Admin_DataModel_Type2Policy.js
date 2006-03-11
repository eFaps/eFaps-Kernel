/*******************************************************************************
* Description:
* ~~~~~~~~~~~~
*
* History:
* ~~~~~~~~
* Revision: $Rev$
* Date:     $Date: 2004-11-21 11:00:11 +0200 (Wed, 20 Oct 2004) $
* By:       $Author: tmo $
*
* Author:
* ~~~~~~~
* TMO
*******************************************************************************/

with (TYPE)  {
  with (addAttribute("OID"))  {
    setAttributeType("OID");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("MODIFIED");
  }
  with (addAttribute("TypeLink"))  {
    setAttributeType("Link");
    setTypeLink("Admin_DataModel_Type");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("DMTYPE");
  }
  with (addAttribute("PolicyLink"))  {
    setAttributeType("Link");
    setTypeLink("Admin_LifeCycle_Policy");
    setSQLTable("Admin_DataModel_Type2PolicyTable");
    setSQLColumn("LCPOLICY");
  }
}
