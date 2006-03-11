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
  setSQLTable("LCACCESSTYPE");
  setSQLColumnID("ID");
  setSQLNewIDSelect("select max(ID)+1 from LCACCESSTYPE");
}
