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
  setSQLTable("LCPOLICY");
  setSQLColumnID("ID");
  setSQLNewIDSelect("select max(ID)+1 from LCACCESSTYPE");
}
