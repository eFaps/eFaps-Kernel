/**
 * Calender JavaScript Library
 *
 * Description:
 * ~~~~~~~~~~~~
 * The function "eFapsCalendarStart" must be called to show the calendar page.
 * The first parameter is the "id" of the object, where the calendar should be
 * displayed, the second is the "id", where the result should be displayed,
 * and the third is the "id", where a computer readable date value stands.
 *
 * Example:
 * ~~~~~~~~
 * <input id="dateReadable" type="hidden"/>
 * <input id="dateShowable" type="text"/>
 * <a id="dateCall"
 *    href="javascript:eFapsCalendarStart('dateCall',
 *                                        'dateShowable',
 *                                        'dateReadable')">
 * Call DatePage
 * </a>
 *
 * Following javascript variables must be set for international use:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * eFapsCalendarMonthNames:       array with the names of the months starting
 *                                with january
 * eFapsCalendarShortMonthNames:  array with the short names of the months
 *                                starting with january
 * eFapsCalendarStringShortDays:  array with the short name of the days
 *                                starting with monday
 * eFapsCalendarFormatStringDate: string of the format date
 * eFapsCalenderPathImages:       path to images
 *
 * License information:
 * ~~~~~~~~~~~~~~~~~~~~
 * The code is under LGPL license.
 */

/**
 * Set of the long names of the months if not defined before.
 */
if (!eFapsCalendarMonthNames)  {
  var eFapsCalendarMonthNames = new Array(
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December'
  );
}

/**
 * Set of the short names of the months if not defined before.
 */
if (!eFapsCalendarShortMonthNames)  {
  var eFapsCalendarShortMonthNames = new Array(
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec'
  );
}

/**
 * Set of the short day names only set if not defined before.
 */
if (!eFapsCalendarStringShortDays)  {
  var eFapsCalendarStringShortDays = new Array("MON","TUE","WED","THU","FRI","SAT","SUN");
}

/**
 * Set of the format of the date string if not defined before.
 */
if (!eFapsCalendarFormatStringDate)  {
  var eFapsCalendarFormatStringDate = "yyyy-MM-dd"
}

/**
 *
 */
if (!eFapsCalenderPathImages)  {
  var eFapsCalenderPathImages = ".";
}

/**
 * This is the dom object of the complete calendar object.
 */
var eFapsCalendarDOMComplete;

/**
 * This is the dom object where all the days are added.
 */
var eFapsCalendarDOMDays;

/**
 * Dom object of the header text element, where the month name and the year
 * is shown.
 */
var eFapsCalendarDOMHeaderText;

/**
 * Id name of the dom object of the for the user showable input object.
 */
var eFapsCalendarValueShowableId;

/**
 * Id name of the dom object of the for javascript readable input object.
 */
var eFapsCalendarValueReadableId;

/**
 * Is the calendar functionality initalised? If value is <i>false</i>, it is
 * not initialised, if value is <i>true</i>, calendar functionality is
 * initalised.
 *
 * @see #eFapsCalendarInit
 * @see #eFapsCalendarStart
 */
var eFapsCalendarInitalised = false;

/**
 * Value of the current showing date for the calendar.
 */
var eFapsCalendarShowDate;

/**
 * Value of the current stored date for the calendar.
 */
var eFapsCalendarCurrentDate;

/**
 * The calendar functionality works with the dom object model. This is done
 * with following steps:<br/>
 * - Create a table<br/>
 * - add header row for the buttons for previous and next month or year and
 *   the showing text of the month and year<br/>
 * - add header row of the short day names<br/>
 * - add content dom object (tfoot) to table<br/>
 * - add the table a div dom object (stored in variable
 *   <code>eFapsCalendarDOMComplete</code>), which is added to the document body
 *   element.<br/>
 * The div dom object is used to change the style of the display (shown and not
 * shown the calendar!).
 *
 * @see #eFapsCalendarAppendDayHeader
 * @see #eFapsCalendarAppendImageHeader
 */
function eFapsCalendarInit()  {
  eFapsCalendarInitalised = true;

  var table = document.createElement("table");
  table.className = "eFapsCalendarTable";

  var thead = document.createElement("thead");
  table.appendChild(thead);

  var tr = document.createElement("tr");
  thead.appendChild(tr);
  eFapsCalendarAppendImageHeader(tr, "eFapsCalendarPrevMonth", eFapsCalenderPathImages+"/eFapsCalendarMonthPrev.gif");
  eFapsCalendarAppendImageHeader(tr, "eFapsCalendarPrevYear", eFapsCalenderPathImages+"/eFapsCalendarYearPrev.gif");
  eFapsCalendarDOMHeaderText = document.createElement("td");
  eFapsCalendarDOMHeaderText.className = "eFapsCalendarHeadText";
  eFapsCalendarDOMHeaderText.colSpan = "3";
  tr.appendChild(eFapsCalendarDOMHeaderText);
  eFapsCalendarAppendImageHeader(tr, "eFapsCalendarNextYear", eFapsCalenderPathImages+"/eFapsCalendarYearNext.gif");
  eFapsCalendarAppendImageHeader(tr, "eFapsCalendarNextMonth", eFapsCalenderPathImages+"/eFapsCalendarMonthNext.gif");

  tr = document.createElement("tr");
  thead.appendChild(tr);
  tr.className = "eFapsCalendarHeader";
  eFapsCalendarAppendDayHeader(tr);
  eFapsCalendarDOMDays = document.createElement("tfoot");
  table.appendChild(eFapsCalendarDOMDays);

  eFapsCalendarDOMComplete = document.createElement("div");
  eFapsCalendarDOMComplete.style.display = 'none';
  eFapsCalendarDOMComplete.style.position = 'absolute';
  eFapsCalendarDOMComplete.appendChild(table);
  document.getElementsByTagName("body")[0].appendChild(eFapsCalendarDOMComplete);
}

