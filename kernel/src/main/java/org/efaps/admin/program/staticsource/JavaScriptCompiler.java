/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.admin.program.staticsource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.efaps.db.Checkout;
import org.efaps.update.program.JavaScriptUpdate;
import org.efaps.util.EFapsException;

public class JavaScriptCompiler extends AbstractSourceCompiler {

  /**
   * UUID of the CSS type.
   */
  private static final UUID TYPE_JAVASCRIPT =
      UUID.fromString("1c9ce325-7e4f-401f-aeb8-74e2e0c9e224");

  /**
   * UUID of the CompiledCSS type.
   */
  public static final UUID TYPE_COMPILED =
      UUID.fromString("5ed4d346-c82e-4f4e-b52e-a4d5afa0e284");

  private static final UUID TYPE_JAVASCRIPT2JAVASCRIPT =
      UUID.fromString("2d24e861-580c-43ad-a59c-3266021ea190");

  @Override
  public UUID getUUID4Type() {
    return TYPE_JAVASCRIPT;
  }

  @Override
  public UUID getUUID4TypeCompiled() {
    return TYPE_COMPILED;
  }

  @Override
  public UUID getUUID4Type2Type() {
    return TYPE_JAVASCRIPT2JAVASCRIPT;
  }

  @Override
  protected String getCompiledString(final String _oid) {
    String ret = "";
    try {
      final Checkout checkout = new Checkout(_oid);
      // TODO check character encoding!!UTF-8
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(checkout.execute()));

      final StringBuffer buffer = new StringBuffer();

      String thisLine;
      while ((thisLine = in.readLine()) != null) {
        if (!thisLine.contains(JavaScriptUpdate.ANNOTATION_VERSION)
            && !thisLine.contains(JavaScriptUpdate.ANNOTATION_EXTENDS)) {
          buffer.append(thisLine).append("\n");
        }
      }

      int start = 0;
      while ((start = buffer.indexOf("/*")) >= 0) {
        final int end = buffer.indexOf("*/", start + 2);
        if (end >= start + 2)
          buffer.delete(start, end + 2);
      }

      ret = buffer.toString();
      in.close();
      checkout.close();
      ret = stripCommentsAndWhitespace(ret);
      ret += "\n";

    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;

  }

  private static int getPrevCount(String s, int fromIndex, char c) {
    int count = 0;
    --fromIndex;
    while (fromIndex >= 0) {
      if (s.charAt(fromIndex--) == c) {
        ++count;
      } else {
        break;
      }
    }
    return count;
  }

  public enum CurrentState {
    LINE_COMMENT,
    REGULAR_TEXT,
    WHITE_SPACE,
    MULTILINE_COMMENT,
    STRING_SINGLE_QUOTE,
    REG_EXP,
    STRING_DOUBLE_QUOTES;
  }

  public String stripCommentsAndWhitespace(final String _org) {
    // let's be optimistic
    final StringBuffer result = new StringBuffer(_org.length() / 2);
    CurrentState state = CurrentState.REGULAR_TEXT;

    for (int i = 0; i < _org.length(); ++i) {
      char c = _org.charAt(i);
      final char next = (i < _org.length() - 1) ? _org.charAt(i + 1) : 0;
      final char prev = (i > 0) ? _org.charAt(i - 1) : 0;

      if (state == CurrentState.WHITE_SPACE) {

        if (Character.isWhitespace(next) == false) {
          state = CurrentState.REGULAR_TEXT;
        }
        continue;
      }

      if (state == CurrentState.REGULAR_TEXT) {
        if (c == '/' && next == '/' && prev != '\\') {
          state = CurrentState.LINE_COMMENT;
          continue;
        } else if (c == '/' && next == '*') {
          state = CurrentState.MULTILINE_COMMENT;
          ++i;
          continue;
        } else if (c == '/') {
          // This might be a divide operator, or it might be a regular
          // expression.
          // Work out if it's a regular expression by finding the previous
          // non-whitespace
          // char, which
          // will be either '=' or '('. If it's not, it's just a divide
          // operator.
          int idx = i - 1;
          while (idx > 0) {
            final char tmp = _org.charAt(idx);
            if (Character.isWhitespace(tmp)) {
              idx--;
              continue;
            }
            if (tmp == '=' || tmp == '(') {
              state = CurrentState.REG_EXP;
              break;
            }
            break;
          }
        } else if (Character.isWhitespace(c) && Character.isWhitespace(next)) {
          // ignore all whitespace characters after this one
          state = CurrentState.WHITE_SPACE;
          c = '\n';
        } else if (c == '\'') {
          state = CurrentState.STRING_SINGLE_QUOTE;
        } else if (c == '"') {
          state = CurrentState.STRING_DOUBLE_QUOTES;
        }
        result.append(c);
        continue;
      }

      if (state == CurrentState.LINE_COMMENT) {
        if (c == '\n' || c == '\r') {
          state = CurrentState.REGULAR_TEXT;
          continue;
        }
      }

      if (state == CurrentState.MULTILINE_COMMENT) {
        if (c == '*' && next == '/') {
          state = CurrentState.REGULAR_TEXT;
          ++i;
          continue;
        }
      }

      if (state == CurrentState.STRING_SINGLE_QUOTE) {
        // to leave a string expression we need even (or zero) number of
        // backslashes
        final int count = getPrevCount(_org, i, '\\');
        if (c == '\'' && count % 2 == 0) {
          state = CurrentState.REGULAR_TEXT;
        }
        result.append(c);
        continue;
      }

      if (state == CurrentState.STRING_DOUBLE_QUOTES) {
        // to leave a string expression we need even (or zero) number of
        // backslashes
        final int count = getPrevCount(_org, i, '\\');
        if (c == '"' && count % 2 == 0) {
          state = CurrentState.REGULAR_TEXT;
        }
        result.append(c);
        continue;
      }

      if (state == CurrentState.REG_EXP) {
        // to leave regular expression we need even (or zero) number of
        // backslashes
        final int count = getPrevCount(_org, i, '\\');
        if (c == '/' && count % 2 == 0) {
          state = CurrentState.REGULAR_TEXT;
        }
        result.append(c);
        continue;
      }
    }

    return result.toString();
  }

  @Override
  public OneSource getNewOneSource(String _name, String _oid, long _id) {
    return new OneJavaScript(_name, _oid, _id);
  }

  protected class OneJavaScript extends OneSource {

    public OneJavaScript(final String _name, final String _oid, final long _id) {
      super(_name, _oid, _id);
    }

  }
}
