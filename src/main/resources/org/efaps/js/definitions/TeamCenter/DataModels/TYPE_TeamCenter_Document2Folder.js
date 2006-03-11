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
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("ID");
  }
  with (addAttribute("ID"))  {
    setAttributeType("Integer");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("ID");
  }
  with (addAttribute("Creator"))  {
    setAttributeType("CreatorLink");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("CREATOR");
  }
  with (addAttribute("Created"))  {
    setAttributeType("Created");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("CREATED");
  }
  with (addAttribute("Modifier"))  {
    setAttributeType("ModifierLink");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("MODIFIER");
  }
  with (addAttribute("Modified"))  {
    setAttributeType("Modified");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("MODIFIED");
  }
  with (addAttribute("Document"))  {
    setAttributeType("Link");
    setTypeLink("TeamCenter_Document");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("TCDOCUMENT");
  }
  with (addAttribute("Folder"))  {
    setAttributeType("Link");
    setTypeLink("TeamCenter_Folder");
    setSQLTable("TeamCenter_Document2FolderSQLTable");
    setSQLColumn("TCFOLDER");
  }
}
