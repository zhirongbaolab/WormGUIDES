/*
 * Bao Lab 2017
 */

package application_src.controllers.layers;

import java.util.*;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import application_src.application_model.search.CElegansSearch.CElegansSearchResults;
import application_src.application_model.search.ModelSearch.EstablishCorrespondence;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.NeighborsSearch;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
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
import javafx.scene.paint.Color;
import application_src.application_model.search.SearchConfiguration.SearchType;
import application_src.application_model.data.CElegansData.Anatomy.AnatomyTerm;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.search.SearchConfiguration.SearchOption;
import static java.lang.Thread.sleep;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static javafx.application.Platform.runLater;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.getLineageNamesByFunctionalName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.isLineageName;
import static application_src.application_model.search.SearchConfiguration.SearchType.CONNECTOME;
import static application_src.application_model.search.SearchConfiguration.SearchType.DESCRIPTION;
import static application_src.application_model.search.SearchConfiguration.SearchType.FUNCTIONAL;
import static application_src.application_model.search.SearchConfiguration.SearchType.GENE;
import static application_src.application_model.search.SearchConfiguration.SearchType.LINEAGE;
import static application_src.application_model.search.SearchConfiguration.SearchType.MULTICELLULAR_STRUCTURE_CELLS;
import static application_src.application_model.data.CElegansData.Anatomy.AnatomyTerm.AMPHID_SENSILLA;
import static application_src.application_model.search.SearchConfiguration.SearchOption.ANCESTOR;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_BODY;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;
import static application_src.application_model.search.SearchConfiguration.SearchOption.DESCENDANT;

public class SearchLayer {

    private final Service<Void> resultsUpdateService;
    private final Service<Void> showLoadingService;

    private final CElegansSearch cElegansSearchPipeline;
    private final NeighborsSearch neighborsSearch;
    private final StructuresSearch structuresSearch;
    private final EstablishCorrespondence establishCorrespondence;
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


