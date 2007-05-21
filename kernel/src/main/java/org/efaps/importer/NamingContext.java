/*
 * Copyright 2003 - 2007 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.importer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class NamingContext implements Context {

  private static Map<String, Object> BINDINGS = new HashMap<String, Object>();

  public Object addToEnvironment(String propName, Object propVal)
      throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public void bind(Name name, Object obj) throws NamingException {
    // TODO Auto-generated method stub
  }

  public void bind(String _name, Object _obj) throws NamingException {
    BINDINGS.put(_name, _obj);
  }

  public void close() throws NamingException {
    // TODO Auto-generated method stub

  }

  public Name composeName(Name name, Name prefix) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public String composeName(String name, String prefix) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public Context createSubcontext(Name name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public Context createSubcontext(String name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public void destroySubcontext(Name name) throws NamingException {
    // TODO Auto-generated method stub

  }

  public void destroySubcontext(String name) throws NamingException {
    // TODO Auto-generated method stub

  }

  public Hashtable<?, ?> getEnvironment() throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getNameInNamespace() throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public NameParser getNameParser(Name name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public NameParser getNameParser(String name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public NamingEnumeration<NameClassPair> list(Name name)
      throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public NamingEnumeration<NameClassPair> list(String name)
      throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public NamingEnumeration<Binding> listBindings(Name name)
      throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public NamingEnumeration<Binding> listBindings(String name)
      throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public Object lookup(Name name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public Object lookup(String _name) throws NamingException {
    Object obj = BINDINGS.get(_name);

    return obj;

  }

  public Object lookupLink(Name name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public Object lookupLink(String name) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public void rebind(Name name, Object obj) throws NamingException {
    // TODO Auto-generated method stub

  }

  public void rebind(String name, Object obj) throws NamingException {
    // TODO Auto-generated method stub

  }

  public Object removeFromEnvironment(String propName) throws NamingException {
    // TODO Auto-generated method stub
    return null;
  }

  public void rename(Name oldName, Name newName) throws NamingException {
    // TODO Auto-generated method stub

  }

  public void rename(String oldName, String newName) throws NamingException {
    // TODO Auto-generated method stub

  }

  public void unbind(Name name) throws NamingException {
    // TODO Auto-generated method stub

  }

  public void unbind(String name) throws NamingException {
    // TODO Auto-generated method stub

  }
}
