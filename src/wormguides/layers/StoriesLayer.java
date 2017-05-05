/*
 * Bao Lab 2017
 */

package wormguides.layers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import acetree.LineageData;
import wormguides.MainApp;
import wormguides.controllers.StoryEditorController;
import wormguides.models.colorrule.Rule;
import wormguides.models.subscenegeometry.SceneElementsList;
import wormguides.stories.Note;
import wormguides.stories.Story;
import wormguides.view.graphicalrepresentations.NoteGraphic;
import wormguides.view.graphicalrepresentations.StoryGraphic;

import static java.lang.Integer.MIN_VALUE;
import static java.util.Objects.requireNonNull;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.geometry.Insets.EMPTY;

import static wormguides.stories.StoriesLoader.loadConfigFile;
import static wormguides.stories.StoryFileUtil.loadFromCSVFile;
import static wormguides.stories.StoryFileUtil.saveToCSVFile;
import static wormguides.util.colorurl.UrlGenerator.generateInternal;
import static wormguides.util.colorurl.UrlGenerator.generateInternalWithoutViewArgs;
import static wormguides.util.colorurl.UrlParser.parseUrlRules;

/**
 * Controller of the list view in the 'Stories' tab
 */
public class StoriesLayer {

    private static final String NEW_STORY_TITLE = "New Story";
    private static final String NEW_STORY_DESCRIPTION = "New story description here";
    private static final String TEMPLATE_STORY_NAME = "Template to Make Your Own Story";
    private static final String TEMPLATE_STORY_DESCRIPTION = "Shows all segmented neurons without further annotation.";

    private final Stage parentStage;

    private final LineageData lineageData;
    private final SceneElementsList sceneElementsList;
    private final SearchLayer searchLayer;

    private final IntegerProperty timeProperty;
    private final DoubleProperty rotateXAngleProperty;
    private final DoubleProperty rotateYAngleProperty;
    private final DoubleProperty rotateZAngleProperty;
    private final DoubleProperty translateXProperty;
    private final DoubleProperty translateYProperty;
    private final DoubleProperty zoomProperty;
    private final DoubleProperty othersOpacityProperty;
    private final BooleanProperty useInternalRulesFlag;
    private final BooleanProperty rebuildSubsceneFlag;
    private final BooleanProperty cellClickedFlag;
    private final StringProperty activeCellNameProperty;
    private final StringProperty activeStoryProperty;

    private final ObservableList<Rule> activeRulesList;
    private final ObservableList<Story> stories;

    private final Button deleteStoryButton;
    private final Button editStoryButton;

    private final int startTime;
    private final int endTime;
    private int movieTimeOffset;

    private Stage editStage;
    private StoryEditorController editController;

    private Story activeStory;
    private Note activeNote;
    private Comparator<Note> noteComparator;
    private double width;

