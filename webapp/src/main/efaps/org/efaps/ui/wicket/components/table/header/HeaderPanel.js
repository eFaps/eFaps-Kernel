/*
 * Copyright 22003 - 2009 The eFaps Team
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
 * @eFapsPackage  org.efaps.ui.wicket.components.table.header
 * @eFapsUUID      62934dc5-42b0-48eb-8e5a-57e6c41a77a8
 * @eFapsRevision $Rev$
 */

var minWidth = 20;
var seperatorWidth = 200;
var lastpos = 0;
var startpos = 0;
var seperatorOffset = 0;
var connections = [];
var seperator;



function headerProperties(){
 this.headerID = "";
 this.bodyID = "";
 this.modelID = "";
 this.storeColumnWidths ="";
 this.storeColumnOrder = "";
 this.reloadTable = "";
} 

function positionTableColumns(_props) {
  var header = document.getElementById(_props.headerID);
  var cells = new Array();
  var widthWeight = 0;
  var widthCor = 0;
  var addCell = 0;
  var celldivs = header.getElementsByTagName("div");
  for(i = 0; i < celldivs.length; i++){
    var cell = celldivs[i];
    var fixed = cell.className.indexOf("eFapsCellFixedWidth");
    if(fixed > -1){
      var addwith = getAdditionalWidth(cell);
      cells.push(new Array(cell.clientWidth, addwith, false));
      widthCor += cell.clientWidth + addwith;
    }
    var f = cell.className.indexOf("eFapsCellWidth");
    if (f>-1){
      var addwith = getAdditionalWidth(cell);
      cells.push(new Array(cell.clientWidth, addwith,true));
      widthWeight += cell.clientWidth;
      widthCor+= addwith;
    }
  }
  var tablebody = document.getElementById(_props.bodyID);
  var completeWidth = (tablebody.clientWidth ) ;
  if (completeWidth != 0) {
    header.style.width = completeWidth + "px";
    var cellWidth;
    var rightshift = 0;
    for(k = 0;k < cells.length; k++){
      if(cells[k][2]==true){
        var rule = getStyleRule(k + _props.modelID, _props.modelID);
        cellWidth = ((100/widthWeight * cells[k][0])/100)* (completeWidth - widthCor - 5);
        rule.style.width= cellWidth + "px";
       
      }else {
        cellWidth = cells[k][0];
      }
      if(k+1 < cells.length){
        rightshift += cellWidth + cells[k][1];
        if(cells[k][2]==true ){
          if(document.getElementById((k + _props.modelID) + "eFapsHeaderSeperator")){
            document.getElementById((k + _props.modelID) + "eFapsHeaderSeperator").style.left = rightshift + "px";
          }
        }
      }
    }
  }
}

 
// function used to retrieve a Style Rule from a StyleSheet
function getStyleRule(_styleIndex, _modelID) {
  var selectorName = ".eFapsCellWidth" + _styleIndex;
  for (i = 0; i < document.styleSheets.length; i++) { 
    var find = document.styleSheets[i].cssRules[0].cssText.indexOf("eFapsCSSId"+ _modelID)
    if(find > -1){
      for (j = 0; j < document.styleSheets[i].cssRules.length; j++) {
        if (document.styleSheets[i].cssRules[j].selectorText == selectorName) {
          return document.styleSheets[i].cssRules[j];
        }
      }
    }
  }
}
//function used to retrieve the additional Width of an DomObject like the margin
// and padding
function getAdditionalWidth(_cell){
  var compu = window.getComputedStyle(_cell,null); 
  var width=0;
  width += parseInt(compu.getPropertyValue("margin-left"));
  width += parseInt(compu.getPropertyValue("margin-right"));
  width += parseInt(compu.getPropertyValue("padding-left"));
  width += parseInt(compu.getPropertyValue("padding-right"));
  width += parseInt(compu.getPropertyValue("border-left-width"));
  width += parseInt(compu.getPropertyValue("border-right-width"));
  return width;
}

function getColumnWidths(_props){
  var header = document.getElementById(_props.headerID);
  var celldivs = header.getElementsByTagName("div");
  var widths="";
  for(i = 0;i<celldivs.length;i++){
    if(celldivs[i].className.indexOf("eFapsCellFixedWidth") > -1 || 
         celldivs[i].className.indexOf("eFapsCellWidth") > -1){
      widths += window.getComputedStyle(celldivs[i],null).getPropertyValue("width") +";";
    }
  }
  return widths;
}
  
function beginColumnSize(_seperator,_event){
  lastpos = _event.screenX;
  startpos = lastpos;
  seperator = _seperator;
  seperatorOffset = _event.screenX - parseInt(_seperator.style.left);
  _seperator.style.width = parseInt(window.getComputedStyle(_seperator,null).getPropertyValue("width")) + seperatorWidth +"px";
  _seperator.style.left = parseInt(_seperator.style.left) - seperatorWidth/2 +"px";
  _seperator.style.backgroundPosition="top center";
  connections[0] = dojo.connect(_seperator,"onmousemove",this,  "doColumnSize" );
  connections[1] = dojo.connect(_seperator,"onmouseout",this,  "cancelColumnSize" );
}


function doColumnSize(_event){
  seperator.style.left= (_event.screenX-seperatorOffset) - seperatorWidth/2 +"px";
  lastpos=_event.screenX;
}

function endColumnSize(_seperator,_event,_props){
  dojo.forEach(connections,dojo.disconnect);
  var dif = lastpos - startpos;
  var i = parseInt(seperator.id);
  var leftrule = getStyleRule(i, _props.modelID);
  var rightrule = getStyleRule(i+1, _props.modelID);
  var leftWidth = parseInt(leftrule.style.width);
  var rightWidth = parseInt(rightrule.style.width);
  var move = 0;
  if(leftWidth + dif > minWidth && rightWidth - dif > minWidth){
    leftrule.style.width = leftWidth + dif +"px";
    rightrule.style.width = rightWidth - dif +"px";
  } else {
    if(dif < 0){
      leftrule.style.width = minWidth +"px";
      rightrule.style.width = rightWidth + leftWidth - minWidth + "px";
      move = (leftWidth + dif-minWidth);
    } else {
      rightrule.style.width = minWidth +"px";
      leftrule.style.width = leftWidth + rightWidth - minWidth + "px";
      move = -(rightWidth - dif - minWidth);
    }
  }
  _seperator.style.width = parseInt(_seperator.style.width) - seperatorWidth +"px";
  _seperator.style.left = parseInt(_seperator.style.left) - move + seperatorWidth/2 +"px";
  _seperator.style.backgroundPosition = "-200px 0";
  _props.storeColumnWidths(getColumnWidths(_props));
}


function cancelColumnSize(_event){
  endColumnSize(seperator,_event);
}

function getColumnOrder(props){
  var header = document.getElementById(props.headerID);
  var celldivs = header.getElementsByTagName("div");
  var ids="";
  for(i = 0;i<celldivs.length;i++){
    if(celldivs[i].className.indexOf("eFapsCellFixedWidth") > -1 || 
              celldivs[i].className.indexOf("eFapsCellWidth") > -1){
      ids += celldivs[i].id + ";";
    }
  }
  return ids;
}

function addOnResizeEvent(func) {
  var oldonload = window.onresize;
  if (typeof window.onresize != 'function') {
    window.onresize = func;
  } else {
    window.onresize = function() {
      oldonload();
      func();
    }
  }
}
