/*
 * Bao Lab 2017
 */

package wormguides.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import partslist.PartsList;
import wormguides.models.colorrule.Rule;
import wormguides.models.subscenegeometry.SceneElementsList;
import wormguides.models.subscenegeometry.StructureTreeNode;

import static java.lang.Double.MAX_VALUE;
import static java.util.Objects.requireNonNull;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.paint.Color.WHITE;

import static partslist.PartsList.getFunctionalNameByLineageName;
import static partslist.PartsList.getLineageNamesByFunctionalName;
import static partslist.PartsList.isLineageName;
import static wormguides.util.AppFont.getBolderFont;
import static wormguides.util.AppFont.getFont;

public class StructuresLayer {

    private final SearchLayer searchLayer;
    private final SceneElementsList sceneElementsList;

    private final ObservableList<String> searchStructuresResultsList;

    private final TreeView<StructureTreeNode> structuresTreeView;

    private final Map<String, List<String>> nameToCellsMap;
    private final Map<String, String> nameToCommentsMap;
    private final Map<String, String> nameToMarkerMap;
    private final Map<String, StructureCellGraphic> structureNameToTreeCellMap;

    private final StringProperty selectedStructureNameProperty;

    private final TextField searchField;

    private Color selectedColor;
    private String searchText;

    public StructuresLayer(
            final SearchLayer searchLayer,
            final SceneElementsList sceneElementsList,
            final StringProperty selectedEntityNameProperty,
            final TextField searchField,
            final ListView<String> structuresSearchResultsListView,
            final TreeView<StructureTreeNode> structuresTreeView,
            final Button addStructureRuleButton,
            final ColorPicker colorPicker,
            final BooleanProperty rebuildSceneFlag) {

        selectedColor = WHITE;

        searchStructuresResultsList = observableArrayList();

        structureNameToTreeCellMap = new HashMap<>();

        requireNonNull(selectedEntityNameProperty);
        selectedStructureNameProperty = new SimpleStringProperty("");
        selectedStructureNameProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                selectedEntityNameProperty.set(newValue);
            }
        });

        this.searchLayer = requireNonNull(searchLayer);

        this.sceneElementsList = requireNonNull(sceneElementsList);
        this.nameToCellsMap = this.sceneElementsList.getNameToCellsMap();
        this.nameToCommentsMap = this.sceneElementsList.getNameToCommentsMap();
        this.nameToMarkerMap = this.sceneElementsList.getNameToMarkerMap();

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
                        addStructureRule(selectedNode.getSceneName(), selectedColor);
                    } else {
                        final Rule headingRule = searchLayer.addStructureRuleByHeading(
                                selectedNode.getNodeText(),
                                selectedColor);
                    }
                    clearStructureTreeNodeSelection();
                }
            } else {
                // otherwise add rule(s) for all structures in the search results
                for (String string : searchStructuresResultsList) {
                    addStructureRule(string, selectedColor);
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

    /**
     * Utilizes the search layer to add a structure color rule by the specified name.
     *
     * @param name
     *         the name of the structure
     * @param color
     *         the color
     */
    public void addStructureRule(String name, final Color color) {
        if (name == null || color == null) {
            return;
        }
        name = name.trim();

        // the scene name may be a functional name - use the lineage name so the rule can be recognized by the cell
        // bodies (whose scene names are the lineage names)
        if (isLineageName(name)) {
            name = getFunctionalNameByLineageName(name);
        }
        searchLayer.addStructureRuleBySceneName(name, color);
    }

    /**
     * Searches for scene elements (single-celled and multicellular) whose scene name or comment is specified by the
     * searched term. The search results list is updated with those structure scene names.
     *
     * @param searched
     *         the searched term
     */
    public void searchAndUpdateResults(String searched) {
        if (searched == null || searched.isEmpty()) {
            return;
        }

        final String[] terms = searched.toLowerCase().split(" ");
        searchStructuresResultsList.clear();

        String nameLower;
        for (String name : sceneElementsList.getAllSceneNames()) {
            if (!searchStructuresResultsList.contains(name)) {
                nameLower = name.toLowerCase();

                boolean appliesToName = false;
                boolean appliesToCell = false;
                boolean appliesToMarker = false;
                boolean appliesToComment = false;

                // search in structure scene names
                for (String term : terms) {
                    if (nameLower.contains(term)) {
                        appliesToName = true;
                        break;
                    }
                }

                // search in cells
                final List<String> cells = nameToCellsMap.get(nameLower);
                if (cells != null) {
                    for (String cell : cells) {
                        // use the first term
                        if (terms.length > 0) {
                            // check if search term is a functional name
                            final List<String> lineageNames = new ArrayList<>(
                                    getLineageNamesByFunctionalName(terms[0]));
                            for (String lineageName : lineageNames) {
                                if (lineageName != null) {
                                    if (cell.toLowerCase().startsWith(lineageName.toLowerCase())) {
                                        appliesToCell = true;
                                        break;
                                    }
                                }
                            }
                            if (cell.toLowerCase().startsWith(terms[0].toLowerCase())) {
                                appliesToCell = true;
                                break;
                            }
                        }
                    }
                }

                if (nameToMarkerMap.get(nameLower).startsWith(terms[0].toLowerCase())) {
                    appliesToMarker = true;
                }

                // search in comments if name does not already apply
                if (nameToCommentsMap.containsKey(nameLower)) {
                    final String commentLowerCase = nameToCommentsMap.get(nameLower).toLowerCase();
                    for (String term : terms) {
                        if (commentLowerCase.contains(term)) {
                            appliesToComment = true;
                        } else {
                            appliesToComment = false;
                            break;
                        }
                    }
                }

                if (appliesToName || appliesToCell || appliesToMarker || appliesToComment) {
                    // use functional name in structure search results instead of lineage names
                    String functionalName = PartsList.getFunctionalNameByLineageName(name);
                    if (functionalName == null) {
                        functionalName = name;
                    }
                    searchStructuresResultsList.add(functionalName);
                }
            }
        }
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
