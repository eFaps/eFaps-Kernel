/**************************************************************************
  Copyright (c) 2001 Geir Landrö (drop@destroydrop.com)
  JavaScript Tree - www.destroydrop.com/hugi/javascript/tree/
  Version 0.96

  This script can be used freely as long as all copyright messages are
  intact.

  Description
  ~~~~~~~~~~~
  The url of
 ${NODEID} wird ersetzt

**************************************************************************/

/**
 * Constructor which creates a new tree node.
 * If the <i>_url</i> has a ${NODE} in the string, it is replaced by the node
 * index.
 *
 * @param _id     (String)    Identifier for the tree node
 * @param _text   (String)    Tree node text
 * @param _isType (Boolean)   the tree node is a type tree node
 * @param _url    (String)    Url of the refence
 * @param _icon   (String)    Url to the icon
 * @parqm _target (String)    target of the url
 * @param _level  (Integer)   level of the deep (default value is 0)
 * @param _parent (eFapsTree) parent tree node
 */
function eFapsTree(_id, _text, _isType, _url, _icon, _target, _width, _height, _level, _parent)  {
  this.id   = _id;
  this.text = _text;
  this.icon = _icon;
  this.subTree = new Array();
  if (_level)  {
    this.level = _level;
  } else  {
    this.level = 0;
  }
  this.parent = _parent;
  this.isLastSibling = true;
  this.index = eFapsTree.prototype.counter++;
  this.shown = false;
  this.spaceImages = new Array();
  this.url = _url;
  this.isType = _isType
  this.target = _target;
  this.width = _width;
  this.height = _height;
  eFapsTree.prototype.allTrees[this.index] = this;

  if (this.level == 0)  {
    this.clean = function()  {
    }
  }
}

///////////////////////////////////////////////////////////////////////////////
// settings for the tab menu
eFapsTree.prototype.imageTabArrowMinus                  = eFapsTreeImagePath+"/eFapsTreeTabArrowMinus.gif";
eFapsTree.prototype.imageTabArrowPlus                   = eFapsTreeImagePath+"/eFapsTreeTabArrowPlus.gif";
eFapsTree.prototype.imageTabEmpty                       = eFapsTreeImagePath+"/eFapsTreeEmpty.gif";
eFapsTree.prototype.imageTabTop                         = eFapsTreeImagePath+"/eFapsTreeTabTop.gif";
eFapsTree.prototype.imageTabTopRight                    = eFapsTreeImagePath+"/eFapsTreeTabTopRight.gif";
eFapsTree.prototype.imageTabRight                       = eFapsTreeImagePath+"/eFapsTreeTabRight.gif";
eFapsTree.prototype.imageTabRightLine                   = eFapsTreeImagePath+"/eFapsTreeTabRightLine.gif";
eFapsTree.prototype.imageTabRightEnd                    = eFapsTreeImagePath+"/eFapsTreeTabRightEnd.gif";
eFapsTree.prototype.classNameTabText                    = "eFapsTreeTab";
eFapsTree.prototype.classNameTabTextHasChildNodes       = "eFapsTreeTabHCN";
eFapsTree.prototype.classNameTabTextFocus               = "eFapsTreeTabFocus";
eFapsTree.prototype.classNameTabTextHasChildNodesFocus  = "eFapsTreeTabHCNFocus";

eFapsTree.prototype.classNameTabTable                   = "eFapsTreeTab";
eFapsTree.prototype.classNameTabTableColumnTop          = "eFapsTreeTabTop";
eFapsTree.prototype.classNameTabTableImageTop           = "eFapsTreeTabTop";
eFapsTree.prototype.classNameTabTableColumnTopRight     = "eFapsTreeTabTopRight";
eFapsTree.prototype.classNameTabTableImageTopRight      = "eFapsTreeTabTopRight";
eFapsTree.prototype.classNameTabTableColumn             = "eFapsTreeTab";
eFapsTree.prototype.classNameTabTableColumnRight        = "eFapsTreeTabRight";
eFapsTree.prototype.classNameTabTableImageRight         = "eFapsTreeTabRight";
eFapsTree.prototype.classNameTabTableImageRightLine     = "eFapsTreeTabRightLine";
eFapsTree.prototype.classNameTabTableImageRightEnd      = "eFapsTreeTabRightEnd";
eFapsTree.prototype.classNameTabImages                  = "eFapsTreeTabIcons";

///////////////////////////////////////////////////////////////////////////////
// settings for the type tree menu
eFapsTree.prototype.imageMinus                          = eFapsTreeImagePath+"/eFapsTreeMinus.gif";
eFapsTree.prototype.imageMinusBottom                    = eFapsTreeImagePath+"/eFapsTreeMinusBottom.gif";
eFapsTree.prototype.imagePlus                           = eFapsTreeImagePath+"/eFapsTreePlus.gif";
eFapsTree.prototype.imagePlusBottom                     = eFapsTreeImagePath+"/eFapsTreePlusBottom.gif";
eFapsTree.prototype.imageJoin                           = eFapsTreeImagePath+"/eFapsTreeJoin.gif";
eFapsTree.prototype.imageJoinBottom                     = eFapsTreeImagePath+"/eFapsTreeJoinBottom.gif";
eFapsTree.prototype.imageLine                           = eFapsTreeImagePath+"/eFapsTreeLine.gif";
eFapsTree.prototype.imageEmpty                          = eFapsTreeImagePath+"/eFapsTreeEmpty.gif";
eFapsTree.prototype.imageTreeRemove                     = eFapsTreeImagePath+"/eFapsTreeRemove.gif";
eFapsTree.prototype.classNameTreeText                   = "eFapsTreeNav";
eFapsTree.prototype.classNameTreeTextHasChildNodes      = "eFapsTreeNavHCN";
eFapsTree.prototype.classNameTreeTextFocus              = "eFapsTreeNavFocus";
eFapsTree.prototype.classNameTreeTextHasChildNodesFocus = "eFapsTreeNavHCNFocus";
eFapsTree.prototype.classNameTreeImages                 = "eFapsTreeNavImages";
eFapsTree.prototype.classNameTreeImages4Text            = "eFapsTreeNavImages4Text";
eFapsTree.prototype.classNameTreeTypeImages             = 'eFapsTreeTypeImages';

