package com.company.deptest.web.newentity;

import com.company.deptest.entity.NewEntity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class NewEntityBrowse extends AbstractLookup {

    /**
     * The {@link CollectionDatasource} instance that loads a list of {@link NewEntity} records
     * to be displayed in {@link NewEntityBrowse#newEntitiesTable} on the left
     */
    @Inject
    private CollectionDatasource<NewEntity, UUID> newEntitiesDs;

    /**
     * The {@link Datasource} instance that contains an instance of the selected entity
     * in {@link NewEntityBrowse#newEntitiesDs}
     * <p/> Containing instance is loaded in {@link CollectionDatasource#addItemChangeListener}
     * with the view, specified in the XML screen descriptor.
     * The listener is set in the {@link NewEntityBrowse#init(Map)} method
     */
    @Inject
    private Datasource<NewEntity> newEntityDs;

    /**
     * The {@link Table} instance, containing a list of {@link NewEntity} records,
     * loaded via {@link NewEntityBrowse#newEntitiesDs}
     */
    @Inject
    private Table<NewEntity> newEntitiesTable;

    /**
     * The {@link BoxLayout} instance that contains components on the left side
     * of {@link SplitPanel}
     */
    @Inject
    private BoxLayout lookupBox;

    /**
     * The {@link BoxLayout} instance that contains buttons to invoke Save or Cancel actions in edit mode
     */
    @Inject
    private BoxLayout actionsPane;

    /**
     * The {@link FieldGroup} instance that is linked to {@link NewEntityBrowse#newEntityDs}
     * and shows fields of the selected {@link NewEntity} record
     */
    @Inject
    private FieldGroup fieldGroup;

    /**
     * The {@link RemoveAction} instance, related to {@link NewEntityBrowse#newEntitiesTable}
     */
    @Named("newEntitiesTable.remove")
    private RemoveAction newEntitiesTableRemove;

    @Inject
    private DataSupplier dataSupplier;

    /**
     * {@link Boolean} value, indicating if a new instance of {@link NewEntity} is being created
     */
    private boolean creating;

    @Override
    public void init(Map<String, Object> params) {

        /**
         * Adding {@link com.haulmont.cuba.gui.data.Datasource.ItemChangeListener} to {@link newEntitiesDs}
         * The listener reloads the selected record with the specified view and sets it to {@link newEntityDs}
         */
        newEntitiesDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                NewEntity reloadedItem = dataSupplier.reload(e.getDs().getItem(), newEntityDs.getView());
                newEntityDs.setItem(reloadedItem);
            }
        });

        /**
         * Adding {@link CreateAction} to {@link newEntitiesTable}
         * The listener removes selection in {@link newEntitiesTable}, sets a newly created item to {@link newEntityDs}
         * and enables controls for record editing
         */
        newEntitiesTable.addAction(new CreateAction(newEntitiesTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity newItem, Datasource parentDs, Map<String, Object> params) {
                newEntitiesTable.setSelected(Collections.emptyList());
                newEntityDs.setItem((NewEntity) newItem);
                enableEditControls(true);
            }
        });

        /**
         * Adding {@link EditAction} to {@link newEntitiesTable}
         * The listener enables controls for record editing
         */
        newEntitiesTable.addAction(new EditAction(newEntitiesTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                if (newEntitiesTable.getSelected().size() == 1) {
                    enableEditControls(false);
                }
            }
        });

        /**
         * Setting {@link RemoveAction#afterRemoveHandler} for {@link newEntitiesTableRemove}
         * to reset record, contained in {@link newEntityDs}
         */
        newEntitiesTableRemove.setAfterRemoveHandler(removedItems -> newEntityDs.setItem(null));

        disableEditControls();
    }

    /**
     * Method that is invoked by clicking Save button after editing an existing or creating a new record
     */
    public void save() {
        getDsContext().commit();

        NewEntity editedItem = newEntityDs.getItem();
        if (creating) {
            newEntitiesDs.includeItem(editedItem);
        } else {
            newEntitiesDs.updateItem(editedItem);
        }
        newEntitiesTable.setSelected(editedItem);

        disableEditControls();
    }

    /**
     * Method that is invoked by clicking Save button after editing an existing or creating a new record
     */
    public void cancel() {
        NewEntity selectedItem = newEntitiesDs.getItem();
        if (selectedItem != null) {
            NewEntity reloadedItem = dataSupplier.reload(selectedItem, newEntityDs.getView());
            newEntitiesDs.setItem(reloadedItem);
        } else {
            newEntityDs.setItem(null);
        }

        disableEditControls();
    }

    /**
     * Enabling controls for record editing
     * @param creating indicates if a new instance of {@link NewEntity} is being created
     */
    private void enableEditControls(boolean creating) {
        this.creating = creating;
        initEditComponents(true);
        fieldGroup.requestFocus();
    }

    /**
     * Disabling editing controls
     */
    private void disableEditControls() {
        initEditComponents(false);
        newEntitiesTable.requestFocus();
    }

    /**
     * Initiating edit controls, depending on if they should be enabled/disabled
     * @param enabled if true - enables editing controls and disables controls on the left side of the splitter
     *                if false - visa versa
     */
    private void initEditComponents(boolean enabled) {
        fieldGroup.setEditable(enabled);
        actionsPane.setVisible(enabled);
        lookupBox.setEnabled(!enabled);
    }
}