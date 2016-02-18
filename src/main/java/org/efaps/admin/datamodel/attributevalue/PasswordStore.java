/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.admin.datamodel.attributevalue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.jasypt.digest.config.SimpleDigesterConfig;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class PasswordStore
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PasswordStore.class);

    /**
     * Prefix for the PropertyKey of the Digest.
     */
    private static final String DIGEST = "Digest";

    /**
     * Prefix for the PropertyKey of the Algorithm.
     */
    private static final String ALGORITHM = "Algorithm";

    /**
     * Prefix for the PropertyKey of the Iterations.
     */
    private static final String ITERATIONS = "Iterations";

    /**
     * Prefix for the PropertyKey of the Saltsize.
     */
    private static final String SALTSIZE = "Saltsize";

    /**
     * Internal Properties used to store the information.
     */
    private final Properties props = new Properties();

    /**
     * The configuration object for the digester.
     */
    private final SimpleDigesterConfig digesterConfig = new SimpleDigesterConfig();

    /**
     * The threshold for password uniqueness.
     */
    private int threshold = 5;

    /**
     * Constructor. To ensure the the Digester Configuration is valid some
     * default values are set.
     */
    public PasswordStore()
    {
        this.digesterConfig.setAlgorithm("SHA-256");
        this.digesterConfig.setIterations(100000);
        this.digesterConfig.setSaltSizeBytes(16);
    }

    /**
     * Initialize the digester configuration by reading values from the kernel
     * SystemConfiguration.
     */
    private void initConfig()
    {
        SystemConfiguration config = null;
        try {
            config = EFapsSystemConfiguration.get();
            if (config != null) {
                final Properties confProps = config.getAttributeValueAsProperties(KernelSettings.PWDSTORE);
                this.digesterConfig.setAlgorithm(confProps.getProperty(PasswordStore.ALGORITHM,
                                this.digesterConfig.getAlgorithm()));
                this.digesterConfig.setIterations(confProps.getProperty(PasswordStore.ITERATIONS,
                                this.digesterConfig.getIterations().toString()));
                this.digesterConfig.setSaltSizeBytes(confProps.getProperty(PasswordStore.SALTSIZE,
                                this.digesterConfig.getSaltSizeBytes().toString()));
                this.threshold = config.getAttributeValueAsInteger(KernelSettings.PWDTH);
            }
        } catch (final EFapsException e) {
            PasswordStore.LOG.error("Error on reading SystemConfiguration for PasswordStore", e);
        }

    }

    /**
     * Read a PasswordStore from a String.
     *
     * @param _readValue String to be read
     * @throws EFapsException on error
     */
    public void read(final String _readValue)
        throws EFapsException
    {
        if (_readValue != null) {
            try {
                this.props.load(new StringReader(_readValue));
            } catch (final IOException e) {
                throw new EFapsException(PasswordStore.class.getName(), e);
            }
        }
    }

    /**
     * Check the given Plain Text Password for equal on the current Hash by
     * applying the algorithm salt etc.
     *
     * @param _plainPassword plain text password
     * @return true if equal, else false
     */
    public boolean checkCurrent(final String _plainPassword)
    {
        return check(_plainPassword, 0);
    }

    /**
     * Check the given Plain Text Password for equal on the Hash by applying the
     * algorithm salt etc.
     *
     * @param _plainPassword plain text password
     * @param _pos position of the password to be checked
     * @return true if equal, else false
     */
    private boolean check(final String _plainPassword,
                          final int _pos)
    {
        boolean ret = false;
        if (this.props.containsKey(PasswordStore.DIGEST + _pos)) {
            final ConfigurablePasswordEncryptor passwordEncryptor = new ConfigurablePasswordEncryptor();
            this.digesterConfig.setAlgorithm(this.props.getProperty(PasswordStore.ALGORITHM + _pos));
            this.digesterConfig.setIterations(this.props.getProperty(PasswordStore.ITERATIONS + _pos));
            this.digesterConfig.setSaltSizeBytes(this.props.getProperty(PasswordStore.SALTSIZE + _pos));
            passwordEncryptor.setConfig(this.digesterConfig);
            ret = passwordEncryptor.checkPassword(_plainPassword, this.props.getProperty(PasswordStore.DIGEST + _pos));
        }
        return ret;
    }

    /**
     * Is the given plain password repeated. It is checked against the existing
     * previous passwords.
     *
     * @param _plainPassword plain text password
     * @return true if repeated, else false
     */
    public boolean isRepeated(final String _plainPassword)
    {
        boolean ret = false;
        for (int i = 1; i < this.threshold + 1; i++) {
            ret = check(_plainPassword, i);
            if (ret) {
                break;
            }
        }
        return ret;
    }

    /**
     * Set the given given Plain Password as the new current Password by
     * encrypting it.
     *
     * @param _plainPassword plain password to be used
     * @param _currentValue current value of the Store
     * @param _currentValue
     * @throws EFapsException on error
     */
    public void setNew(final String _plainPassword,
                       final String _currentValue)
        throws EFapsException
    {
        initConfig();
        read(_currentValue);
        final ConfigurablePasswordEncryptor passwordEncryptor = new ConfigurablePasswordEncryptor();
        passwordEncryptor.setConfig(this.digesterConfig);
        final String encrypted = passwordEncryptor.encryptPassword(_plainPassword);
        shiftAll();
        this.props.setProperty(PasswordStore.DIGEST + 0, encrypted);
        this.props.setProperty(PasswordStore.ALGORITHM + 0, this.digesterConfig.getAlgorithm());
        this.props.setProperty(PasswordStore.ITERATIONS + 0, this.digesterConfig.getIterations().toString());
        this.props.setProperty(PasswordStore.SALTSIZE + 0, this.digesterConfig.getSaltSizeBytes().toString());
    }

    /**
     * Shift all Properties.
     */
    private void shiftAll()
    {
        shift(PasswordStore.DIGEST);
        shift(PasswordStore.ALGORITHM);
        shift(PasswordStore.ITERATIONS);
        shift(PasswordStore.SALTSIZE);
    }

    /**
     * Shift a property.
     *
     * @param _key key the property must be shifted for
     */
    private void shift(final String _key)
    {
        for (int i = this.threshold; i > 0; i--) {
            this.props.setProperty(_key + i, this.props.getProperty(_key + (i - 1), "").trim());
        }
        int i = this.threshold + 1;
        while (this.props.contains(_key + i)) {
            this.props.remove(_key + i);
            i++;
        }
    }

    @Override
    public String toString()
    {
        final StringWriter writer = new StringWriter();
        try {
            this.props.store(writer, "Stored by User: " + Context.getThreadContext().getPersonId());
        } catch (final IOException e) {
            PasswordStore.LOG.error("Could not write to Writer", e);
        } catch (final EFapsException e) {
            PasswordStore.LOG.error("Could not read Context", e);
        }
        return writer.toString();
    }
}
