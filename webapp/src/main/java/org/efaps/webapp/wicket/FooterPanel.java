package org.efaps.webapp.wicket;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;

import org.efaps.webapp.models.IModelAbstract;

public class FooterPanel extends Panel {

  private static final long serialVersionUID = -1722339596237748160L;

  public FooterPanel(String _id, IModel _model) {
    super(_id, _model);
    IModelAbstract model= (IModelAbstract) super.getModel();
    String label = null;
    if(model.isCreateMode()){
     label = "Create";
    }else if(model.isEditMode()){
      label = "Update";
    }else if (model.isSearchMode()){
      label = "Search";
    }
    
    final Link CreateEditSearchLink = new Link("CreateEditSearch") {
      private static final long serialVersionUID = 1L;

      public void onClick() {

      }
    };
    CreateEditSearchLink.add(new Image("eFapsButtonDone"));
    CreateEditSearchLink.add(new Label("eFapsButtonDoneLabel", label));
    add(CreateEditSearchLink);
    
    
    
    final Link CancelLink = new Link("Cancel") {
      private static final long serialVersionUID = 1L;

      public void onClick() {

      }
    };
    CancelLink.add(new Image("eFapsButtonCancel"));
    CancelLink.add(new Label("eFapsButtonCancelLabel", "Cancel"));
    add(CancelLink);

  }

}
