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
  with (addAttribute("IndexPosition"))  {
    setAttributeType("Integer");
    setSQLTable("Admin_Event_DefinitionTable");
    setSQLColumn("INDEXPOS");
  }
  with (addAttribute("Abstract"))  {
    setAttributeType("Link");
    setTypeLink("Admin_Abstract");
    setSQLTable("Admin_Event_DefinitionTable");
    setSQLColumn("ABSTRACT");
  }
}
