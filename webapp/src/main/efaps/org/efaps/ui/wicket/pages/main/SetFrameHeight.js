 /*
 * Copyright 2003 - 2009 The eFaps Team
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
 * Author:          jmox
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */
 
 /* 
 * @eFapsPackage  org.efaps.ui.wicket.pages.main
 * @eFapsUUID     7014a635-2c09-4ac8-918f-1edb2d22596d
 * @eFapsRevision $Rev$
 * 
 */

function eFapsSetIFrameHeight() {
  total = window.innerHeight;
  logo = document.getElementById('eFapsLogo').offsetHeight;
  menu = document.getElementById('eFapsMainMenu').offsetHeight; 
  document.getElementById('eFapsFrameContent').style.height = total - logo - menu + "px";
}