///////////////////////////////////////////////////////////////////////////////
// settings for the main toolbar menu
eFapsTree.prototype.classNameMainToolBarImages          = "eFapsMainToolBarImages";

///////////////////////////////////////////////////////////////////////////////
// settings for the normal menu
eFapsTree.prototype.imageMenuTitleSubMenu               = eFapsTreeImagePath+"/eFapsTreeMenuTitleSubMenu.gif";
eFapsTree.prototype.imageMenuColumnNullImage            = eFapsTreeImagePath+"/eFapsTreeMenuColumnNullImage.gif";
eFapsTree.prototype.imageMenuColumnSubMenu              = eFapsTreeImagePath+"/eFapsTreeMenuColumnSubMenu.gif";
eFapsTree.prototype.classNameMenu                       = "eFapsMenu";
eFapsTree.prototype.classNameMenuTitle                  = "eFapsMenuTitle";
eFapsTree.prototype.classNameMenuIsolator               = "eFapsMenuIsolator";
eFapsTree.prototype.classNameMenuSubMenu                = "eFapsMenuSubMenu";
eFapsTree.prototype.classNameMenuSubMenuRow             = "eFapsMenuSubMenuRow";
eFapsTree.prototype.classNameMenuSubMenuRowSelected     = "eFapsMenuSubMenuRowSelected";
eFapsTree.prototype.classNameMenuColumnImage            = "eFapsMenuColumnImage";
eFapsTree.prototype.classNameMenuColumnText             = "eFapsMenuColumnText";
eFapsTree.prototype.classNameMenuColumnSubMenu          = "eFapsMenuColumnSubMenu";


///////////////////////////////////////////////////////////////////////////////

/**
 * The static variable stores the focused object to 'unfocus' the old
 * selected object.
 */
eFapsTree.prototype.focusedObject;

/**
 * The static variable stores the numbers of stores trees.
 */
eFapsTree.prototype.counter = 1;

/**
 * The static variable stores all trees for direct accessing.
 */
eFapsTree.prototype.allTrees = new Array();

///////////////////////////////////////////////////////////////////////////////

/**
 * Test, if a sub tree node exists with the given id.
 *
 * @return <i>true</i> if sub tree node with <i>_id</i> exists, otherwise
 *         <i>false</i>
 * @param _id id for which the sub tree node is searched
 */
eFapsTree.prototype.hasSubNodeWithId = function(_id) {
  var ret = false;
  for (var i=0; i<this.subTree.length; i++)  {
    if (this.subTree[i].id && this.subTree[i].id==_id)  {
      ret = true;
      break;
    }
  }
  return ret;
}

/**
 * The method returns the sub tree node with the given id.
 *
 * @return sub tree node with <i>_id</i>, otherwise null
 * @param _id id for which the sub tree node is searched
 */
eFapsTree.prototype.getSubNodeWithId = function(_id) {
  var ret;
  for (var i=0; i<this.subTree.length; i++)  {
    if (this.subTree[i].id && this.subTree[i].id==_id)  {
      ret = this.subTree[i];
      break;
    }
  }
  return ret;
}

/**
 * The method returns the index number (position in the array of the parent
 * node).
 *
 * @return index number
 */
eFapsTree.prototype.getIndex = function()  {
  var index = -1;
  for (var i=0; i<this.parent.subTree.length; i++)  {
    if (this.parent.subTree[i] == this)  {
      index = i;
      break;
    }
  }
  return index;
}

/**
 * The method sets the icon of the menu / command.
 *
 * @param _icon (String) icon url
 */
eFapsTree.prototype.setIcon = function(_icon)  {
  this.icon = _icon;
}

/**
 * The method sets the url of the menu / command.
 *
 * @param _url (String) url
 * @see #setJavaScript
 */
eFapsTree.prototype.setUrl = function(_url)  {
  this.url = _url;
}

/**
 * Instead of an url a javascript funtion can be called.
 *
 * @param _javaScript (String) javascript function to call
 * @see #setUrl
 */
eFapsTree.prototype.setJavaScript = function(_javaScript)  {
  this.javaScript = _javaScript;
}

/**
 * The method sets the target of the action of the menu / command.
 *
 * @param _target (String) target ('Content' or 'Popup')
 */
eFapsTree.prototype.setTarget = function(_target)  {
  this.target = _target;
}

/**
 * The method sets the target width of the action of the menu / command.
 *
 * @param _width  (Integer) window width
 */
eFapsTree.prototype.setWidth = function(_width)  {
  this.width = _width;
}

/**
 * The method sets the target height of the action of the menu / command.
 *
 * @param _height (Integer) window height
 */
eFapsTree.prototype.setHeight = function(_height)  {
  this.height = _height;
}

/**
 * The method sets the question of the action of the menu / command. The 
 * question is asked before an sction is executed.
 *
 * @param _question (String) question to ask
 */
eFapsTree.prototype.setQuestion = function(_question)  {
  this.question = _question;
}

/**
 * The methods sets that the command submits the form.
 *
 * @param _submit (Boolean) <i>true</i> means, the command submits the form,
 *                          otherwise only  a new window is opened
 */
eFapsTree.prototype.setSubmit = function(_submit)  {
  this.submit = _submit;
}


/**
 * The methods sets the form submitted by this command.
 *
 * @param _submitForm (Form) form to submit
 * @see #setSubmit
 */
