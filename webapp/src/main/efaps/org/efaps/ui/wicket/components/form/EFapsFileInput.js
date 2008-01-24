/*
 * Copyright 2003-2008 The eFaps Team
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
 * This script is needed because when a AjaxSubmit is done with the script from 
 * wicket the elements are serialized and filtered. The Filter does not pass the 
 * values from inputs of type "file". Therefor this script creates hidden inputs 
 * with the sam name and value as the filtered one. 
 * The script is executed as a precondition script for the ajaxsubmitbehavior 
 * from wicket, therefore it must return true to allow the ajaxcall to advance.
 */

function eFapsFileInput(){
  var ins = document.getElementsByTagName("input");
  
  for (var i = 0; i < ins.length; ++i) {
    if(ins[i].type.toLowerCase() == "file"){
      var fileNode = ins[i];
      var parent = fileNode.parentNode;
      var  node = fileNode.nextSibling; 
      var add = true; 
      while (node != null) {
        if(node.nodeName == "INPUT"){
          if(node.type == "hidden" && node.name == fileNode.name){
            node.value = fileNode.value;
            add = false;
          }
        }
        node = node.nextSibling;
      }
      if(add){
        var newinput = document.createElement("input");
        newinput.type = "hidden";
        newinput.name = fileNode.name;
        newinput.value = fileNode.value;
        parent.appendChild(newinput);
      }
    }
  }  
  return true;
}