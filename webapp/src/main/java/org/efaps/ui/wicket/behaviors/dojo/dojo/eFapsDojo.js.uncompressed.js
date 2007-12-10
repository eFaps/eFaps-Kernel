/*
	Copyright (c) 2004-2007, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(!dojo._hasResource["dojo.cookie"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.cookie"] = true;
dojo.provide("dojo.cookie");

/*=====
dojo.__cookieProps = function(kwArgs){
	//	expires: Date|Number?
	//		If a number, seen as the number of days from today. If a date, the
	//		date past which the cookie is invalid. If expires is in the past,
	//		the cookie will be deleted If expires is left out or is 0, the
	//		cookie will expire when the browser closes.
	//	path: String?
	//		The path to use for the cookie.
	//	domain: String?
	//		The domain to use for the cookie.
	//	secure: Boolean?
	//		Whether to only send the cookie on secure connections
}
=====*/


dojo.cookie = function(/*String*/name, /*String?*/value, /*dojo.__cookieProps?*/props){
	//	summary: 
	//		Get or set a cookie.
	//	description:
	// 		If you pass in one argument, the the value of the cookie is returned
	//
	// 		If you pass in two arguments, the cookie value is set to the second
	// 		argument.
	//
	// 		If you pass in three arguments, the cookie value is set to the
	// 		second argument, and the options on the third argument are used for
	// 		extended properties on the cookie
	//	name:
	//		The name of the cookie
	//	value:
	//		Optional. The value for the cookie.
	//	props: 
	//		Optional additional properties for the cookie
	//	example:
	//		set a cookie with the JSON-serialized contents of an object which
	//		will expire 5 days from now:
	//	|	dojo.cookie("configObj", dojo.toJson(config), { expires: 5 });
	//	
	//	example:
	//		de-serialize a cookie back into a JavaScript object:
	//	|	var config = dojo.fromJson(dojo.cookie("configObj"));
	//	
	//	example:
	//		delete a cookie:
	//	|	dojo.cookie("configObj", null);
	var c = document.cookie;
	if(arguments.length == 1){
		var idx = c.lastIndexOf(name+'=');
		if(idx == -1){ return null; }
		var start = idx+name.length+1;
		var end = c.indexOf(';', idx+name.length+1);
		if(end == -1){ end = c.length; }
		return decodeURIComponent(c.substring(start, end)); 
	}else{
		props = props || {};
		value = encodeURIComponent(value);
		if(typeof(props.expires) == "number"){ 
			var d = new Date();
			d.setTime(d.getTime()+(props.expires*24*60*60*1000));
			props.expires = d;
		}
		document.cookie = name + "=" + value 
			+ (props.expires ? "; expires=" + props.expires.toUTCString() : "")
			+ (props.path ? "; path=" + props.path : "")
			+ (props.domain ? "; domain=" + props.domain : "")
			+ (props.secure ? "; secure" : "");
		return null;
	}
};

}