eFapsTree.prototype.setSubmitForm = function(_submitForm)  {
  this.submitForm = _submitForm;
}

/**
 * If an isolator is set for this command / menu, a line is shown on the right
 * of this command (works only for menus!).
 *
 * @param _isolator (Boolean) <i>true</i> means an isolator is shown
 */
eFapsTree.prototype.setIsolator = function(_isolator)  {
  this.isolator = _isolator;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// show methods for the navigator view (type tree)

/**
 * create in _obj the tree.
 */
eFapsTree.prototype.createInTree = function(_obj) {
  this.shown = true;
  this.isTree = true;

  this.div = document.createElement("div");
  _obj.appendChild(this.div);
  this.showText(this.div);

  // append new line
  var br = document.createElement("br");
  this.div.appendChild(br);

  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showTree(this.div);
  }

}

/**
 * @param _obj    (node)    parent div html object
 */
eFapsTree.prototype.showTree = function(_obj)  {
  this.shown = true;
  this.isTree = true;

  // span object to add all depending object for this sub tree
  this.spanAll = document.createElement("span");
  _obj.appendChild(this.spanAll);

  // append empty icons for levels
  var imageOld;
  var i = this.level-1;
  for (var par=this.parent; par.parent; par=par.parent)  {
    var image = document.createElement("img");
    image.className = eFapsTree.prototype.classNameTreeImages;
    if (par.isLastSibling)  {
        image.src =   eFapsTree.prototype.imageEmpty;
    } else  {
        image.src =   eFapsTree.prototype.imageLine;
    }
//    image.align = "absbottom";
    image.alt = this.text;
    if (imageOld)  {
      this.spanAll.insertBefore(image, imageOld);
    } else  {
      this.spanAll.appendChild(image);
    }
    imageOld = image;
    this.spaceImages[i] = image;
    i--;
  }

  // create a href for + / - icon
  this.imageHRef = document.createElement("a");
  if (this.subTree.length>0)  {
    this.imageHRef.href = "javascript:eFapsTreeDummy()";
    this.imageHRef.onclick = function() {this.menuObject.nodeOpenClose();};
    this.imageHRef.menuObject = this;
  }
  this.spanAll.appendChild(this.imageHRef);

  // append normal icon
  this.image = document.createElement("img");
  this.image.className = eFapsTree.prototype.classNameTreeImages;
  if (this.isLastSibling)  {
    if (this.subTree.length==0)  {
      this.image.src = eFapsTree.prototype.imageJoin;
    } else  {
      this.image.src = eFapsTree.prototype.imageMinus;
    }
  } else  {
    if (this.subTree.length==0)  {
      this.image.src = eFapsTree.prototype.imageJoinBottom;
    } else  {
      this.image.src = eFapsTree.prototype.imageMinusBottom;
    }
  }
//  this.image.align = "absbottom";
  this.image.alt = this.text;
  this.imageHRef.appendChild(this.image);

  this.showText(this.spanAll);

if (this.isType)  {
  var spanImages = document.createElement("span");
  spanImages.className = eFapsTree.prototype.classNameTreeTypeImages;
  this.spanAll.appendChild(spanImages);

  var aRemove = document.createElement("a");
  aRemove.href = "javascript:eFapsTreeDummy()";
  aRemove.onclick = function() {this.menuObject.remove(this);};
  aRemove.menuObject = this;
  spanImages.appendChild(aRemove);
  var imgRemove = document.createElement("img");
  imgRemove.src = eFapsTree.prototype.imageTreeRemove;
  imgRemove.className = eFapsTree.prototype.classNameTreeTypeImages;
  aRemove.appendChild(imgRemove);
}

  // append new line
  var br = document.createElement("br");
  this.spanAll.appendChild(br);

  this.div = document.createElement("div");
  this.spanAll.appendChild(this.div);

  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showTree(this.div);
  }
}

/**
 * append text
 */
eFapsTree.prototype.showText = function(_obj)  {
  if (this.url && this.url.length>0)  {
    this.domText = document.createElement("a");
if (this.target)  {
    this.domText.href = "javascript:eFapsTreeDummy()";
} else  {
    this.domText.href = this.url;
}
    // onclick function to focus the selected item
    this.domText.onclick = function() {this.menuObject.makeFocus();};
    // reference to this
    this.domText.menuObject = this;
    // reference to focus object
    this.makeFocusObject = this.domText;
  } else  {
    this.domText = document.createElement("span");
  }
  this.domText.className = eFapsTree.prototype.classNameTreeText;
  _obj.appendChild(this.domText);
  if (this.icon && this.icon.length>0)  {
    var icon4Type = document.createElement("img");
    icon4Type.src = this.icon;
    icon4Type.className = eFapsTree.prototype.classNameTreeImages4Text;
    icon4Type.border = 0;
    this.domText.appendChild(icon4Type);
    var text = document.createTextNode(" ");
    this.domText.appendChild(text);
  }
  var text = document.createTextNode(this.text);
  this.domText.appendChild(text);
  if (this.subTree.length>0)  {
    this.domText.className = eFapsTree.prototype.classNameTreeTextHasChildNodes;
  }
}

/**
 * The method focus the current object. The method is called from the
 * <i>onClick</i> event.
 */
