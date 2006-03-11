/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_UI_LinkList4Links
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

with (TABLE)  {
  with (addField("type"))  {
    addProperty("Expression",             "Type");
    addProperty("Label",                  "Admin_UI_LinkList/Type4Links.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("totype"))  {
    addProperty("AlternateOID",           "To.OID");
    addProperty("Expression",             "To.Type");
    addProperty("Label",                  "Admin_UI_LinkList/ToType.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("toname"))  {
    addProperty("AlternateOID",           "To.OID");
    addProperty("Expression",             "To.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_UI_LinkList/ToName.Label");
  }
  with (addField("openInNewWindow"))  {
    addProperty("AlternateOID",           "To.OID");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}
