/*
 * Bao Lab 2017
 */

package application_src.controllers.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import application_src.application_model.search.ModelSearch.EstablishCorrespondence;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.NeighborsSearch;
import application_src.application_model.search.SearchConfiguration.SearchOption;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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

import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_BODY;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;
import static application_src.application_model.search.SearchConfiguration.SearchType.*;
import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;

import static javafx.application.Platform.runLater;
import static javafx.scene.paint.Color.WHITE;

/**
 * Controller for the context menu that shows up on right click on a 3D entity. The menu can be accessed via the 3D
 * subscene or the sulston tree.
 */

public class ContextMenuController extends AnchorPane implements Initializable {

    /**
     * Wait time in miliseconds between showing a different number of periods after loading (for gene option)
     */
    private static final long WAIT_TIME_MILLI = 750;

    private static final double MAX_MENU_HEIGHT = 200;

    private static final int PRE_SYN_INDEX = 0,
            POST_SYN_INDEX = 1,
            ELECTR_INDEX = 2,
            NEURO_INDEX = 3;

    /**
     * Default color of the rules that are created by the context menu
     */
    private static final Color DEFAULT_COLOR = WHITE;

    private final Stage ownStage;

    private CElegansSearch cElegansSearchPipeline;
    private GeneSearchManager geneSearchManager;
    private NeighborsSearch neighborsSearch;
    private EstablishCorrespondence establishCorrespondence;
    private AnnotationManager annotationManager;

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

    private int count; // to show loading in progress
    private String cellName; // lineage name of cell
    private ContextMenu expressesMenu;
    private MenuItem expressesTitle;
    private MenuItem loadingMenuItem;
    private Service<Void> loadingService;
    private ContextMenu wiredToMenu;
    private MenuItem colorAll;
    private Menu preSyn, postSyn, electr, neuro;
    private Stage parentStage;
    private BooleanProperty bringUpInfoProperty;
    private boolean isStructure;

