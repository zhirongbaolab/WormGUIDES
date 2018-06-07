/*
 * Bao Lab 2017
 */

package application_src.controllers.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;
import application_src.application_model.threeD.subscenegeometry.StructureTreeNode;

import static java.lang.Double.MAX_VALUE;
import static java.util.Objects.requireNonNull;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.paint.Color.WHITE;

import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.getLineageNamesByFunctionalName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.isLineageName;
import static application_src.application_model.resources.utilities.AppFont.getBolderFont;
import static application_src.application_model.resources.utilities.AppFont.getFont;

public class StructuresLayer {
    private StructuresSearch structuresSearch;
    private final ObservableList<String> searchStructuresResultsList;
    private final TreeView<StructureTreeNode> structuresTreeView;
    private final StringProperty selectedStructureNameProperty;
    private final TextField searchField;
    private Color selectedColor;
    private String searchText;

    public StructuresLayer(
            final StructuresSearch structuresSearch,
            final AnnotationManager annotationManager,
            final SceneElementsList sceneElementsList,
            final StringProperty selectedEntityNameProperty,
            final TextField searchField,
            final ListView<String> structuresSearchResultsListView,
            final TreeView<StructureTreeNode> structuresTreeView,
            final Button addStructureRuleButton,
            final ColorPicker colorPicker,
            final BooleanProperty rebuildSceneFlag) {

        this.structuresSearch = structuresSearch;
        selectedColor = WHITE;

        searchStructuresResultsList = observableArrayList();

        requireNonNull(selectedEntityNameProperty);
        selectedStructureNameProperty = new SimpleStringProperty("");
        selectedStructureNameProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                selectedEntityNameProperty.set(newValue);
            }
        });

        this.searchField = requireNonNull(searchField);
        this.searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchText = newValue.toLowerCase();
            if (searchText.isEmpty()) {
                searchStructuresResultsList.clear();
            } else {
                selectedStructureNameProperty.set("");
                clearStructureTreeNodeSelection();
                searchAndUpdateResults(newValue.toLowerCase());
            }
        });

        requireNonNull(structuresSearchResultsListView).setItems(searchStructuresResultsList);

        this.structuresTreeView = requireNonNull(structuresTreeView);
        this.structuresTreeView.setShowRoot(false);
        this.structuresTreeView.setRoot(sceneElementsList.getTreeRoot());
        this.structuresTreeView.setCellFactory(new StructureTreeCellFactory());
        this.structuresTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        final StructureTreeNode selectedNode = newValue.getValue();
                        if (selectedNode.isLeafNode()) {
                            searchField.clear();
                            selectedStructureNameProperty.set(selectedNode.getSceneName());
                        }
                    }
                });

        requireNonNull(rebuildSceneFlag);
        requireNonNull(addStructureRuleButton).setOnAction(event -> {
            // if a category/structure is highlighted in the tree view, add rule(s) for that
            final TreeItem<StructureTreeNode> selectedItem = structuresTreeView
                    .getSelectionModel()
                    .getSelectedItem();
            if (selectedItem != null) {
                final StructureTreeNode selectedNode = selectedItem.getValue();
                if (selectedNode != null) {
                    if (selectedNode.isLeafNode()) {
                        annotationManager.addStructureRuleBySceneName(selectedNode.getSceneName(), selectedColor);
                    } else {
                        // execute the search
                        List<String> structuresToAdd = structuresSearch.executeStructureSearchUnderHeading(selectedNode.getNodeText());

                        // add the rule to the annotation manager
                        annotationManager.addStructureRuleByHeading(selectedNode.getNodeText(), selectedColor, structuresToAdd);
                    }
                    clearStructureTreeNodeSelection();
                }
            } else {
                // otherwise add rule(s) for all structures in the search results
                for (String string : searchStructuresResultsList) {
                    annotationManager.addStructureRuleBySceneName(string, selectedColor);
                }
                searchField.clear();
            }
            rebuildSceneFlag.set(true);
        });

        requireNonNull(colorPicker).setOnAction(event -> selectedColor = ((ColorPicker) event.getSource()).getValue());
    }

    public TreeItem<StructureTreeNode> getStructuresTreeRoot() {
        return structuresTreeView.getRoot();
    }

    /**
     * Deselects any structure in the tree that was active
     */
    private void clearStructureTreeNodeSelection() {
        structuresTreeView.getSelectionModel().clearSelection();
    }

    public void searchAndUpdateResults(String searchString) {
        searchStructuresResultsList.clear();
        searchStructuresResultsList.addAll(structuresSearch.searchStructures(searchString));
    }

    public StringProperty getSelectedStructureNameProperty() {
        return selectedStructureNameProperty;
    }

    public String getSearchText() {
        return searchText;
    }

    /**
     * Callback for TreeCell<String> so that fonts are uniform
     */
    private class StructureTreeCellFactory
            implements Callback<TreeView<StructureTreeNode>, TreeCell<StructureTreeNode>> {

        @Override
        public TreeCell<StructureTreeNode> call(TreeView<StructureTreeNode> param) {
            return new StructureTreeCell();
        }
    }

    private class StructureTreeCell extends TreeCell<StructureTreeNode> {
        @Override
        protected void updateItem(final StructureTreeNode item, final boolean empty) {
            super.updateItem(item, empty);
            setContentDisplay(GRAPHIC_ONLY);
            setFocusTraversable(false);
            if (item != null && !empty) {
                final StructureCellGraphic graphic = new StructureCellGraphic(item);
                setGraphic(graphic);
            } else {
                setGraphic(null);
            }
        }
    }

    /**
     * Graphical representation of a structure list cell (not including the expansion arrow)
     */
    private class StructureCellGraphic extends HBox {

        private final Label label;

        public StructureCellGraphic(final StructureTreeNode treeNode) {
            super();
            label = new Label(requireNonNull(treeNode).getNodeText());
            if (treeNode.isLeafNode()) {
                label.setFont(getBolderFont());
            } else {
                label.setFont(getFont());
            }
            getChildren().add(label);
            setMaxWidth(MAX_VALUE);
            setPickOnBounds(false);
        }
    }
}