/**
 * Append the short day names to the header <i>_tr</i>.
 *
 * @param _tr
 * @see #eFapsCalendarInit
 */
function eFapsCalendarAppendDayHeader(_tr)  {
  for (var i=0; i<eFapsCalendarStringShortDays.length; i++)  {
    var th = document.createElement("th");
    th.appendChild(document.createTextNode(eFapsCalendarStringShortDays[i]));
    _tr.appendChild(th);
  }
}

/**
 * @see #eFapsCalendarInit
 */
function eFapsCalendarAppendImageHeader(_tr, _func, _img)  {
  var td = document.createElement("td");
  _tr.appendChild(td);

  var a = document.createElement("a");
  td.appendChild(a);
  a.href = "javascript:"+_func+"()";

  var img = document.createElement("img");
  a.appendChild(img);
  img.src = _img;
  img.className = "eFapsCalendarImage";
}

/**
 * Show the month in the calendar.
 */
function eFapsCalendarShowMonth() {

  var node = eFapsCalendarDOMHeaderText.firstChild;
  while (node)  {
    eFapsCalendarDOMHeaderText.removeChild(node);
    node = eFapsCalendarDOMHeaderText.firstChild;
  }

  eFapsCalendarDOMHeaderText.appendChild(document.createTextNode(eFapsCalendarMonthNames[eFapsCalendarShowDate.getMonth()]));
  eFapsCalendarDOMHeaderText.appendChild(document.createElement("br"));
  eFapsCalendarDOMHeaderText.appendChild(document.createTextNode(eFapsCalendarShowDate.getFullYear()));
    ;
  var node = eFapsCalendarDOMDays.firstChild;
  while (node)  {
    eFapsCalendarDOMDays.removeChild(node);
    node = eFapsCalendarDOMDays.firstChild;
  }

  var startDay = 2;

  var testDate = new Date(eFapsCalendarShowDate.getFullYear(), eFapsCalendarShowDate.getMonth(), 1);
  var start = -testDate.getDay()+startDay;
  if (start>1)  {
    start-=7;
  }
  if (start<=-6)  {
    start+=7;
  }

  testDate = new Date(eFapsCalendarShowDate.getFullYear(), eFapsCalendarShowDate.getMonth()+1, 0);
  var end = testDate.getDate()+6-testDate.getDay()+startDay;
  if ((end-testDate.getDate())>7)  {
    end-=7;
  }
  if ((end-testDate.getDate())<1)  {
    end+=7;
  }

  var tr = document.createElement("tr");
  eFapsCalendarDOMDays.appendChild(tr);

  var aHRef;
  var newLine = (startDay-1 >= 0 ? startDay-1 : startDay+6);
  for (var i=start;i<end;i++)  {
    testDate = new Date(eFapsCalendarShowDate.getFullYear(), eFapsCalendarShowDate.getMonth(), i);
    if (testDate.getDay()==newLine)  {
      tr = document.createElement("tr");
      eFapsCalendarDOMDays.appendChild(tr);
    }
    var td = document.createElement("td");
    tr.appendChild(td);

    aHRef = document.createElement("a");
    aHRef.href="javascript:eFapsCalendarSetDate("+testDate.getFullYear()+","+
    testDate.getMonth()+","+testDate.getDate()+")";
    aHRef.appendChild(document.createTextNode(testDate.getDate()));
    td.appendChild(aHRef);

    td.align='right';
    if (testDate.getMonth()!=eFapsCalendarShowDate.getMonth())  {
      td.className='eFapsCalendarNotCurMonth';
      aHRef.className='eFapsCalendarNotCurMonth';
    } else if (testDate.getDate()==eFapsCalendarCurrentDate.getDate()
        && testDate.getMonth()==eFapsCalendarCurrentDate.getMonth()
        && testDate.getFullYear()==eFapsCalendarCurrentDate.getFullYear())  {
      td.className='eFapsCalendarCurDate';
      aHRef.className='eFapsCalendarCurDate';
    } else  {
      td.className='eFapsCalendarCurMonth';
      aHRef.className='eFapsCalendarCurMonth';
    }
  }
}

/**
 * Add one month to the date.
 */
function eFapsCalendarNextMonth()  {
  eFapsCalendarShowDate.setMonth(eFapsCalendarShowDate.getMonth()+1);
  eFapsCalendarShowMonth();
}

