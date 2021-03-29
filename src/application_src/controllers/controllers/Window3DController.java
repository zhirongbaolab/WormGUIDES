/*
 * Bao Lab 2017
 */

package application_src.controllers.controllers;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;


//import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import application_src.MainApp;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import application_src.application_model.data.LineageData;
import application_src.application_model.data.CElegansData.Connectome.Connectome;
import application_src.controllers.layers.SearchLayer;
import application_src.controllers.layers.StoriesLayer;
import application_src.application_model.threeD.camerageometry.Xform;
import application_src.application_model.cell_case_logic.CasesLists;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.threeD.subscenegeometry.SceneElement;
import application_src.application_model.threeD.subscenegeometry.SceneElementMeshView;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;
import application_src.application_model.threeD.subscenegeometry.StructureTreeNode;
import application_src.application_model.resources.ProductionInfo;
import application_src.application_model.annotation.stories.Note;
import application_src.application_model.annotation.stories.Note.Display;
import application_src.application_model.annotation.color.ColorComparator;
import application_src.application_model.annotation.color.ColorHash;
import application_src.application_model.threeD.subscenesaving.JavaPicture;
import application_src.application_model.threeD.subscenesaving.JpegImagesToMovie;

import static application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.ModelSpecificSearchUtil.getFirstOccurenceOf;
import static application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.ModelSpecificSearchUtil.getLastOccurenceOf;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import static javafx.application.Platform.runLater;
import static javafx.embed.swing.SwingFXUtils.fromFXImage;
import static javafx.scene.Cursor.CLOSED_HAND;
import static javafx.scene.Cursor.DEFAULT;
import static javafx.scene.Cursor.HAND;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseButton.SECONDARY;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_ENTERED;
import static javafx.scene.input.MouseEvent.MOUSE_ENTERED_TARGET;
import static javafx.scene.input.MouseEvent.MOUSE_MOVED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import static javafx.scene.input.ScrollEvent.SCROLL;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;
import static javafx.scene.paint.Color.web;
import static javafx.scene.text.FontSmoothingType.LCD;
import static javafx.scene.transform.Rotate.X_AXIS;
import static javafx.scene.transform.Rotate.Y_AXIS;
import static javafx.scene.transform.Rotate.Z_AXIS;

import static com.sun.javafx.scene.CameraHelper.project;
import static javax.imageio.ImageIO.write;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.search.SearchConfiguration.SearchType.LINEAGE;
import static application_src.application_model.search.SearchConfiguration.SearchType.NEIGHBOR;
import static application_src.application_model.loaders.NoteImageLoader.createImageView;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_BODY;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;
import static application_src.application_model.annotation.stories.Note.Display.CALLOUT_LOWER_LEFT;
import static application_src.application_model.annotation.stories.Note.Display.CALLOUT_LOWER_RIGHT;
import static application_src.application_model.annotation.stories.Note.Display.CALLOUT_UPPER_LEFT;
import static application_src.application_model.annotation.stories.Note.Display.CALLOUT_UPPER_RIGHT;
import static application_src.application_model.annotation.stories.Note.Display.OVERLAY;
import static application_src.application_model.annotation.stories.Note.Display.SPRITE;
import static application_src.application_model.resources.utilities.AppFont.getBillboardFont;
import static application_src.application_model.resources.utilities.AppFont.getOrientationIndicatorFont;
import static application_src.application_model.resources.utilities.AppFont.getSpriteAndOverlayFont;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getBillboardScale;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getCameraFarClip;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getCameraInitialDistance;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getCameraNearClip;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getDefaultOthersOpacity;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getInitialTranslateX;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getInitialTranslateY;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getInitialZoom;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getLabelSpriteYOffset;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getNoteBillboardImageScale;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getNoteBillboardTextWidth;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getNoteSpriteTextWidth;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getSelectabilityVisibilityCutoff;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getSizeScale;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getStoryOverlayPaneWidth;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getSubsceneBackgroundColorHex;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getUniformRadius;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getVisibilityCutoff;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getWaitTimeMilli;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getModelScaleFactor;
import static application_src.application_model.threeD.subsceneparameters.Parameters.getShapesIndexPad;

/**
 * The controller for the 3D subscene inside the rootEntitiesGroup layout. This class contains the subscene itself, and
 * places it into the AnchorPane called modelAnchorPane inside the rootEntitiesGroup layout. It is also responsible
 * for refreshing the scene on timeProperty, search, wormguides.stories, notes, and rules change. This class contains
 * observable properties that are passed to other classes so that a subscene refresh can be trigger from that other
 * class.
 * <p>
 * An "entity" in the subscene is either a cell, cell body, or multicellular structure. These are graphically
 * represented by the Shape3Ds Sphere and MeshView available in JavaFX. {@link Sphere}s represent cells, and
 * {@link MeshView}s represent cell bodies and multicellular structures. Notes and labels are rendered as
 * {@link Text}s. This class queries the {@link LineageData} and {@link SceneElementsList} for a certain timeProperty
 * and renders the entities, notes, story, and labels present in that timeProperty point.
 * <p>
 * For the coloring of entities, an observable list of {@link Rule}s is queried to see which ones apply to a
 * particular entity, then queries the {@link ColorHash} for the {@link Material} to use for the entity.
 */
public class Window3DController {
    private static final String CS = ", ";
    private static final String ACTIVE_LABEL_COLOR_HEX = "#ffff66",
            SPRITE_COLOR_HEX = "#ffffff",
            TRANSIENT_LABEL_COLOR_HEX = "#f0f0f0";

    private static final int X_COR_INDEX = 0,
            Y_COR_INDEX = 1,
            Z_COR_INDEX = 2;

    /** Y offset of the callout line segment endpoint from the actual callout {@link Text} **/
    private static final int CALLOUT_LINE_Y_OFFSET = 10;
    /** X offset of the callout line segment endpoint from the actual callout {@link Text} **/
    private static final int CALLOUT_LINE_X_OFFSET = 0;

    // rotation stuff
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Rotate rotateZ;
    private final Rotate rotateXIndicator;
    private final Rotate rotateYIndicator;
    private final Rotate rotateZIndicator;
    // transformation stuff
    private final Group rootEntitiesGroup;
    // switching timepoints stuff
    private final BooleanProperty playingMovieProperty;
    private final PlayService playService;
    private final RenderService renderService;
    /** Search results local to window 3d that only contains lineage names */
    private final List<String> localSearchResults;
    // color rules stuff
    private final ColorHash colorHash;
    private final Comparator<Color> colorComparator;
    private final Comparator<Shape3D> opacityComparator;
    // opacity value for "other" cells (with no rule attached)
    private final DoubleProperty othersOpacityProperty;
    private final DoubleProperty numPrevProperty;
    private final Slider prevSlider;
    private final Label prevValue;
    private final double nonSelectableOpacity = 0.25;
    private final List<String> otherCells;
    private final Vector<JavaPicture> javaPictures;
    private final SearchLayer searchLayer;
    private final Stage parentStage;
    private final LineageData lineageData;
    private final SubScene subscene;
    private final TextField searchField;

    //expression stuff
    private final IntegerProperty exprUpperProperty;
    private final Slider exprUpperSlider;
    private final TextField exprUpperField;
    private final IntegerProperty exprLowerProperty;
    private final Slider exprLowerSlider;
    private final TextField exprLowerField;
    private boolean expressionOn;


    // housekeeping stuff
    private final BooleanProperty rebuildSubsceneFlag;
    private final DoubleProperty rotateXAngleProperty;
    private final DoubleProperty rotateYAngleProperty;
    private final DoubleProperty rotateZAngleProperty;
    private final DoubleProperty translateXProperty;
    private final DoubleProperty translateYProperty;
    private final IntegerProperty timeProperty;
    private final IntegerProperty totalNucleiProperty;
    private final double[] initialRotation;

    /** Start timeProperty of the lineage without movie timeProperty offset */
    private final int startTime;
    /** End timeProperty of the lineage without movie timeProperty offset */
    private final int endTime;
    private final DoubleProperty zoomProperty;

    // Cell clicking/selection stuff
    private final IntegerProperty selectedIndex;
    private final StringProperty selectedNameProperty;
    private final StringProperty selectedNameLabeledProperty;
    private boolean externalSelectedFlag;
    private final Stage contextMenuStage;
    private final ContextMenuController contextMenuController;
    private final BooleanProperty cellClickedProperty;
    private final ObservableList<String> searchResultsList;
    private final ObservableList<Rule> rulesList;

    // Scene Elements stuff
    private final boolean defaultEmbryoFlag;
    private final SceneElementsList sceneElementsList;

    // Story elements stuff
    private final StoriesLayer storiesLayer;
    /** Map of current note graphics to their note objects */
    private final Map<Node, Note> currentGraphicsToNotesMap;
    /** Map of current notes to their scene elements */
    private final Map<Note, SceneElementMeshView> currentNotesToMeshesMap;
    /** Map of note sprites attached to cell, or cell and timeProperty */
    private final Map<Node, VBox> entitySpriteMap;
    /** Map of front-facing billboards attached to their entities */
    private final Map<Text, Node> billboardFrontEntityMap;
    /** Map of image views to their entities */
    private final Map<ImageView, Node> billboardImageEntityMap;
    /** Map of a cell entity to its label */
    private final Map<Node, Text> entityLabelMap;
    /** Map of upper left note callouts attached to a cell/structure */
    private final Map<Node, List<Text>> entityCalloutULMap;
    /** Map of upper right note callouts attached to a cell/structure */
    private final Map<Node, List<Text>> entityCalloutURMap;
    /** Map of lower left note callouts attached to a cell/structure */
    private final Map<Node, List<Text>> entityCalloutLLMap;
    /** Map of lower right note callouts attached to a cell/structure */
    private final Map<Node, List<Text>> entityCalloutLRMap;
    /* Map of all callout Texts to their Lines */
    private final Map<Text, Line> calloutLineMap;
    // orientation indicator
    private Cylinder orientationIndicator;
    private final ProductionInfo productionInfo;
    private final BooleanProperty bringUpInfoFlag;
    private final SubsceneSizeListener subsceneSizeListener;

    // rotation - AP
    private double[] keyValuesRotate;
    private double[] keyFramesRotate;

    // subscene state parameters
    private LinkedList<Sphere> spheres;
    private LinkedList<SceneElementMeshView> meshes;
    private LinkedList<String> cellNames;
    private LinkedList<String> meshNames;
    private boolean[] isCellSearchedFlags;
    private boolean[] isMeshSearchedFlags;
    private LinkedList<Double[]> positions;
    private LinkedList<Double> diameters;
    private LinkedList<Integer> rweights;
    private List<SceneElement> sceneElementsAtCurrentTime;
    private List<SceneElementMeshView> currentSceneElementMeshes;
//    private List<MeshView> currentSceneElementMeshes;
    private List<SceneElement> currentSceneElements;
    private PerspectiveCamera camera;
    private Xform xform;
    private double mousePosX, mousePosY, mousePosZ;
    private double mouseOldX, mouseOldY, mouseOldZ;

    // Label stuff
    private double mouseDeltaX, mouseDeltaY;
    // average position offsets of nuclei from zero
    private double offsetX, offsetY, offsetZ;
    private double angleOfRotation;
    // searched highlighting stuff
    private boolean isInSearchMode;
    // Uniform nuclei sizef
    private boolean uniformSize;
    // Cell body and cell nucleus highlighting in search mode
    private boolean cellNucleusTicked;
    private boolean cellBodyTicked;
    /** All notes that are active, or visible, in a frame */
    private List<Note> currentNotes;
    /**
     * Rectangular box that resides in the upper-right-hand corner of the subscene. The active story title and
     * description are shown here.
     */
    private VBox storyOverlayVBox;
    /** Overlay of the subscene. Note sprites are inserted into this overlay. */
    private Pane spritesPane;
    /** Labels that exist in any of the timeProperty frames */
    private List<String> allLabels;
    /** Labels currently visible in the frame */
    private List<String> currentLabels;
    /** Label that shows up on hover */
    private Text transientLabelText;
    private Rotate indicatorRotation;// this is the timeProperty varying component of
    private BooleanProperty captureVideo;
    private Timer timer;
    private Vector<File> movieFiles;
    private int count;
    private String movieName;
    private String moviePath;
    private File frameDir;
    private String frameDirPath;

    // private Quaternion quaternion;

    /** X-scale of the subscene coordinate axis read from ProductionInfo.csv */
    private double xScale;
    /** Y-scale of the subscene coordinate axis read from ProductionInfo.csv */
    private double yScale;
    /** Z-scale of the subscene coordinate axis read from ProductionInfo.csv */
    private double zScale;

