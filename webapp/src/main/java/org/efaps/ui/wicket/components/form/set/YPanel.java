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
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.cell.XYModel;
import org.efaps.ui.wicket.models.cell.XYValue;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.resources.EFapsContentReference;


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

  public static final EFapsContentReference ICON_ADD =
    new EFapsContentReference(YPanel.class, "add.png");

  public static final EFapsContentReference ICON_DELETE =
    new EFapsContentReference(YPanel.class, "delete.png");
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

    if (set.isEditMode()) {
      final AjaxAddNew  addNew = new AjaxAddNew("addNew", _model, view);
      add(addNew);
       final StaticImageComponent image = new StaticImageComponent("add");
       image.setReference(ICON_ADD);
       addNew.add(image);
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

      _item.add(new ValuePanel("valuepanel", super.getDefaultModel(),_item));
    }

    @Override
    public Item<XYValue> newItem(final String id, final int index, final IModel<XYValue> _model)
    {
        return new Item<XYValue>(id, index, _model);
    }


  }

  public class AjaxAddNew extends AjaxLink<UIFormCellSet> {

    private static final long serialVersionUID = 1L;
    private final YRefreshingView view;


    /**
     * @param _view
     * @param repeater
     * @param id
     */
    public AjaxAddNew(final String _id,final IModel<UIFormCellSet> _model, final YRefreshingView _view) {
      super(_id,_model);
      this.view = _view;
    }



    /* (non-Javadoc)
     * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    public void onClick(final AjaxRequestTarget _target) {

      final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();

      final StringBuilder script = new StringBuilder();
     script.append("var div = document.createElement('div');")

     .append("var container = document.getElementById('").append(this.getParent().getMarkupId()).append("');")
     .append("div.innerHTML='");
     final UIForm formmodel = (UIForm) this.getPage().getDefaultModelObject();
     final Map<String, String[]> newmap = formmodel.getNewValues();
     final Integer count = set.getNewCount();
     final String keyName = set.getName() + "eFapsNew";
     if (!newmap.containsKey(keyName)){
       newmap.put(keyName,new String[]{count.toString()});
     } else {
       final String[] oldvalues = newmap.get(keyName);
       final String[] newvalues = new String[oldvalues.length+1];
       for (int i = 0;i<oldvalues.length;i++) {
         newvalues[i] = oldvalues[i];
       }
       newvalues[oldvalues.length] = count.toString();
       newmap.put(keyName,newvalues);
     }


     final Pattern tagpattern = Pattern.compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
     final StringBuilder regex = new StringBuilder().append("(?i)name\\s*=\\s*\"(?-i)").append(set.getName()).append("\"");
     final NumberFormat nf= NumberFormat.getInstance();
     nf.setMinimumIntegerDigits(2);
     nf.setMaximumIntegerDigits(2);

     final AjaxRemoveNew remove = new AjaxRemoveNew(this.view.newChildId(),set.getName(),count.toString());
     this.view.add(remove);

     final StringBuilder bld = new StringBuilder();
     bld.append("<table id=\"").append(remove.getMarkupId()).append("\" ><tr>")
     .append("<td><a onclick=\"").append(remove.getJavaScript()).append("\"")
     .append(" href=\"#\">")
     .append("<img src=\"")
     .append(ICON_DELETE.getImageUrl())
     .append("\"/>")
     .append("</a>")
     .append("</td>");
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
         .append("eFapsNew").append(nf.format(count)).append(nf.format(x)).append("\" ");

         bld.append(tag.replaceAll(regex.toString(), name.toString()));
         start= matcher.end();
       }
       bld.append(value.substring(start ,value.length()));
       bld.append("</td>");
     }
     bld.append("</tr></table>");

     script.append(bld.toString().replace("\"","\\\""));
     script.append("'; ")
       .append("container.insertBefore(div, document.getElementById('").append(this.getMarkupId()).append("'));");
      _target.appendJavascript(script.toString());
      }

  }

  public class AjaxRemoveNew extends WebComponent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String count;

    /**
     * @param _id
     * @param _count
     */
    public AjaxRemoveNew(final String _id, final String _name, final String _count) {
      super(_id);
      this.name = _name;
      this.count = _count;
      this.add(new AjaxOpenModalBehavior());
    }

    public String getJavaScript() {
      return ((AjaxOpenModalBehavior) super.getBehaviors().get(0))
          .getJavaScript();
    }
    /**
     * for the JSCookMenu nothing must be renderd, because JavaScript is used to
     * create the Menu
     *
     * @see org.apache.wicket.markup.html.WebComponent#onRender(org.apache.wicket.markup.MarkupStream)
     */
    @Override
    protected void onRender(final MarkupStream _markupStream) {
      _markupStream.next();
    }

    public class AjaxOpenModalBehavior extends AjaxEventBehavior {

      private static final long serialVersionUID = 1L;

      public AjaxOpenModalBehavior() {
        super("onclick");
      }

      public String getJavaScript() {
        return super.getCallbackScript().toString().replace("'", "\\\'");
      }

      @Override
      protected void onEvent(final AjaxRequestTarget _target) {
        final UIForm formmodel = (UIForm) this.getComponent().getPage().getDefaultModelObject();
        final Map<String, String[]> newmap = formmodel.getNewValues();

        final String keyName = AjaxRemoveNew.this.name + "eFapsNew";

        if (newmap.containsKey(keyName)){
          final String[] oldvalues = newmap.get(keyName);
          final List<String> newvalues = new ArrayList<String>();
          for (final String oldValue: oldvalues) {
            if(!oldValue.equals(AjaxRemoveNew.this.count.toString())){
              newvalues.add(oldValue);
            }
          }
          newmap.put(keyName, newvalues.toArray(new String[newvalues.size()]));
        }



        final StringBuilder script = new StringBuilder();
        script.append("var thisNode = document.getElementById('")
          .append(this.getComponent().getMarkupId()).append("');")
          .append("thisNode.parentNode.removeChild(thisNode);");
        _target.appendJavascript(script.toString());
      }

      @Override
      protected CharSequence getPreconditionScript() {
        return null;
      }
    }
  }
}
