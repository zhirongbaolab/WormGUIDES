/*
 * Bao Lab 2017
 */

package wormguides.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import javafx.stage.Stage;

import acetree.LineageData;
import connectome.Connectome;
import partslist.PartsList;
import partslist.celldeaths.CellDeaths;
import wormguides.MainApp;
import wormguides.layers.DisplayLayer;
import wormguides.layers.SearchLayer;
import wormguides.layers.StoriesLayer;
import wormguides.layers.StructuresLayer;
import wormguides.loaders.ImageLoader;
import wormguides.models.LineageTree;
import wormguides.models.cellcase.CasesLists;
import wormguides.models.colorrule.Rule;
import wormguides.models.subscenegeometry.SceneElementsList;
import wormguides.models.subscenegeometry.StructureTreeNode;
import wormguides.resources.ProductionInfo;
import wormguides.stories.Story;
import wormguides.util.ColorHash;
import wormguides.util.StringCellFactory;
import wormguides.util.subsceneparameters.Parameters;
import wormguides.view.DraggableTab;
import wormguides.view.infowindow.InfoWindow;
import wormguides.view.popups.AboutPane;
import wormguides.view.popups.StorySavePane;
import wormguides.view.popups.SulstonTreePane;
import wormguides.view.urlwindow.URLLoadWarningDialog;
import wormguides.view.urlwindow.URLLoadWindow;
import wormguides.view.urlwindow.URLShareWindow;

import static java.lang.System.lineSeparator;
import static java.time.Duration.between;
import static java.util.Collections.sort;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.Cursor.DEFAULT;
import static javafx.scene.Cursor.HAND;
import static javafx.scene.SceneAntialiasing.BALANCED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.GRAY;
import static javafx.scene.paint.Color.web;
import static javafx.stage.Modality.NONE;
import static javafx.stage.StageStyle.UNDECORATED;

import static acetree.tablelineagedata.AceTreeTableLineageDataLoader.getAvgXOffsetFromZero;
import static acetree.tablelineagedata.AceTreeTableLineageDataLoader.getAvgYOffsetFromZero;
import static acetree.tablelineagedata.AceTreeTableLineageDataLoader.getAvgZOffsetFromZero;
import static acetree.tablelineagedata.AceTreeTableLineageDataLoader.loadNucFiles;
import static acetree.tablelineagedata.AceTreeTableLineageDataLoader.setOriginToZero;
import static partslist.PartsList.getFunctionalNameByLineageName;
import static partslist.celldeaths.CellDeaths.isInCellDeaths;
import static search.SearchUtil.getStructureComment;
import static search.SearchUtil.isMulticellularStructureByName;
import static search.SearchUtil.isStructureWithComment;
import static wormguides.util.colorurl.UrlParser.processUrl;

/**
 * Controller for RootLayout.fxml that contains all GUI components of the main WormGUIDES application window
 */
public class RootLayoutController extends BorderPane implements Initializable {

    private static final String UNLINEAGED_START = "Nuc";
    private static final String ROOT = "ROOT";
    private static final String FILL_COLOR_HEX = "#272727";

    // Panels stuff
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private VBox displayVBox;
    @FXML
    private AnchorPane modelAnchorPane;
    @FXML
    private ScrollPane infoPane;
    @FXML
    private HBox sceneControlsBox;

    // Subscene controls
    @FXML
    private Button backwardButton,
            forwardButton,
            playButton;
    @FXML
    private Label timeLabel,
            totalNucleiLabel;
    @FXML
    private Slider timeSlider;
    @FXML
    private Button zoomInButton,
            zoomOutButton;

    // Tab stuff
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab storiesTab;
    @FXML
    private Tab colorAndDisplayTab;
    @FXML
    private TabPane colorAndDisplayTabPane;
    @FXML
    private Tab cellsTab;
    @FXML
    private Tab structuresTab;
    @FXML
    private Tab displayTab;

    // Search stuff
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> searchResultsListView;
    @FXML
    private RadioButton sysRadioBtn,
            funRadioBtn,
            desRadioBtn,
            genRadioBtn,
            conRadioBtn,
            multiRadioBtn;
    @FXML
    private CheckBox cellNucleusCheckBox,
            cellBodyCheckBox,
            ancestorCheckBox,
            descendantCheckBox;
    @FXML
    private Label descendantLabel;
    @FXML
    private AnchorPane colorPickerPane;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button addSearchBtn;
    @FXML
    private CheckBox presynapticCheckBox,
            postsynapticCheckBox,
            electricalCheckBox,
            neuromuscularCheckBox;
    @FXML
    private ListView<Rule> rulesListView;
    @FXML
    private CheckBox uniformSizeCheckBox;
    @FXML
    private Button clearAllLabelsButton;
    @FXML
    private Slider opacitySlider;

