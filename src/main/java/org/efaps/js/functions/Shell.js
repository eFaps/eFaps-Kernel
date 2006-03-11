function _shellHelp(_outp)  {
  _outp.println("");
  _outp.println("Command    Description");
  _outp.println("~~~~~~~    ~~~~~~~~~~~");
  _outp.println("help       Display usage and help messages.");
  _outp.println("export     Exports eFaps objects. Use 'export -help'");
  _outp.println("           to get further help.");
  _outp.println("import     Import eFaps objects. Use 'import -help'");
  _outp.println("           to get further help.");
  _outp.println("quit       Quit the eFaps shell.");
  _outp.println("");
}

function shell()  {
  var inp = new java.io.BufferedReader(new java.io.InputStreamReader(Main.getIn()));
  var outp = Main.getOut();
  outp.println("eFaps Shell");

  var exit = false;
  while (!exit)  {
    outp.print("eFaps> ");
    var line = inp.readLine();

    var scan = new Packages.java.util.Scanner(line);

    if (scan.hasNext())  {

      var cmd = scan.next();

      if (cmd=="quit")  {
        exit = true;
      } else if (cmd=="help")  {
        _shellHelp(outp);
      } else if (cmd=="export")  {
        var array = new Array();
        while (scan.hasNext())  {
          array.push(scan.next());
        }
        _shellExport(outp, array);
      } else if (cmd=="import")  {
        var array = new Array();
        while (scan.hasNext())  {
          array.push(scan.next());
        }
        _shellImport(outp, array);
      } else  {
        outp.println("Unknown command '"+cmd+"'. Use help for further information.");
      }
    }
  }
}