eFapsTree.prototype.makeFocusTree = function()  {
  this.makeFocusObject.blur();
  if (eFapsTree.prototype.focusedObject)  {
    // for focused tree text
    if (eFapsTree.prototype.focusedObject.className == eFapsTree.prototype.classNameTreeTextFocus)  {
      eFapsTree.prototype.focusedObject.className = eFapsTree.prototype.classNameTreeText;
    // for focused tree text child noded
    } else if (eFapsTree.prototype.focusedObject.className == eFapsTree.prototype.classNameTreeTextHasChildNodesFocus)  {
      eFapsTree.prototype.focusedObject.className = eFapsTree.prototype.classNameTreeTextHasChildNodes;
    }
  }
  // for focused tree text
  if (this.makeFocusObject.className == eFapsTree.prototype.classNameTreeText)  {
    this.makeFocusObject.className = eFapsTree.prototype.classNameTreeTextFocus;
  // for focused tree text child nodes
  } else if (this.makeFocusObject.className == eFapsTree.prototype.classNameTreeTextHasChildNodes)  {
    this.makeFocusObject.className = eFapsTree.prototype.classNameTreeTextHasChildNodesFocus;
  }
  eFapsTree.prototype.focusedObject = this.makeFocusObject;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// show methods for the tab view

/**
 * create in _obj the tree.
 */
eFapsTree.prototype.createInTab = function(_obj) {
  this.shown = true;

  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showTabTop(_obj);
  }
}

/**
 * The method shows the top line with the text of a navigator tree.
 *
 * @param _obj  html object to appped the childs
 */
eFapsTree.prototype.showTabTop = function(_obj) {
  this.isTab = true;

  // create table
  var topTable = document.createElement("table");
  topTable.className = eFapsTree.prototype.classNameTabTable;
  _obj.appendChild(topTable);

  // create body
  var topBody = document.createElement("tbody");
  topTable.appendChild(topBody);

  // create row with two columns
  var topRow = document.createElement("tr");      topBody.appendChild(topRow);
  var topColumn1 = document.createElement("td");  topRow.appendChild(topColumn1);
  var topColumn2 = document.createElement("td");  topRow.appendChild(topColumn2);
  topColumn1.className = eFapsTree.prototype.classNameTabTableColumnTop;
  topColumn2.className = eFapsTree.prototype.classNameTabTableColumnTopRight;

  // append top image for first column
  var topImg = document.createElement("img");
  topImg.src = eFapsTree.prototype.imageTabTop;
  topImg.className = eFapsTree.prototype.classNameTabTableImageTop;
  topColumn1.appendChild(topImg);

  // append top right image
  var rightImg = document.createElement("img");
  rightImg.src = eFapsTree.prototype.imageTabTopRight;
  rightImg.className = eFapsTree.prototype.classNameTabTableImageTopRight;
  topColumn2.appendChild(rightImg);

  // create new row for text
  topRow = document.createElement("tr");          topBody.appendChild(topRow);
  topColumn1 = document.createElement("td");      topRow.appendChild(topColumn1);
  topColumn2 = document.createElement("td");      topRow.appendChild(topColumn2);
  topColumn1.className = eFapsTree.prototype.classNameTabTableColumn;
  topColumn2.className = eFapsTree.prototype.classNameTabTableColumnRight;

  // append right image
  rightImg = document.createElement("img");
  rightImg.src = eFapsTree.prototype.imageTabRight;
  rightImg.className = eFapsTree.prototype.classNameTabTableImageRight;
  topColumn2.appendChild(rightImg);
  rightImg = document.createElement("img");
  rightImg.src = eFapsTree.prototype.imageTabRightLine;
  rightImg.className = eFapsTree.prototype.classNameTabTableImageRightLine;
  topColumn2.appendChild(rightImg);
  rightImg = document.createElement("img");
  rightImg.src = eFapsTree.prototype.imageTabRightEnd;
  rightImg.className = eFapsTree.prototype.classNameTabTableImageRightEnd;
  topColumn2.appendChild(rightImg);

  // create a href for + / - icon
  var a = document.createElement("a");
  a.href = "javascript:eFapsTreeDummy()";
  a.onclick = function() {this.menuObject.nodeOpenClose();};
  a.menuObject = this;
  topColumn1.appendChild(a);

  // append normal icon
  this.image = document.createElement("img");
  this.image.className = eFapsTree.prototype.classNameTabImages;
  this.image.src = eFapsTree.prototype.imageTabArrowMinus;
//  this.image.align = "absbottom";
  this.image.alt = this.text;
  a.appendChild(this.image);

  // append text
  var text = document.createTextNode(this.text);
  if (this.url && this.url.length>0)  {
    this.p4text = document.createElement("a");
if (this.target)  {
    this.p4text.href = "javascript:eFapsTreeDummy()";
} else  {
    this.p4text.href = this.url;
}
this.p4text.menuObject = this;
  } else  {
    this.p4text = document.createElement("span");
  }
  this.p4text.className = eFapsTree.prototype.classNameTabText;
  topColumn1.appendChild(this.p4text);
  this.p4text.appendChild(text);

  // new row for sub tree
  subRow = document.createElement("tr");
  topBody.appendChild(subRow);
  var subCol = document.createElement("td");
  subCol.colSpan = 2;
  subRow.appendChild(subCol);

  // div element for sub tree
  this.div = document.createElement("div");
  subCol.appendChild(this.div);

  // show all sub trees
  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showTab(this.div);
  }
  if (this.subTree.length==0)  {
    this.image.style.display = 'none';
    this.p4text.className = eFapsTree.prototype.classNameTabTextHasChildNodes;
  }

  // hide sub trees
  this.nodeOpenClose();
}


/**
 * The method shows the sub trees of the navigator tree.
 */
