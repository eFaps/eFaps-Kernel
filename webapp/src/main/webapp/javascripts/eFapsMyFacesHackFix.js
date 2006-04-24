//
// Overrides the original MyFaces and JSCookMenu function to work with eFaps
//
function cmItemMouseUp (obj, index)
{
  var item = _cmItemList[index];

  var link = null, target = '_self';

  if (item.length > 2)
    link = item[2];
  if (item.length > 3 && item[3])
    form = item[3];
  if (item.length > 4 && item[4])
    target = item[4];

  if (link != null)
  {
    // changes by Richard J. Barbalace
    if (link.match(/^\w*:A\]relative:/) != null ) {
      // Link is a relative URL
      link = link.replace(/^\w*:A\]relative:/, "");  // Remove JSF ID and relative marker
	  var erg = target.match("popup(.*)x(.*)");
	  if (erg)
	  {
		var height = erg[1];
		var width  = erg[2];
        var properties = "height="+height+",width="+width
		this.open(link, "_blank", properties);
	  }
      else
      {
		this.open(link, target, properties);
      }
/*	  else
	  {
		  this.document.forms[form].target = target;
		  this.document.forms[form].action = link;
		  this.document.forms[form].submit();
	  } */
    } else if (link.match(/^\w*:A\]\w*:\/\//) != null ) {
      // Link is a URL
      link = link.replace(/^\w*:A\]/, "");  // Remove JSF ID
      window.open (link, target);
    } else if (link.match(/^\w*:A\]\w*:/) != null ) {
      // Link is a script method
      link = link.replace(/^\w*:A\]\w*:/, "");  // Remove JSF ID
      eval(link);
    } else {
      // Link is a JSF action
      var dummyForm = document.forms[target];
      dummyForm.elements['jscook_action'].value = link;
      dummyForm.submit();
    }
  }

  var prefix = obj.cmPrefix;
  var thisMenu = cmGetThisMenu (obj, prefix);

  var hasChild = (item.length > 5);
  if (!hasChild)
  {
    if (cmIsDefaultItem (item))
    {
      if (obj.cmIsMain)
        obj.className = prefix + 'MainItem';
      else
        obj.className = prefix + 'MenuItem';
    }
    cmHideMenu (thisMenu, null, prefix);
  }
  else
  {
    if (cmIsDefaultItem (item))
    {
      if (obj.cmIsMain)
        obj.className = prefix + 'MainItemHover';
      else
        obj.className = prefix + 'MenuItemHover';
    }
  }
}

function loadURL(_link, _target)
{
	  if (findFrame(this, _target) != null)
	  {
		location.href = _link;
	  }
	  else
      {
		window.open(_link, _target);
	  }
}

function findFrame(_window, _frameName)
{
	if (_window.frames.length > 0)
	{
		for (var i = 0; i < _window.frames.length; i++)
		{
			if (_window.frames[i].name == _frameName)
			{
				return _window.frames[i];
			}
			else 
			{
				var foundFrame = findFrame(_window.frames[i], _frameName);
				if (foundFrame != null)
				{
					return foundFrame;
				}
			}
		}
	}

	return null;
}