    public StoriesLayer(
            final Stage parentStage,
            final SearchLayer searchLayer,
            final SceneElementsList elementsList,
            final ListView<Story> storiesListView,
            final ObservableList<Rule> rulesList,
            final StringProperty activeCellNameProperty,
            final StringProperty activeStoryProperty,
            final BooleanProperty cellClickedFlag,
            final IntegerProperty timeProperty,
            final DoubleProperty rotateXAngleProperty,
            final DoubleProperty rotateYAngleProperty,
            final DoubleProperty rotateZAngleProperty,
            final DoubleProperty translateXProperty,
            final DoubleProperty translateYProperty,
            final DoubleProperty zoomProperty,
            final DoubleProperty othersOpacityProperty,
            final BooleanProperty useInternalRulesFlag,
            final BooleanProperty rebuildSubsceneFlag,
            final LineageData lineageData,
            final Button newStoryButton,
            final Button deleteStoryButton,
            final Button editStoryButton,
            final int startTime,
            final int endTime,
            final int movieTimeOffset,
            final boolean defaultEmbryoFlag) {

        this.parentStage = requireNonNull(parentStage);
        this.searchLayer = requireNonNull(searchLayer);
        this.lineageData = requireNonNull(lineageData);
        this.sceneElementsList = requireNonNull(elementsList);

        this.activeRulesList = requireNonNull(rulesList);

        this.cellClickedFlag = requireNonNull(cellClickedFlag);
        this.rebuildSubsceneFlag = requireNonNull(rebuildSubsceneFlag);

        this.timeProperty = requireNonNull(timeProperty);
        this.rotateXAngleProperty = requireNonNull(rotateXAngleProperty);
        this.rotateYAngleProperty = requireNonNull(rotateYAngleProperty);
        this.rotateZAngleProperty = requireNonNull(rotateZAngleProperty);
        this.translateXProperty = requireNonNull(translateXProperty);
        this.translateYProperty = requireNonNull(translateYProperty);
        this.zoomProperty = requireNonNull(zoomProperty);
        this.othersOpacityProperty = requireNonNull(othersOpacityProperty);

        this.activeCellNameProperty = requireNonNull(activeCellNameProperty);
        this.activeStoryProperty = requireNonNull(activeStoryProperty);

        this.useInternalRulesFlag = requireNonNull(useInternalRulesFlag);

        this.startTime = startTime;
        this.endTime = endTime;
        this.movieTimeOffset = movieTimeOffset;

        stories = observableArrayList(story -> new Observable[]{
                story.getChangedProperty(),
                story.getActiveProperty()});
        stories.addListener((ListChangeListener<Story>) c -> {
            while (c.next()) {
                // need this listener to detect change for some reason
                // leave this empty
            }
            rebuildSubsceneFlag.set(true);
        });

        newStoryButton.setOnAction(event -> {
            Story story = new Story(NEW_STORY_TITLE, NEW_STORY_DESCRIPTION, "");
            stories.add(story);
            setActiveStory(story);
            setActiveNoteWithSubsceneRebuild(null);
            bringUpEditor();
        });

        this.deleteStoryButton = requireNonNull(deleteStoryButton);
        this.deleteStoryButton.setOnAction(event -> {
            if (activeStory != null) {
                stories.remove(activeStory);
                setActiveStory(null);
            }
        });

        this.editStoryButton = requireNonNull(editStoryButton);
        this.editStoryButton.setOnAction(event -> bringUpEditor());

        width = 0;

        if (defaultEmbryoFlag) {
            loadConfigFile(stories, this.movieTimeOffset);
        }

        addBlankStory();

        noteComparator = (o1, o2) -> {
            final Integer t1 = getEffectiveStartTime(o1);
            final Integer t2 = getEffectiveStartTime(o2);
            if (t1.equals(t2)) {
                return o1.getTagName().compareTo(o2.getTagName());
            }
            return t1.compareTo(t2);
        };

        for (Story story : stories) {
            story.setComparator(noteComparator);
        }

        setActiveStory(stories.get(0));

        requireNonNull(storiesListView);
        storiesListView.setItems(stories);
        storiesListView.setCellFactory(getStoryCellFactory());
        storiesListView.widthProperty().addListener(
                (observable, oldValue, newValue) -> width = newValue.doubleValue() - 20);
        storiesListView.setOnScrollStarted(event -> {
            // ignore horizontal scrolls
            if (event != null && event.getDeltaX() != 0) {
                event.consume();
            }
        });
    }

