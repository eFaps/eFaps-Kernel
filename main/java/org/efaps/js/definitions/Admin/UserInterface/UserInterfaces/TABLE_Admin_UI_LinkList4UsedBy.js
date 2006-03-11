/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_UI_LinkList4UsedBy
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
    addProperty("Label",                  "Admin_UI_LinkList/Type4UsedBy.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("fromtype"))  {
    addProperty("AlternateOID",           "From.OID");
    addProperty("Expression",             "From.Type");
    addProperty("Label",                  "Admin_UI_LinkList/FromType.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("fromname"))  {
    addProperty("AlternateOID",           "From.OID");
    addProperty("Expression",             "From.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_UI_LinkList/FromName.Label");
  }
  with (addField("openInNewWindow"))  {
    addProperty("AlternateOID",           "From.OID");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Target",                 "popup");
    addIcon("eFapsActionNewWindow");
  }
}
