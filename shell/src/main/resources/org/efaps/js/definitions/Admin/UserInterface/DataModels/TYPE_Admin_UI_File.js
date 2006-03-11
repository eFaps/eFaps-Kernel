/*******************************************************************************
* Admin_DataModel_Type:
* ~~~~~~~~~~~~~~~~~~~~~
* Admin_UI_File
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
  setParentType(new Type("Admin_UI_Direct"));
  with (addAttribute("FileName"))  {
    setAttributeType("String");
    setSQLTable("Admin_UI_FileTable");
    setSQLColumn("FILENAME");
  }
  with (addAttribute("FileLength"))  {
    setAttributeType("Long");
    setSQLTable("Admin_UI_FileTable");
    setSQLColumn("FILELENGTH");
  }
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=Admin_UI_FileImage");
  addProperty("Tree",                   "Admin_UI_FileTree");
  addProperty("VFSFileLengthAttribute", "FileLength");
  addProperty("VFSFileNameAttribute",   "FileName");
  addProperty("VFSPrefix",              "UIFILE:ID:FILECONTENT");
  addProperty("VFSProvider",            "org.efaps.db.vfs.provider.sqldatabase.SQLDataBaseFileProvider");
}