    public Window3DController(
            final Stage parentStage,
            final Group rootEntitiesGroup,
            final SubScene subscene,
            final AnchorPane parentPane,
            final LineageData lineageData,
            final ProductionInfo productionInfo,
            final SceneElementsList sceneElementsList,
            final TreeItem<StructureTreeNode> structureTreeRoot,
            final StoriesLayer storiesLayer,
            final SearchLayer searchLayer,
            final BooleanProperty bringUpInfoFlag,
            final double offsetX,
            final double offsetY,
            final double offsetZ,
            final boolean defaultEmbryoFlag,
            final double xScale,
            final double yScale,
            final double zScale,
            final AnchorPane modelAnchorPane,
            final Button backwardButton,
            final Button forwardButton,
            final Button zoomOutButton,
            final Button zoomInButton,
            final Button clearAllLabelsButton,
            final TextField searchField,
            final Slider opacitySlider,
            final Slider prevSlider,
            final Label prevValue,
            final CheckBox expressionOnCheckBox,
            final Slider exprUpperSlider,
            final TextField exprUpperField,
            final Slider exprLowerSlider,
            final TextField exprLowerField,
            final CheckBox uniformSizeCheckBox,
            final CheckBox cellNucleusCheckBox,
            final CheckBox cellBodyCheckBox,
            //final RadioButton multiRadioBtn,
            final int startTime,
            final int endTime,
            final IntegerProperty timeProperty,
            final IntegerProperty totalNucleiProperty,
            final DoubleProperty zoomProperty,
            final DoubleProperty othersOpacityProperty,
            final DoubleProperty numPrevProperty,
            final IntegerProperty exprUpperProperty,
            final IntegerProperty exprLowerProperty,
            final DoubleProperty rotateXAngleProperty,
            final DoubleProperty rotateYAngleProperty,
            final DoubleProperty rotateZAngleProperty,
            final DoubleProperty translateXProperty,
            final DoubleProperty translateYProperty,
            final StringProperty selectedNameProperty,
            final StringProperty selectedNameLabeledProperty,
            final BooleanProperty cellClickedFlag,
            final BooleanProperty playingMovieFlag,
            final BooleanProperty geneResultsUpdatedFlag,
            final BooleanProperty rebuildSubsceneFlag,
            final ObservableList<Rule> rulesList,
            final ColorHash colorHash,
            final Stage contextMenuStage,
            final ContextMenuController contextMenuController,
            final ObservableList<String> searchResultsList) {

        this.parentStage = requireNonNull(parentStage);

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        this.startTime = startTime;
        this.endTime = endTime;

        this.rootEntitiesGroup = requireNonNull(rootEntitiesGroup);
        this.lineageData = lineageData;
        this.productionInfo = requireNonNull(productionInfo);
        this.sceneElementsList = requireNonNull(sceneElementsList);
        this.storiesLayer = requireNonNull(storiesLayer);
        this.searchLayer = requireNonNull(searchLayer);

        this.defaultEmbryoFlag = defaultEmbryoFlag;

        // Set listener properties for the timeProperty variable. Updates time. If in movie capture mode,
        // a screenshot is captured per frame. Thus, movies are only captured during play mode
        this.timeProperty = requireNonNull(timeProperty);
        this.timeProperty.addListener((observable, oldValue, newValue) -> {
            final int newTime = newValue.intValue();
            final int oldTime = oldValue.intValue();
            if (startTime <= newTime && newTime <= endTime) {
                hideContextPopups();
            } else if (newTime < startTime) {
                timeProperty.set(startTime);
            } else if (newTime > endTime) {
                timeProperty.set(endTime);
            }

            if (captureVideo.get()) {
                WritableImage screenCapture = subscene.snapshot(new SnapshotParameters(), null);
                try {
                    File file = new File(frameDirPath + "movieFrame" + count++ + ".JPEG");

                    if (file != null) {
                        RenderedImage renderedImage = fromFXImage(screenCapture, null);
                        write(renderedImage, "JPEG", file);
                        movieFiles.addElement(file);
                    }
                } catch (Exception e) {
                    System.out.println("Could not write frame of movie to file.");
                }
            }

        });

        // set orientation indicator frames and rotation from production info
        keyFramesRotate = productionInfo.getKeyFramesRotate();
        keyValuesRotate = productionInfo.getKeyValuesRotate();
        initialRotation = productionInfo.getInitialRotation();

        spheres = new LinkedList<>();
        meshes = new LinkedList<>();
        cellNames = new LinkedList<>();
        meshNames = new LinkedList<>();
        positions = new LinkedList<>();
        diameters = new LinkedList<>();
        rweights = new LinkedList<>();
        isCellSearchedFlags = new boolean[1];
        isMeshSearchedFlags = new boolean[1];

        selectedIndex = new SimpleIntegerProperty(-1);
        captureVideo = new SimpleBooleanProperty();

        this.selectedNameProperty = requireNonNull(selectedNameProperty);
        this.selectedNameProperty.addListener((observable, oldValue, newValue) -> {
            int selected = getIndexByCellName(newValue);
            if (selected != -1) {
                selectedIndex.set(selected);
            }
        });

        externalSelectedFlag = false;

        this.selectedNameLabeledProperty = requireNonNull(selectedNameLabeledProperty);
        this.selectedNameLabeledProperty.addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && newValue != selectedNameProperty.getValue()) {
                String lineageName = newValue;

                // go to labeled name
                int startTime1;
                int endTime1;
                startTime1 = getFirstOccurenceOf(lineageName);
                endTime1 = getLastOccurenceOf(lineageName);

                // do not change scene if entity does not exist at any timeProperty
                if (startTime1 <= 0 || endTime1 <= 0) {
                    System.out.println("not exist " + lineageName);
                }

                if (timeProperty.get() < startTime1 || timeProperty.get() > endTime1) {
                    //System.out.println("Updating time property to entity startTime=" + startTime1 + " because current time: " + timeProperty.get() + " isn't in cell lifetime range. Endtime = " + endTime1);
                    timeProperty.set(startTime1);
                }
                externalSelectedFlag = true;
                buildScene();
            }
        });

        this.rulesList = requireNonNull(rulesList);

        this.cellClickedProperty = requireNonNull(cellClickedFlag);
        this.totalNucleiProperty = requireNonNull(totalNucleiProperty);

        this.subscene = requireNonNull(subscene);
        buildCamera();
        parentPane.getChildren().add(this.subscene);
        this.subscene.setFill(web(getSubsceneBackgroundColorHex()));

        isInSearchMode = false;

        subsceneSizeListener = new SubsceneSizeListener();
        parentPane.widthProperty().addListener(subsceneSizeListener);
        parentPane.heightProperty().addListener(subsceneSizeListener);

        mousePosX = 0.0;
        mousePosY = 0.0;
        mousePosZ = 0.0;
        mouseOldX = 0.0;
        mouseOldY = 0.0;
        mouseOldZ = 0.0;
        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;
        angleOfRotation = 0.0;

        playService = new PlayService();
        this.playingMovieProperty = requireNonNull(playingMovieFlag);
        this.playingMovieProperty.addListener((observable, oldValue, newValue) -> {
            hideContextPopups();
            if (newValue) {
                playService.restart();
            } else {
                playService.cancel();
            }
        });

        renderService = new RenderService();

        this.zoomProperty = requireNonNull(zoomProperty);
        this.zoomProperty.set(getInitialZoom());
        this.zoomProperty.addListener((observable, oldValue, newValue) -> {
            xform.setScale(zoomProperty.get());
            repositionNotes();
        });
        xform.setScale(zoomProperty.get());

        localSearchResults = new ArrayList<>();

        requireNonNull(geneResultsUpdatedFlag).addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                updateLocalSearchResults();
                geneResultsUpdatedFlag.set(false);
            }
        });

        otherCells = new ArrayList<>();

        rotateXIndicator = new Rotate(0, X_AXIS);
        rotateYIndicator = new Rotate(0, Y_AXIS);
        rotateZIndicator = new Rotate(0, Z_AXIS);

        rotateX = new Rotate(initialRotation[0], X_AXIS);
        rotateY = new Rotate(initialRotation[1], Y_AXIS);
        rotateZ = new Rotate(initialRotation[2], Z_AXIS);

        // initialize
        this.rotateXAngleProperty = requireNonNull(rotateXAngleProperty);
        this.rotateXAngleProperty.set(rotateX.getAngle());
        this.rotateYAngleProperty = requireNonNull(rotateYAngleProperty);
        this.rotateYAngleProperty.set(rotateY.getAngle());
        this.rotateZAngleProperty = requireNonNull(rotateZAngleProperty);
        this.rotateZAngleProperty.set(rotateZ.getAngle());

        // add listener for control from rotationcontroller
        this.rotateXAngleProperty.addListener(getRotateXAngleListener());
        this.rotateYAngleProperty.addListener(getRotateYAngleListener());
        this.rotateZAngleProperty.addListener(getRotateZAngleListener());

        this.translateXProperty = requireNonNull(translateXProperty);
        this.translateXProperty.addListener(getTranslateXListener());
        this.translateXProperty.set(getInitialTranslateX());
        this.translateYProperty = requireNonNull(translateYProperty);
        this.translateYProperty.addListener(getTranslateYListener());
        this.translateYProperty.set(getInitialTranslateY());

        this.colorHash = requireNonNull(colorHash);
        colorComparator = new ColorComparator();
        opacityComparator = new OpacityComparator();

        if (defaultEmbryoFlag) {
            currentSceneElementMeshes = new ArrayList<>();
            currentSceneElements = new ArrayList<>();
        }

        currentNotes = new ArrayList<>();
        currentGraphicsToNotesMap = new HashMap<>();
        currentNotesToMeshesMap = new HashMap<>();
        billboardImageEntityMap = new HashMap<>();
        entitySpriteMap = new HashMap<>();
        billboardFrontEntityMap = new HashMap<>();
        entityCalloutULMap = new HashMap<>();
        entityCalloutURMap = new HashMap<>();
        entityCalloutLLMap = new HashMap<>();
        entityCalloutLRMap = new HashMap<>();

        calloutLineMap = new HashMap<>();

        allLabels = new ArrayList<>();
        currentLabels = new ArrayList<>();
        entityLabelMap = new HashMap<>();

        final EventHandler<MouseEvent> mouseHandler = this::handleMouseEvent;
        subscene.setOnMouseClicked(mouseHandler);
        subscene.setOnMouseDragged(mouseHandler);
        subscene.setOnMouseEntered(mouseHandler);
        subscene.setOnMousePressed(mouseHandler);
        subscene.setOnMouseReleased(mouseHandler);

        final EventHandler<ScrollEvent> mouseScrollHandler = this::handleScrollEvent;
        subscene.setOnScroll(mouseScrollHandler);

        setNotesPane(parentPane);

        movieFiles = new Vector<>();
        javaPictures = new Vector<>();
        count = -1;

        // set up the orientation indicator in bottom right corner
        double radius = 5.0;
        double height = 15.0;
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(RED);
        if (defaultEmbryoFlag) {
            orientationIndicator = new Cylinder(radius, height);
            orientationIndicator.setMaterial(material);

            xform.getChildren().add(createOrientationIndicator());
        }

        this.bringUpInfoFlag = requireNonNull(bringUpInfoFlag);

        this.rebuildSubsceneFlag = requireNonNull(rebuildSubsceneFlag);
        // reset rebuild subscene flag to false because it may have been set to true by another layer's initialization
        this.rebuildSubsceneFlag.set(false);
        this.rebuildSubsceneFlag.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                buildScene();
                rebuildSubsceneFlag.set(false);
            }
        });

        // set up the scaling value to convert from microns to pixel values, we set x,y = 1 and z = ratio of z to
        // original y note that xScale and yScale are not the same
        if (xScale != yScale) {
            System.err.println(
                    "xScale does not equal yScale - using ratio of Z to X for zScale value in pixels\n"
                            + "X, Y should be the same value");
        }
        this.xScale = 1;
        this.yScale = 1;
        this.zScale = zScale / xScale;

        this.searchField = requireNonNull(searchField);
        this.searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                isInSearchMode = false;
                buildScene();
            } else {
                isInSearchMode = true;
            }
        });

        requireNonNull(modelAnchorPane).setOnMouseClicked(getNoteClickHandler());

        requireNonNull(backwardButton).setOnAction(getBackwardButtonListener());
        requireNonNull(forwardButton).setOnAction(getForwardButtonListener());
        requireNonNull(zoomOutButton).setOnAction(getZoomOutButtonListener());
        requireNonNull(zoomInButton).setOnAction(getZoomInButtonListener());

        this.othersOpacityProperty = requireNonNull(othersOpacityProperty);
        requireNonNull(opacitySlider).valueProperty().addListener((observable, oldValue, newValue) -> {
            final double newRounded = round(newValue.doubleValue()) / 100.0;
            final double oldRounded = round(oldValue.doubleValue()) / 100.0;
            if (newRounded != oldRounded) {
                othersOpacityProperty.set(newRounded);
                buildScene();
            }
        });
        this.othersOpacityProperty.addListener((observable, oldValue, newValue) -> {
            final double newVal = newValue.doubleValue();
            final double oldVal = oldValue.doubleValue();
            if (newVal != oldVal && newVal >= 0 && newVal <= 1.0) {
                opacitySlider.setValue(newVal * 100);
            }
        });
        if (defaultEmbryoFlag) {
            this.othersOpacityProperty.setValue(getDefaultOthersOpacity());
        } else {
            /*
             * if a non-default model has been loaded, set the opacity cutoff at the level where labels will
             * appear by default
             */
            this.othersOpacityProperty.set(0.26);

        }

        this.numPrevProperty = requireNonNull(numPrevProperty);
        this.prevValue = requireNonNull(prevValue);
        this.prevSlider = requireNonNull(prevSlider);
        requireNonNull(prevSlider).valueProperty().addListener((observable, oldValue, newValue) -> {
            final double newRounded = round(newValue.doubleValue());
            final double oldRounded = round(oldValue.doubleValue());
            if (newRounded != oldRounded) {
                numPrevProperty.set(newRounded);
                prevValue.setText(String.valueOf(newRounded));
                buildScene();
            }
        });
        this.numPrevProperty.addListener((observable, oldValue, newValue) -> {
            final double newVal = newValue.doubleValue();
            final double oldVal = oldValue.doubleValue();
            if (newVal != oldVal && newVal >= 1 && newVal <= timeProperty.get()) {
                prevSlider.setValue(newVal);
            }
        });
        this.numPrevProperty.setValue(1);

        // expression upper and lower bound sliders
        this.exprUpperProperty = requireNonNull(exprUpperProperty);
        this.exprUpperField = requireNonNull(exprUpperField);
        this.exprUpperSlider = requireNonNull(exprUpperSlider);
        requireNonNull(exprUpperSlider).valueProperty().addListener((observable, oldValue, newValue) -> {
            final int newVal = newValue.intValue();
            final int oldVal = oldValue.intValue();
            if (newVal != oldVal) {
                exprUpperProperty.set(newVal);
                exprUpperField.setText(String.valueOf(newVal));
                buildScene();
            }
        });
        this.exprUpperField.textProperty().addListener((observable, oldValue, newValue) -> {
            // allow no entry or "-", but do nothing
            if (newValue.isEmpty() || newValue.equals("-")) {
                return;
            } else {
                // use try catch to filter out non integer input
                try {
                    int newVal = Integer.parseInt(newValue);
                    if (newVal < exprUpperSlider.getMin()) { // when new value is smaller than min slider value, set it to min
                        exprUpperSlider.setValue(exprUpperSlider.getMin());
                        exprUpperField.setText("" + (int)exprUpperSlider.getMin());
                    } else if (newVal > exprUpperSlider.getMax()) { // when new value is larger than max slider value, set it to max
                        exprUpperSlider.setValue(exprUpperSlider.getMax());
                        exprUpperField.setText("" + (int)exprUpperSlider.getMax());
                    } else {
                        exprUpperSlider.setValue(newVal);
                    }
                } catch (NumberFormatException e) {
                    exprUpperField.setText(oldValue);
                }
            }
        });
        this.exprUpperProperty.addListener((observable, oldValue, newValue) -> {
            final double newVal = newValue.doubleValue();
            final double oldVal = oldValue.doubleValue();
            if (newVal != oldVal && newVal >= exprUpperSlider.getMin() && newVal <= exprUpperSlider.getMax()) {
                exprUpperSlider.setValue(newVal);
            }
        });
        this.exprUpperProperty.setValue(0);

        this.exprLowerProperty = requireNonNull(exprLowerProperty);
        this.exprLowerField = requireNonNull(exprLowerField);
        this.exprLowerSlider = requireNonNull(exprLowerSlider);
        requireNonNull(exprLowerSlider).valueProperty().addListener((observable, oldValue, newValue) -> {
            final int newVal = newValue.intValue();
            final int oldVal = oldValue.intValue();
            if (newVal != oldVal) {
                exprLowerProperty.set(newVal);
                exprLowerField.setText(String.valueOf(newVal));
                buildScene();
            }
        });
        this.exprLowerField.textProperty().addListener((observable, oldValue, newValue) -> {
            // allow no entry or "-", but do nothing
            if (newValue.isEmpty() || newValue.equals("-")) {
                return;
            } else {
                // use try catch to filter out non integer input
                try {
                    int newVal = Integer.parseInt(newValue);
                    if (newVal < exprLowerSlider.getMin()) { // when new value is smaller than min slider value, set it to min
                        exprLowerSlider.setValue(exprLowerSlider.getMin());
                        exprLowerField.setText("" + (int)exprLowerSlider.getMin());
                    } else if (newVal > exprLowerSlider.getMax()) { // when new value is larger than max slider value, set it to max
                        exprLowerSlider.setValue(exprLowerSlider.getMax());
                        exprLowerField.setText("" + (int)exprLowerSlider.getMax());
                    } else {
                        exprLowerSlider.setValue(newVal);
                    }
                } catch (NumberFormatException e) {
                    exprLowerField.setText(oldValue);
                }
            }
        });
        this.exprLowerProperty.addListener((observable, oldValue, newValue) -> {
            final double newVal = newValue.doubleValue();
            final double oldVal = oldValue.doubleValue();
            if (newVal != oldVal && newVal >= exprLowerSlider.getMin() && newVal <= exprLowerSlider.getMax()) {
                exprLowerSlider.setValue(newVal);
            }
        });
        this.exprLowerProperty.setValue(0);

        expressionOnCheckBox.setSelected(true);
        expressionOn = true;
        requireNonNull(expressionOnCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> {
            expressionOn = newValue;
            buildScene();
        });

        uniformSizeCheckBox.setSelected(true);
        uniformSize = true;
        requireNonNull(uniformSizeCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> {
            uniformSize = newValue;
            buildScene();
        });

        requireNonNull(clearAllLabelsButton).setOnAction(getClearAllLabelsButtonListener());
        requireNonNull(cellNucleusCheckBox).selectedProperty().addListener(getCellNucleusTickListener());
        requireNonNull(cellBodyCheckBox).selectedProperty().addListener(getCellBodyTickListener());
        //requireNonNull(multiRadioBtn).selectedProperty().addListener(getMulticellModeListener());

        this.contextMenuStage = requireNonNull(contextMenuStage);
        this.contextMenuController = requireNonNull(contextMenuController);

        this.searchResultsList = requireNonNull(searchResultsList);
        this.searchResultsList.addListener((ListChangeListener)(c -> {
            updateLocalSearchResults();
        }));


        this.captureVideo = new SimpleBooleanProperty();
    }

    /**
     * Creates the orientation indicator and transforms
     * <p>
     * (for new model as of 1/5/2016)
     *
     * @return the group containing the orientation indicator texts
     */
    private Group createOrientationIndicator() {
        indicatorRotation = new Rotate();
        // top level group
        // had rotation to make it match main rotation
        final Group orientationIndicator = new Group();
        // has rotation to make it match biological orientation
        final Group middleTransformGroup = new Group();

        // set up the orientation indicator in bottom right corner
        Text t = makeOrientationIndicatorText("A     P");
        t.setTranslateX(-10);
        middleTransformGroup.getChildren().add(t);

        t = makeOrientationIndicatorText("R     L");
        t.setTranslateX(-10);
        t.setRotate(90);
        middleTransformGroup.getChildren().add(t);

        t = makeOrientationIndicatorText("V    D");
        t.setTranslateX(5);
        t.setTranslateZ(15);
        t.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
        middleTransformGroup.getChildren().add(t);

        // xy relocates z shrinks apparent by moving away from camera? improves resolution?
        middleTransformGroup.getTransforms().add(new Scale(3, 3, 3));

        // set the location of the indicator in the bottom right corner of the screen
        orientationIndicator.getTransforms().add(new Translate(270, 200, 800));

        // add rotation variables
        orientationIndicator.getTransforms().addAll(rotateXIndicator, rotateYIndicator, rotateZIndicator);

        // add the directional symbols to the group
        orientationIndicator.getChildren().add(middleTransformGroup);

        // add rotation
        middleTransformGroup.getTransforms().add(indicatorRotation);

        return orientationIndicator;
    }

    //handle external selected cell
    private void handleExternalSelectedCell() {
        String lineageName = selectedNameLabeledProperty.getValue();
        this.selectedNameProperty.set(lineageName);

        if (!allLabels.contains(lineageName)) {
            allLabels.add(lineageName);
        }


        Shape3D entity = getEntityWithName(lineageName);

        // go to labeled name
        int startTime1;
        int endTime1;

        startTime1 = getFirstOccurenceOf(lineageName);
        endTime1 = getLastOccurenceOf(lineageName);

        // do not change scene if entity does not exist at any timeProperty
        if (startTime1 <= 0 || endTime1 <= 0) {
            return;
        }

        if (timeProperty.get() < startTime1 || timeProperty.get() > endTime1) {
            //System.out.println("Updating time property to entity startTime=" + startTime1 + " because current time: " + timeProperty.get() + " isn't in cell lifetime range. Endtime = " + endTime1);
            timeProperty.set(startTime1);
        }

        insertLabelFor(lineageName, entity);
        highlightActiveCellLabel(entity);

        // set the name in MainApp so that other apps opening WormGUIDES can catch this event
        MainApp.seletedEntityLabelMainApp.set(lineageName);

        //center the external selected cell
        Double[] entity_position = positions.get(getIndexByCellName(lineageName));
        double translateX = entity_position[0];
        double translateY = entity_position[1];
        xform.setTranslateX(translateX);
        xform.setTranslateY(translateY);
        translateXProperty.set(translateX);
        translateYProperty.set(translateY);
        repositionNotes();

        externalSelectedFlag = false;
    }

    private double computeInterpolatedValue(int timevalue, double[] keyFrames, double[] keyValues) {
        if (timevalue <= keyFrames[0]) {
            return keyValues[0];
        }
        if (timevalue >= keyFrames[keyFrames.length - 1]) {
            return keyValues[keyValues.length - 1];
        }
        int i;
        for (i = 0; i < keyFrames.length; i++) {
            if (keyFrames[i] == timevalue) {
                return (keyValues[i]);
            }
            if (keyFrames[i] > timevalue) {
                break;
            }
        }
        // interpolate btw values at i and i-1
        double alpha = ((timevalue - keyFrames[i - 1]) / (keyFrames[i] - keyFrames[i - 1]));
        double value = keyValues[i] * alpha + keyValues[i - 1] * (1 - alpha);
        return value;
    }

    /**
     * Inserts a transient label into the sprites pane for the specified entity if the entity is not an 'other' entity
     * that has an opacity less than the cutoff (specified as a parameter in
     * /wormguides/util/subsceneparameters/parameters.txt)
     *
     * @param name
     *         the name that appears on the transient label
     * @param entity
     *         The entity that the label should appear on
     */
    private void insertTransientLabel(String name, final Shape3D entity) {
        final double opacity = othersOpacityProperty.get();
        if (entity != null) {
            // do not create transient label for "other" entities when their visibility is under the selectability
            // cutoff
            if ((entity.getMaterial() == colorHash.getOthersMaterial(opacity))
                    && (othersOpacityProperty.get() < getSelectabilityVisibilityCutoff())) {
                return;
            }
            if (!currentLabels.contains(name)) {
                final Bounds b = entity.getBoundsInParent();
                if (b != null) {
                    final String funcName = getFunctionalNameByLineageName(name);
                    if (funcName != null) {
                        name = funcName;
                    }
                    transientLabelText = makeNoteSpriteText(name);
                    transientLabelText.setWrappingWidth(-1);
                    transientLabelText.setFill(web(TRANSIENT_LABEL_COLOR_HEX));
                    transientLabelText.setOnMouseEntered(Event::consume);
                    transientLabelText.setOnMouseClicked(Event::consume);
                    final Point2D p = project(
                            camera,
                            new Point3D(
                                    (b.getMinX() + b.getMaxX())*getModelScaleFactor() / 2.0,
                                    (b.getMinY() + b.getMaxY())*getModelScaleFactor() / 2.0,
                                    (b.getMaxZ() + b.getMinZ())*getModelScaleFactor() / 2.0));
                    double x = p.getX();
                    double y = p.getY();

                    y -= getLabelSpriteYOffset();
                    transientLabelText.getTransforms().add(new Translate(x, y));
                    // disable text to take away label flickering when mouse is on top top of it
                    transientLabelText.setDisable(true);
                    spritesPane.getChildren().add(transientLabelText);
                }
            }
        }
    }

    /**
     * Removes transient label from sprites pane.
     */
    private void removeTransientLabel() {
        spritesPane.getChildren().remove(transientLabelText);
    }

    /**
     * Triggers zoom in and out on mouse wheel scroll
     * DeltaY indicates the direction of scroll:
     * -Y: zoom out
     * +Y: zoom in
     *
     * @param se
     *         the scroll event
     */
    public void handleScrollEvent(final ScrollEvent se) {
        final EventType<ScrollEvent> type = se.getEventType();
        if (type == SCROLL) {
            double z = zoomProperty.get();
            if (se.getDeltaY() < 0) {
                // zoom out
                if (z < 24.75) {
                    zoomProperty.set(z + 0.25);
                }
            } else if (se.getDeltaY() > 0) {
                // zoom in
                if (z > 0.25) {
                    z -= 0.25;
                } else if (z < 0) {
                    z = 0;
                }
                zoomProperty.set(z);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void handleMouseEvent(final MouseEvent me) {
        final EventType<MouseEvent> type = (EventType<MouseEvent>) me.getEventType();
        if (type == MOUSE_ENTERED_TARGET
                || type == MOUSE_ENTERED
                || type == MOUSE_RELEASED
                || type == MOUSE_MOVED) {
            handleMouseReleasedOrEntered();
        } else if (type == MOUSE_CLICKED && me.isStillSincePress()) {
            handleMouseClicked(me);
        } else if (type == MOUSE_DRAGGED) {
            handleMouseDragged(me);
        } else if (type == MOUSE_PRESSED) {
            handleMousePressed(me);
        }
    }

    private void handleMouseDragged(final MouseEvent event) {
        hideContextPopups();
        spritesPane.setCursor(CLOSED_HAND);

        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mouseOldZ = mousePosZ;
        mousePosX = event.getSceneX();
        mousePosY = event.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        mouseDeltaX /= 2;
        mouseDeltaY /= 2;

        angleOfRotation = rotationAngleFromMouseMovement();
        mousePosZ = computeZCoord(mousePosX, mousePosY, angleOfRotation);

        if (event.isSecondaryButtonDown() || event.isMetaDown() || event.isControlDown()) {
            double translateX = 0.;
            double translateY = 0.;
            if (xform.s.getX() < 1.) {
                translateX = xform.getTranslateX() - (mouseDeltaX * xform.s.getX());
                translateY = xform.getTranslateY() - (mouseDeltaY * xform.s.getY());
            } else if (xform.s.getX() < 2.) {
                translateX = xform.getTranslateX() - (mouseDeltaX / xform.s.getX());
                translateY = xform.getTranslateY() - (mouseDeltaY / xform.s.getY());
            } else {
                translateX = xform.getTranslateX() - mouseDeltaX;
                translateY = xform.getTranslateY() - mouseDeltaY;
            }

            xform.setTranslateX(translateX);
            xform.setTranslateY(translateY);
            translateXProperty.set(translateX);
            translateYProperty.set(translateY);
            repositionNotes();

        } else {
            if (event.isPrimaryButtonDown()) {
                double modifier = 10.0;
                double modifierFactor = 0.1;

                rotateXAngleProperty.set((
                        (rotateXAngleProperty.get() + mouseDeltaY * modifierFactor * modifier * 2.0)
                                % 360 + 540) % 360 - 180);
                rotateXIndicator.setAngle(rotateX.getAngle() - initialRotation[0]);
                rotateYAngleProperty.set((
                        (rotateYAngleProperty.get() + mouseDeltaX * modifierFactor * modifier * 2.0)
                                % 360 + 540) % 360 - 180);
                rotateYIndicator.setAngle(rotateY.getAngle() - initialRotation[1]);
            }
        }
    }

    private void handleMouseReleasedOrEntered() {
        spritesPane.setCursor(DEFAULT);
    }

    private void handleMouseClicked(final MouseEvent event) {
        spritesPane.setCursor(DEFAULT);
        hideContextPopups();

        final Node node = event.getPickResult().getIntersectedNode();

        if (node instanceof Sphere) {
            // Nucleus
            Sphere picked = (Sphere) node;
            int index = getPickedSphereIndex(picked);
            String name = normalizeName(cellNames.get(index));

            cellClickedProperty.set(true);
            selectedNameProperty.set(name);
            selectedIndex.set(index);

            // right click
            if (event.getButton() == SECONDARY
                    || (event.getButton() == PRIMARY
                    && (event.isMetaDown() || event.isControlDown()))) {
                boolean hasFunctionalName = false;
                if (getFunctionalNameByLineageName(name) != null) {
                    hasFunctionalName = true;
                }
                showContextMenu(
                        name,
                        event.getScreenX(),
                        event.getScreenY(),
                        false,
                        false,
                        hasFunctionalName);

            } else if (event.getButton() == PRIMARY) {
                // regular click
                if (allLabels.contains(name)) {
                    removeLabelFor(name);
                } else {
                    if (!allLabels.contains(name)) {
                        allLabels.add(name);
                        currentLabels.add(name);
                        final Shape3D entity = getEntityWithName(name);
                        insertLabelFor(name, entity);
                        highlightActiveCellLabel(entity);
                        // set the name in MainApp so that other apps opening WormGUIDES can catch this event
                        MainApp.seletedEntityLabelMainApp.set(name);
                    }
                }
            }

        } else if (node instanceof SceneElementMeshView) {
            // Structure
            boolean found = false; // this will indicate whether this meshview is a scene element
            SceneElementMeshView curr;
//            MeshView curr;
            SceneElement clickedSceneElement;
            String funcName;
            for (int i = 0; i < currentSceneElementMeshes.size(); i++) {
                curr = currentSceneElementMeshes.get(i);
                if (curr.equals(node)) {
                    clickedSceneElement = currentSceneElements.get(i);
                    if (!clickedSceneElement.isSelectable()) {
                        selectedIndex.set(-1);
                        selectedNameProperty.set("");
                        return;
                    }

                    found = true;

                    String name = normalizeName(clickedSceneElement.getSceneName());
                    selectedNameProperty.set(name);

                    if (event.getButton() == SECONDARY
                            || (event.getButton() == PRIMARY && (event.isMetaDown() || event.isControlDown()))) {
                        // right click
                        if (sceneElementsList.isStructureSceneName(name)) {
                            boolean hasFunctionalName = false;
                            if (getFunctionalNameByLineageName(name) != null) {
                                hasFunctionalName = true;
                            }
                            showContextMenu(
                                    name,
                                    event.getScreenX(),
                                    event.getScreenY(),
                                    true,
                                    sceneElementsList.isMulticellStructureName(name),
                                    hasFunctionalName);
                        }

                    } else if (event.getButton() == PRIMARY) {
                        // regular click
                        if (allLabels.contains(name)) {
                            removeLabelFor(name);
                        } else {
                            allLabels.add(name);
                            currentLabels.add(name);
                            final Shape3D entity = getEntityWithName(name);
                            insertLabelFor(name, entity);
                            highlightActiveCellLabel(entity);
                            // set the name in MainApp so that other apps opening WormGUIDES can catch this event
                            MainApp.seletedEntityLabelMainApp.set(name);
                        }
                    }
                    break;
                }
            }

            // if the node isn't a SceneElement
            if (!found) {
                // note structure
                currentNotesToMeshesMap.keySet()
                        .stream()
                        .filter(note -> currentNotesToMeshesMap.get(note).equals(node))
                        .forEachOrdered(note -> selectedNameProperty.set(note.getTagName()));
            }
        } else {
            selectedIndex.set(-1);
            selectedNameProperty.set("");
        }
    }

    private double[] vectorBWPoints(double px, double py, double pz, double qx, double qy, double qz) {
        double[] vector = new double[3];
        double vx, vy, vz;
        vx = qx - px;
        vy = qy - py;
        vz = qz - pz;
        vector[0] = vx;
        vector[1] = vy;
        vector[2] = vz;
        return vector;
    }

    private double computeZCoord(double xCoord, double yCoord, double angleOfRotation) {
        // http://stackoverflow.com/questions/14954317/know-coordinate-of-z-from-xy-value-and-angle
        // --> law of cosines: https://en.wikipedia.org/wiki/Law_of_cosines
        // http://answers.ros.org/question/42803/convert-coordinates-2d-to-3d-point-theoretical-question/
        return sqrt(pow(xCoord, 2) + pow(yCoord, 2) - (2 * xCoord * yCoord * Math.cos(angleOfRotation)));
    }

    private double rotationAngleFromMouseMovement() {
        // http://math.stackexchange.com/questions/59/calculating-an-angle-from-2-points-in-space
        double rotationAngleRadians = Math.acos(
                ((mouseOldX * mousePosX) + (mouseOldY * mousePosY) + (mouseOldZ * mousePosZ))
                        / sqrt((pow(mouseOldX, 2) + pow(mouseOldY, 2) + pow(mouseOldZ, 2))
                        * (pow(mousePosX, 2) + pow(mousePosY, 2) + pow(mousePosZ, 2))));
        return rotationAngleRadians;
    }

    /**
     * Source: http://mathworld.wolfram.com/CrossProduct.html
     *
     * @return length-3 vector containing the cross product of valid inputs, null otherwise
     */
    private double[] crossProduct(double[] u, double[] v) {
        if (u.length != 3 || v.length != 3) {
            return null;
        }
        double[] cross = new double[3];
        cross[0] = (u[1] * v[2]) - (u[2] * v[1]);
        cross[1] = (u[2] * v[0]) - (u[0] * v[2]);
        cross[2] = (u[0] * v[1]) - (u[1] * v[0]);
        return cross;
    }

    private String normalizeName(String name) {
        if (name.contains("(")
                && name.contains(")")
                && (name.indexOf("(") < name.indexOf(")"))) {
            name = name.substring(0, name.indexOf("("));
        }
        return name.trim();
    }

    private void handleMousePressed(MouseEvent event) {
        mousePosX = event.getSceneX();
        mousePosY = event.getSceneY();
    }

    /**
     * Displays the context menu for an entity in the UI
     *
     * @param name
     *         the lineage name of a cell, the scene name of a multicellular structure or tract, or the functional
     *         name of a cell body
     * @param sceneX
     *         the x coordinate of the mouse in the scene
     * @param sceneY
     *         the y coordinate of the mouse in the scene
     * @param isStructure
     *         true if the entity is a structure, false otherwise
     * @param isMulticellularStructureOrTract
     *         true if the entity is a multicellular structure or a tract model, false otherwise
     * @param hasFunctionalName
     *         true if the entity has a functional name, false otherwise
     */
    private void showContextMenu(
            String name,
            final double sceneX,
            final double sceneY,
            final boolean isStructure,
            final boolean isMulticellularStructureOrTract,
            final boolean hasFunctionalName) {

        contextMenuController.setName(name);
        contextMenuController.setColorButtonText(isStructure);

        // disable terminal cell options for multicellular structures and tracts
        if (isStructure) {
            contextMenuController.disableMoreInfoFunction(isMulticellularStructureOrTract);
            contextMenuController.disableWiredToFunction(isMulticellularStructureOrTract);
            contextMenuController.disableGeneExpressionFunction(isMulticellularStructureOrTract);
            contextMenuController.disableColorNeighborsFunction(isMulticellularStructureOrTract);
            contextMenuController.setIsStructure(true);
        }

        if (hasFunctionalName) {
            contextMenuController.disableWiredToFunction(false);
        } else {
            contextMenuController.disableWiredToFunction(true);
        }

        contextMenuStage.setX(sceneX);
        contextMenuStage.setY(sceneY);

        contextMenuStage.show();
        ((Stage) contextMenuStage.getScene().getWindow()).toFront();
    }

    /**
     * Repositions sprites, labels, callouts, and front-facing billboards
     */
    private void repositionNotes() {
        repositionSpritesAndLabels();
        repositionCallouts();
        repositionFrontFacingBillboardsAndImages();
    }

    /**
     * Repositions labels and note sprites on the overlaid sprites pane
     */
    private void repositionSpritesAndLabels() {
        for (Node entity : entityLabelMap.keySet()) {
            alignTextWithEntity(entityLabelMap.get(entity), entity, null);
        }
        for (Node entity : entitySpriteMap.keySet()) {
            alignTextWithEntity(entitySpriteMap.get(entity), entity, SPRITE);
        }
    }

    /**
     * Repositions callouts on the overlaid sprites pane
     */
    private void repositionCallouts() {
        for (Node entity : entityCalloutULMap.keySet()) {
            for (Node calloutGraphic : entityCalloutULMap.get(entity)) {
                alignTextWithEntity(calloutGraphic, entity, CALLOUT_UPPER_LEFT);
            }
        }
        for (Node entity : entityCalloutLLMap.keySet()) {
            for (Node calloutGraphic : entityCalloutLLMap.get(entity)) {
                alignTextWithEntity(calloutGraphic, entity, CALLOUT_LOWER_LEFT);
            }
        }
        for (Node entity : entityCalloutURMap.keySet()) {
            for (Node calloutGraphic : entityCalloutURMap.get(entity)) {
                alignTextWithEntity(calloutGraphic, entity, CALLOUT_UPPER_RIGHT);
            }
        }
        for (Node entity : entityCalloutLRMap.keySet()) {
            for (Node calloutGraphic : entityCalloutLRMap.get(entity)) {
                alignTextWithEntity(calloutGraphic, entity, CALLOUT_LOWER_RIGHT);
            }
        }
    }

    /**
     * Aligns a note graphic to its entity. The graphic is either a {@link Text} or a {@link VBox}.
     *
     * @param noteOrLabelGraphic
     *         the graphical representation of a note/notes (could be a {@link Text} or a {@link VBox})
     * @param entity
     *         the entity that the note graphic should attach to
     * @param noteDisplay
     *         the display type of the note, null if the graphic is a label
     */
    private void alignTextWithEntity(
            final Node noteOrLabelGraphic,
            final Node entity,
            final Display noteDisplay) {
        if (entity != null) {
            final Bounds b = entity.getBoundsInParent();
            if (b != null) {
                final Point2D p = project(
                        camera,
                        new Point3D(
                                (b.getMinX() + b.getMaxX())*getModelScaleFactor() / 2.0,
                                (b.getMinY() + b.getMaxY())*getModelScaleFactor() / 2.0,
                                (b.getMaxZ() + b.getMinZ())*getModelScaleFactor() / 2.0));
                double x = p.getX();
                double y = p.getY();
                double height = b.getHeight();
                double width = b.getWidth();

                // if graphic is a label
                if (noteDisplay == null) {
                    y -= getLabelSpriteYOffset();
                    noteOrLabelGraphic.getTransforms().clear();
                    noteOrLabelGraphic.getTransforms().add(new Translate(x, y));

                } else {
                    // if graphic is a note
                    final double calloutOffset = 10.0;
                    double calloutX;
                    double calloutY;
                    switch (noteDisplay) {
                        case SPRITE:
                            noteOrLabelGraphic.getTransforms().clear();
                            noteOrLabelGraphic.getTransforms().add(new Translate(x, y));
                            break;
                        case CALLOUT_UPPER_LEFT:
                            calloutY = y - (height + calloutOffset);
                            calloutX = x - (width + calloutOffset + (getNoteSpriteTextWidth()));
                            addCalloutSubsceneTranslation(
                                    noteOrLabelGraphic.getTransforms(),
                                    new Translate(calloutX, calloutY));
                            if (entity instanceof Sphere) {
                                realignCalloutLineToSphere(noteOrLabelGraphic, b, x, y, CALLOUT_UPPER_LEFT);
                            } else if (entity instanceof SceneElementMeshView) {
                                realignCalloutLineToSceneElementMesh(
                                        noteOrLabelGraphic,
                                        (SceneElementMeshView) entity,
                                        CALLOUT_UPPER_LEFT);
                            }
                            break;
                        case CALLOUT_LOWER_LEFT:
                            calloutY = y + (height + calloutOffset);
                            calloutX = x - (width + calloutOffset + (getNoteSpriteTextWidth()));
                            addCalloutSubsceneTranslation(
                                    noteOrLabelGraphic.getTransforms(),
                                    new Translate(calloutX, calloutY));
                            if (entity instanceof Sphere) {
                                realignCalloutLineToSphere(noteOrLabelGraphic, b, x, y, CALLOUT_LOWER_LEFT);
                            } else if (entity instanceof SceneElementMeshView) {
                                realignCalloutLineToSceneElementMesh(
                                        noteOrLabelGraphic,
                                        (SceneElementMeshView) entity,
                                        CALLOUT_LOWER_LEFT);
                            }
                            break;
                        case CALLOUT_UPPER_RIGHT:
                            calloutY = y - (height + calloutOffset);
                            calloutX = x + (width + calloutOffset);
                            addCalloutSubsceneTranslation(
                                    noteOrLabelGraphic.getTransforms(),
                                    new Translate(calloutX, calloutY));
                            if (entity instanceof Sphere) {
                                realignCalloutLineToSphere(noteOrLabelGraphic, b, x, y, CALLOUT_UPPER_RIGHT);
                            } else if (entity instanceof SceneElementMeshView) {
                                realignCalloutLineToSceneElementMesh(
                                        noteOrLabelGraphic,
                                        (SceneElementMeshView) entity,
                                        CALLOUT_UPPER_RIGHT);
                            }
                            break;
                        case CALLOUT_LOWER_RIGHT:
                            calloutY = y + (height + calloutOffset);
                            calloutX = x + (width + calloutOffset);
                            addCalloutSubsceneTranslation(
                                    noteOrLabelGraphic.getTransforms(),
                                    new Translate(calloutX, calloutY));
                            if (entity instanceof Sphere) {
                                realignCalloutLineToSphere(noteOrLabelGraphic, b, x, y, CALLOUT_LOWER_RIGHT);
                            } else if (entity instanceof SceneElementMeshView) {
                                realignCalloutLineToSceneElementMesh(
                                        noteOrLabelGraphic,
                                        (SceneElementMeshView) entity,
                                        CALLOUT_LOWER_RIGHT);
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Realigns the line segment for a scene element mesh entity's callout note by setting one end point a marker
     * point on the mesh closest to the callout and the other at the callout
     *
     * @param calloutGraphic
     *         the callout, not null
     * @param meshView
     *         the scene element mesh view, not null
     * @param display
     *         the display type specifying the type of callout, not null
     */
    private void realignCalloutLineToSceneElementMesh(
            final Node calloutGraphic,
            final SceneElementMeshView meshView,
            final Display display) {
        if (calloutGraphic != null
                && calloutGraphic instanceof Text
                && meshView != null
                && display != null) {
            final Bounds calloutBounds = calloutGraphic.getBoundsInParent();
            final Line line = calloutLineMap.get(calloutGraphic);
            if (calloutBounds != null) {
                // create invisible spherical markers (similar to the markers for notes with a location attachment)
                final List<Sphere> sphereMarkers = new ArrayList<>();
                // transform marker points as the rest of the subscene entities
                meshView.getMarkerCoordinates().forEach(marker -> {
                    final Sphere markerSphere = createLocationMarker(marker[0], marker[1], marker[2]);
                    sphereMarkers.add(markerSphere);
                    rootEntitiesGroup.getChildren().add(markerSphere);
                });
                // create projected 2d points from the marker sphere centers
                final List<Point2D> markerPoints2D = new ArrayList<>();
                sphereMarkers.forEach(marker -> {
                    final Bounds b = marker.getBoundsInParent();
                    if (b != null) {
                        markerPoints2D.add(project(
                                camera,
                                new Point3D(
                                        (b.getMinX() + b.getMaxX()) / 2,
                                        (b.getMinY() + b.getMaxY()) / 2,
                                        (b.getMinZ() + b.getMaxZ()) / 2)));
                    }
                });
                switch (display) {
                    case CALLOUT_UPPER_LEFT:
                        // find point with minimum x value and minimum y value
                        Point2D upperLeftPoint = null;
                        for (Point2D marker : markerPoints2D) {
                            if (upperLeftPoint == null) {
                                upperLeftPoint = marker;
                            } else if (marker.getX() < upperLeftPoint.getX()
                                    && marker.getY() < upperLeftPoint.getY()) {
                                upperLeftPoint = marker;
                            }
                        }
                        line.setStartX(upperLeftPoint.getX());
                        line.setStartY(upperLeftPoint.getY());
                        line.setEndX(calloutBounds.getMaxX() + CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    case CALLOUT_LOWER_LEFT:
                        // find point with minimum x value and maximum y value
                        Point2D lowerLeftPoint = null;
                        for (Point2D marker : markerPoints2D) {
                            if (lowerLeftPoint == null) {
                                lowerLeftPoint = marker;
                            } else if (marker.getX() < lowerLeftPoint.getX()
                                    && marker.getY() > lowerLeftPoint.getY()) {
                                lowerLeftPoint = marker;
                            }
                        }
                        line.setStartX(lowerLeftPoint.getX());
                        line.setStartY(lowerLeftPoint.getY());
                        line.setEndX(calloutBounds.getMaxX() + CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    case CALLOUT_UPPER_RIGHT:
                        // find point with maximum x value and minimum y value
                        Point2D upperRightPoint = null;
                        for (Point2D marker : markerPoints2D) {
                            if (upperRightPoint == null) {
                                upperRightPoint = marker;
                            } else if (marker.getX() > upperRightPoint.getX()
                                    && marker.getY() < upperRightPoint.getY()) {
                                upperRightPoint = marker;
                            }
                        }
                        line.setStartX(upperRightPoint.getX());
                        line.setStartY(upperRightPoint.getY());
                        line.setEndX(calloutBounds.getMinX() - CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    case CALLOUT_LOWER_RIGHT:
                        // find point with maximum x value and maximum y value
                        Point2D lowerRightPoint = null;
                        for (Point2D marker : markerPoints2D) {
                            if (lowerRightPoint == null) {
                                lowerRightPoint = marker;
                            } else if (marker.getX() > lowerRightPoint.getX()
                                    && marker.getY() > lowerRightPoint.getY()) {
                                lowerRightPoint = marker;
                            }
                        }
                        line.setStartX(lowerRightPoint.getX());
                        line.setStartY(lowerRightPoint.getY());
                        line.setEndX(calloutBounds.getMinX() - CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Realigns the line segment for a spherical entity's callout note by setting one endpoint at the sphere's center
     * and the other at the callout
     *
     * @param calloutGraphic
     *         the callout, not null
     * @param entityBounds
     *         the bounds for the entity that the callout is attached to, not null
     * @param display
     *         the display type specifying the type of callout, not null
     */
    private void realignCalloutLineToSphere(
            final Node calloutGraphic,
            final Bounds entityBounds,
            final double entityCenterX,
            final double entityCenterY,
            final Display display) {
        if (calloutGraphic != null
                && calloutGraphic instanceof Text
                && entityBounds != null
                && display != null) {
            final Bounds calloutBounds = calloutGraphic.getBoundsInParent();
            final Line line = calloutLineMap.get(calloutGraphic);
            if (calloutBounds != null) {
                switch (display) {
                    case CALLOUT_UPPER_LEFT:
                        line.setStartX(entityCenterX);
                        line.setStartY(entityCenterY);
                        line.setEndX(calloutBounds.getMaxX() + CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    case CALLOUT_LOWER_LEFT:
                        line.setStartX(entityCenterX);
                        line.setStartY(entityCenterY);
                        line.setEndX(calloutBounds.getMaxX() + CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    case CALLOUT_UPPER_RIGHT:
                        line.setStartX(entityCenterX);
                        line.setStartY(entityCenterY);
                        line.setEndX(calloutBounds.getMinX() - CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    case CALLOUT_LOWER_RIGHT:
                        line.setStartX(entityCenterX);
                        line.setStartY(entityCenterY);
                        line.setEndX(calloutBounds.getMinX() - CALLOUT_LINE_X_OFFSET);
                        line.setEndY(calloutBounds.getMinY() + CALLOUT_LINE_Y_OFFSET);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Adds a translation to the list of transforms for a callout. All of the callout's previous translations are
     * cleared except for the one that defines the horizontal/vertical offsets from the note.
     *
     * @param transforms
     *         the list of transforms for a callout
     * @param translation
     *         the translation to add
     */
    private void addCalloutSubsceneTranslation(final List<Transform> transforms, final Translate translation) {
        for (int i = 0; i < transforms.size(); i++) {
            if (i > 0) {
                transforms.remove(i);
            }
        }
        transforms.add(translation);
    }

    /**
     * Repositions front-facing billboards and image billboards to the right-hand side of the entities they are attached
     * to. The billboard resides on y- and z-coordinates that are the averages of the maximum and minimum y and z
     * values of the entity's bounds in the subscene.
     */
    private void repositionFrontFacingBillboardsAndImages() {
        // billboards with text
        for (Text billboard : billboardFrontEntityMap.keySet()) {
            final Node entity = billboardFrontEntityMap.get(billboard);
            if (entity != null) {
                billboard.getTransforms().clear();
                final Bounds b = entity.getBoundsInParent();
                if (b != null) {
                    billboard.getTransforms().clear();
                    double x = b.getMaxX();
                    double y = (b.getMinY() + b.getMaxY()) / 2;
                    double z = (b.getMinZ() + b.getMaxZ()) / 2;
                    billboard.getTransforms().add(new Translate(x, y, z));
                }
            }
        }
        // image billboards
        for (ImageView image : billboardImageEntityMap.keySet()) {
            final Node entity = billboardImageEntityMap.get(image);
            if (entity != null) {
                image.getTransforms().clear();
                final Bounds b = entity.getBoundsInParent();
                if (b != null) {
                    image.getTransforms().clear();
                    double x = b.getMaxX();
                    double y = (b.getMinY() + b.getMaxY()) / 2;
                    double z = (b.getMinZ() + b.getMaxZ()) / 2;
                    image.getTransforms().add(new Translate(x, y, z));
                    // scale all note billboard images down instead of inserting smaller images to preserve
                    // the image quality
                    image.getTransforms().add(new Scale(
                            getNoteBillboardImageScale(),
                            getNoteBillboardImageScale()));
                }
            }
        }
    }

    private int getIndexByCellName(final String name) {
        for (int i = 0; i < cellNames.size(); i++) {
            if (cellNames.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private int getPickedSphereIndex(final Sphere picked) {
        for (int i = 0; i < cellNames.size(); i++) {
            if (spheres.get(i).equals(picked)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Calls the service to retrieve subscene data at current timeProperty point then render entities, notes, and
     * labels
     */
    public void buildScene() {
        // Spool thread for actual rendering to subscene
        renderService.restart();
    }

    private void getSceneData() {
        final int requestedTime = timeProperty.get();
        cellNames = new LinkedList<>(asList(lineageData.getNames(requestedTime)));
        positions = new LinkedList<>();
        for (double[] position : lineageData.getPositions(requestedTime)) {
            positions.add(new Double[]{
                    position[0],
                    position[1],
                    position[2]
            });
        }
        diameters = new LinkedList<>();
        for (double diameter : lineageData.getDiameters(requestedTime)) {
            diameters.add(diameter);
        }
        rweights = new LinkedList<>();
        for (int rweight : lineageData.getRweights(requestedTime)) {
            rweights.add(rweight);
        }
        otherCells.clear();

        totalNucleiProperty.set(cellNames.size());

        spheres = new LinkedList<>();
        if (defaultEmbryoFlag) {
            meshes = new LinkedList<>();
        }

        if (defaultEmbryoFlag) {
            // start scene element list, find scene elements present at current time, build meshes
            // empty meshes and scene element references from last rendering
            // same for story elements
            if (sceneElementsList != null) {
                meshNames = new LinkedList<>(asList(sceneElementsList.getSceneElementNamesAtTime(requestedTime)));
            }

            if (!currentSceneElementMeshes.isEmpty()) {
                currentSceneElementMeshes.clear();
                currentSceneElements.clear();
            }


            sceneElementsAtCurrentTime = sceneElementsList.getSceneElementsAtTime(requestedTime);
            for (SceneElement se : sceneElementsAtCurrentTime) {
                final SceneElementMeshView mesh = se.buildGeometry(requestedTime - getShapesIndexPad());
                if (mesh != null) {
                    mesh.getTransforms().addAll(rotateX, rotateY, rotateZ);

                    // TRANSFORMS FOR LIBRARY LOADER
                    //mesh.getTransforms().add(new Rotate(180., new Point3D(1, 0, 0)));
//                    mesh.getTransforms().add(new Translate(
//                            -offsetX * xScale,
//                            offsetY * yScale,
//                            offsetZ * zScale));

                    // TRANSFORMS FOR MANUAL LOADER
                    mesh.getTransforms().add(new Translate(
                            -offsetX * xScale,
                            -offsetY * yScale,
                            -offsetZ * zScale));

                    // add rendered mesh to meshes list
                    currentSceneElementMeshes.add(mesh);
                    // add scene element to rendered scene element reference for on-click responsiveness
                    currentSceneElements.add(se);
                }
            }
        }

        // Label stuff
        entityLabelMap.clear();
        currentLabels.clear();

        for (String label : allLabels) {
            if (defaultEmbryoFlag) {
                for (SceneElement currentSceneElement : currentSceneElements) {
                    if (!currentLabels.contains(label)
                            && label.equalsIgnoreCase(normalizeName(currentSceneElement.getSceneName()))) {
                        currentLabels.add(label);
                        break;
                    }
                }
            }

            for (String cell : cellNames) {
                if (!currentLabels.contains(label) && cell.equalsIgnoreCase(label)) {
                    currentLabels.add(label);
                    break;
                }
            }
        }
        // End label stuff

        // Story stuff
        // Notes are indexed starting from 1 (1 + offset is shown to the user)
        if (storiesLayer != null) {
            currentNotes.clear();
            currentGraphicsToNotesMap.clear();
            currentNotesToMeshesMap.clear();

            entitySpriteMap.clear();
            entityCalloutULMap.clear();
            entityCalloutLLMap.clear();
            entityCalloutURMap.clear();
            entityCalloutLRMap.clear();

            billboardFrontEntityMap.clear();
            billboardImageEntityMap.clear();

            currentNotes = storiesLayer.getNotesAtTime(requestedTime);

            for (Note note : currentNotes) {
                // Revert to overlay display if we have invalid
                // display/attachment
                // type combination
                if (note.hasLocationError() || note.hasEntityNameError()) {
                    note.setDisplay(OVERLAY);
                }

                if (defaultEmbryoFlag) {
                    // make mesh views for scene elements from note resources
                    if (note.hasSceneElements()) {
                        for (SceneElement se : note.getSceneElements()) {
                            final SceneElementMeshView mesh = se.buildGeometry(requestedTime - getShapesIndexPad());
                            if (mesh != null) {
                                mesh.setMaterial(colorHash.getNoteSceneElementMaterial());
                                mesh.getTransforms().addAll(rotateX, rotateY, rotateZ);
                                mesh.getTransforms().add(new Translate(
                                        -offsetX * xScale,
                                        -offsetY * yScale,
                                        -offsetZ * zScale));
                                currentNotesToMeshesMap.put(note, mesh);
                            }
                        }
                    }
                }
            }
        }
        // End story stuff

        // SearchLayer stuff
        if (localSearchResults.isEmpty()) {
            isCellSearchedFlags = new boolean[cellNames.size()];
            isMeshSearchedFlags = new boolean[meshNames.size()];
        } else {
            consultSearchResultsList();
        }
        // End search stuff
    }

    //Only get the cell scene data to provide faster rendering speed for the previous time point feature
    private void getCellSceneData(int time) {
        final int requestedTime = time;
        cellNames = new LinkedList<>(asList(lineageData.getNames(requestedTime)));
        positions = new LinkedList<>();
        for (double[] position : lineageData.getPositions(requestedTime)) {
            positions.add(new Double[]{
                    position[0],
                    position[1],
                    position[2]
            });
        }
        diameters = new LinkedList<>();
        for (double diameter : lineageData.getDiameters(requestedTime)) {
            diameters.add(diameter);
        }
        rweights = new LinkedList<>();
        for (int rweight : lineageData.getRweights(requestedTime)) {
            rweights.add(rweight);
        }

        totalNucleiProperty.set(cellNames.size());

        //spheres = new LinkedList<>();
        if (defaultEmbryoFlag) {
            meshes = new LinkedList<>();
        }
    }

    // TODO -> this should tap the annotation manager which has just been populated with these results
    private void updateLocalSearchResults() {
        if (searchResultsList == null) {
            return;
        }
        localSearchResults.clear();
        for (String name : searchResultsList) {
            if (name.contains(" (")) {
                localSearchResults.add(name.substring(0, name.indexOf(" (")).trim());
            } else {
                localSearchResults.add(name);
            }
        }
        rebuildSubsceneFlag.set(true);
    }

    private void refreshScene() {
        // clear note billboards, cell spheres and meshes
        rootEntitiesGroup.getChildren().clear();
        rootEntitiesGroup.getChildren().add(xform);

        // clear note sprites and overlays
        storyOverlayVBox.getChildren().clear();

        final Iterator<Node> iter = spritesPane.getChildren().iterator();
        while (iter.hasNext()) {
            Node node = iter.next();
            if (node instanceof Text) {
                iter.remove();
            } else if (node instanceof VBox && node != storyOverlayVBox) {
                iter.remove();
            } else if (node instanceof Line) {
                iter.remove();
            }
        }

        if (defaultEmbryoFlag) {
            double newrotate = computeInterpolatedValue(timeProperty.get(), keyFramesRotate, keyValuesRotate);
            indicatorRotation.setAngle(-newrotate);
            indicatorRotation.setAxis(new Point3D(1, 0, 0));
        }
    }

    private void addEntitiesAndNotes() {
        final List<Shape3D> entities = new ArrayList<>();
        final List<Node> noteGraphics = new ArrayList<>();

        // add cell and cell body geometries
        addEntities(entities);
        entities.sort(opacityComparator);
        rootEntitiesGroup.getChildren().addAll(entities);

        // add notes
        insertOverlayTitles();

        if (!currentNotes.isEmpty()) {
            addNoteGeometries(noteGraphics);
        }

        // add labels
        Shape3D activeEntity = null;
        for (String name : currentLabels) {
            insertLabelFor(name, getEntityWithName(name));

            if (name.equalsIgnoreCase(selectedNameProperty.get())) {
                activeEntity = getEntityWithName(name);
            }
        }
        if (activeEntity != null) {
            highlightActiveCellLabel(activeEntity);
            // set the name in MainApp so that other apps opening WormGUIDES can catch this event
            MainApp.seletedEntityLabelMainApp.set(selectedNameProperty.get());
        }

        if (!noteGraphics.isEmpty()) {
            // insert note graphics to the beginning of the group so they can be rendered last (otherwise, the notes
            // will not be completely visible behind semi-opaque entities)
            rootEntitiesGroup.getChildren().addAll(0, noteGraphics);
        }
        rootEntitiesGroup.setScaleX(rootEntitiesGroup.getScaleX() * getModelScaleFactor());
        rootEntitiesGroup.setScaleY(rootEntitiesGroup.getScaleY() * getModelScaleFactor());
        rootEntitiesGroup.setScaleZ(rootEntitiesGroup.getScaleZ() * getModelScaleFactor());

        repositionNotes();
    }

    //For Adding previous time points graphics of cells that exitst in the ruleslist
    //For previous time points feature
    private void addEntitiesNoNotesWithColorRule() {
        List<Shape3D> entities = new ArrayList();
        this.addColoredGeometries(entities);
        entities.sort(this.opacityComparator);
        this.rootEntitiesGroup.getChildren().addAll(entities);
    }

    /**
     * Inserts appropriate 3d geometries into the list of entities that is later added to the subscene
     *
     * @param entities
     *         list of subscene entities
     */
    private void addEntities(final List<Shape3D> entities) {
        // add spheres
        addCellGeometries(entities);
        // add scene element meshes (from notes and from scene elements list)
        addSceneElementGeometries(entities);
    }

    /**
     * Inserts spherical cells into the list of entities that is later added to the subscene. "Other" cells with
     * visibility under the visibility cutoff are not rendered.
     *
     * @param entities
     *         list of subscene entities
     */
    private void addCellGeometries(final List<Shape3D> entities) {
        final Material othersMaterial = colorHash.getOthersMaterial(othersOpacityProperty.get());
        final ListIterator<String> iter = cellNames.listIterator();
        int index = -1;
        while (iter.hasNext()) {
            final String cellName = iter.next();
            index++;

            // size the sphere
            double radius;
            if (!uniformSize) {
                radius = getSizeScale() * diameters.get(index) / 2;
            } else {
                radius = getSizeScale() * getUniformRadius();
            }
            final Sphere sphere = new Sphere(radius);

            // create the color material
            Material material;
            // if in search, do highlighting
            if (isInSearchMode) {
                if (isCellSearchedFlags[index]) {
                    material = colorHash.getHighlightMaterial();
                } else {
                    material = colorHash.getTranslucentMaterial();
                    sphere.setDisable(true);
                }

            } else {
                // if not in search (flashlight mode), consult active list of rules
                final List<Color> colors = new ArrayList<>();
                for (Rule rule : rulesList) {
                    //System.out.println("checking rule: " + rule.getSearchedText());
                    if (rule.appliesToCellNucleus(cellName)) {
                        //System.out.println("rule applies to: " + cellName);
                        colors.add(web(rule.getColor().toString()));
                        // check if opacity of rule is below cutoff, then it's not selectable
                        if (rule.getColor().getOpacity() <= getSelectabilityVisibilityCutoff()) {
                            sphere.setDisable(true);
                        }
                    }
                }

                if (colors.isEmpty()) {
                    // do not render this "other" cell if visibility is under the cutoff
                    // remove this cell from scene data at current time point
                    double opacity = othersOpacityProperty.get();
                    if (opacity <= getVisibilityCutoff()) {
                        iter.remove();
                        positions.remove(index);
                        diameters.remove(index);
                        index--;
                        continue;
                    } else {
                        // experimental feature
                        if (expressionOn) {
                            if (rweights.size() > index && (rweights.get(index) >= exprLowerProperty.intValue() && rweights.get(index) < exprUpperProperty.intValue())) {
                                material = colorHash.getExpressionMaterial(opacity, rweights.get(index), exprLowerProperty.intValue(), exprUpperProperty.intValue());
                            } else {
                                material = othersMaterial;
                            }
                        } else {
                            material = othersMaterial;
                        }

                        if (opacity <= getSelectabilityVisibilityCutoff()) {
                            sphere.setDisable(true);
                        }
                    }
                } else {
                    colors.sort(colorComparator);
                    material = colorHash.getMaterial(colors);
                }
            }
            sphere.setMaterial(material);

            // transform and add sphere to list
            sphere.getTransforms().addAll(rotateX, rotateY, rotateZ);
            final Double[] position = positions.get(index);
            sphere.getTransforms().add(new Translate(
                    position[X_COR_INDEX] * xScale,
                    position[Y_COR_INDEX] * yScale,
                    position[Z_COR_INDEX] * zScale));
            spheres.add(sphere);

            if (!sphere.isDisable()) {
                sphere.setOnMouseEntered((MouseEvent event) -> {
                    spritesPane.setCursor(HAND);
                    // make label appear
                    if (!currentLabels.contains(cellName.toLowerCase())) {
                        insertTransientLabel(cellName, getEntityWithName(cellName));
                    }
                });
                sphere.setOnMouseExited(event -> {
                    spritesPane.setCursor(DEFAULT);
                    // make label disappear
                    removeTransientLabel();
                });
            }

            entities.add(sphere);
        }
    }

    /**
     * Inserts meshes into the list of entities that is later added to the subscene. "Other" scene elements with
     * visibility under the visibility cutoff are not rendered.
     *
     * @param entities
     *         list of subscene entities
     */
    private void addSceneElementGeometries(final List<Shape3D> entities) {
        if (defaultEmbryoFlag) {
            // add scene elements from notes
            entities.addAll(currentNotesToMeshesMap.keySet()
                    .stream()
                    .map(currentNotesToMeshesMap::get)
                    .collect(toList()));

            // consult rules/search results
            final ListIterator<SceneElement> iter = currentSceneElements.listIterator();
            SceneElement sceneElement;
//            SceneElementMeshView meshView;
            MeshView meshView;
            int index = -1;
            while (iter.hasNext()) {
                index++;
                sceneElement = iter.next();
                meshView = currentSceneElementMeshes.get(index);

                if (isInSearchMode) {
                    if (cellBodyTicked && isMeshSearchedFlags[index]) {
                        meshView.setMaterial(colorHash.getHighlightMaterial());
                    } else {
                        meshView.setMaterial(colorHash.getTranslucentMaterial());
                        meshView.setDisable(true);
                    }
                } else {
                    // in regular viewing mode
                    final List<String> structureCells = sceneElement.getAllCells();
                    final List<Color> colors = new ArrayList<>();

                    if (structureCells.isEmpty()) {
                        // check if any rules apply to this no-cell structure
                        for (Rule rule : rulesList) {
                            if (rule.appliesToStructureWithSceneName(sceneElement.getSceneName())) {
                                colors.add(rule.getColor());
                                if (rule.getColor().getOpacity() <= getSelectabilityVisibilityCutoff()) {
                                    meshView.setDisable(true);
                                }
                            }
                        }
                    } else {
                        for (Rule rule : rulesList) {
                            if (rule.appliesToStructureWithSceneName(sceneElement.getSceneName())) {
                                colors.add(rule.getColor());

                                if (rule.getColor().getOpacity() <= getSelectabilityVisibilityCutoff()) {
                                    meshView.setDisable(true);
                                }
                            } else {
                                for (int g = 0; g < structureCells.size(); g++) {
                                    if (rule.appliesToCellBody(structureCells.get(g))) {
                                        colors.add(rule.getColor());
                                        if (rule.getColor().getOpacity() <= getSelectabilityVisibilityCutoff()) {
                                            meshView.setDisable(true);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // if no rules applied
                    if (colors.isEmpty()) {
                        // do not render this "other" scene element if visibility is under the cutoff
                        // remove scene element and its mesh from scene data at current time point
                        final double opacity = othersOpacityProperty.get();
                        if (opacity <= getVisibilityCutoff()) {
                            iter.remove();
                            currentSceneElementMeshes.remove(index--);
                            continue;
                        } else {
                            meshView.setMaterial(colorHash.getOthersMaterial(othersOpacityProperty.get()));
                            if (opacity <= getSelectabilityVisibilityCutoff()) {
                                meshView.setDisable(true);
                            }
                        }
                    } else {
                        colors.sort(colorComparator);
                        meshView.setMaterial(colorHash.getMaterial(colors));
                    }
                }

                if (sceneElement.isSelectable() && !meshView.isDisable()) {
                    final String sceneName = sceneElement.getSceneName();
                    meshView.setOnMouseEntered(event -> {
                        spritesPane.setCursor(HAND);
                        // make label appear
                        final String name = normalizeName(sceneName);
                        if (!currentLabels.contains(name.toLowerCase())) {
                            insertTransientLabel(name, getEntityWithName(name));
                        }
                    });
                    meshView.setOnMouseExited(event -> {
                        spritesPane.setCursor(DEFAULT);
                        // make label disappear
                        removeTransientLabel();
                    });
                } else {
                    meshView.setDisable(true);
                }
                entities.add(meshView);
            }
        }
    }

    /**
     * only add cells that are on the ruleslist without any interactive functions for faster rendering speed.
     * does not add any extra graphic to the entities when in search mode
     *
     * @param entities
     *         list of subscene entities
     */
    private void addColoredGeometries(final List<Shape3D> entities) {
        final Material othersMaterial = colorHash.getOthersMaterial(othersOpacityProperty.get());
        final ListIterator<String> iter = cellNames.listIterator();
        int index = -1;
        boolean needRender = false;
        while (iter.hasNext()) {
            final String cellName = iter.next();
            index++;
            needRender = false;

            // create the color material
            Material material;
            // if in search, skip rendering previous time point
            if (isInSearchMode) {
                break;
            } else {
                // if not in search (flashlight mode), consult active list of rules
                // check if a cell is in the rulelist
                final List<Color> colors = new ArrayList<>();
                for (Rule rule : rulesList) {
                    if (rule.appliesToCellNucleus(cellName) && rule.getColor().getOpacity() > getSelectabilityVisibilityCutoff()) {
                        colors.add(web(rule.getColor().toString()));
                        needRender = true;
                    }
                }

                if (needRender) { //render the cell if conditions are met
                    // size the sphere
                    double radius;
                    if (!uniformSize) {
                        radius = getSizeScale() * diameters.get(index) / 2;
                    } else {
                        radius = getSizeScale() * getUniformRadius();
                    }
                    final Sphere sphere = new Sphere(radius);

                    colors.sort(colorComparator);
                    material = colorHash.getMaterial(colors);

                    sphere.setMaterial(material);

                    // transform and add sphere to list
                    sphere.getTransforms().addAll(rotateX, rotateY, rotateZ);
                    final Double[] position = positions.get(index);
                    sphere.getTransforms().add(new Translate(
                            position[X_COR_INDEX] * xScale,
                            position[Y_COR_INDEX] * yScale,
                            position[Z_COR_INDEX] * zScale));

                    //spheres.add(sphere);

                    entities.add(sphere);
                } else { // remove this cell from scene data at current time point
                    iter.remove();
                    positions.remove(index);
                    diameters.remove(index);
                    index--;
                    continue;
                }
            }
        }
    }

    /**
     * Removes the label from the entity with the specified name
     *
     * @param name
     *         the name of the entity to be unlabeled
     */
    private void removeLabelFor(final String name) {
        allLabels.remove(name);
        currentLabels.remove(name);

        Node entity = getEntityWithName(name);

        if (entity != null) {
            removeLabelFrom(entity);
        }
    }

    /**
     * Removes the label from the specified entity
     *
     * @param entity
     *         the entity to be unlabeled
     */
    private void removeLabelFrom(final Node entity) {
        if (entity != null) {
            spritesPane.getChildren().remove(entityLabelMap.get(entity));
            entityLabelMap.remove(entity);
        }
    }

    /**
     * Inserts a name label for the specified 3D entity
     *
     * @param name
     *         name to show on the label
     * @param entity
     *         3D shape to label
     */
    private void insertLabelFor(final String name, final Node entity) {
        // if label is already in scene, make all labels white and highlight that one
        final Text label = entityLabelMap.get(entity);
        if (label != null) {
            for (Node shape : entityLabelMap.keySet()) {
                entityLabelMap.get(shape).setFill(web(SPRITE_COLOR_HEX));
            }
            label.setFill(web(ACTIVE_LABEL_COLOR_HEX));
            return;
        }

        // otherwise, create a highlight new label
        final String funcName = getFunctionalNameByLineageName(name);
        final Text text;
        if (funcName != null) {
            text = makeNoteSpriteText(funcName);
        } else {
            text = makeNoteSpriteText(name);
        }
        text.setOnMouseEntered(event -> text.setCursor(HAND));
        text.setOnMouseExited(event -> text.setCursor(DEFAULT));

        final String tempName = name;
        text.setOnMouseClicked(event -> removeLabelFor(tempName));
        text.setWrappingWidth(-1);

        entityLabelMap.put(entity, text);

        spritesPane.getChildren().add(text);

        alignTextWithEntity(text, entity, null);
    }

    private void highlightActiveCellLabel(Shape3D entity) {
        for (Node shape3D : entityLabelMap.keySet()) {
            entityLabelMap.get(shape3D).setFill(web(SPRITE_COLOR_HEX));
        }
        if (entity != null && entityLabelMap.get(entity) != null) {
            entityLabelMap.get(entity).setFill(web(ACTIVE_LABEL_COLOR_HEX));
        }
    }

    /**
     * @return The 3D entity with input name. Priority is given to nuclei (if a mesh and a nucleus have the same name,
     * then the nucleus sphere is returned).
     */
    private Shape3D getEntityWithName(final String name) {
        // sphere label
        for (int i = 0; i < cellNames.size(); i++) {
            if (spheres.get(i) != null && cellNames.get(i).equalsIgnoreCase(name)) {
                return spheres.get(i);
            }
        }
        // mesh view label
        if (defaultEmbryoFlag) {
            for (int i = 0; i < currentSceneElements.size(); i++) {
                if (normalizeName(currentSceneElements.get(i).getSceneName()).equalsIgnoreCase(name)
                        && currentSceneElementMeshes.get(i) != null) {
                    return currentSceneElementMeshes.get(i);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a note into the list of Text nodes mapped to a specific subscene entity if the list already exists.
     * Creates a list and then adds the note if it does not.
     *
     * @param noteGraphic
     *         the Text object
     * @param subsceneEntity
     *         the subscene entity
     * @param entityCalloutMap
     *         the callout map specific to the callout position
     */
    private void addNoteGraphicToEntityCalloutMap(
            final Text noteGraphic,
            final Node subsceneEntity,
            final Map<Node, List<Text>> entityCalloutMap) {
        if (entityCalloutMap.get(subsceneEntity) == null) {
            final List<Text> noteGraphicsList = new ArrayList<>();
            entityCalloutMap.put(subsceneEntity, noteGraphicsList);
            noteGraphicsList.add(noteGraphic);
        } else {
            entityCalloutMap.get(subsceneEntity).add(noteGraphic);
        }
    }

    /**
     * Inserts note geometries into the subscene. The callout notes objects are tracked in their own maps to their
     * respective entities, but are graphically inserted into the subscene separately later because they have to keep
     * track of their horizontal/vertical offsets from the entity.
     *
     * @param list
     *         the list of nodes that billboards are added to, which are added to to the subscene. Note overlays
     *         and sprites are added to the pane that contains the subscene.
     */
    private void addNoteGeometries(final List<Node> list) {
        for (Note note : currentNotes) {
            if (note.isVisible()) {
                // map notes to their sphere/mesh view
                final Node noteGraphic = makeNoteGraphic(note);
                currentGraphicsToNotesMap.put(noteGraphic, note);

                noteGraphic.setOnMouseEntered(event -> spritesPane.setCursor(HAND));
                noteGraphic.setOnMouseExited(event -> spritesPane.setCursor(DEFAULT));

                // callouts
                if (note.isCallout()) {
                    Shape3D subsceneEntity = null;
                    if (note.attachedToCell()) {
                        subsceneEntity = getSubsceneSphereWithName(note.getCellName());
                    } else if (note.attachedToStructure() && defaultEmbryoFlag) {
                        subsceneEntity = getSubsceneMeshWithName(note.getCellName());
                    }
                    if (subsceneEntity != null) {
                        switch (note.getTagDisplay()) {
                            case CALLOUT_UPPER_LEFT:
                                if (noteGraphic instanceof Text) {
                                    addNoteGraphicToEntityCalloutMap(
                                            (Text) noteGraphic,
                                            subsceneEntity,
                                            entityCalloutULMap);
                                    spritesPane.getChildren().add(noteGraphic);
                                    noteGraphic.getTransforms().add(new Translate(
                                            -note.getCalloutHorizontalOffset(),
                                            -note.getCalloutVerticalOffset()));
                                    final Line line = new Line(0, 0, 0, 0);
                                    line.setStyle("-fx-stroke-width: 2; -fx-stroke: #DDDDDD;");
                                    spritesPane.getChildren().add(line);
                                    // map callout text to its line so they can be repositioned together during
                                    // note-entity alignment
                                    calloutLineMap.put((Text) noteGraphic, line);
                                }
                                break;
                            case CALLOUT_LOWER_LEFT:
                                if (noteGraphic instanceof Text) {
                                    addNoteGraphicToEntityCalloutMap(
                                            (Text) noteGraphic,
                                            subsceneEntity,
                                            entityCalloutLLMap);
                                    spritesPane.getChildren().add(noteGraphic);
                                    noteGraphic.getTransforms().add(new Translate(
                                            -note.getCalloutHorizontalOffset(),
                                            note.getCalloutVerticalOffset()));
                                    final Line line = new Line(0, 0, 0, 0);
                                    line.setStyle("-fx-stroke-width: 2; -fx-stroke: #FFFFFF;");
                                    spritesPane.getChildren().add(line);
                                    calloutLineMap.put((Text) noteGraphic, line);
                                }
                                break;
                            case CALLOUT_UPPER_RIGHT:
                                if (noteGraphic instanceof Text) {
                                    addNoteGraphicToEntityCalloutMap(
                                            (Text) noteGraphic,
                                            subsceneEntity,
                                            entityCalloutURMap);
                                    spritesPane.getChildren().add(noteGraphic);
                                    noteGraphic.getTransforms().add(new Translate(
                                            note.getCalloutHorizontalOffset(),
                                            -note.getCalloutVerticalOffset()));
                                    final Line line = new Line(0, 0, 0, 0);
                                    line.setStyle("-fx-stroke-width: 2; -fx-stroke: #DDDDDD;");
                                    spritesPane.getChildren().add(line);
                                    calloutLineMap.put((Text) noteGraphic, line);
                                }
                                break;
                            case CALLOUT_LOWER_RIGHT:
                                if (noteGraphic instanceof Text) {
                                    addNoteGraphicToEntityCalloutMap(
                                            (Text) noteGraphic,
                                            subsceneEntity,
                                            entityCalloutLRMap);
                                    spritesPane.getChildren().add(noteGraphic);
                                    noteGraphic.getTransforms().add(new Translate(
                                            note.getCalloutHorizontalOffset(),
                                            note.getCalloutVerticalOffset()));
                                    final Line line = new Line(0, 0, 0, 0);
                                    line.setStyle("-fx-stroke-width: 2; -fx-stroke: #DDDDDD;");
                                    spritesPane.getChildren().add(line);
                                    calloutLineMap.put((Text) noteGraphic, line);
                                }
                                break;
                            default:
                                break;
                        }
                    }

                } else if (note.isSprite()) {
                    // sprites
                    // location attachment
                    if (note.attachedToLocation()) {
                        final VBox box = new VBox(3);
                        box.setPrefWidth(getNoteSpriteTextWidth());
                        box.getChildren().add(noteGraphic);
                        // add     inivisible location marker to scene at location specified by note
                        final Sphere marker = createLocationMarker(note.getX(), note.getY(), note.getZ());
                        rootEntitiesGroup.getChildren().add(marker);
                        entitySpriteMap.put(marker, box);
                        // add vbox to sprites pane
                        spritesPane.getChildren().add(box);

                    } else {
                        Node subsceneEntity = null;
                        if (note.attachedToCell()) {
                            // cell attachment
                            subsceneEntity = getSubsceneSphereWithName(note.getCellName());
                        } else if (note.attachedToStructure() && defaultEmbryoFlag) {
                            // structure attachment
                            subsceneEntity = getSubsceneMeshWithName(note.getCellName());
                        }
                        if (subsceneEntity != null) {
                            // if another non-callout note is already attached to the subscene entity,
                            // create a vbox for note stacking
                            if (!entitySpriteMap.containsKey(subsceneEntity)) {
                                final VBox box = new VBox(3);
                                box.getChildren().add(noteGraphic);
                                entitySpriteMap.put(subsceneEntity, box);
                                spritesPane.getChildren().add(box);
                            } else {
                                // otherwise add note to the existing vbox for that entity
                                entitySpriteMap.get(subsceneEntity).getChildren().add(noteGraphic);
                            }
                        }
                    }
                } else if (note.isBillboardFront()) {
                    if (noteGraphic instanceof Text) {
                        if (note.attachedToLocation()) {
                            final Sphere marker = createLocationMarker(note.getX(), note.getY(), note.getZ());
                            rootEntitiesGroup.getChildren().add(marker);
                            billboardFrontEntityMap.put(
                                    (Text) noteGraphic,
                                    marker);
                        } else if (note.attachedToCell()) {
                            billboardFrontEntityMap.put(
                                    (Text) noteGraphic,
                                    getSubsceneSphereWithName(note.getCellName()));
                        } else if (note.attachedToStructure() && defaultEmbryoFlag) {
                            final SceneElementMeshView meshView = getSubsceneMeshWithName(note.getCellName());
                            if (meshView != null) {
                                billboardFrontEntityMap.put(
                                        (Text) noteGraphic,
                                        meshView);
                            }
                        }
                    }

                } else if (note.isBillboardImage()) {
                    if (noteGraphic != null && noteGraphic instanceof ImageView) {
                        // no need to do anything with the note graphic text since it will not be shown
                        // only the image view is shown
                        if (note.attachedToLocation()) {
                            final Sphere marker = createLocationMarker(note.getX(), note.getY(), note.getZ());
                            rootEntitiesGroup.getChildren().add(marker);
                            billboardImageEntityMap.put(
                                    (ImageView) noteGraphic,
                                    marker);
                        } else if (note.attachedToCell()) {
                            billboardImageEntityMap.put(
                                    (ImageView) noteGraphic,
                                    getSubsceneSphereWithName(note.getCellName()));
                        } else if (note.attachedToStructure() && defaultEmbryoFlag) {
//                            final SceneElementMeshView meshView = getSubsceneMeshWithName(note.getCellName());
//                            if (meshView != null) {
//                                billboardImageEntityMap.put(
//                                        (ImageView) noteGraphic,
//                                        meshView);
//                            }
                        }
                    }

                } else if (note.isBillboard()) {
                    // TODO non-front-facing billboard positioning has to be fixed (see below)
                    // they currently move with the entities they are attached to, but are offset far away - need to
                    // find the cause of this offset)
                    if (note.attachedToLocation()) {
                        // location attachment
                        noteGraphic.getTransforms().addAll(rotateX, rotateY, rotateZ);
                        noteGraphic.getTransforms().add(new Translate(note.getX(), note.getY(), note.getZ()));
                    } else if (note.attachedToCell()) {
                        // cell attachment
                        final Sphere sphere = getSubsceneSphereWithName(note.getCellName());
                        if (sphere != null) {
//                            double offset = 5;
//                            if (!uniformSize) {
//                                offset = sphere.getRadius() + 2;
//                            }
                            noteGraphic.getTransforms().addAll(sphere.getTransforms());
//                            noteGraphic.getTransforms().add(new Translate(offset, offset));
                        }
                    } else if (note.attachedToStructure() && defaultEmbryoFlag) {
                        // structure attachment
//                        final SceneElementMeshView meshView = getSubsceneMeshWithName(note.getCellName());
//                        if (meshView != null) {
//                            double offset = 5;
//                            noteGraphic.getTransforms().addAll(meshView.getTransforms());
////                            noteGraphic.getTransforms().add(new Translate(offset, offset));
//                        }
                    }
                }

                // add graphic to appropriate place (the subscene itself, overlay box, or on sprites pane
                // overlaid on the subscene)
                final Display display = note.getTagDisplay();
                if (display != null) {
                    switch (display) {
                        case CALLOUT_UPPER_LEFT: // all callouts fall to sprite case
                        case CALLOUT_UPPER_RIGHT:
                        case CALLOUT_LOWER_LEFT:
                        case CALLOUT_LOWER_RIGHT:
                        case SPRITE: // do nothing
                            break;
                        case IMAGE: // fall to billboard case
                        case BILLBOARD_FRONT: // fall to billboard case
                        case BILLBOARD:
                            list.add(noteGraphic);
                            break;
                        case OVERLAY: // fall to default case
                        case BLANK: // fall to default case
                        default:
                            storyOverlayVBox.getChildren().add(noteGraphic);
                            break;
                    }
                }
            }
        }
    }

    /**
     * @param sceneName
     *         the scene name of the scene element mesh
     *
     * @return the mesh view representing the scene element with that scene name, null if none were found in the
     * current time frame
     */
    private SceneElementMeshView getSubsceneMeshWithName(final String sceneName) {
        for (int i = 0; i < currentSceneElements.size(); i++) {
            if (currentSceneElements.get(i).getSceneName().equalsIgnoreCase(sceneName)) {
                return currentSceneElementMeshes.get(i);
            }
        }
        return null;
    }

    /**
     * @param lineageName
     *         the lineage name of the cell
     *
     * @return the sphere representing the cell with that lineage name, null if none were found in the current time
     * frame
     */
    private Sphere getSubsceneSphereWithName(final String lineageName) {
        for (int i = 0; i < cellNames.size(); i++) {
            if (cellNames.get(i).equalsIgnoreCase(lineageName) && spheres.get(i) != null) {
                return spheres.get(i);
            }
        }
        return null;
    }

    private void insertOverlayTitles() {
        if (storiesLayer != null) {
            final Text infoPaneTitle = makeNoteOverlayText("Story Title:");
            if (storiesLayer.getActiveStory() != null) {
                final Text storyTitle = makeNoteOverlayText(storiesLayer.getActiveStory().getTitle());
                storyOverlayVBox.getChildren().addAll(infoPaneTitle, storyTitle);
            } else {
                final Text noStoryTitle = makeNoteOverlayText("none");
                storyOverlayVBox.getChildren().addAll(infoPaneTitle, noStoryTitle);
            }
        }
    }

    private Text makeNoteOverlayText(final String title) {
        final Text text = new Text(title);
        text.setFill(web(SPRITE_COLOR_HEX));
        text.setFontSmoothingType(LCD);
        text.setWrappingWidth(storyOverlayVBox.getWidth());
        text.setFont(getSpriteAndOverlayFont());
        return text;
    }

    private Text makeNoteSpriteText(final String title) {
        final Text text = makeNoteOverlayText(title);
        text.setWrappingWidth(getNoteSpriteTextWidth());
        return text;
    }

    /**
     * Creates the text for the orientation indicator
     *
     * @param string
     *         the indicator string ("R    L", "A    P", or "V    D")
     *
     * @return the text
     */
    private Text makeOrientationIndicatorText(final String string) {
        final Text text = new Text(string);
        text.setFont(getOrientationIndicatorFont());
        text.setSmooth(false);
        text.setFontSmoothingType(LCD);
        text.setFill(web(SPRITE_COLOR_HEX));
        return text;
    }

    private Text makeNoteBillboardText(final String title) {
        final Text text = new Text(title);
        text.setWrappingWidth(getNoteBillboardTextWidth());
        text.setFont(getBillboardFont());
        text.setSmooth(false);
        text.setFontSmoothingType(LCD);
        text.setFill(web(SPRITE_COLOR_HEX));
        text.getTransforms().add(new Scale(getBillboardScale(), getBillboardScale()));
        return text;
    }

    private Sphere createLocationMarker(final double x, final double y, final double z) {
        final Sphere sphere = new Sphere(0.05);
        sphere.getTransforms().addAll(rotateX, rotateY, rotateZ);
        sphere.getTransforms().add(new Translate(
                (-offsetX + x) * xScale,
                (-offsetY + y) * yScale,
                (-offsetZ + z) * zScale));
        // make marker transparent
        sphere.setMaterial(colorHash.getOthersMaterial(0));
        return sphere;
    }

    /**
     * Creates the graphic for a note, whether the graphic is a text or an image view
     *
     * @param note
     *         the note to create the graphic for
     *
     * @return the note graphic
     */
    private Node makeNoteGraphic(final Note note) {
        String title = note.getTagName();
        if (note.isExpandedInScene() && note.getTagContents().length() > 0) {
            title += ": " + note.getTagContents();
        } else if (note.getTagContents().length() > 0) {
            title += " [more...]";
        }

        Node node = null;
        if (note.getTagDisplay() != null) {
            switch (note.getTagDisplay()) {
                case CALLOUT_UPPER_LEFT: // fall to callout_lower_left case

                case CALLOUT_LOWER_LEFT: // fall to sprite case
                    Text t = makeNoteSpriteText(title);
                    t.setTextAlignment(TextAlignment.RIGHT); // make the text right aligned for left side callouts
                    node = t;
                    break;

                case CALLOUT_UPPER_RIGHT: // fall to sprite case

                case CALLOUT_LOWER_RIGHT: // fall to sprite case

                case SPRITE:
                    node = makeNoteSpriteText(title);
                    break;

                case BILLBOARD:
                    node = makeNoteBillboardText(title);
                    break;

                case BILLBOARD_FRONT:
                    node = makeNoteBillboardText(title);
                    break;

                case IMAGE:
                    node = createImageView(note.getResourceLocation());
                    break;

                case OVERLAY: // fall to default case

                case BLANK: // fall to default case

                default:
                    node = makeNoteOverlayText(title);
                    break;
            }
        }
        return node;
    }

    private void buildCamera() {
        camera = new PerspectiveCamera(true);
        xform = new Xform();
        xform.reset();
        rootEntitiesGroup.getChildren().add(xform);
        xform.getChildren().add(camera);
        camera.setNearClip(getCameraNearClip());
        camera.setFarClip(getCameraFarClip());
        camera.setTranslateZ(getCameraInitialDistance());
        subscene.setCamera(camera);
    }

    /**
     * Consults the local search results list (containing only lineage names, no functional names) and sets the flags
     * for cell and mesh highlighting. If the sphere or mesh view should be highlighted in the current active search,
     * then the flag at its index it set to true.
     */
    private void consultSearchResultsList() {
        isCellSearchedFlags = new boolean[cellNames.size()];
        if (defaultEmbryoFlag) {
            isMeshSearchedFlags = new boolean[currentSceneElements.size()];
        }

        // cells
        for (int i = 0; i < cellNames.size(); i++) {
            isCellSearchedFlags[i] = localSearchResults.contains(cellNames.get(i));
        }

        // meshes
        if (defaultEmbryoFlag) {
            SceneElement sceneElement;
            String sceneName;
            for (int i = 0; i < currentSceneElements.size(); i++) {
                sceneElement = currentSceneElements.get(i);
                sceneName = sceneElement.getSceneName();
                isMeshSearchedFlags[i] = localSearchResults.contains(sceneName);
            }
        }
    }

    public boolean captureImagesForMovie() {
        movieFiles.clear();
        this.count = 0;

        final Stage fileChooserStage = new Stage();

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save Location");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("MOV File", "*.mov"));

        final File tempFile = fileChooser.showSaveDialog(fileChooserStage);

        if (tempFile == null) {
            return false;
        }

        // save the name from the file chooser for later MOV file
        movieName = tempFile.getName();
        moviePath = tempFile.getAbsolutePath();

        // make a temp directory for the frames at the given save location
        String path = tempFile.getAbsolutePath();
        if (path.lastIndexOf("/") < 0) {
            path = path.substring(0, path.lastIndexOf("\\") + 1) + "tempFrameDir";
        } else {
            path = path.substring(0, path.lastIndexOf("/") + 1) + "tempFrameDir";
        }

        frameDir = new File(path);

        try {
            frameDir.mkdir();
        } catch (SecurityException se) {
            return false;
        }

        this.frameDirPath = frameDir.getAbsolutePath() + "/";

        captureVideo.set(true);
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (captureVideo.get()) {
//                    runLater(() -> {
//                        WritableImage screenCapture = subscene.snapshot(new SnapshotParameters(), null);
//                        try {
//                            File file = new File(frameDirPath + "movieFrame" + count++ + ".JPEG");
//
//                            if (file != null) {
//                                RenderedImage renderedImage = fromFXImage(screenCapture, null);
//                                write(renderedImage, "JPEG", file);
//                                movieFiles.addElement(file);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("Could not write frame of movie to file.");
//                        }
//                    });
//                } else {
//                    timer.cancel();
//                }
//            }
//        }, 0, 1000);

        return true;
    }

    /**
     * Converts saved frames of development in "play" mode to a single video file
     * Notes:
     * - The outputted video has the dimensions of the subscene width and height at capture time (if the
     * window is resized during capture, these parameters will be their values at the time "Stop Capture..."
     * is pressed)
     * - The frame rate is set at 6 frames/sec
     */
    public void convertImagesToMovie() {
        captureVideo.set(false);
        javaPictures.clear();

        for (File movieFile : movieFiles) {
            JavaPicture jp = new JavaPicture();

            jp.loadImage(movieFile);

            javaPictures.addElement(jp);
        }

        if (javaPictures.size() > 0) {
            new JpegImagesToMovie(
                    (int) subscene.getWidth(),
                    (int) subscene.getHeight(),
                    6,
                    movieName,
                    javaPictures);

            // move the movie to the originally specified location
            final File movJustMade = new File(movieName);
            movJustMade.renameTo(new File(moviePath + ".mov"));

            // remove the .movtemp.jpg file
            final File movtempjpg = new File(".movtemp.jpg");
            movtempjpg.delete();
        }

        // remove all of the images in the frame directory
        if (frameDir != null && frameDir.isDirectory()) {
            final File[] frames = frameDir.listFiles();
            if (frames != null) {
                for (File frame : frames) {
                    frame.delete();
                }
            }
            frameDir.delete();
        }
    }

    /**
     * Saves a snapshot of the screen
     */
    public void stillscreenCapture() {
        final Stage fileChooserStage = new Stage();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save Location");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG File", "*.png"));

        final WritableImage screenCapture = subscene.snapshot(new SnapshotParameters(), null);

        //write the image to a file
        try {
            final File file = fileChooser.showSaveDialog(fileChooserStage);
            if (file != null) {
                final RenderedImage renderedImage = fromFXImage(screenCapture, null);
                write(renderedImage, "png", file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printCellNames() {
        for (int i = 0; i < cellNames.size(); i++) {
            System.out.println(cellNames.get(i) + CS + spheres.get(i));
        }
    }

    public void printMeshNames() {
        if (defaultEmbryoFlag) {
            for (int i = 0; i < meshNames.size(); i++) {
                System.out.println(meshNames.get(i) + CS + meshes.get(i));
            }
        }
    }

    /**
     * Sets transparent anchor pane overlay for sprite notes display
     *
     * @param parentPane
     *         The {@link AnchorPane} in which labels and sprites reside
     */
    public void setNotesPane(AnchorPane parentPane) {
        if (parentPane != null) {
            spritesPane = parentPane;

            storyOverlayVBox = new VBox(5);
            storyOverlayVBox.setPrefWidth(getStoryOverlayPaneWidth());
            storyOverlayVBox.setMaxWidth(storyOverlayVBox.getPrefWidth());
            storyOverlayVBox.setMinWidth(storyOverlayVBox.getPrefWidth());

            setTopAnchor(storyOverlayVBox, 5.0);
            setRightAnchor(storyOverlayVBox, 5.0);

            spritesPane.getChildren().add(storyOverlayVBox);
        }
    }

    // Hides cell name label/context menu
    private void hideContextPopups() {
        contextMenuStage.hide();
    }

    private ChangeListener<Number> getTranslateXListener() {
        return (observable, oldValue, newValue) -> {
            final double value = newValue.doubleValue();
            if (xform.getTranslateX() != value) {
                xform.setTranslateX(value);
            }
        };
    }

    private ChangeListener<Number> getTranslateYListener() {
        return (observable, oldValue, newValue) -> {
            final double value = newValue.doubleValue();
            if (xform.getTranslateY() != value) {
                xform.setTranslateY(value);
            }
        };
    }

    private ChangeListener<Number> getRotateXAngleListener() {
        return (observable, oldValue, newValue) -> {
            double newAngle = newValue.doubleValue();
            this.rotateXAngleProperty.set(newAngle);
            rotateX.setAngle(rotateXAngleProperty.get());
            rotateXIndicator.setAngle(newAngle - initialRotation[0]);
            repositionNotes();
        };
    }

    private ChangeListener<Number> getRotateYAngleListener() {
        return (observable, oldValue, newValue) -> {
            double newAngle = newValue.doubleValue();
            this.rotateYAngleProperty.set(newAngle);
            rotateY.setAngle(rotateYAngleProperty.get());
            rotateYIndicator.setAngle(newAngle - initialRotation[1]);
            repositionNotes();
        };
    }

    private ChangeListener<Number> getRotateZAngleListener() {
        return (observable, oldValue, newValue) -> {
            double newAngle = newValue.doubleValue();
            this.rotateZAngleProperty.set(newAngle);
            rotateZ.setAngle(rotateZAngleProperty.get());;
            rotateZIndicator.setAngle(newAngle - initialRotation[2]);
            repositionNotes();
        };
    }

    private EventHandler<ActionEvent> getZoomInButtonListener() {
        return event -> {
            hideContextPopups();
            double z = zoomProperty.get();
            if (z > 0.25) {
                z -= 0.25;
            } else if (z < 0) {
                // normalize zoom by making 0 its minimum
                // javafx has a bug where for a zoom below 0, the camera flips and does not pass through the scene
                // The API does not recognize that the camera orientation has changed and thus the back of back face
                // culled shapes appear, surrounded w/ artifacts.
                z = 0;
            }
            zoomProperty.set(z);
        };
    }

    private EventHandler<ActionEvent> getZoomOutButtonListener() {
        return event -> {
            hideContextPopups();
            zoomProperty.set(zoomProperty.get() + 0.25);
        };
    }

    private EventHandler<ActionEvent> getBackwardButtonListener() {
        return event -> {
            hideContextPopups();
            if (!playingMovieProperty.get()) {
                timeProperty.set(timeProperty.get() - 1);
            }
        };
    }

    private EventHandler<ActionEvent> getForwardButtonListener() {
        return event -> {
            hideContextPopups();
            if (!playingMovieProperty.get()) {
                timeProperty.set(timeProperty.get() + 1);
            }
        };
    }

    private EventHandler<ActionEvent> getClearAllLabelsButtonListener() {
        return event -> {
            allLabels.clear();
            currentLabels.clear();
            buildScene();
        };
    }

    /**
     * This method returns the {@link ChangeListener} that listens for the {@link BooleanProperty} that changes when
     * 'cell nucleus' is ticked/unticked in the search tab. On change, the scene refreshes and cell bodies are
     * highlighted/unhighlighted accordingly.
     *
     * @return The listener.
     */
    private ChangeListener<Boolean> getCellNucleusTickListener() {
        return (observable, oldValue, newValue) -> {
            cellNucleusTicked = newValue;
            buildScene();
        };
    }

    /**
     * This method returns the {@link ChangeListener} that listens for the {@link BooleanProperty} that changes when
     * 'cell body' is ticked/unticked in the search tab. On change, the scene refreshes and cell bodies are
     * highlighted/unhighlighted accordingly.
     *
     * @return The listener.
     */
    private ChangeListener<Boolean> getCellBodyTickListener() {
        return (observable, oldValue, newValue) -> {
            cellBodyTicked = newValue;
            buildScene();
        };
    }

    private ChangeListener<Boolean> getMulticellModeListener() {
        return (observable, oldValue, newValue) -> {
        };
    }

    /**
     * The getter for the {@link EventHandler} for the {@link MouseEvent} that is fired upon clicking on a note. The
     * handler expands the note on click.
     *
     * @return The event handler.
     */
    private EventHandler<MouseEvent> getNoteClickHandler() {
        return event -> {
            if (event.isStillSincePress()) {
                final Node result = event.getPickResult().getIntersectedNode();
                if (result instanceof Text) {
                    final Text picked = (Text) result;
                    final Note note = currentGraphicsToNotesMap.get(picked);
                    if (note != null) {
                        note.setExpandedInScene(!note.isExpandedInScene());
                        if (note.isExpandedInScene()) {
                            picked.setText(note.getTagName() + ": " + note.getTagContents());
                        } else {
                            picked.setText(note.getTagName() + "\n[more...]");
                        }
                    }
                }
            }
        };
    }

    /**
     * This service spools a thread that
     * <p>
     * 1) retrieves the data for cells, cell bodies, and multicellular
     * structures for the current timeProperty
     * <p>
     * 2) clears the notes, labels, and entities in the subscene
     * <p>
     * 3) adds the current notes, labels, and entities to the subscene
     * <p>
     * 4) adds previous time points if requested.
     */
    private final class RenderService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    runLater(() -> {
                        refreshScene();

                        //render previous time points
                        int loop = (int)numPrevProperty.get();

                        //avoid index out of bound
                        int loop_end = timeProperty.get() - loop;
                        if (loop_end < 0) {
                            loop_end = 0;
                        }

                        for(int i = timeProperty.get() - 1; i > loop_end; --i) {
                            getCellSceneData(i);
                            addEntitiesNoNotesWithColorRule();
                        }

                        //render current time point
                        getSceneData();
                        addEntitiesAndNotes();

                        if (externalSelectedFlag) {
                            handleExternalSelectedCell();
                        }
                    });
                    return null;
                }
            };
        }
    }


    /**
     * This JavaFX {@link Service} of type Void spools a thread to play the subscene movie. It waits the timeProperty
     * in milliseconds defined in the variable WAIT_TIME_MILLI before rendering the next
     * timeProperty frame.
     */
    private final class PlayService extends Service<Void> {
        @Override
        protected final Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    while (true) {
                        if (isCancelled()) {
                            break;
                        }
                        runLater(() -> timeProperty.set(timeProperty.get() + 1));
                        try {
                            sleep(getWaitTimeMilli());
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                    return null;
                }
            };
        }
    }

    /**
     * This class is the {@link ChangeListener} that listens changes in the height or width of the modelAnchorPane in
     * which the subscene lives. When the size changes, front-facing billboards and sprites (notes and labels) are
     * repositioned to align with their appropriate positions (whether it is a location to an entity).
     */
    private final class SubsceneSizeListener implements ChangeListener<Number> {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            repositionNotes();
        }
    }

    /**
     * This class is the Comparator for Shape3Ds that compares based on opacity. This is used for z-buffering for
     * semi-opaque materials. Entities with opaque materials should be rendered last (added first to the
     * rootEntitiesGroup group.
     */
    private final class OpacityComparator implements Comparator<Shape3D> {
        @Override
        public int compare(Shape3D o1, Shape3D o2) {
            double op1 = colorHash.getMaterialOpacity(o1.getMaterial());
            double op2 = colorHash.getMaterialOpacity(o2.getMaterial());
            if (op1 < op2) {
                return 1;
            } else if (op1 > op2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}