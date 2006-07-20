/*
 * Copyright 2006 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:          tmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

/**
 * The private function creates all Team Center tables.
 *
 * @param _stmt SQL statement to work on
 */
function _eFapsCreateAllTeamCenterTables(_con, _stmt)  {

  print("Create TeamCenter Tables");

  eFapsCommonSQLTableCreate(_con, _stmt, "Folder object", "TCFOLDER", "ABSTRACT",[
      ["PARENTFOLDER          "+TYPE_INTEGER],
      ["constraint TCFOLDER_FK_PRNTFOL foreign key(PARENTFOLDER) references TCFOLDER(ID)"]
  ]);

  eFapsCommonSQLTableCreate(_con, _stmt, "Document object", "TCDOCUMENT", "ABSTRACT",[
      ["FILENAME              "+TYPE_STRING_SHORT+"(128)"],
      ["FILELENGTH            "+TYPE_INTEGER]
  ]);

  eFapsCommonSQLTableCreate(_con, _stmt, "Connection beetween Documents and Folders", "TCDOC2FOL", null,[
      ["TCDOCUMENT            "+TYPE_INTEGER+"                   not null"],
      ["TCFOLDER              "+TYPE_INTEGER+"                   not null"],
      ["CREATOR               "+TYPE_INTEGER+"                   not null"],
      ["CREATED               "+TYPE_DATETIME+"                  not null"],
      ["MODIFIER              "+TYPE_INTEGER+"                   not null"],
      ["MODIFIED              "+TYPE_DATETIME+"                  not null"],
      ["constraint TCDOC2FOL_UK_FOLDOC unique(TCDOCUMENT,TCFOLDER)"],
      ["constraint TCDOC2FOL_FK_TCDOC  foreign key(TCDOCUMENT)   references ABSTRACT(ID)"],
      ["constraint TCDOC2FOL_FK_TCFLDR foreign key(TCFOLDER)     references TCFOLDER(ID)"],
      ["constraint TCDOC2FOL_FK_CRTR   foreign key(CREATOR)      references USERPERSON(ID)"],
      ["constraint TCDOC2FOL_FK_MDFR   foreign key(MODIFIER)     references USERPERSON(ID)"]
  ]);
}


//_eFapsCreateAllTeamCenterTables(con, stmt);