    // Structures tab
    private StructuresLayer structuresLayer;
    @FXML
    private TextField structuresSearchField;
    @FXML
    private ListView<String> structuresSearchListView;
    @FXML
    private TreeView<StructureTreeNode> allStructuresTreeView;
    @FXML
    private Button addStructureRuleBtn;
    @FXML
    private ColorPicker structureRuleColorPicker;

    // Cell information panel
    @FXML
    private Text displayedName;
    @FXML
    private Text moreInfoClickableText;
    @FXML
    private Text displayedDescription;
    @FXML
    private Text displayedStory;
    @FXML
    private Text displayedStoryDescription;

    // Stories tab
    @FXML
    private ListView<Story> storiesListView;
    @FXML
    private Button editNoteButton;
    @FXML
    private Button newStoryButton;
    @FXML
    private Button deleteStoryButton;

    // Movie capture stuff
    @FXML
    private MenuItem captureVideoMenuItem;
    @FXML
    private MenuItem stopCaptureVideoMenuItem;

    // Root layout's own stage (the main application stage)
    private Stage mainStage;

    // Other windows
    private Stage aboutStage;
    private Stage sulstonTreeStage;
    private Stage urlDisplayStage;
    private Stage urlLoadStage;
    private Stage rotationControllerStage;
    private Stage contextMenuStage;
    private Popup exitSavePopup;

    // URL generation/loading
    private URLShareWindow urlShareWindow;
    private URLLoadWindow urlLoadWindow;
    private URLLoadWarningDialog warning;

    private RotationController rotationController;

    private Window3DController window3DController;
    private DoubleProperty subsceneWidth;
    private DoubleProperty subsceneHeight;

    private SearchLayer searchLayer;

    private Connectome connectome;

    private StoriesLayer storiesLayer;

    private SceneElementsList sceneElementsList;

    private DisplayLayer displayLayer;

    private ProductionInfo productionInfo;

    // Info window stuff
    private CasesLists casesLists;
    private InfoWindow infoWindow;
    private ImageView playIcon, pauseIcon;

    // Lineage tree stuff
    private TreeItem<String> lineageTreeRoot;
    private LineageData lineageData;

    // Shared properties
    /** Name that appears in the info panel */
    private StringProperty selectedEntityNameProperty;
    private StringProperty selectedNameLabeledProperty;
    private StringProperty activeStoryProperty;
    private BooleanProperty geneResultsUpdatedFlag;
    private BooleanProperty usingInternalRulesFlag;
    private BooleanProperty bringUpInfoFlag;
    private BooleanProperty playingMovieFlag;
    private BooleanProperty capturingVideoFlag;
    private BooleanProperty cellClickedFlag;
    private BooleanProperty rebuildSubsceneFlag;
    private IntegerProperty timeProperty;
    private IntegerProperty totalNucleiProperty;
    private DoubleProperty rotateXAngleProperty;
    private DoubleProperty rotateYAngleProperty;
    private DoubleProperty rotateZAngleProperty;
    private DoubleProperty translateXProperty;
    private DoubleProperty translateYProperty;
    private DoubleProperty zoomProperty;
    private DoubleProperty othersOpacityProperty;

    // Other shared variables
    private ObservableList<Rule> rulesList;
    private ObservableList<String> searchResultsList;
    private int startTime;
    private int endTime;
    private int movieTimeOffset;
    private boolean defaultEmbryoFlag;
    private Service<Void> searchResultsUpdateService;
    private ContextMenuController contextMenuController;
    private ColorHash colorHash;
    private SubScene subscene;
    private Group rootEntitiesGroup;

    @FXML
    public void menuLoadStory() {
        if (storiesLayer != null) {
            storiesLayer.loadStory();
        }
    }

    @FXML
    public void menuSaveStory() {
        storiesLayer.saveActiveStory();
    }

    @FXML
    public void menuSaveImageAction() {
        window3DController.stillscreenCapture();
    }

    @FXML
    public void menuCloseAction() {
        initCloseApplication();
    }

    @FXML
    public void menuAboutAction() {
        if (aboutStage == null) {
            aboutStage = new Stage();
            aboutStage.setScene(new Scene(new AboutPane()));
            aboutStage.setTitle("About WormGUIDES");
            aboutStage.initModality(NONE);
            aboutStage.setHeight(400.0);
            aboutStage.setWidth(300.0);
            aboutStage.setResizable(false);
        }
        aboutStage.show();
    }

