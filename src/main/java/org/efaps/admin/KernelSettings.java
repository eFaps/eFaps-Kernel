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

package org.efaps.admin;

import java.util.UUID;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public interface KernelSettings
{
    ///////////////////////////////
    // Static Variables use by the kernel
    ///////////////////////////////

    /**
     * Fixed UUID of the Main Administrator Person.
     */
    UUID USER_PERSON_ADMINISTRATOR = UUID.fromString("f48e4b45-d910-4ac8-8a08-a4e99b9ade09");

    /**
     * Fixed UUID of the Main Administration Role.
     */
    UUID USER_ROLE_ADMINISTRATION = UUID.fromString("1d89358d-165a-4689-8c78-fc625d37aacd");

    ///////////////////////////////
    // Settings Parameters
    ///////////////////////////////

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
     * Boolean (true/false): show the Keys for the DBProperties..
     */
    String SHOW_DBPROPERTIES_KEY = "org.efaps.kernel.ShowDBPropertiesKey";

    /**
     * Boolean (true/false): activate the BPM process mechanism.
     */
    String REQUIRE_PERSON_UUID = "org.efaps.kernel.RequirePersonUUID";

    /**
     * String: activate the BPM process mechanism.
     */
    String USERUI_DISPLAYPERSON = "org.efaps.kernel.UserUIDisplay4Person";

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

    /**
     * Properties. Can be Concatenated.
     * e.g. for Archives:<br/>
     * Archives_ArchiveRoot.Role.AsList=Role1;Role2<br/>
     * Archives_ArchiveRoot.Role.SimpleAccess4Type=Archives_Admin<br/>
     * Archives_ArchiveRoot.AccessSets=Archives_Modifier<br/>
     * Archives_ArchiveNode.ParentAttribute=ParentLink<br/>
     * Archives_ArchiveFile.ParentAttribute=ParentLink<br/>
     */
    String ACCESS4OBJECT = "org.efaps.kernel.AccessCheck4Object";

    /**
     * Profiles to be applied on update if not specified explicitly.
     * Properties. Can be Concatenated.
     * e.g. for Archives:<br/>
     * eFapsApp-Sales=Role.AsList=ubicaciones;products<br/>
     */
    String PROFILES4UPDATE = "org.efaps.kernel.update.Profiles";

    /**
     * Activate the general index mechanism.
     */
    String INDEXACTIVATE = "org.efaps.kernel.index.Activate";


    /**
     * ClassName of the class used for getting the Analyzer. Must implement
     * org.efaps.admin.index.IAnalyzerProvider
     */
    String INDEXANALYZERPROVCLASS = "org.efaps.kernel.index.AnalyzerProvider";

    /**
     * ClassName of the class used for getting the Directory. Must implement
     * org.efaps.admin.index.IDirectoryProvider
     */
    String INDEXDIRECTORYPROVCLASS = "org.efaps.kernel.index.DirectoryProvider";

    /**
     * ClassName of the class used for getting the SearchDefinition. Must implement
     * org.efaps.admin.index.ISearchDefinition.
     */
    String INDEXSEARCHCLASS = "org.efaps.kernel.index.Search";

}