if(!dojo._hasResource["dijit._base.focus"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.focus"] = true;
dojo.provide("dijit._base.focus");

// summary:
//		These functions are used to query or set the focus and selection.
//
//		Also, they trace when widgets become actived/deactivated,
//		so that the widget can fire _onFocus/_onBlur events.
//		"Active" here means something similar to "focused", but
//		"focus" isn't quite the right word because we keep track of
//		a whole stack of "active" widgets.  Example:  Combobutton --> Menu -->
//		MenuItem.   The onBlur event for Combobutton doesn't fire due to focusing
//		on the Menu or a MenuItem, since they are considered part of the
//		Combobutton widget.  It only happens when focus is shifted
//		somewhere completely different.

dojo.mixin(dijit,
{
	// _curFocus: DomNode
	//		Currently focused item on screen
	_curFocus: null,

	// _prevFocus: DomNode
	//		Previously focused item on screen
	_prevFocus: null,

	isCollapsed: function(){
		// summary: tests whether the current selection is empty
		var _window = dojo.global;
		var _document = dojo.doc;
		if(_document.selection){ // IE
			return !_document.selection.createRange().text; // Boolean
		}else if(_window.getSelection){
			var selection = _window.getSelection();
			if(dojo.isString(selection)){ // Safari
				return !selection; // Boolean
			}else{ // Mozilla/W3
				return selection.isCollapsed || !selection.toString(); // Boolean
			}
		}
	},

	getBookmark: function(){
		// summary: Retrieves a bookmark that can be used with moveToBookmark to return to the same range
		var bookmark, selection = dojo.doc.selection;
		if(selection){ // IE
			var range = selection.createRange();
			if(selection.type.toUpperCase()=='CONTROL'){
				bookmark = range.length ? dojo._toArray(range) : null;
			}else{
				bookmark = range.getBookmark();
			}
		}else{
			if(dojo.global.getSelection){
				selection = dojo.global.getSelection();
				if(selection){
					var range = selection.getRangeAt(0);
					bookmark = range.cloneRange();
				}
			}else{
				console.debug("No idea how to store the current selection for this browser!");
			}
		}
		return bookmark; // Array
	},

	moveToBookmark: function(/*Object*/bookmark){
		// summary: Moves current selection to a bookmark
		// bookmark: this should be a returned object from dojo.html.selection.getBookmark()
		var _document = dojo.doc;
		if(_document.selection){ // IE
			var range;
			if(dojo.isArray(bookmark)){
				range = _document.body.createControlRange();
				dojo.forEach(bookmark, range.addElement);
			}else{
				range = _document.selection.createRange();
				range.moveToBookmark(bookmark);
			}
			range.select();
		}else{ //Moz/W3C
			var selection = dojo.global.getSelection && dojo.global.getSelection();
			if(selection && selection.removeAllRanges){
				selection.removeAllRanges();
				selection.addRange(bookmark);
			}else{
				console.debug("No idea how to restore selection for this browser!");
			}
		}
	},

	getFocus: function(/*Widget*/menu, /*Window*/ openedForWindow){
		// summary:
		//	Returns the current focus and selection.
		//	Called when a popup appears (either a top level menu or a dialog),
		//	or when a toolbar/menubar receives focus
		//
		// menu:
		//	the menu that's being opened
		//
		// openedForWindow:
		//	iframe in which menu was opened
		//
		// returns:
		//	a handle to restore focus/selection

		return {
			// Node to return focus to
			node: menu && dojo.isDescendant(dijit._curFocus, menu.domNode) ? dijit._prevFocus : dijit._curFocus,

			// Previously selected text
			bookmark:
				!dojo.withGlobal(openedForWindow||dojo.global, dijit.isCollapsed) ?
				dojo.withGlobal(openedForWindow||dojo.global, dijit.getBookmark) :
				null,

			openedForWindow: openedForWindow
		}; // Object
	},

	focus: function(/*Object || DomNode */ handle){
		// summary:
		//		Sets the focused node and the selection according to argument.
		//		To set focus to an iframe's content, pass in the iframe itself.
		// handle:
		//		object returned by get(), or a DomNode

		if(!handle){ return; }

		var node = "node" in handle ? handle.node : handle,		// because handle is either DomNode or a composite object
			bookmark = handle.bookmark,
			openedForWindow = handle.openedForWindow;

		// Set the focus
		// Note that for iframe's we need to use the <iframe> to follow the parentNode chain,
		// but we need to set focus to iframe.contentWindow
		if(node){
			var focusNode = (node.tagName.toLowerCase()=="iframe") ? node.contentWindow : node;
			if(focusNode && focusNode.focus){
				try{
					// Gecko throws sometimes if setting focus is impossible,
					// node not displayed or something like that
					focusNode.focus();
				}catch(e){/*quiet*/}
			}			
			dijit._onFocusNode(node);
		}

		// set the selection
		// do not need to restore if current selection is not empty
		// (use keyboard to select a menu item)
		if(bookmark && dojo.withGlobal(openedForWindow||dojo.global, dijit.isCollapsed)){
			if(openedForWindow){
				openedForWindow.focus();
			}
			try{
				dojo.withGlobal(openedForWindow||dojo.global, moveToBookmark, null, [bookmark]);
			}catch(e){
				/*squelch IE internal error, see http://trac.dojotoolkit.org/ticket/1984 */
			}
		}
	},

	// List of currently active widgets (focused widget and it's ancestors)
	_activeStack: [],

	registerWin: function(/*Window?*/targetWindow){
		// summary:
		//		Registers listeners on the specified window (either the main
		//		window or an iframe) to detect when the user has clicked somewhere.
		//		Anyone that creates an iframe should call this function.

		if(!targetWindow){
			targetWindow = window;
		}

		dojo.connect(targetWindow.document, "onmousedown", null, function(evt){
			dijit._justMouseDowned = true;
			setTimeout(function(){ dijit._justMouseDowned = false; }, 0);
			dijit._onTouchNode(evt.target||evt.srcElement);
		});
		//dojo.connect(targetWindow, "onscroll", ???);

		// Listen for blur and focus events on targetWindow's body
		var body = targetWindow.document.body || targetWindow.document.getElementsByTagName("body")[0];
		if(body){
			if(dojo.isIE){
				body.attachEvent('onactivate', function(evt){
					if(evt.srcElement.tagName.toLowerCase() != "body"){
						dijit._onFocusNode(evt.srcElement);
					}
				});
				body.attachEvent('ondeactivate', function(evt){ dijit._onBlurNode(evt.srcElement); });
			}else{
				body.addEventListener('focus', function(evt){ dijit._onFocusNode(evt.target); }, true);
				body.addEventListener('blur', function(evt){ dijit._onBlurNode(evt.target); }, true);
			}
		}
		body = null;	// prevent memory leak (apparent circular reference via closure)
	},

	_onBlurNode: function(/*DomNode*/ node){
		// summary:
		// 		Called when focus leaves a node.
		//		Usually ignored, _unless_ it *isn't* follwed by touching another node,
		//		which indicates that we tabbed off the last field on the page,
		//		in which case every widget is marked inactive
		dijit._prevFocus = dijit._curFocus;
		dijit._curFocus = null;

		var w = dijit.getEnclosingWidget(node);
		if (w && w._setStateClass){
			w._focused = false;
			w._setStateClass();
		}
		if(dijit._justMouseDowned){
			// the mouse down caused a new widget to be marked as active; this blur event
			// is coming late, so ignore it.
			return;
		}

		// if the blur event isn't followed by a focus event then mark all widgets as inactive.
		if(dijit._clearActiveWidgetsTimer){
			clearTimeout(dijit._clearActiveWidgetsTimer);
		}
		dijit._clearActiveWidgetsTimer = setTimeout(function(){
			delete dijit._clearActiveWidgetsTimer; dijit._setStack([]); }, 100);
	},

	_onTouchNode: function(/*DomNode*/ node){
		// summary
		//		Callback when node is focused or mouse-downed

		// ignore the recent blurNode event
		if(dijit._clearActiveWidgetsTimer){
			clearTimeout(dijit._clearActiveWidgetsTimer);
			delete dijit._clearActiveWidgetsTimer;
		}

		// compute stack of active widgets (ex: ComboButton --> Menu --> MenuItem)
		var newStack=[];
		try{
			while(node){
				if(node.dijitPopupParent){
					node=dijit.byId(node.dijitPopupParent).domNode;
				}else if(node.tagName && node.tagName.toLowerCase()=="body"){
					// is this the root of the document or just the root of an iframe?
					if(node===dojo.body()){
						// node is the root of the main document
						break;
					}
					// otherwise, find the iframe this node refers to (can't access it via parentNode,
					// need to do this trick instead) and continue tracing up the document
					node=dojo.query("iframe").filter(function(iframe){ return iframe.contentDocument.body===node; })[0];
				}else{
					var id = node.getAttribute && node.getAttribute("widgetId");
					if(id){
						newStack.unshift(id);
					}
					node=node.parentNode;
				}
			}
		}catch(e){ /* squelch */ }

		dijit._setStack(newStack);
	},

	_onFocusNode: function(/*DomNode*/ node){
		// summary
		//		Callback when node is focused
		if(node && node.tagName && node.tagName.toLowerCase() == "body"){
			return;
		}
		dijit._onTouchNode(node);
		if(node==dijit._curFocus){ return; }
		dijit._prevFocus = dijit._curFocus;
		dijit._curFocus = node;
		dojo.publish("focusNode", [node]);

		// handle focus/blur styling
		var w = dijit.getEnclosingWidget(node);
		if (w && w._setStateClass){
			w._focused = true;
			w._setStateClass();
		}
	},

	_setStack: function(newStack){
		// summary
		//	The stack of active widgets has changed.  Send out appropriate events and record new stack

		var oldStack = dijit._activeStack;		
		dijit._activeStack = newStack;

		// compare old stack to new stack to see how many elements they have in common
		for(var nCommon=0; nCommon<Math.min(oldStack.length, newStack.length); nCommon++){
			if(oldStack[nCommon] != newStack[nCommon]){
				break;
			}
		}

		// for all elements that have gone out of focus, send blur event
		for(var i=oldStack.length-1; i>=nCommon; i--){
			var widget = dijit.byId(oldStack[i]);
			if(widget){
				dojo.publish("widgetBlur", [widget]);
				if(widget._onBlur){
					widget._onBlur();
				}
			}
		}

		// for all element that have come into focus, send focus event
		for(var i=nCommon; i<newStack.length; i++){
			var widget = dijit.byId(newStack[i]);
			if(widget){
				dojo.publish("widgetFocus", [widget]);
				if(widget._onFocus){
					widget._onFocus();
				}
			}
		}
	}
});

// register top window and all the iframes it contains
dojo.addOnLoad(dijit.registerWin);

}

if(!dojo._hasResource["dijit._base.manager"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.manager"] = true;
dojo.provide("dijit._base.manager");

dojo.declare("dijit.WidgetSet", null, {
	constructor: function(){
		// summary:
		//	A set of widgets indexed by id
		this._hash={};
	},

	add: function(/*Widget*/ widget){
		if(this._hash[widget.id]){
			throw new Error("Tried to register widget with id==" + widget.id + " but that id is already registered");
		}
		this._hash[widget.id]=widget;
	},

	remove: function(/*String*/ id){
		delete this._hash[id];
	},

	forEach: function(/*Function*/ func){
		for(var id in this._hash){
			func(this._hash[id]);
		}
	},

	filter: function(/*Function*/ filter){
		var res = new dijit.WidgetSet();
		this.forEach(function(widget){
			if(filter(widget)){ res.add(widget); }
		});
		return res;		// dijit.WidgetSet
	},

	byId: function(/*String*/ id){
		return this._hash[id];
	},

	byClass: function(/*String*/ cls){
		return this.filter(function(widget){ return widget.declaredClass==cls; });	// dijit.WidgetSet
	}
	});

// registry: list of all widgets on page
dijit.registry = new dijit.WidgetSet();

dijit._widgetTypeCtr = {};

dijit.getUniqueId = function(/*String*/widgetType){
	// summary
	//	Generates a unique id for a given widgetType

	var id;
	do{
		id = widgetType + "_" +
			(dijit._widgetTypeCtr[widgetType] !== undefined ?
				++dijit._widgetTypeCtr[widgetType] : dijit._widgetTypeCtr[widgetType] = 0);
	}while(dijit.byId(id));
	return id; // String
};


if(dojo.isIE){
	// Only run this for IE because we think it's only necessary in that case,
	// and because it causes problems on FF.  See bug #3531 for details.
	dojo.addOnUnload(function(){
		dijit.registry.forEach(function(widget){ widget.destroy(); });
	});
}

dijit.byId = function(/*String|Widget*/id){
	// summary:
	//		Returns a widget by its id, or if passed a widget, no-op (like dojo.byId())
	return (dojo.isString(id)) ? dijit.registry.byId(id) : id; // Widget
};

dijit.byNode = function(/* DOMNode */ node){
	// summary:
	//		Returns the widget as referenced by node
	return dijit.registry.byId(node.getAttribute("widgetId")); // Widget
};

dijit.getEnclosingWidget = function(/* DOMNode */ node){
	// summary:
	//		Returns the widget whose dom tree contains node or null if
	//		the node is not contained within the dom tree of any widget
	while(node){
		if(node.getAttribute && node.getAttribute("widgetId")){
			return dijit.registry.byId(node.getAttribute("widgetId"));
		}
		node = node.parentNode;
	}
	return null;
};

}

if(!dojo._hasResource["dijit._base.place"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.place"] = true;
dojo.provide("dijit._base.place");

// ported from dojo.html.util

dijit.getViewport = function(){
	//	summary
	//	Returns the dimensions and scroll position of the viewable area of a browser window

	var _window = dojo.global;
	var _document = dojo.doc;

	// get viewport size
	var w = 0, h = 0;
	if(dojo.isMozilla){
		// mozilla
		// _window.innerHeight includes the height taken by the scroll bar
		// clientHeight is ideal but has DTD issues:
		// #4539: FF reverses the roles of body.clientHeight/Width and documentElement.clientHeight/Width based on the DTD!
		// check DTD to see whether body or documentElement returns the viewport dimensions using this algorithm:
		var minw, minh, maxw, maxh;
		if(_document.body.clientWidth>_document.documentElement.clientWidth){
			minw = _document.documentElement.clientWidth;
			maxw = _document.body.clientWidth;
		}else{
			maxw = _document.documentElement.clientWidth;
			minw = _document.body.clientWidth;
		}
		if(_document.body.clientHeight>_document.documentElement.clientHeight){
			minh = _document.documentElement.clientHeight;
			maxh = _document.body.clientHeight;
		}else{
			maxh = _document.documentElement.clientHeight;
			minh = _document.body.clientHeight;
		}
		w = (maxw > _window.innerWidth) ? minw : maxw;
		h = (maxh > _window.innerHeight) ? minh : maxh;
	}else if(!dojo.isOpera && _window.innerWidth){
		//in opera9, dojo.body().clientWidth should be used, instead
		//of window.innerWidth/document.documentElement.clientWidth
		//so we have to check whether it is opera
		w = _window.innerWidth;
		h = _window.innerHeight;
	}else if(dojo.isIE && _document.documentElement && _document.documentElement.clientHeight){
		w = _document.documentElement.clientWidth;
		h = _document.documentElement.clientHeight;
	}else if(dojo.body().clientWidth){
		// IE5, Opera
		w = dojo.body().clientWidth;
		h = dojo.body().clientHeight;
	}

	// get scroll position
	var scroll = dojo._docScroll();

	return { w: w, h: h, l: scroll.x, t: scroll.y };	//	object
};

dijit.placeOnScreen = function(
	/* DomNode */	node,
	/* Object */		pos,
	/* Object */		corners,
	/* boolean? */		tryOnly){
	//	summary:
	//		Keeps 'node' in the visible area of the screen while trying to
	//		place closest to pos.x, pos.y. The input coordinates are
	//		expected to be the desired document position.
	//
	//		Set which corner(s) you want to bind to, such as
	//		
	//			placeOnScreen(node, {x: 10, y: 20}, ["TR", "BL"])
	//		
	//		The desired x/y will be treated as the topleft(TL)/topright(TR) or
	//		BottomLeft(BL)/BottomRight(BR) corner of the node. Each corner is tested
	//		and if a perfect match is found, it will be used. Otherwise, it goes through
	//		all of the specified corners, and choose the most appropriate one.
	//		
	//		NOTE: node is assumed to be absolutely or relatively positioned.

	var choices = dojo.map(corners, function(corner){ return { corner: corner, pos: pos }; });

	return dijit._place(node, choices);
}

dijit._place = function(/*DomNode*/ node, /* Array */ choices, /* Function */ layoutNode){
	// summary:
	//		Given a list of spots to put node, put it at the first spot where it fits,
	//		of if it doesn't fit anywhere then the place with the least overflow
	// choices: Array
	//		Array of elements like: {corner: 'TL', pos: {x: 10, y: 20} }
	//		Above example says to put the top-left corner of the node at (10,20)
	//	layoutNode: Function(node, orient)
	//		for things like tooltip, they are displayed differently (and have different dimensions)
	//		based on their orientation relative to the parent.   This adjusts the popup based on orientation.

	// get {x: 10, y: 10, w: 100, h:100} type obj representing position of
	// viewport over document
	var view = dijit.getViewport();

	// This won't work if the node is inside a <div style="position: relative">,
	// so reattach it to document.body.   (Otherwise, the positioning will be wrong
	// and also it might get cutoff)
	if(!node.parentNode || String(node.parentNode.tagName).toLowerCase() != "body"){
		dojo.body().appendChild(node);
	}

	var best=null;
	for(var i=0; i<choices.length; i++){
		var corner = choices[i].corner;
		var pos = choices[i].pos;

		// configure node to be displayed in given position relative to button
		// (need to do this in order to get an accurate size for the node, because
		// a tooltips size changes based on position, due to triangle)
		if(layoutNode){
			layoutNode(corner);
		}

		// get node's size
		var oldDisplay = node.style.display;
		var oldVis = node.style.visibility;
		node.style.visibility = "hidden";
		node.style.display = "";
		var mb = dojo.marginBox(node);
		node.style.display = oldDisplay;
		node.style.visibility = oldVis;

		// coordinates and size of node with specified corner placed at pos,
		// and clipped by viewport
		var startX = (corner.charAt(1)=='L' ? pos.x : Math.max(view.l, pos.x - mb.w)),
			startY = (corner.charAt(0)=='T' ? pos.y : Math.max(view.t, pos.y -  mb.h)),
			endX = (corner.charAt(1)=='L' ? Math.min(view.l+view.w, startX+mb.w) : pos.x),
			endY = (corner.charAt(0)=='T' ? Math.min(view.t+view.h, startY+mb.h) : pos.y),
			width = endX-startX,
			height = endY-startY,
			overflow = (mb.w-width) + (mb.h-height);

		if(best==null || overflow<best.overflow){
			best = {
				corner: corner,
				aroundCorner: choices[i].aroundCorner,
				x: startX,
				y: startY,
				w: width,
				h: height,
				overflow: overflow
			};
		}
		if(overflow==0){
			break;
		}
	}

	node.style.left = best.x + "px";
	node.style.top = best.y + "px";
	return best;
}

dijit.placeOnScreenAroundElement = function(
	/* DomNode */		node,
	/* DomNode */		aroundNode,
	/* Object */		aroundCorners,
	/* Function */		layoutNode){

	//	summary
	//	Like placeOnScreen, except it accepts aroundNode instead of x,y
	//	and attempts to place node around it.  Uses margin box dimensions.
	//
	//	aroundCorners
	//		specify Which corner of aroundNode should be
	//		used to place the node => which corner(s) of node to use (see the
	//		corners parameter in dijit.placeOnScreen)
	//		e.g. {'TL': 'BL', 'BL': 'TL'}
	//
	//	layoutNode: Function(node, orient)
	//		for things like tooltip, they are displayed differently (and have different dimensions)
	//		based on their orientation relative to the parent.   This adjusts the popup based on orientation.


	// get coordinates of aroundNode
	aroundNode = dojo.byId(aroundNode);
	var oldDisplay = aroundNode.style.display;
	aroundNode.style.display="";
	// #3172: use the slightly tighter border box instead of marginBox
	var aroundNodeW = aroundNode.offsetWidth; //mb.w;
	var aroundNodeH = aroundNode.offsetHeight; //mb.h;
	var aroundNodePos = dojo.coords(aroundNode, true);
	aroundNode.style.display=oldDisplay;

	// Generate list of possible positions for node
	var choices = [];
	for(var nodeCorner in aroundCorners){
		choices.push( {
			aroundCorner: nodeCorner,
			corner: aroundCorners[nodeCorner],
			pos: {
				x: aroundNodePos.x + (nodeCorner.charAt(1)=='L' ? 0 : aroundNodeW),
				y: aroundNodePos.y + (nodeCorner.charAt(0)=='T' ? 0 : aroundNodeH)
			}
		});
	}

	return dijit._place(node, choices, layoutNode);
}

}

if(!dojo._hasResource["dijit._base.window"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.window"] = true;
dojo.provide("dijit._base.window");

dijit.getDocumentWindow = function(doc){
	//	summary
	// 	Get window object associated with document doc

	// With Safari, there is not way to retrieve the window from the document, so we must fix it.
	if(dojo.isSafari && !doc._parentWindow){
		/*
			This is a Safari specific function that fix the reference to the parent
			window from the document object.
		*/
		var fix=function(win){
			win.document._parentWindow=win;
			for(var i=0; i<win.frames.length; i++){
				fix(win.frames[i]);
			}
		}
		fix(window.top);
	}

	//In some IE versions (at least 6.0), document.parentWindow does not return a
	//reference to the real window object (maybe a copy), so we must fix it as well
	//We use IE specific execScript to attach the real window reference to
	//document._parentWindow for later use
	if(dojo.isIE && window !== document.parentWindow && !doc._parentWindow){
		/*
		In IE 6, only the variable "window" can be used to connect events (others
		may be only copies).
		*/
		doc.parentWindow.execScript("document._parentWindow = window;", "Javascript");
		//to prevent memory leak, unset it after use
		//another possibility is to add an onUnload handler which seems overkill to me (liucougar)
		var win = doc._parentWindow;
		doc._parentWindow = null;
		return win;	//	Window
	}

	return doc._parentWindow || doc.parentWindow || doc.defaultView;	//	Window
}

}

if(!dojo._hasResource["dijit._base.popup"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.popup"] = true;
dojo.provide("dijit._base.popup");





dijit.popup = new function(){
	// summary:
	//		This class is used to show/hide widgets as popups.
	//

	var stack = [],
		beginZIndex=1000,
		idGen = 1;

	this.open = function(/*Object*/ args){
		// summary:
		//		Popup the widget at the specified position
		//
		// args: Object
		//		popup: Widget
		//			widget to display,
		//		parent: Widget
		//			the button etc. that is displaying this popup
		//		around: DomNode
		//			DOM node (typically a button); place popup relative to this node
		//		orient: Object
		//			structure specifying possible positions of popup relative to "around" node
		//		onCancel: Function
		//			callback when user has canceled the popup by
		//				1. hitting ESC or
		//				2. by using the popup widget's proprietary cancel mechanism (like a cancel button in a dialog);
		//				   ie: whenever popupWidget.onCancel() is called, args.onCancel is called
		//		onClose: Function
		//			callback whenever this popup is closed
		//		onExecute: Function
		//			callback when user "executed" on the popup/sub-popup by selecting a menu choice, etc. (top menu only)
		//
		// examples:
		//		1. opening at the mouse position
		//			dijit.popup.open({popup: menuWidget, x: evt.pageX, y: evt.pageY});
		//		2. opening the widget as a dropdown
		//			dijit.popup.open({parent: this, popup: menuWidget, around: this.domNode, onClose: function(){...}  });
		//
		//	Note that whatever widget called dijit.popup.open() should also listen to it's own _onBlur callback
		//	(fired from _base/focus.js) to know that focus has moved somewhere else and thus the popup should be closed.

		var widget = args.popup,
			orient = args.orient || {'BL':'TL', 'TL':'BL'},
			around = args.around,
			id = (args.around && args.around.id) ? (args.around.id+"_dropdown") : ("popup_"+idGen++);

		// make wrapper div to hold widget and possibly hold iframe behind it.
		// we can't attach the iframe as a child of the widget.domNode because
		// widget.domNode might be a <table>, <ul>, etc.
		var wrapper = dojo.doc.createElement("div");
		wrapper.id = id;
		wrapper.className="dijitPopup";
		wrapper.style.zIndex = beginZIndex + stack.length;
		wrapper.style.visibility = "hidden";
		if(args.parent){
			wrapper.dijitPopupParent=args.parent.id;
		}
		dojo.body().appendChild(wrapper);

		widget.domNode.style.display="";
		wrapper.appendChild(widget.domNode);

		var iframe = new dijit.BackgroundIframe(wrapper);

		// position the wrapper node
		var best = around ?
			dijit.placeOnScreenAroundElement(wrapper, around, orient, widget.orient ? dojo.hitch(widget, "orient") : null) :
			dijit.placeOnScreen(wrapper, args, orient == 'R' ? ['TR','BR','TL','BL'] : ['TL','BL','TR','BR']);

		wrapper.style.visibility = "visible";
		// TODO: use effects to fade in wrapper

		var handlers = [];

		// Compute the closest ancestor popup that's *not* a child of another popup.
		// Ex: For a TooltipDialog with a button that spawns a tree of menus, find the popup of the button.
		function getTopPopup(){
			for(var pi=stack.length-1; pi > 0 && stack[pi].parent === stack[pi-1].widget; pi--);
			return stack[pi];
		}

		// provide default escape and tab key handling
		// (this will work for any widget, not just menu)
		handlers.push(dojo.connect(wrapper, "onkeypress", this, function(evt){
			if(evt.keyCode == dojo.keys.ESCAPE && args.onCancel){
				args.onCancel();
			}else if(evt.keyCode == dojo.keys.TAB){
				dojo.stopEvent(evt);
				var topPopup = getTopPopup();
				if(topPopup && topPopup.onCancel){
					topPopup.onCancel();
				}
			}
		}));

		// watch for cancel/execute events on the popup and notify the caller
		// (for a menu, "execute" means clicking an item)
		if(widget.onCancel){
			handlers.push(dojo.connect(widget, "onCancel", null, args.onCancel));
		}

		handlers.push(dojo.connect(widget, widget.onExecute ? "onExecute" : "onChange", null, function(){
			var topPopup = getTopPopup();
			if(topPopup && topPopup.onExecute){
				topPopup.onExecute();
			}
		}));

		stack.push({
			wrapper: wrapper,
			iframe: iframe,
			widget: widget,
			parent: args.parent,
			onExecute: args.onExecute,
			onCancel: args.onCancel,
 			onClose: args.onClose,
			handlers: handlers
		});

		if(widget.onOpen){
			widget.onOpen(best);
		}

		return best;
	};

	this.close = function(/*Widget*/ popup){
		// summary:
		//		Close specified popup and any popups that it parented
		while(dojo.some(stack, function(elem){return elem.widget == popup;})){
			var top = stack.pop(),
				wrapper = top.wrapper,
				iframe = top.iframe,
				widget = top.widget,
				onClose = top.onClose;
	
			if(widget.onClose){
				widget.onClose();
			}
			dojo.forEach(top.handlers, dojo.disconnect);
	
			// #2685: check if the widget still has a domNode so ContentPane can change its URL without getting an error
			if(!widget||!widget.domNode){ return; }
			dojo.style(widget.domNode, "display", "none");
			dojo.body().appendChild(widget.domNode);
			iframe.destroy();
			dojo._destroyElement(wrapper);
	
			if(onClose){
				onClose();
			}
		}
	};
}();

dijit._frames = new function(){
	// summary: cache of iframes
	var queue = [];

	this.pop = function(){
		var iframe;
		if(queue.length){
			iframe = queue.pop();
			iframe.style.display="";
		}else{
			if(dojo.isIE){
				var html="<iframe src='javascript:\"\"'"
					+ " style='position: absolute; left: 0px; top: 0px;"
					+ "z-index: -1; filter:Alpha(Opacity=\"0\");'>";
				iframe = dojo.doc.createElement(html);
			}else{
			 	var iframe = dojo.doc.createElement("iframe");
				iframe.src = 'javascript:""';
				iframe.className = "dijitBackgroundIframe";
			}
			iframe.tabIndex = -1; // Magic to prevent iframe from getting focus on tab keypress - as style didnt work.
			dojo.body().appendChild(iframe);
		}
		return iframe;
	};

	this.push = function(iframe){
		iframe.style.display="";
		if(dojo.isIE){
			iframe.style.removeExpression("width");
			iframe.style.removeExpression("height");
		}
		queue.push(iframe);
	}
}();

// fill the queue
if(dojo.isIE && dojo.isIE < 7){
	dojo.addOnLoad(function(){
		var f = dijit._frames;
		dojo.forEach([f.pop()], f.push);
	});
}


dijit.BackgroundIframe = function(/* DomNode */node){
	//	summary:
	//		For IE z-index schenanigans. id attribute is required.
	//
	//	description:
	//		new dijit.BackgroundIframe(node)
	//			Makes a background iframe as a child of node, that fills
	//			area (and position) of node

	if(!node.id){ throw new Error("no id"); }
	if((dojo.isIE && dojo.isIE < 7) || (dojo.isFF && dojo.isFF < 3 && dojo.hasClass(dojo.body(), "dijit_a11y"))){
		var iframe = dijit._frames.pop();
		node.appendChild(iframe);
		if(dojo.isIE){
			iframe.style.setExpression("width", "document.getElementById('" + node.id + "').offsetWidth");
			iframe.style.setExpression("height", "document.getElementById('" + node.id + "').offsetHeight");
		}
		this.iframe = iframe;
	}
};

dojo.extend(dijit.BackgroundIframe, {
	destroy: function(){
		//	summary: destroy the iframe
		if(this.iframe){
			dijit._frames.push(this.iframe);
			delete this.iframe;
		}
	}
});

}

if(!dojo._hasResource["dijit._base.scroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.scroll"] = true;
dojo.provide("dijit._base.scroll");

dijit.scrollIntoView = function(/* DomNode */node){
	//	summary
	//	Scroll the passed node into view, if it is not.

	// don't rely on that node.scrollIntoView works just because the function is there
	// it doesnt work in Konqueror or Opera even though the function is there and probably
	// not safari either
	// dont like browser sniffs implementations but sometimes you have to use it
	if(dojo.isIE){
		//only call scrollIntoView if there is a scrollbar for this menu,
		//otherwise, scrollIntoView will scroll the window scrollbar
		if(dojo.marginBox(node.parentNode).h <= node.parentNode.scrollHeight){ //PORT was getBorderBox
			node.scrollIntoView(false);
		}
	}else if(dojo.isMozilla){
		node.scrollIntoView(false);
	}else{
		var parent = node.parentNode;
		var parentBottom = parent.scrollTop + dojo.marginBox(parent).h; //PORT was getBorderBox
		var nodeBottom = node.offsetTop + dojo.marginBox(node).h;
		if(parentBottom < nodeBottom){
			parent.scrollTop += (nodeBottom - parentBottom);
		}else if(parent.scrollTop > node.offsetTop){
			parent.scrollTop -= (parent.scrollTop - node.offsetTop);
		}
	}
};

}

if(!dojo._hasResource["dijit._base.sniff"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.sniff"] = true;
dojo.provide("dijit._base.sniff");

// ported from dojo.html.applyBrowserClass (style.js)

//	summary:
//		Applies pre-set class names based on browser & version to the
//		top-level HTML node.  Simply doing a require on this module will
//		establish this CSS.  Modified version of Morris' CSS hack.
(function(){
	var d = dojo;
	var ie = d.isIE;
	var opera = d.isOpera;
	var maj = Math.floor;
	var classes = {
		dj_ie: ie,
//		dj_ie55: ie == 5.5,
		dj_ie6: maj(ie) == 6,
		dj_ie7: maj(ie) == 7,
		dj_iequirks: ie && d.isQuirks,
// NOTE: Opera not supported by dijit
		dj_opera: opera,
		dj_opera8: maj(opera) == 8,
		dj_opera9: maj(opera) == 9,
		dj_khtml: d.isKhtml,
		dj_safari: d.isSafari,
		dj_gecko: d.isMozilla
	}; // no dojo unsupported browsers

	for(var p in classes){
		if(classes[p]){
			var html = dojo.doc.documentElement; //TODO browser-specific DOM magic needed?
			if(html.className){
				html.className += " " + p;
			}else{
				html.className = p;
			}
		}
	}
})();

}

if(!dojo._hasResource["dijit._base.bidi"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.bidi"] = true;
dojo.provide("dijit._base.bidi");

// summary: applies a class to the top of the document for right-to-left stylesheet rules

dojo.addOnLoad(function(){
	if(!dojo._isBodyLtr()){
		dojo.addClass(dojo.body(), "dijitRtl");
	}
});

}

if(!dojo._hasResource["dijit._base.typematic"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.typematic"] = true;
dojo.provide("dijit._base.typematic");

dijit.typematic = {
	// summary:
	//	These functions are used to repetitively call a user specified callback
	//	method when a specific key or mouse click over a specific DOM node is
	//	held down for a specific amount of time.
	//	Only 1 such event is allowed to occur on the browser page at 1 time.

	_fireEventAndReload: function(){
		this._timer = null;
		this._callback(++this._count, this._node, this._evt);
		this._currentTimeout = (this._currentTimeout < 0) ? this._initialDelay : ((this._subsequentDelay > 1) ? this._subsequentDelay : Math.round(this._currentTimeout * this._subsequentDelay));
		this._timer = setTimeout(dojo.hitch(this, "_fireEventAndReload"), this._currentTimeout);
	},

	trigger: function(/*Event*/ evt, /* Object */ _this, /*DOMNode*/ node, /* Function */ callback, /* Object */ obj, /* Number */ subsequentDelay, /* Number */ initialDelay){
		// summary:
		//      Start a timed, repeating callback sequence.
		//      If already started, the function call is ignored.
		//      This method is not normally called by the user but can be
		//      when the normal listener code is insufficient.
		//	Parameters:
		//	evt: key or mouse event object to pass to the user callback
		//	_this: pointer to the user's widget space.
		//	node: the DOM node object to pass the the callback function
		//	callback: function to call until the sequence is stopped called with 3 parameters:
		//		count: integer representing number of repeated calls (0..n) with -1 indicating the iteration has stopped
		//		node: the DOM node object passed in
		//		evt: key or mouse event object
		//	obj: user space object used to uniquely identify each typematic sequence
		//	subsequentDelay: if > 1, the number of milliseconds until the 3->n events occur
		//		or else the fractional time multiplier for the next event's delay, default=0.9
		//	initialDelay: the number of milliseconds until the 2nd event occurs, default=500ms
		if(obj != this._obj){
			this.stop();
			this._initialDelay = initialDelay || 500;
			this._subsequentDelay = subsequentDelay || 0.90;
			this._obj = obj;
			this._evt = evt;
			this._node = node;
			this._currentTimeout = -1;
			this._count = -1;
			this._callback = dojo.hitch(_this, callback);
			this._fireEventAndReload();
		}
	},

	stop: function(){
		// summary:
		//	  Stop an ongoing timed, repeating callback sequence.
		if(this._timer){
			clearTimeout(this._timer);
			this._timer = null;
		}
		if(this._obj){
			this._callback(-1, this._node, this._evt);
			this._obj = null;
		}
	},

	addKeyListener: function(/*DOMNode*/ node, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
		// summary: Start listening for a specific typematic key.
		//	keyObject: an object defining the key to listen for.
		//		key: (mandatory) the keyCode (number) or character (string) to listen for.
		//		ctrlKey: desired ctrl key state to initiate the calback sequence:
		//			pressed (true)
		//			released (false)
		//			either (unspecified)
		//		altKey: same as ctrlKey but for the alt key
		//		shiftKey: same as ctrlKey but for the shift key
		//	See the trigger method for other parameters.
		//	Returns an array of dojo.connect handles
		return [
			dojo.connect(node, "onkeypress", this, function(evt){
				if(evt.keyCode == keyObject.keyCode && (!keyObject.charCode || keyObject.charCode == evt.charCode) &&
				(keyObject.ctrlKey === undefined || keyObject.ctrlKey == evt.ctrlKey) &&
				(keyObject.altKey === undefined || keyObject.altKey == evt.ctrlKey) &&
				(keyObject.shiftKey === undefined || keyObject.shiftKey == evt.ctrlKey)){
					dojo.stopEvent(evt);
					dijit.typematic.trigger(keyObject, _this, node, callback, keyObject, subsequentDelay, initialDelay);
				}else if(dijit.typematic._obj == keyObject){
					dijit.typematic.stop();
				}
			}),
			dojo.connect(node, "onkeyup", this, function(evt){
				if(dijit.typematic._obj == keyObject){
					dijit.typematic.stop();
				}
			})
		];
	},

	addMouseListener: function(/*DOMNode*/ node, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
		// summary: Start listening for a typematic mouse click.
		//	See the trigger method for other parameters.
		//	Returns an array of dojo.connect handles
		var dc = dojo.connect;
		return [
			dc(node, "mousedown", this, function(evt){
				dojo.stopEvent(evt);
				dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
			}),
			dc(node, "mouseup", this, function(evt){
				dojo.stopEvent(evt);
				dijit.typematic.stop();
			}),
			dc(node, "mouseout", this, function(evt){
				dojo.stopEvent(evt);
				dijit.typematic.stop();
			}),
			dc(node, "mousemove", this, function(evt){
				dojo.stopEvent(evt);
			}),
			dc(node, "dblclick", this, function(evt){
				dojo.stopEvent(evt);
				if(dojo.isIE){
					dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
					setTimeout(dijit.typematic.stop, 50);
				}
			})
		];
	},

	addListener: function(/*Node*/ mouseNode, /*Node*/ keyNode, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
		// summary: Start listening for a specific typematic key and mouseclick.
		//	This is a thin wrapper to addKeyListener and addMouseListener.
		//	mouseNode: the DOM node object to listen on for mouse events.
		//	keyNode: the DOM node object to listen on for key events.
		//	See the addMouseListener and addKeyListener methods for other parameters.
		//	Returns an array of dojo.connect handles
		return this.addKeyListener(keyNode, keyObject, _this, callback, subsequentDelay, initialDelay).concat(
			this.addMouseListener(mouseNode, _this, callback, subsequentDelay, initialDelay));
	}
};

}

if(!dojo._hasResource["dijit._base.wai"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.wai"] = true;
dojo.provide("dijit._base.wai");

dijit.wai = {
	onload: function(){
		// summary:
		//		Function that detects if we are in high-contrast mode or not,
		//		and sets up a timer to periodically confirm the value.
		//		figure out the background-image style property
		//		and apply that to the image.src property.
		// description:
		//		This must be a named function and not an anonymous
		//		function, so that the widget parsing code can make sure it
		//		registers its onload function after this function.
		//		DO NOT USE "this" within this function.

		// create div for testing if high contrast mode is on or images are turned off
		var div = document.createElement("div");
		div.id = "a11yTestNode";
		div.style.cssText = 'border: 1px solid;'
			+ 'border-color:red green;'
			+ 'position: absolute;'
			+ 'height: 5px;'
			+ 'top: -999px;'
			+ 'background-image: url("' + dojo.moduleUrl("dojo", "resources/blank.gif") + '");';
		dojo.body().appendChild(div);

		// test it
		function check(){
			var cs = dojo.getComputedStyle(div);
			if(cs){
				var bkImg = cs.backgroundImage;
				var needsA11y = (cs.borderTopColor==cs.borderRightColor) || (bkImg != null && (bkImg == "none" || bkImg == "url(invalid-url:)" ));
				dojo[needsA11y ? "addClass" : "removeClass"](dojo.body(), "dijit_a11y");
			}
		}
		check();
		if(dojo.isIE){
			setInterval(check, 4000);
		}
	}
};

// Test if computer is in high contrast mode.
// Make sure the a11y test runs first, before widgets are instantiated.
if(dojo.isIE || dojo.isMoz){	// NOTE: checking in Safari messes things up
	dojo._loaders.unshift(dijit.wai.onload);
}

dojo.mixin(dijit,
{
	hasWaiRole: function(/*Element*/ elem){
		// Summary: Return true if elem has a role attribute and false if not.
		if(elem.hasAttribute){
			return elem.hasAttribute("role");
		}else{
			return elem.getAttribute("role") ? true : false;
		}
	},

	getWaiRole: function(/*Element*/ elem){
		// Summary: Return the role of elem or an empty string if
		//		elem does not have a role.
		var value = elem.getAttribute("role");
		if(value){
			var prefixEnd = value.indexOf(":");
			return prefixEnd == -1 ? value : value.substring(prefixEnd+1);
		}else{
			return "";
		}
	},

	setWaiRole: function(/*Element*/ elem, /*String*/ role){
		// Summary: Set the role on elem. On Firefox 2 and below, "wairole:" is
		//		prepended to the provided role value.
		if(dojo.isFF && dojo.isFF < 3){
			elem.setAttribute("role", "wairole:"+role);
		}else{
			elem.setAttribute("role", role);
		}
	},

	removeWaiRole: function(/*Element*/ elem){
		// Summary: Removes the role attribute from elem.
		elem.removeAttribute("role");
	},

	hasWaiState: function(/*Element*/ elem, /*String*/ state){
		// Summary: Return true if elem has a value for the given state and
		//		false if it does not.
		//		On Firefox 2 and below, we check for an attribute in namespace
		//		"http://www.w3.org/2005/07/aaa" with a name of the given state.
		//		On all other browsers, we check for an attribute called
		//		"aria-"+state.
		if(dojo.isFF && dojo.isFF < 3){
			return elem.hasAttributeNS("http://www.w3.org/2005/07/aaa", state);
		}else{
			if(elem.hasAttribute){
				return elem.hasAttribute("aria-"+state);
			}else{
				return elem.getAttribute("aria-"+state) ? true : false;
			}
		}
	},

	getWaiState: function(/*Element*/ elem, /*String*/ state){
		// Summary: Return the value of the requested state on elem
		//		or an empty string if elem has no value for state.
		//		On Firefox 2 and below, we check for an attribute in namespace
		//		"http://www.w3.org/2005/07/aaa" with a name of the given state.
		//		On all other browsers, we check for an attribute called
		//		"aria-"+state.
		if(dojo.isFF && dojo.isFF < 3){
			return elem.getAttributeNS("http://www.w3.org/2005/07/aaa", state);
		}else{
			var value =  elem.getAttribute("aria-"+state);
			return value ? value : "";
		}
	},

	setWaiState: function(/*Element*/ elem, /*String*/ state, /*String*/ value){
		// Summary: Set state on elem to value.
		//		On Firefox 2 and below, we set an attribute in namespace
		//		"http://www.w3.org/2005/07/aaa" with a name of the given state.
		//		On all other browsers, we set an attribute called
		//		"aria-"+state.
		if(dojo.isFF && dojo.isFF < 3){
			elem.setAttributeNS("http://www.w3.org/2005/07/aaa",
				"aaa:"+state, value);
		}else{
			elem.setAttribute("aria-"+state, value);
		}
	},

	removeWaiState: function(/*Element*/ elem, /*String*/ state){
		// Summary: Removes the given state from elem.
		//		On Firefox 2 and below, we remove the attribute in namespace
		//		"http://www.w3.org/2005/07/aaa" with a name of the given state.
		//		On all other browsers, we remove the attribute called
		//		"aria-"+state.
		if(dojo.isFF && dojo.isFF < 3){
			elem.removeAttributeNS("http://www.w3.org/2005/07/aaa", state);
		}else{
			elem.removeAttribute("aria-"+state);
		}
	}
});

}

if(!dojo._hasResource["dijit._base"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base"] = true;
dojo.provide("dijit._base");












}

if(!dojo._hasResource["dijit._Widget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Widget"] = true;
dojo.provide("dijit._Widget");



dojo.declare("dijit._Widget", null, {
	// summary:
	//		The foundation of dijit widgets. 	
	//
	// id: String
	//		a unique, opaque ID string that can be assigned by users or by the
	//		system. If the developer passes an ID which is known not to be
	//		unique, the specified ID is ignored and the system-generated ID is
	//		used instead.
	id: "",

	// lang: String
	//	Language to display this widget in (like en-us).
	//	Defaults to brower's specified preferred language (typically the language of the OS)
	lang: "",

	// dir: String
	//  Bi-directional support, as defined by the HTML DIR attribute. Either left-to-right "ltr" or right-to-left "rtl".
	dir: "",

	// class: String
	// HTML class attribute
	"class": "",

	// style: String
	// HTML style attribute
	style: "",

	// title: String
	// HTML title attribute
	title: "",

	// srcNodeRef: DomNode
	//		pointer to original dom node
	srcNodeRef: null,

	// domNode: DomNode
	//		this is our visible representation of the widget! Other DOM
	//		Nodes may by assigned to other properties, usually through the
	//		template system's dojoAttachPonit syntax, but the domNode
	//		property is the canonical "top level" node in widget UI.
	domNode: null,

	// attributeMap: Object
	//		A map of attributes and attachpoints -- typically standard HTML attributes -- to set
	//		on the widget's dom, at the "domNode" attach point, by default.
	//		Other node references can be specified as properties of 'this'
	attributeMap: {id:"", dir:"", lang:"", "class":"", style:"", title:""},  // TODO: add on* handlers?

	//////////// INITIALIZATION METHODS ///////////////////////////////////////

	postscript: function(params, srcNodeRef){
		this.create(params, srcNodeRef);
	},

	create: function(params, srcNodeRef){
		// summary:
		//		To understand the process by which widgets are instantiated, it
		//		is critical to understand what other methods create calls and
		//		which of them you'll want to override. Of course, adventurous
		//		developers could override create entirely, but this should
		//		only be done as a last resort.
		//
		//		Below is a list of the methods that are called, in the order
		//		they are fired, along with notes about what they do and if/when
		//		you should over-ride them in your widget:
		//			
		//			postMixInProperties:
		//				a stub function that you can over-ride to modify
		//				variables that may have been naively assigned by
		//				mixInProperties
		//			# widget is added to manager object here
		//			buildRendering
		//				Subclasses use this method to handle all UI initialization
		//				Sets this.domNode.  Templated widgets do this automatically
		//				and otherwise it just uses the source dom node.
		//			postCreate
		//				a stub function that you can over-ride to modify take
		//				actions once the widget has been placed in the UI

		// store pointer to original dom tree
		this.srcNodeRef = dojo.byId(srcNodeRef);

		// For garbage collection.  An array of handles returned by Widget.connect()
		// Each handle returned from Widget.connect() is an array of handles from dojo.connect()
		this._connects=[];

		// _attaches: String[]
		// 		names of all our dojoAttachPoint variables
		this._attaches=[];

		//mixin our passed parameters
		if(this.srcNodeRef && (typeof this.srcNodeRef.id == "string")){ this.id = this.srcNodeRef.id; }
		if(params){
			dojo.mixin(this,params);
		}
		this.postMixInProperties();

		// generate an id for the widget if one wasn't specified
		// (be sure to do this before buildRendering() because that function might
		// expect the id to be there.
		if(!this.id){
			this.id=dijit.getUniqueId(this.declaredClass.replace(/\./g,"_"));
		}
		dijit.registry.add(this);

		this.buildRendering();

		// Copy attributes listed in attributeMap into the [newly created] DOM for the widget.
		// The placement of these attributes is according to the property mapping in attributeMap.
		// Note special handling for 'style' and 'class' attributes which are lists and can
		// have elements from both old and new structures, and some attributes like "type"
		// cannot be processed this way as they are not mutable.
		if(this.domNode){
			for(var attr in this.attributeMap){
				var mapNode = this[this.attributeMap[attr] || "domNode"];
				var value = this[attr];
				if(typeof value != "object" && (value !== "" || (params && params[attr]))){
					switch(attr){
					case "class":
						dojo.addClass(mapNode, value);
						break;
					case "style":
						if(mapNode.style.cssText){
							mapNode.style.cssText += "; " + value;// FIXME: Opera
						}else{
							mapNode.style.cssText = value;
						}
						break;
					default:
						mapNode.setAttribute(attr, value);
					}
				}
			}
		}

		if(this.domNode){
			this.domNode.setAttribute("widgetId", this.id);
		}
		this.postCreate();

		// If srcNodeRef has been processed and removed from the DOM (e.g. TemplatedWidget) then delete it to allow GC.
		if(this.srcNodeRef && !this.srcNodeRef.parentNode){
			delete this.srcNodeRef;
		}	
	},

	postMixInProperties: function(){
		// summary
		//	Called after the parameters to the widget have been read-in,
		//	but before the widget template is instantiated.
		//	Especially useful to set properties that are referenced in the widget template.
	},

	buildRendering: function(){
		// summary:
		//		Construct the UI for this widget, setting this.domNode.
		//		Most widgets will mixin TemplatedWidget, which overrides this method.
		this.domNode = this.srcNodeRef || dojo.doc.createElement('div');
	},

	postCreate: function(){
		// summary:
		//		Called after a widget's dom has been setup
	},

	startup: function(){
		// summary:
		//		Called after a widget's children, and other widgets on the page, have been created.
		//		Provides an opportunity to manipulate any children before they are displayed
		//		This is useful for composite widgets that need to control or layout sub-widgets
		//		Many layout widgets can use this as a wiring phase
	},

	//////////// DESTROY FUNCTIONS ////////////////////////////////

	destroyRecursive: function(/*Boolean*/ finalize){
		// summary:
		// 		Destroy this widget and it's descendants. This is the generic
		// 		"destructor" function that all widget users should call to
		// 		cleanly discard with a widget. Once a widget is destroyed, it's
		// 		removed from the manager object.
		// finalize: Boolean
		//		is this function being called part of global environment
		//		tear-down?

		this.destroyDescendants();
		this.destroy();
	},

	destroy: function(/*Boolean*/ finalize){
		// summary:
		// 		Destroy this widget, but not its descendants
		// finalize: Boolean
		//		is this function being called part of global environment
		//		tear-down?
		this.uninitialize();
		dojo.forEach(this._connects, function(array){
			dojo.forEach(array, dojo.disconnect);
		});
		this.destroyRendering(finalize);
		dijit.registry.remove(this.id);
	},

	destroyRendering: function(/*Boolean*/ finalize){
		// summary:
		//		Destroys the DOM nodes associated with this widget
		// finalize: Boolean
		//		is this function being called part of global environment
		//		tear-down?

		if(this.bgIframe){
			this.bgIframe.destroy();
			delete this.bgIframe;
		}

		if(this.domNode){
			dojo._destroyElement(this.domNode);
			delete this.domNode;
		}

		if(this.srcNodeRef){
			dojo._destroyElement(this.srcNodeRef);
			delete this.srcNodeRef;
		}
	},

	destroyDescendants: function(){
		// summary:
		//		Recursively destroy the children of this widget and their
		//		descendants.

		// TODO: should I destroy in the reverse order, to go bottom up?
		dojo.forEach(this.getDescendants(), function(widget){ widget.destroy(); });
	},

	uninitialize: function(){
		// summary:
		//		stub function. Over-ride to implement custom widget tear-down
		//		behavior.
		return false;
	},

	////////////////// MISCELLANEOUS METHODS ///////////////////

	toString: function(){
		// summary:
		//		returns a string that represents the widget. When a widget is
		//		cast to a string, this method will be used to generate the
		//		output. Currently, it does not implement any sort of reversable
		//		serialization.
		return '[Widget ' + this.declaredClass + ', ' + (this.id || 'NO ID') + ']'; // String
	},

	getDescendants: function(){
		// summary:
		//	return all the descendant widgets
		var list = dojo.query('[widgetId]', this.domNode);
		return list.map(dijit.byNode);		// Array
	},

	nodesWithKeyClick : ["input", "button"],

	connect: function(
			/*Object|null*/ obj,
			/*String*/ event,
			/*String|Function*/ method){

		// summary:
		//		Connects specified obj/event to specified method of this object
		//		and registers for disconnect() on widget destroy.
		//		Special event: "ondijitclick" triggers on a click or enter-down or space-up
		//		Similar to dojo.connect() but takes three arguments rather than four.
		var handles =[];
		if(event == "ondijitclick"){
			var w = this;
			// add key based click activation for unsupported nodes.
			if(!this.nodesWithKeyClick[obj.nodeName]){
				handles.push(dojo.connect(obj, "onkeydown", this,
					function(e){
						if(e.keyCode == dojo.keys.ENTER){
							return (dojo.isString(method))?
								w[method](e) : method.call(w, e);
						}else if(e.keyCode == dojo.keys.SPACE){
							// stop space down as it causes IE to scroll
							// the browser window
							dojo.stopEvent(e);
						}
			 		}));
				handles.push(dojo.connect(obj, "onkeyup", this,
					function(e){
						if(e.keyCode == dojo.keys.SPACE){
							return dojo.isString(method) ?
								w[method](e) : method.call(w, e);
						}
			 		}));
			}
			event = "onclick";
		}
		handles.push(dojo.connect(obj, event, this, method));

		// return handles for FormElement and ComboBox
		this._connects.push(handles);
		return handles;
	},

	disconnect: function(/*Object*/ handles){
		// summary:
		//		Disconnects handle created by this.connect.
		//		Also removes handle from this widget's list of connects
		for(var i=0; i<this._connects.length; i++){
			if(this._connects[i]==handles){
				dojo.forEach(handles, dojo.disconnect);
				this._connects.splice(i, 1);
				return;
			}
		}
	},

	isLeftToRight: function(){
		// summary:
		//		Checks the DOM to for the text direction for bi-directional support
		// description:
		//		This method cannot be used during widget construction because the widget
		//		must first be connected to the DOM tree.  Parent nodes are searched for the
		//		'dir' attribute until one is found, otherwise left to right mode is assumed.
		//		See HTML spec, DIR attribute for more information.

		if(typeof this._ltr == "undefined"){
			this._ltr = dojo.getComputedStyle(this.domNode).direction != "rtl";
		}
		return this._ltr; //Boolean
	},

	isFocusable: function(){
		// summary:
		//		Return true if this widget can currently be focused
		//		and false if not
		return this.focus && (dojo.style(this.domNode, "display") != "none");
	}
});

}

if(!dojo._hasResource["dijit._Container"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Container"] = true;
dojo.provide("dijit._Container");

dojo.declare("dijit._Contained",
	null,
	{
		// summary
		//		Mixin for widgets that are children of a container widget
		//
		// example:
		// | 	// make a basic custom widget that knows about it's parents
		// |	dojo.declare("my.customClass",[dijit._Widget,dijit._Contained],{});
		// 
		getParent: function(){
			// summary:
			//		Returns the parent widget of this widget, assuming the parent
			//		implements dijit._Container
			for(var p=this.domNode.parentNode; p; p=p.parentNode){
				var id = p.getAttribute && p.getAttribute("widgetId");
				if(id){
					var parent = dijit.byId(id);
					return parent.isContainer ? parent : null;
				}
			}
			return null;
		},

		_getSibling: function(which){
			var node = this.domNode;
			do{
				node = node[which+"Sibling"];
			}while(node && node.nodeType != 1);
			if(!node){ return null; } // null
			var id = node.getAttribute("widgetId");
			return dijit.byId(id);
		},

		getPreviousSibling: function(){
			// summary:
			//		Returns null if this is the first child of the parent,
			//		otherwise returns the next element sibling to the "left".

			return this._getSibling("previous"); // Mixed
		},

		getNextSibling: function(){
			// summary:
			//		Returns null if this is the last child of the parent,
			//		otherwise returns the next element sibling to the "right".

			return this._getSibling("next"); // Mixed
		}
	}
);

dojo.declare("dijit._Container",
	null,
	{
		// summary:
		//		Mixin for widgets that contain a list of children.
		// description:
		//		Use this mixin when the widget needs to know about and
		//		keep track of it's widget children. Widgets like SplitContainer
		//		and TabContainer.  

		isContainer: true,

		addChild: function(/*Widget*/ widget, /*int?*/ insertIndex){
			// summary:
			//		Process the given child widget, inserting it's dom node as
			//		a child of our dom node

			if(insertIndex === undefined){
				insertIndex = "last";
			}
			var refNode = this.containerNode || this.domNode;
			if(insertIndex && typeof insertIndex == "number"){
				var children = dojo.query("> [widgetid]", refNode);
				if(children && children.length >= insertIndex){
					refNode = children[insertIndex-1]; insertIndex = "after";
				}
			}
			dojo.place(widget.domNode, refNode, insertIndex);

			// If I've been started but the child widget hasn't been started,
			// start it now.  Make sure to do this after widget has been
			// inserted into the DOM tree, so it can see that it's being controlled by me,
			// so it doesn't try to size itself.
			if(this._started && !widget._started){
				widget.startup();
			}
		},

		removeChild: function(/*Widget*/ widget){
			// summary:
			//		Removes the passed widget instance from this widget but does
			//		not destroy it
			var node = widget.domNode;
			node.parentNode.removeChild(node);	// detach but don't destroy
		},

		_nextElement: function(node){
			do{
				node = node.nextSibling;
			}while(node && node.nodeType != 1);
			return node;
		},

		_firstElement: function(node){
			node = node.firstChild;
			if(node && node.nodeType != 1){
				node = this._nextElement(node);
			}
			return node;
		},

		getChildren: function(){
			// summary:
			//		Returns array of children widgets
			return dojo.query("> [widgetId]", this.containerNode || this.domNode).map(dijit.byNode); // Array
		},

		hasChildren: function(){
			// summary:
			//		Returns true if widget has children
			var cn = this.containerNode || this.domNode;
			return !!this._firstElement(cn); // Boolean
		},

		_getSiblingOfChild: function(/*Widget*/ child, /*int*/ dir){
			// summary:
			//		Get the next or previous widget sibling of child
			// dir:
			//		if 1, get the next sibling
			//		if -1, get the previous sibling
			var node = child.domNode;
			var which = (dir>0 ? "nextSibling" : "previousSibling");
			do{
				node = node[which];
			}while(node && (node.nodeType != 1 || !dijit.byNode(node)));
			return node ? dijit.byNode(node) : null;
		}
	}
);

dojo.declare("dijit._KeyNavContainer",
	[dijit._Container],
	{

		// summary: A _Container with keyboard navigation of its children.
		// decscription:
		//		To use this mixin, call connectKeyNavHandlers() in
		//		postCreate() and call startupKeyNavChildren() in startup().
		//		It provides normalized keyboard and focusing code for Container
		//		widgets.
/*=====
		// focusedChild: Widget
		//		The currently focused child widget, or null if there isn't one
		focusedChild: null,
=====*/

		_keyNavCodes: {},

		connectKeyNavHandlers: function(/*Array*/ prevKeyCodes, /*Array*/ nextKeyCodes){
			// summary:
			//		Call in postCreate() to attach the keyboard handlers
			//		to the container.
			// preKeyCodes: Array
			//		Key codes for navigating to the previous child.
			// nextKeyCodes: Array
			//		Key codes for navigating to the next child.

			var keyCodes = this._keyNavCodes = {};
			var prev = dojo.hitch(this, this.focusPrev);
			var next = dojo.hitch(this, this.focusNext);
			dojo.forEach(prevKeyCodes, function(code){ keyCodes[code] = prev });
			dojo.forEach(nextKeyCodes, function(code){ keyCodes[code] = next });
			this.connect(this.domNode, "onkeypress", "_onContainerKeypress");
			if(dojo.isIE){
				this.connect(this.domNode, "onactivate", "_onContainerFocus");
				this.connect(this.domNode, "ondeactivate", "_onContainerBlur");
			}else{
				this.connect(this.domNode, "onfocus", "_onContainerFocus");
				this.connect(this.domNode, "onblur", "_onContainerBlur");
			}
		},

		startupKeyNavChildren: function(){
			// summary:
			//		Call in startup() to set child tabindexes to -1
			dojo.forEach(this.getChildren(), dojo.hitch(this, "_setTabIndexMinusOne"));
		},

		addChild: function(/*Widget*/ widget, /*int?*/ insertIndex){
			// summary: Add a child to our _Container
			dijit._KeyNavContainer.superclass.addChild.apply(this, arguments);
			this._setTabIndexMinusOne(widget);
		},

		focus: function(){
			// summary: Default focus() implementation: focus the first child.
			this.focusFirstChild();
		},

		focusFirstChild: function(){
			// summary: Focus the first focusable child in the container.
			this.focusChild(this._getFirstFocusableChild());
		},

		focusNext: function(){
			// summary: Focus the next widget or focal node (for widgets
			//		with multiple focal nodes) within this container.
			if(this.focusedChild && this.focusedChild.hasNextFocalNode
					&& this.focusedChild.hasNextFocalNode()){
				this.focusedChild.focusNext();
				return;
			}
			var child = this._getNextFocusableChild(this.focusedChild, 1);
			if(child.getFocalNodes){
				this.focusChild(child, child.getFocalNodes()[0]);
			}else{
				this.focusChild(child);
			}
		},

		focusPrev: function(){
			// summary: Focus the previous widget or focal node (for widgets
			//		with multiple focal nodes) within this container.
			if(this.focusedChild && this.focusedChild.hasPrevFocalNode
					&& this.focusedChild.hasPrevFocalNode()){
				this.focusedChild.focusPrev();
				return;
			}
			var child = this._getNextFocusableChild(this.focusedChild, -1);
			if(child.getFocalNodes){
				var nodes = child.getFocalNodes();
				this.focusChild(child, nodes[nodes.length-1]);
			}else{
				this.focusChild(child);
			}
		},

		focusChild: function(/*Widget*/ widget, /*Node?*/ node){
			// summary: Focus widget. Optionally focus 'node' within widget.
			if(widget){
				if(this.focusedChild && widget !== this.focusedChild){
					this._onChildBlur(this.focusedChild);
				}
				this.focusedChild = widget;
				if(node && widget.focusFocalNode){
					widget.focusFocalNode(node);
				}else{
					widget.focus();
				}
			}
		},

		_setTabIndexMinusOne: function(/*Widget*/ widget){
			if(widget.getFocalNodes){
				dojo.forEach(widget.getFocalNodes(), function(node){
					node.setAttribute("tabIndex", -1);
				});
			}else{
				(widget.focusNode || widget.domNode).setAttribute("tabIndex", -1);
			}
		},

		_onContainerFocus: function(evt){
			this.domNode.setAttribute("tabIndex", -1);
			if(evt.target === this.domNode){
				this.focusFirstChild();
			}else{
				var widget = dijit.getEnclosingWidget(evt.target);
				if(widget && widget.isFocusable()){
					this.focusedChild = widget;
				}
			}
		},

		_onContainerBlur: function(evt){
			if(this.tabIndex){
				this.domNode.setAttribute("tabIndex", this.tabIndex);
			}
		},

		_onContainerKeypress: function(evt){
			if(evt.ctrlKey || evt.altKey){ return; }
			var func = this._keyNavCodes[evt.keyCode];
			if(func){
				func();
				dojo.stopEvent(evt);
			}
		},

		_onChildBlur: function(/*Widget*/ widget){
			// summary:
			//		Called when focus leaves a child widget to go
			//		to a sibling widget.
		},

		_getFirstFocusableChild: function(){
			return this._getNextFocusableChild(null, 1);
		},

		_getNextFocusableChild: function(child, dir){
			if(child){
				child = this._getSiblingOfChild(child, dir);
			}
			var children = this.getChildren();
			for(var i=0; i < children.length; i++){
				if(!child){
					child = children[(dir>0) ? 0 : (children.length-1)];
				}
				if(child.isFocusable()){
					return child;
				}
				child = this._getSiblingOfChild(child, dir);
			}
		}
	}
);

}

if(!dojo._hasResource["dijit.layout._LayoutWidget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout._LayoutWidget"] = true;
dojo.provide("dijit.layout._LayoutWidget");




dojo.declare("dijit.layout._LayoutWidget",
	[dijit._Widget, dijit._Container, dijit._Contained],
	{
		// summary
		//		Mixin for widgets that contain a list of children like SplitContainer.
		//		Widgets which mixin this code must define layout() to lay out the children

		isLayoutContainer: true,

		postCreate: function(){
			dojo.addClass(this.domNode, "dijitContainer");
		},

		startup: function(){
			// summary:
			//		Called after all the widgets have been instantiated and their
			//		dom nodes have been inserted somewhere under document.body.
			//
			//		Widgets should override this method to do any initialization
			//		dependent on other widgets existing, and then call
			//		this superclass method to finish things off.
			//
			//		startup() in subclasses shouldn't do anything
			//		size related because the size of the widget hasn't been set yet.

			if(this._started){ return; }
			this._started=true;

			if(this.getChildren){
				dojo.forEach(this.getChildren(), function(child){ child.startup(); });
			}

			// If I am a top level widget
			if(!this.getParent || !this.getParent()){
				// Do recursive sizing and layout of all my descendants
				// (passing in no argument to resize means that it has to glean the size itself)
				this.resize();

				// since my parent isn't a layout container, and my style is width=height=100% (or something similar),
				// then I need to watch when the window resizes, and size myself accordingly
				// (passing in no argument to resize means that it has to glean the size itself)
				this.connect(window, 'onresize', function(){this.resize();});
			}
		},

		resize: function(args){
			// summary:
			//		Explicitly set this widget's size (in pixels),
			//		and then call layout() to resize contents (and maybe adjust child widgets)
			//	
			// args: Object?
			//		{w: int, h: int, l: int, t: int}

			var node = this.domNode;

			// set margin box size, unless it wasn't specified, in which case use current size
			if(args){
				dojo.marginBox(node, args);

				// set offset of the node
				if(args.t){ node.style.top = args.t + "px"; }
				if(args.l){ node.style.left = args.l + "px"; }
			}
			// If either height or width wasn't specified by the user, then query node for it.
			// But note that setting the margin box and then immediately querying dimensions may return
			// inaccurate results, so try not to depend on it.
			var mb = dojo.mixin(dojo.marginBox(node), args||{});

			// Save the size of my content box.
			this._contentBox = dijit.layout.marginBox2contentBox(node, mb);

			// Callback for widget to adjust size of it's children
			this.layout();
		},

		layout: function(){
			//	summary
			//		Widgets override this method to size & position their contents/children.
			//		When this is called this._contentBox is guaranteed to be set (see resize()).
			//
			//		This is called after startup(), and also when the widget's size has been
			//		changed.
		}
	}
);

dijit.layout.marginBox2contentBox = function(/*DomNode*/ node, /*Object*/ mb){
	// summary:
	//		Given the margin-box size of a node, return it's content box size.
	//		Functions like dojo.contentBox() but is more reliable since it doesn't have
	//		to wait for the browser to compute sizes.
	var cs = dojo.getComputedStyle(node);
	var me=dojo._getMarginExtents(node, cs);
	var pb=dojo._getPadBorderExtents(node, cs);
	return {
		l: dojo._toPixelValue(node, cs.paddingLeft),
		t: dojo._toPixelValue(node, cs.paddingTop),
		w: mb.w - (me.w + pb.w),
		h: mb.h - (me.h + pb.h)
	};
};

(function(){
	var capitalize = function(word){
		return word.substring(0,1).toUpperCase() + word.substring(1);
	};

	var size = function(widget, dim){
		// size the child
		widget.resize ? widget.resize(dim) : dojo.marginBox(widget.domNode, dim);

		// record child's size, but favor our own numbers when we have them.
		// the browser lies sometimes
		dojo.mixin(widget, dojo.marginBox(widget.domNode));
		dojo.mixin(widget, dim);
	};

	dijit.layout.layoutChildren = function(/*DomNode*/ container, /*Object*/ dim, /*Object[]*/ children){
		/**
		 * summary
		 *		Layout a bunch of child dom nodes within a parent dom node
		 * container:
		 *		parent node
		 * dim:
		 *		{l, t, w, h} object specifying dimensions of container into which to place children
		 * children:
		 *		an array like [ {domNode: foo, layoutAlign: "bottom" }, {domNode: bar, layoutAlign: "client"} ]
		 */

		// copy dim because we are going to modify it
		dim = dojo.mixin({}, dim);

		dojo.addClass(container, "dijitLayoutContainer");

		// Move "client" elements to the end of the array for layout.  a11y dictates that the author
		// needs to be able to put them in the document in tab-order, but this algorithm requires that
		// client be last.
		children = dojo.filter(children, function(item){ return item.layoutAlign != "client"; })
			.concat(dojo.filter(children, function(item){ return item.layoutAlign == "client"; }));

		// set positions/sizes
		dojo.forEach(children, function(child){
			var elm = child.domNode,
				pos = child.layoutAlign;

			// set elem to upper left corner of unused space; may move it later
			var elmStyle = elm.style;
			elmStyle.left = dim.l+"px";
			elmStyle.top = dim.t+"px";
			elmStyle.bottom = elmStyle.right = "auto";

			dojo.addClass(elm, "dijitAlign" + capitalize(pos));

			// set size && adjust record of remaining space.
			// note that setting the width of a <div> may affect it's height.
			if(pos=="top" || pos=="bottom"){
				size(child, { w: dim.w });
				dim.h -= child.h;
				if(pos=="top"){
					dim.t += child.h;
				}else{
					elmStyle.top = dim.t + dim.h + "px";
				}
			}else if(pos=="left" || pos=="right"){
				size(child, { h: dim.h });
				dim.w -= child.w;
				if(pos=="left"){
					dim.l += child.w;
				}else{
					elmStyle.left = dim.l + dim.w + "px";
				}
			}else if(pos=="client"){
				size(child, dim);
			}
		});
	};

})();

}

if(!dojo._hasResource["dijit.layout.SplitContainer"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.SplitContainer"] = true;
dojo.provide("dijit.layout.SplitContainer");

//
// FIXME: make it prettier
// FIXME: active dragging upwards doesn't always shift other bars (direction calculation is wrong in this case)
//




dojo.declare("dijit.layout.SplitContainer",
	dijit.layout._LayoutWidget,
	{
	// summary: 
	//	A Container widget with sizing handles in-between each child
	// description:
	//		Contains multiple children widgets, all of which are displayed side by side
	//		(either horizontally or vertically); there's a bar between each of the children,
	//		and you can adjust the relative size of each child by dragging the bars.
	//
	//		You must specify a size (width and height) for the SplitContainer.
	//
	// activeSizing: Boolean
	//		If true, the children's size changes as you drag the bar;
	//		otherwise, the sizes don't change until you drop the bar (by mouse-up)
	activeSizing: false,

	// sizerWidth: Integer
	//		Size in pixels of the bar between each child
	sizerWidth: 7, // FIXME: this should be a CSS attribute (at 7 because css wants it to be 7 until we fix to css)

	// orientation: String
	//		either 'horizontal' or vertical; indicates whether the children are
	//		arranged side-by-side or up/down.
	orientation: 'horizontal',

	// persist: Boolean
	//		Save splitter positions in a cookie
	persist: true,

	postMixInProperties: function(){
		this.inherited("postMixInProperties",arguments);
		this.isHorizontal = (this.orientation == 'horizontal');
	},

	postCreate: function(){
		this.inherited("postCreate",arguments);
		this.sizers = [];
		dojo.addClass(this.domNode, "dijitSplitContainer");
		// overflow has to be explicitly hidden for splitContainers using gekko (trac #1435)
		// to keep other combined css classes from inadvertantly making the overflow visible
		if(dojo.isMozilla){
			this.domNode.style.overflow = '-moz-scrollbars-none'; // hidden doesn't work
		}

		// create the fake dragger
		if(typeof this.sizerWidth == "object"){
			try{ //FIXME: do this without a try/catch
				this.sizerWidth = parseInt(this.sizerWidth.toString());
			}catch(e){ this.sizerWidth = 7; }
		}
		var sizer = this.virtualSizer = document.createElement('div');
		sizer.style.position = 'relative';

		// #1681: work around the dreaded 'quirky percentages in IE' layout bug
		// If the splitcontainer's dimensions are specified in percentages, it
		// will be resized when the virtualsizer is displayed in _showSizingLine
		// (typically expanding its bounds unnecessarily). This happens because
		// we use position: relative for .dijitSplitContainer.
		// The workaround: instead of changing the display style attribute,
		// switch to changing the zIndex (bring to front/move to back)

		sizer.style.zIndex = 10;
		sizer.className = this.isHorizontal ? 'dijitSplitContainerVirtualSizerH' : 'dijitSplitContainerVirtualSizerV';
		this.domNode.appendChild(sizer);
		dojo.setSelectable(sizer, false);
	},

	startup: function(){
		if(this._started){ return; }
		dojo.forEach(this.getChildren(), function(child, i, children){
			// attach the children and create the draggers
			this._injectChild(child);

			if(i < children.length-1){
				this._addSizer();
			}
		}, this);

		if(this.persist){
			this._restoreState();
		}
		this.inherited("startup",arguments); 
		this._started = true;
	},

	_injectChild: function(child){
		child.domNode.style.position = "absolute";
		dojo.addClass(child.domNode, "dijitSplitPane");
	},

	_addSizer: function(){
		var i = this.sizers.length;

		// TODO: use a template for this!!!
		var sizer = this.sizers[i] = document.createElement('div');
		sizer.className = this.isHorizontal ? 'dijitSplitContainerSizerH' : 'dijitSplitContainerSizerV';

		// add the thumb div
		var thumb = document.createElement('div');
		thumb.className = 'thumb';
		sizer.appendChild(thumb);

		// FIXME: are you serious? why aren't we using mover start/stop combo?
		var self = this;
		var handler = (function(){ var sizer_i = i; return function(e){ self.beginSizing(e, sizer_i); } })();
		dojo.connect(sizer, "onmousedown", handler);

		this.domNode.appendChild(sizer);
		dojo.setSelectable(sizer, false);
	},

	removeChild: function(widget){
		// summary: Remove sizer, but only if widget is really our child and
		// we have at least one sizer to throw away
		if(this.sizers.length && dojo.indexOf(this.getChildren(), widget) != -1){
			var i = this.sizers.length - 1;
			dojo._destroyElement(this.sizers[i]);
			this.sizers.length--;
		}

		// Remove widget and repaint
		this.inherited("removeChild",arguments); 
		if(this._started){
			this.layout();
		}
	},

	addChild: function(/*Widget*/ child, /*Integer?*/ insertIndex){
		// summary: Add a child widget to the container
		// child: a widget to add
		// insertIndex: postion in the "stack" to add the child widget
		
		this.inherited("addChild",arguments); 

		if(this._started){
			// Do the stuff that startup() does for each widget
			this._injectChild(child);
			var children = this.getChildren();
			if(children.length > 1){
				this._addSizer();
			}

			// and then reposition (ie, shrink) every pane to make room for the new guy
			this.layout();
		}
	},

	layout: function(){
		// summary:
		//		Do layout of panels

		// base class defines this._contentBox on initial creation and also
		// on resize
		this.paneWidth = this._contentBox.w;
		this.paneHeight = this._contentBox.h;

		var children = this.getChildren();
		if(!children.length){ return; }

		//
		// calculate space
		//

		var space = this.isHorizontal ? this.paneWidth : this.paneHeight;
		if(children.length > 1){
			space -= this.sizerWidth * (children.length - 1);
		}

		//
		// calculate total of SizeShare values
		//
		var outOf = 0;
		dojo.forEach(children, function(child){
			outOf += child.sizeShare;
		});

		//
		// work out actual pixels per sizeshare unit
		//
		var pixPerUnit = space / outOf;

		//
		// set the SizeActual member of each pane
		//
		var totalSize = 0;
		dojo.forEach(children.slice(0, children.length - 1), function(child){
			var size = Math.round(pixPerUnit * child.sizeShare);
			child.sizeActual = size;
			totalSize += size;
		});

		children[children.length-1].sizeActual = space - totalSize;

		//
		// make sure the sizes are ok
		//
		this._checkSizes();

		//
		// now loop, positioning each pane and letting children resize themselves
		//

		var pos = 0;
		var size = children[0].sizeActual;
		this._movePanel(children[0], pos, size);
		children[0].position = pos;
		pos += size;

		// if we don't have any sizers, our layout method hasn't been called yet
		// so bail until we are called..TODO: REVISIT: need to change the startup
		// algorithm to guaranteed the ordering of calls to layout method
		if(!this.sizers){
			return;
		}

		dojo.some(children.slice(1), function(child, i){
			// error-checking
			if(!this.sizers[i]){
				return true;
			}
			// first we position the sizing handle before this pane
			this._moveSlider(this.sizers[i], pos, this.sizerWidth);
			this.sizers[i].position = pos;
			pos += this.sizerWidth;

			size = child.sizeActual;
			this._movePanel(child, pos, size);
			child.position = pos;
			pos += size;
		}, this);
	},

	_movePanel: function(panel, pos, size){
		if(this.isHorizontal){
			panel.domNode.style.left = pos + 'px';	// TODO: resize() takes l and t parameters too, don't need to set manually
			panel.domNode.style.top = 0;
			var box = {w: size, h: this.paneHeight};
			if(panel.resize){
				panel.resize(box);
			}else{
				dojo.marginBox(panel.domNode, box);
			}
		}else{
			panel.domNode.style.left = 0;	// TODO: resize() takes l and t parameters too, don't need to set manually
			panel.domNode.style.top = pos + 'px';
			var box = {w: this.paneWidth, h: size};
			if(panel.resize){
				panel.resize(box);
			}else{
				dojo.marginBox(panel.domNode, box);
			}
		}
	},

	_moveSlider: function(slider, pos, size){
		if(this.isHorizontal){
			slider.style.left = pos + 'px';
			slider.style.top = 0;
			dojo.marginBox(slider, { w: size, h: this.paneHeight });
		}else{
			slider.style.left = 0;
			slider.style.top = pos + 'px';
			dojo.marginBox(slider, { w: this.paneWidth, h: size });
		}
	},

	_growPane: function(growth, pane){
		if(growth > 0){
			if(pane.sizeActual > pane.sizeMin){
				if((pane.sizeActual - pane.sizeMin) > growth){

					// stick all the growth in this pane
					pane.sizeActual = pane.sizeActual - growth;
					growth = 0;
				}else{
					// put as much growth in here as we can
					growth -= pane.sizeActual - pane.sizeMin;
					pane.sizeActual = pane.sizeMin;
				}
			}
		}
		return growth;
	},

	_checkSizes: function(){

		var totalMinSize = 0;
		var totalSize = 0;
		var children = this.getChildren();

		dojo.forEach(children, function(child){
			totalSize += child.sizeActual;
			totalMinSize += child.sizeMin;
		});

		// only make adjustments if we have enough space for all the minimums

		if(totalMinSize <= totalSize){

			var growth = 0;

			dojo.forEach(children, function(child){
				if(child.sizeActual < child.sizeMin){
					growth += child.sizeMin - child.sizeActual;
					child.sizeActual = child.sizeMin;
				}
			});

			if(growth > 0){
				var list = this.isDraggingLeft ? children.reverse() : children;
				dojo.forEach(list, function(child){
					growth = this._growPane(growth, child);
				}, this);
			}
		}else{
			dojo.forEach(children, function(child){
				child.sizeActual = Math.round(totalSize * (child.sizeMin / totalMinSize));
			});
		}
	},

	beginSizing: function(e, i){
		var children = this.getChildren();
		this.paneBefore = children[i];
		this.paneAfter = children[i+1];

		this.isSizing = true;
		this.sizingSplitter = this.sizers[i];

		if(!this.cover){
			this.cover = dojo.doc.createElement('div');
			this.domNode.appendChild(this.cover);
			var s = this.cover.style;
			s.position = 'absolute';
			s.zIndex = 1;
			s.top = 0;
			s.left = 0;
			s.width = "100%";
			s.height = "100%";
		}else{
			this.cover.style.zIndex = 1;
		}
		this.sizingSplitter.style.zIndex = 2;

		// TODO: REVISIT - we want MARGIN_BOX and core hasn't exposed that yet (but can't we use it anyway if we pay attention? we do elsewhere.)
		this.originPos = dojo.coords(children[0].domNode, true);
		if(this.isHorizontal){
			var client = (e.layerX ? e.layerX : e.offsetX);
			var screen = e.pageX;
			this.originPos = this.originPos.x;
		}else{
			var client = (e.layerY ? e.layerY : e.offsetY);
			var screen = e.pageY;
			this.originPos = this.originPos.y;
		}
		this.startPoint = this.lastPoint = screen;
		this.screenToClientOffset = screen - client;
		this.dragOffset = this.lastPoint - this.paneBefore.sizeActual - this.originPos - this.paneBefore.position;

		if(!this.activeSizing){
			this._showSizingLine();
		}

		//					
		// attach mouse events
		//
		this._connects = [];
		this._connects.push(dojo.connect(document.documentElement, "onmousemove", this, "changeSizing"));
		this._connects.push(dojo.connect(document.documentElement, "onmouseup", this, "endSizing"));

		dojo.stopEvent(e);
	},

	changeSizing: function(e){
		if(!this.isSizing){ return; }
		this.lastPoint = this.isHorizontal ? e.pageX : e.pageY;
		this.movePoint();
		if(this.activeSizing){
			this._updateSize();
		}else{
			this._moveSizingLine();
		}
		dojo.stopEvent(e);
	},

	endSizing: function(e){
		if(!this.isSizing){ return; }
		if(this.cover){
			this.cover.style.zIndex = -1;
		}
		if(!this.activeSizing){
			this._hideSizingLine();
		}

		this._updateSize();

		this.isSizing = false;

		if(this.persist){
			this._saveState(this);
		}

		dojo.forEach(this._connects,dojo.disconnect); 
	},

	movePoint: function(){

		// make sure lastPoint is a legal point to drag to
		var p = this.lastPoint - this.screenToClientOffset;

		var a = p - this.dragOffset;
		a = this.legaliseSplitPoint(a);
		p = a + this.dragOffset;

		this.lastPoint = p + this.screenToClientOffset;
	},

	legaliseSplitPoint: function(a){

		a += this.sizingSplitter.position;

		this.isDraggingLeft = !!(a > 0);

		if(!this.activeSizing){
			var min = this.paneBefore.position + this.paneBefore.sizeMin;
			if(a < min){
				a = min;
			}

			var max = this.paneAfter.position + (this.paneAfter.sizeActual - (this.sizerWidth + this.paneAfter.sizeMin));
			if(a > max){
				a = max;
			}
		}

		a -= this.sizingSplitter.position;

		this._checkSizes();

		return a;
	},

	_updateSize: function(){
	//FIXME: sometimes this.lastPoint is NaN
		var pos = this.lastPoint - this.dragOffset - this.originPos;

		var start_region = this.paneBefore.position;
		var end_region   = this.paneAfter.position + this.paneAfter.sizeActual;

		this.paneBefore.sizeActual = pos - start_region;
		this.paneAfter.position	= pos + this.sizerWidth;
		this.paneAfter.sizeActual  = end_region - this.paneAfter.position;

		dojo.forEach(this.getChildren(), function(child){
			child.sizeShare = child.sizeActual;
		});

		if(this._started){
			this.layout();
		}
	},

	_showSizingLine: function(){

		this._moveSizingLine();

		dojo.marginBox(this.virtualSizer,
			this.isHorizontal ? { w: this.sizerWidth, h: this.paneHeight } : { w: this.paneWidth, h: this.sizerWidth });

		this.virtualSizer.style.display = 'block';
	},

	_hideSizingLine: function(){
		this.virtualSizer.style.display = 'none';
	},

	_moveSizingLine: function(){
		var pos = (this.lastPoint - this.startPoint) + this.sizingSplitter.position;
		dojo.style(this.virtualSizer,(this.isHorizontal ? "left" : "top"),pos+"px");
		// this.virtualSizer.style[ this.isHorizontal ? "left" : "top" ] = pos + 'px'; // FIXME: remove this line if the previous is better
	},

	_getCookieName: function(i){
		return this.id + "_" + i;
	},

	_restoreState: function(){
		dojo.forEach(this.getChildren(), function(child, i){
			var cookieName = this._getCookieName(i);
			var cookieValue = dojo.cookie(cookieName);
			if(cookieValue){
				var pos = parseInt(cookieValue);
				if(typeof pos == "number"){
					child.sizeShare = pos;
				}
			}
		}, this);
	},

	_saveState: function(){
		dojo.forEach(this.getChildren(), function(child, i){
			dojo.cookie(this._getCookieName(i), child.sizeShare);
		}, this);
	}
});

// These arguments can be specified for the children of a SplitContainer.
// Since any widget can be specified as a SplitContainer child, mix them
// into the base widget class.  (This is a hack, but it's effective.)
dojo.extend(dijit._Widget, {
	// sizeMin: Integer
	//	Minimum size (width or height) of a child of a SplitContainer.
	//	The value is relative to other children's sizeShare properties.
	sizeMin: 10,

	// sizeShare: Integer
	//	Size (width or height) of a child of a SplitContainer.
	//	The value is relative to other children's sizeShare properties.
	//	For example, if there are two children and each has sizeShare=10, then
	//	each takes up 50% of the available space.
	sizeShare: 10
});

}

if(!dojo._hasResource["dojo.date.stamp"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.date.stamp"] = true;
dojo.provide("dojo.date.stamp");

// Methods to convert dates to or from a wire (string) format using well-known conventions

dojo.date.stamp.fromISOString = function(/*String*/formattedString, /*Number?*/defaultTime){
	//	summary:
	//		Returns a Date object given a string formatted according to a subset of the ISO-8601 standard.
	//
	//	description:
	//		Accepts a string formatted according to a profile of ISO8601 as defined by
	//		RFC3339 (http://www.ietf.org/rfc/rfc3339.txt), except that partial input is allowed.
	//		Can also process dates as specified by http://www.w3.org/TR/NOTE-datetime
	//		The following combinations are valid:
	// 			* dates only
	//				yyyy
	//				yyyy-MM
	//				yyyy-MM-dd
	// 			* times only, with an optional time zone appended
	//				THH:mm
	//				THH:mm:ss
	//				THH:mm:ss.SSS
	// 			* and "datetimes" which could be any combination of the above
	//		timezones may be specified as Z (for UTC) or +/- followed by a time expression HH:mm
	//		Assumes the local time zone if not specified.  Does not validate.  Improperly formatted
	//		input may return null.  Arguments which are out of bounds will be handled
	// 		by the Date constructor (e.g. January 32nd typically gets resolved to February 1st)
	//
  	//	formattedString:
	//		A string such as 2005-06-30T08:05:00-07:00 or 2005-06-30 or T08:05:00
	//
	//	defaultTime:
	//		Used for defaults for fields omitted in the formattedString.
	//		Uses 1970-01-01T00:00:00.0Z by default.

	if(!dojo.date.stamp._isoRegExp){
		dojo.date.stamp._isoRegExp =
//TODO: could be more restrictive and check for 00-59, etc.
			/^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
	}

	var match = dojo.date.stamp._isoRegExp.exec(formattedString);
	var result = null;

	if(match){
		match.shift();
		match[1] && match[1]--; // Javascript Date months are 0-based
		match[6] && (match[6] *= 1000); // Javascript Date expects fractional seconds as milliseconds

		if(defaultTime){
			// mix in defaultTime.  Relatively expensive, so use || operators for the fast path of defaultTime === 0
			defaultTime = new Date(defaultTime);
			dojo.map(["FullYear", "Month", "Date", "Hours", "Minutes", "Seconds", "Milliseconds"], function(prop){
				return defaultTime["get" + prop]();
			}).forEach(function(value, index){
				if(match[index] === undefined){
					match[index] = value;
				}
			});
		}
		result = new Date(match[0]||1970, match[1]||0, match[2]||0, match[3]||0, match[4]||0, match[5]||0, match[6]||0);

		var offset = 0;
		var zoneSign = match[7] && match[7].charAt(0);
		if(zoneSign != 'Z'){
			offset = ((match[8] || 0) * 60) + (Number(match[9]) || 0);
			if(zoneSign != '-'){ offset *= -1; }
		}
		if(zoneSign){
			offset -= result.getTimezoneOffset();
		}
		if(offset){
			result.setTime(result.getTime() + offset * 60000);
		}
	}

	return result; // Date or null
}

dojo.date.stamp.toISOString = function(/*Date*/dateObject, /*Object?*/options){
	//	summary:
	//		Format a Date object as a string according a subset of the ISO-8601 standard
	//
	//	description:
	//		When options.selector is omitted, output follows RFC3339 (http://www.ietf.org/rfc/rfc3339.txt)
	//		The local time zone is included as an offset from GMT, except when selector=='time' (time without a date)
	//		Does not check bounds.
	//
	//	dateObject:
	//		A Date object
	//
	//	object {selector: string, zulu: boolean, milliseconds: boolean}
	//		selector- "date" or "time" for partial formatting of the Date object.
	//			Both date and time will be formatted by default.
	//		zulu- if true, UTC/GMT is used for a timezone
	//		milliseconds- if true, output milliseconds

	var _ = function(n){ return (n < 10) ? "0" + n : n; }
	options = options || {};
	var formattedDate = [];
	var getter = options.zulu ? "getUTC" : "get";
	var date = "";
	if(options.selector != "time"){
		date = [dateObject[getter+"FullYear"](), _(dateObject[getter+"Month"]()+1), _(dateObject[getter+"Date"]())].join('-');
	}
	formattedDate.push(date);
	if(options.selector != "date"){
		var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
		var millis = dateObject[getter+"Milliseconds"]();
		if(options.milliseconds){
			time += "."+ (millis < 100 ? "0" : "") + _(millis);
		}
		if(options.zulu){
			time += "Z";
		}else if(options.selector != "time"){
			var timezoneOffset = dateObject.getTimezoneOffset();
			var absOffset = Math.abs(timezoneOffset);
			time += (timezoneOffset > 0 ? "-" : "+") + 
				_(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
		}
		formattedDate.push(time);
	}
	return formattedDate.join('T'); // String
}

}

if(!dojo._hasResource["dojo.parser"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.parser"] = true;
dojo.provide("dojo.parser");


dojo.parser = new function(){

	var d = dojo;

	function val2type(/*Object*/ value){
		// summary:
		//		Returns name of type of given value.

		if(d.isString(value)){ return "string"; }
		if(typeof value == "number"){ return "number"; }
		if(typeof value == "boolean"){ return "boolean"; }
		if(d.isFunction(value)){ return "function"; }
		if(d.isArray(value)){ return "array"; } // typeof [] == "object"
		if(value instanceof Date) { return "date"; } // assume timestamp
		if(value instanceof d._Url){ return "url"; }
		return "object";
	}

	function str2obj(/*String*/ value, /*String*/ type){
		// summary:
		//		Convert given string value to given type
		switch(type){
			case "string":
				return value;
			case "number":
				return value.length ? Number(value) : NaN;
			case "boolean":
				// for checked/disabled value might be "" or "checked".  interpret as true.
				return typeof value == "boolean" ? value : !(value.toLowerCase()=="false");
			case "function":
				if(d.isFunction(value)){
					// IE gives us a function, even when we say something like onClick="foo"
					// (in which case it gives us an invalid function "function(){ foo }"). 
					//  Therefore, convert to string
					value=value.toString();
					value=d.trim(value.substring(value.indexOf('{')+1, value.length-1));
				}
				try{
					if(value.search(/[^\w\.]+/i) != -1){
						// TODO: "this" here won't work
						value = d.parser._nameAnonFunc(new Function(value), this);
					}
					return d.getObject(value, false);
				}catch(e){ return new Function(); }
			case "array":
				return value.split(/\s*,\s*/);
			case "date":
				switch(value){
					case "": return new Date("");	// the NaN of dates
					case "now": return new Date();	// current date
					default: return d.date.stamp.fromISOString(value);
				}
			case "url":
				return d.baseUrl + value;
			default:
				return d.fromJson(value);
		}
	}

	var instanceClasses = {
		// map from fully qualified name (like "dijit.Button") to structure like
		// { cls: dijit.Button, params: {label: "string", disabled: "boolean"} }
	};
	
	function getClassInfo(/*String*/ className){
		// className:
		//		fully qualified name (like "dijit.Button")
		// returns:
		//		structure like
		//			{ 
		//				cls: dijit.Button, 
		//				params: { label: "string", disabled: "boolean"}
		//			}

		if(!instanceClasses[className]){
			// get pointer to widget class
			var cls = d.getObject(className);
			if(!d.isFunction(cls)){
				throw new Error("Could not load class '" + className +
					"'. Did you spell the name correctly and use a full path, like 'dijit.form.Button'?");
			}
			var proto = cls.prototype;
	
			// get table of parameter names & types
			var params={};
			for(var name in proto){
				if(name.charAt(0)=="_"){ continue; } 	// skip internal properties
				var defVal = proto[name];
				params[name]=val2type(defVal);
			}

			instanceClasses[className] = { cls: cls, params: params };
		}
		return instanceClasses[className];
	}

	this._functionFromScript = function(script){
		var preamble = "";
		var suffix = "";
		var argsStr = script.getAttribute("args");
		if(argsStr){
			d.forEach(argsStr.split(/\s*,\s*/), function(part, idx){
				preamble += "var "+part+" = arguments["+idx+"]; ";
			});
		}
		var withStr = script.getAttribute("with");
		if(withStr && withStr.length){
			d.forEach(withStr.split(/\s*,\s*/), function(part){
				preamble += "with("+part+"){";
				suffix += "}";
			});
		}
		return new Function(preamble+script.innerHTML+suffix);
	}

	this.instantiate = function(/* Array */nodes){
		// summary:
		//		Takes array of nodes, and turns them into class instances and
		//		potentially calls a layout method to allow them to connect with
		//		any children		
		var thelist = [];
		d.forEach(nodes, function(node){
			if(!node){ return; }
			var type = node.getAttribute("dojoType");
			if((!type)||(!type.length)){ return; }
			var clsInfo = getClassInfo(type);
			var clazz = clsInfo.cls;
			var ps = clazz._noScript||clazz.prototype._noScript;

			// read parameters (ie, attributes).
			// clsInfo.params lists expected params like {"checked": "boolean", "n": "number"}
			var params = {};
			var attributes = node.attributes;
			for(var name in clsInfo.params){
				var item = attributes.getNamedItem(name);
				if(!item || (!item.specified && (!dojo.isIE || name.toLowerCase()!="value"))){ continue; }
				var value = item.value;
				// Deal with IE quirks for 'class' and 'style'
				switch(name){
				case "class":
					value = node.className;
					break;
				case "style":
					value = node.style && node.style.cssText; // FIXME: Opera?
				}
				var _type = clsInfo.params[name];
				params[name] = str2obj(value, _type);
			}

			// Process <script type="dojo/*"> script tags
			// <script type="dojo/method" event="foo"> tags are added to params, and passed to
			// the widget on instantiation.
			// <script type="dojo/method"> tags (with no event) are executed after instantiation
			// <script type="dojo/connect" event="foo"> tags are dojo.connected after instantiation
			if(!ps){
				var connects = [],	// functions to connect after instantiation
					calls = [];		// functions to call after instantiation

				d.query("> script[type^='dojo/']", node).orphan().forEach(function(script){
					var event = script.getAttribute("event"),
						type = script.getAttribute("type"),
						nf = d.parser._functionFromScript(script);
					if(event){
						if(type == "dojo/connect"){
							connects.push({event: event, func: nf});
						}else{
							params[event] = nf;
						}
					}else{
						calls.push(nf);
					}
				});
			}

			var markupFactory = clazz["markupFactory"];
			if(!markupFactory && clazz["prototype"]){
				markupFactory = clazz.prototype["markupFactory"];
			}
			// create the instance
			var instance = markupFactory ? markupFactory(params, node, clazz) : new clazz(params, node);
			thelist.push(instance);

			// map it to the JS namespace if that makes sense
			var jsname = node.getAttribute("jsId");
			if(jsname){
				d.setObject(jsname, instance);
			}

			// process connections and startup functions
			if(!ps){
				dojo.forEach(connects, function(connect){
					dojo.connect(instance, connect.event, null, connect.func);
				});
				dojo.forEach(calls, function(func){
					func.call(instance);
				});
			}
		});

		// Call startup on each top level instance if it makes sense (as for
		// widgets).  Parent widgets will recursively call startup on their
		// (non-top level) children
		d.forEach(thelist, function(instance){
			if(	instance  && 
				(instance.startup) && 
				((!instance.getParent) || (!instance.getParent()))
			){
				instance.startup();
			}
		});
		return thelist;
	};

	this.parse = function(/*DomNode?*/ rootNode){
		// summary:
		//		Search specified node (or root node) recursively for class instances,
		//		and instantiate them Searches for
		//		dojoType="qualified.class.name"
		var list = d.query('[dojoType]', rootNode);
		// go build the object instances
		var instances = this.instantiate(list);
		return instances;
	};
}();

//Register the parser callback. It should be the first callback
//after the a11y test.

(function(){
	var parseRunner = function(){ 
		if(djConfig["parseOnLoad"] == true){
			dojo.parser.parse(); 
		}
	};

	// FIXME: need to clobber cross-dependency!!
	if(dojo.exists("dijit.wai.onload") && (dijit.wai.onload === dojo._loaders[0])){
		dojo._loaders.splice(1, 0, parseRunner);
	}else{
		dojo._loaders.unshift(parseRunner);
	}
})();

//TODO: ported from 0.4.x Dojo.  Can we reduce this?
dojo.parser._anonCtr = 0;
dojo.parser._anon = {}; // why is this property required?
dojo.parser._nameAnonFunc = function(/*Function*/anonFuncPtr, /*Object*/thisObj){
	// summary:
	//		Creates a reference to anonFuncPtr in thisObj with a completely
	//		unique name. The new name is returned as a String. 
	var jpn = "$joinpoint";
	var nso = (thisObj|| dojo.parser._anon);
	if(dojo.isIE){
		var cn = anonFuncPtr["__dojoNameCache"];
		if(cn && nso[cn] === anonFuncPtr){
			return anonFuncPtr["__dojoNameCache"];
		}
	}
	var ret = "__"+dojo.parser._anonCtr++;
	while(typeof nso[ret] != "undefined"){
		ret = "__"+dojo.parser._anonCtr++;
	}
	nso[ret] = anonFuncPtr;
	return ret; // String
}

}

if(!dojo._hasResource["dojo.string"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.string"] = true;
dojo.provide("dojo.string");

dojo.string.pad = function(/*String*/text, /*int*/size, /*String?*/ch, /*boolean?*/end){
	// summary:
	//		Pad a string to guarantee that it is at least 'size' length by
	//		filling with the character 'c' at either the start or end of the
	//		string. Pads at the start, by default.
	// text: the string to pad
	// size: length to provide padding
	// ch: character to pad, defaults to '0'
	// end: adds padding at the end if true, otherwise pads at start

	var out = String(text);
	if(!ch){
		ch = '0';
	}
	while(out.length < size){
		if(end){
			out += ch;
		}else{
			out = ch + out;
		}
	}
	return out;	// String
};

dojo.string.substitute = function(	/*String*/template, 
									/*Object or Array*/map, 
									/*Function?*/transform, 
									/*Object?*/thisObject){
	// summary:
	//		Performs parameterized substitutions on a string. Throws an
	//		exception if any parameter is unmatched.
	// description:
	//		For example,
	//		|	dojo.string.substitute("File '${0}' is not found in directory '${1}'.",["foo.html","/temp"]);
	//		|	dojo.string.substitute("File '${name}' is not found in directory '${info.dir}'.",{name: "foo.html", info: {dir: "/temp"}});
	//		both return
	//			"File 'foo.html' is not found in directory '/temp'."
	// template: 
	//		a string with expressions in the form ${key} to be replaced or
	//		${key:format} which specifies a format function.  NOTE syntax has
	//		changed from %{key}
	// map: where to look for substitutions
	// transform: 
	//		a function to process all parameters before substitution takes
	//		place, e.g. dojo.string.encodeXML
	// thisObject: 
	//		where to look for optional format function; default to the global
	//		namespace

	return template.replace(/\$\{([^\s\:\}]+)(?:\:([^\s\:\}]+))?\}/g, function(match, key, format){
		var value = dojo.getObject(key,false,map);
		if(format){ value = dojo.getObject(format,false,thisObject)(value);}
		if(transform){ value = transform(value, key); }
		return value.toString();
	}); // string
};

dojo.string.trim = function(/*String*/ str){
	// summary: trims whitespaces from both sides of the string
	// description:
	//	This version of trim() was taken from Steven Levithan's blog: 
	//	http://blog.stevenlevithan.com/archives/faster-trim-javascript.
	//	The short yet good-performing version of this function is 
	//	dojo.trim(), which is part of the base.
	str = str.replace(/^\s+/, '');
	for(var i = str.length - 1; i > 0; i--){
		if(/\S/.test(str.charAt(i))){
			str = str.substring(0, i + 1);
			break;
		}
	}
	return str;	// String
};

}

if(!dojo._hasResource["dojo.i18n"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.i18n"] = true;
dojo.provide("dojo.i18n");

dojo.i18n.getLocalization = function(/*String*/packageName, /*String*/bundleName, /*String?*/locale){
	//	summary:
	//		Returns an Object containing the localization for a given resource
	//		bundle in a package, matching the specified locale.
	//	description:
	//		Returns a hash containing name/value pairs in its prototypesuch
	//		that values can be easily overridden.  Throws an exception if the
	//		bundle is not found.  Bundle must have already been loaded by
	//		dojo.requireLocalization() or by a build optimization step.  NOTE:
	//		try not to call this method as part of an object property
	//		definition (var foo = { bar: dojo.i18n.getLocalization() }).  In
	//		some loading situations, the bundle may not be available in time
	//		for the object definition.  Instead, call this method inside a
	//		function that is run after all modules load or the page loads (like
	//		in dojo.adOnLoad()), or in a widget lifecycle method.
	//	packageName:
	//		package which is associated with this resource
	//	bundleName:
	//		the base filename of the resource bundle (without the ".js" suffix)
	//	locale:
	//		the variant to load (optional).  By default, the locale defined by
	//		the host environment: dojo.locale

	locale = dojo.i18n.normalizeLocale(locale);

	// look for nearest locale match
	var elements = locale.split('-');
	var module = [packageName,"nls",bundleName].join('.');
	var bundle = dojo._loadedModules[module];
	if(bundle){
		var localization;
		for(var i = elements.length; i > 0; i--){
			var loc = elements.slice(0, i).join('_');
			if(bundle[loc]){
				localization = bundle[loc];
				break;
			}
		}
		if(!localization){
			localization = bundle.ROOT;
		}

		// make a singleton prototype so that the caller won't accidentally change the values globally
		if(localization){
			var clazz = function(){};
			clazz.prototype = localization;
			return new clazz(); // Object
		}
	}

	throw new Error("Bundle not found: " + bundleName + " in " + packageName+" , locale=" + locale);
};

dojo.i18n.normalizeLocale = function(/*String?*/locale){
	//	summary:
	//		Returns canonical form of locale, as used by Dojo.
	//
	//  description:
	//		All variants are case-insensitive and are separated by '-' as specified in RFC 3066.
	//		If no locale is specified, the dojo.locale is returned.  dojo.locale is defined by
	//		the user agent's locale unless overridden by djConfig.

	var result = locale ? locale.toLowerCase() : dojo.locale;
	if(result == "root"){
		result = "ROOT";
	}
	return result; // String
};

dojo.i18n._requireLocalization = function(/*String*/moduleName, /*String*/bundleName, /*String?*/locale, /*String?*/availableFlatLocales){
	//	summary:
	//		See dojo.requireLocalization()
	//	description:
	// 		Called by the bootstrap, but factored out so that it is only
	// 		included in the build when needed.

	var targetLocale = dojo.i18n.normalizeLocale(locale);
 	var bundlePackage = [moduleName, "nls", bundleName].join(".");
	// NOTE: 
	//		When loading these resources, the packaging does not match what is
	//		on disk.  This is an implementation detail, as this is just a
	//		private data structure to hold the loaded resources.  e.g.
	//		tests/hello/nls/en-us/salutations.js is loaded as the object
	//		tests.hello.nls.salutations.en_us={...} The structure on disk is
	//		intended to be most convenient for developers and translators, but
	//		in memory it is more logical and efficient to store in a different
	//		order.  Locales cannot use dashes, since the resulting path will
	//		not evaluate as valid JS, so we translate them to underscores.
	
	//Find the best-match locale to load if we have available flat locales.
	var bestLocale = "";
	if(availableFlatLocales){
		var flatLocales = availableFlatLocales.split(",");
		for(var i = 0; i < flatLocales.length; i++){
			//Locale must match from start of string.
			if(targetLocale.indexOf(flatLocales[i]) == 0){
				if(flatLocales[i].length > bestLocale.length){
					bestLocale = flatLocales[i];
				}
			}
		}
		if(!bestLocale){
			bestLocale = "ROOT";
		}		
	}

	//See if the desired locale is already loaded.
	var tempLocale = availableFlatLocales ? bestLocale : targetLocale;
	var bundle = dojo._loadedModules[bundlePackage];
	var localizedBundle = null;
	if(bundle){
		if(djConfig.localizationComplete && bundle._built){return;}
		var jsLoc = tempLocale.replace(/-/g, '_');
		var translationPackage = bundlePackage+"."+jsLoc;
		localizedBundle = dojo._loadedModules[translationPackage];
	}

	if(!localizedBundle){
		bundle = dojo["provide"](bundlePackage);
		var syms = dojo._getModuleSymbols(moduleName);
		var modpath = syms.concat("nls").join("/");
		var parent;

		dojo.i18n._searchLocalePath(tempLocale, availableFlatLocales, function(loc){
			var jsLoc = loc.replace(/-/g, '_');
			var translationPackage = bundlePackage + "." + jsLoc;
			var loaded = false;
			if(!dojo._loadedModules[translationPackage]){
				// Mark loaded whether it's found or not, so that further load attempts will not be made
				dojo["provide"](translationPackage);
				var module = [modpath];
				if(loc != "ROOT"){module.push(loc);}
				module.push(bundleName);
				var filespec = module.join("/") + '.js';
				loaded = dojo._loadPath(filespec, null, function(hash){
					// Use singleton with prototype to point to parent bundle, then mix-in result from loadPath
					var clazz = function(){};
					clazz.prototype = parent;
					bundle[jsLoc] = new clazz();
					for(var j in hash){ bundle[jsLoc][j] = hash[j]; }
				});
			}else{
				loaded = true;
			}
			if(loaded && bundle[jsLoc]){
				parent = bundle[jsLoc];
			}else{
				bundle[jsLoc] = parent;
			}
			
			if(availableFlatLocales){
				//Stop the locale path searching if we know the availableFlatLocales, since
				//the first call to this function will load the only bundle that is needed.
				return true;
			}
		});
	}

	//Save the best locale bundle as the target locale bundle when we know the
	//the available bundles.
	if(availableFlatLocales && targetLocale != bestLocale){
		bundle[targetLocale.replace(/-/g, '_')] = bundle[bestLocale.replace(/-/g, '_')];
	}
};

(function(){
	// If other locales are used, dojo.requireLocalization should load them as
	// well, by default. 
	// 
	// Override dojo.requireLocalization to do load the default bundle, then
	// iterate through the extraLocale list and load those translations as
	// well, unless a particular locale was requested.

	var extra = djConfig.extraLocale;
	if(extra){
		if(!extra instanceof Array){
			extra = [extra];
		}

		var req = dojo.i18n._requireLocalization;
		dojo.i18n._requireLocalization = function(m, b, locale, availableFlatLocales){
			req(m,b,locale, availableFlatLocales);
			if(locale){return;}
			for(var i=0; i<extra.length; i++){
				req(m,b,extra[i], availableFlatLocales);
			}
		};
	}
})();

dojo.i18n._searchLocalePath = function(/*String*/locale, /*Boolean*/down, /*Function*/searchFunc){
	//	summary:
	//		A helper method to assist in searching for locale-based resources.
	//		Will iterate through the variants of a particular locale, either up
	//		or down, executing a callback function.  For example, "en-us" and
	//		true will try "en-us" followed by "en" and finally "ROOT".

	locale = dojo.i18n.normalizeLocale(locale);

	var elements = locale.split('-');
	var searchlist = [];
	for(var i = elements.length; i > 0; i--){
		searchlist.push(elements.slice(0, i).join('-'));
	}
	searchlist.push(false);
	if(down){searchlist.reverse();}

	for(var j = searchlist.length - 1; j >= 0; j--){
		var loc = searchlist[j] || "ROOT";
		var stop = searchFunc(loc);
		if(stop){ break; }
	}
};

dojo.i18n._preloadLocalizations = function(/*String*/bundlePrefix, /*Array*/localesGenerated){
	//	summary:
	//		Load built, flattened resource bundles, if available for all
	//		locales used in the page. Only called by built layer files.

	function preload(locale){
		locale = dojo.i18n.normalizeLocale(locale);
		dojo.i18n._searchLocalePath(locale, true, function(loc){
			for(var i=0; i<localesGenerated.length;i++){
				if(localesGenerated[i] == loc){
					dojo["require"](bundlePrefix+"_"+loc);
					return true; // Boolean
				}
			}
			return false; // Boolean
		});
	}
	preload();
	var extra = djConfig.extraLocale||[];
	for(var i=0; i<extra.length; i++){
		preload(extra[i]);
	}
};

}

if(!dojo._hasResource["dijit.layout.ContentPane"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.ContentPane"] = true;
dojo.provide("dijit.layout.ContentPane");








dojo.declare(
	"dijit.layout.ContentPane",
	dijit._Widget,
{
	// summary:
	//		A widget that acts as a Container for other widgets, and includes a ajax interface
	// description:
	//		A widget that can be used as a standalone widget
	//		or as a baseclass for other widgets
	//		Handles replacement of document fragment using either external uri or javascript
	//		generated markup or DOM content, instantiating widgets within that content.
	//		Don't confuse it with an iframe, it only needs/wants document fragments.
	//		It's useful as a child of LayoutContainer, SplitContainer, or TabContainer.
	//		But note that those classes can contain any widget as a child.
	// example:
	//		Some quick samples:
	//		To change the innerHTML use .setContent('<b>new content</b>')
	//
	//		Or you can send it a NodeList, .setContent(dojo.query('div [class=selected]', userSelection))
	//		please note that the nodes in NodeList will copied, not moved
	//
	//		To do a ajax update use .setHref('url')
	//
	// href: String
	//		The href of the content that displays now.
	//		Set this at construction if you want to load data externally when the
	//		pane is shown.  (Set preload=true to load it immediately.)
	//		Changing href after creation doesn't have any effect; see setHref();
	href: "",

	// extractContent: Boolean
	//	Extract visible content from inside of <body> .... </body>
	extractContent: false,

	// parseOnLoad: Boolean
	//	parse content and create the widgets, if any
	parseOnLoad:	true,

	// preventCache: Boolean
	//		Cache content retreived externally
	preventCache:	false,

	// preload: Boolean
	//	Force load of data even if pane is hidden.
	preload: false,

	// refreshOnShow: Boolean
	//		Refresh (re-download) content when pane goes from hidden to shown
	refreshOnShow: false,

	// loadingMessage: String
	//	Message that shows while downloading
	loadingMessage: "<span class='dijitContentPaneLoading'>${loadingState}</span>", 

	// errorMessage: String
	//	Message that shows if an error occurs
	errorMessage: "<span class='dijitContentPaneError'>${errorState}</span>", 

	// isLoaded: Boolean
	//	Tells loading status see onLoad|onUnload for event hooks
	isLoaded: false,

	// class: String
	//	Class name to apply to ContentPane dom nodes
	"class": "dijitContentPane",

	postCreate: function(){
		// remove the title attribute so it doesn't show up when i hover
		// over a node
		this.domNode.title = "";

		if(this.preload){
			this._loadCheck();
		}

		var messages = dojo.i18n.getLocalization("dijit", "loading", this.lang);
		this.loadingMessage = dojo.string.substitute(this.loadingMessage, messages);
		this.errorMessage = dojo.string.substitute(this.errorMessage, messages);

		// for programatically created ContentPane (with <span> tag), need to muck w/CSS
		// or it's as though overflow:visible is set
		dojo.addClass(this.domNode, this["class"]);
	},

	startup: function(){
		if(this._started){ return; }
		this._checkIfSingleChild();
		if(this._singleChild){
			this._singleChild.startup();
		}
		this._loadCheck();
		this._started = true;
	},

	_checkIfSingleChild: function(){
		// summary:
		// 	Test if we have exactly one widget as a child, and if so assume that we are a container for that widget,
		//	and should propogate startup() and resize() calls to it.
		var childNodes = dojo.query(">", this.containerNode || this.domNode),
			childWidgets = childNodes.filter("[widgetId]");

		if(childNodes.length == 1 && childWidgets.length == 1){
			this.isContainer = true;
			this._singleChild = dijit.byNode(childWidgets[0]);
		}else{
			delete this.isContainer;
			delete this._singleChild;
		}
	},

	refresh: function(){
		// summary:
		//	Force a refresh (re-download) of content, be sure to turn off cache

		// we return result of _prepareLoad here to avoid code dup. in dojox.layout.ContentPane
		return this._prepareLoad(true);
	},

	setHref: function(/*String|Uri*/ href){
		// summary:
		//		Reset the (external defined) content of this pane and replace with new url
		//		Note: It delays the download until widget is shown if preload is false
		//	href:
		//		url to the page you want to get, must be within the same domain as your mainpage
		this.href = href;

		// we return result of _prepareLoad here to avoid code dup. in dojox.layout.ContentPane
		return this._prepareLoad();
	},

	setContent: function(/*String|DomNode|Nodelist*/data){
		// summary:
		//		Replaces old content with data content, include style classes from old content
		//	data:
		//		the new Content may be String, DomNode or NodeList
		//
		//		if data is a NodeList (or an array of nodes) nodes are copied
		//		so you can import nodes from another document implicitly

		// clear href so we cant run refresh and clear content
		// refresh should only work if we downloaded the content
		if(!this._isDownloaded){
			this.href = "";
			this._onUnloadHandler();
		}

		this._setContent(data || "");

		this._isDownloaded = false; // must be set after _setContent(..), pathadjust in dojox.layout.ContentPane

		if(this.parseOnLoad){
			this._createSubWidgets();
		}

		this._checkIfSingleChild();
		if(this._singleChild && this._singleChild.resize){
			this._singleChild.resize(this._contentBox);
		}

		this._onLoadHandler();
	},

	cancel: function(){
		// summary:
		//		Cancels a inflight download of content
		if(this._xhrDfd && (this._xhrDfd.fired == -1)){
			this._xhrDfd.cancel();
		}
		delete this._xhrDfd; // garbage collect
	},

	destroy: function(){
		// if we have multiple controllers destroying us, bail after the first
		if(this._beingDestroyed){
			return;
		}
		// make sure we call onUnload
		this._onUnloadHandler();
		this._beingDestroyed = true;
		this.inherited("destroy",arguments);
	},

	resize: function(size){
		dojo.marginBox(this.domNode, size);

		// Compute content box size in case we [later] need to size child
		// If either height or width wasn't specified by the user, then query node for it.
		// But note that setting the margin box and then immediately querying dimensions may return
		// inaccurate results, so try not to depend on it.
		var node = this.containerNode || this.domNode,
			mb = dojo.mixin(dojo.marginBox(node), size||{});

		this._contentBox = dijit.layout.marginBox2contentBox(node, mb);

		// If we have a single widget child then size it to fit snugly within my borders
		if(this._singleChild && this._singleChild.resize){
			this._singleChild.resize(this._contentBox);
		}
	},

	_prepareLoad: function(forceLoad){
		// sets up for a xhrLoad, load is deferred until widget onShow
		// cancels a inflight download
		this.cancel();
		this.isLoaded = false;
		this._loadCheck(forceLoad);
	},

	_loadCheck: function(forceLoad){
		// call this when you change onShow (onSelected) status when selected in parent container
		// it's used as a trigger for href download when this.domNode.display != 'none'

		// sequence:
		// if no href -> bail
		// forceLoad -> always load
		// this.preload -> load when download not in progress, domNode display doesn't matter
		// this.refreshOnShow -> load when download in progress bails, domNode display !='none' AND
		//						this.open !== false (undefined is ok), isLoaded doesn't matter
		// else -> load when download not in progress, if this.open !== false (undefined is ok) AND
		//						domNode display != 'none', isLoaded must be false

		var displayState = ((this.open !== false) && (this.domNode.style.display != 'none'));

		if(this.href &&	
			(forceLoad ||
				(this.preload && !this._xhrDfd) ||
				(this.refreshOnShow && displayState && !this._xhrDfd) ||
				(!this.isLoaded && displayState && !this._xhrDfd)
			)
		){
			this._downloadExternalContent();
		}
	},

	_downloadExternalContent: function(){
		this._onUnloadHandler();

		// display loading message
		this._setContent(
			this.onDownloadStart.call(this)
		);

		var self = this;
		var getArgs = {
			preventCache: (this.preventCache || this.refreshOnShow),
			url: this.href,
			handleAs: "text"
		};
		if(dojo.isObject(this.ioArgs)){
			dojo.mixin(getArgs, this.ioArgs);
		}

		var hand = this._xhrDfd = (this.ioMethod || dojo.xhrGet)(getArgs);

		hand.addCallback(function(html){
			try{
				self.onDownloadEnd.call(self);
				self._isDownloaded = true;
				self.setContent.call(self, html); // onload event is called from here
			}catch(err){
				self._onError.call(self, 'Content', err); // onContentError
			}
			delete self._xhrDfd;
			return html;
		});

		hand.addErrback(function(err){
			if(!hand.cancelled){
				// show error message in the pane
				self._onError.call(self, 'Download', err); // onDownloadError
			}
			delete self._xhrDfd;
			return err;
		});
	},

	_onLoadHandler: function(){
		this.isLoaded = true;
		try{
			this.onLoad.call(this);
		}catch(e){
			console.error('Error '+this.widgetId+' running custom onLoad code');
		}
	},

	_onUnloadHandler: function(){
		this.isLoaded = false;
		this.cancel();
		try{
			this.onUnload.call(this);
		}catch(e){
			console.error('Error '+this.widgetId+' running custom onUnload code');
		}
	},

	_setContent: function(cont){
		this.destroyDescendants();

		try{
			var node = this.containerNode || this.domNode;
			while(node.firstChild){
				dojo._destroyElement(node.firstChild);
			}
			if(typeof cont == "string"){
				// dijit.ContentPane does only minimal fixes,
				// No pathAdjustments, script retrieval, style clean etc
				// some of these should be available in the dojox.layout.ContentPane
				if(this.extractContent){
					match = cont.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
					if(match){ cont = match[1]; }
				}
				node.innerHTML = cont;
			}else{
				// domNode or NodeList
				if(cont.nodeType){ // domNode (htmlNode 1 or textNode 3)
					node.appendChild(cont);
				}else{// nodelist or array such as dojo.Nodelist
					dojo.forEach(cont, function(n){
						node.appendChild(n.cloneNode(true));
					});
				}
			}
		}catch(e){
			// check if a domfault occurs when we are appending this.errorMessage
			// like for instance if domNode is a UL and we try append a DIV
			var errMess = this.onContentError(e);
			try{
				node.innerHTML = errMess;
			}catch(e){
				console.error('Fatal '+this.id+' could not change content due to '+e.message, e);
			}
		}
	},

	_onError: function(type, err, consoleText){
		// shows user the string that is returned by on[type]Error
		// overide on[type]Error and return your own string to customize
		var errText = this['on' + type + 'Error'].call(this, err);
		if(consoleText){
			console.error(consoleText, err);
		}else if(errText){// a empty string won't change current content
			this._setContent.call(this, errText);
		}
	},

	_createSubWidgets: function(){
		// summary: scan my contents and create subwidgets
		var rootNode = this.containerNode || this.domNode;
		try{
			dojo.parser.parse(rootNode, true);
		}catch(e){
			this._onError('Content', e, "Couldn't create widgets in "+this.id
				+(this.href ? " from "+this.href : ""));
		}
	},

	// EVENT's, should be overide-able
	onLoad: function(e){
		// summary:
		//		Event hook, is called after everything is loaded and widgetified
	},

	onUnload: function(e){
		// summary:
		//		Event hook, is called before old content is cleared
	},

	onDownloadStart: function(){
		// summary:
		//		called before download starts
		//		the string returned by this function will be the html
		//		that tells the user we are loading something
		//		override with your own function if you want to change text
		return this.loadingMessage;
	},

	onContentError: function(/*Error*/ error){
		// summary:
		//		called on DOM faults, require fault etc in content
		//		default is to display errormessage inside pane
	},

	onDownloadError: function(/*Error*/ error){
		// summary:
		//		Called when download error occurs, default is to display
		//		errormessage inside pane. Overide function to change that.
		//		The string returned by this function will be the html
		//		that tells the user a error happend
		return this.errorMessage;
	},

	onDownloadEnd: function(){
		// summary:
		//		called when download is finished
	}
});

}

if(!dojo._hasResource["dojo.dnd.common"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.common"] = true;
dojo.provide("dojo.dnd.common");

dojo.dnd._copyKey = navigator.appVersion.indexOf("Macintosh") < 0 ? "ctrlKey" : "metaKey";

dojo.dnd.getCopyKeyState = function(e) {
	// summary: abstracts away the difference between selection on Mac and PC,
	//	and returns the state of the "copy" key to be pressed.
	// e: Event: mouse event
	return e[dojo.dnd._copyKey];	// Boolean
};

dojo.dnd._uniqueId = 0;
dojo.dnd.getUniqueId = function(){
	// summary: returns a unique string for use with any DOM element
	var id;
	do{
		id = "dojoUnique" + (++dojo.dnd._uniqueId);
	}while(dojo.byId(id));
	return id;
};

dojo.dnd._empty = {};

dojo.dnd.isFormElement = function(/*Event*/ e){
	// summary: returns true, if user clicked on a form element
	var t = e.target;
	if(t.nodeType == 3 /*TEXT_NODE*/){
		t = t.parentNode;
	}
	return " button textarea input select option ".indexOf(" " + t.tagName.toLowerCase() + " ") >= 0;	// Boolean
};

}

if(!dojo._hasResource["dojo.dnd.Container"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Container"] = true;
dojo.provide("dojo.dnd.Container");




/*
	Container states:
		""		- normal state
		"Over"	- mouse over a container
	Container item states:
		""		- normal state
		"Over"	- mouse over a container item
*/

dojo.declare("dojo.dnd.Container", null, {
	// summary: a Container object, which knows when mouse hovers over it, 
	//	and know over which element it hovers
	
	// object attributes (for markup)
	skipForm: false,
	
	constructor: function(node, params){
		// summary: a constructor of the Container
		// node: Node: node or node's id to build the container on
		// params: Object: a dict of parameters, recognized parameters are:
		//	creator: Function: a creator function, which takes a data item, and returns an object like that:
		//		{node: newNode, data: usedData, type: arrayOfStrings}
		//	skipForm: Boolean: don't start the drag operation, if clicked on form elements
		//	_skipStartup: Boolean: skip startup(), which collects children, for deferred initialization
		//		(this is used in the markup mode)
		this.node = dojo.byId(node);
		if(!params){ params = {}; }
		this.creator = params.creator || null;
		this.skipForm = params.skipForm;
		this.defaultCreator = dojo.dnd._defaultCreator(this.node);

		// class-specific variables
		this.map = {};
		this.current = null;

		// states
		this.containerState = "";
		dojo.addClass(this.node, "dojoDndContainer");
		
		// mark up children
		if(!(params && params._skipStartup)){
			this.startup();
		}

		// set up events
		this.events = [
			dojo.connect(this.node, "onmouseover", this, "onMouseOver"),
			dojo.connect(this.node, "onmouseout",  this, "onMouseOut"),
			// cancel text selection and text dragging
			dojo.connect(this.node, "ondragstart",   this, "onSelectStart"),
			dojo.connect(this.node, "onselectstart", this, "onSelectStart")
		];
	},
	
	// object attributes (for markup)
	creator: function(){},	// creator function, dummy at the moment
	
	// abstract access to the map
	getItem: function(/*String*/ key){
		// summary: returns a data item by its key (id)
		return this.map[key];	// Object
	},
	setItem: function(/*String*/ key, /*Object*/ data){
		// summary: associates a data item with its key (id)
		this.map[key] = data;
	},
	delItem: function(/*String*/ key){
		// summary: removes a data item from the map by its key (id)
		delete this.map[key];
	},
	forInItems: function(/*Function*/ f, /*Object?*/ o){
		// summary: iterates over a data map skipping members, which 
		//	are present in the empty object (IE and/or 3rd-party libraries).
		o = o || dojo.global;
		var m = this.map, e = dojo.dnd._empty;
		for(var i in this.map){
			if(i in e){ continue; }
			f.call(o, m[i], i, m);
		}
	},
	clearItems: function(){
		// summary: removes all data items from the map
		this.map = {};
	},
	
	// methods
	getAllNodes: function(){
		// summary: returns a list (an array) of all valid child nodes
		return dojo.query("> .dojoDndItem", this.parent);	// NodeList
	},
	insertNodes: function(data, before, anchor){
		// summary: inserts an array of new nodes before/after an anchor node
		// data: Array: a list of data items, which should be processed by the creator function
		// before: Boolean: insert before the anchor, if true, and after the anchor otherwise
		// anchor: Node: the anchor node to be used as a point of insertion
		if(!this.parent.firstChild){
			anchor = null;
		}else if(before){
			if(!anchor){
				anchor = this.parent.firstChild;
			}
		}else{
			if(anchor){
				anchor = anchor.nextSibling;
			}
		}
		if(anchor){
			for(var i = 0; i < data.length; ++i){
				var t = this._normalizedCreator(data[i]);
				this.setItem(t.node.id, {data: t.data, type: t.type});
				this.parent.insertBefore(t.node, anchor);
			}
		}else{
			for(var i = 0; i < data.length; ++i){
				var t = this._normalizedCreator(data[i]);
				this.setItem(t.node.id, {data: t.data, type: t.type});
				this.parent.appendChild(t.node);
			}
		}
		return this;	// self
	},
	destroy: function(){
		// summary: prepares the object to be garbage-collected
		dojo.forEach(this.events, dojo.disconnect);
		this.clearItems();
		this.node = this.parent = this.current;
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new dojo.dnd.Container(node, params);
	},
	startup: function(){
		// summary: collects valid child items and populate the map
		
		// set up the real parent node
		this.parent = this.node;
		if(this.parent.tagName.toLowerCase() == "table"){
			var c = this.parent.getElementsByTagName("tbody");
			if(c && c.length){ this.parent = c[0]; }
		}

		// process specially marked children
		dojo.query("> .dojoDndItem", this.parent).forEach(function(node){
			if(!node.id){ node.id = dojo.dnd.getUniqueId(); }
			var type = node.getAttribute("dndType"),
				data = node.getAttribute("dndData");
			this.setItem(node.id, {
				data: data ? data : node.innerHTML,
				type: type ? type.split(/\s*,\s*/) : ["text"]
			});
		}, this);
	},

	// mouse events
	onMouseOver: function(e){
		// summary: event processor for onmouseover
		// e: Event: mouse event
		var n = e.relatedTarget;
		while(n){
			if(n == this.node){ break; }
			try{
				n = n.parentNode;
			}catch(x){
				n = null;
			}
		}
		if(!n){
			this._changeState("Container", "Over");
			this.onOverEvent();
		}
		n = this._getChildByEvent(e);
		if(this.current == n){ return; }
		if(this.current){ this._removeItemClass(this.current, "Over"); }
		if(n){ this._addItemClass(n, "Over"); }
		this.current = n;
	},
	onMouseOut: function(e){
		// summary: event processor for onmouseout
		// e: Event: mouse event
		for(var n = e.relatedTarget; n;){
			if(n == this.node){ return; }
			try{
				n = n.parentNode;
			}catch(x){
				n = null;
			}
		}
		if(this.current){
			this._removeItemClass(this.current, "Over");
			this.current = null;
		}
		this._changeState("Container", "");
		this.onOutEvent();
	},
	onSelectStart: function(e){
		// summary: event processor for onselectevent and ondragevent
		// e: Event: mouse event
		if(!this.skipForm || !dojo.dnd.isFormElement(e)){
			dojo.stopEvent(e);
		}
	},
	
	// utilities
	onOverEvent: function(){
		// summary: this function is called once, when mouse is over our container
	},
	onOutEvent: function(){
		// summary: this function is called once, when mouse is out of our container
	},
	_changeState: function(type, newState){
		// summary: changes a named state to new state value
		// type: String: a name of the state to change
		// newState: String: new state
		var prefix = "dojoDnd" + type;
		var state  = type.toLowerCase() + "State";
		//dojo.replaceClass(this.node, prefix + newState, prefix + this[state]);
		dojo.removeClass(this.node, prefix + this[state]);
		dojo.addClass(this.node, prefix + newState);
		this[state] = newState;
	},
	_addItemClass: function(node, type){
		// summary: adds a class with prefix "dojoDndItem"
		// node: Node: a node
		// type: String: a variable suffix for a class name
		dojo.addClass(node, "dojoDndItem" + type);
	},
	_removeItemClass: function(node, type){
		// summary: removes a class with prefix "dojoDndItem"
		// node: Node: a node
		// type: String: a variable suffix for a class name
		dojo.removeClass(node, "dojoDndItem" + type);
	},
	_getChildByEvent: function(e){
		// summary: gets a child, which is under the mouse at the moment, or null
		// e: Event: a mouse event
		var node = e.target;
		if(node){
			for(var parent = node.parentNode; parent; node = parent, parent = node.parentNode){
				if(parent == this.parent && dojo.hasClass(node, "dojoDndItem")){ return node; }
			}
		}
		return null;
	},
	_normalizedCreator: function(item, hint){
		// summary: adds all necessary data to the output of the user-supplied creator function
		var t = (this.creator ? this.creator : this.defaultCreator)(item, hint);
		if(!dojo.isArray(t.type)){ t.type = ["text"]; }
		if(!t.node.id){ t.node.id = dojo.dnd.getUniqueId(); }
		dojo.addClass(t.node, "dojoDndItem");
		return t;
	}
});

dojo.dnd._createNode = function(tag){
	// summary: returns a function, which creates an element of given tag 
	//	(SPAN by default) and sets its innerHTML to given text
	// tag: String: a tag name or empty for SPAN
	if(!tag){ return dojo.dnd._createSpan; }
	return function(text){	// Function
		var n = dojo.doc.createElement(tag);
		n.innerHTML = text;
		return n;
	};
};

dojo.dnd._createTrTd = function(text){
	// summary: creates a TR/TD structure with given text as an innerHTML of TD
	// text: String: a text for TD
	var tr = dojo.doc.createElement("tr");
	var td = dojo.doc.createElement("td");
	td.innerHTML = text;
	tr.appendChild(td);
	return tr;	// Node
};

dojo.dnd._createSpan = function(text){
	// summary: creates a SPAN element with given text as its innerHTML
	// text: String: a text for SPAN
	var n = dojo.doc.createElement("span");
	n.innerHTML = text;
	return n;	// Node
};

// dojo.dnd._defaultCreatorNodes: Object: a dicitionary, which maps container tag names to child tag names
dojo.dnd._defaultCreatorNodes = {ul: "li", ol: "li", div: "div", p: "div"};

dojo.dnd._defaultCreator = function(node){
	// summary: takes a container node, and returns an appropriate creator function
	// node: Node: a container node
	var tag = node.tagName.toLowerCase();
	var c = tag == "table" ? dojo.dnd._createTrTd : dojo.dnd._createNode(dojo.dnd._defaultCreatorNodes[tag]);
	return function(item, hint){	// Function
		var isObj = dojo.isObject(item) && item;
		var data = (isObj && item.data) ? item.data : item;
		var type = (isObj && item.type) ? item.type : ["text"];
		var t = String(data), n = (hint == "avatar" ? dojo.dnd._createSpan : c)(t);
		n.id = dojo.dnd.getUniqueId();
		return {node: n, data: data, type: type};
	};
};

}

if(!dojo._hasResource["dojo.dnd.Selector"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Selector"] = true;
dojo.provide("dojo.dnd.Selector");




/*
	Container item states:
		""			- an item is not selected
		"Selected"	- an item is selected
		"Anchor"	- an item is selected, and is an anchor for a "shift" selection
*/

dojo.declare("dojo.dnd.Selector", dojo.dnd.Container, {
	// summary: a Selector object, which knows how to select its children
	
	constructor: function(node, params){
		// summary: a constructor of the Selector
		// node: Node: node or node's id to build the selector on
		// params: Object: a dict of parameters, recognized parameters are:
		//	singular: Boolean: allows selection of only one element, if true
		//	the rest of parameters are passed to the container
		if(!params){ params = {}; }
		this.singular = params.singular;
		// class-specific variables
		this.selection = {};
		this.anchor = null;
		this.simpleSelection = false;
		// set up events
		this.events.push(
			dojo.connect(this.node, "onmousedown", this, "onMouseDown"),
			dojo.connect(this.node, "onmouseup",   this, "onMouseUp"));
	},
	
	// object attributes (for markup)
	singular: false,	// is singular property
	
	// methods
	getSelectedNodes: function(){
		// summary: returns a list (an array) of selected nodes
		var t = new dojo.NodeList();
		var e = dojo.dnd._empty;
		for(var i in this.selection){
			if(i in e){ continue; }
			t.push(dojo.byId(i));
		}
		return t;	// Array
	},
	selectNone: function(){
		// summary: unselects all items
		return this._removeSelection()._removeAnchor();	// self
	},
	selectAll: function(){
		// summary: selects all items
		this.forInItems(function(data, id){
			this._addItemClass(dojo.byId(id), "Selected");
			this.selection[id] = 1;
		}, this);
		return this._removeAnchor();	// self
	},
	deleteSelectedNodes: function(){
		// summary: deletes all selected items
		var e = dojo.dnd._empty;
		for(var i in this.selection){
			if(i in e){ continue; }
			var n = dojo.byId(i);
			this.delItem(i);
			dojo._destroyElement(n);
		}
		this.anchor = null;
		this.selection = {};
		return this;	// self
	},
	insertNodes: function(addSelected, data, before, anchor){
		// summary: inserts new data items (see Container's insertNodes method for details)
		// addSelected: Boolean: all new nodes will be added to selected items, if true, no selection change otherwise
		// data: Array: a list of data items, which should be processed by the creator function
		// before: Boolean: insert before the anchor, if true, and after the anchor otherwise
		// anchor: Node: the anchor node to be used as a point of insertion
		var oldCreator = this._normalizedCreator;
		this._normalizedCreator = function(item, hint){
			var t = oldCreator.call(this, item, hint);
			if(addSelected){
				if(!this.anchor){
					this.anchor = t.node;
					this._removeItemClass(t.node, "Selected");
					this._addItemClass(this.anchor, "Anchor");
				}else if(this.anchor != t.node){
					this._removeItemClass(t.node, "Anchor");
					this._addItemClass(t.node, "Selected");
				}
				this.selection[t.node.id] = 1;
			}else{
				this._removeItemClass(t.node, "Selected");
				this._removeItemClass(t.node, "Anchor");
			}
			return t;
		};
		dojo.dnd.Selector.superclass.insertNodes.call(this, data, before, anchor);
		this._normalizedCreator = oldCreator;
		return this;	// self
	},
	destroy: function(){
		// summary: prepares the object to be garbage-collected
		dojo.dnd.Selector.superclass.destroy.call(this);
		this.selection = this.anchor = null;
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new dojo.dnd.Selector(node, params);
	},

	// mouse events
	onMouseDown: function(e){
		// summary: event processor for onmousedown
		// e: Event: mouse event
		if(!this.current){ return; }
		if(!this.singular && !dojo.dnd.getCopyKeyState(e) && !e.shiftKey && (this.current.id in this.selection)){
			this.simpleSelection = true;
			dojo.stopEvent(e);
			return;
		}
		if(!this.singular && e.shiftKey){
			if(!dojo.dnd.getCopyKeyState(e)){
				this._removeSelection();
			}
			var c = dojo.query("> .dojoDndItem", this.parent);
			if(c.length){
				if(!this.anchor){
					this.anchor = c[0];
					this._addItemClass(this.anchor, "Anchor");
				}
				this.selection[this.anchor.id] = 1;
				if(this.anchor != this.current){
					var i = 0;
					for(; i < c.length; ++i){
						var node = c[i];
						if(node == this.anchor || node == this.current){ break; }
					}
					for(++i; i < c.length; ++i){
						var node = c[i];
						if(node == this.anchor || node == this.current){ break; }
						this._addItemClass(node, "Selected");
						this.selection[node.id] = 1;
					}
					this._addItemClass(this.current, "Selected");
					this.selection[this.current.id] = 1;
				}
			}
		}else{
			if(this.singular){
				if(this.anchor == this.current){
					if(dojo.dnd.getCopyKeyState(e)){
						this.selectNone();
					}
				}else{
					this.selectNone();
					this.anchor = this.current;
					this._addItemClass(this.anchor, "Anchor");
					this.selection[this.current.id] = 1;
				}
			}else{
				if(dojo.dnd.getCopyKeyState(e)){
					if(this.anchor == this.current){
						delete this.selection[this.anchor.id];
						this._removeAnchor();
					}else{
						if(this.current.id in this.selection){
							this._removeItemClass(this.current, "Selected");
							delete this.selection[this.current.id];
						}else{
							if(this.anchor){
								this._removeItemClass(this.anchor, "Anchor");
								this._addItemClass(this.anchor, "Selected");
							}
							this.anchor = this.current;
							this._addItemClass(this.current, "Anchor");
							this.selection[this.current.id] = 1;
						}
					}
				}else{
					if(!(this.current.id in this.selection)){
						this.selectNone();
						this.anchor = this.current;
						this._addItemClass(this.current, "Anchor");
						this.selection[this.current.id] = 1;
					}
				}
			}
		}
		dojo.stopEvent(e);
	},
	onMouseUp: function(e){
		// summary: event processor for onmouseup
		// e: Event: mouse event
		if(!this.simpleSelection){ return; }
		this.simpleSelection = false;
		this.selectNone();
		if(this.current){
			this.anchor = this.current;
			this._addItemClass(this.anchor, "Anchor");
			this.selection[this.current.id] = 1;
		}
	},
	onMouseMove: function(e){
		// summary: event processor for onmousemove
		// e: Event: mouse event
		this.simpleSelection = false;
	},
	
	// utilities
	onOverEvent: function(){
		// summary: this function is called once, when mouse is over our container
		this.onmousemoveEvent = dojo.connect(this.node, "onmousemove", this, "onMouseMove");
	},
	onOutEvent: function(){
		// summary: this function is called once, when mouse is out of our container
		dojo.disconnect(this.onmousemoveEvent);
		delete this.onmousemoveEvent;
	},
	_removeSelection: function(){
		// summary: unselects all items
		var e = dojo.dnd._empty;
		for(var i in this.selection){
			if(i in e){ continue; }
			var node = dojo.byId(i);
			if(node){ this._removeItemClass(node, "Selected"); }
		}
		this.selection = {};
		return this;	// self
	},
	_removeAnchor: function(){
		if(this.anchor){
			this._removeItemClass(this.anchor, "Anchor");
			this.anchor = null;
		}
		return this;	// self
	}
});

}

if(!dojo._hasResource["dojo.dnd.autoscroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.autoscroll"] = true;
dojo.provide("dojo.dnd.autoscroll");

dojo.dnd.getViewport = function(){
	// summary: returns a viewport size (visible part of the window)

	// FIXME: need more docs!!
	var d = dojo.doc, dd = d.documentElement, w = window, b = dojo.body();
	if(dojo.isMozilla){
		return {w: dd.clientWidth, h: w.innerHeight};	// Object
	}else if(!dojo.isOpera && w.innerWidth){
		return {w: w.innerWidth, h: w.innerHeight};		// Object
	}else if (!dojo.isOpera && dd && dd.clientWidth){
		return {w: dd.clientWidth, h: dd.clientHeight};	// Object
	}else if (b.clientWidth){
		return {w: b.clientWidth, h: b.clientHeight};	// Object
	}
	return null;	// Object
};

dojo.dnd.V_TRIGGER_AUTOSCROLL = 32;
dojo.dnd.H_TRIGGER_AUTOSCROLL = 32;

dojo.dnd.V_AUTOSCROLL_VALUE = 16;
dojo.dnd.H_AUTOSCROLL_VALUE = 16;

dojo.dnd.autoScroll = function(e){
	// summary:
	//		a handler for onmousemove event, which scrolls the window, if
	//		necesary
	// e: Event:
	//		onmousemove event

	// FIXME: needs more docs!
	var v = dojo.dnd.getViewport(), dx = 0, dy = 0;
	if(e.clientX < dojo.dnd.H_TRIGGER_AUTOSCROLL){
		dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
	}else if(e.clientX > v.w - dojo.dnd.H_TRIGGER_AUTOSCROLL){
		dx = dojo.dnd.H_AUTOSCROLL_VALUE;
	}
	if(e.clientY < dojo.dnd.V_TRIGGER_AUTOSCROLL){
		dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
	}else if(e.clientY > v.h - dojo.dnd.V_TRIGGER_AUTOSCROLL){
		dy = dojo.dnd.V_AUTOSCROLL_VALUE;
	}
	window.scrollBy(dx, dy);
};

dojo.dnd._validNodes = {"div": 1, "p": 1, "td": 1};
dojo.dnd._validOverflow = {"auto": 1, "scroll": 1};

dojo.dnd.autoScrollNodes = function(e){
	// summary:
	//		a handler for onmousemove event, which scrolls the first avaialble
	//		Dom element, it falls back to dojo.dnd.autoScroll()
	// e: Event:
	//		onmousemove event

	// FIXME: needs more docs!
	for(var n = e.target; n;){
		if(n.nodeType == 1 && (n.tagName.toLowerCase() in dojo.dnd._validNodes)){
			var s = dojo.getComputedStyle(n);
			if(s.overflow.toLowerCase() in dojo.dnd._validOverflow){
				var b = dojo._getContentBox(n, s), t = dojo._abs(n, true);
				// console.debug(b.l, b.t, t.x, t.y, n.scrollLeft, n.scrollTop);
				b.l += t.x + n.scrollLeft;
				b.t += t.y + n.scrollTop;
				var w = Math.min(dojo.dnd.H_TRIGGER_AUTOSCROLL, b.w / 2), 
					h = Math.min(dojo.dnd.V_TRIGGER_AUTOSCROLL, b.h / 2),
					rx = e.pageX - b.l, ry = e.pageY - b.t, dx = 0, dy = 0;
				if(rx > 0 && rx < b.w){
					if(rx < w){
						dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
					}else if(rx > b.w - w){
						dx = dojo.dnd.H_AUTOSCROLL_VALUE;
					}
				}
				//console.debug("ry =", ry, "b.h =", b.h, "h =", h);
				if(ry > 0 && ry < b.h){
					if(ry < h){
						dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
					}else if(ry > b.h - h){
						dy = dojo.dnd.V_AUTOSCROLL_VALUE;
					}
				}
				var oldLeft = n.scrollLeft, oldTop = n.scrollTop;
				n.scrollLeft = n.scrollLeft + dx;
				n.scrollTop  = n.scrollTop  + dy;
				// if(dx || dy){ console.debug(oldLeft + ", " + oldTop + "\n" + dx + ", " + dy + "\n" + n.scrollLeft + ", " + n.scrollTop); }
				if(oldLeft != n.scrollLeft || oldTop != n.scrollTop){ return; }
			}
		}
		try{
			n = n.parentNode;
		}catch(x){
			n = null;
		}
	}
	dojo.dnd.autoScroll(e);
};

}

if(!dojo._hasResource["dojo.dnd.Avatar"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Avatar"] = true;
dojo.provide("dojo.dnd.Avatar");



dojo.dnd.Avatar = function(manager){
	// summary: an object, which represents transferred DnD items visually
	// manager: Object: a DnD manager object
	this.manager = manager;
	this.construct();
};

dojo.extend(dojo.dnd.Avatar, {
	construct: function(){
		// summary: a constructor function;
		//	it is separate so it can be (dynamically) overwritten in case of need
		var a = dojo.doc.createElement("table");
		a.className = "dojoDndAvatar";
		a.style.position = "absolute";
		a.style.zIndex = 1999;
		a.style.margin = "0px"; // to avoid dojo.marginBox() problems with table's margins
		var b = dojo.doc.createElement("tbody");
		var tr = dojo.doc.createElement("tr");
		tr.className = "dojoDndAvatarHeader";
		var td = dojo.doc.createElement("td");
		td.innerHTML = this._generateText();
		tr.appendChild(td);
		dojo.style(tr, "opacity", 0.9);
		b.appendChild(tr);
		var k = Math.min(5, this.manager.nodes.length);
		var source = this.manager.source;
		for(var i = 0; i < k; ++i){
			tr = dojo.doc.createElement("tr");
			tr.className = "dojoDndAvatarItem";
			td = dojo.doc.createElement("td");
			var node = source.creator ?
				// create an avatar representation of the node
				node = source._normalizedCreator(source.getItem(this.manager.nodes[i].id).data, "avatar").node :
				// or just clone the node and hope it works
				node = this.manager.nodes[i].cloneNode(true);
			node.id = "";
			td.appendChild(node);
			tr.appendChild(td);
			dojo.style(tr, "opacity", (9 - i) / 10);
			b.appendChild(tr);
		}
		a.appendChild(b);
		this.node = a;
	},
	destroy: function(){
		// summary: a desctructor for the avatar, called to remove all references so it can be garbage-collected
		dojo._destroyElement(this.node);
		this.node = false;
	},
	update: function(){
		// summary: updates the avatar to reflect the current DnD state
		dojo[(this.manager.canDropFlag ? "add" : "remove") + "Class"](this.node, "dojoDndAvatarCanDrop");
		// replace text
		var t = this.node.getElementsByTagName("td");
		for(var i = 0; i < t.length; ++i){
			var n = t[i];
			if(dojo.hasClass(n.parentNode, "dojoDndAvatarHeader")){
				n.innerHTML = this._generateText();
				break;
			}
		}
	},
	_generateText: function(){
		// summary: generates a proper text to reflect copying or moving of items
		return this.manager.nodes.length.toString();
	}
});

}

if(!dojo._hasResource["dojo.dnd.Manager"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Manager"] = true;
dojo.provide("dojo.dnd.Manager");





dojo.dnd.Manager = function(){
	// summary: the manager of DnD operations (usually a singleton)
	this.avatar  = null;
	this.source = null;
	this.nodes = [];
	this.copy  = true;
	this.target = null;
	this.canDropFlag = false;
	this.events = [];
};

dojo.extend(dojo.dnd.Manager, {
	// avatar's offset from the mouse
	OFFSET_X: 16,
	OFFSET_Y: 16,
	// methods
	overSource: function(source){
		// summary: called when a source detected a mouse-over conditiion
		// source: Object: the reporter
		if(this.avatar){
			this.target = (source && source.targetState != "Disabled") ? source : null;
			this.avatar.update();
		}
		dojo.publish("/dnd/source/over", [source]);
	},
	outSource: function(source){
		// summary: called when a source detected a mouse-out conditiion
		// source: Object: the reporter
		if(this.avatar){
			if(this.target == source){
				this.target = null;
				this.canDropFlag = false;
				this.avatar.update();
				dojo.publish("/dnd/source/over", [null]);
			}
		}else{
			dojo.publish("/dnd/source/over", [null]);
		}
	},
	startDrag: function(source, nodes, copy){
		// summary: called to initiate the DnD operation
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		// copy: Boolean: copy items, if true, move items otherwise
		this.source = source;
		this.nodes  = nodes;
		this.copy   = Boolean(copy); // normalizing to true boolean
		this.avatar = this.makeAvatar();
		dojo.body().appendChild(this.avatar.node);
		dojo.publish("/dnd/start", [source, nodes, this.copy]);
		this.events = [
			dojo.connect(dojo.doc, "onmousemove", this, "onMouseMove"),
			dojo.connect(dojo.doc, "onmouseup",   this, "onMouseUp"),
			dojo.connect(dojo.doc, "onkeydown",   this, "onKeyDown"),
			dojo.connect(dojo.doc, "onkeyup",     this, "onKeyUp")
		];
		var c = "dojoDnd" + (copy ? "Copy" : "Move");
		dojo.addClass(dojo.body(), c); 
	},
	canDrop: function(flag){
		// summary: called to notify if the current target can accept items
		var canDropFlag = this.target && flag;
		if(this.canDropFlag != canDropFlag){
			this.canDropFlag = canDropFlag;
			this.avatar.update();
		}
	},
	stopDrag: function(){
		// summary: stop the DnD in progress
		dojo.removeClass(dojo.body(), "dojoDndCopy");
		dojo.removeClass(dojo.body(), "dojoDndMove");
		dojo.forEach(this.events, dojo.disconnect);
		this.events = [];
		this.avatar.destroy();
		this.avatar = null;
		this.source = null;
		this.nodes = [];
	},
	makeAvatar: function(){
		// summary: makes the avatar, it is separate to be overwritten dynamically, if needed
		return new dojo.dnd.Avatar(this);
	},
	updateAvatar: function(){
		// summary: updates the avatar, it is separate to be overwritten dynamically, if needed
		this.avatar.update();
	},
	// mouse event processors
	onMouseMove: function(e){
		// summary: event processor for onmousemove
		// e: Event: mouse event
		var a = this.avatar;
		if(a){
			//dojo.dnd.autoScrollNodes(e);
			dojo.dnd.autoScroll(e);
			dojo.marginBox(a.node, {l: e.pageX + this.OFFSET_X, t: e.pageY + this.OFFSET_Y});
			var copy = Boolean(this.source.copyState(dojo.dnd.getCopyKeyState(e)));
			if(this.copy != copy){ 
				this._setCopyStatus(copy);
			}
		}
	},
	onMouseUp: function(e){
		// summary: event processor for onmouseup
		// e: Event: mouse event
		if(this.avatar && this.source.mouseButton == e.button){
			if(this.target && this.canDropFlag){
				dojo.publish("/dnd/drop", [this.source, this.nodes, Boolean(this.source.copyState(dojo.dnd.getCopyKeyState(e)))]);
			}else{
				dojo.publish("/dnd/cancel");
			}
			this.stopDrag();
		}
	},
	// keyboard event processors
	onKeyDown: function(e){
		// summary: event processor for onkeydown:
		//	watching for CTRL for copy/move status, watching for ESCAPE to cancel the drag
		// e: Event: keyboard event
		if(this.avatar){
			switch(e.keyCode){
				case dojo.keys.CTRL:
					var copy = Boolean(this.source.copyState(true));
					if(this.copy != copy){ 
						this._setCopyStatus(copy);
					}
					break;
				case dojo.keys.ESCAPE:
					dojo.publish("/dnd/cancel");
					this.stopDrag();
					break;
			}
		}
	},
	onKeyUp: function(e){
		// summary: event processor for onkeyup, watching for CTRL for copy/move status
		// e: Event: keyboard event
		if(this.avatar && e.keyCode == dojo.keys.CTRL){
			var copy = Boolean(this.source.copyState(false));
			if(this.copy != copy){ 
				this._setCopyStatus(copy);
			}
		}
	},
	// utilities
	_setCopyStatus: function(copy){
		// summary: changes the copy status
		// copy: Boolean: the copy status
		this.copy = copy;
		this.source._markDndStatus(this.copy);
		this.updateAvatar();
		dojo.removeClass(dojo.body(), "dojoDnd" + (this.copy ? "Move" : "Copy"));
		dojo.addClass(dojo.body(), "dojoDnd" + (this.copy ? "Copy" : "Move"));
	}
});

// summary: the manager singleton variable, can be overwritten, if needed
dojo.dnd._manager = null;

dojo.dnd.manager = function(){
	// summary: returns the current DnD manager, creates one if it is not created yet
	if(!dojo.dnd._manager){
		dojo.dnd._manager = new dojo.dnd.Manager();
	}
	return dojo.dnd._manager;	// Object
};

}

if(!dojo._hasResource["dojo.dnd.Source"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Source"] = true;
dojo.provide("dojo.dnd.Source");




/*
	Container property:
		"Horizontal"- if this is the horizontal container
	Source states:
		""			- normal state
		"Moved"		- this source is being moved
		"Copied"	- this source is being copied
	Target states:
		""			- normal state
		"Disabled"	- the target cannot accept an avatar
	Target anchor state:
		""			- item is not selected
		"Before"	- insert point is before the anchor
		"After"		- insert point is after the anchor
*/

dojo.declare("dojo.dnd.Source", dojo.dnd.Selector, {
	// summary: a Source object, which can be used as a DnD source, or a DnD target
	
	// object attributes (for markup)
	isSource: true,
	horizontal: false,
	copyOnly: false,
	skipForm: false,
	withHandles: false,
	accept: ["text"],
	
	constructor: function(node, params){
		// summary: a constructor of the Source
		// node: Node: node or node's id to build the source on
		// params: Object: a dict of parameters, recognized parameters are:
		//	isSource: Boolean: can be used as a DnD source, if true; assumed to be "true" if omitted
		//	accept: Array: list of accepted types (text strings) for a target; assumed to be ["text"] if omitted
		//	horizontal: Boolean: a horizontal container, if true, vertical otherwise or when omitted
		//	copyOnly: Boolean: always copy items, if true, use a state of Ctrl key otherwise
		//	withHandles: Boolean: allows dragging only by handles
		//	the rest of parameters are passed to the selector
		if(!params){ params = {}; }
		this.isSource = typeof params.isSource == "undefined" ? true : params.isSource;
		var type = params.accept instanceof Array ? params.accept : ["text"];
		this.accept = null;
		if(type.length){
			this.accept = {};
			for(var i = 0; i < type.length; ++i){
				this.accept[type[i]] = 1;
			}
		}
		this.horizontal = params.horizontal;
		this.copyOnly = params.copyOnly;
		this.withHandles = params.withHandles;
		// class-specific variables
		this.isDragging = false;
		this.mouseDown = false;
		this.targetAnchor = null;
		this.targetBox = null;
		this.before = true;
		// states
		this.sourceState  = "";
		if(this.isSource){
			dojo.addClass(this.node, "dojoDndSource");
		}
		this.targetState  = "";
		if(this.accept){
			dojo.addClass(this.node, "dojoDndTarget");
		}
		if(this.horizontal){
			dojo.addClass(this.node, "dojoDndHorizontal");
		}
		// set up events
		this.topics = [
			dojo.subscribe("/dnd/source/over", this, "onDndSourceOver"),
			dojo.subscribe("/dnd/start",  this, "onDndStart"),
			dojo.subscribe("/dnd/drop",   this, "onDndDrop"),
			dojo.subscribe("/dnd/cancel", this, "onDndCancel")
		];
	},
	
	// methods
	checkAcceptance: function(source, nodes){
		// summary: checks, if the target can accept nodes from this source
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		if(this == source){ return true; }
		for(var i = 0; i < nodes.length; ++i){
			var type = source.getItem(nodes[i].id).type;
			// type instanceof Array
			var flag = false;
			for(var j = 0; j < type.length; ++j){
				if(type[j] in this.accept){
					flag = true;
					break;
				}
			}
			if(!flag){
				return false;	// Boolean
			}
		}
		return true;	// Boolean
	},
	copyState: function(keyPressed){
		// summary: Returns true, if we need to copy items, false to move.
		//		It is separated to be overwritten dynamically, if needed.
		// keyPressed: Boolean: the "copy" was pressed
		return this.copyOnly || keyPressed;	// Boolean
	},
	destroy: function(){
		// summary: prepares the object to be garbage-collected
		dojo.dnd.Source.superclass.destroy.call(this);
		dojo.forEach(this.topics, dojo.unsubscribe);
		this.targetAnchor = null;
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new dojo.dnd.Source(node, params);
	},

	// mouse event processors
	onMouseMove: function(e){
		// summary: event processor for onmousemove
		// e: Event: mouse event
		if(this.isDragging && this.targetState == "Disabled"){ return; }
		dojo.dnd.Source.superclass.onMouseMove.call(this, e);
		var m = dojo.dnd.manager();
		if(this.isDragging){
			// calculate before/after
			var before = false;
			if(this.current){
				if(!this.targetBox || this.targetAnchor != this.current){
					this.targetBox = {
						xy: dojo.coords(this.current, true),
						w: this.current.offsetWidth,
						h: this.current.offsetHeight
					};
				}
				if(this.horizontal){
					before = (e.pageX - this.targetBox.xy.x) < (this.targetBox.w / 2);
				}else{
					before = (e.pageY - this.targetBox.xy.y) < (this.targetBox.h / 2);
				}
			}
			if(this.current != this.targetAnchor || before != this.before){
				this._markTargetAnchor(before);
				m.canDrop(!this.current || m.source != this || !(this.current.id in this.selection));
			}
		}else{
			if(this.mouseDown && this.isSource){
				var nodes = this.getSelectedNodes();
				if(nodes.length){
					m.startDrag(this, nodes, this.copyState(dojo.dnd.getCopyKeyState(e)));
				}
			}
		}
	},
	onMouseDown: function(e){
		// summary: event processor for onmousedown
		// e: Event: mouse event
		if(this._legalMouseDown(e) && (!this.skipForm || !dojo.dnd.isFormElement(e))){
			this.mouseDown = true;
			this.mouseButton = e.button;
			dojo.dnd.Source.superclass.onMouseDown.call(this, e);
		}
	},
	onMouseUp: function(e){
		// summary: event processor for onmouseup
		// e: Event: mouse event
		if(this.mouseDown){
			this.mouseDown = false;
			dojo.dnd.Source.superclass.onMouseUp.call(this, e);
		}
	},
	
	// topic event processors
	onDndSourceOver: function(source){
		// summary: topic event processor for /dnd/source/over, called when detected a current source
		// source: Object: the source which has the mouse over it
		if(this != source){
			this.mouseDown = false;
			if(this.targetAnchor){
				this._unmarkTargetAnchor();
			}
		}else if(this.isDragging){
			var m = dojo.dnd.manager();
			m.canDrop(this.targetState != "Disabled" && (!this.current || m.source != this || !(this.current.id in this.selection)));
		}
	},
	onDndStart: function(source, nodes, copy){
		// summary: topic event processor for /dnd/start, called to initiate the DnD operation
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		// copy: Boolean: copy items, if true, move items otherwise
		if(this.isSource){
			this._changeState("Source", this == source ? (copy ? "Copied" : "Moved") : "");
		}
		var accepted = this.accept && this.checkAcceptance(source, nodes);
		this._changeState("Target", accepted ? "" : "Disabled");
		if(accepted){
			dojo.dnd.manager().overSource(this);
		}
		this.isDragging = true;
	},
	onDndDrop: function(source, nodes, copy){
		// summary: topic event processor for /dnd/drop, called to finish the DnD operation
		// source: Object: the source which provides items
		// nodes: Array: the list of transferred items
		// copy: Boolean: copy items, if true, move items otherwise
		do{ //break box
			if(this.containerState != "Over"){ break; }
			var oldCreator = this._normalizedCreator;
			if(this != source){
				// transferring nodes from the source to the target
				if(this.creator){
					// use defined creator
					this._normalizedCreator = function(node, hint){
						return oldCreator.call(this, source.getItem(node.id).data, hint);
					};
				}else{
					// we have no creator defined => move/clone nodes
					if(copy){
						// clone nodes
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							var n = node.cloneNode(true);
							n.id = dojo.dnd.getUniqueId();
							return {node: n, data: t.data, type: t.type};
						};
					}else{
						// move nodes
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							source.delItem(node.id);
							return {node: node, data: t.data, type: t.type};
						};
					}
				}
			}else{
				// transferring nodes within the single source
				if(this.current && this.current.id in this.selection){ break; }
				if(this.creator){
					// use defined creator
					if(copy){
						// create new copies of data items
						this._normalizedCreator = function(node, hint){
							return oldCreator.call(this, source.getItem(node.id).data, hint);
						};
					}else{
						// move nodes
						if(!this.current){ break; }
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							return {node: node, data: t.data, type: t.type};
						};
					}
				}else{
					// we have no creator defined => move/clone nodes
					if(copy){
						// clone nodes
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							var n = node.cloneNode(true);
							n.id = dojo.dnd.getUniqueId();
							return {node: n, data: t.data, type: t.type};
						};
					}else{
						// move nodes
						if(!this.current){ break; }
						this._normalizedCreator = function(node, hint){
							var t = source.getItem(node.id);
							return {node: node, data: t.data, type: t.type};
						};
					}
				}
			}
			this._removeSelection();
			if(this != source){
				this._removeAnchor();
			}
			if(this != source && !copy && !this.creator){
				source.selectNone();
			}
			this.insertNodes(true, nodes, this.before, this.current);
			if(this != source && !copy && this.creator){
				source.deleteSelectedNodes();
			}
			this._normalizedCreator = oldCreator;
		}while(false);
		this.onDndCancel();
	},
	onDndCancel: function(){
		// summary: topic event processor for /dnd/cancel, called to cancel the DnD operation
		if(this.targetAnchor){
			this._unmarkTargetAnchor();
			this.targetAnchor = null;
		}
		this.before = true;
		this.isDragging = false;
		this.mouseDown = false;
		this._changeState("Source", "");
		this._changeState("Target", "");
	},
	
	// utilities
	onOverEvent: function(){
		// summary: this function is called once, when mouse is over our container
		dojo.dnd.Source.superclass.onOverEvent.call(this);
		dojo.dnd.manager().overSource(this);
	},
	onOutEvent: function(){
		// summary: this function is called once, when mouse is out of our container
		dojo.dnd.Source.superclass.onOutEvent.call(this);
		dojo.dnd.manager().outSource(this);
	},
	_markTargetAnchor: function(before){
		// summary: assigns a class to the current target anchor based on "before" status
		// before: Boolean: insert before, if true, after otherwise
		if(this.current == this.targetAnchor && this.before == before){ return; }
		if(this.targetAnchor){
			this._removeItemClass(this.targetAnchor, this.before ? "Before" : "After");
		}
		this.targetAnchor = this.current;
		this.targetBox = null;
		this.before = before;
		if(this.targetAnchor){
			this._addItemClass(this.targetAnchor, this.before ? "Before" : "After");
		}
	},
	_unmarkTargetAnchor: function(){
		// summary: removes a class of the current target anchor based on "before" status
		if(!this.targetAnchor){ return; }
		this._removeItemClass(this.targetAnchor, this.before ? "Before" : "After");
		this.targetAnchor = null;
		this.targetBox = null;
		this.before = true;
	},
	_markDndStatus: function(copy){
		// summary: changes source's state based on "copy" status
		this._changeState("Source", copy ? "Copied" : "Moved");
	},
	_legalMouseDown: function(e){
		// summary: checks if user clicked on "approved" items
		// e: Event: mouse event
		if(!this.withHandles){ return true; }
		for(var node = e.target; node && !dojo.hasClass(node, "dojoDndItem"); node = node.parentNode){
			if(dojo.hasClass(node, "dojoDndHandle")){ return true; }
		}
		return false;	// Boolean
	}
});

dojo.declare("dojo.dnd.Target", dojo.dnd.Source, {
	// summary: a Target object, which can be used as a DnD target
	
	constructor: function(node, params){
		// summary: a constructor of the Target --- see the Source constructor for details
		this.isSource = false;
		dojo.removeClass(this.node, "dojoDndSource");
	},

	// markup methods
	markupFactory: function(params, node){
		params._skipStartup = true;
		return new dojo.dnd.Target(node, params);
	}
});

}


dojo.i18n._preloadLocalizations("dojo.nls.eFapsDojo", ["es-es", "es", "hu", "it-it", "de", "pt-br", "pl", "fr-fr", "zh-cn", "pt", "en-us", "zh", "ru", "xx", "fr", "zh-tw", "it", "cs", "en-gb", "de-de", "ja-jp", "ko-kr", "ko", "en", "ROOT", "ja"]);
