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

eFapsMenu.prototype.imageBullet = "/eFaps/images/eFapsButtonSmallDefault.gif";

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
 * The function calles current url with the cacheKey of the name of the map
 * in cache and the sort key with the column to sort.
 *
 * @param _cacheKey   (String)  name of the cache
 * @param _sortKey    (String)  column to sort
 * @param _sortDir    (String)  sort direction
 */
function eFapsSortTable(_cacheKey, _sortKey, _sortDir)  {
  var url = location.href;
  var index = url.indexOf('?');
  if (index>=0)  {
    url = url.substring(0, index);
  }
  url = url + "?cacheKey=" + _cacheKey + "&sortKey=" + _sortKey;
  if (location.href.indexOf('mode=print')>=0)  {
    url = url + "&mode=print";
  }
  if (_sortDir)  {
    url += "&sortDir=" + _sortDir;
  }
  location.href = url;
}

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
// General functions for searches
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * The javascript function display or display not the search commands for
 * a search menu. It is called from the search menus.
 *
 * @param _searchMenu name of the search menu
 */
function eFapsSearchPlusMinus(_searchMenu)  {
  var div = document.getElementById(_searchMenu);
  var a   = div.parentNode.firstChild;
  while (a.nodeName!='A' && a.nextSibling)  {
    a = a.nextSibling;
  }
  var img = a.firstChild;
  while (img.nodeName!='IMG' && img.nextSibling)  {
    img = img.nextSibling;
  }
  if (div.style.display=='none')  {
    div.style.display='';
    img.src = img.src.replace('Plus','Minus');
  } else  {
    div.style.display='none';
    img.src = img.src.replace('Minus','Plus');
  }
  a.blur();
}

/**
 * The variable stores the last select command used by the function
 * eFapsSearchSelect to deselect the command (change back to the original
 * cascade style sheet class).
 */
var eFapsSearchCommandSelected;

/**
 * The command is called from the search commands. It selects the command
 * (change the cascade style sheet class) and opens the depending search form.
 *
 * @param _searchCommand  name of search command
 */
function eFapsSearchSelectOld(_searchCommand)  {
  var a = document.getElementById(_searchCommand);

  if (eFapsSearchCommandSelected)  {
    eFapsSearchCommandSelected.className = 'eFapsSearchCommand';
  }
  eFapsSearchCommandSelected = a;
  a.className = 'eFapsSearchCommandSelected';
  a.blur();
  parent.parent.eFapsProcessStart();
  window.parent.content.location.href = window.location.href.replace('SearchNavigator.jsp?', 'SearchForm.jsp?') +
      "&command=" + _searchCommand;
}

/**
 * The command is called from the search commands. It selects the command
 * (change the cascade style sheet class) and opens the depending search form.
 *
 * @param _searchCommand  name of search command
 */
