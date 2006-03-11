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
  setSQLTable("LCSTATUS");
  setSQLColumnID("ID");
  setSQLNewIDSelect("select max(ID)+1 from LCSTATUS");
}
