/*
 * Copyright 2003-2007 The eFaps Team
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

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// Browser Detection
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var tmp = navigator.userAgent.toLowerCase();
var isOp = tmp.indexOf("opera") >= 0;
var isIE = (tmp.indexOf("msie") > -1) && !isOp;
var isMoz = ((tmp.indexOf("gecko") >= 0) || (tmp.indexOf("netscape6") >= 0)) && !isOp;

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// Common eFaps functions
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * Returns the current window height.
 */
function eFapsCommonGetWindowHeight() {
  var height = 0;
  if (window.innerHeight)  {
    // the - 18 must be done if a scrollbar is defined!
    height = window.innerHeight/* - 18*/;
  } else if (document.documentElement && document.documentElement.clientHeight)  { 
    height = document.documentElement.clientHeight;
  } else if (document.body && document.body.clientHeight)  { 
    height = document.body.clientHeight;
  }
  return height;
}

/**
 * Returns the current window width.
 */
function eFapsCommonGetWindowWidth() {
  var width = 0;
  if (window.innerWidth)  {
    // the - 18 must be done if a scrollbar is defined!
    width = window.innerWidth/*- 18*/;
  } else if (document.documentElement && document.documentElement.clientWidth)  {
    width = document.documentElement.clientWidth;
  } else if (document.body && document.body.clientWidth)  {
    width = document.body.clientWidth;
  }
}

/**
 * Return the scroll x position.
 */
function eFapsCommonGetScrollX() {
  var scrollX = 0;

  if (typeof window.pageXOffset == "number")  {
    scrollX = window.pageXOffset;
  } else if (document.documentElement && document.documentElement.scrollLeft)  {
    scrollX = document.documentElement.scrollLeft;
  } else if (document.body && document.body.scrollLeft)  { 
    scrollX = document.body.scrollLeft; 
  } else if (window.scrollX)  {
    scrollX = window.scrollX;
  }

  return scrollX;
}

/**
 * Return the scroll y position.
 */
function eFapsCommonGetScrollY() {
  var scrollY = 0;

  if (typeof window.pageYOffset == "number")  {
    scrollY = window.pageYOffset;
  } else if (document.documentElement && document.documentElement.scrollTop)  {
    scrollY = document.documentElement.scrollTop;
  } else if (document.body && document.body.scrollTop)  { 
    scrollY = document.body.scrollTop; 
  } else if (window.scrollY)  {
    scrollY = window.scrollY;
  }

  return scrollY;
}

function eFapsCommonSubmit(_href,_target){
  document.eFapsSubmitForm.action = _href;
  document.eFapsSubmitForm.target = _target
  document.eFapsSubmitForm.submit();
}


/**
 * The function opens the url in a window.
 *
 * @param _href   (string)  url to open
 * @param _target (string)  name of the target where to open the url:
 *                          "Popup" means new window
 *                          "Replace" means same window
 * @param _width  (int)     width of the popup window
 * @param _height (int)     height of the popup window
 */
function eFapsCommonOpenUrl(_href, _target, _width, _height)  {
  if (_target == "Popup")  {
    eFapsCommonOpenWindowNonModal(_href, _width, _height);
  } else if (_target == "Replace")  {
    window.location.href = _href;
  } else  {
    var target;
    if (window.location.href.indexOf("MenuRequest")>=0)  {
      target = eFapsCommonFindFrame(parent, _target);
    } else if ((_href.indexOf("&nodeId=") > 0) || (_href.indexOf("?nodeId=") > 0))  {
      target = eFapsCommonFindFrame(parent, _target);
      if (!target)  {
        target = eFapsCommonFindFrame(parent.parent, _target);
      }
    }
    if (target)  {
      target.location.href = _href;
    } else  {
      target = eFapsCommonFindFrame(top, _target);
      if (target)  {
        target.location.href = _href;
      } else  {
        if (top.opener)  {
          target = eFapsCommonFindFrame(top.opener.top, _target);
        }
        if(target)  {
          target.location.href = _href;
        } else  {
          eFapsCommonOpenWindowNonModal(_href, _width, _height);
        }
      }
    }
  }
}

/**
 * Non-modal window
 * register the window in the childWindows array
 *        //set focus to the dialog
 *
 * @param _href     (String)
 * @param _width    (Integer)
 * @param _height   (Integer)
 */
function eFapsCommonOpenWindowNonModal(_href, _width, _height) {
  var top = parseInt((screen.height - _height) / 2);
  var left = parseInt((screen.width - _width) / 2);
  var attr = "location=no,menubar=no,titlebar=no,"+
      "top="+top+",left="+left+",width="+_width+",height="+_height+","+
      "resizable=yes,status=no,toolbar=no";
  var win = window.open(_href, "NonModalWindow" + (new Date()).getTime(), attr);
  win.focus();
}

