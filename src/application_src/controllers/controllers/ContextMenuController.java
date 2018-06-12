/*
 * Bao Lab 2017
 */

package application_src.controllers.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.CElegansData.Gene.WormBaseQuery;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import application_src.application_model.search.ModelSearch.EstablishCorrespondence;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.NeighborsSearch;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
import application_src.application_model.search.SearchConfiguration.SearchOption;
import application_src.application_model.search.SearchConfiguration.SearchType;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import application_src.controllers.layers.SearchLayer;
import application_src.application_model.cell_case_logic.CasesLists;
import application_src.application_model.cell_case_logic.cases.TerminalCellCase;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.resources.ProductionInfo;

import static application_src.application_model.search.SearchConfiguration.SearchType.LINEAGE;
import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;

import static javafx.application.Platform.runLater;
import static javafx.scene.paint.Color.WHITE;

import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.search.SearchConfiguration.SearchType.GENE;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;

/**
 * Controller for the context menu that shows up on right click on a 3D entity. The menu can be accessed via the 3D
 * subscene or the sulston tree.
 */

public class ContextMenuController extends AnchorPane implements Initializable {

    /** Wait time in miliseconds between showing a different number of periods after loading (for gene option) */
    private static final long WAIT_TIME_MILLI = 750;

    private static final double MAX_MENU_HEIGHT = 200;

    private static final int PRE_SYN_INDEX = 0,
            POST_SYN_INDEX = 1,
            ELECTR_INDEX = 2,
            NEURO_INDEX = 3;

    /** Default color of the rules that are created by the context menu */
    private static final Color DEFAULT_COLOR = WHITE;

    private final Stage ownStage;

    @FXML
    private VBox mainVBox;
    @FXML
    private HBox expressesHBox;
    @FXML
    private HBox wiredToHBox;
    @FXML
    private Text nameText;
    @FXML
    private Button info;
    @FXML
    private Button color;
    @FXML
    private Button expresses;
    @FXML
    private Button wiredTo;
    @FXML
    private Button colorNeighbors;


    private List<String> searchResults;
    private SearchType searchType;
    private int count; // to show loading in progress
    private String cellName; // lineage name of cell
    private ContextMenu expressesMenu;
    private MenuItem expressesTitle;
    private MenuItem loadingMenuItem;
    private Service<Void> searchService;
    private Service<Void> loadingService;
    private ContextMenu wiredToMenu;
    private MenuItem colorAll;
    private Menu preSyn, postSyn, electr, neuro;
    private Stage parentStage;
    private BooleanProperty bringUpInfoProperty;
    private CElegansSearch cElegansSearchPipeline;
    private NeighborsSearch neighborsSearch;
    private EstablishCorrespondence establishCorrespondence;
    private AnnotationManager annotationManager;



