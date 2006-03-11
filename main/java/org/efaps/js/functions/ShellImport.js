

function _shellImportHelp()  {
  print("Import Help");
  print("~~~~~~~~~~~");
  print("-help");
  print("");
  print("-admin [MATCH]");
  print("");
  print("-userinterface / -ui [MATCH]");
  print("");
  print("Data Model related:");
  print("===================");
  print("-sqltable [MATCH]              import SQL table");
  print("-type [MATCH]                  import types");
  print("");
  print("User Interface related:");
  print("=======================");
  print("-command [MATCH]               import commands");
  print("-form [MATCH]                  import forms");
  print("-image [MATCH]                 import images");
  print("-menu [MATCH]                  import menus");
  print("-search [MATCH]                import searches");
  print("-table [MATCH]                 import tables");
}


function _shellImport(_outp, _args)  {
  var path = "";
  var author = "";
  for (var i=0; i<_args.length; i++)  {
    var arg = _args[i];
    if (arg=="-admin")  {
      var matchAdmin = _args[++i];
    } else if (arg=="-command")  {
      var matchCommand = _args[++i];
    } else if (arg=="-help")  {
      _shellImportHelp();
      return;
    } else if (arg=="-form")  {
      var matchForm = _args[++i];
    } else if (arg=="-image")  {
      var matchImage = _args[++i];
    } else if (arg=="-menu")  {
      var matchMenu = _args[++i];
    } else if (arg=="-search")  {
      var matchSearch = _args[++i];
    } else if (arg=="-sqltable")  {
      var matchSqlTable = _args[++i];
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
    var fileList = eFapsGetAllFiles(matchAdmin, true);

    // import data model
    importSQLTables(fileList);
    importTypes(fileList);

    // first create all needed ui objects which can be referenced
    createMenus(fileList);
    createSearches(fileList);

    // now import all ui objects
    importImages(fileList);
    importForms(fileList);
    importTables(fileList);
    importCommands(fileList);
    importMenus(fileList);
    importSearches(fileList);
  } else if (matchUI)  {
    var fileList = eFapsGetAllFiles(matchUI, true);

    // first create all needed ui objects which can be referenced
    createMenus(fileList);
    createSearches(fileList);

    // now import all ui objects
    importImages(fileList);
    importForms(fileList);
    importTables(fileList);
    importCommands(fileList);
    importMenus(fileList);
    importSearches(fileList);
  } else  {
    // import data model
    if (matchSqlTable)  {
      var fileList = eFapsGetAllFiles(matchSqlTable, true);
      importSQLTables(fileList);
    }
    if (matchType)  {
      var fileList = eFapsGetAllFiles(matchType, true);
      importTypes(fileList);
    }

    // first create all needed ui objects which can be referenced
    if (matchMenu)  {
      var fileList = eFapsGetAllFiles(matchMenu, true);
      createMenus(fileList);
    }
    if (matchSearch)  {
      var fileList = eFapsGetAllFiles(matchSearch, true);
      createSearches(fileList);
    }

    // now import all ui objects
    if (matchImage)  {
      var fileList = eFapsGetAllFiles(matchImage, true);
      importImages(fileList);
    }
    if (matchForm)  {
      var fileList = eFapsGetAllFiles(matchForm, true);
      importForms(fileList);
    }
    if (matchTable)  {
      var fileList = eFapsGetAllFiles(matchTable, true);
      importTables(fileList);
    }
    if (matchCommand)  {
      var fileList = eFapsGetAllFiles(matchCommand, true);
      importCommands(fileList);
    }
    if (matchMenu)  {
      var fileList = eFapsGetAllFiles(matchMenu, true);
      importMenus(fileList);
    }
    if (matchSearch)  {
      var fileList = eFapsGetAllFiles(matchSearch, true);
      importSearches(fileList);
    }
  }
}
