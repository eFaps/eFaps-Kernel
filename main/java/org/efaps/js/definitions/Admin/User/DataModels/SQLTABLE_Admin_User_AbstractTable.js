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

with (SQLTABLE)  {
  setSQLTable("USERABSTRACT");
  setSQLColumnID("ID");
  setSQLColumnType("TYPEID");
  setSQLNewIDSelect("select max(ID)+1 from USERABSTRACT");
}
