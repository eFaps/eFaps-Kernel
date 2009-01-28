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

package org.efaps.ui.wicket.components.form.set;

import java.text.NumberFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.cell.XYValue;
import org.efaps.ui.wicket.models.objects.UIForm;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public class ValuePanel extends Panel {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param id
   * @param model
   */
  public ValuePanel(final String _wicketId, final IModel<?> model,
                    final Item<XYValue> _item) {
    super(_wicketId, model);
    final XYValue asd = (XYValue) _item.getDefaultModelObject();
    final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
    final Pattern tagpattern = Pattern
        .compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
    final StringBuilder regex = new StringBuilder()
      .append("(?i)name\\s*=\\s*\"(?-i)").append(set.getName()).append("\"");
    final NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(2);
    nf.setMaximumIntegerDigits(2);

    long valueid = 0;

    final StringBuilder bld = new StringBuilder();
    if (set.isEditMode()) {
      valueid = set.getInstance(asd.getY()).getId();
      bld.append("<input type=\"hidden\" value=\"")
        .append(valueid).append("\" name=\"hiddenId")
        .append(set.getName()).append(nf.format(asd.getY())).append("\"/>");
    }
    for (int x = 0; x < asd.getX(); x++) {
      bld.append("<td>");
      final String value = set.getXYValue(x, asd.getY());
      final Matcher matcher = tagpattern.matcher(value);
      int start = 0;
      while (matcher.find()) {
        value.substring(start , matcher.start());
        bld.append(value.substring(start , matcher.start()));
        final String tag = matcher.group();
        final StringBuilder name = new StringBuilder()
          .append(" name=\"").append(set.getName())
          .append(nf.format(asd.getY())).append(nf.format(x)).append("\" ");

        bld.append(tag.replaceAll(regex.toString(), name.toString()));
        start = matcher.end();
      }
      bld.append(value.substring(start, value.length()));
      bld.append("</td>");
    }
    final WebMarkupContainer table = new WebMarkupContainer("eFpasSetValueTable");
    add(table);
    table.setOutputMarkupId(true);

    table.add(new LabelComponent("label", bld.toString()));
    if (set.isEditMode()) {
      final WebMarkupContainer td = new WebMarkupContainer("removeTD");
      table.add(td);
      final AjaxRemove remove = new AjaxRemove("remove",
                                               valueid,
                                               set.getName());
      td.add(remove);
      final StaticImageComponent image = new StaticImageComponent("removeIcon");
      image.setReference(YPanel.ICON_DELETE);
      remove.add(image);
    } else {
      table.add(new WebMarkupContainer("removeTD").setVisible(false));
    }
  }

  public class AjaxRemove extends AjaxLink<UIFormCellSet> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Long valueID;
    private final String name;

    /**
     * @param id
     * @param model
     */
    public AjaxRemove(final String id, final long _valueid,final String _name) {
      super(id);
      this.valueID = _valueid;
      this.name = _name;
   }


    @Override
    public void onClick(final AjaxRequestTarget _target) {
      final UIForm formmodel = (UIForm) this.getPage().getDefaultModelObject();

      final Map<String, String[]> newmap = formmodel.getNewValues();
      final String keyString = this.name + "eFapsRemove";
      if (!newmap.containsKey(keyString)){
        newmap.put(keyString, new String[]{this.valueID.toString()});
      } else {
        final String[] oldvalues = newmap.get(keyString);
        final String[] newvalues = new String[oldvalues.length+1];
        for (int i = 0;i<oldvalues.length;i++) {
          newvalues[i] = oldvalues[i];
        }
        newvalues[oldvalues.length] = this.valueID.toString();
        newmap.put(keyString,newvalues);
      }
      this.getParent().getParent().setVisible(false);
      _target.addComponent(this.getParent().getParent());
    }
  }
}