    public SearchLayer(
            final CElegansSearch CElegansSearchPipeline,
            final NeighborsSearch neighborsSearch,
            final StructuresSearch structuresSearch,
            final EstablishCorrespondence establishCorrespondence,
            final AnnotationManager annotationManager,
            final ObservableList<String> searchResultsList,
            final TextField searchTextField,
            final RadioButton lineageRadioButton,
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
            final Button addRuleButton) {


        //////// SEARCH PIPELINES, MODULES AND MANUALS ///////////////////
        // the API through which all search of C Elegans data is completed
        this.cElegansSearchPipeline = requireNonNull(CElegansSearchPipeline);

        // the two model specific search modules
        this.neighborsSearch = neighborsSearch;
        this.structuresSearch = structuresSearch;

        // the module for establishing correspondence between C elegans data and the model data
        this.establishCorrespondence = establishCorrespondence;

        // the internal representation of the annotation rules list in the "Display Options" tab
        this.annotationManager = requireNonNull(annotationManager);
        //////////////////////////////////////////////////////////////////////


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

        // the service that indicates that results are loading in the search results area
        // although this is activated on all searches, in reality, it will only appear
        // when genes are searched because other searches are local and complete quickly.
        // Therefore, there is a check in the service that determines if this is a gene search
        // and only then will it display the loading text

        showLoadingService = new ShowLoadingService();

        this.resultsUpdateService = new Service<Void>() {
            @Override
            protected final Task<Void> createTask() {
                final Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        runLater(() -> performSearchAndAlertAnnotationManager(
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
        this.resultsUpdateService.setOnScheduled(event -> showLoadingService.restart());
        this.resultsUpdateService.setOnSucceeded(event -> showLoadingService.cancel());

        // search type toggle
        searchTypeToggleGroup = new ToggleGroup();
        initSearchTypeToggleGroup(
                requireNonNull(lineageRadioButton),
                requireNonNull(functionalRadioButton),
                requireNonNull(descriptionRadioButton),
                requireNonNull(geneRadioButton),
                requireNonNull(connectomeRadioButton),
                requireNonNull(multicellRadioButton),
                requireNonNull(descendantLabel));
    }

    /**
     *
     * @param lineageRadioButton
     * @param functionalRadioButton
     * @param descriptionRadioButton
     * @param geneRadioButton
     * @param connectomeRadioButton
     * @param multicellRadioButton
     * @param descendantLabel
     */
    private void initSearchTypeToggleGroup(
            final RadioButton lineageRadioButton,
            final RadioButton functionalRadioButton,
            final RadioButton descriptionRadioButton,
            final RadioButton geneRadioButton,
            final RadioButton connectomeRadioButton,
            final RadioButton multicellRadioButton,
            final Label descendantLabel) {

        lineageRadioButton.setToggleGroup(searchTypeToggleGroup);
        lineageRadioButton.setUserData(LINEAGE);

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


        // listener for change in the search toggles
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
        lineageRadioButton.setSelected(true);
    }




    //////////////////////////// MAIN SEARCH CONTROLLER METHODS ////////////////////////////////
    /**
     ** This is the primary method through which the search layer works.
     * 1. Parse all search criteria
     * 2. Dispatch search to appropriate subroutines
     * 3. Pass the results to the CElegansSearchResults class
     * 4. Trigger the EstablishCorrespondence class to run on the update results
     * 5. Alert the AnnotationManager that there are new results to display
     *
     * @param searchType
     * @param searchedTerm
     * @param isCellNucleusFetched
     * @param isCellBodyFetched
     * @param areDescendantsFetched
     * @param areAncestorsFetched
     */
    private void performSearchAndAlertAnnotationManager(
            final SearchType searchType,
            String searchedTerm,
            final boolean isCellNucleusFetched,
            final boolean isCellBodyFetched,
            final boolean areDescendantsFetched,
            final boolean areAncestorsFetched) {

        if (!searchedTerm.isEmpty()) {
            searchedTerm = searchedTerm.trim().toLowerCase();

            // depending on the search type, either:
            // 1. run the search on the C Elegans data through the CElegansSearchPipelin
            //      and populate these results to the CElegansSearchResultsClass
            CElegansSearchResults cElegansDataSearchResults = new CElegansSearchResults(new AbstractMap.SimpleEntry<OrganismDataType, List<String>>(null, new ArrayList<>()));
            List<String> modelDataSearchResults = new ArrayList<>();
            switch (searchType) {
                case LINEAGE: // C Elegans data
                    cElegansDataSearchResults = new CElegansSearchResults(
                            cElegansSearchPipeline.executeLineageSearch(searchedTerm,
                                    areAncestorsFetched,
                                    areDescendantsFetched));
                    break;
                case FUNCTIONAL: // C Elegans data
                    cElegansDataSearchResults = new CElegansSearchResults(
                            cElegansSearchPipeline.executeFunctionalSearch(searchedTerm,
                                    areAncestorsFetched,
                                    areDescendantsFetched,
                                    OrganismDataType.LINEAGE));
                    break;
                case DESCRIPTION: // C Elegans data
                    cElegansDataSearchResults = new CElegansSearchResults(
                            cElegansSearchPipeline.executeDescriptionSearch(searchedTerm,
                                    areAncestorsFetched,
                                    areDescendantsFetched,
                                    OrganismDataType.LINEAGE));
                    break;
                case GENE: // C Elegans data
                    if (CElegansSearch.isGeneFormat(searchedTerm)) {
                        cElegansDataSearchResults = new CElegansSearchResults(
                                cElegansSearchPipeline.executeGeneSearch(searchedTerm,
                                        areAncestorsFetched,
                                        areDescendantsFetched,
                                        true,
                                        false,
                                        OrganismDataType.LINEAGE));
                    } else {
                        // before issuing gene search, make sure the search is either a valid
                        // lineage name or a valid functional name so the thread doesn't run unnecessarily
                        if (CElegansSearch.isValidLineageSearchTerm(searchedTerm)
                                || CElegansSearch.isValidFunctionalSearchTerm(searchedTerm)) {
                            cElegansDataSearchResults = new CElegansSearchResults(
                                    cElegansSearchPipeline.executeGeneSearch(searchedTerm,
                                            areAncestorsFetched,
                                            areDescendantsFetched,
                                            false,
                                            true,
                                            OrganismDataType.GENE));
                        }
                    }
                    break;
                case CONNECTOME: // C Elegans data
                    cElegansDataSearchResults = new CElegansSearchResults(
                            cElegansSearchPipeline.executeConnectomeSearch(searchedTerm,
                                    areAncestorsFetched,
                                    areDescendantsFetched,
                                    presynapticCheckBox.isSelected(),
                                    postsynapticCheckBox.isSelected(),
                                    neuromuscularCheckBox.isSelected(),
                                    electricalCheckBox.isSelected(),
                                    OrganismDataType.LINEAGE));
                    break;
                case MULTICELLULAR_STRUCTURE_CELLS: // model specific data
                    break;
                case NEIGHBOR: // model specific data
                    modelDataSearchResults = neighborsSearch.getNeighboringCells(searchedTerm);
                    break;
            }

            final List<String> entitiesForAnnotation = new ArrayList<>();

            if (cElegansDataSearchResults.hasResults()) {
                // find the correspondence between the C elegans search results
                entitiesForAnnotation.addAll(establishCorrespondence.establishCorrespondence(cElegansDataSearchResults,
                                                                                                isCellNucleusFetched,
                                                                                                isCellBodyFetched));
            }

            if (!modelDataSearchResults.isEmpty()) {
                // this comes directly from the model and should be formatted correctly,
                // so there is no need to pass it through the correspondence pipeline.
                // Simply add it to the cellsForListView
                entitiesForAnnotation.addAll(modelDataSearchResults);
            }

            sort(entitiesForAnnotation);

            // pass the results to the annotation manager so that
            annotationManager.updateAnnotation(entitiesForAnnotation);

            // this appends functional names to the lineage names (unless they are gene names),
            // and then places in the ObservableList<String> searchResultsListView -> this triggers
            // RootLayoutController to populate the results window with them
            appendFunctionalToLineageNames(entitiesForAnnotation);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////// UTILITY METHODS /////////////////////////////////////////
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
    /////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Adds a connectome rule that contains all the cell results retrieved based on the input query parameters
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
    public Rule addConnectomeColorRule(
            final String funcName,
            final Color color,
            final boolean isPresynapticTicked,
            final boolean isPostsynapticTicked,
            final boolean isElectricalTicked,
            final boolean isNeuromuscularTicked) {

        List<String> searchResults = cElegansSearchPipeline.executeConnectomeSearch(
                funcName,
                false,
                false,
                isPresynapticTicked,
                isPostsynapticTicked,
                isElectricalTicked,
                isNeuromuscularTicked,
                OrganismDataType.LINEAGE).getValue();

        return annotationManager.addConnectomeColorRule(funcName, color, searchResults,
                isPresynapticTicked,
                isPostsynapticTicked,
                isElectricalTicked,
                isNeuromuscularTicked,
                new ArrayList<>());
    }


    //////////////////////////////////// UI COMPONENT HANDLERS AND LISTENERS /////////////////////////////////
    /**
     *
     * @return
     */
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

            annotationManager.addColorRule(
                    (SearchType) searchTypeToggleGroup.getSelectedToggle().getUserData(),
                    getSearchedText(),
                    colorPicker.getValue(),
                    searchResultsList,
                    options);

            searchTextField.clear();
        };
    }

    /**
     *
     * @return
     */
    public ChangeListener<Boolean> getOptionsCheckBoxListener() {
        return (observableValue, oldValue, newValue) -> {
            if (!searchTextField.getText().isEmpty()) {
                searchResultsList.clear();
                resultsUpdateService.restart();
            }

//            if (searchTypeToggleGroup.getSelectedToggle().getUserData() == GENE) {
//                if (!getSearchedText().isEmpty()) {
//                    if (CElegansSearchPipeline.isGeneFormat(getSearchedText())) {
//                        CElegansSearchPipeline.startGeneSearch(getSearchedText(),
//                                false,
//                                false,
//                                true,
//                                false,
//                                OrganismDataType.GENE);
//                    } else {
//                        CElegansSearchPipeline.startGeneSearch(getSearchedText(),
//                                ancestorCheckBox.isSelected(),
//                                descendantCheckBox.isSelected(),
//                                false,
//                                false,
//                                OrganismDataType.LINEAGE);
//                        updateGeneResults(getSearchedText());
//                    }
//                }
//            } else {
//                resultsUpdateService.restart();
//            }
        };
    }

    /**
     *
     * @return
     */
    private ChangeListener<String> getTextFieldListener() {
        return (observable, oldValue, newValue) -> {
            if (searchTextField.getText().isEmpty()) {
                searchResultsList.clear();
            } else {
                resultsUpdateService.restart();
            }
        };
    }

    /**
     *
     * @return
     */
    private ChangeListener<Boolean> getConnectomeCheckBoxListener() {
        return (observable, oldValue, newValue) -> {
            if (!searchTextField.getText().isEmpty()) {
                searchResultsList.clear();
                resultsUpdateService.restart();
            }
        };
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////

    /**
     * Method taken from RootLayoutController --> how can InfoWindowLinkController generate page without pointer to
     * RootLayoutController?
     */




    public Service<Void> getResultsUpdateService() {
        return resultsUpdateService;
    }

    // TODO -> this just generally needs figuring out. Why is this search in a thread? It's not computationally expensive. Why was it here



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
                    if (searchTypeToggleGroup.getSelectedToggle().getUserData() == GENE) {
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
                    return null;
                }
            };
        }
    }
}