/**
 * Subtract one month to the date.
 */
function eFapsCalendarPrevMonth()  {
  eFapsCalendarShowDate.setMonth(eFapsCalendarShowDate.getMonth()-1);
  eFapsCalendarShowMonth();
}

/**
 * Add one year to the date.
 */
function eFapsCalendarNextYear()  {
  eFapsCalendarShowDate.setYear(eFapsCalendarShowDate.getFullYear()+1);
  eFapsCalendarShowMonth();
}

/**
 * Subtract one year to the date.
 */
function eFapsCalendarPrevYear()  {
  eFapsCalendarShowDate.setYear(eFapsCalendarShowDate.getFullYear()-1);
  eFapsCalendarShowMonth();
}

/**
 * Set the date in the input dom objects.
 *
 * @param _year   year value
 * @param _month  month in year value
 * @param _date   day in month value
 */
function eFapsCalendarSetDate(_year, _month, _date)  {
  eFapsCalendarDOMComplete.style.display = 'none';

  var date = new Date(_year, _month, _date);

  var obj = document.getElementById(eFapsCalendarValueShowableId);
  obj.value = eFapsCalendarFormatDate(date, eFapsCalendarFormatStringDate);

  obj = document.getElementById(eFapsCalendarValueReadableId);
  obj.value = eFapsCalendarFormatDate(date, "yyyy-MM-dd");
}

/**
 * If the calendar is not initialised, initialise it first.
 * Store the names of the showable and readable id of the object ids.
 * Change the position of the calendar at the dom object (given with the
 * parameter <code>_showId</code>). The starting date is the date in the
 * value of dom object represented with the readable id. If the value is
 * blank, the date value is the current date. Show the calender (changing the
 * display style to nothing).
 *
 * @param _showId     id of the dom object where the calender will show
 * @param _valueShowableId  id of the showable date dom object
 * @param _valueReadableId  id of the readable date dom object
 * @see #eFapsCalendarInit
 */
function eFapsCalendarStart(_showId, _valueShowableId, _valueReadableId)  {
  if (eFapsCalendarInitalised == false)  {
    eFapsCalendarInit();
  }

  eFapsCalendarValueShowableId = _valueShowableId;
  eFapsCalendarValueReadableId = _valueReadableId;

  var obj = document.getElementById(_showId);
  var parent = obj;
  var xPos = 0;
  var yPos = 0;
  while (parent)  {
    xPos += parent.offsetLeft;
    yPos += parent.offsetTop;
    parent = parent.offsetParent;
  }
  yPos += obj.offsetHeight;
  eFapsCalendarDOMComplete.style.top = ''+yPos+'px';
  eFapsCalendarDOMComplete.style.left = ''+xPos+'px';

  var value = document.getElementById(_valueReadableId).value;
  if (!value || value.length==0)  {
    eFapsCalendarShowDate = new Date();
  } else  {
    value = value.split("-");
    if (value.length!=3)  {
      eFapsCalendarShowDate = new Date();
    } else  {
      eFapsCalendarShowDate = new Date(value[0], eval(value[1]+"-1"), value[2]);
    }
  }
  eFapsCalendarCurrentDate = new Date(eFapsCalendarShowDate.getFullYear(),
      eFapsCalendarShowDate.getMonth(), eFapsCalendarShowDate.getDate());

  eFapsCalendarShowMonth();
  eFapsCalendarDOMComplete.style.display = '';
}

/**
 * Returns a formated string of numbers with a <i>0</i> in front of the
 * number if the number is lower than ten.
 *
 * @param _number number to format
 * @return formated number string
 */
function eFapsCalendarAddZero(_number){
  return ((_number < 10) ? "0" : "") + _number;
}

/**
 * Format the date depending of the format and return this string.
 *
 * @return formated date string.
 * @param _date   date value to format
 * @param _format format string
 */
function eFapsCalendarFormatDate(_date, _format){
  var vDay            = eFapsCalendarAddZero(_date.getDate());
  var vMonth          = eFapsCalendarAddZero(_date.getMonth()+1);
  var vYearLong       = eFapsCalendarAddZero(_date.getFullYear());
  var vYearShort      = eFapsCalendarAddZero(_date.getFullYear().toString().substring(3,4));
  var vYear           = (_format.indexOf("yyyy")>-1?vYearLong:vYearShort)
  var vHour           = eFapsCalendarAddZero(_date.getHours());
  var vMinute         = eFapsCalendarAddZero(_date.getMinutes());
  var vSecond         = eFapsCalendarAddZero(_date.getSeconds());
  var dateString      = _format.replace(/dd/g, vDay);
  dateString          = dateString.replace(/d/g, _date.getDate());
  dateString          = dateString.replace(/y{1,4}/g, vYear);
  dateString          = dateString.replace(/MMM/g, eFapsCalendarShortMonthNames[_date.getMonth()]);
  dateString          = dateString.replace(/MM/g, vMonth);
  dateString          = dateString.replace(/hh/g, vHour).replace(/mm/g, vMinute).replace(/ss/g, vSecond)
  return dateString
}
