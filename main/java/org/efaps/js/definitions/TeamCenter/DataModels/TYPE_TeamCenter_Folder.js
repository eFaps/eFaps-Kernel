/*******************************************************************************
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
  setParentType(new Type("Admin_Abstract"));
  with (addAttribute("ParentFolder"))  {
    setAttributeType("Link");
    setTypeLink("TeamCenter_Folder");
    setSQLTable("TeamCenter_FolderSQLTable");
    setSQLColumn("PARENTFOLDER");
  }
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=TeamCenter_FolderImage");
  addProperty("Tree",                   "TeamCenter_FolderTree");
}
