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
 * Revision:        $Rev:  $
 * Last Changed:    $Date:  $
 * Last Changed By: $Author: $
 */

package org.efaps.ui.wicket.components.form.set;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.cell.XYModel;
import org.efaps.ui.wicket.models.cell.XYValue;
import org.efaps.ui.wicket.models.objects.UIForm;


/**
 * TODO comment
 *
 * @author jmox
 * @version $Id: $
 */
public class YPanel extends Panel{

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param id
   * @param _model
   */
  public YPanel(final String id, final IModel<UIFormCellSet> _model) {
    super(id, _model);
    final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
    this.setOutputMarkupId(true);

    final YRefreshingView view = new YRefreshingView("yView", _model);
    add(view);

//    for (int y=0; y < set.getYsize(); y++){
//        final Item item = view.newItem(view.newChildId(), y, _model);
//        view.populateItem(item);
//        view.add(item);
//      }


//    for (int y=0; y < set.getYsize(); y++){
//      final XPanel xpanel = new XPanel(yRepeater.newChildId(), _model,y);
//      yRepeater.add(xpanel);
//    }
//    yRepeater.setOutputMarkupId(true);
//
    if (set.isEditMode()) {
      final AjaxAddNew  addNew = new AjaxAddNew("addNew", _model, view);
      add(addNew);
    } else {
      final Component invisible =
        new WebMarkupContainer("addNew").setVisible(false);
      add(invisible);
    }
  }

  public class YRefreshingView extends RefreshingView<XYValue>{
    private static final long serialVersionUID = 1L;
    /**
     * @param id
     * @param model
     */
    public YRefreshingView(final String id, final IModel<UIFormCellSet> _model) {
      super(id, _model);
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.repeater.RefreshingView#getItemModels()
     */
    @Override
    protected Iterator<IModel<XYValue>> getItemModels() {
      final List<IModel<XYValue>> models = new ArrayList<IModel<XYValue>>();
      final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
      for (int y=0; y < set.getYsize(); y++){
        final XYValue xyvalue = new XYValue(y);
        for(int x=0; x < set.getXsize(); x++){
          xyvalue.addX(x);
        }
        models.add(new XYModel(xyvalue));
      }
      return models.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.repeater.RefreshingView#populateItem(org.apache.wicket.markup.repeater.Item)
     */
    @Override
    protected void populateItem(final Item<XYValue> _item) {
      final XYValue asd = (XYValue) _item.getDefaultModelObject();
      final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
      final Pattern tagpattern = Pattern.compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
      final StringBuilder regex = new StringBuilder().append("(?i)name\\s*=\\s*\"(?-i)").append(set.getName()).append("\"");
      final NumberFormat nf= NumberFormat.getInstance();
      nf.setMinimumIntegerDigits(2);
      nf.setMaximumIntegerDigits(2);


      final StringBuilder bld = new StringBuilder();
      bld.append("<tr>").append("<input type=\"hidden\" value=\"")
      .append(set.getInstance(asd.getY()).getId()).append("\" name=\"hiddenId")
      .append(set.getName()).append(nf.format(asd.getY())).append("\"");
      for (int x = 0; x<asd.getX();x++){
        bld.append("<td>");
        final String value = set.getXYValue(x, asd.getY());
        final Matcher matcher = tagpattern.matcher(value);
        int start = 0;
        while (matcher.find()) {
          value.substring(start , matcher.start());
          bld.append(value.substring(start , matcher.start()));
          final String tag = matcher.group();
          final StringBuilder name = new StringBuilder().append(" name=\"").append(set.getName())
          .append(nf.format(asd.getY())).append(nf.format(x)).append("\" ");
          ;
          bld.append(tag.replaceAll(regex.toString(), name.toString()));
          start= matcher.end();
        }
        bld.append(value.substring(start ,value.length()));
        bld.append("</td>");
      }
      bld.append("</tr>");
      _item.add(new LabelComponent("label", bld.toString()));
    }

    @Override
    public Item<XYValue> newItem(final String id, final int index, final IModel<XYValue> _model)
    {
        return new Item<XYValue>(id, index, _model);
    }

  }

  public class AjaxAddNew extends AjaxLink<UIFormCellSet> {

    private static final long serialVersionUID = 1L;
    private final YRefreshingView repeater;

    /**
     * @param repeater
     * @param id
     */
    public AjaxAddNew(final String _id,final IModel<UIFormCellSet> _model, final YRefreshingView _repeater ) {
      super(_id,_model);
      this.repeater = _repeater;
    }



    /* (non-Javadoc)
     * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    public void onClick(final AjaxRequestTarget _target) {

     // final XPanel xpanel = new XPanel(this.repeater.newChildId(), super.getModel());
      final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();

     final StringBuilder script = new StringBuilder();
     script.append("var div = document.createElement('div');")
     //.append("div.id ='").append(set.getName()).append(set.getNewCount()).append("';")
     .append("var container = document.getElementById('").append(this.getParent().getMarkupId()).append("');")
     .append("div.innerHTML='");
     final UIForm formmodel = (UIForm) this.getPage().getDefaultModelObject();
     final Map<String, String[]> newmap = formmodel.getNewValues();
     final Integer count = set.getNewCount();
     if (!newmap.containsKey(set.getName())){
       newmap.put(set.getName(),new String[]{count.toString()});
     } else {
       final String[] oldvalues = newmap.get(set.getName());
       final String[] newvalues = new String[oldvalues.length+1];
       for (int i = 0;i<oldvalues.length;i++) {
         newvalues[i] = oldvalues[i];
       }
       newvalues[oldvalues.length] = count.toString();
       newmap.put(set.getName(),newvalues);
     }


     final Pattern tagpattern = Pattern.compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
     final StringBuilder regex = new StringBuilder().append("(?i)name\\s*=\\s*\"(?-i)").append(set.getName()).append("\"");
     final NumberFormat nf= NumberFormat.getInstance();
     nf.setMinimumIntegerDigits(2);
     nf.setMaximumIntegerDigits(2);


     final StringBuilder bld = new StringBuilder();
     bld.append("<table><tr>");
     for (int x = 0; x< set.getDefinitionsize();x++){

       bld.append("<td>");
       final String value = set.getDefinitionValue(x);
       final Matcher matcher = tagpattern.matcher(value);
       int start = 0;
       while (matcher.find()) {
         value.substring(start , matcher.start());
         bld.append(value.substring(start , matcher.start()));
         final String tag = matcher.group();
         final StringBuilder name = new StringBuilder().append(" name=\"").append(set.getName())
         .append("New").append(nf.format(count)).append(nf.format(x)).append("\" ");
         ;
         bld.append(tag.replaceAll(regex.toString(), name.toString()));
         start= matcher.end();
       }
       bld.append(value.substring(start ,value.length()));
       bld.append("</td>");
     }
     bld.append("</tr></table>");
     script.append(bld.toString().replace("\"","\\\""));
     script.append("'; ")
     .append("container.appendChild(div);");


      _target.appendJavascript(script.toString());
    }

  }
}