    /**
     * @param parentStage
     * @param ownStage
     * @param cElegansSearchPipeline
     * @param geneSearchManager
     * @param neighborsSearch
     * @param establishCorrespondence
     * @param annotationManager
     * @param bringUpInfoProperty
     */
    public ContextMenuController(
            final Stage parentStage,
            final Stage ownStage,
            final CElegansSearch cElegansSearchPipeline,
            final GeneSearchManager geneSearchManager,
            final NeighborsSearch neighborsSearch,
            final EstablishCorrespondence establishCorrespondence,
            final AnnotationManager annotationManager,
            final BooleanProperty bringUpInfoProperty) {

        super();

        this.isStructure = false;
        this.parentStage = requireNonNull(parentStage);
        this.ownStage = requireNonNull(ownStage);

        this.cElegansSearchPipeline = requireNonNull(cElegansSearchPipeline);
        this.geneSearchManager = requireNonNull(geneSearchManager);
        this.neighborsSearch = requireNonNull(neighborsSearch);
        this.establishCorrespondence = requireNonNull(establishCorrespondence);
        this.annotationManager = requireNonNull(annotationManager);

        this.bringUpInfoProperty = requireNonNull(bringUpInfoProperty);

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

        geneSearchManager.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                switch (newState) {
                    case SCHEDULED:
                        if (ownStage.isShowing()) { // this will tell us whether this gene search is being called by the search layer or here
                            expressesMenu.getItems().addAll(loadingMenuItem);
                            loadingService.restart();
                        }
                        break;
                    case READY:
                    case RUNNING:
                        break;
                    case SUCCEEDED:
                        if (ownStage.isShowing()) {
                            loadingService.cancel();
                            resetLoadingMenuItem();

                            System.out.println("fetching cached search results with: " + cellName);
                            System.out.println(geneSearchManager.getPreviouslyFetchedGeneResults(cellName) == null);
                            final List<String> results = geneSearchManager.getPreviouslyFetchedGeneResults(cellName).getValue();
                            if (results != null) {
                                for (String res : results) {
                                    final MenuItem item = new MenuItem(res);
                                    item.setOnAction(event12 -> {
                                        ArrayList<SearchOption> options = new ArrayList<>();
                                        options.add(CELL_NUCLEUS);

                                        geneSearchManager.setSearchTerm(res);
                                        geneSearchManager.setSearchOptions(false, false, true, false, OrganismDataType.LINEAGE);
                                        geneSearchManager.restart();

                                        boolean searchSucceeded = false;
                                        while (searchSucceeded) {
                                            switch (geneSearchManager.getState()) {
                                                case SUCCEEDED:
                                                    searchSucceeded = true;
                                            }
                                        }

                                        List<String> r = geneSearchManager.getPreviouslyFetchedGeneResults(res).getValue();
                                        if (r != null) {
                                            final Rule rule = annotationManager.addGeneColorRule(res,
                                                    DEFAULT_COLOR,
                                                    r,
                                                    options);
                                            rule.showEditStage(parentStage);
                                            ownStage.hide();
                                        }
                                    });
                                    expressesMenu.getItems().add(item);
                                }
                            }
                        }
                        break;
                    case CANCELLED:
                        if (ownStage.isShowing()) {
                            resetLoadingMenuItem();
                            loadingService.cancel();
                        }
                        break;
                    case FAILED:
                        break;

                }
            }
        });
    }

    public void setIsStructure(boolean b) {
        this.isStructure = b;
    }

    /**
     * Sets the listener for the 'more info' button click in the menu
     *
     * @param handler the handler (provided by RootLayoutController) that handles the 'more info' button click action
     */
    public void setMoreInfoButtonListener(final EventHandler<MouseEvent> handler) {
        info.setOnMouseClicked(handler);
    }

    /**
     * Sets te listener for the 'color this cell' button click in the menu. Called by Window3DController and
     * SulstonTreePane since they handle the click differently. A different mouse click listener is set depending on
     * where the menu pops up (whether in the 3D subscene or the sulston tree)
     *
     * @param handler the handler (provided by Window3DController or SulstonTreePane) that handles the 'color this cell'
     *                button click action
     */
    public void setColorButtonListener(EventHandler<MouseEvent> handler) {
        color.setOnMouseClicked(handler);
    }

    public void setColorButtonText(final boolean isStructure) {
        if (isStructure) {
            color.setText("Color Structure");
        } else {
            color.setText("Color Cell");
        }
    }

    /**
     * Sets te listener for the 'color neighbors' button click in the menu. Called by Window3DController and
     * SulstonTreePane since they handle the click differently. A different mouse click listener is set depending on
     * where the menu pops up (whether in the 3D subscene or the sulston tree)
     *
     * @param handler the handler (provided by Window3DController or
     *                SulstonTreePane) that handles the 'color neighbors' button
     *                click action
     */
    public void setColorNeighborsButtonListener(EventHandler<MouseEvent> handler) {
        colorNeighbors.setOnMouseClicked(handler);
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
     * @param name lineage name of cell/cell body that the context menu is for
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
     * Disables/enables the 'More Info' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable true if the entity is a multicellular structure or a tract, false otherwise
     */
    public void disableMoreInfoFunction(final boolean disable) {
        info.setDisable(disable);
    }

    /**
     * Disables/enables the 'Wired To' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable true if the entity is a multicellular structure or a tract, false otherwise
     */
    public void disableWiredToFunction(final boolean disable) {
        wiredTo.setDisable(disable);
    }

    /**
     * Disables/enables the 'Gene Expression' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable true if the entity is a multicellular structure or a tract, false otherwise
     */
    public void disableGeneExpressionFunction(final boolean disable) {
        expresses.setDisable(disable);
    }

    /**
     * Disables/enables the 'Color Neighbors' functionality depending on whether the entity is a terminal cell/cell body
     *
     * @param disable true if entity is a multicellular structure or tract, false otherwise
     */
    public void disableColorNeighborsFunction(final boolean disable) {
        colorNeighbors.setDisable(disable);
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

    /**
     *
     */
    private void performConnectomeSearchAndPopulateMenu() {

        wiredToMenu.getItems().clear();
        preSyn.getItems().clear();
        postSyn.getItems().clear();
        electr.getItems().clear();
        neuro.getItems().clear();

        if (cellName != null && !cellName.isEmpty()) {
            List<List<String>> results = new ArrayList<>();

            // these calls return functional names
            results.add(
                    PRE_SYN_INDEX,
                    cElegansSearchPipeline.executeConnectomeSearch(
                            cellName,
                            false,
                            false,
                            true,
                            false,
                            false,
                            false,
                            OrganismDataType.LINEAGE).getValue());
            results.add(
                    POST_SYN_INDEX,
                    cElegansSearchPipeline.executeConnectomeSearch(
                            cellName,
                            false,
                            false,
                            false,
                            true,
                            false,
                            false,
                            OrganismDataType.LINEAGE).getValue());
            results.add(
                    ELECTR_INDEX,
                    cElegansSearchPipeline.executeConnectomeSearch(
                            cellName,
                            false,
                            false,
                            false,
                            false,
                            true,
                            false,
                            OrganismDataType.LINEAGE).getValue());
            results.add(
                    NEURO_INDEX,
                    cElegansSearchPipeline.executeConnectomeSearch(
                            cellName,
                            false,
                            false,
                            false,
                            false,
                            false,
                            true,
                            OrganismDataType.LINEAGE).getValue());

            if (results.isEmpty()) {
                wiredToMenu.getItems().add(new MenuItem("None"));
                return;
            }

            // colors all connections
            final MenuItem all = new MenuItem("Color All");
            wiredToMenu.getItems().add(all);
            all.setOnAction(event -> {
                ArrayList<String> allResults = new ArrayList<>();
                allResults.addAll(results.get(PRE_SYN_INDEX));
                allResults.addAll(results.get(POST_SYN_INDEX));
                allResults.addAll(results.get(ELECTR_INDEX));
                allResults.addAll(results.get(NEURO_INDEX));
                List<SearchOption> options = new ArrayList<>();
                options.add(CELL_NUCLEUS);
                options.add(CELL_BODY);
                final Rule rule = annotationManager.addConnectomeColorRule(
                        CElegansSearch.checkQueryCell(cellName),
                        DEFAULT_COLOR,
                        allResults,
                        true,
                        true,
                        true,
                        true,
                        options);
                rule.showEditStage(parentStage);
            });

            // populate each of the respective menus
            if (!results.get(PRE_SYN_INDEX).isEmpty()) {
                // add the Color All button for presynaptic results
                final MenuItem allPresyn = new MenuItem("Color All");
                preSyn.getItems().add(allPresyn);
                allPresyn.setOnAction(event -> {
                    List<SearchOption> options = new ArrayList<>();
                    options.add(CELL_NUCLEUS);
                    options.add(CELL_BODY);
                    final Rule rule = annotationManager.addConnectomeColorRule(
                            CElegansSearch.checkQueryCell(cellName),
                            DEFAULT_COLOR,
                            results.get(PRE_SYN_INDEX),
                            true,
                            false,
                            false,
                            false,
                            options);
                    rule.showEditStage(parentStage);
                });

                // add all of the results to the dropdown menu for reference, but they're not clickable
                for (String preSynRes : results.get(PRE_SYN_INDEX)) {
                    MenuItem item = new MenuItem(preSynRes);
                    item.setDisable(false);
                    preSyn.getItems().add(item);
                }
            } else {
                preSyn.getItems().add(new MenuItem("None"));
            }

            if (!results.get(POST_SYN_INDEX).isEmpty()) {
                final MenuItem allPostyn = new MenuItem("Color All");
                postSyn.getItems().add(allPostyn);
                allPostyn.setOnAction(event -> {
                    List<SearchOption> options = new ArrayList<>();
                    options.add(CELL_NUCLEUS);
                    options.add(CELL_BODY);
                    final Rule rule = annotationManager.addConnectomeColorRule(
                            CElegansSearch.checkQueryCell(cellName),
                            DEFAULT_COLOR,
                            results.get(POST_SYN_INDEX),
                            false,
                            true,
                            false,
                            false,
                            options);
                    rule.showEditStage(parentStage);
                });

                for (String postSynRes : results.get(POST_SYN_INDEX)) {
                    MenuItem item = new MenuItem(postSynRes);
                    item.setDisable(false);
                    postSyn.getItems().add(item);
                }
            } else {
                postSyn.getItems().add(new MenuItem("None"));
            }

            if (!results.get(ELECTR_INDEX).isEmpty()) {
                final MenuItem allElec = new MenuItem("Color All");
                electr.getItems().add(allElec);
                allElec.setOnAction(event -> {
                    List<SearchOption> options = new ArrayList<>();
                    options.add(CELL_NUCLEUS);
                    options.add(CELL_BODY);
                    final Rule rule = annotationManager.addConnectomeColorRule(
                            CElegansSearch.checkQueryCell(cellName),
                            DEFAULT_COLOR,
                            results.get(ELECTR_INDEX),
                            false,
                            false,
                            true,
                            false,
                            options);
                    rule.showEditStage(parentStage);
                });

                for (String elecRes : results.get(ELECTR_INDEX)) {
                    MenuItem item = new MenuItem(elecRes);
                    item.setDisable(false);
                    electr.getItems().add(item);
                }
            } else {
                electr.getItems().add(new MenuItem("None"));
            }

            if (!results.get(NEURO_INDEX).isEmpty()) {
                final MenuItem allNeuro = new MenuItem("Color All");
                neuro.getItems().add(allNeuro);
                allNeuro.setOnAction(event -> {
                    List<SearchOption> options = new ArrayList<>();
                    options.add(CELL_NUCLEUS);
                    options.add(CELL_BODY);
                    final Rule rule = annotationManager.addConnectomeColorRule(
                            CElegansSearch.checkQueryCell(cellName),
                            DEFAULT_COLOR,
                            results.get(NEURO_INDEX),
                            false,
                            false,
                            false,
                            true,
                            options);
                    rule.showEditStage(parentStage);
                });

                for (String neuroRes : results.get(NEURO_INDEX)) {
                    MenuItem item = new MenuItem(neuroRes);
                    item.setDisable(false);
                    neuro.getItems().add(item);
                }
            } else {
                neuro.getItems().add(new MenuItem("None"));
            }
        }
    }

    /**
     * Initializer for the loading of ContextMenuLayout.fxml. Sets 'wired to'
     * and 'gene expression' button actions.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadingMenuItem = new MenuItem("Loading");

        info.setOnAction(event -> {
            // generate the info window page

            // show the info window
            showInfoAction();
        });

        color.setOnAction(event -> {
            // make the rule with no search options
            if (isStructure) {
                final Rule rule = annotationManager.addStructureRuleBySceneName(cellName, DEFAULT_COLOR);
                rule.showEditStage(parentStage);
            } else {
                ArrayList<SearchOption> options = new ArrayList<>();
                options.add(CELL_NUCLEUS);
                final Rule rule = annotationManager.addColorRule(LINEAGE, cellName, DEFAULT_COLOR, new ArrayList<>(),  options);
                rule.showEditStage(parentStage);
            }
            ownStage.hide();
        });

        colorNeighbors.setOnAction(event -> {
            if (cellName != null && !cellName.isEmpty()) {
                List<String> results = neighborsSearch.getNeighboringCells(cellName);
                if (!results.isEmpty()) {
                    ArrayList<SearchOption> options = new ArrayList<>();
                    options.add(CELL_NUCLEUS);
                    final Rule rule = annotationManager.addColorRule(NEIGHBOR, cellName, DEFAULT_COLOR, results, options);
                    rule.showEditStage(parentStage);
                    ownStage.hide();
                }
            }
        });

        expresses.setOnAction(event -> {
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

            System.out.println("setting search term: " + cellName);
            geneSearchManager.setSearchTerm(cellName);
            geneSearchManager.setSearchOptions(false, false, false, true, OrganismDataType.GENE);
            geneSearchManager.reset();
            geneSearchManager.start();
        });

        wiredTo.setOnAction(event -> {
            if (wiredToMenu == null) {
                wiredToMenu = new ContextMenu();
                wiredToMenu.setMaxHeight(MAX_MENU_HEIGHT);

                wiredTo.setContextMenu(wiredToMenu);

                wiredToMenu.setOnHidden(event1 -> wiredToMenu.getItems().clear());

                wiredToMenu.setAutoHide(true);

                colorAll = new MenuItem("Color All");
                preSyn = new Menu("Pre-Synaptic");
                postSyn = new Menu("Post-Synaptic");
                electr = new Menu("Electrical");
                neuro = new Menu("Neuromuscular");
            }

            wiredToMenu.getItems().clear();
            performConnectomeSearchAndPopulateMenu();
            wiredToMenu.getItems().addAll(preSyn, postSyn, electr, neuro);
            wiredToMenu.show(wiredTo, Side.RIGHT, 0, 0);
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
}
