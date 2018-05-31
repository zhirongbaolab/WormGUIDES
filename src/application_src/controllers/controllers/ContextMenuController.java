/*
 * Bao Lab 2017
 */

package application_src.controllers.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application_src.application_model.data.CElegansData.Gene.WormBaseQuery;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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

import application_src.application_model.data.CElegansData.Connectome.Connectome;
import application_src.controllers.layers.SearchLayer;
import application_src.application_model.cell_case_logic.CasesLists;
import application_src.application_model.cell_case_logic.cases.TerminalCellCase;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.resources.ProductionInfo;

import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;

import static javafx.application.Platform.runLater;
import static javafx.scene.paint.Color.WHITE;

import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.search.OLD_PIPELINE_CLASSES.SearchType.GENE;
import static application_src.application_model.search.OLD_PIPELINE_CLASSES.SearchOption.CELL_NUCLEUS;

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

    private final SearchLayer searchLayer;

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
    private Service<List<String>> expressesQueryService;
    private Service<Void> loadingService;
    private ContextMenu wiredToMenu;
    private MenuItem colorAll;
    private Menu preSyn, postSyn, electr, neuro;
    private Service<List<List<String>>> wiredToQueryService;
    private ProductionInfo productionInfo;
    private Stage parentStage;
    private BooleanProperty bringUpInfoProperty;
    private Connectome connectome;

    /**
     * Controller constructor
     *
     * @param parentStage
     *         the parent stage (or popup window) that the context menu lives
     *         in, used for rule editing window popups
     * @param bringUpInfoProperty
     *         when set to true, RootLayoutController brings up the cell info
     *         window
     * @param cases
     *         list of cell cases (terminal and non-terminal) that are
     *         currently in the program
     * @param productionInfo
     *         production information about WormGUIDES
     * @param connectome
     *         connectome object that has information about cell connectome
     */
    public ContextMenuController(
            final Stage parentStage,
            final Stage ownStage,
            final SearchLayer searchLayer,
            final CasesLists cases,
            final ProductionInfo productionInfo,
            final Connectome connectome,
            final BooleanProperty bringUpInfoProperty) {

        super();

        this.parentStage = requireNonNull(parentStage);
        this.ownStage = requireNonNull(ownStage);

        this.searchLayer = requireNonNull(searchLayer);
        this.productionInfo = requireNonNull(productionInfo);

        this.bringUpInfoProperty = requireNonNull(bringUpInfoProperty);

        this.connectome = connectome;

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

        expressesQueryService = new Service<List<String>>() {
            @Override
            protected Task<List<String>> createTask() {
                final Task<List<String>> task = new Task<List<String>>() {
                    @Override
                    protected List<String> call() throws Exception {
                        if (cellName != null && !cellName.isEmpty()) {
                            if (cases == null) {
                                System.out.println("null cell name");
                                return null; // error check
                            }

                            return WormBaseQuery.issueWormBaseAnatomyTermQuery(cellName);

                        }
                        return null;
                    }
                };
                return task;
            }
        };

        expressesQueryService.setOnSucceeded(event -> {
            loadingService.cancel();
            resetLoadingMenuItem();
            final List<String> results = expressesQueryService.getValue();
            if (results != null) {
                for (String result : results) {
                    final MenuItem item = new MenuItem(result);
                    item.setOnAction(event12 -> {
                        final Rule rule = searchLayer.addColorRule(GENE, result, DEFAULT_COLOR, CELL_NUCLEUS);
                        rule.showEditStage(this.parentStage);
                    });
                    expressesMenu.getItems().add(item);
                }
            }
        });

        expressesQueryService.setOnScheduled(event -> {
            runLater(() -> expressesMenu.getItems().addAll(loadingMenuItem));
            loadingService.restart();
        });

        expressesQueryService.setOnCancelled(event -> {
            resetLoadingMenuItem();
            loadingService.cancel();
        });

        wiredToQueryService = new Service<List<List<String>>>() {
            @Override
            protected Task<List<List<String>>> createTask() {
                final Task<List<List<String>>> task = new Task<List<List<String>>>() {
                    @Override
                    protected List<List<String>> call() throws Exception {
                        if (cellName != null && !cellName.isEmpty()) {
                            List<List<String>> results = new ArrayList<>();

                            // translate the name if necessary
                            String funcName = connectome.checkQueryCell(cellName).toLowerCase();

                            if (cases.containsCellCase(funcName)) {
                                TerminalCellCase terminalCase = (TerminalCellCase) cases.getCellCase(funcName);
                                results.add(PRE_SYN_INDEX, terminalCase.getPresynapticPartners());
                                results.add(POST_SYN_INDEX, terminalCase.getPostsynapticPartners());
                                results.add(ELECTR_INDEX, terminalCase.getElectricalPartners());
                                results.add(NEURO_INDEX, terminalCase.getNeuromuscularPartners());
                            } else {
                                // these calls return functional names
                                results.add(
                                        PRE_SYN_INDEX,
                                        connectome.queryConnectivity(
                                                funcName,
                                                true,
                                                false,
                                                false,
                                                false,
                                                false));
                                results.add(
                                        POST_SYN_INDEX,
                                        connectome.queryConnectivity(
                                                funcName,
                                                false,
                                                true,
                                                false,
                                                false,
                                                false));
                                results.add(
                                        ELECTR_INDEX,
                                        connectome.queryConnectivity(
                                                funcName,
                                                false,
                                                false,
                                                true,
                                                false,
                                                false));
                                results.add(
                                        NEURO_INDEX,
                                        connectome.queryConnectivity(
                                                funcName,
                                                false,
                                                false,
                                                false,
                                                true,
                                                false));
                            }
                            return results;
                        }
                        return null;
                    }
                };
                return task;
            }
        };

        wiredToQueryService.setOnSucceeded(event -> {
            final List<List<String>> results = wiredToQueryService.getValue();
            if (results != null) {
                colorAll.setOnAction(event1 -> {
                    // translate the name if necessary
                    String funcName = connectome.checkQueryCell(cellName).toLowerCase();

                    final Rule rule = searchLayer.addConnectomeColorRuleFromContextMenu(
                            funcName,
                            DEFAULT_COLOR,
                            true,
                            true,
                            true,
                            true);
                    rule.showEditStage(this.parentStage);
                });

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
            } else {
                wiredToMenu.getItems().clear();
                wiredToMenu.getItems().add(new MenuItem("None"));
            }

            wiredToMenu.show(wiredTo, Side.RIGHT, 0, 0);
        });
    }

    /**
     * Sets the listener for the 'more info' button click in the menu
     *
     * @param handler
     *         the handler (provided by RootLayoutController) that handles the 'more info' button click action
     */
    public void setMoreInfoButtonListener(final EventHandler<MouseEvent> handler) {
        info.setOnMouseClicked(handler);
    }

    /**
     * Sets te listener for the 'color this cell' button click in the menu. Called by Window3DController and
     * SulstonTreePane since they handle the click differently. A different mouse click listener is set depending on
     * where the menu pops up (whether in the 3D subscene or the sulston tree)
     *
     * @param handler
     *         the handler (provided by Window3DController or SulstonTreePane) that handles the 'color this cell'
     *         button click action
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
     * @param handler
     *         the handler (provided by Window3DController or
     *         SulstonTreePane) that handles the 'color neighbors' button
     *         click action
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

    /**
     * Initializer for the loading of ContextMenuLayout.fxml. Sets 'wired to'
     * and 'gene expression' button actions.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadingMenuItem = new MenuItem("Loading");

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

            expressesQueryService.restart();
        });

        wiredTo.setOnAction(event -> {
            if (wiredToMenu == null) {
                wiredToMenu = new ContextMenu();
                wiredToMenu.setMaxHeight(MAX_MENU_HEIGHT);

                wiredTo.setContextMenu(wiredToMenu);

                wiredToMenu.setOnHidden(event1 -> wiredToQueryService.cancel());

                wiredToMenu.setAutoHide(true);

                colorAll = new MenuItem("Color All");
                preSyn = new Menu("Pre-Synaptic");
                postSyn = new Menu("Post-Synaptic");
                electr = new Menu("Electrical");
                neuro = new Menu("Neuromuscular");
            }

            wiredToMenu.getItems().clear();
            wiredToMenu.getItems().addAll(colorAll, preSyn, postSyn, electr, neuro);

            wiredToQueryService.restart();
        });
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

        String funcName = connectome.checkQueryCell(cellName).toLowerCase();

        if (results.isEmpty()) {
            menu.getItems().add(new MenuItem("None"));
            return;
        }

        final MenuItem all = new MenuItem("Color All");
        menu.getItems().add(all);
        all.setOnAction(event -> {
            final Rule rule = searchLayer.addConnectomeColorRuleFromContextMenu(
                    funcName,
                    DEFAULT_COLOR,
                    isPresynaptic,
                    isPostsynaptic,
                    isElectrical,
                    isNeuromuscular);
            rule.showEditStage(parentStage);
        });

        for (String result : results) {
            final MenuItem item = new MenuItem(result);
            menu.getItems().add(item);
            item.setOnAction(event -> {
                final Rule rule = searchLayer.addConnectomeColorRuleFromContextMenu(
                        connectome.checkQueryCell(result).toLowerCase(),
                        DEFAULT_COLOR,
                        isPresynaptic,
                        isPostsynaptic,
                        isElectrical,
                        isNeuromuscular);
                rule.showEditStage(parentStage);
            });
        }
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