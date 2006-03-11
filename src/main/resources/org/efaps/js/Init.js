importClass(Packages.java.io.File);
importClass(Packages.java.io.FileReader);
importClass(Packages.java.io.InputStreamReader);
importClass(Packages.java.util.TreeSet);

importClass(Packages.org.mozilla.javascript.tools.shell.Main);

importClass(Packages.org.efaps.js.Shell);



_eFapsClassLoader = new Shell().getClass().getClassLoader();

/**
 * Returns all files in the given path including sub directories if wanted.
 *
 * @param _file    (File)     path
 * @param _subDir  (boolean)  if true, also files in the subdirectory are 
 *                            returned
 * @return (TreeSet) sorted set with all found file names
 */
function _eFapsGetAllFilesFromPath(_file, _subDir)  {
  var files = new TreeSet();
  var list = _file.list();
  if (list!=null)  {
    for  (index in list)  {
      var fileName = _file.toString() + "/" + list[index];
      var testFile = new File(fileName);
      if (testFile.isFile())  {
        files.add(fileName);
      } else if (testFile.isDirectory() && _subDir)  {
        files.addAll(_eFapsGetAllFilesFromPath(new File(fileName), _subDir));
      }
    }
  }
  return files;
}

/**
 * @param _url    (URL)
 * @param _subDir (boolean) if true, also files in the subdirectory are 
 *                          returned
 */
function _eFapsGetAllFilesFromJar(_url, _subDir)  {
  var files = new TreeSet();

  var path = _url.toURI().toString();
  path = path.substring(path.indexOf('!')+2)
  if (path.endsWith("/"))  {
    path = path.substring(0, path.length()-1);
  }
  var entries = _url.openConnection().getJarFile().entries();
  while (entries.hasMoreElements())  {
    var entry = entries.nextElement().getName();
    if (entry.startsWith(path) && !entry.endsWith("/"))  {
      if (_subDir)  {
        files.add(entry);
      } else  {
        var subPath = entry.substring(0, entry.lastIndexOf('/'));
        if (path.equals(subPath))  {
          files.add(entry);
        }
      }
    }
  }
  return files;
}

/**
 * @param _path    (String) 
 * @param _subDir  (boolean)  if true, also files in the subdirectory are 
 *                            returned
 * @return (Array) set of found files
 */
function eFapsGetAllFiles(_path, _subDir)  {
  var files = null;
  var url = _eFapsClassLoader.getResource(_path);
  if (url==null)  {
    files = _eFapsGetAllFilesFromPath(new File(_path), _subDir);
  } else  {
    var schema = url.toURI().getScheme();
    if (schema=="file")  {
      files = _eFapsGetAllFilesFromPath(new File(url.toURI()), _subDir);
    } else if (schema=="jar")  {
      files = _eFapsGetAllFilesFromJar(url, _subDir);
    } else  {
// TODO: unknown what to do
    }
  }
  return files.toArray();
}


/**
 * The load command replaces the original load. The load command is extended,
 * that the load also loads javascript files from the java classpath 
 * (including javascript files in jar files). If a file found via class path 
 * and via normal path, the file is loaded via class path!
 *
 * @param _fileName     (String)  name of the file to load
 * @param _throwNoError (Boolean) true, if no error should thrown
 */
function eFapsLoad(_fileName, _throwNoError)  {
  var context = Packages.org.mozilla.javascript.Context.enter();
  var reader;
  var errorString;
  
  try  {
    var stream = _eFapsClassLoader.getResourceAsStream(_fileName);
    if (stream!=null)  {
      var reader = new InputStreamReader(stream);
    } else  {
      var reader = new FileReader(new File(_fileName));
    }
    context.evaluateReader(Main.getGlobal(), reader, _fileName, 1, null);
  } catch (e)  {
    if (_throwNoError)  {
      print('');
      print('File \''+_fileName+'\' not loadable:');
      print(e.toString());
      print('');
    } else  {
      throw e.toString();
    }
  } finally  {
    try  {reader.close();} catch(f) {}
  }
  return errorString;
}

/**
 * Loads all found javascript files in the given directory.
 *
 * @param _path   (String)  path of the directory
 * @param _subDir (boolean) if true, also javascipt files in the 
 *                          subdirectories are loaded
 */
function eFapsLoadPath(_path, _subDir)  {
  var files = eFapsGetAllFiles(_path, _subDir);
  for (var i=0; i<files.length; i++)  {
    if (files[i].endsWith(".js"))  {
      eFapsLoad(files[i], true);
    }    
  }
}

/**
 * Store the original load command in an other function called originalLoad to 
 * be sure, that the original load command exists if the current load command
 * does not work correctly.<br/>
 * The original load is overwritten with the specific implementation from this
 * javascript file.<br/>
 * All functions and classes in the sub directories are loaded.
 */
try  {
  originalLoad
} catch (e)  {
  originalLoad=load
  load=eFapsLoad;
  eFapsLoadPath("org/efaps/js/functions", true);
  eFapsLoadPath("org/efaps/js/classes", true);
}
