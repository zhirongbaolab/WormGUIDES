/*
 * Bao Lab 2017
 */

package wormguides.view.infowindow;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import acetree.LineageData;
import connectome.Connectome;
import connectome.NeuronalSynapse;
import netscape.javascript.JSObject;
import wormguides.controllers.InfoWindowLinkController;
import wormguides.layers.SearchLayer;
import wormguides.models.anatomy.AmphidSensillaTerm;
import wormguides.models.cellcase.CasesLists;
import wormguides.models.cellcase.NonTerminalCellCase;
import wormguides.models.cellcase.TerminalCellCase;
import wormguides.models.subscenegeometry.SceneElement;
import wormguides.resources.ProductionInfo;
import wormguides.view.DraggableTab;

import static java.util.Objects.requireNonNull;

import static javafx.application.Platform.runLater;
import static javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS;

import static partslist.PartsList.getDescriptions;
import static partslist.PartsList.getFunctionalNameByLineageName;
import static partslist.PartsList.getFunctionalNames;
import static partslist.PartsList.getLineageNames;
import static partslist.PartsList.isLineageName;
import static search.SearchUtil.isMulticellularStructureByName;

/**
 * Top level container for the list of info window cell cases pages. This holds the tabpane of cases.
 */
public class InfoWindow {

    /** Wait time between the changing the number of ellipses shown during loading */
    private static final long WAIT_TIME_MILLI = 750;

    private Stage infoWindowStage;
    private TabPane tabPane;
    private Scene scene;
    private Stage parentStage;

    private InfoWindowLinkController linkController;
    private ProductionInfo productionInfo;
    private String nameToQuery;

    private SearchLayer searchLayer;

    private Service<Void> addNameService;
    private Service<Void> showLoadingService;

    // stages for various info windows
    private Stage cellShapesIndexStage;
    private Stage partsListStage;
    private Stage connectomeStage;
    private Stage cellDeathsStage;
    private Stage productionInfoStage;

    /** Used to show that loading is in progress */
    private int count;