eFapsTree.prototype.showTab = function(_parent) {
  this.shown = true;
  this.isTab = true;

  // append empty icons for levels
  for (var i=1; i<this.level; i++)  {
    var image = document.createElement("img");
    image.className = eFapsTree.prototype.classNameTabImages;
    image.src =   eFapsTree.prototype.imageTabEmpty;
//    image.align = "absbottom";
    image.alt = this.text;
    _parent.appendChild(image);
  }

  // create a href for + / - icon
  var a = document.createElement("a");
  a.href = "javascript:eFapsTreeDummy()";
  a.onclick = function() {this.menuObject.nodeOpenClose();};
  a.menuObject = this;
  _parent.appendChild(a);

  // append normal icon
  this.image = document.createElement("img");
  this.image.className = eFapsTree.prototype.classNameTabImages;
  this.image.src = eFapsTree.prototype.imageTabArrowMinus;
//  this.image.align = "absbottom";
  this.image.alt = this.text;
  a.appendChild(this.image);

  // append text
  var text = document.createTextNode(this.text);
  if (this.url && this.url.length>0)  {
    this.p4text = document.createElement("a");
if (this.target)  {
    this.p4text.href = "javascript:eFapsTreeDummy()";
} else  {
    this.p4text.href = this.url;
}
    // onclick function to focus the selected item
    this.p4text.onclick = function() {this.menuObject.makeFocus();};
    // reference to this
    this.p4text.menuObject = this;
    // reference to focus object
    this.makeFocusObject = this.p4text;
  } else  {
    this.p4text = document.createElement("span");
  }
  this.p4text.className = eFapsTree.prototype.classNameTabText;
  _parent.appendChild(this.p4text);
  this.p4text.appendChild(text);

  // append new line
  var br = document.createElement("br");
  _parent.appendChild(br);

  // append div element for sub tree
  this.div = document.createElement("div");
  _parent.appendChild(this.div);

  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showTab(this.div);
  }
  if (this.subTree.length==0)  {
    this.image.style.display = 'none';
    this.p4text.className = eFapsTree.prototype.classNameTabTextHasChildNodes;
  }
}

/**
 * The method focus the current object. The method is called from the
 * <i>onClick</i> event.
 */
eFapsTree.prototype.makeFocusTab = function()  {
  this.makeFocusObject.blur();
  if (eFapsTree.prototype.focusedObject)  {
    // for focused tab text
    if (eFapsTree.prototype.focusedObject.className == eFapsTree.prototype.classNameTabTextFocus)  {
      eFapsTree.prototype.focusedObject.className = eFapsTree.prototype.classNameTabText;
    // for focused tab text child nodes
    } else if (eFapsTree.prototype.focusedObject.className == eFapsTree.prototype.classNameTabTextHasChildNodesFocus)  {
      eFapsTree.prototype.focusedObject.className = eFapsTree.prototype.classNameTabTextHasChildNodes;
    }
  }
  // for focused tab text
  if (this.makeFocusObject.className == eFapsTree.prototype.classNameTabText)  {
    this.makeFocusObject.className = eFapsTree.prototype.classNameTabTextFocus;
  // for focused tab text child nodes
  } else if (this.makeFocusObject.className == eFapsTree.prototype.classNameTabTextHasChildNodes)  {
    this.makeFocusObject.className = eFapsTree.prototype.classNameTabTextHasChildNodesFocus;
  }
  eFapsTree.prototype.focusedObject = this.makeFocusObject;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// show methods for the main toolbar menu

/**
 * create in _obj the tree.
 */
eFapsTree.prototype.createInMainToolBar = function(_obj) {
  this.shown = true;
  this.isToolBar = true;

  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showMainToolBar(_obj);
  }
}

/**
 * @param _obj  (HTMLObject)  html object where to add one toolbar object
 */
