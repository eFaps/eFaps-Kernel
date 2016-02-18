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

package org.efaps.jaas.xml;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The class implements the {@link java.security.Principal} interface for a
 * person. The class is used from the {@link XMLUserLoginModule} class to implement
 * a JAAS login module and set the person principals.<br/>
 * A person principal instance stores also all found {@link #groups} and
 * {@link #roles}.
 *
 * @author The eFaps Team
 *
 */
public class XMLPersonPrincipal
    extends AbstractXMLPrincipal
{
    /***
     * The password of this person is stored in this instance variable.
     *
     * @see #getPassword()
     * @see #setPassword(String)
     */
    private String password = null;

    /***
     * The first name of this person is stored in this instance variable.
     *
     * @see #getFirstName
     * @see #setFirstName
     */
    private String firstName = null;

    /***
     * The last name of this person is stored in this instance variable.
     *
     * @see #getLastName
     * @see #setLastName
     */
    private String lastName = null;

    /***
     * The email adresse of this person is stored in this instance variable.
     *
     * @see #getEmail
     * @see #setEmail
     */
    private String email = null;

    /***
     * The organization name of this person is stored in this instance variable.
     *
     * @see #getOrganisation
     * @see #setOrganisation
     */
    private String organisation = null;

    /***
     * The URL of this person is stored in this instance variable.
     *
     * @see #getUrl
     * @see #setUrl
     */
    private String url = null;

    /***
     * The phone number of this person is stored in this instance variable.
     *
     * @see #getPhone
     * @see #setPhone
     */
    private String phone = null;

    /***
     * The mobile number of this person is stored in this instance variable.
     *
     * @see #getMobile
     * @see #setMobile
     */
    private String mobile = null;

    /***
     * The fax number of this person is stored in this instance variable.
     *
     * @see #getFax
     * @see #setFax
     */
    private String fax = null;

    /**
     * All groups assign to this person are stored in this instance variable.
     *
     * @see #getRoles
     */
    private final Set<XMLRolePrincipal> roles = new HashSet<XMLRolePrincipal>();

    /**
     * All groups assign to this person are stored in this instance variable.
     *
     * @see #getGroups
     */
    private final Set<XMLGroupPrincipal> groups = new HashSet<XMLGroupPrincipal>();

    /**
     * A new role with givevn name is added. The role is added as role
     * principal (instance of {@link XMLRolePrincipal}).
     *
     * @param _role   name of role to add
     * @see #roles
     */
    public void addRole(final String _role)
    {
        this.roles.add(new XMLRolePrincipal(_role));
    }

    /**
     * A new group with givevn name is added. The group is added as role
     * principal (instance of {@link XMLGroupPrincipal}).
     *
     * @param _group  name of group to add
     * @see #groups
     */
    public void addGroup(final String _group)
    {
        this.groups.add(new XMLGroupPrincipal(_group));
    }

    /**
     * Returns the password of this principal stored in instance variable
     * {@link #password}.
     *
     * @return name of this person principal
     * @see #password
     * @see #setPassword
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Sets the password of this person principal stored in instance variable
     * {@link #password}. The method must be public, because it is set from the
     * XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _password new name to set for this person principal
     * @see #password
     * @see #getPassword
     */
    public void setPassword(final String _password)
    {
        this.password = _password;
    }

    /**
     * Returns the first name of this person principal stored in instance
     * variable {@link #firstName}.
     *
     * @return name of this person principal
     * @see #firstName
     * @see #setFirstName
     */
    public String getFirstName()
    {
        return this.firstName;
    }

    /**
     * Sets the first name of this person principal stored in instance variable
     * {@link #firstName}. The method must be public, because it is set from
     * the XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _firstName new first name to set for this person principal
     * @see #firstName
     * @see #getFirstName
     */
    public void setFirstName(final String _firstName)
    {
        this.firstName = _firstName;
    }

    /**
     * Returns the last name of this person principal stored in instance
     * variable {@link #lastName}.
     *
     * @return name of this person principal
     * @see #lastName
     * @see #setLastName
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * Sets the last name of this person principal stored in instance variable
     * {@link #lastNaem}. The method must be public, because it is set from the
     * XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _lastName new last name to set for this person principal
     * @see #lastName
     * @see #getLastName
     */
    public void setLastName(final String _lastName)
    {
        this.lastName = _lastName;
    }

    /**
     * Returns the email of this person principal stored in instance variable
     * {@link #email}.
     *
     * @return email address of this person principal
     * @see #email
     * @see #setEmail
     */
    public String getEmail()
    {
        return this.email;
    }

    /**
     * Sets the email address of this person principal stored in instance
     * variable {@link #email}. The method must be public, because it is set
     * from the XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _email new email address to set for this person principal
     * @see #email
     * @see #getEmail
     */
    public void setEmail(final String _email)
    {
        this.email = _email;
    }

    /**
     * Returns the organization name of this person principal stored in instance
     * variable {@link #organisation}.
     *
     * @return organization name of this person principal
     * @see #organisation
     * @see #setOrganisation
     */
    public String getOrganisation()
    {
        return this.organisation;
    }

    /**
     * Sets the organisation name of this person principal stored in instance
     * variable {@link #organisation}. The method must be public, because it is
     * set from the XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _organisation new organisation name to set for this person
     *                      principal
     * @see #organisation
     * @see #getOrganisation
     */
    public void setOrganisation(final String _organisation)
    {
        this.organisation = _organisation;
    }

    /**
     * Returns the URL of this person principal stored in instance variable
     * {@link #url}.
     *
     * @return URL address of this person principal
     * @see #url
     * @see #setUrl
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * Sets the URL address of this person principal stored in instance
     * variable {@link #url}. The method must be public, because it is set from
     * the XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _url new url address to set for this person principal
     * @see #url
     * @see #getUrl
     */
    public void setUrl(final String _url)
    {
        this.url = _url;
    }

    /**
     * Returns the phone number of this person principal stored in instance
     * variable {@link #phone}.
     *
     * @return phone number of this person principal
     * @see #phone
     * @see #setPhone
     */
    public String getPhone()
    {
        return this.phone;
    }

    /**
     * Sets the phone number of this person principal stored in instance
     * variable {@link #phone}. The method must be public, because it is set
     * from the XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _phone new phone number to set for this person principal
     * @see #phone
     * @see #getPhone
     */
    public void setPhone(final String _phone)
    {
        this.phone = _phone;
    }

    /**
     * Returns the mobile number of this person principal stored in instance
     * variable {@link #mobile}.
     *
     * @return mobile number of this person principal
     * @see #mobile
     * @see #setMobile
     */
    public String getMobile()
    {
        return this.mobile;
    }

    /**
     * Sets the mobile number of this person principal stored in instance variable
     * {@link #mobile}. The method must be public, because it is set from the
     * XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _mobile new phone number to set for this person principal
     * @see #mobile
     * @see #getMobile
     */
    public void setMobile(final String _mobile)
    {
        this.mobile = _mobile;
    }

    /**
     * Returns the fax number of this person principal stored in instance
     * variable {@link #fax}.
     *
     * @return fax number of this person principal
     * @see #fax
     * @see #setFax
     */
    public String getFax()
    {
        return this.fax;
    }

    /**
     * Sets the fax number of this person principal stored in instance variable
     * {@link #fax}. The method must be public, because it is set from the
     * XML to bean converter in {@link XMLUserLoginModule}.
     *
     * @param _fax new fax number to set for this person principal
     * @see #fax
     * @see #getPhone
     */
    public void setFax(final String _fax)
    {
        this.fax = _fax;
    }

    /**
     * Returns the name of this principal stored in instance variable
     * {@link #roles}.
     *
     * @return all assigned roles of this person principal
     * @see #roles
     */
    public Set<XMLRolePrincipal> getRoles()
    {
        return this.roles;
    }

    /**
     * Returns the groups of this principal stored in instance variable
     * {@link #groups}.
     *
     * @return all assigned groups of this person principal
     * @see #groups
     */
    public Set<XMLGroupPrincipal> getGroups()
    {
        return this.groups;
    }

    /**
     * Returns a string representation of this person principal.
     *
     * @return string representation of this person principal
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("firstName",    getFirstName())
            .append("lastName",     getLastName())
            .append("email",        getEmail())
            .append("organisation", getOrganisation())
            .append("url",          getEmail())
            .append("phone",        getPhone())
            .append("fax",          getFax())
            .append("password",     getPassword())
            .append("roles",        getRoles())
            .append("groups",       getGroups())
            .toString();
    }
}
