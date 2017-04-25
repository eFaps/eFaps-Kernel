/*
 * Copyright 2003 - 2017 The eFaps Team
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


package org.efaps.admin.program.esjp;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotation used to mark an esjp that it will be used as an Listener.
 *
 * @author The eFaps Team
 * @version $Id: EFapsListener.java 12673 2014-05-09 20:18:54Z jan@moxter.net $
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface EFapsFormatFactory
{

    /**
     * Name.
     *
     * @return the string
     */
    String name();
}
