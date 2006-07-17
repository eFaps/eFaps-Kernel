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

function eFapsUpdateDataModel()  {
  print("############ Reload Cache");
  reloadCache();

  Shell.transactionManager.begin();
  var context = new Context(Shell.transactionManager.getTransaction(), Packages.org.efaps.admin.user.Person.get("Administrator"), null);
  Context.setThreadContext(context);
  Shell.setContext(context);
  var fileList = eFapsGetAllFiles("org/efaps/js/definitions", true);


  importImages(fileList);
  importSQLTables(fileList);
  importTypes(fileList);

  importForms(fileList);
  importTables(fileList);

  createMenus(fileList);
  createSearches(fileList);

  importCommands(fileList);
  importMenus(fileList);
  importSearches(fileList);


  Shell.transactionManager.commit();
  context.close();

}
