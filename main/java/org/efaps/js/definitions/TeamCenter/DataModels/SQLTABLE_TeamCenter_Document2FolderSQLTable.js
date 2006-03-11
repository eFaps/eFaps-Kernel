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
  setSQLTable("TCDOC2FOL");
  setSQLColumnID("ID");
  setSQLNewIDSelect("select max(ID)+1 from TCDOC2FOL");
}