    @FXML
    public void viewTreeAction() {
        if (sulstonTreeStage == null) {
            sulstonTreeStage = new Stage();
            final SulstonTreePane treePane = new SulstonTreePane(
                    sulstonTreeStage,
                    searchLayer,
                    lineageData,
                    movieTimeOffset,
                    lineageTreeRoot,
                    rulesList,
                    colorHash,
                    timeProperty,
                    contextMenuStage,
                    contextMenuController,
                    selectedNameLabeledProperty,
                    rebuildSubsceneFlag,
                    defaultEmbryoFlag);
            sulstonTreeStage.setScene(new Scene(treePane));
            sulstonTreeStage.setTitle("LineageTree");
            sulstonTreeStage.initModality(NONE);
            sulstonTreeStage.show();
            treePane.addDrawing();
            mainStage.show();
        } else {
            sulstonTreeStage.show();
            runLater(() -> ((Stage) sulstonTreeStage.getScene().getWindow()).toFront());
        }
    }

    @FXML
    public void generateURLAction() {
        if (urlDisplayStage == null) {
            urlDisplayStage = new Stage();
            urlShareWindow = new URLShareWindow(
                    rulesList,
                    timeProperty,
                    rotateXAngleProperty,
                    rotateYAngleProperty,
                    rotateZAngleProperty,
                    translateXProperty,
                    translateYProperty,
                    zoomProperty,
                    othersOpacityProperty);
            urlShareWindow.getCloseButton().setOnAction(event -> urlDisplayStage.hide());
            urlDisplayStage.setScene(new Scene(urlShareWindow));
            urlDisplayStage.setTitle("Share Scene");
            urlDisplayStage.setResizable(false);
            urlDisplayStage.initModality(NONE);
        }
        urlShareWindow.resetURLs();
        urlDisplayStage.show();
    }

    @FXML
    public void loadURLAction() {
        if (urlLoadStage == null) {
            urlLoadStage = new Stage();

            urlLoadWindow = new URLLoadWindow();
            urlLoadWindow.getLoadButton().setOnAction(event -> {
                if (warning == null) {
                    warning = new URLLoadWarningDialog();
                }
                if (!warning.doNotShowAgain()) {
                    final Optional<ButtonType> result = warning.showAndWait();
                    if (result.get() == warning.getButtonTypeOkay()) {
                        urlLoadStage.hide();
                        processUrl(
                                urlLoadWindow.getInputURL(),
                                rulesList,
                                searchLayer,
                                timeProperty,
                                rotateXAngleProperty,
                                rotateYAngleProperty,
                                rotateZAngleProperty,
                                translateXProperty,
                                translateYProperty,
                                zoomProperty,
                                othersOpacityProperty,
                                rebuildSubsceneFlag);
                    }
                } else {
                    urlLoadStage.hide();
                    processUrl(
                            urlLoadWindow.getInputURL(),
                            rulesList,
                            searchLayer,
                            timeProperty,
                            rotateXAngleProperty,
                            rotateYAngleProperty,
                            rotateZAngleProperty,
                            translateXProperty,
                            translateYProperty,
                            zoomProperty,
                            othersOpacityProperty,
                            rebuildSubsceneFlag);
                }
            });
            urlLoadWindow.getCancelButton().setOnAction(event -> urlLoadStage.hide());

            urlLoadStage.setScene(new Scene(urlLoadWindow));
            urlLoadStage.setTitle("Load Scene");
            urlLoadStage.setResizable(false);
            urlLoadStage.initModality(NONE);
        }

        urlLoadWindow.clearField();
        urlLoadStage.show();
    }

    @FXML
    public void saveSearchResultsAction() {
        final ObservableList<String> items = searchResultsListView.getItems();
        if (!(items.size() > 0)) {
            System.out.println("no searchLayer results to write to file");
        }

        final Stage fileChooserStage = new Stage();

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save Location");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("TXT File", "*.txt"));
        try {
            final File output = fileChooser.showSaveDialog(fileChooserStage);
            // check
            if (output == null) {
                System.out.println("error creating file to write searchLayer results");
                return;
            }
            // create the header line that will format the search criteria corresponding to these search results
            String searchType = "";
            if (sysRadioBtn.isSelected()) {
            	searchType = "Lineage Name";
            } else if (funRadioBtn.isSelected()) {
            	searchType = "Function Name";
            } else if (desRadioBtn.isSelected()) {
            	searchType = "PartsList Desciption";
            } else if (genRadioBtn.isSelected()) {
            	searchType = "Gene";
            } else if (conRadioBtn.isSelected()) {
            	searchType = "Connectome - ";
            	if (presynapticCheckBox.isSelected()) {
            		searchType += "pre-synaptic, ";
            	}
            	if (postsynapticCheckBox.isSelected()) {
            		searchType += "post-synaptic, ";
            	}
            	if (electricalCheckBox.isSelected()) {
            		searchType += "electrical, ";
            	}
            	if (neuromuscularCheckBox.isSelected()) {
            		searchType += "neuromuscular";
            	}
            	if (searchType.substring(searchType.length()-2).equals(", ")) {
            		searchType = searchType.substring(0, searchType.length()-2);
            	}
            } else if (multiRadioBtn.isSelected()) {
            	searchType = "Multicellular Structure";
            }

            String searchOptions = "";

            if (ancestorCheckBox.isSelected() && descendantCheckBox.isSelected()) {
            	searchOptions = "ancestors, descdendants";
            } else if (ancestorCheckBox.isSelected() && !descendantCheckBox.isSelected()) {
            	searchOptions = "ancestors";
            } else if (!ancestorCheckBox.isSelected() && descendantCheckBox.isSelected()) {
            	searchOptions = "descendants";
            }

            String searchCriteria = "'" + searchField.getText() + "' (Options: " + searchType;
            if (!searchOptions.isEmpty()) {
            	searchCriteria += ", " + searchOptions;
            }
            searchCriteria += ")";

            final FileWriter writer = new FileWriter(output);

            // write header line to file
            writer.write(searchCriteria);
            writer.write(lineSeparator());

            for (String s : items) {
                writer.write(s);
                writer.write(lineSeparator());
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            System.out.println("IOException thrown writing searchLayer results to file");
        }
    }

    @FXML
    public void openInfoWindow() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.showWindow();
    }

