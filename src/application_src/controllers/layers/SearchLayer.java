/*
 * Bao Lab 2017
 */

package application_src.controllers.layers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

import application_src.application_model.data.LineageData;
import application_src.application_model.data.CElegansData.Connectome.Connectome;
import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.application_model.search.SearchConfiguration.SearchType;
import application_src.application_model.data.CElegansData.Anatomy.AnatomyTerm;
import application_src.application_model.cell_case_logic.CasesLists;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.search.SearchConfiguration.SearchOption;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;
import application_src.application_model.threeD.subscenegeometry.StructureTreeNode;
import application_src.application_model.resources.ProductionInfo;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;

import static javafx.application.Platform.runLater;
import static javafx.scene.paint.Color.DARKSEAGREEN;
import static javafx.scene.paint.Color.web;

import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.getLineageNamesByFunctionalName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.isLineageName;
import static application_src.application_model.search.SearchConfiguration.SearchType.CONNECTOME;
import static application_src.application_model.search.SearchConfiguration.SearchType.DESCRIPTION;
import static application_src.application_model.search.SearchConfiguration.SearchType.FUNCTIONAL;
import static application_src.application_model.search.SearchConfiguration.SearchType.GENE;
import static application_src.application_model.search.SearchConfiguration.SearchType.LINEAGE;
import static application_src.application_model.search.SearchConfiguration.SearchType.MULTICELLULAR_STRUCTURE_CELLS;
import static application_src.application_model.search.SearchConfiguration.SearchType.STRUCTURES_BY_HEADING;
import static application_src.application_model.search.SearchConfiguration.SearchType.STRUCTURE_BY_SCENE_NAME;
import static application_src.application_model.data.CElegansData.SulstonLineage.LineageTree.getCaseSensitiveName;
import static application_src.application_model.data.CElegansData.Anatomy.AnatomyTerm.AMPHID_SENSILLA;
import static application_src.application_model.search.SearchConfiguration.SearchOption.ANCESTOR;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_BODY;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;
import static application_src.application_model.search.SearchConfiguration.SearchOption.DESCENDANT;

public class SearchLayer {

    private final Service<Void> resultsUpdateService;
    private final Service<Void> showLoadingService;

    private final CElegansSearch CElegansSearchPipeline;
    private final AnnotationManager annotationManager;

    private final ObservableList<String> searchResultsList;

    // gui components
    private final TextField searchTextField;

    // the Search Type components
    private final ToggleGroup searchTypeToggleGroup;

    private final CheckBox presynapticCheckBox;
    private final CheckBox postsynapticCheckBox;
    private final CheckBox neuromuscularCheckBox;
    private final CheckBox electricalCheckBox;

    private final CheckBox cellNucleusCheckBox;
    private final CheckBox cellBodyCheckBox;

    private final CheckBox ancestorCheckBox;
    private final CheckBox descendantCheckBox;

    private final ColorPicker colorPicker;
    private final Button addRuleButton;

    /** Tells the subscene controller to rebuild the 3D subscene */
    private final BooleanProperty rebuildSubsceneFlag;

    // queried databases
    private CasesLists casesLists;
    private ProductionInfo productionInfo;
    private WiringService wiringService;
    private TreeItem<StructureTreeNode> structureTreeRoot;