eFapsTree.prototype.showMainToolBar = function(_obj) {
  this.shown = true;
  this.isToolBar = true;

  // create a href for + / - icon
  var a = document.createElement("a");
  a.href = "javascript:eFapsTreeDummy()";
  a.onclick = function() {this.menuObject.makeFocus();};
  a.menuObject = this;
  _obj.appendChild(a);

  // append normal icon
  this.image = document.createElement("img");
  this.image.className = eFapsTree.prototype.classNameMainToolBarImages;
  this.image.src = this.icon;
  this.image.alt = this.text;
  a.appendChild(this.image);
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// show methods for the menu

/**
 * create in _obj the tree.
 */
eFapsTree.prototype.createInMenu = function(_obj) {
  this.shown = true;
  this.isMenu = true;

  this.mainMenu = document.createElement("span");
  this.mainMenu.className = eFapsTree.prototype.classNameMenu;
  _obj.appendChild(this.mainMenu);

  for (var i=0; i<this.subTree.length; i++)  {
    this.subTree[i].showMenu(this, this.mainMenu, _obj);
    if (this.subTree[i].isolator)  {
      var isolator = document.createElement("span");
      isolator.className = eFapsTree.prototype.classNameMenuIsolator;
      this.mainMenu.appendChild(isolator);
    }
  }

  this.clean = function()  {
    this.mainMenu.parentNode.removeChild(this.mainMenu);
    this.shown = false;
    this.isMenu = false;
    this.mainMenu = null;
    this.subTree = new Array();
  }
}

/**
 * @param _obj  (HTMLObject)  html object where to add one toolbar object
 */
eFapsTree.prototype.showMenu = function(_rootNode, _obj, _rootHtmlObj) {
  this.shown = true;
  this.isMenu = true;
  this.rootNode = _rootNode;


this.menu = document.createElement("span");
if (this.javaScript || this.url)  {
  this.menu.onclick = function() {this.menuObject.makeFocus();};
}
this.menu.onmouseover = function() {
  this.menuObject.viewMenu();
tmotemp=this.menuObject;
tmotemp2=false;
window.setTimeout("test()",2000);
};

this.menu.menuObject = this;
_obj.appendChild(this.menu);

  // create a href for + / - icon
  this.a = document.createElement("a");
  this.a.href = "javascript:eFapsTreeDummy()";
  this.a.className = eFapsTree.prototype.classNameMenuTitle;
this.menu.appendChild(this.a);

  // append text
  if (this.text)  {
    this.a.appendChild(document.createTextNode(this.text));
  }

  // append icon
  if (this.icon)  {
    var img = document.createElement("img");
    img.src = this.icon;
    this.a.appendChild(img);
  }

if (this.subTree.length>0)  {

  // append arrow down
  var img = document.createElement("img");
  img.src = eFapsTree.prototype.imageMenuTitleSubMenu;
  this.a.appendChild(img);

  this.subMenu = document.createElement("table");
  this.subMenu.style.display = 'none';
  this.subMenu.className = eFapsTree.prototype.classNameMenuSubMenu;
//  this.subMenu.onmouseout = function() {
//    if (!this.menuObject.selectedMenu || !this.menuObject.selectedMenu.subMenu)  {
//      this.menuObject.hideMenu();
//    }
//  };
//this.subMenu.onmouseover = function(_event) {this.menuObject.viewMenu(_event);};
  this.subMenu.menuObject = this;
  this.menu.appendChild(this.subMenu);

var tbody = document.createElement("tbody");
this.subMenu.appendChild(tbody);

  for (var i=0; i<this.subTree.length; i++)  {
    var tr = document.createElement("tr");
    this.subTree[i].showMenu2(this.rootNode, tr, _rootHtmlObj);
    tbody.appendChild(tr);
  }
}


}

var tmotemp = null;
var tmotemp2 = false;
function test()  {
  if (tmotemp!=null)  {
    if (tmotemp2==true)  {
      tmotemp2 = false;
      window.setTimeout("test()",2000);
    } else  {
      tmotemp.hideMenu();
    }
  }
}

/**
 * @param _obj  (HTMLObject)  html object where to add one toolbar object
 */
eFapsTree.prototype.showMenu2 = function(_rootNode, _obj, _rootHtmlObj) {

  this.row = _obj;
  this.row.className = eFapsTree.prototype.classNameMenuSubMenuRow;
  this.row.menuObject = this;
  this.row.onmouseover = function() {
    if (this.menuObject.parent.selectedMenu && this.menuObject.parent.selectedMenu!=null && this.menuObject.parent.selectedMenu!=this.menuObject)  {
      this.menuObject.parent.selectedMenu.row.className = eFapsTree.prototype.classNameMenuSubMenuRow;
      this.menuObject.parent.selectedMenu.hideMenu();
    }
    this.className = eFapsTree.prototype.classNameMenuSubMenuRowSelected;
    this.menuObject.parent.selectedMenu = this.menuObject;
tmotemp2=true;
  };

//  this.row.onmouseout = function() {
//    if (!this.menuObject.shownMenu || this.menuObject.shownMenu==null)  {
//      this.className = 'eFapsMenuSubMenuRow';
//    }
//  };


  this.shown = true;
  this.isMenu = true;
  this.rootNode = _rootNode;

  // append column for images / status etc.
  this.columnImage = document.createElement("td");
  this.columnImage.className = eFapsTree.prototype.classNameMenuColumnImage;
  var icon = document.createElement("img");
  if (this.icon)  {
    icon.src = this.icon;
  } else  {
    icon.src = eFapsTree.prototype.imageMenuColumnNullImage;
  }
  this.columnImage.appendChild(icon);
  _obj.appendChild(this.columnImage);

  // append text column (with href)
  this.columnText = document.createElement("td");
  this.columnText.className = eFapsTree.prototype.classNameMenuColumnText;
  if (this.subTree.length>0)  {
    this.columnText.appendChild(document.createTextNode(this.text));
  } else  {
    var a = document.createElement("a");
    a.href = "javascript:eFapsTreeDummy()";
    a.onclick = function() {this.menuObject.makeFocus();};
    a.menuObject = this;
    a.appendChild(document.createTextNode(this.text));
    this.columnText.appendChild(a);
  }
  _obj.appendChild(this.columnText);
  
  // append submenu column
  this.columnSubMenu = document.createElement("td");
  this.columnSubMenu.className = eFapsTree.prototype.classNameMenuColumnSubMenu;
  _obj.appendChild(this.columnSubMenu);


if (this.subTree.length>0)  {

  var subMenuImg = document.createElement("img");
  subMenuImg.src = eFapsTree.prototype.imageMenuColumnSubMenu;
  this.columnSubMenu.appendChild(subMenuImg);

  this.row.onclick = function()  {
    this.menuObject.viewSubMenu();
  }


  this.subMenu = document.createElement("table");

  this.subMenu.style.display = 'none';
  this.subMenu.className = eFapsTree.prototype.classNameMenuSubMenu;
//  this.subMenu.onmouseout = function() {
//    this.menuObject.hideMenu();
//  };
  //this.subMenu.onmouseover = function(_event) {this.menuObject.viewMenu(_event);};
  this.subMenu.menuObject = this;
  _rootHtmlObj.appendChild(this.subMenu);

  var tbody = document.createElement("tbody");
  this.subMenu.appendChild(tbody);

  for (var i=0; i<this.subTree.length; i++)  {
    var tr = document.createElement("tr");
    this.subTree[i].showMenu2(this.rootNode, tr);
    tbody.appendChild(tr);
  }
}


}


eFapsTree.prototype.viewSubMenu = function(_event)  {
  if (this.subMenu && (!this.parent.shownMenu || (this.parent.shownMenu!=null && this.parent.shownMenu!=this)))  {
    if (this.parent.shownMenu && this.parent.shownMenu!=null)  {
      this.parent.shownMenu.hideMenu();
    }

    var x = -11;
    var y = 0;

    var parentObj = this;
    while (!parentObj.a)  {
      x += parentObj.columnSubMenu.offsetWidth;
      y += parentObj.parent.subMenu.offsetHeight;
      parentObj = parentObj.parent;
    }
    x += parentObj.a.offsetLeft + parentObj.subMenu.offsetWidth;
    y += parentObj.a.offsetTop + parentObj.a.offsetHeight + this.getIndex() * this.row.offsetHeight;

    this.subMenu.style.left = x;
    this.subMenu.style.top = y;

    this.subMenu.style.display = '';
    this.parent.shownMenu = this;
  }
}



eFapsTree.prototype.viewMenu = function(_event)  {
  if (this.subMenu && (!this.parent.shownMenu || (this.parent.shownMenu!=null && this.parent.shownMenu!=this)))  {
    if (this.parent.shownMenu && this.parent.shownMenu!=null)  {
      this.parent.shownMenu.hideMenu();
    }
    this.subMenu.style.left = this.a.offsetLeft;
    this.subMenu.style.top = this.a.offsetTop + this.a.offsetHeight;
    this.subMenu.style.display = '';
    this.parent.shownMenu = this;
  } else if (!this.subMenu && this.parent.shownMenu && this.parent.shownMenu!=null)  {
    this.parent.shownMenu.hideMenu();
    this.parent.shownMenu = null;
  }
}

eFapsTree.prototype.hideMenu = function()  {
  if (this.subMenu)  {
    this.subMenu.style.display = 'none';
    for (var i=0; i<this.subTree.length; i++)  {
      this.subTree[i].hideMenu();
    }
    if (this.selectedMenu)  {
      this.selectedMenu.row.className = 'eFapsMenuSubMenuRow';
      this.selectedMenu = null;
    }
    this.parent.shownMenu = null;
  }
}

/**
 * After the user selects a command, all open menus must be hide!
 */
eFapsTree.prototype.makeFocusMenu = function()  {
  var rootMenu = this.parent;
  while (rootMenu.parent)  {
    rootMenu = rootMenu.parent;
  }
  for (var i=0; i<rootMenu.subTree.length; i++)  {
    rootMenu.subTree[i].hideMenu();
//rootMenu.subTree[i].menu.blur();
  }
  if (this.a)  {
    this.a.blur();
  }
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

/**
 * Add a new sub tree node to a node. The new created tree node is returned.
 *
 * @return new created tree node
 * @param _id     Identifier for the tree node
 * @param _text   text for the sub tree node
 * @param _url    url of the text for the sub tree node
 * @param _icon   icon url for the sub tree node
 */
eFapsTree.prototype.addSubNode = function(_id, _text, _isType, _url, _icon, _content, _width, _height) {
  var newTree = new eFapsTree(_id, _text, _isType, _url, _icon, _content, _width, _height, this.level+1, this);
  if (this.subTree.length>0)  {
    newTree.previous = this.subTree[this.subTree.length-1];
    newTree.previous.isLastSibling = false;
    newTree.previous.next = newTree;
  }
  this.subTree[this.subTree.length] = newTree;
  if (this.shown)  {
    this.show4addSubNode(newTree);
  }
  return newTree;
}

/**
 * The method is called from {@link #addSubNode} in the same <i>this</i>
 * context. The calling of the event handling is made here (with calling
 * method {@link #event4addSubNode} in the context of <i>_newTree</i>.
 *
 * @param _newTree  new added sub tree created in {@link #addSubNode}
 */
eFapsTree.prototype.show4addSubNode = function(_newTree) {
  _newTree.showTree(this.div);
  if (_newTree.previous)  {
    _newTree.previous.event4addSubNode(_newTree.level);
  } else  {
    if (this.isLastSibling)  {
      this.image.src = eFapsTree.prototype.imageMinus;
    } else  {
      this.image.src = eFapsTree.prototype.imageMinusBottom;
    }
    this.domText.className = eFapsTree.prototype.classNameTreeTextHasChildNodes;
    this.imageHRef.href = "javascript:eFapsTreeDummy()";
    this.imageHRef.onclick = function() {this.menuObject.nodeOpenClose();};
    this.imageHRef.menuObject = this;
  }
}

/**
 * This function is called as an event whil adding a new sub tree node. The
 * event is executed on the previous tree node.<br/>
 * The event is distributed to the sub nodes.<br/>
 * If it is the same level as new added tree node, the image of gets a bottom
 * line. If not, in the space images a line icon is replaced instead of an
 * empty icon.
 *
 * @param _level  level of the added node
 */
eFapsTree.prototype.event4addSubNode = function(_level) {
  for (var i = 0; i<this.subTree.length; i++)  {
    this.subTree[i].event4addSubNode(_level);
  }
  if (this.level == _level)  {
    if (this.subTree.length==0)  {
      this.image.src = eFapsTree.prototype.imageJoinBottom;
    } else  {
      this.image.src = eFapsTree.prototype.imageMinusBottom;
//      this.domText.className = eFapsTree.prototype.classNameTreeTextHasChildNodes;
    }
  } else  {
    this.spaceImages[_level].src =   eFapsTree.prototype.imageLine;
  }
}

///////////////////////////////////////////////////////////////////////////////

/**
 * The method focus the current object. The method is called from the
 * <i>onClick</i> event.
 */
eFapsTree.prototype.makeFocus = function()  {
  if (this.isTree)  {
    this.makeFocusTree();
  } else if (this.isTab)  {
    this.makeFocusTab();
  } else if (this.isMenu)  {
    this.makeFocusMenu();
  }

  var bck = true;

  if (this.question)  {
    bck = confirm(this.question);
  }
  if (bck)  {
    if (this.javaScript)  {
      eval(this.javaScript);
    } else if (this.target=="Hidden")  {
      this.executeHidden();
    } else  {
      var url = this.url;
      if (this.isTree || this.isMenu)  {
        if (this.url.indexOf("?")>=0)  {
          url += "&nodeId=" + this.index;
        } else  {
          url += "?nodeId=" + this.index;
        }
      }
      if (this.submit)  {
        this.executeSubmit(url);
      } else  {
        eFapsCommonOpenUrl(url, this.target, this.width, this.height);
      }
    }
  }
}

/**
 * The method calls in the hidden frame the href.
 *
 * @see #execute
 */
eFapsTree.prototype.executeHidden = function()  {
  eFapsProcessStart();
  eFapsFrameHidden.location.href = this.url;
}


/**
 * The current form is submitted to the given href.
 *
 * @see #execute
 */
eFapsTree.prototype.executeSubmit = function(_url)  {
  eFapsProcessStart();
  this.submitForm.action = _url;
//      if (this.getSubmitForm().command)  {
//        this.getSubmitForm().command.value = this.name;
//      }
  this.submitForm.submit();
}

///////////////////////////////////////////////////////////////////////////////

/**
 * The instance method removes this sub tree node from the parent node.
 */
eFapsTree.prototype.remove = function(_object)  {
  if (_object)  {
    _object.blur();
  }

  // remove this tree node from all trees
  eFapsTree.prototype.allTrees[this.index] = null;

  // remove this tree node from parent tree node
  var newArray = new Array();
  for (var i = 0; i<this.parent.subTree.length; i++)  {
    if (this.parent.subTree[i].index != this.index)  {
      newArray[newArray.length] = this.parent.subTree[i];
    }
  }
  this.parent.subTree = newArray;

  // set the previous and next variables
  if (this.previous)  {
    this.previous.next = this.next;
  }
  if (this.next)  {
    this.next.previous = this.previous;
  }

  // change image from previous node if no next node is given
  if (this.previous && !this.next && this.previous.subTree.length>0)  {
    if (this.previous.div.style.display=='none')  {
      this.previous.image.src = eFapsTree.prototype.imagePlus;
    } else  {
      this.previous.image.src = eFapsTree.prototype.imageMinus;
    }
    this.previous.isLastSibling = true;

    this.previous._event4removeSubNode(this.level);
  }

//  this.parent.imageHRef.href = null;
  this.parent.imageHRef.onclick = null;

  // replace plus / minus imape with the join / join bottom image
  if (this.parent.isLastSibling)  {
    if (this.parent.subTree.length==0)  {
      this.parent.image.src = eFapsTree.prototype.imageJoin;
    } else  {
      this.parent.image.src = eFapsTree.prototype.imageMinus;
    }
  } else  {
    if (this.parent.subTree.length==0)  {
      this.parent.image.src = eFapsTree.prototype.imageJoinBottom;
    } else  {
      this.image.src = eFapsTree.prototype.imageMinusBottom;
    }
  }

  // make html remove and focus on parent node
  this.spanAll.parentNode.removeChild(this.spanAll);
  this.parent.makeFocus();

}

/**
 * This function is called as an event whil adding a new sub tree node. The
 * event is executed on the previous tree node.<br/>
 * The event is distributed to the sub nodes.<br/>
 * If it is the same level as new added tree node, the image of gets a bottom
 * line. If not, in the space images a line icon is replaced instead of an
 * empty icon.
 *
 * @param _level  level of the added node
 */
eFapsTree.prototype._event4removeSubNode = function(_level) {
  for (var i = 0; i<this.subTree.length; i++)  {
    this.subTree[i]._event4removeSubNode(_level);
  }
  if (this.level != _level)  {
    this.spaceImages[_level].src = eFapsTree.prototype.imageEmpty;
  }
}


/**
 * Opens or closes a node. Method is called from the a html element inside
 * the href statement. This is made by testing the display style. If it is
 * <i>none</i>, it is changed to nothing, if it is nothing, it is changed to
 * <i>none</i>. This changes of the display style is made on the <i>div</i>
 * html element which includes the html elements of all sub tree . A display
 * style of <i>none</i> means, that the complete html element is not seen in
 * the web browser.
 *
 * @see #nodeOpen
 * @see #nodeClose
 */
eFapsTree.prototype.nodeOpenClose = function()  {
  if (this.div.style.display == 'none') {
    this.nodeOpen();
  } else  {
    this.nodeClose();
  }
}

/**
 * The method opens a node. The depending images are changed and the display 
 * style of the div object is changed to nothing.
 *
 * @see #nodeOpenClose
 */
eFapsTree.prototype.nodeOpen = function()  {
  this.div.style.display = '';
  // tab menu
  if (this.isTab && this.image)  {
    this.image.src = eFapsTree.prototype.imageTabArrowMinus;
  // tree menu
  } else if (this.isTree && this.image)  {
    if (this.isLastSibling)  {
      this.image.src = eFapsTree.prototype.imageMinus;
    } else  {
      this.image.src = eFapsTree.prototype.imageMinusBottom;
    }
  }
}

/**
 * The method closes a node. The depending images are changed and the display 
 * style of the div object is changed to 'none'.
 *
 * @see #nodeOpenClose
 */
eFapsTree.prototype.nodeClose = function()  {
  this.div.style.display = 'none';
  // tab menu
  if (this.isTab && this.image)  {
    this.image.src = eFapsTree.prototype.imageTabArrowPlus;
  // tree menu
  } else if (this.isTree && this.image)  {
    if (this.isLastSibling)  {
      this.image.src = eFapsTree.prototype.imagePlus;
    } else  {
      this.image.src = eFapsTree.prototype.imagePlusBottom;
    }
  }
}

/**
 * The function is called if this node is selected. This means all previous 
 * and next nodes are closed, this node is opened and this node is focused.
 *
 * @see #nodeClose
 * @see #nodeOpen
 * @see #makeFocus
 */
eFapsTree.prototype.nodeSelect = function()  {
  this.makeFocus();
  if (this.subTree.length>0)  {
    this.nodeOpen();
  }
  var tmp = this.previous;
  while (tmp)  {
    if (tmp.subTree.length>0)  {
      tmp.nodeClose();
    }
    tmp = tmp.previous;
  }
  tmp = this.next;
  while (tmp)  {
    if (tmp.subTree.length>0)  {
      tmp.nodeClose();
    }
    tmp = tmp.next;
  }
}

///////////////////////////////////////////////////////////////////////////////

/**
 * The function is only a dummy function called from href dom objects so that 
 * no javascript error is thrown
 */
function eFapsTreeDummy()  {
}

