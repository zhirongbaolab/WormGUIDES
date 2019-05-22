/*
 * Bao Lab 2017
 */

package application_src.controllers.layers;

import java.util.*;
import java.util.concurrent.TimeUnit;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import application_src.application_model.search.CElegansSearch.CElegansSearchResults;
import application_src.application_model.search.ModelSearch.EstablishCorrespondence;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.ModelSpecificSearchUtil;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.NeighborsSearch;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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
import static javafx.concurrent.Worker.State.RUNNING;
import static javafx.concurrent.Worker.State.SCHEDULED;

public class SearchLayer {

    private final Service<Void> resultsUpdateService;
    private final GeneSearchManager geneSearchManager;
    private final Service<Void> showLoadingService;

    private final CElegansSearch cElegansSearchPipeline;
    private final NeighborsSearch neighborsSearch;
    private final StructuresSearch structuresSearch;
    private final EstablishCorrespondence establishCorrespondence;
    private final AnnotationManager annotationManager;

    private final ObservableList<String> searchResultsList;
    private final List<String> localUnformattedResults; // internal list of the search results that's passed to rules

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
            final GeneSearchManager geneSearchManager,
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
            //final RadioButton multicellRadioButton,
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
        this.geneSearchManager = requireNonNull(geneSearchManager);

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
        this.localUnformattedResults = new ArrayList<>();

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
                                descendantCheckBox.isSelected(),
                                ancestorCheckBox.isSelected()));
                        return null;
                    }
                };
                return task;
            }
        };

        geneSearchManager.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                switch (newState) {
                    case SCHEDULED:
                        showLoadingService.restart();
                        break;
                    case READY:
                    case RUNNING:
                        break;
                    case SUCCEEDED:
                        showLoadingService.cancel();
                        searchResultsList.clear();
                        // call this again to get the results read into the search results list - it will pick up the cached results
                        performSearchAndAlertAnnotationManager(GENE,
                                getSearchedText(),
                                descendantCheckBox.isSelected(),
                                ancestorCheckBox.isSelected());
                        break;
                    case CANCELLED:
                        showLoadingService.cancel();
                        searchResultsList.clear();
                        localUnformattedResults.clear();
                        break;
                    case FAILED:
                        break;

                }
            }
        });


        // search type toggle
        searchTypeToggleGroup = new ToggleGroup();
        initSearchTypeToggleGroup(
                requireNonNull(lineageRadioButton),
                requireNonNull(functionalRadioButton),
                requireNonNull(descriptionRadioButton),
                requireNonNull(geneRadioButton),
                requireNonNull(connectomeRadioButton),
                //requireNonNull(multicellRadioButton),
                requireNonNull(descendantLabel));
    }

    /**
     *
     * @param lineageRadioButton
     * @param functionalRadioButton
     * @param descriptionRadioButton
     * @param geneRadioButton
     * @param connectomeRadioButton
     //* @param multicellRadioButton
     * @param descendantLabel
     */
    private void initSearchTypeToggleGroup(
            final RadioButton lineageRadioButton,
            final RadioButton functionalRadioButton,
            final RadioButton descriptionRadioButton,
            final RadioButton geneRadioButton,
            final RadioButton connectomeRadioButton,
            //final RadioButton multicellRadioButton,
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

        //multicellRadioButton.setToggleGroup(searchTypeToggleGroup);
        //multicellRadioButton.setUserData(MULTICELLULAR_STRUCTURE_CELLS);


        // listener for change in the search toggles
        searchTypeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            final SearchType newType = (SearchType) newValue.getUserData();
            // disable descendant options for terminal cell searches
            if (newType == FUNCTIONAL || newType == DESCRIPTION) {
                descendantCheckBox.setSelected(false);
                descendantCheckBox.disableProperty().set(true);
                descendantLabel.disableProperty().set(true);
            } else {
                descendantCheckBox.disableProperty().set(false);
                descendantLabel.disableProperty().set(false);
            }

            if (newType != CONNECTOME) {
                // untick any/all connectome checkboxes that may have been left ticked
                presynapticCheckBox.setSelected(false);
                postsynapticCheckBox.setSelected(false);
                electricalCheckBox.setSelected(false);
                neuromuscularCheckBox.setSelected(false);
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
     * @param areDescendantsFetched
     * @param areAncestorsFetched
     */
    private void performSearchAndAlertAnnotationManager(
            final SearchType searchType,
            String searchedTerm,
            final boolean areDescendantsFetched,
            final boolean areAncestorsFetched) {

        searchResultsList.clear();
        localUnformattedResults.clear();

        if (!searchedTerm.isEmpty()) {
            searchedTerm = searchedTerm.trim();

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

                    /** The lineage type has a fallthrough method which does a strict
                     * string matching search if there are no static results. That way,
                     * in the event of a non-sulston embryo, you can search directly for
                     * the names of specific entities */
                    if (!cElegansDataSearchResults.hasResults()) {
                        modelDataSearchResults = ModelSpecificSearchUtil.nonSulstonLineageSearch(searchedTerm,
                                                                                                areAncestorsFetched,
                                                                                                areDescendantsFetched);
                    }

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
                        AbstractMap.SimpleEntry<OrganismDataType, List<String>> geneSearchResults = geneSearchManager.getPreviouslyFetchedGeneResults(searchedTerm);
                        if (geneSearchResults == null) {
                            // necessary to handle the search that will start with genes that have the format abc-##
                            // i.e. more than 1 digit because a search will begin on the first digit
                            if (geneSearchManager.getState() == RUNNING) {
                                geneSearchManager.cancel();
                            }
                            geneSearchManager.setSearchTerm(searchedTerm);
                            geneSearchManager.setSearchOptions(areAncestorsFetched, areDescendantsFetched, true, false, OrganismDataType.LINEAGE);
                            geneSearchManager.reset();
                            geneSearchManager.start();
                        } else {
                            cElegansDataSearchResults = new CElegansSearchResults(geneSearchResults);
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
                                    electricalCheckBox.isSelected(),
                                    neuromuscularCheckBox.isSelected(),
                                    OrganismDataType.LINEAGE));
                    break;
                //case MULTICELLULAR_STRUCTURE_CELLS: // model specific data
                    //break;
                case NEIGHBOR: // model specific data
                    modelDataSearchResults = neighborsSearch.getNeighboringCells(searchedTerm);
                    break;
            }

            if (cElegansDataSearchResults.hasResults() || !modelDataSearchResults.isEmpty()) {
                localUnformattedResults.addAll(cElegansDataSearchResults.getSearchResults());
                final List<String> entitiesForAnnotation = new ArrayList<>();

                // add the c elegans search results to the list to be propagated to the results list and trigger the annotation pipeline
                entitiesForAnnotation.addAll(cElegansDataSearchResults.getSearchResults());


                if (!modelDataSearchResults.isEmpty()) {
                    // this comes directly from the model and should be formatted correctly,
                    // so there is no need to pass it through the correspondence pipeline.
                    // Simply add it to the cellsForListView
                    localUnformattedResults.addAll(modelDataSearchResults);
                    entitiesForAnnotation.addAll(modelDataSearchResults);
                }

                sort(entitiesForAnnotation);

                // this appends functional names to the lineage names (unless they are gene names),
                // and then places in the ObservableList<String> searchResultsListView -> this triggers
                // RootLayoutController to populate the results window with them
                appendFunctionalToLineageNames(entitiesForAnnotation);
            }
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

        // keep the formatted results in a temporary list and then add them all at once so that the
        // trigger in Window3d controller to correspondingly annotate the model only runs once and
        // does so with all of the results loaded

        ArrayList<String> localFormattedResults = new ArrayList<>();
        for (String result : list) {
            if (getFunctionalNameByLineageName(result) != null) {
                result += " (" + getFunctionalNameByLineageName(result) + ")";
            }
            localFormattedResults.add(result);
        }

        // --> this updated is trigger at updateLocalSearchResults() in Window3DController
        searchResultsList.addAll(localFormattedResults);
    }

    /**
     * Access the text in the search bar
     *
     * @return
     */
    private String getSearchedText() {
        final String searched = searchTextField.getText();
        return searched;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////


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


            SearchType searchType = (SearchType) searchTypeToggleGroup.getSelectedToggle().getUserData();
            switch (searchType) {
                case LINEAGE: // C Elegans data
                    annotationManager.addColorRule(
                            LINEAGE,
                            getSearchedText(),
                            colorPicker.getValue(),
                            localUnformattedResults,
                            options);
                    break;
                case FUNCTIONAL: // C Elegans data
                    annotationManager.addColorRule(
                            FUNCTIONAL,
                            getSearchedText(),
                            colorPicker.getValue(),
                            localUnformattedResults,
                            options);
                    break;
                case DESCRIPTION: // C Elegans data
                    annotationManager.addColorRule(
                            DESCRIPTION,
                            getSearchedText(),
                            colorPicker.getValue(),
                            localUnformattedResults,
                            options);
                    break;
                case GENE: // C Elegans data
                    annotationManager.addGeneColorRule(
                            getSearchedText(),
                            colorPicker.getValue(),
                            localUnformattedResults,
                            options);
                    break;
                case CONNECTOME: // C Elegans data
                    final Rule rule = annotationManager.addConnectomeColorRule(
                            CElegansSearch.checkQueryCell(getSearchedText()),
                            colorPicker.getValue(),
                            localUnformattedResults,
                            presynapticCheckBox.isSelected(),
                            postsynapticCheckBox.isSelected(),
                            electricalCheckBox.isSelected(),
                            neuromuscularCheckBox.isSelected(),
                            options);
                    break;
            }
            //rebuildSubsceneFlag.setValue(true);
            searchTextField.clear();
            localUnformattedResults.clear();
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

    public Service<Void> getResultsUpdateService() {
        return resultsUpdateService;
    }

    /**
     * Service that shows when gene results are being fetched by the GeneSearchManager so that the user does
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