    public SearchLayer(
            final CElegansSearch CElegansSearchPipeline,
            final AnnotationManager annotationManager,
            final ObservableList<String> searchResultsList,
            final TextField searchTextField,
            final RadioButton systematicRadioButton,
            final RadioButton functionalRadioButton,
            final RadioButton descriptionRadioButton,
            final RadioButton geneRadioButton,
            final RadioButton connectomeRadioButton,
            final RadioButton multicellRadioButton,
            final Label descendantLabel,
            final CheckBox presynapticCheckBox,
            final CheckBox postsynapticCheckBox,
            final CheckBox neuromuscularCheckBox,
            final CheckBox electricalCheckBox,
            final CheckBox cellNucleusCheckBox,
            final CheckBox cellBodyCheckBox,
            final CheckBox ancestorCheckBox,
            final CheckBox descendantCheckBox,
            final ColorPicker colorPicker,
            final Button addRuleButton,
            final BooleanProperty geneResultsUpdatedFlag,
            final BooleanProperty rebuildSubsceneFlag) {

        // the API through which all search of C Elegans data is completed
        this.CElegansSearchPipeline = requireNonNull(CElegansSearchPipeline);

        // the internal representation of the annotation rules list in the "Display Options" tab
        this.annotationManager = requireNonNull(annotationManager);

        // the internal representation of the search results that are displayed in the Find Cells tab
        this.searchResultsList = requireNonNull(searchResultsList);

        // text field
        this.searchTextField = requireNonNull(searchTextField);
        this.searchTextField.textProperty().addListener(getTextFieldListener());

        /** The UI components that correspond to {@Link SearchOptions} */
        final ChangeListener<Boolean> connectomeCheckBoxListener = getConnectomeCheckBoxListener();
        this.presynapticCheckBox = requireNonNull(presynapticCheckBox);
        this.presynapticCheckBox.selectedProperty().addListener(connectomeCheckBoxListener);
        this.postsynapticCheckBox = requireNonNull(postsynapticCheckBox);
        this.postsynapticCheckBox.selectedProperty().addListener(connectomeCheckBoxListener);
        this.neuromuscularCheckBox = requireNonNull(neuromuscularCheckBox);
        this.neuromuscularCheckBox.selectedProperty().addListener(connectomeCheckBoxListener);
        this.electricalCheckBox = requireNonNull(electricalCheckBox);
        this.electricalCheckBox.selectedProperty().addListener(connectomeCheckBoxListener);

        final ChangeListener<Boolean> optionsCheckBoxListener = getOptionsCheckBoxListener();
        this.cellNucleusCheckBox = requireNonNull(cellNucleusCheckBox);
        this.cellNucleusCheckBox.selectedProperty().addListener(optionsCheckBoxListener);
        this.cellBodyCheckBox = requireNonNull(cellBodyCheckBox);
        this.cellBodyCheckBox.selectedProperty().addListener(optionsCheckBoxListener);

        this.ancestorCheckBox = requireNonNull(ancestorCheckBox);
        this.ancestorCheckBox.selectedProperty().addListener(optionsCheckBoxListener);
        this.descendantCheckBox = requireNonNull(descendantCheckBox);
        this.descendantCheckBox.selectedProperty().addListener(optionsCheckBoxListener);

        // color
        this.colorPicker = requireNonNull(colorPicker);

        // add rule button
        this.addRuleButton = requireNonNull(addRuleButton);
        this.addRuleButton.setOnAction(getAddButtonClickHandler());

        this.resultsUpdateService = new Service<Void>() {
            @Override
            protected final Task<Void> createTask() {
                final Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        runLater(() -> refreshSearchResultsList(
                                (SearchType) searchTypeToggleGroup.getSelectedToggle().getUserData(),
                                getSearchedText(),
                                cellNucleusCheckBox.isSelected(),
                                cellBodyCheckBox.isSelected(),
                                descendantCheckBox.isSelected(),
                                ancestorCheckBox.isSelected()));
                        return null;
                    }
                };
                return task;
            }
        };

        this.rebuildSubsceneFlag = requireNonNull(rebuildSubsceneFlag);
        showLoadingService = new ShowLoadingService();


        // TODO all of this to be redone
        // the loading service is an open problem
        // the set on succeeded is an operation that should happen with the annotation manager
        // --> after a search is complete, it should be propogated to a holding area in the
        // annotation manager so that it is ready if a user decides to create a rule
        geneSearchService.setOnScheduled(event -> showLoadingService.restart());
        geneSearchService.setOnCancelled(event -> {
            showLoadingService.cancel();
            searchResultsList.clear();
            geneSearchService.resetSearchedGene();
        });

        geneSearchService.setOnSucceeded(event -> {
            showLoadingService.cancel();
            searchResultsList.clear();

            final String searchedGene = geneSearchService.getSearchedGene();
            updateGeneResults(searchedGene);

            // set the cells for gene-based rules if not already set
            final String searchedQuoted = "'" + searchedGene + "'";
            rulesList.stream()
                    .filter(rule -> rule.isGeneRule()
                            && !rule.areCellsSet()
                            && rule.getSearchedText().contains(searchedQuoted))
                    .forEach(rule -> rule.setCells(geneSearchService.getValue()));
        });

        // search type toggle
        searchTypeToggleGroup = new ToggleGroup();
        initSearchTypeToggleGroup(
                requireNonNull(systematicRadioButton),
                requireNonNull(functionalRadioButton),
                requireNonNull(descriptionRadioButton),
                requireNonNull(geneRadioButton),
                requireNonNull(connectomeRadioButton),
                requireNonNull(multicellRadioButton),
                requireNonNull(descendantLabel));
    }

    private void initSearchTypeToggleGroup(
            final RadioButton systematicRadioButton,
            final RadioButton functionalRadioButton,
            final RadioButton descriptionRadioButton,
            final RadioButton geneRadioButton,
            final RadioButton connectomeRadioButton,
            final RadioButton multicellRadioButton,
            final Label descendantLabel) {

        systematicRadioButton.setToggleGroup(searchTypeToggleGroup);
        systematicRadioButton.setUserData(LINEAGE);

        functionalRadioButton.setToggleGroup(searchTypeToggleGroup);
        functionalRadioButton.setUserData(FUNCTIONAL);

        descriptionRadioButton.setToggleGroup(searchTypeToggleGroup);
        descriptionRadioButton.setUserData(DESCRIPTION);

        geneRadioButton.setToggleGroup(searchTypeToggleGroup);
        geneRadioButton.setUserData(GENE);

        connectomeRadioButton.setToggleGroup(searchTypeToggleGroup);
        connectomeRadioButton.setUserData(CONNECTOME);

        multicellRadioButton.setToggleGroup(searchTypeToggleGroup);
        multicellRadioButton.setUserData(MULTICELLULAR_STRUCTURE_CELLS);

        searchTypeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            // if toggle was previously on 'gene' then cancel whatever wormbase search was issued
            if (oldValue != null && oldValue.getUserData() == GENE) {
                searchResultsList.clear();
            }
            final SearchType type = (SearchType) newValue.getUserData();
            // disable descendant options for terminal cell searches
            if (type == FUNCTIONAL || type == DESCRIPTION) {
                descendantCheckBox.setSelected(false);
                descendantCheckBox.disableProperty().set(true);
                descendantLabel.disableProperty().set(true);
            } else {
                descendantCheckBox.disableProperty().set(false);
                descendantLabel.disableProperty().set(false);
            }
            // re-search whatever is in the search field with this new search type
            resultsUpdateService.restart();
        });

        // select lineage search on start
        systematicRadioButton.setSelected(true);
    }

    public void setStructureTreeRoot(final TreeItem<StructureTreeNode> root) {
        structureTreeRoot = requireNonNull(root);
    }


    /**
     * This is the primary method through which the search layer works.
     * 1. Parse all search criteria
     * 2. Dispatch search to appropriate subroutines
     * 3. Pass the results to the CElegansSearchResults class
     * 4. Trigger the EstablishCorrespondence class to run on the update results
     * 5. Alert the AnnotationManager that there are new results to display
     */
     public void performSearchAndAlertAnnotationManager() {

    }


    /**
     * Adds a giant connectome rule that contains all the cell results retrieved based on the input query parameters
     *
     * @param funcName
     *         the functional name of the cell
     * @param color
     *         color to apply to cell entities
     * @param isPresynapticTicked
     *         true if the presynaptic option was ticked, false otherwise
     * @param isPostsynapticTicked
     *         true if the postsynaptic option was ticked, false otherwise
     * @param isElectricalTicked
     *         true if the electrical option was ticked, false otherwise
     * @param isNeuromuscularTicked
     *         true if the neuromuscular option was ticked, false otherwise
     *
     * @return the rule that was added to the internal list
     */
    public Rule addConnectomeColorRuleFromContextMenu(
            final String funcName,
            final Color color,
            final boolean isPresynapticTicked,
            final boolean isPostsynapticTicked,
            final boolean isElectricalTicked,
            final boolean isNeuromuscularTicked) {

        final StringBuilder sb = createLabelForConnectomeRule(
                funcName,
                isPresynapticTicked, isPostsynapticTicked, isElectricalTicked, isNeuromuscularTicked);
        final Rule rule = new Rule(rebuildSubsceneFlag, sb.toString(), color, CONNECTOME, CELL_NUCLEUS);
        rule.setCells(CElegansSearchPipeline.executeConnectomeSearch(
                funcName,
                false,
                false,
                isPresynapticTicked,
                isPostsynapticTicked,
                isElectricalTicked,
                isNeuromuscularTicked,
                OrganismDataType.LINEAGE).getValue());
        rule.setSearchedText(sb.toString());
        rule.resetLabel(sb.toString());
        rulesList.add(rule);
        return rule;
    }


    // TODO -> this is a search method, so we want to move this to CElegansSearch
    private void updateGeneResults(final String searchedGene) {
        // check what kind of gene search this was first. if the results are genes, then we can't do
        // any further work on this search
        final List<String> results = geneSearchService.getPreviouslyFetchedGeneResults(searchedGene);
        if (results == null || results.isEmpty()) {
            return;
        }

        if (descendantCheckBox.isSelected()) {
            getDescendantsList(results, searchedGene)
                    .stream()
                    .filter(name -> !results.contains(name))
                    .forEachOrdered(results::add);
        }
        if (ancestorCheckBox.isSelected()) {
            getAncestorsList(results, searchedGene)
                    .stream()
                    .filter(name -> !results.contains(name))
                    .forEachOrdered(results::add);
        }
        if (!cellNucleusCheckBox.isSelected()) {
            final Iterator<String> iterator = results.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equalsIgnoreCase(searchedGene)) {
                    iterator.remove();
                    break;
                }
            }
        }
        sort(results);
        appendFunctionalToLineageNames(results);
        geneResultsUpdatedFlag.set(true);
    }


    /**
     * This is to populate the SearchResults list view with more useful data
     * @param list
     */
    private void appendFunctionalToLineageNames(final List<String> list) {
        searchResultsList.clear();
        for (String result : list) {
            if (getFunctionalNameByLineageName(result) != null) {
                result += " (" + getFunctionalNameByLineageName(result) + ")";
            }
            searchResultsList.add(result);
        }
    }

    /**
     * Access the text in the search bar
     *
     * @return
     */
    private String getSearchedText() {
        final String searched = searchTextField.getText().toLowerCase();
        return searched;
    }


    // TODO -> move this to structures search class --> no actual searching done in this class, just a controller
    /**
     * Adds a color rule for a collection of multicellular structures under a heading in the structures tree in the
     * Find Structures tab. All the structures under sub-headings are affected by the rule as well. Adding a rule does
     * not rebuild the subscene. In order for any changes to be visible, the calling class must set the
     * 'rebuildSubsceneFlag' to true or set a property that triggers a subscene rebuild.
     *
     * @param heading
     *         the structures heading
     * @param color
     *         the color to apply to all structures under the heading
     *
     * @return the color rule, null if there was no heading
     */
    public Rule addStructureRuleByHeading(final String heading, final Color color) {
        final Rule rule = addColorRule(STRUCTURES_BY_HEADING, heading, color, new ArrayList<>());

        final List<String> structuresToAdd = new ArrayList<>();
        final Queue<TreeItem<StructureTreeNode>> nodeQueue = new LinkedList<>();
        nodeQueue.add(structureTreeRoot);

        // find the node with the desired heading
        TreeItem<StructureTreeNode> headingNode = null;

        TreeItem<StructureTreeNode> treeItem;
        StructureTreeNode node;
        while (!nodeQueue.isEmpty()) {
            treeItem = nodeQueue.remove();
            if (treeItem != null) {
                node = treeItem.getValue();
                if (node.isHeading()) {
                    if (node.getNodeText().equalsIgnoreCase(heading)) {
                        headingNode = treeItem;
                        break;
                    } else {
                        nodeQueue.addAll(treeItem.getChildren());
                    }
                }
            }
        }

        // get all structures under this heading (structures in sub-headings are included as well)
        if (headingNode != null) {
            nodeQueue.clear();
            nodeQueue.add(headingNode);
            while (!nodeQueue.isEmpty()) {
                treeItem = nodeQueue.remove();
                node = treeItem.getValue();
                if (node.isHeading()) {
                    nodeQueue.addAll(treeItem.getChildren());
                } else {
                    structuresToAdd.add(node.getSceneName());
                }
            }
            rule.setCells(structuresToAdd);
        }
        return rule;
    }

    private List<String> getCellsList(final List<String> names) {
        List<String> lineageNames = new ArrayList<String>();

        for (String name : names) {
            if (SearchUtil.isLineageName(name)) { // lineage name already
                lineageNames.add(name);
            } else if (SearchUtil.isMulticellularStructureByName(name)) { // get all the cells associated with structure
                List<String> cells = getCellsInMulticellularStructure(name);
                for (String cell : cells) {
                    lineageNames.add(cell);
                }
            } else { // functional name
                List<String> cells = PartsList.getLineageNamesByFunctionalName(name);
                for (String cell : cells) {
                    lineageNames.add(cell);
                }
            }
        }

        return lineageNames;
    }

    private List<String> getCellsList(final SearchType type, final String searched) {
        List<String> cells = new ArrayList<>();
        if (type != null) {
            switch (type) {
                case LINEAGE:
                    cells = getCellsWithLineageName(searched);
                    break;

                case FUNCTIONAL:
                    cells = getCellsWithFunctionalName(searched);
                    break;

                case DESCRIPTION:
                    cells = getCellsWithFunctionalDescription(searched);
                    break;

                case GENE:
                    switch (geneSearchService.getState()) {
                        case RUNNING:
                            geneSearchService.cancel();
                        case CANCELLED:
                        case SUCCEEDED:
                            geneSearchService.reset();
                            geneSearchService.resetSearchedGene();
                            break;
                    }
                    if (isGeneFormat(searched)) {
                        final List<String> geneCells = geneSearchService.getPreviouslyFetchedGeneResults(searched);
                        if (geneCells != null) {
                            return geneCells;
                        } else {
                            geneSearchService.setSearchedGene(searched);
                            geneSearchService.start();
                        }
                    }
                    break;

                case MULTICELLULAR_STRUCTURE_CELLS:
                    cells = getCellsInMulticellularStructure(searched);
                    break;

                case CONNECTOME:
                    cells = getCellsWithConnectivity(
                            searched,
                            presynapticCheckBox.isSelected(),
                            postsynapticCheckBox.isSelected(),
                            neuromuscularCheckBox.isSelected(),
                            electricalCheckBox.isSelected());
                    break;

                case NEIGHBOR:
                    cells = getNeighboringCells(searched);
            }
        }

        return cells;
    }



    public EventHandler<ActionEvent> getAddButtonClickHandler() {
        return event -> {
            // do not add new ColorRule if search has no matches
            if (searchResultsList.isEmpty()) {
                return;
            }

            final List<SearchOption> options = new ArrayList<>();
            if (cellNucleusCheckBox.isSelected()) {
                options.add(CELL_NUCLEUS);
            }
            if (cellBodyCheckBox.isSelected()) {
                options.add(CELL_BODY);
            }
            if (ancestorCheckBox.isSelected()) {
                options.add(ANCESTOR);
            }
            if (descendantCheckBox.isSelected()) {
                options.add(DESCENDANT);
            }

            addColorRule(
                    (SearchType) searchTypeToggleGroup.getSelectedToggle().getUserData(),
                    getSearchedText(),
                    colorPicker.getValue(),
                    options);

            searchTextField.clear();
        };
    }

    public ChangeListener<Boolean> getOptionsCheckBoxListener() {
        return (observableValue, oldValud, newValue) -> {
            if (searchTypeToggleGroup.getSelectedToggle().getUserData() == GENE) {
                if (!getSearchedText().isEmpty()) {
                    if (CElegansSearchPipeline.isGeneFormat(getSearchedText())) {
                        CElegansSearchPipeline.startGeneSearch(getSearchedText(),
                                false,
                                false,
                                true,
                                false,
                                OrganismDataType.GENE);
                    } else {
                        CElegansSearchPipeline.startGeneSearch(getSearchedText(),
                                ancestorCheckBox.isSelected(),
                                descendantCheckBox.isSelected(),
                                false,
                                false,
                                OrganismDataType.LINEAGE);
                        updateGeneResults(getSearchedText());
                    }
                }
            } else {
                resultsUpdateService.restart();
            }
        };
    }

    private ChangeListener<String> getTextFieldListener() {
        return (observable, oldValue, newValue) -> {
            if (searchTextField.getText().isEmpty()) {
                searchResultsList.clear();
            } else {
                resultsUpdateService.restart();
            }
        };
    }



    private void refreshSearchResultsList(
            final SearchType searchType,
            String searchedTerm,
            final boolean isCellNucleusFetched,
            final boolean isCellBodyFetched,
            final boolean areDescendantsFetched,
            final boolean areAncestorsFetched) {

        if (!searchedTerm.isEmpty()) {
            searchedTerm = searchedTerm.trim().toLowerCase();

            final List<String> cells = getCellsList(searchType, searchedTerm);

            if (cells != null) {
                final String searchedText = getSearchedText();
                final List<String> cellsForListView = new ArrayList<>();
                if (areDescendantsFetched) {
                    getDescendantsList(cells, searchedText)
                            .stream()
                            .filter(name -> !cellsForListView.contains(name))
                            .forEach(cellsForListView::add);
                }
                if (areAncestorsFetched) {
                    getAncestorsList(cells, searchedText)
                            .stream()
                            .filter(name -> !cellsForListView.contains(name))
                            .forEach(cellsForListView::add);
                }
                if (isCellNucleusFetched) {
                    cellsForListView.addAll(cells);
                } else if (isCellBodyFetched) {
                    cellsForListView.addAll(getCellBodiesList(cells));
                }

                sort(cellsForListView);
                appendFunctionalToLineageNames(cellsForListView);
            }
        }
    }

    public boolean hasCellCase(final String cellName) {
        return casesLists != null && casesLists.hasCellCase(cellName);
    }

    public void removeCellCase(final String cellName) {
        if (casesLists != null && cellName != null) {
            casesLists.removeCellCase(cellName);
        }
    }

    public void addToInfoWindow(final AnatomyTerm term) {
        if (term.equals(AMPHID_SENSILLA)) {
            if (!casesLists.containsAnatomyTermCase(term.getTerm())) {
                casesLists.makeAnatomyTermCase(term);
            }
        }
    }

    /**
     * Method taken from RootLayoutController --> how can InfoWindowLinkController generate page without pointer to
     * RootLayoutController?
     */
    public void addToInfoWindow(final String name) {
        if (wiringService == null) {
            wiringService = new WiringService();
        }
        wiringService.setSearchString(name);
        wiringService.restart();
    }

    private ChangeListener<Boolean> getConnectomeCheckBoxListener() {
        return (observable, oldValue, newValue) -> resultsUpdateService.restart();
    }

    public Service<Void> getResultsUpdateService() {
        return resultsUpdateService;
    }

    // TODO -> this just generally needs figuring out. Why is this search in a thread? It's not computationally expensive. Why was it here
    // and not in the connectome class? Probably just get rid of it entirely and route this back through the CElegansSearchPipeline
    private final class WiringService extends Service<Void> {

        private String searchString;

        public String getSearchString() {
            final String searched = searchString;
            return searched;
        }

        public void setSearchString(final String searchString) {
            this.searchString = requireNonNull(searchString);
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    List<String> searchedCells = new ArrayList<>();
                    String searched = getSearchString();
                    // update to lineage name if functional
                    final List<String> lineageNames = getLineageNamesByFunctionalName(searched);
                    if (!lineageNames.isEmpty()) {
                        searchedCells.addAll(lineageNames);
                    } else {
                        searchedCells.add(searched);
                    }

                    for (String searchedCell : searchedCells) {
                        // GENERATE CELL TAB ON CLICK
                        if (searchedCell != null && !searchedCell.isEmpty()) {
                            if (casesLists == null || productionInfo == null) {
                                return null; // error check
                            }
                            if (isLineageName(searchedCell)) {
                                if (casesLists.containsCellCase(searchedCell)) {
                                    // show the tab
                                } else {
                                    // translate the name if necessary
                                    String funcName = CElegansSearchPipeline.checkQueryCell(searchedCell).toUpperCase();
                                    // add a terminal case --> pass the wiring partners
                                    casesLists.makeTerminalCase(
                                            searchedCell,
                                            funcName,
                                            CElegansSearchPipeline.executeConnectomeSearch(
                                                    funcName,
                                                    false,
                                                    false,
                                                    true,
                                                    false,
                                                    false,
                                                    false,
                                                    OrganismDataType.FUNCTIONAL).getValue(),
                                            CElegansSearchPipeline.executeConnectomeSearch(
                                                    funcName,
                                                    false,
                                                    false,
                                                    false,
                                                    true,
                                                    false,
                                                    false,
                                                    OrganismDataType.FUNCTIONAL).getValue(),
                                            CElegansSearchPipeline.executeConnectomeSearch(
                                                    funcName,
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    true,
                                                    false,
                                                    OrganismDataType.FUNCTIONAL).getValue(),
                                            CElegansSearchPipeline.executeConnectomeSearch(
                                                    funcName,
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    true,
                                                    OrganismDataType.FUNCTIONAL).getValue(),
                                            productionInfo.getNuclearInfo(),
                                            productionInfo.getCellShapeData(searchedCell));
                                }
                            } else {
                                // not in connectome --> non terminal case
                                if (casesLists.containsCellCase(searchedCell)) {
                                    // show tab
                                } else {
                                    // add a non terminal case
                                    casesLists.makeNonTerminalCase(
                                            searchedCell,
                                            productionInfo.getNuclearInfo(),
                                            productionInfo.getCellShapeData(searchedCell));
                                }
                            }
                        }
                    }
                    return null;
                }
            };
        }
    }

    /**
     * Service that shows when gene results are being fetched by the {@link GeneSearchManager} so that the user does
     * not think that the application is not responding.
     */
    private final class ShowLoadingService extends Service<Void> {

        /** Time between changes in the number of ellipses periods during loading */
        private static final long WAIT_TIME_MILLIS = 1000;

        /** Maximum number of ellipses periods to show, plus 1 */
        private static final int MODULUS = 5;

        /** Changing number of ellipses periods to display during loading */
        private int count = 0;

        @Override
        protected final Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    while (true) {
                        if (isCancelled()) {
                            break;
                        }
                        runLater(() -> {
                            String loadingString = "Fetching data from WormBase";
                            int num = count % MODULUS;
                            for (int i = 0; i < num; i++) {
                                loadingString += ".";
                            }
                            searchResultsList.clear();
                            searchResultsList.add(loadingString);
                        });
                        try {
                            sleep(WAIT_TIME_MILLIS);
                            count++;
                            count %= MODULUS;
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                    return null;
                }
            };
        }
    }
}