    public InfoWindow(
            final Stage stage,
            final StringProperty cellNameProperty,
            final CasesLists casesLists,
            final ProductionInfo productionInfo,
            final Connectome connectome,
            final boolean defaultEmbryoFlag,
            final LineageData lineageData,
            final SearchLayer searchLayer) {

        infoWindowStage = new Stage();
        infoWindowStage.setTitle("Cell Info Window");

        this.productionInfo = requireNonNull(productionInfo);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(ALL_TABS);

        scene = new Scene(new Group());
        scene.setRoot(tabPane);

        this.searchLayer = requireNonNull(searchLayer);

        infoWindowStage.setScene(scene);

        infoWindowStage.setMinHeight(300);
        infoWindowStage.setMinWidth(600);
        infoWindowStage.setHeight(620);
        infoWindowStage.setWidth(950);

        infoWindowStage.setResizable(true);

        parentStage = stage;
        linkController = new InfoWindowLinkController(parentStage, searchLayer, cellNameProperty);

        count = 0;
        showLoadingService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        final int modulus = 5;
                        while (true) {
                            if (isCancelled()) {
                                infoWindowStage.setTitle("Cell Info Window");
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
                                    case 4:
                                        loading += "....";
                                        break;
                                    default:
                                        //loading = "Cell Info Window";
                                        break;
                                }
                                infoWindowStage.setTitle(loading);
                            });
                            try {
                                Thread.sleep(WAIT_TIME_MILLI);
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

        addNameService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                final Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        final String lineageName = nameToQuery;

                        if (!defaultEmbryoFlag
                                && !lineageData.isSulstonMode()) {
                            return null;

                        } else if (!defaultEmbryoFlag
                                && lineageData.isSulstonMode()
                                && nameToQuery.startsWith("Nuc")) {
                            return null;
                        }

                        if (lineageName != null && !lineageName.isEmpty()) {
                            if (casesLists == null) {
                                return null; // error check
                            }

                            if (isLineageName(lineageName)) {
                                // select tab if it already exists
                                if (casesLists.containsCellCase(lineageName)) {
                                    for (Tab tab : tabPane.getTabs()) {
                                        if (tab.getId().equalsIgnoreCase(getFunctionalNameByLineageName(lineageName))) {
                                            tabPane.getSelectionModel().select(tab);
                                        }
                                    }
                                } else {
                                    // if no tab exists, check default flag for image series info validation
                                    if (defaultEmbryoFlag) {
                                        String funcName = connectome.checkQueryCell(lineageName).toLowerCase();
                                        casesLists.makeTerminalCase(
                                                lineageName,
                                                funcName,
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        true,
                                                        false,
                                                        false,
                                                        false,
                                                        false),
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        false),
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        false,
                                                        false,
                                                        true,
                                                        false,
                                                        false),
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        false,
                                                        false,
                                                        false,
                                                        true,
                                                        false),
                                                productionInfo.getNuclearInfo(),
                                                productionInfo.getCellShapeData(funcName));
                                    } else {
                                        String funcName = connectome.checkQueryCell(lineageName).toUpperCase();
                                        casesLists.makeTerminalCase(
                                                lineageName,
                                                funcName,
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        true,
                                                        false,
                                                        false,
                                                        false,
                                                        false),
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        false),
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        false,
                                                        false,
                                                        true,
                                                        false,
                                                        false),
                                                connectome.queryConnectivity(
                                                        funcName,
                                                        false,
                                                        false,
                                                        false,
                                                        true,
                                                        false),
                                                new ArrayList<>(),
                                                new ArrayList<>());
                                    }

                                }

                            } else {
                                // if not a lineage name
                                // not in connectome --> non terminal case
                                if (casesLists.containsCellCase(lineageName)) {

                                    // show tab
                                } else {
                                    // add a non terminal case
                                    if (defaultEmbryoFlag) {
                                        casesLists.makeNonTerminalCase(
                                                lineageName,
                                                productionInfo.getNuclearInfo(),
                                                productionInfo.getCellShapeData(lineageName));
                                    } else {
                                        casesLists.makeNonTerminalCase(
                                                lineageName,
                                                new ArrayList<>(),
                                                new ArrayList<>());
                                    }

                                }
                            }
                        }
                        return null;
                    }
                };
                return task;
            }
        };

        addNameService.setOnScheduled(event -> showLoadingService.restart());
        addNameService.setOnSucceeded(event -> {
            showLoadingService.cancel();
            setInfoWindowTitle();
        });
        addNameService.setOnCancelled(event -> {
            showLoadingService.cancel();
            setInfoWindowTitle();
        });
        showLoadingService.setOnCancelled(event -> {
            showWindow();
            setInfoWindowTitle();
        });
    }

    private void setInfoWindowTitle() {
        runLater(() -> infoWindowStage.setTitle("Cell Info Window"));
    }

    public void addName(final String name) {
        if (name != null && !isMulticellularStructureByName(name)) {
            nameToQuery = name;
            addNameService.restart();
        }
    }

    public void showWindow() {
        if (infoWindowStage != null) {
            infoWindowStage.show();
            infoWindowStage.toFront();
        }
    }

    /**
     * Adds a tab for a cell case object
     *
     * @param cellCase
     *         the cell case to add
     */
    public void addTab(final Object cellCase) {
        final InfoWindowDOM dom;
        if (cellCase instanceof TerminalCellCase) {
            dom = new InfoWindowDOM(((TerminalCellCase) cellCase));
        } else if (cellCase instanceof NonTerminalCellCase) {
            dom = new InfoWindowDOM(((NonTerminalCellCase) cellCase));
        } else if (cellCase instanceof AmphidSensillaTerm) {
            dom = new InfoWindowDOM(((AmphidSensillaTerm) cellCase));
        } else {
            dom = new InfoWindowDOM();
        }

        runLater(() -> {
            final DraggableTab tab = new DraggableTab(dom.getName());
            tab.setId(dom.getName());
            tabPane.getTabs().add(0, tab); // prepend the tab

            final WebView webview = new WebView();
            webview.getEngine().loadContent(dom.DOMtoString());
            webview.setContextMenuEnabled(false);

            // link controller
            final JSObject window = (JSObject) webview.getEngine().executeScript("window");
            window.setMember("app", linkController);

            // link handler
            tab.setContent(webview);
            tabPane.getSelectionModel().select(tab); // show the new tab

            tab.setOnClosed(e -> {
                final Tab t = (Tab) e.getSource();
                String cellName = t.getId();
                searchLayer.removeCellCase(cellName);
            });

            tabPane.setFocusTraversable(true);
        });
    }

    public Stage getStage() {
        return infoWindowStage;
    }

    /**
     * Generate a window that contains the cell geometry information
     * Build a stage and webview, pass the data to the DOM generator and add the content, and show the stage
     *
     * @param sceneElementsList
     *         - the data to be rendered in the window
     */
    public void generateCellShapesIndexWindow(final List<SceneElement> sceneElementsList) {
        if (cellShapesIndexStage == null) {
            cellShapesIndexStage = new Stage();
            cellShapesIndexStage.setTitle("Cell Shapes Index");

            // webview to render cell shapes list i.e. sceneElementsList
            WebView cellShapesIndexWebView = new WebView();
            cellShapesIndexWebView.getEngine().loadContent(new InfoWindowDOM(sceneElementsList, true).DOMtoString());

            // link controller
            final JSObject window = (JSObject) cellShapesIndexWebView.getEngine().executeScript("window");
            window.setMember("app", linkController);
            
            VBox root = new VBox();
            root.getChildren().addAll(cellShapesIndexWebView);
            Scene scene = new Scene(new Group());
            scene.setRoot(root);

            cellShapesIndexStage.setScene(scene);
            cellShapesIndexStage.setResizable(true);
        }
        cellShapesIndexStage.show();
    }

    public void generatePartsListWindow() {
        if (partsListStage == null) {
            partsListStage = new Stage();
            partsListStage.setTitle("Parts List");

            // build webview scene to render parts list
            WebView partsListWebView = new WebView();
            partsListWebView.getEngine().loadContent(new InfoWindowDOM(
                    getFunctionalNames(),
                    getLineageNames(),
                    getDescriptions()).DOMtoString());

            VBox root = new VBox();
            root.getChildren().addAll(partsListWebView);
            Scene scene = new Scene(new Group());
            scene.setRoot(root);

            partsListStage.setScene(scene);
            partsListStage.setResizable(true);
        }
        partsListStage.show();

    }

    public void generateConnectomeWindow(List<NeuronalSynapse> synapses) {
        if (connectomeStage == null) {
            connectomeStage = new Stage();
            connectomeStage.setTitle("Connectome");

            // build webview scene to render html
            WebView connectomeHTML = new WebView();
            connectomeHTML.getEngine().loadContent(new InfoWindowDOM(synapses).DOMtoString());

            VBox root = new VBox();
            root.getChildren().addAll(connectomeHTML);
            Scene scene = new Scene(new Group());
            scene.setRoot(root);

            connectomeStage.setScene(scene);
            connectomeStage.setResizable(true);
        }
        connectomeStage.show();
    }

    public void generateCellDeathsWindow(Object[] cellDeaths) {
        if (cellDeathsStage == null) {
            cellDeathsStage = new Stage();
            cellDeathsStage.setWidth(400.);
            cellDeathsStage.setTitle("Cell Deaths");

            WebView cellDeathsWebView = new WebView();
            cellDeathsWebView.getEngine().loadContent(new InfoWindowDOM(cellDeaths).DOMtoString());

            VBox root = new VBox();
            root.getChildren().addAll(cellDeathsWebView);
            Scene scene = new Scene(new Group());
            scene.setRoot(root);

            cellDeathsStage.setScene(scene);
            cellDeathsStage.setResizable(true);
        }
        cellDeathsStage.show();

    }

    public void generateProductionInfoWindow() {
        if (productionInfoStage == null) {
            productionInfoStage = new Stage();
            productionInfoStage.setTitle("Experimental Data");

            final WebView productionInfoWebView = new WebView();
            productionInfoWebView.getEngine().loadContent(new InfoWindowDOM(productionInfo).DOMtoString());
            productionInfoWebView.setContextMenuEnabled(false);
            
            // link controller
            final JSObject window = (JSObject) productionInfoWebView.getEngine().executeScript("window");
            window.setMember("app", linkController);

            final VBox root = new VBox();
            root.getChildren().addAll(productionInfoWebView);
            final Scene scene = new Scene(new Group());
            scene.setRoot(root);

            productionInfoStage.setScene(scene);
            productionInfoStage.setResizable(true);
        }
        productionInfoStage.show();
    }
}