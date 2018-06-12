/*
 * Bao Lab 2017
 */

package application_src.controllers.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.data.CElegansData.AnalogousCells.EmbryonicAnalogousCells;
import application_src.application_model.data.CElegansData.Anatomy.Anatomy;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.CElegansData.SulstonLineage.SulstonLineage;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import application_src.application_model.search.ModelSearch.EstablishCorrespondence;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.NeighborsSearch;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
import application_src.views.popups.TimelineChart;

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
import javafx.scene.control.*;
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

import application_src.application_model.data.LineageData;
import application_src.application_model.data.CElegansData.Connectome.Connectome;
import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.application_model.data.CElegansData.CellDeaths.CellDeaths;
import application_src.MainApp;
import application_src.controllers.layers.DisplayLayer;
import application_src.controllers.layers.SearchLayer;
import application_src.controllers.layers.StoriesLayer;
import application_src.controllers.layers.StructuresLayer;
import application_src.application_model.loaders.IconImageLoader;
import application_src.application_model.data.CElegansData.SulstonLineage.LineageTree;
import application_src.application_model.cell_case_logic.CasesLists;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;
import application_src.application_model.threeD.subscenegeometry.StructureTreeNode;
import application_src.application_model.resources.ProductionInfo;
import application_src.application_model.annotation.stories.Story;
import application_src.application_model.annotation.color.ColorHash;
import application_src.application_model.resources.utilities.StringCellFactory;
import application_src.application_model.threeD.subsceneparameters.Parameters;
import application_src.views.DraggableTab;
import application_src.views.info_window.InfoWindow;
import application_src.views.popups.AboutPane;
import application_src.views.popups.StorySavePane;
import application_src.views.popups.SulstonTreePane;
import application_src.views.url_window.URLLoadWarningDialog;
import application_src.views.url_window.URLLoadWindow;
import application_src.views.url_window.URLShareWindow;

import static java.lang.System.lineSeparator;
import static java.time.Duration.between;

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
import static javafx.stage.Modality.NONE;
import static javafx.stage.StageStyle.UNDECORATED;

import static application_src.application_model.loaders.AceTreeTableLineageDataLoader.getAvgXOffsetFromZero;
import static application_src.application_model.loaders.AceTreeTableLineageDataLoader.getAvgYOffsetFromZero;
import static application_src.application_model.loaders.AceTreeTableLineageDataLoader.getAvgZOffsetFromZero;
import static application_src.application_model.loaders.AceTreeTableLineageDataLoader.loadNucFiles;
import static application_src.application_model.loaders.AceTreeTableLineageDataLoader.setOriginToZero;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.annotation.color.URL.UrlParser.processUrl;

/**
 * Controller for RootLayout.fxml that contains all GUI components of the main WormGUIDES application window
 */
public class RootLayoutController extends BorderPane implements Initializable {

    private static final String UNLINEAGED_START = "Nuc";
    private static final String ROOT = "ROOT";

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
    private TreeView<StructureTreeNode> structuresTreeView;
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
    private Stage timelineStage;
    private TimelineChart<Number, String> chart;

    // URL generation/loading
    private URLShareWindow urlShareWindow;
    private URLLoadWindow urlLoadWindow;
    private URLLoadWarningDialog warning;

    private Window3DController window3DController;
    private RotationController rotationController;
    private DoubleProperty subsceneWidth;
    private DoubleProperty subsceneHeight;

    // the model-agnostic search pipeline
    private CElegansSearch cElegansSearchPipeline; // model agnostic

    // the model specific search pipelines
    private StructuresSearch structuresSearch;
    private NeighborsSearch neighborsSearch;

    // the module that establishes the correspondence between C elegans search results and the model
    private EstablishCorrespondence establishCorrespondence;

    // the annotation manager
    private AnnotationManager annotationManager;

    private SearchLayer searchLayer;
    private StoriesLayer storiesLayer;
    private DisplayLayer displayLayer;

    private SceneElementsList sceneElementsList;
    private ProductionInfo productionInfo;

    // Info window stuff
    private CasesLists casesLists;
    private InfoWindow infoWindow;
    private ImageView playIcon, pauseIcon;

    // SulstonLineage tree stuff
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
        if (lineageData.isSulstonMode()) {
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
    }

    @FXML
    public void viewTimeline() {
        if (timelineStage == null) {
            initTimelineChart();
        }
        timelineStage.show();
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
                                cElegansSearchPipeline,
                                neighborsSearch,
                                structuresSearch,
                                annotationManager,
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
                            cElegansSearchPipeline,
                            neighborsSearch,
                            structuresSearch,
                            annotationManager,
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
                searchType = "SulstonLineage Name";
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
        infoWindow.generateConnectomeWindow(Connectome.getSynapseList());
    }

