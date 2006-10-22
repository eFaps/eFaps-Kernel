/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.webdav.method;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The enum defines the DAV properties described in RFC2518 chapter 13.
 */
public enum DAVProperty  {
  /**
   * Records the time and date the resource was created.
   */
  creationdate {
    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    private final SimpleDateFormat creationDateFormat
                        = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");  
    {
      this.creationDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    String makeXML(Object _creationDate) {
      StringBuffer ret = new StringBuffer();
      ret.append('<').append(name()).append(">")
         .append(creationDateFormat.format(_creationDate))
         .append("</").append(name()).append(">");
      return ret.toString();
    }
  },

  /**
   * Provides a name for the resource that is suitable for presentation to a
   * user.
   */
  displayname {
    String makeXML(Object _name) {
      StringBuffer ret = new StringBuffer();
      ret.append('<').append(name()).append(">")
         .append("<![CDATA[").append(_name).append("]]>")
         .append("</").append(name()).append(">");
      return ret.toString();
    }
  },

  /**
   * Contains the Content-Language header returned by a GET without accept
   * headers
   */
  getcontentlanguage {
  },

  /**
   * Contains the Content-Length header returned by a GET without accept
   * headers.
   */
  getcontentlength {
  },

  /**
   * Contains the Content-Type header returned by a GET without accept
   * headers.
   */
  getcontenttype {
  },

  /**
   * Contains the ETag header returned by a GET without accept headers.
   */
  getetag {
  },

  /**
   * Contains the Last-Modified header returned by a GET method without
   * accept headers.
   */
  getlastmodified {
    /** HTTP date format used to format last modified dates. */
    private final SimpleDateFormat modifiedDateFormat
                  = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", 
                                         Locale.US);
    {
      this.modifiedDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    String makeXML(Object _modifiedDate) {
      StringBuffer ret = new StringBuffer();
      ret.append('<').append(name()).append(">")
         .append(this.modifiedDateFormat.format(_modifiedDate))
         .append("</").append(name()).append(">");
      return ret.toString();
    }
  },

  /**
   * Describes the active locks on a resource.
   */
  lockdiscovery {
  },

  /**
   * Specifies the nature of the resource.
   */
  resourcetype {
  },

  /**
   * The destination of the source link identifies the resource that contains
   * the unprocessed source of the link's source.
   */
  source {
  },

  /**
   * To provide a listing of the lock capabilities supported by the resource.
   */
  supportedlock {
  };


  String makeXML(Object _text) {
    StringBuffer ret = new StringBuffer();
    ret.append('<').append(name()).append(">")
       .append(_text)
       .append("</").append(name()).append(">");
    return ret.toString();
  }
}

