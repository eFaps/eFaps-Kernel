/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.admin;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface KernelSettings
{

    /**
     * Boolean (true/false): activate the BPM process mechanism. Default: false;
     */
    String ACTIVATE_BPM = "org.efaps.kernel.ActivateBPM";

    /**
     * String: 1.5, 1.6 or 1.7 (Default: 1.7) The Java version to be used to
     * compile the bpmn process files. The valeu will be send to the ECLISPE
     * Compiler by using the setting for
     * "drools.dialect.java.compiler.lnglevel".
     */
    String BPM_COMPILERLEVEL = "org.efaps.kernel.BPMCompilerLevel";

    /**
     * Boolean (true/false): activate the Groups Access Mechanism. Default:
     * false
     */
    String ACTIVATE_GROUPS = "org.efaps.kernel.ActivateGroups";

    /**
     * Boolean (true/false): activate the BPM process mechanism.
     */
    String SHOW_DBPROPERTIES_KEY = "org.efaps.kernel.ShowDBPropertiesKey";

    /**
     * Boolean (true/false): activate the BPM process mechanism.
     */
    String REQUIRE_PERSON_UUID = "org.efaps.kernel.RequirePersonUUID";

    /**
     * Integer: Maximum number of tries to login with the wrong Password into
     * eFaps, before the User is going to be deactivated. To deactivate this
     * mechanism set the value to 0.
     */
    String LOGIN_MAX_TRIES = "org.efaps.kernel.LoginMaximumTries";

    /**
     * Integer: This attribute defines the time in minutes which must elapse
     * after trying n-times to login with the wrong password, before the user
     * has again the possibility to try to login. To deactivate this mechanism
     * set the value to 0.
     */
    String LOGIN_TIME_RETRY = "org.efaps.kernel.LoginTimeElapseBeforeRetry";

    /**
     * Boolean (true/false): deactivate the Caching mechanism for Access.
     * Default: false
     */
    String DEACTIVATE_ACCESSCACHE = "org.efaps.kernel.DeactivateAccessCache";

    /**
     * Boolean (true/false): deactivate the Caching mechanism for Access.
     * Default: false
     */
    String DEACTIVATE_QUERYCACHE = "org.efaps.kernel.DeactivateQueryCache";

    /**
     * Integer: timeOut for JMS sessions. Default: 0
     */
    String JMS_TIMEOOUT = "org.efaps.kernel.JmsSessionTimeout";

    /**
     * Boolean (true/false): deactivate the Javascript Compression mechanism.
     * Default: false
     */
    String UPDATE_DEACTIVATEJSCOMP = "org.efaps.kernel.update.DeactivateJavaScriptCompression";

    /**
     * Boolean (true/false): activate the Javascript Compiler Warning mechanism.
     * Default: false
     */
    String UPDATE_ACTIVATEJSCOMPWAR = "org.efaps.kernel.update.ActivateJavaScriptCompiledWarn";

    /**
     * Properties: Key for the SystemConfiguration attribute that contains the
     * properties for the Quartz Scheduler.
     */
    String QUARTZPROPS = "org.efaps.kernel.QuartzProperties";

    /**
     * Boolean (true/false): Key for the SystemConfiguration attribute that
     * activates the SystemMessage Trigger.
     */
    String MSGTRIGGERACTIVE = "org.efaps.kernel.SystemMessageTriggerActivated";

    /**
     * Integer :Key for the SystemConfiguration attribute that sets the Interval
     * for the SystemMessage Trigger.
     */
    String MSGTRIGGERINTERVAL = "org.efaps.kernel.SystemMessageTriggerInterval";

    /**
     * String: TimeZoneId from Java Definition.
     */
    String DBTIMEZONE = "org.efaps.kernel.DataBaseTimeZone";

    /**
     * Properties: PasswordStore Digester Configuration.
     */
    String PWDSTORE = "org.efaps.kernel.PasswordStore";

    /**
     * Integer: The Threshold of how many passwords hashs will be stored to
     * compare it with a new given password.
     */
    String PWDTH = "org.efaps.kernel.PasswordRepeatedThreshold";

    /**
     * Integer: The Minimum length of a new Password.
     */
    String PWDLENGHT = "org.efaps.kernel.PasswordLenghtMinimum";

    /**
     * Properties:  This Attribute defines the Menu which is added as a DefaultMenu to all
     * Menubars. To deactivate this feature set the value to "none". The
     * DefaultMenu can also be deactivated individually in every Command or Menu
     * by setting the Property "TargetDefaultMenu" to "none".<br/>
     * e.g.:<br/>
     * Menu0=Common_Main_0Default<br/>
     * Enabled4Form0=false/true(default:false)<br/>
     * Enabled4Table0=false/true(default:true)<br/>
     */
    String DEFAULTMENU = "org.efaps.kernel.DefaultMenu";

    /**
     * String: The path to the libaries the webapp is build on. Needed for
     * compilation of java and Jasperreports. e.g.
     * "/tmp/Jetty_0_0_0_0_8060_efaps.war__efaps__.bo28gn/webapp/WEB-INF/lib/"
     */
    String CLASSPATHS = "org.efaps.kernel.rest.ClassPaths";
}

