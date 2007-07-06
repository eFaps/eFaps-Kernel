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
  var reader;
  var errorString;
  
  try  {
    var stream = _eFapsClassLoader.getResourceAsStream(_fileName);
    if (stream!=null)  {
      var reader = new InputStreamReader(stream);
    } else  {
      var reader = new FileReader(new File(_fileName));
    }
    javaScriptContext.evaluateReader(javaScriptScope, reader, _fileName, 1, null);
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
 * Prints the given text out.
 *
 * @param _text (String) text to print out
 */
function print(_text)  {
  java.lang.System.out.println(_text);
}

// load all scripts defined in the functions directory
eFapsLoadPath("org/efaps/js/functions", true);