    public ContextMenuController(
            final Stage parentStage,
            final Stage ownStage,
            final BooleanProperty bringUpInfoProperty,
            final CElegansSearch CElegansSearchPipeline,
            final NeighborsSearch neighborsSearch,
            final EstablishCorrespondence establishCorrespondence,
            final AnnotationManager annotationManager) {

        super();

        this.parentStage = requireNonNull(parentStage);
        this.ownStage = requireNonNull(ownStage);

        this.bringUpInfoProperty = requireNonNull(bringUpInfoProperty);

        this.cElegansSearchPipeline = requireNonNull(CElegansSearchPipeline);
        this.neighborsSearch = requireNonNull(neighborsSearch);
        this.establishCorrespondence = requireNonNull(establishCorrespondence);
        this.annotationManager = requireNonNull(annotationManager);

        searchResults = new ArrayList<>();
        searchType = SearchType.LINEAGE;

        // build up the loading service
        loadingService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        final int modulus = 4;
                        while (true) {
                            if (isCancelled()) {
                                break;
                            }
                            runLater(() -> {
                                String loading = "Loading";
                                int num = count % modulus;
                                switch (num) {
                                    case 1:
                                        loading += ".";
                                        break;
                                    case 2:
                                        loading += "..";
                                        break;
                                    case 3:
                                        loading += "...";
                                        break;
                                    default:
                                        break;
                                }
                                loadingMenuItem.setText(loading);
                            });
                            try {
                                sleep(WAIT_TIME_MILLI);
                                count++;
                                if (count < 0) {
                                    count = 0;
                                }
                            } catch (InterruptedException ie) {
                                break;
                            }
                        }
                        return null;
                    }
                };
            }
        };

        // the service that will perform the searches available in the context menu:
        // Lineage
        // Neighbors
        // Gene
        // Connectome
        this.searchService = new Service<Void>() {
            @Override
            protected final Task<Void> createTask() {
                final Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        runLater(() -> performSearch());
                        return null;
                    }
                };
                return task;
            }
        };


        searchService.setOnScheduled(event -> {
            runLater(() -> {
                if (searchType.equals(SearchType.GENE)) {
                    expressesMenu.getItems().addAll(loadingMenuItem);
                    loadingService.restart();
                }
            });

        });

        // on succeeded tasks
        searchService.setOnSucceeded(event -> {
            loadingService.cancel();
            resetLoadingMenuItem();
            if (searchType.equals(SearchType.GENE)) {
                // set an on action command for each gene so that it can be clicked and made a rule
                for (String result : searchResults) {
                    System.out.println("gene result: " + result);
                    final MenuItem item = new MenuItem(result);
                    item.setOnAction(event12 -> {
                        ArrayList<SearchOption> options = new ArrayList<>();
                        options.add(CELL_NUCLEUS);
                        final Rule rule = annotationManager.addColorRule(GENE,
                                cellName,
                                DEFAULT_COLOR,
                                searchResults,
                                options);
                        rule.showEditStage(this.parentStage);
                        ownStage.hide();
                    });
                    expressesMenu.getItems().add(item);

                }
            } else if (searchType.equals(SearchType.CONNECTOME)) {
                // get the formatted results for the context menu
                List<List<String>> results = getConnectomeResultsFormattedForContextMenu();

                // set those rules to the context menu pane
                populateWiredToMenu(
                        results.get(PRE_SYN_INDEX),
                        preSyn,
                        true,
                        false,
                        false,
                        false);
                populateWiredToMenu(
                        results.get(POST_SYN_INDEX),
                        postSyn,
                        false,
                        true,
                        false,
                        false);
                populateWiredToMenu(
                        results.get(ELECTR_INDEX),
                        electr,
                        false,
                        false,
                        true,
                        false);
                populateWiredToMenu(
                        results.get(NEURO_INDEX),
                        neuro,
                        false,
                        false,
                        false,
                        false);

                // set the call to search layer on the 'Color All' button
                colorAll.setOnAction(event1 -> {

                    // translate the name if necessary
                    String funcName = CElegansSearchPipeline.checkQueryCell(cellName).toLowerCase();

                    List<String> searchResults = cElegansSearchPipeline.executeConnectomeSearch(
                            funcName,
                            false,
                            false,
                            true,
                            true,
                            true,
                            true,
                            OrganismDataType.LINEAGE).getValue();

                    ArrayList<SearchOption> options = new ArrayList<>();
                    options.add(CELL_NUCLEUS);
                    final Rule rule = annotationManager.addConnectomeColorRule(
                            funcName,
                            DEFAULT_COLOR,
                            searchResults,
                            true,
                            true,
                            true,
                            true,
                            options);
                    rule.showEditStage(this.parentStage);

                });

                wiredToMenu.show(wiredTo, Side.RIGHT, 0, 0);
            } else if (searchType.equals(SearchType.NEIGHBOR)) {
                // show the rule stage
                // show the rule stage
                ArrayList<SearchOption> options = new ArrayList<>();
                options.add(CELL_NUCLEUS);
                final Rule rule = annotationManager.addColorRule(LINEAGE,
                        cellName,
                        DEFAULT_COLOR,
                        searchResults,
                        options);
                rule.showEditStage(this.parentStage);
                ownStage.hide();
            } else if (searchType.equals(SearchType.LINEAGE)) {
                // show the rule stage
                ArrayList<SearchOption> options = new ArrayList<>();
                options.add(CELL_NUCLEUS);
                final Rule rule = annotationManager.addColorRule(LINEAGE,
                        cellName,
                        DEFAULT_COLOR,
                        searchResults,
                        options);
                rule.showEditStage(this.parentStage);
                ownStage.hide();
            }
        });

        // on cancelled tasks
        searchService.setOnCancelled(event -> {
            resetLoadingMenuItem();
            loadingService.cancel();
        });
    }

    private void performSearch() {
        searchResults.clear();
        switch (searchType) {
            case LINEAGE:
                break;
            case NEIGHBOR:
                searchResults.addAll(neighborsSearch.getNeighboringCells(cellName));
                break;
            case GENE:
                cElegansSearchPipeline.startGeneSearch(cellName, false, false, false, true, OrganismDataType.LINEAGE);
                searchResults.addAll(GeneSearchManager.getPreviouslyFetchedGeneResults(cellName).getValue());
                break;
            case CONNECTOME:
                break;
        }
    }

    /**
     *
     * @return
     */
    private List<List<String>> getConnectomeResultsFormattedForContextMenu() {
        List<List<String>> results = new ArrayList<>();

        // translate the name if necessary
        String funcName = CElegansSearch.checkQueryCell(cellName).toLowerCase();

        // these calls return functional names
        results.add(
                PRE_SYN_INDEX,
                cElegansSearchPipeline.executeConnectomeSearch(
                        funcName,
                        false,
                        false,
                        true,
                        false,
                        false,
                        false,
                        OrganismDataType.FUNCTIONAL).getValue());
        results.add(
                POST_SYN_INDEX,
                cElegansSearchPipeline.executeConnectomeSearch(
                        funcName,
                        false,
                        false,
                        false,
                        true,
                        false,
                        false,
                        OrganismDataType.FUNCTIONAL).getValue());
        results.add(
                ELECTR_INDEX,
                cElegansSearchPipeline.executeConnectomeSearch(
                        funcName,
                        false,
                        false,
                        false,
                        false,
                        true,
                        false,
                        OrganismDataType.FUNCTIONAL).getValue());
        results.add(
                NEURO_INDEX,
                cElegansSearchPipeline.executeConnectomeSearch(
                        funcName,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true,
                        OrganismDataType.FUNCTIONAL).getValue());
        return results;
    }

    /**
     * Populates the input menu with MenuItems for each of the 'wired-to' results.
     *
     * @param results
     *         Results from either a pre-synaptic, post-synaptic, electrical, or neuromuscular query to the connectome
     * @param menu
     *         The pre-synaptic, post-synaptic, eletrical, or neuromuscular menu that should be populated with these
     *         results
     * @param isPresynaptic
     *         true if a pre-synaptic query to the connectome was issued, false otherwise
     * @param isPostsynaptic
     *         true if a pose-synaptic query to the connectome was issued, false otherwise
     * @param isElectrical
     *         true if an electrical query to the connectome was issued, false otherwise
     * @param isNeuromuscular
     *         true if a neuromuscular query to the connectome was issued, false otherwise
     */
    private void populateWiredToMenu(
            final List<String> results,
            final Menu menu,
            final boolean isPresynaptic,
            final boolean isPostsynaptic,
            final boolean isElectrical,
            final boolean isNeuromuscular) {

        menu.getItems().clear();

        String funcName = CElegansSearch.checkQueryCell(cellName).toLowerCase();

        if (results.isEmpty()) {
            menu.getItems().add(new MenuItem("None"));
            return;
        }

        final MenuItem all = new MenuItem("Color All");
        menu.getItems().add(all);
        all.setOnAction(event -> {
            // perform a local search
            List<String> searchResultsLcl = cElegansSearchPipeline.executeConnectomeSearch(
                    funcName,
                    false,
                    false,
                    isPresynaptic,
                    isPostsynaptic,
                    isElectrical,
                    isNeuromuscular,
                    OrganismDataType.LINEAGE).getValue();

            // add the rule through the annotation manager
            ArrayList<SearchOption> options = new ArrayList<>();
            options.add(CELL_NUCLEUS);
            final Rule rule = annotationManager.addConnectomeColorRule(
                    funcName,
                    DEFAULT_COLOR,
                    searchResultsLcl,
                    isPresynaptic,
                    isPostsynaptic,
                    isElectrical,
                    isNeuromuscular,
                    options);
            rule.showEditStage(parentStage);
            ownStage.hide();
        });

        for (String result : results) {
            System.out.println(result);
            final MenuItem item = new MenuItem(result);
            item.setDisable(true);
            menu.getItems().add(item);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////// UTILITY METHODS /////////////////////////////////////////////
    public void setColorButtonText(final boolean isStructure) {
        if (isStructure) {
            color.setText("Color Structure");
        } else {
            color.setText("Color Cell");
        }
    }

    /**
     * Returns the cell name of the context menu (also its title). This name is either the lineage name or the
     * functional name (if the cell is a terminal cell)
     *
     * @return cell name (title of the context menu)
     */
    public String getName() {
        return cellName;
    }

    /**
     * Sets the linage name (cell/cellbody scope) of the context menu
     *
     * @param name
     *         lineage name of cell/cell body that the context menu is for
     */
    public void setName(String name) {
        name = name.trim();

        if (name.startsWith("Ab")) {
            name = "AB" + name.substring(2);
        }

        cellName = name;

        String funcName = getFunctionalNameByLineageName(name);
        if (funcName != null) {
            name = name + " (" + funcName + ")";
        }

        nameText.setText(name);
    }

    /**
     * Removes the MenuItem that shows that gene expression web querying is in
     * progress.
     */
    private void resetLoadingMenuItem() {
        if (loadingMenuItem != null) {
            runLater(() -> {
                if (wiredToMenu != null && wiredToMenu.getItems().contains(loadingMenuItem)) {
                    wiredToMenu.getItems().remove(loadingMenuItem);
                } else if (expressesMenu != null && expressesMenu.getItems().contains(loadingMenuItem)) {
                    expressesMenu.getItems().remove(loadingMenuItem);
                }

                loadingMenuItem.setText("Loading");
            });
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Disables/enables the 'More Info' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable
     *         true if the entity is a multicellular structure or a tract, false otherwise
     */
    public void disableMoreInfoFunction(final boolean disable) {
        info.setDisable(disable);
    }

    /**
     * Disables/enables the 'Wired To' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable
     *         true if the entity is a multicellular structure or a tract, false otherwise
     */
    public void disableWiredToFunction(final boolean disable) {
        wiredTo.setDisable(disable);
    }

    /**
     * Disables/enables the 'Gene Expression' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable
     *         true if the entity is a multicellular structure or a tract, false otherwise
     */
    public void disableGeneExpressionFunction(final boolean disable) {
        expresses.setDisable(disable);
    }

    /**
     * Disables/enables the 'Color Neighbors' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable
     *         true if entity is a multicellular structure or tract, false otherwise
     */
    public void disableColorNeighborsFunction(final boolean disable) {
        colorNeighbors.setDisable(disable);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Initializer for the loading of ContextMenuLayout.fxml. Sets 'wired to'
     * and 'gene expression' button actions.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadingMenuItem = new MenuItem("Loading");

        expresses.setOnAction(event -> {
            searchType = SearchType.GENE;
            if (expressesMenu == null) {
                expressesMenu = new ContextMenu();
                expressesMenu.setMaxHeight(MAX_MENU_HEIGHT);

                expresses.setContextMenu(expressesMenu);

                expressesMenu.setOnHidden(event13 -> {
                    if (expressesMenu.getItems().contains(loadingMenuItem)) {
                        expressesMenu.getItems().remove(loadingMenuItem);
                    }
                });

                expressesTitle = new MenuItem("Pick Gene To Color");
                expressesTitle.setOnAction(Event::consume);

                expressesMenu.setAutoHide(true);
            }

            expressesMenu.getItems().clear();
            expressesMenu.getItems().addAll(expressesTitle);
            expressesMenu.show(expresses, Side.RIGHT, 0, 0);

            searchService.restart();
        });

        wiredTo.setOnAction(event -> {
            searchType = SearchType.CONNECTOME;
            if (wiredToMenu == null) {
                wiredToMenu = new ContextMenu();
                wiredToMenu.setMaxHeight(MAX_MENU_HEIGHT);

                wiredTo.setContextMenu(wiredToMenu);

                wiredToMenu.setOnHidden(event1 -> searchService.cancel());

                wiredToMenu.setAutoHide(true);

                colorAll = new MenuItem("Color All");
                preSyn = new Menu("Pre-Synaptic");
                postSyn = new Menu("Post-Synaptic");
                electr = new Menu("Electrical");
                neuro = new Menu("Neuromuscular");
            }

            wiredToMenu.getItems().clear();
            wiredToMenu.getItems().addAll(colorAll, preSyn, postSyn, electr, neuro);
            wiredToMenu.show(wiredTo, Side.RIGHT, 0, 0);

            searchService.restart();
        });

        color.setOnAction(event -> {
            searchType = SearchType.LINEAGE;
            searchService.restart();
        });
        colorNeighbors.setOnAction(event -> {
            searchType = SearchType.NEIGHBOR;
            searchService.restart();
        });
    }

    /**
     * Toggles the BooleanProperty bringUpInfoProperty so that the cell info window is displayed. The controller for
     * the context menu listens for changes in this toggle.
     */
    @FXML
    public void showInfoAction() {
        bringUpInfoProperty.set(true);
        bringUpInfoProperty.set(false);
    }
} // end class





// OLD METHODS - stored here in the event that they are useful in the future
// 06/2018
//    /**
//     * Sets the listener for the 'more info' button click in the menu
//     *
//     * @param handler
//     *         the handler (provided by RootLayoutController) that handles the 'more info' button click action
//     */

//    /**
//     * Sets te listener for the 'color this cell' button click in the menu. Called by Window3DController and
//     * SulstonTreePane since they handle the click differently. A different mouse click listener is set depending on
//     * where the menu pops up (whether in the 3D subscene or the sulston tree)
//     *
//     * @param handler
//     *         the handler (provided by Window3DController or SulstonTreePane) that handles the 'color this cell'
//     *         button click action
//     */
//    public void setColorButtonListener(EventHandler<MouseEvent> handler) {
//        color.setOnMouseClicked(handler);
//    }
//
//    /**
//     * Sets the listener for the 'color neighbors' button click in the menu. Called by Window3DController and
//     * SulstonTreePane since they handle the click differently. A different mouse click listener is set depending on
//     * where the menu pops up (whether in the 3D subscene or the sulston tree)
//     *
//     * @param handler
//     *         the handler (provided by Window3DController or
//     *         SulstonTreePane) that handles the 'color neighbors' button
//     *         click action
//     */
//    public void setColorNeighborsButtonListener(EventHandler<MouseEvent> handler) {
//        colorNeighbors.setOnMouseClicked(handler);
//    }


