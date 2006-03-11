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
  with (addAttribute("FileName"))  {
    setAttributeType("String");
    setSQLTable("TeamCenter_DocumentSQLTable");
    setSQLColumn("FILENAME");
  }
  with (addAttribute("FileLength"))  {
    setAttributeType("Long");
    setSQLTable("TeamCenter_DocumentSQLTable");
    setSQLColumn("FILELENGTH");
  }
  addProperty("Icon",                   "${COMMONURL}/Image.jsp?name=TeamCenter_DocumentImage");
  addProperty("Tree",                   "TeamCenter_DocumentTree");
  addProperty("VFSFileLengthAttribute", "FileLength");
  addProperty("VFSFileNameAttribute",   "FileName");
  addProperty("VFSPrefix",              "S:/efaps/TESTSTORE");
  addProperty("VFSProvider",            "org.apache.commons.vfs.provider.local.DefaultLocalFileProvider");
}