    /**
     * @return the callback that is the renderer for a {@link Story} item. It graphically renders an active story
     * with black text and an inactive one with grey text. For an active story, its notes are also rendered beneath
     * the story title and description.
     */
    private Callback<ListView<Story>, ListCell<Story>> getStoryCellFactory() {
        return new Callback<ListView<Story>, ListCell<Story>>() {
            @Override
            public ListCell<Story> call(ListView<Story> param) {
                final ListCell<Story> cell = new ListCell<Story>() {
                    @Override
                    protected void updateItem(final Story item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            final StoryGraphic storyGraphic = new StoryGraphic(item);
                            storyGraphic.setStoryClickedHandler(event -> {
                                item.setActive(!item.isActive());
                                if (item.isActive()) {
                                    setActiveStory(item);
                                } else {
                                    setActiveStory(null);
                                }
                            });
                            // add notes inside story graphic if story is active
                            if (item.isActive()) {
                                for (final Note note : item.getNotes()) {
                                    final NoteGraphic noteGraphic = new NoteGraphic(note, rebuildSubsceneFlag);
                                    noteGraphic.setClickedHandler(event -> {
                                        if (!noteGraphic.isExpandIconClicked(
                                                event.getPickResult().getIntersectedNode())) {
                                            note.setActive(!note.isActive());
                                            if (note.isActive()) {
                                                setActiveNoteWithSubsceneRebuild(note);
                                            } else {
                                                setActiveNoteWithSubsceneRebuild(null);
                                            }
                                        }
                                    });
                                    storyGraphic.addNoteGraphic(noteGraphic);
                                }
                            }
                            setGraphic(storyGraphic);
                        } else {
                            setGraphic(null);
                        }
                        setStyle("-fx-background-color: transparent;");
                        setPadding(EMPTY);
                    }
                };
                return cell;
            }
        };
    }

    /**
     * Adds a blank story
     */
    private void addBlankStory() {
        stories.add(new Story(
                TEMPLATE_STORY_NAME,
                TEMPLATE_STORY_DESCRIPTION,
                "http://scene.wormguides.org/wormguides/testurlscript?/set/ash-n$@+#ff8"
                        + "fbc8f/rib-n$@+#ff663366/avg-n$@+#ffb41919/dd-n@+#ff4a24c1/da-"
                        + "n@+#ffc56002/dd-n$+#ffb30a95/da-n$+#ffe6b34d/rivl-n@+#ffffb366/"
                        + "rivr-n@+#ffffe6b3/sibd-n@+#ffe6ccff/siav-n@+#ff8099ff/view/"
                        + "time=393/rX=51.625/rY=-2.125/rZ=0.0/tX=0.0/tY=0.0/scale=2.25/dim=0.25/browser/"));
    }

    /**
     * Loades story from file and sets it as active story. Uses a {@link FileChooser} to allow the user to pick a
     * load location.
     */
    public void loadStory() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Story");
        chooser.setInitialFileName("WormGUIDES Story.csv");
        chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
        final File file = chooser.showOpenDialog(parentStage);
        if (file != null) {
            final Story newStory = loadFromCSVFile(stories, file, movieTimeOffset);
            newStory.setComparator(noteComparator);
            setActiveStory(newStory);
        }
    }

    /**
     * Saves active story to a file. {@link FileChooser} is used to allow the user to specify a save location and
     * file name.
     *
     * @return true when the file has been saved, false otherwise.
     */
    public boolean saveActiveStory() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle("Save WormGUIDES Story");
        chooser.setInitialFileName("WormGUIDES Story.csv");
        chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
        final File file = chooser.showSaveDialog(parentStage);
        // if user clicks save
        if (file != null) {
            if (activeStory != null) {
                if (activeNote != null && activeNote.hasColorScheme()) {
                    // if the story color scheme is not being used, save rules into the note's URL
                    activeNote.setColorUrl(generateInternalWithoutViewArgs(activeRulesList));
                    // copy the note's rules and temporarily use the story's color scheme
                    // then put back the copied rules into the active rules list
                    final List<Rule> rulesCopy = new ArrayList<>(activeRulesList);
                    parseUrlRules(activeStory.getColorUrl(), activeRulesList, searchLayer);
                    activeStory.setColorUrl(generateInternal(
                            activeRulesList,
                            timeProperty.get(),
                            rotateXAngleProperty.get(),
                            rotateYAngleProperty.get(),
                            rotateZAngleProperty.get(),
                            translateXProperty.get(),
                            translateYProperty.get(),
                            zoomProperty.get(),
                            othersOpacityProperty.get()));
                    activeRulesList.clear();
                    activeRulesList.addAll(rulesCopy);
                } else {
                    activeStory.setColorUrl(generateInternal(
                            activeRulesList,
                            timeProperty.get(),
                            rotateXAngleProperty.get(),
                            rotateYAngleProperty.get(),
                            rotateZAngleProperty.get(),
                            translateXProperty.get(),
                            translateYProperty.get(),
                            zoomProperty.get(),
                            othersOpacityProperty.get()));
                }

                saveToCSVFile(activeStory, file, movieTimeOffset);
                System.out.println("File saved to " + file.getAbsolutePath());
                return true;
            } else {
                System.out.println("No active story to save");
            }
        }
        return false;
    }

    /**
     * @return The {@link StringProperty} activeStoryProperty that changes when the active story changes. The value
     * of the String is the name of the currently active story.
     */
    public StringProperty getActiveStoryProperty() {
        return activeStoryProperty;
    }

    /**
     * @return The description of the current active story
     */
    public String getActiveStoryDescription() {
        if (activeStory != null) {
            return activeStory.getDescription();
        }
        return "";
    }

    /**
     * @return Effective start time of currently active story
     */
    public int getActiveStoryStartTime() {
        if (activeStory != null && activeStory.hasNotes()) {
            return getEffectiveStartTime(activeStory.getNotes().get(0));
        }
        return MIN_VALUE;
    }

    /**
     * Sets the active note to the input note parameter. Makes the current note inactive, then makes the input note
     * active. If the previously active note had a color scheme URL, the rules are saved back into the note as a new
     * URL.
     *
     * @param note
     *         the note that should become active
     */
    private void setActiveNoteWithSubsceneRebuild(final Note note) {
        boolean wasUsingNoteUrl = activeNote != null && activeNote.hasColorScheme();
        boolean willUseNoteUrl = note != null && note.hasColorScheme();
        if (wasUsingNoteUrl) {
            activeNote.setColorUrl(generateInternalWithoutViewArgs(activeRulesList));
        } else {
            if (willUseNoteUrl) {
                // save the scheme to the story if the old note has no color scheme and the new one does
                activeStory.setColorUrl(generateInternalWithoutViewArgs(activeRulesList));
            } else {
                // no need to do anything if we stay in the story context
            }
        }

        // deactivate the previously active note
        if (activeNote != null) {
            activeNote.setActive(false);
        }

        // make new note active
        activeNote = note;
        if (activeNote != null) {
            activeNote.setActive(true);

            if (activeNote.hasColorScheme()) {
                parseUrlRules(
                        activeNote.getColorUrl(),
                        activeRulesList,
                        searchLayer);
            } else if (wasUsingNoteUrl && !willUseNoteUrl) {
                parseUrlRules(
                        activeStory.getColorUrl(),
                        activeRulesList,
                        searchLayer);
            }

            final int noteStartTime = getEffectiveStartTime(activeNote);
            // only need one of these changes to trigger a subscene rebuild
            if (noteStartTime == timeProperty.get()) {
                rebuildSubsceneFlag.set(true);
            } else {
                timeProperty.set(getEffectiveStartTime(activeNote));
            }
        }

        // update story/note editor
        if (editController != null) {
            editController.setActiveNote(activeNote);
        }
    }

    /**
     * Retrieve the effective end time of the input note parameter, whether it is the one explicitly stated by the
     * 'end time' field or the one implicitly specified by the cell, cell body, or multicellular structure.
     *
     * @param note
     *         the note queried
     *
     * @return the effective end time of the input note. An Integer object is returned instead of the primitive int
     * so that it can be passed into the note comparator
     */
    private Integer getEffectiveEndTime(Note note) {
        int time = MIN_VALUE;

        if (note != null) {
            if (note.attachedToCell() || note.attachedToStructure()) {

                int entityStartTime;
                int entityEndTime;

                if (note.attachedToCell()) {
                    entityStartTime = lineageData.getFirstOccurrenceOf(note.getCellName());
                    entityEndTime = lineageData.getLastOccurrenceOf(note.getCellName());
                } else {
                    entityStartTime = sceneElementsList.getFirstOccurrenceOf(note.getCellName());
                    entityEndTime = sceneElementsList.getLastOccurrenceOf(note.getCellName());
                }

                // attached to cell/structure and time is specified
                if (note.isTimeSpecified()) {
                    int noteStartTime = note.getStartTime();
                    int noteEndTime = note.getEndTime();

                    // make sure times actually overlap
                    if (noteStartTime <= entityEndTime && entityEndTime <= noteEndTime) {
                        time = entityEndTime;
                    } else if (entityStartTime <= noteEndTime && noteEndTime < entityEndTime) {
                        time = noteEndTime;
                    }
                }

                // attached to cell/structure and time not specified
                else {
                    time = entityEndTime;
                }
            } else if (note.isTimeSpecified()) {
                time = note.getEndTime();
            }

        }

        return time;
    }

    /**
     * Retrieve the effective start time of the input note parameter, whether it is the one explicitly stated by the
     * 'start time' field or the one implicitly specified by the cell, cell body, or multicellular structure.
     *
     * @param note
     *         The {@link Note} whose effective start time is queried
     *
     * @return the effective start time of the input note. An Integer object is returned instead of the primitive int
     * so that it can be passed into the note comparator
     */
    private Integer getEffectiveStartTime(final Note note) {
        int time = MIN_VALUE;
        if (note != null) {
            if (note.attachedToCell() || note.attachedToStructure()) {
                int entityStartTime;
                int entityEndTime;

                final String cellName = note.getCellName();
                if (note.attachedToCell()) {
                    entityStartTime = lineageData.getFirstOccurrenceOf(cellName);
                    entityEndTime = lineageData.getLastOccurrenceOf(cellName);
                } else {
                    entityStartTime = sceneElementsList.getFirstOccurrenceOf(cellName);
                    entityEndTime = sceneElementsList.getLastOccurrenceOf(cellName);
                }

                // attached to cell/structure and time is specified
                if (note.isTimeSpecified()) {
                    int noteStartTime = note.getStartTime();
                    int noteEndTime = note.getEndTime();

                    // make sure times actually overlap
                    if (noteStartTime <= entityStartTime && entityStartTime <= noteEndTime) {
                        time = entityStartTime;
                    } else if (entityStartTime <= noteStartTime && noteStartTime < entityEndTime) {
                        time = noteStartTime;
                    }
                } else {
                    // if attached to cell/structure and time is not specified
                    time = entityStartTime;
                }

            } else if (note.isTimeSpecified()) {
                time = note.getStartTime();
            }
        }

        return time;
    }

    /**
     * @return the currently active story
     */
    public Story getActiveStory() {
        return activeStory;
    }

    /**
     * Ultimately sets the active story to the input story. Sets the currently active story to be inactive if it is
     * not null, then sets the input story to active.
     *
     * @param story
     *         story to make active
     */
    public void setActiveStory(final Story story) {
        // disable previous active story, copy current rules changes back to story/note
        if (activeStory != null) {
            if (activeNote != null) {
                if (!activeNote.hasColorScheme()) {
                    activeStory.setColorUrl(generateInternalWithoutViewArgs(activeRulesList));
                } else {
                    activeNote.setColorUrl(generateInternalWithoutViewArgs(activeRulesList));
                }
            }
            activeStory.setActive(false);
        }

        activeNote = null;
        useInternalRulesFlag.set(true);

        activeStory = story;
        if (activeStory != null) {
            // enable delete/edit story buttons
            deleteStoryButton.setDisable(false);
            editStoryButton.setDisable(false);

            // sort notes choronologically
            activeStory.sortNotes();

            activeStory.setActive(true);
            activeStoryProperty.set(activeStory.getTitle());
            // if story does not come with a url, set its url to contain the internal color rules
            if (!activeStory.hasColorScheme()) {
                activeStory.setColorUrl(generateInternal(
                        activeRulesList,
                        timeProperty.get(),
                        rotateXAngleProperty.get(),
                        rotateYAngleProperty.get(),
                        rotateZAngleProperty.get(),
                        translateXProperty.get(),
                        translateYProperty.get(),
                        zoomProperty.get(),
                        othersOpacityProperty.get()));
            }
            useInternalRulesFlag.set(false);
            parseUrlRules(
                    activeStory.getColorUrl(),
                    activeRulesList,
                    searchLayer);
            if (activeStory.hasNotes()) {
                timeProperty.set(getEffectiveStartTime(activeStory.getNotes().get(0)));
            }
        } else {
            // if there is no newly active story
            // disable delete/edit story buttons
            deleteStoryButton.setDisable(true);
            editStoryButton.setDisable(true);

            activeStoryProperty.set("");
            useInternalRulesFlag.set(true);
            rebuildSubsceneFlag.set(true);
        }

        if (editController != null) {
            // there is no active note on a story context switch
            editController.setActiveNote(null);
            editController.setActiveStory(activeStory);
        }
    }

    /**
     * @param tagName
     *         tag name of that note whose comments the user wants to retrieve
     *
     * @return the comments of the note whose tag name is specified by the input parameter
     */
    public String getNoteComments(final String tagName) {
        String comments = "";
        for (Story story : stories) {
            if (!story.getNoteComment(tagName).isEmpty()) {
                comments = story.getNoteComment(tagName);
                break;
            }
        }
        return comments;
    }

    public List<Note> getNotesWithEntity() {
        ArrayList<Note> notes = new ArrayList<>();
        stories.stream()
                .filter(Story::isActive)
                .forEachOrdered(story -> notes.addAll(story.getNotesWithEntity()));
        return notes;
    }

    /**
     * @param time
     *         the queried time
     *
     * @return all notes that can exist at the at input time. This includes notes attached to an entity if entity is
     * present at input time. These notes are later filtered out.
     */
    public List<Note> getNotesAtTime(final int time) {
        final List<Note> notes = new ArrayList<>();
        stories.stream()
                .filter(Story::isActive)
                .forEachOrdered(story -> notes.addAll(story.getPossibleNotesAtTime(time)));

        if (!notes.isEmpty()) {
            final Iterator<Note> iter = notes.iterator();
            Note note;
            while (iter.hasNext()) {
                note = iter.next();
                int effectiveStart = getEffectiveStartTime(note);
                int effectiveEnd = getEffectiveEndTime(note);
                if (effectiveStart != MIN_VALUE
                        && effectiveEnd != MIN_VALUE
                        && (time < effectiveStart || effectiveEnd < time)) {
                    iter.remove();
                }
            }
        }
        return notes;
    }

    /**
     * @return list of stories that are visible in the 'Stories' tab
     */
    public ObservableList<Story> getStories() {
        return stories;
    }

    /**
     * Brings up the story/notes editor window, controlled by the {@link StoryEditorController}. Upon the editor's
     * initialization/fxml load, listenable properties are passed to the editor so that the it can rename
     * labels/change the UI according to changes in time and active cell name.
     * <p>
     * The editor is initialized so that it always lives on top of the main application window and moves when the
     * main window is moved. This is to ensure that the user can always edit a story when the window is opened even
     * when he/she is clicking around in the 3D subscene to change the time/active cell.
     */
    private void bringUpEditor() {
        if (editStage == null) {
            editController = new StoryEditorController(
                    movieTimeOffset,
                    lineageData,
                    sceneElementsList.getAllMulticellSceneNames(),
                    activeCellNameProperty,
                    cellClickedFlag,
                    timeProperty);

            editController.setActiveStory(activeStory);
            editController.setActiveNote(activeNote);

            editStage = new Stage();

            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/layouts/StoryEditorLayout.fxml"));

            loader.setController(editController);
            loader.setRoot(editController);

            try {
                editStage.setScene(new Scene(loader.load()));

                editStage.setTitle("Story/Note Editor");
                editStage.setResizable(true);

                editStage.setOnCloseRequest(event -> rebuildSubsceneFlag.set(true));

                editController.getNoteCreatedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        final Note newNote = editController.getActiveNote();
                        editController.setNoteCreated(false);
                        activeStory.addNote(newNote);
                        setActiveNoteWithSubsceneRebuild(newNote);
                        rebuildSubsceneFlag.set(true);
                    }
                });

                editController.addDeleteButtonListener(event -> {
                    if (activeNote != null) {
                        activeStory.removeNote(activeNote);
                    }
                    setActiveNoteWithSubsceneRebuild(null);
                });

                for (Node node : editStage.getScene().getRoot().getChildrenUnmodifiable()) {
                    node.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
                }

            } catch (IOException e) {
                System.out.println("error in initializing note editor.");
                e.printStackTrace();
            }
        }
        editStage.show();
        editStage.toFront();
    }

    /**
     * Changes the color of the input {@link Text} items by modifying the java-fx css attribute '-fx-fill' to the
     * specified input color. Used by story and note graphic items.
     *
     * @param color
     *         color to change the texts to
     * @param texts
     *         texts whose color should change
     */
    public static void colorTexts(final Color color, final Text... texts) {
        if (color != null && texts != null) {
            for (Text text : texts) {
                if (text != null) {
                    text.setStyle("-fx-fill:" + color.toString().toLowerCase().replace("0x", "#"));
                }
            }
        }
    }

    /**
     * @return stories visible in the 'Stories' tab
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Stories:\n");
        Story story;
        for (int i = 0; i < stories.size(); i++) {
            story = stories.get(i);
            sb.append(story.getTitle())
                    .append(": ")
                    .append(story.getNumberOfNotes())
                    .append(" notes\n");
            for (Note note : story.getNotes()) {
                sb.append("\t")
                        .append(note.getTagName())
                        .append(": times ")
                        .append(note.getStartTime())
                        .append(" ")
                        .append(note.getEndTime())
                        .append("\n");
            }
            if (i < stories.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}