function eFapsSearchSelect(_searchCommand)  {
  eFapsProcessStart();
  window.genMain.content.location.href = window.location.href.replace('Link.jsf?', 'SearchForm.jsp?').replace('command=', 'search=') +
      "&command=" + _searchCommand;

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

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// General administration functions for the menus (header, footer and footer
// action menu)
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * The variable stores the footer action menu object
 */
var eFapsFrameFooterAction;

/**
 * The function returns the footer action menu object.
 */
function eFapsGetFrameFooterAction()  {
  if (!eFapsFrameFooterAction)  {
    eFapsFrameFooterAction = new eFapsMenu(document, document.getElementsByTagName("body")[0], "eFapsFrameFooterAction");
  }
  return eFapsFrameFooterAction;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// Class Menu for the Header and Footer menu
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var eFapsMenus = new Array();

/**
 * @param _doc
 * @param _parent
 * @param _id
 * @param _name
 * @param _text
 * @param _href
 * @param _submitForm
 * @param _popup      (boolean) the target window must be popupped
 * @param _hidden     (boolean) the target window is the hidden window
 * @param _width      (int)     width of the popup window
 * @param _heigth     (int)     height of the popup window
 * @param _className  (string)
 * @param _question   (string)  question ask before executing command
 * @param _image      (string)  image to show
 */
function eFapsMenu(_doc, _parent, _id, _name, _text, _href, _submitForm, _popup, _hidden, _width, _height, _className, _question, _image)  {
  if (_name)  {
    this.name = _name;
  }
  if (_className)  {
    this.className = _className;
  } else  {
    this.className = _id;
  }
  if (_question)  {
    this.question = _question;
  }
  if (_submitForm)  {
    this.submitForm = _submitForm;
  } else  {
    this.submitForm = false;
  }
  if (_popup)  {
    this.popup  = _popup;
  } else  {
    this.popup = false;
  }
  if (_hidden)  {
    this.hidden  = _hidden;
  } else  {
    this.hidden = false;
  }
  if (_width)  {
    this.width = _width;
  } else  {
    this.width = 600;
  }
  if (_height)  {
    this.height = _height;
  } else  {
    this.height = 400;
  }
  this.parentHtmlElement = _parent;
  this.document = _doc,
  this.menuDiv = _doc.createElement("div");
  this.menuDiv.className = this.className;
  this.submitLock = false;
  if (_id && _id!="")  {
    this.menuDiv.id = _id;
  }
  _parent.appendChild(this.menuDiv);
  this.subMenus = new Array();

  this.id = eFapsMenus.length;
  eFapsMenus[eFapsMenus.length] = this;

  if (_text)  {
    // href for icon
    this.aHtml4ImgElement = this.document.createElement("a");
    this.aHtml4ImgElement.className = this.className;
//    if (this.submitForm)  {
      this.aHtml4ImgElement.href = 'javascript:eFapsMenus['+this.id+'].execute()';
      this.href = _href;
//    } else  {
//        this.aHtml4ImgElement.href = _href;
//    }
    this.menuDiv.appendChild(this.aHtml4ImgElement);
    // append normal icon to href
    this.imageHtmlElement = this.document.createElement("img");
    this.imageHtmlElement.className = this.className;
    if (_image)  {
      this.imageHtmlElement.src = _image;
    } else  {
      this.imageHtmlElement.src = eFapsMenu.prototype.imageBullet;
    }
    this.aHtml4ImgElement.appendChild(this.imageHtmlElement);
    // append href element
    this.aHtmlElement = this.document.createElement("a");
    this.aHtmlElement.className = this.className;
//    if (this.submitForm)  {
      this.aHtmlElement.href = 'javascript:eFapsMenus['+this.id+'].execute()';
      this.href = _href;
//    } else  {
//        this.aHtmlElement.href = _href;
//    }
    this.menuDiv.appendChild(this.aHtmlElement);
    // append text
    this.textHtmlElement = this.document.createTextNode(_text);
    this.aHtmlElement.appendChild(this.textHtmlElement);
  }

}

/**
 *
 * @param _submitForm the action is a submit (the given form is submitted
 * @param _popup      (boolean) the target window must be popupped
 * @param _hidden     (boolean) the target window is the hidden window
 */
eFapsMenu.prototype.add = function(_title, _href, _submitForm, _popup, _hidden, _width, _height, _name, _question, _image)  {
  var subMenu = new eFapsMenu(this.document, this.menuDiv, null, _name, _title, _href, _submitForm, _popup, _hidden, _width, _height, this.className, _question, _image);
  subMenu.setForm(this.getForm());
  this.subMenus[this.subMenus.length] = subMenu;
}

/**
 *
 */
eFapsMenu.prototype.setForm = function(_form)  {
  this.form = _form;
}

/**
 *
 */
eFapsMenu.prototype.getForm = function()  {
  return this.form;
}

/**
 * @see #executeSubmit
 * @see #executeHidden
 */
eFapsMenu.prototype.execute = function()  {
  var bck = true;

  if (this.question)  {
    bck = confirm(this.question);
  }
  if (bck)  {
    if (this.submitForm)  {
      this.executeSubmit();
    } else if (this.popup)  {
      eFapsCommonOpenWindowNonModal(this.href, this.width, this.height);
    } else if (this.hidden)  {
      this.executeHidden();
    } else {
      eval(this.href);
    }
  }
}

/**
 * The method calls in the hidden frame the href.
 *
 * @see #execute
 */
eFapsMenu.prototype.executeHidden = function()  {
  eFapsProcessStart();
  eFapsFrameHidden.location.href = this.href;
}


/**
 * The current form is submitted to the given href.
 *
 * @see #execute
 */
eFapsMenu.prototype.executeSubmit = function()  {
  if (!this.submitLock)
  {
    var errorMsg = checkFormForRequirements(this.getForm());
    if (errorMsg == null || errorMsg.length == 0)
    {
      this.submitLock = true;
      eFapsProcessStart();
      this.getForm().action=this.href;
      if (this.getForm().command)  {
        this.getForm().command.value=this.name;
      }
      this.getForm().submit();
    }
    else
    {
      alert(errorMsg);
    }
  }
  else
  {
	  alert("Please click only once on '"+this.textHtmlElement.nodeValue+"'");
  }
}

/**
 * The function cleans the menu (removes all sub menus from the menu)
 *
 * @param _isSubMenu    true, if the menu is a sub menu
 */
eFapsMenu.prototype.clean = function(_isSubMenu)  {
  while (this.subMenus.length>0)  {
    var subMenu = this.subMenus.pop();
    subMenu.clean(true);
  }
  if (_isSubMenu)  {
    this.menuDiv.parentNode.removeChild(this.menuDiv);
  }
}

/**
 * The function checks if all mandatory fields on a form are filled
 *
 * @param _form   the form object to be checked
 * @return empty strin if all mandatory fields are filled, a error text else.
 */
function checkFormForRequirements(_form)
{
  var tables = _form.getElementsByTagName("table");
  var i = 0;
  var formTable = null;
  var errorMsg = "";

  //ermittle die formulartabelle
  while ((i < tables.length) && (formTable == null))  {
    if(tables[i].className == "eFapsForm")  {
      formTable = tables[i];
    }
    i++;
  }

  if (formTable != null)  {
    var tableRows = formTable.getElementsByTagName("tr");
    var rowFields = new Array();
    var fieldInputs = new Array();

    for (i = tableRows.length-1; i >= 0; i--)  {
      rowFields = tableRows[i].getElementsByTagName("td");
      if (rowFields.length > 1)  {
        if (rowFields[0].className == "eFapsFormLabelRequired")  {
          //checke 'input' felder die einen text enthalten können
          fieldInputs = rowFields[1].getElementsByTagName("input");
          if (fieldInputs.length > 0 &&
            (fieldInputs[0].type == null || fieldInputs[0].type == "" || 
            fieldInputs[0].type == "text" || fieldInputs[0].type == "file" || 
            fieldInputs[0].type == "password" || fieldInputs[0].type == "hidden") && 
            (fieldInputs[0].value == null || fieldInputs[0].value == ""))
          {
            errorMsg = "Please fill out field '"+rowFields[0].firstChild.data+"'\n"+errorMsg;
            if (fieldInputs[0].type != "hidden")
            {
              fieldInputs[0].focus();
            }
          }

          // check fields of type 'textarea'
          fieldInputs = rowFields[1].getElementsByTagName("textarea");
          if (fieldInputs.length > 0 &&
            (fieldInputs[0].value == null || fieldInputs[0].value == ""))
          {
            errorMsg = "Please fill out field '"+rowFields[0].firstChild.data+"'\n"+errorMsg;
            fieldInputs[0].focus();
          }

          // check fields of type 'select'
          fieldInputs = rowFields[1].getElementsByTagName("select");
          if (fieldInputs.length > 0)  {
            var selectionOptions = fieldInputs[0].options;
            var hasQualifiedSelection = false;
            for (var j = 0; j < selectionOptions.length && !hasQualifiedSelection; j++)
            {
              if (selectionOptions[j].selected && selectionOptions[j].text != "")
              {
                hasQualifiedSelection = true;
              }
            }
            if (!hasQualifiedSelection)
            {
              errorMsg = "Please make a selection on '"+rowFields[0].firstChild.data+"'\n"+errorMsg;
              fieldInputs[0].focus();
            }
          }
        }
      }
    }
    return errorMsg;
  }
}
