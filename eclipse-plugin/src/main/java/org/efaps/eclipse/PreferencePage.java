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

package org.efaps.eclipse;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author tmo
 * @version $Id$
 */
public class PreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

  public PreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
    this.setPreferenceStore(org.efaps.eclipse.EfapsPlugin.getDefault().getPreferenceStore());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
   */
  @Override
  protected void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final StringFieldEditor nameField = new StringFieldEditor(
        "name",
        "eFaps Login Name",
        parent);
    addField(nameField);

    final StringFieldEditor passwdField = new StringFieldEditor(
        "password",
        "Password",
        parent);
    addField(passwdField);

    final ComboFieldEditor dbType = new ComboFieldEditor(
          "dbtype",
          "Database Type",
          new String[][] {{"Derby", "org.efaps.db.databases.DerbyDatabase"},
          {"PostgreSQL", "org.efaps.db.databases.PostgreSQLDatabase"}},
          parent);
//    dbType.setPage(this);
//    dbType.setPreferenceStore(getPreferenceStore());
//    dbType.load();
    addField(dbType);

    final StringFieldEditor factoryField = new StringFieldEditor(
        "dbfactory",
        "Factory",
        parent);
    addField(factoryField);

    final MyTextEditor test = new MyTextEditor(getFieldEditorParent());
//  test.setPage(this);
//  test.setPreferenceStore(getPreferenceStore());
//  test.load();
    addField(test);


    //MyEditor test = new MyEditor(getFieldEditorParent());
    //addField(test);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(final IWorkbench _workbench) {
    // TODO Auto-generated method stub

  }


  class MyTextEditor extends StringFieldEditor {

    MyTextEditor(final Composite _parent)  {
      super("dbproperties", "Database Properties", _parent);
    }

    /**
     * Fills this field editor's basic controls into the given parent.
     * <p>
     * The string field implementation of this <code>FieldEditor</code>
     * framework method contributes the text field. Subclasses may override
     * but must call <code>super.doFillIntoGrid</code>.
     * </p>
     */
    @Override
    protected void doFillIntoGrid(final Composite parent, final int numColumns) {
      super.doFillIntoGrid(parent, numColumns);

      final Text textField = getTextControl();

      final GridData gd = new GridData();
      gd.horizontalSpan = numColumns - 1;
      gd.horizontalAlignment = GridData.FILL;
      gd.grabExcessHorizontalSpace = true;
      gd.verticalSpan = 15;
      gd.verticalAlignment = GridData.FILL;
      gd.grabExcessVerticalSpace = true;
      textField.setLayoutData(gd);

    }

    /**
     * Returns this field editor's text control.
     * <p>
     * The control is created if it does not yet exist
     * </p>
     *
     * @param parent the parent
     * @return the text control
     */
    @Override
    public Text getTextControl(final Composite parent) {
      Text textField = getTextControl();

      if (textField == null) {
        textField = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        textField.setFont(parent.getFont());
        textField.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(final KeyEvent e) {
            clearErrorMessage();
          }
        });
        textField.addFocusListener(new FocusAdapter() {
          @Override
          public void focusGained(final FocusEvent e) {
            refreshValidState();
          }

          @Override
          public void focusLost(final FocusEvent e) {
            valueChanged();
            clearErrorMessage();
          }
        });
        textField.addDisposeListener(new DisposeListener() {
          public void widgetDisposed(final DisposeEvent event) {
//            textField = null;
          }
        });
      }
      return textField;
    }
  }
}
