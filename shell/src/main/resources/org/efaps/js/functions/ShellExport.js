

function _shellExportHelp()  {
  print("Export Help");
  print("~~~~~~~~~~~");
  print("-help");
  print("-path [PATH]");
  print("-author [AUTHOR]");
  print("");
  print("-admin [MATCH]");
  print("");
  print("Data Model related:");
  print("===================");
  print("-type [MATCH]");
  print("");
  print("User Interface related:");
  print("=======================");
  print("-userinterface / -ui [MATCH]");
  print("");
  print("-command [MATCH]               ");
  print("-form [MATCH]                  ");
  print("-image [MATCH]                 ");
  print("-menu [MATCH]                  ");
  print("-search [MATCH]                ");
  print("-table [MATCH]                 export matching tables");
}


function _shellExport(_outp, _args)  {
  var path = "";
  var author = "";
  for (var i=0; i<_args.length; i++)  {
    var arg = _args[i];
    if (arg=="-admin")  {
      var matchAdmin = _args[++i];
    } else if (arg=="-author")  {
      author = _args[++i];
    } else if (arg=="-command")  {
      var matchCommand = _args[++i];
    } else if (arg=="-form")  {
      var matchForm = _args[++i];
    } else if (arg=="-help")  {
      _shellExportHelp();
      return;
    } else if (arg=="-image")  {
      var matchImage = _args[++i];
    } else if (arg=="-menu")  {
      var matchMenu = _args[++i];
    } else if (arg=="-path")  {
      path = _args[++i];
    } else if (arg=="-search")  {
      var matchSearche = _args[++i];
    } else if (arg=="-table")  {
      var matchTable = _args[++i];
    } else if (arg=="-type")  {
      var matchType = _args[++i];
    } else if (arg=="-ui" || arg=="-userinterface")  {
      var matchUI = _args[++i];
    } else  {
      _outp.println("Unknown parameter '"+arg+"'. Use -help for further information.");
      return;
    }
  }
  
  if (matchAdmin)  {
    createScriptTypes   (path, matchAdmin, author);
    createScriptImages  (path, matchAdmin, author);
    createScriptForms   (path, matchAdmin, author);
    createScriptTables  (path, matchAdmin, author);
    createScriptCommands(path, matchAdmin, author);
    createScriptMenus   (path, matchAdmin, author);
    createScriptSearches(path, matchAdmin, author);
  } else if (matchUI)  {
    createScriptImages(path, matchUI, author);
    createScriptForms(path, matchUI, author);
    createScriptTables(path, matchUI, author);
    createScriptCommands(path, matchUI, author);
    createScriptMenus(path, matchUI, author);
    createScriptSearches(path, matchUI, author);
  } else  {
    if (matchType)      {createScriptTypes(path, matchType, author);}

    if (matchImage)     {createScriptImages(path, matchImage, author);}
    if (matchForm)      {createScriptForms(path, matchForm, author);}
    if (matchTable)     {createScriptTables(path, matchTable, author);}
    if (matchCommand)   {createScriptCommands(path, matchCommand, author);}
    if (matchMenu)      {createScriptMenus(path, matchMenu, author);}
    if (matchSearche)   {createScriptSearches(path, matchSearche, author);}
  }
}
