/*******************************************************************************
* Admin_UI_Table:
* ~~~~~~~~~~~~~~~
* Admin_LifeCycle_StatusList
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
  with (addField("id"))  {
    addProperty("Expression",             "ID");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_LifeCycle_Status/ID.Label");
  }
  with (addField("name"))  {
    addProperty("Expression",             "Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_LifeCycle_Status/Name.Label");
    addProperty("ShowTypeIcon",           "true");
  }
  with (addField("PolicyLink"))  {
    addProperty("AlternateOID",           "PolicyLink.OID");
    addProperty("Expression",             "PolicyLink.Name");
    addProperty("HRef",                   "${COMMONURL}/MenuTree.jsp");
    addProperty("Label",                  "Admin_LifeCycle_Status/PolicyLink.Label");
    addProperty("ShowTypeIcon",           "true");
  }
}