    // START View->Primary Data menu items
    @FXML
    public void viewCellShapesIndex() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.generateCellShapesIndexWindow(sceneElementsList.getElementsList());
    }

    @FXML
    public void viewPartsList() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.generatePartsListWindow();
    }

    @FXML
    public void viewConnectome() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.generateConnectomeWindow(connectome.getSynapseList());
    }

    @FXML
    public void viewCellDeaths() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.generateCellDeathsWindow(CellDeaths.getCellDeathsAsArray());
    }

    @FXML
    public void productionInfoAction() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.generateProductionInfoWindow();
    }
    // END View->Primary Data menu items

    @FXML
    public void openRotationController() {
        if (rotationControllerStage == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/layouts/RotationControllerLayout.fxml"));

            if (rotationController == null) {
                rotationController = new RotationController(
                        rotateXAngleProperty,
                        rotateYAngleProperty,
                        rotateZAngleProperty);
            }

            rotationControllerStage = new Stage();

            loader.setController(rotationController);

            try {
                rotationControllerStage.setScene(new Scene(loader.load()));

                rotationControllerStage.setTitle("Rotation Controller");
                rotationControllerStage.initOwner(mainStage);
                rotationControllerStage.initModality(NONE);
                rotationControllerStage.setResizable(true);

            } catch (IOException e) {
                System.out.println("error in initializing note editor.");
                e.printStackTrace();
            }
        }

        rotationControllerStage.show();
        rotationControllerStage.toFront();
    }

    @FXML
    public void captureVideo() {
        captureVideoMenuItem.setDisable(true);
        stopCaptureVideoMenuItem.setDisable(false);
        // start the image capture
        if (window3DController != null) {
            if (!window3DController.captureImagesForMovie()) {
                // error saving movie, update UI
                captureVideoMenuItem.setDisable(false);
                stopCaptureVideoMenuItem.setDisable(true);
                capturingVideoFlag.set(false);
            }
        }
    }

    @FXML
    public void stopCaptureAndSave() {
        captureVideoMenuItem.setDisable(false);
        stopCaptureVideoMenuItem.setDisable(true);
        capturingVideoFlag.set(false);
        // convert captured images to movie
        if (window3DController != null) {
            window3DController.convertImagesToMovie();
        }

    }

    public void initCloseApplication() {
        // check if there is an active story to prompt save dialog
        if (storiesLayer.getActiveStory() != null) {
            promptStorySave();
        } else {
            exitApplication();
        }
    }

    private void promptStorySave() {
        if (exitSavePopup == null) {
            // create handlers for yes, no and cancel buttons
            final EventHandler<ActionEvent> yesHandler = event -> {
                exitSavePopup.hide();
                if (storiesLayer.saveActiveStory()) {
                    exitApplication();
                }
            };
            final EventHandler<ActionEvent> noHandler = event -> {
                exitSavePopup.hide();
                exitApplication();
            };
            final EventHandler<ActionEvent> cancelHandler = event -> exitSavePopup.hide();

            exitSavePopup = new Popup();
            exitSavePopup.getContent().add(new StorySavePane(
                    yesHandler,
                    noHandler,
                    cancelHandler));
            // position dialog on screen
            exitSavePopup.setAutoFix(true);
        }

        exitSavePopup.show(mainStage);
        exitSavePopup.centerOnScreen();
    }

    private void exitApplication() {
        System.out.println("Exiting...");
        if (!defaultEmbryoFlag) {
            sulstonTreeStage.hide();
            mainStage.hide();
            return;
        }
        System.exit(0);
    }

    private void initWindow3DController() {
        final double[] xyzScale = lineageData.getXYZScale();
        window3DController = new Window3DController(
                mainStage,
                rootEntitiesGroup,
                subscene,
                modelAnchorPane,
                lineageData,
                casesLists,
                productionInfo,
                connectome,
                sceneElementsList,
                storiesLayer,
                searchLayer,
                bringUpInfoFlag,
                getAvgXOffsetFromZero(),
                getAvgYOffsetFromZero(),
                getAvgZOffsetFromZero(),
                defaultEmbryoFlag,
                xyzScale[0],
                xyzScale[1],
                xyzScale[2],
                modelAnchorPane,
                backwardButton,
                forwardButton,
                zoomOutButton,
                zoomInButton,
                clearAllLabelsButton,
                searchField,
                opacitySlider,
                uniformSizeCheckBox,
                cellNucleusCheckBox,
                cellBodyCheckBox,
                multiRadioBtn,
                startTime,
                endTime,
                timeProperty,
                totalNucleiProperty,
                zoomProperty,
                othersOpacityProperty,
                rotateXAngleProperty,
                rotateYAngleProperty,
                rotateZAngleProperty,
                translateXProperty,
                translateYProperty,
                selectedEntityNameProperty,
                selectedNameLabeledProperty,
                cellClickedFlag,
                playingMovieFlag,
                geneResultsUpdatedFlag,
                rebuildSubsceneFlag,
                rulesList,
                colorHash,
                contextMenuStage,
                contextMenuController,
                searchResultsUpdateService,
                searchResultsList);

        timeProperty.addListener((observable, oldValue, newValue) -> {
            timeSlider.setValue(timeProperty.get());
            if (timeProperty.get() >= endTime - 1) {
                playButton.setGraphic(playIcon);
                playingMovieFlag.set(false);
            }
        });
        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            final int value = newValue.intValue();
            if (value != oldValue.intValue()) {
                timeProperty.set(value);
                rebuildSubsceneFlag.set(true);
            }
        });

        // initial start at movie end (builds subscene automatically)
        timeProperty.set(endTime);
    }

    public void setStage(final Stage stage) {
        mainStage = stage;
    }

    private void addListeners() {
        // searchLayer stuff
        searchResultsListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> selectedEntityNameProperty.set(newValue));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                mainTabPane.getSelectionModel().select(colorAndDisplayTab);
                colorAndDisplayTabPane.getSelectionModel().select(cellsTab);
            }
        });

        // selectedName string property that has the name of the clicked sphere
        selectedEntityNameProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                setSelectedEntityInfo(selectedEntityNameProperty.get());
            }
        });

        // Disable click on structures search results list view
        structuresSearchListView.addEventFilter(MOUSE_PRESSED, Event::consume);

        // Modify font for string list/tree cells
        structuresSearchListView.setCellFactory(new StringCellFactory.StringListCellFactory());
        searchResultsListView.setCellFactory(new StringCellFactory.StringListCellFactory());

        // More info clickable text
        moreInfoClickableText.setOnMouseClicked(event -> {
            if (infoWindow == null) {
                initInfoWindow();
            }
            infoWindow.addName(selectedEntityNameProperty.get());
            openInfoWindow();
        });
        moreInfoClickableText.setOnMouseEntered(event -> moreInfoClickableText.setCursor(HAND));
        moreInfoClickableText.setOnMouseExited(event -> moreInfoClickableText.setCursor(DEFAULT));

        // More info in context menu
        bringUpInfoFlag.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (infoWindow == null) {
                    initInfoWindow();
                }
                infoWindow.addName(selectedEntityNameProperty.get());
                openInfoWindow();
            }
        });
    }

    private void setSelectedEntityInfo(String name) {
        if (name == null || name.isEmpty()) {
            displayedName.setText("Active Cell: none");
            moreInfoClickableText.setVisible(false);
            displayedDescription.setText("");
            return;
        }

        if (name.contains("(")) {
            name = name.substring(0, name.indexOf("("));
        }
        name = name.trim();

        displayedName.setText("Active Cell: " + name);
        moreInfoClickableText.setVisible(true);
        if (isMulticellularStructureByName(name)) {
            moreInfoClickableText.setDisable(true);
            moreInfoClickableText.setFill(GRAY);
        } else {
            moreInfoClickableText.setDisable(false);
            moreInfoClickableText.setFill(BLACK);
        }

        displayedDescription.setText("");
        // Note
        displayedDescription.setText(storiesLayer.getNoteComments(name));
        // Cell body/structue
        if (isStructureWithComment(name)) {
            displayedDescription.setText(getStructureComment(name));
        }
        // Cell lineage name
        else {
            String functionalName = getFunctionalNameByLineageName(name);
            if (functionalName != null) {
                displayedName.setText("Active Cell: " + name + " (" + functionalName + ")");
                displayedDescription.setText(PartsList.getDescriptionByFunctionalName(functionalName));
            } else if (isInCellDeaths(name)) {
                displayedName.setText("Active Cell: " + name);
                displayedDescription.setText("Cell Death");
            }
        }
    }

    /**
     * Binds the subscene width and height to those of its parent anchor pane
     */
    private void sizeSubscene() {
        subsceneWidth = new SimpleDoubleProperty();
        subsceneWidth.bind(modelAnchorPane.widthProperty());
        subsceneHeight = new SimpleDoubleProperty();
        subsceneHeight.bind(modelAnchorPane.heightProperty());

        setTopAnchor(subscene, 0.0);
        setLeftAnchor(subscene, 0.0);
        setRightAnchor(subscene, 0.0);
        setBottomAnchor(subscene, 0.0);

        subscene.widthProperty().bind(subsceneWidth);
        subscene.heightProperty().bind(subsceneHeight);
        subscene.setManaged(false);
    }

    /**
     * Binds the widths and heights of components in the information panel below the subscene so that it scales nicely
     */
    private void sizeInfoPane() {
        infoPane.prefHeightProperty().bind(displayVBox.heightProperty().divide(6.5));
        displayedDescription.wrappingWidthProperty().bind(infoPane.widthProperty().subtract(15));
        displayedStory.wrappingWidthProperty().bind(infoPane.widthProperty().subtract(15));
        displayedStoryDescription.wrappingWidthProperty().bind(infoPane.widthProperty().subtract(15));
    }

    /**
     * Sets the appropriate labels for movie timeProperty and number of nuclei in a timeProperty frame
     */
    private void setLabels() {
        timeProperty.addListener((observable, oldValue, newValue) -> {
            if (defaultEmbryoFlag) {
                timeLabel.setText("~" + (newValue.intValue() + movieTimeOffset) + " min p.f.c.");
            } else {
                timeLabel.setText("~" + newValue.intValue() + " min");
            }
        });
        timeLabel.setText("~" + (timeProperty.get() + movieTimeOffset) + " min p.f.c.");
        timeLabel.toFront();

        totalNucleiProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                totalNucleiLabel.setText(newValue.intValue() + " Nucleus");
            } else {
                totalNucleiLabel.setText(newValue.intValue() + " Nuclei");
            }
        });
        totalNucleiLabel.setText(totalNucleiProperty.get() + " Nuclei");
        totalNucleiLabel.toFront();
    }

    /**
     * Sets the icons for the GUI buttons
     */
    private void setIcons() {
        backwardButton.setGraphic(ImageLoader.getBackwardIcon());
        forwardButton.setGraphic(ImageLoader.getForwardIcon());
        zoomInButton.setGraphic(new ImageView(ImageLoader.getPlusIcon()));
        zoomOutButton.setGraphic(new ImageView(ImageLoader.getMinusIcon()));

        playIcon = ImageLoader.getPlayIcon();
        pauseIcon = ImageLoader.getPauseIcon();
        playButton.setGraphic(playIcon);
        playButton.setOnAction(event -> {
            playingMovieFlag.set(!playingMovieFlag.get());
            if (playingMovieFlag.get()) {
                playButton.setGraphic(pauseIcon);
            } else {
                playButton.setGraphic(playIcon);
            }
        });
    }

    private void setSlidersProperties() {
        timeSlider.setMin(startTime);
        timeSlider.setMax(endTime);

        opacitySlider.setMin(0);
        opacitySlider.setMax(100);
    }

    private void initSearchLayer() {
        cellNucleusCheckBox.setSelected(true);
        searchLayer = new SearchLayer(
                rulesList,
                searchResultsList,
                searchField,
                sysRadioBtn,
                funRadioBtn,
                desRadioBtn,
                genRadioBtn,
                conRadioBtn,
                multiRadioBtn,
                descendantLabel,
                presynapticCheckBox,
                postsynapticCheckBox,
                neuromuscularCheckBox,
                electricalCheckBox,
                cellNucleusCheckBox,
                cellBodyCheckBox,
                ancestorCheckBox,
                descendantCheckBox,
                colorPicker,
                addSearchBtn,
                geneResultsUpdatedFlag,
                rebuildSubsceneFlag);
        searchResultsListView.setItems(searchResultsList);
        searchLayer.addDefaultInternalColorRules();
        searchResultsUpdateService = searchLayer.getResultsUpdateService();
    }

    private void initDisplayLayer() {
        displayLayer = new DisplayLayer(rulesList, usingInternalRulesFlag, rebuildSubsceneFlag);
        rulesListView.setItems(rulesList);
        rulesListView.setCellFactory(displayLayer.getRuleCellFactory());
    }

    private void initLineageTree(final List<String> allCellNames) {
        if (!defaultEmbryoFlag) {
            // remove unlineaged cells
            for (int i = 0; i < allCellNames.size(); i++) {
                if (allCellNames.get(i).toLowerCase().startsWith(UNLINEAGED_START.toLowerCase())
                        || allCellNames.get(i).toLowerCase().startsWith(ROOT.toLowerCase())) {
                    allCellNames.remove(i--);
                }
            }
            //sort the lineage names that remain
            sort(allCellNames);
        }
        final LineageTree lineageTree = new LineageTree(
                allCellNames.toArray(new String[allCellNames.size()]),
                lineageData.isSulstonMode());
        lineageTreeRoot = lineageTree.getRoot();
    }

    private void initStructuresLayer() {
        structuresLayer = new StructuresLayer(
                searchLayer,
                sceneElementsList,
                selectedEntityNameProperty,
                structuresSearchField,
                structuresSearchListView,
                allStructuresTreeView,
                addStructureRuleBtn,
                structureRuleColorPicker,
                rebuildSubsceneFlag);
    }

    private void initStoriesLayer() {
        storiesLayer = new StoriesLayer(
                mainStage,
                searchLayer,
                sceneElementsList,
                storiesListView,
                rulesList,
                selectedEntityNameProperty,
                activeStoryProperty,
                cellClickedFlag,
                timeProperty,
                rotateXAngleProperty,
                rotateYAngleProperty,
                rotateZAngleProperty,
                translateXProperty,
                translateYProperty,
                zoomProperty,
                othersOpacityProperty,
                usingInternalRulesFlag,
                rebuildSubsceneFlag,
                lineageData,
                newStoryButton,
                deleteStoryButton,
                editNoteButton,
                startTime,
                endTime,
                movieTimeOffset,
                defaultEmbryoFlag);

        displayedStory.setText("Active Story: " + storiesLayer.getActiveStory().getTitle());
        displayedStoryDescription.setText(storiesLayer.getActiveStoryDescription());

        storiesLayer.getActiveStoryProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                displayedStory.setText("Active Story: none");
                displayedStoryDescription.setText("");
            } else {
                displayedStory.setText("Active Story: " + newValue);
                displayedStoryDescription.setText(storiesLayer.getActiveStoryDescription());
            }
        });
    }

    private void initInfoWindow() {
        infoWindow = new InfoWindow(
                mainStage,
                selectedNameLabeledProperty,
                casesLists,
                productionInfo,
                connectome,
                defaultEmbryoFlag,
                lineageData,
                searchLayer);
        casesLists.setInfoWindow(infoWindow);
    }

    /**
     * Replaces all application tabs with dockable ones
     *
     * @see DraggableTab
     */
    private void replaceTabsWithDraggableTabs() {
        final DraggableTab cellsDragTab = new DraggableTab(cellsTab.getText());
        cellsDragTab.setCloseable(false);
        cellsDragTab.setContent(cellsTab.getContent());

        final DraggableTab structuresDragTab = new DraggableTab(structuresTab.getText());
        structuresDragTab.setCloseable(false);
        structuresDragTab.setContent(structuresTab.getContent());

        final DraggableTab displayDragTab = new DraggableTab(displayTab.getText());
        displayDragTab.setCloseable(false);
        displayDragTab.setContent(displayTab.getContent());

        colorAndDisplayTabPane.getTabs().clear();
        cellsTab = cellsDragTab;
        structuresTab = structuresDragTab;
        displayTab = displayDragTab;

        colorAndDisplayTabPane.getTabs().addAll(cellsTab, structuresTab, displayTab);

        final DraggableTab storiesDragTab = new DraggableTab(storiesTab.getText());
        storiesDragTab.setCloseable(false);
        storiesDragTab.setContent(storiesTab.getContent());

        final DraggableTab colorAndDisplayDragTab = new DraggableTab(colorAndDisplayTab.getText());
        colorAndDisplayDragTab.setCloseable(false);
        colorAndDisplayDragTab.setContent(colorAndDisplayTab.getContent());

        mainTabPane.getTabs().clear();
        storiesTab = storiesDragTab;
        colorAndDisplayTab = colorAndDisplayDragTab;

        mainTabPane.getTabs().addAll(storiesTab, colorAndDisplayTab);
        mainTabPane.toFront();
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        productionInfo = new ProductionInfo();

        casesLists = new CasesLists();

        if (bundle != null) {
            lineageData = (LineageData) bundle.getObject("lineageData");
            defaultEmbryoFlag = false;
            setOriginToZero(lineageData, defaultEmbryoFlag);
        } else {
            // takes about 2800ms (dictates noticeable part of startup time)
            final Instant start = Instant.now();
            lineageData = loadNucFiles(productionInfo);
            final Instant end = Instant.now();
            System.out.println("Nuc files loaded in " + between(start, end).toMillis() + "ms");
            defaultEmbryoFlag = true;
            lineageData.setIsSulstonModeFlag(productionInfo.getIsSulstonFlag());
        }
        
        // set values based on default vs. other model
        if (defaultEmbryoFlag) {
            startTime = productionInfo.getDefaultStartTime();
            movieTimeOffset = productionInfo.getMovieTimeOffset();
        } else {
            startTime = 0;
            movieTimeOffset = 0;
        }
        
        endTime = lineageData.getNumberOfTimePoints() - 1;
        movieTimeOffset = productionInfo.getMovieTimeOffset();

        // takes about 58ms
        replaceTabsWithDraggableTabs();

        // takes about 10ms
        PartsList.init();

        // takes about 6ms
        CellDeaths.init();

        Parameters.init();

        initSharedVariables();

        // takes about 3ms
        initDisplayLayer();

        setSlidersProperties();

        mainTabPane.getSelectionModel().select(storiesTab);

        // takes about 1050ms
        initializeWithLineageData();
    }

    private void initializeWithLineageData() {
        // takes about 65ms
        initLineageTree(lineageData.getAllCellNames());

        // takes ~170ms
        sceneElementsList = new SceneElementsList(lineageData);

        // takes ~20ms
        connectome = new Connectome();

        // takes ~140ms
        initSearchLayer();
        searchLayer.initDatabases(lineageData, sceneElementsList, connectome, casesLists, productionInfo);

        // takes ~120ms
        initStoriesLayer();

        // takes ~5ms
        initStructuresLayer();

        initContextMenuStage();

        addListeners();

        setIcons();

        sizeSubscene();
        sizeInfoPane();

        // takes ~700ms
        viewTreeAction();

        // takes ~50ms
        initWindow3DController();

        setLabels();
    }

    private void initSharedVariables() {
        timeProperty = new SimpleIntegerProperty(startTime);
        totalNucleiProperty = new SimpleIntegerProperty(0);

        othersOpacityProperty = new SimpleDoubleProperty(1.0);
        rotateXAngleProperty = new SimpleDoubleProperty();
        rotateYAngleProperty = new SimpleDoubleProperty();
        rotateZAngleProperty = new SimpleDoubleProperty();
        translateXProperty = new SimpleDoubleProperty();
        translateYProperty = new SimpleDoubleProperty();
        zoomProperty = new SimpleDoubleProperty();

        selectedEntityNameProperty = new SimpleStringProperty("");
        selectedNameLabeledProperty = new SimpleStringProperty("");
        activeStoryProperty = new SimpleStringProperty("");

        cellClickedFlag = new SimpleBooleanProperty(false);
        geneResultsUpdatedFlag = new SimpleBooleanProperty(false);

        rebuildSubsceneFlag = new SimpleBooleanProperty(false);
        usingInternalRulesFlag = new SimpleBooleanProperty(true);
        bringUpInfoFlag = new SimpleBooleanProperty(false);
        playingMovieFlag = new SimpleBooleanProperty(false);
        capturingVideoFlag = new SimpleBooleanProperty(false);

        colorHash = new ColorHash();
        rootEntitiesGroup = new Group();
        subscene = new SubScene(
                rootEntitiesGroup,
                mainStage.widthProperty().get(),
                mainStage.heightProperty().get(),
                true,
                BALANCED);
        subscene.setFill(web(FILL_COLOR_HEX));

        rulesList = observableArrayList();
        searchResultsList = observableArrayList();
    }

    private void initContextMenuStage() {
        if (contextMenuStage == null) {
            contextMenuStage = new Stage();
            contextMenuStage.initStyle(UNDECORATED);

            contextMenuController = new ContextMenuController(
                    mainStage,
                    contextMenuStage,
                    searchLayer,
                    casesLists,
                    productionInfo,
                    connectome,
                    bringUpInfoFlag);

            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/layouts/ContextMenuLayout.fxml"));
            loader.setController(contextMenuController);
            loader.setRoot(contextMenuController);

            try {
                contextMenuStage.setScene(new Scene(loader.load()));
                contextMenuStage.initModality(NONE);
                contextMenuStage.setResizable(false);
                contextMenuStage.setTitle("Menu");
                for (Node node : contextMenuStage.getScene().getRoot().getChildrenUnmodifiable()) {
                    node.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
                }
                contextMenuController.setInfoButtonListener(event -> contextMenuStage.hide());

            } catch (IOException e) {
                System.out.println("Error in initializing context menu");
                e.printStackTrace();
            }
        }
    }
}