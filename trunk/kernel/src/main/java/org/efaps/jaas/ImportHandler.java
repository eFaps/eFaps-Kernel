/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.jaas;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.user.JAASSystem;
import org.efaps.admin.user.Person;
import org.efaps.util.EFapsException;

/**
 * The class is used to import from all JAAS system the user into eFaps so that
 * all users which could authentificate them self outside eFaps are known inside
 * eFaps (e.g. to assign them for access or to send then an email).<br/> To
 * start an import, the class must be instanciated and method
 * {@link #importPersons} must be called.
 *
 * @author tmo
 * @version $Id$
 */
public class ImportHandler extends LoginHandler {

  // ////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(ImportHandler.class);

  // ////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All person mapper instances are stored in this instance variable.
   */
  private final Set<PersonMapper> persMappers = new HashSet<PersonMapper>();

  /**
   * Map the person mapper instances to the user name of the persons.
   */
  private final Map<String, PersonMapper> name2persMapper =
      new HashMap<String, PersonMapper>();

  /**
   * Map the person mapper instances to the eFaps person instances.
   */
  private final Map<Person, PersonMapper> pers2persMapper =
      new HashMap<Person, PersonMapper>();

  // ////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to initialize the login handler. If <i>null</i> is given to
   * the application name, the default value defined in {@link #application} is
   * used.
   *
   * @param _application
   *                application name of the JAAS configuration
   */
  public ImportHandler(final String _application) {
    super(_application);
  }

  // ////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The method calls first method {@link #readPersons} to read all persons in
   * the cache. Then all found persons are updated with the information from
   *
   * @see #readPersons
   * @see #updatePersons
   */
  public void importPersons() {
    readPersons();
    updatePersons();
  }

  /**
   * The algorithm for identification of persons with specified key and name in
   * a JAAS system is:
   * <ul>
   * <li>search person by key in eFaps</li>
   * <li>if not found search person by name in eFaps</li>
   * </ul>
   * If no person inside eFaps is found, search if another JAAS system
   * identifies a person with the same name!<br/> If an exception is thrown
   * inside called methods, the exceptions are only written to the log as
   * errors.
   */
  protected void readPersons() {
    try {
      final LoginContext login =
          new LoginContext(getApplication(), new LoginCallbackHandler(
              ActionCallback.Mode.ALL_PERSONS, null, null));
      login.login();

      for (JAASSystem system : JAASSystem.getAllJAASSystems()) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("check JAAS system '" + system + "'");
        }
        final Set<?> users =
            login.getSubject().getPrincipals(
                system.getPersonJAASPrincipleClass());
        for (Object persObj : users) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("- check person '" + persObj + "'");
          }
          try {
            final String persKey =
                (String) system.getPersonMethodKey().invoke(persObj,
                    (Object) null);
            final String persName =
                (String) system.getPersonMethodName().invoke(persObj,
                    (Object) null);

            PersonMapper persMapper = null;

            Person foundPerson = Person.getWithJAASKey(system, persKey);
            if (foundPerson == null) {
              foundPerson = Person.get(persName);
            }

            if (foundPerson == null) {
              persMapper = this.name2persMapper.get(persName);
            } else {
              persMapper = this.pers2persMapper.get(foundPerson);
              if (persMapper == null) {
                persMapper = this.name2persMapper.get(persName);
              }
            }

            if (persMapper == null) {
              persMapper = new PersonMapper(foundPerson, persName);
            }

            persMapper.addJAASSystem(system, persKey);

          } catch (EFapsException e) {
            LOG.error("could not search for person with JAAS key "
                + system.getName(), e);
          } catch (IllegalAccessException e) {
            LOG.error("could not execute person key method for system "
                + system.getName(), e);
          } catch (IllegalArgumentException e) {
            LOG.error("could not execute person key method for system "
                + system.getName(), e);
          } catch (InvocationTargetException e) {
            LOG.error("could not execute person key method for system "
                + system.getName(), e);
          }
        }
      }
    } catch (LoginException e) {
      LOG.error("could not create login context", e);
    }
  }

  /**
   * Each found person is updated in eFaps (or created if not already existing).
   * The methods from the login handler {@link LoginHandler} are used for the
   * update.<br/> Each user name is written as debug information to the log
   * file.<br/> If an exception is thrown inside called methods, the exceptions
   * are only written to the log as errors.
   *
   * @see LoginHandler#getPerson
   * @see LoginHandler#createPerson
   * @see LoginHandler#updatePerson
   * @see LoginHandler#updateRoles
   * @see LoginHandler#updateGroups
   */
  protected void updatePersons() {
    for (PersonMapper persMapper : this.persMappers) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("update person '" + persMapper.name + "'");
      }
      try {
        final LoginContext login =
            new LoginContext(getApplication(), new LoginCallbackHandler(
                ActionCallback.Mode.PERSON_INFORMATION, persMapper.name, null));
        login.login();

        Person person = getPerson(login);

        if (person == null) {
          person = createPerson(login);
        }

        if (person != null) {

          updatePerson(login, person);

          person.cleanUp();

          updateRoles(login, person);
          updateGroups(login, person);
        }
      } catch (EFapsException e) {
        LOG.error("update failed for '" + persMapper.name + "'", e);
      } catch (LoginException e) {
        LOG.error("update failed for '" + persMapper.name + "'", e);
      }
    }
  }

  // ////////////////////////////////////////////////////////////////////////////
  // internal classes

  /**
   *
   */
  private class PersonMapper {

    // /////////////////////////////////////////////////////////////////////////
    // instance variable

    /**
     * eFaps Person instance.
     */
    private final Person person;

    /**
     * User Name of the person.
     */
    private final String name;

    /**
     * Mapping of the key for the keys.
     */
    private final Map<JAASSystem, String> keys =
        new HashMap<JAASSystem, String>();

    // /////////////////////////////////////////////////////////////////////////
    // constructor

    /**
     * If a new person mapper object is instanciated, the mapping for names to
     * person mapper in {@link #name2persMapper} and the mapping for persons to
     * person mapper in {@link #pers2persMapper} are updated. The new instance
     * is added to the list of all person mappers in {@link #persMappers}.
     *
     * @param _person
     *                JAVA person instance (if already existing in eFaps)
     * @param _name
     *                name of the person
     */
    PersonMapper(final Person _person, final String _name) {
      this.person = _person;
      this.name = _name;
      ImportHandler.this.name2persMapper.put(this.name, this);
      if (this.person != null) {
        ImportHandler.this.pers2persMapper.put(this.person, this);
      }
      ImportHandler.this.persMappers.add(this);
    }

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * A new key of the person for the JAAS system is defined for the person.
     *
     * @param _jaasSystem
     *                JAAS system in which the person is defined
     * @param _key
     *                key of the person in the given JAAS system
     */
    private void addJAASSystem(final JAASSystem _jaasSystem, final String _key) {
      this.keys.put(_jaasSystem, _key);
    }
  }
}
