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
  setSQLTable("DMTYPE2POLICY");
  setSQLColumnID("ID");
  setSQLNewIDSelect("select max(ID)+1 from DMTYPE2POLICY");
}
