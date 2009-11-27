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

package org.efaps.util;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;
import org.joda.time.chrono.JulianChronology;

import org.efaps.admin.dbproperty.DBProperties;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public enum ChronologyType {

  /**
   * Instance for the Buddhist Calendar.
   */
  BUDDHIST("Buddhist") {

    /**
     * Method to get an Instance of BuddhistChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of BuddhistChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return BuddhistChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the Coptic Calendar.
   */
  COPTIC("Coptic") {

    /**
     * Method to get an Instance of CopticChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of CopticChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return CopticChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the Ethiopic Calendar.
   */
  ETHIOPIC("Ethiopic") {

    /**
     * Method to get an Instance of EthiopicChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of EthiopicChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return EthiopicChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the Gregorian Calendar.
   */
  GREGORIAN("Gregorian") {

    /**
     * Method to get an Instance of GregorianChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of GregorianChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return GregorianChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the GregorianJulian Calendar.
   */
  GREGORIAN_JULIAN("GregorianJulian") {

    /**
     * Method to get an Instance of GJChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of GJChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return GJChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the Islamic Calendar.
   */
  ISLAMIC("Islamic") {

    /**
     * Method to get an Instance of IslamicChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of IslamicChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return IslamicChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the ISO8601 Calendar.
   */
  ISO8601("ISO8601") {

    /**
     * Method to get an Instance of ISOChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of ISOChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return ISOChronology.getInstance(_timeZone);
    }
  },

  /**
   * Instance for the Julian Calendar.
   */
  JULIAN("Julian") {

    /**
     * Method to get an Instance of JulianChronology with the given Timezone.
     *
     * @param _timeZone   Timezone the Chronology must use
     * @return an instance of JulianChronology
     */
    @Override
    public Chronology getInstance(final DateTimeZone _timeZone) {
      return JulianChronology.getInstance(_timeZone);
    }
  };

  /**
   * The class is required because an enum definition does not allow to own
   * and access directly static variables.
   */
  private static class Mapper {
      /**
       * Mapping between the Key of a Chronology Key and the related enum
       * instance.
       */
      private static final Map<String, ChronologyType> KEY2ENUM
              = new HashMap<String, ChronologyType>();
  }

  /**
   * Stores the key for the Type.
   */
  private final String key;

  /**
   * Constructor setting the instance variables.
   *
   * @param _key Key of the ChronologyType
   */
  private ChronologyType(final String _key) {
    this.key = _key;
    Mapper.KEY2ENUM.put(this.key, this);
  }

  /**
   * Method to get the Label for the ChronologyType.
   *
   * @return String containing the Label.
   */
  public String getLabel() {
    return DBProperties.getProperty("org.efaps.util.ChronologyType."
                                        + this.key);
  }

  /**
   * Method to get the instance of a ChronologyType by the Key.
   * @param _key  Key for the ChronologyType
   * @return instance of a ChronologyType
   */
  public static ChronologyType getByKey(final String _key) {
    final ChronologyType ret = Mapper.KEY2ENUM.get(_key);
    if (ret == null) {
        throw new IllegalArgumentException("Unknown Key '"
                                        + _key + "' for enum ChronologyType.");
    }
    return ret;

  }

  /**
   * Getter method for instance variable {@link #key}.
   *
   * @return value of instance variable {@link #key}
   *
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Method is overwritten by all instances.
   *
   * @param _timeZone Timzone the Chronology must belong to.
   * @return Chronology
   */
  public Chronology getInstance(final DateTimeZone _timeZone) {
    return null;
  }
}