/**
 * Register the child windows
 *
//register the window in the childWindows array
          //store the window in the childWindows array
 * @param _
 */
/*function gen_ui3_registerChildWindows(_windowObj, _topWindowObj)  {
  if ((_topWindowObj.childWindows != null) && (_topWindowObj.childWindows != "undefined"))  {
    _topWindowObj.childWindows[_topWindowObj.childWindows.length] = _windowObj;
  } else if ((_topWindowObj.opener.top != null) && (_topWindowObj.opener.top != "undefined")) {
    var parentTop = _topWindowObj.opener.top;
    gen_ui3_registerChildWindows(_windowObj, parentTop)
  }
}
*/

/**
 * The function finds the frame with the name in the value of the parameter
 * <i>_target</i>.
 *
 * @param _current  (window)  current window object
 * @param _target   (string)  name of target
 * @return found window object
 */
function eFapsCommonFindFrame(_current, _target)  {
  var ret = _current.frames[_target];
  if (!ret) {
    for (var i=0; i < _current.frames.length && !ret; i++)  {
      ret = eFapsCommonFindFrame(_current.frames[i], _target);
    }
  }
  return ret;
}

/**
 * The function closes the window.
 */
function eFapsCommonCloseWindow() {
  top.window.close();
  return;
}

/**
 * The function sets the title.
 *
 * @param _newTitle (String) new title to set
 */
function eFapsSetTitle(_newTitle)  {
  var obj = document.getElementById('eFapsFrameHeaderText');
  obj.firstChild.data=_newTitle;
  top.document.title = _newTitle;
}

function eFapsProcessEnd()  {
  var obj = document.getElementById('eFapsFrameHeaderProgressBar');
  obj.style.display = 'none';
}

function eFapsProcessStart()  {
  var obj = document.getElementById('eFapsFrameHeaderProgressBar');
  obj.style.display = '';
}


/**
 * The function
 *
 * @param _cacheKey  (String)  time stamp in the cache to clean up
 */
function eFapsCleanUpSession(_cacheKey)  {
  var hiddenFrame;
  if (top.opener)  {
    hiddenFrame = top.opener.top.eFapsFrameHidden;
  } else  {
    hiddenFrame = top.eFapsFrameHidden;
  }
  hiddenFrame.location.href = '../common/CleanupSession.jsp?cacheKey=' + _cacheKey;
}

/**
 * The function opens the url in a window. If parameter <i>_target</i> is
 * "popup", the url is opened in a new window
 *
 * @param _href   (string)  url to open
 */
function eFapsCommonPrint(_href)  {
  var attr = "location=no,menubar=no,titlebar=no,"+
      "resizable=yes,status=no,toolbar=no,scrollbars=yes";
  var win = window.open(_href, "NonModalWindow" + (new Date()).getTime(), attr);
  win.focus();
}

