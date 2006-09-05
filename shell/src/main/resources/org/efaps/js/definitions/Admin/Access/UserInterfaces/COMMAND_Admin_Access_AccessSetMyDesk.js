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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

with (COMMAND)  {
  addProperty("Target",                 "content");
  addProperty("TargetQueryTypes",       "Admin_LifeCycle_AccessSet");
  addProperty("TargetShowCheckBoxes",   "true");
//  addIcon("eFapsAdminLifeCycleAccessType");
  addTargetMenu("Admin_LifeCycle_AccessSetMyDesk_Menu");
  addTargetTable("Admin_LifeCycle_AccessSetTable");
  addRole("Administration");
}