    @FXML
    public void viewCellDeaths() {
        if (infoWindow == null) {
            initInfoWindow();
        }
        infoWindow.generateCellDeathsWindow(CellDeaths.getCellDeaths().toArray());
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
            loader.setLocation(MainApp.class.getResource("/application_src/views/layouts/RotationControllerLayout.fxml"));

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
                productionInfo,
                sceneElementsList,
                structuresLayer.getStructuresTreeRoot(),
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
        if (structuresSearch.isMulticellularStructureByName(name)) {
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
        if (StructuresSearch.isStructureWithComment(name)) {
            displayedDescription.setText(StructuresSearch.getStructureComment(name));
        } else { // Cell lineage name
            String functionalName = getFunctionalNameByLineageName(name);
            if (functionalName != null) {
                displayedName.setText("Active Cell: " + name + " (" + functionalName + ")");
                displayedDescription.setText(PartsList.getDescriptionByFunctionalName(functionalName));
            } else if (CElegansSearch.isCellDeath(name)) {
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
        backwardButton.setGraphic(IconImageLoader.getBackwardIcon());
        forwardButton.setGraphic(IconImageLoader.getForwardIcon());
        zoomInButton.setGraphic(new ImageView(IconImageLoader.getPlusIcon()));
        zoomOutButton.setGraphic(new ImageView(IconImageLoader.getMinusIcon()));

        playIcon = IconImageLoader.getPlayIcon();
        pauseIcon = IconImageLoader.getPauseIcon();
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
                cElegansSearchPipeline,
                neighborsSearch,
                structuresSearch,
                establishCorrespondence,
                annotationManager,
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
                addSearchBtn);
        searchResultsListView.setItems(searchResultsList);
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
        }
        final LineageTree lineageTree = new LineageTree(
                allCellNames.toArray(new String[allCellNames.size()]),
                lineageData.isSulstonMode());
        lineageTreeRoot = lineageTree.getRoot();
    }

    private void initStructuresLayer() {
        structuresLayer = new StructuresLayer(
                structuresSearch,
                annotationManager,
                sceneElementsList,
                selectedEntityNameProperty,
                structuresSearchField,
                structuresSearchListView,
                structuresTreeView,
                addStructureRuleBtn,
                structureRuleColorPicker,
                rebuildSubsceneFlag);
        if (structuresSearch != null) {
            structuresSearch.setStructureTreeRoot(structuresLayer.getStructuresTreeRoot());
        }
    }

    private void initStoriesLayer() {
        storiesLayer = new StoriesLayer(
                mainStage,
                cElegansSearchPipeline,
                neighborsSearch,
                structuresSearch,
                annotationManager,
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
                defaultEmbryoFlag,
                timelineStage);

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
                cElegansSearchPipeline,
                defaultEmbryoFlag,
                lineageData,
                cElegansSearchPipeline,
                structuresSearch);
        casesLists.setInfoWindow(infoWindow);
    }

    private void initTimelineChart() {
        timelineStage = new Stage();
        timelineStage.setTitle("Timeline");
        timelineStage.setScene(TimelineChart.initialize(storiesLayer, productionInfo));
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
        colorAndDisplayTabPane.getSelectionModel().select(displayTab);

        final DraggableTab storiesDragTab = new DraggableTab(storiesTab.getText());
        storiesDragTab.setCloseable(false);
        storiesDragTab.setContent(storiesTab.getContent());

        final DraggableTab colorAndDisplayDragTab = new DraggableTab(colorAndDisplayTab.getText());
        colorAndDisplayDragTab.setCloseable(false);
        colorAndDisplayDragTab.setContent(colorAndDisplayTab.getContent());

        mainTabPane.getTabs().clear();
        storiesTab = storiesDragTab;
        colorAndDisplayTab = colorAndDisplayDragTab;

        mainTabPane.getTabs().add(storiesTab);
        mainTabPane.getTabs().add(colorAndDisplayTab);
        mainTabPane.toFront();
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        // initialize the static C Elegans data
        SulstonLineage.init();
        PartsList.init();
        CellDeaths.init();
        Connectome.init();
        Anatomy.init();
        EmbryonicAnalogousCells.init();
        GeneSearchManager.init();

        // now set up the Search pipeline for the static C Elegans data
        cElegansSearchPipeline = new CElegansSearch();

        // transition to model and program specific data initialization
        Parameters.init();
        initSharedVariables();
        annotationManager = new AnnotationManager(rulesList, rebuildSubsceneFlag);

        productionInfo = new ProductionInfo();
        casesLists = new CasesLists();

        if (bundle != null) {
            defaultEmbryoFlag = false;
            lineageData = (LineageData) bundle.getObject("lineageData");
            System.out.println("loading external model");
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
            startTime = 1;
            movieTimeOffset = 0;
        }

        endTime = lineageData.getNumberOfTimePoints() - 1;
        movieTimeOffset = productionInfo.getMovieTimeOffset();

        replaceTabsWithDraggableTabs();
        initDisplayLayer();
        setSlidersProperties();

        mainTabPane.getSelectionModel().select(colorAndDisplayTab);

        initializeWithLineageData();
    }

    private void initializeWithLineageData() {
        initLineageTree(lineageData.getAllCellNames());

        neighborsSearch = new NeighborsSearch(this.lineageData);

        sceneElementsList = new SceneElementsList(this.lineageData, this.defaultEmbryoFlag);
        initStructuresLayer();
        structuresSearch = new StructuresSearch(sceneElementsList, structuresLayer.getStructuresTreeRoot(), annotationManager);

        establishCorrespondence = new EstablishCorrespondence(this.lineageData, this.structuresSearch);

        initSearchLayer();
        initTimelineChart();
        initStoriesLayer();
        initContextMenuStage();

        addListeners();
        setIcons();

        sizeSubscene();
        sizeInfoPane();

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
                    bringUpInfoFlag,
                    cElegansSearchPipeline,
                    neighborsSearch,
                    establishCorrespondence,
                    annotationManager);

            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/application_src/views/layouts/ContextMenuLayout.fxml"));
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
                //contextMenuController.getMoreInfoButtonListener(event -> contextMenuStage.hide());

            } catch (IOException e) {
                System.out.println("Error in initializing context menu");
                e.printStackTrace();
            }
        }
    }
}