function eFapsCommonRefresh()  {
  location.href = location.href;
//  location.reload();
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// General functions for tables
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * The function selectes or deselects all checkboxes starting with the name
 * standing in the parameter <i>_name</i>. The function is used e.g. from the
 * table header to (de)select all rows of the table with one click.
 *
 * @param _form     (Form)    form object
 * @param _name     (String)  name of the field
 * @param _selected (Boolean) <i>true</i> means, that all checkboxes must be
 *                            selected, <i>false</i> deselects all checkboxes
 */
function eFapsTableSelectDeselectAll(_document, _name, _selected)  {
  var objs = _document.getElementsByName(_name);
  for (var i=0; i<objs.length; i++)  {
    objs[i].checked = _selected;
  }
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// General administration functions to set the position of the content frame
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * The javascript function positions the main frame to maximum height. The
 * maximum height is defined by the window height minus position and heigth of
 * the toolbar menu.
 */
function eFapsPositionContent() {
  var iFrame = document.getElementById("eFapsFrameContent");
  var menu = document.getElementById("eFapsMainTableRowMenu");
  var newHeight = eFapsCommonGetWindowHeight()
                        - menu.offsetTop
                        - menu.offsetHeight;

  if (newHeight > 0)  {
    iFrame.style.height = newHeight + 'px';
  } else  {
    iFrame.style.height = newHeight + '0px';
  }
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// General administration functions to set the position of the main frame
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var eFapsPositionMainBottomHeight = 55;

/**
 * The javascript function positions the main frame to maximum width and height.
 */
function eFapsPositionMain() {
  var newHeight;
  var newWidth;

  var iFrame = document.getElementById("eFapsFrameMain");

  if (isIE)  {
    newHeight = iFrame.offsetParent.clientHeight - eFapsPositionMainBottomHeight - iFrame.offsetTop;
    newWidth  = iFrame.offsetParent.clientWidth-3;
  } else  {
    newHeight = window.innerHeight - eFapsPositionMainBottomHeight - iFrame.offsetTop;
    newWidth = window.innerWidth-3;
  }

  if (newHeight>50 && newWidth>30)  {
    if (iFrame.style.height!=newHeight+'px' || iFrame.style.width!=newWidth+'px')  {
      iFrame.style.height = newHeight+'px';
      iFrame.style.width  = newWidth+'px';
      iFrame.style.display='';
    }
  } else  {
    iFrame.style.display='none';
  }
//  var body = document.getElementsByTagName('body')[0];
//  body.style.height = '10px';
//  body.style.width = '50px';

  // if browser has scrollbar flag, scrollbar is not visible (for mozilla)
  if (window.scrollbars)  {
    window.scrollbars.visible = false;
  }

  window.setTimeout("eFapsPositionMain()",1);
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// General administration functions for filters of tables
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * The variable stores the filter object.
 */
var eFapsFrameFilterInstance;

/**
 * The function returns the filter object.
 */
function eFapsGetFrameFilter()  {
  if (!eFapsFrameFilterInstance)  {
    eFapsFrameFilterInstance = new eFapsFrameFilter();
  }
  return eFapsFrameFilterInstance;
}

//filter.options[0].selected = true;

/**
 * The function is the constructor for the eFaps Frame Filter object.
 */
function eFapsFrameFilter()  {
  this.filterDomObject = document.getElementsByName("eFapsFrameFilter")[0];
}

/**
 * The method removes all filters of the frame.
 */
eFapsFrameFilter.prototype.clean = function()  {
  while (this.filterDomObject.length>0) {
    this.filterDomObject.remove(0);
  }
  this.filterDomObject.style.display = 'none';
}

/**
 * The method adds a new option to the filter.
 *
 * @param _text   (String) text of the new option of the filter
 * @param _value  (String) value of the new option of the filter (name of the 
 *                         option)
 */
eFapsFrameFilter.prototype.add = function(_text, _value, _selected)  {
  var newOption = document.createElement("option");
  newOption.text = _text;
  newOption.value = _value;
  if (_selected)  {
    newOption.selected = true;
  }
  if (isIE)  {
    this.filterDomObject.add(newOption, this.filterDomObject.length);
  } else  {
    this.filterDomObject.add(newOption, null);
  }
  this.filterDomObject.style.display = '';
}

/**
 * The method sets the form for this filter.
 *
 * @param _form   (DOMObject) form DOM object
 */
eFapsFrameFilter.prototype.setForm = function(_form)  {
  this.form = _form;
}

/**
 * The method executes a filter.
 *
 * @param _value  (String) value of the new option of the filter (name of the 
 *                         option)
 */
eFapsFrameFilter.prototype.execute = function(_value)  {
  // search document element for this form
  var doc = this.form;
  while (doc.parentNode)  {
    doc = doc.parentNode;
  }
  var url = doc.location.href;
  var index = url.indexOf('?');
  if (index>=0)  {
    url = url.substring(0, index);
  }
  url = url + "?cacheKey=" + this.form.eFapsCacheKey.value + "&filter=" + _value;
  if (doc.location.href.indexOf('mode=print')>=0)  {
    url = url + "&mode=print";
  }
  doc.location.href = url;
}


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//
//
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * @param _self (Object)
 */
function eFapsUniqueKeyValueChange(_self)  {
  var formTag = _self;
  while (formTag.nodeName != "FORM")  {
    formTag = formTag.parentNode;
  }
  parent.eFapsProcessStart();
  formTag.action = '../common/FormProcessUniqueKeyValue.jsp';
  var target = formTag.target;
  formTag.target = 'eFapsFrameHidden';
  formTag.submit();
  formTag.target = target;
}

function eFapsAddChildText(_element, _txt){
    while (_element.firstChild){
     _element.removeChild(_element.firstChild);
    }
    var textar = _txt.split("<br/>");
    for (var i=0; i<textar.length; i++)  {
      _element.appendChild(document.createTextNode(textar[i]));
      _element.appendChild(document.createElement("br"));
    }
  }
 
 
 function eFapsShowAdvancedError(errorpage)  {
       
        var winleft = parseInt((screen.width - 700) / 2);
        var wintop = parseInt((screen.height - 700) / 2);
        var myWin = window.open("", "test",
            "dependent=no,"+
            "location=no,"+
            "menubar=no,"+
            "titlebar=no,"+
            "hotkeys=no,"+
            "status=no,"+
            "toolbar=no,"+
            "scrollbars=yes,"+
            "resizable=yes,"+
            "height=700,"+
            "width=700,"+
            "left="+winleft+","+
            "top="+wintop);
        myWin.document.write(unescape(errorpage));
        myWin.focus();
        }
