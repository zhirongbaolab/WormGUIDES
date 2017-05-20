/*
 * Bao Lab 2017
 */

package wormguides.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

import acetree.LineageData;
import wormguides.stories.Note;
import wormguides.stories.Note.Attachment;
import wormguides.stories.Note.Display;
import wormguides.stories.Story;
import wormguides.util.StringCellFactory;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import static wormguides.controllers.StoryEditorController.Time.CURRENT;
import static wormguides.controllers.StoryEditorController.Time.GLOBAL;
import static wormguides.controllers.StoryEditorController.Time.RANGE;
import static wormguides.stories.Note.Attachment.BLANK;
import static wormguides.stories.Note.Attachment.CELL;
import static wormguides.stories.Note.Attachment.STRUCTURE;
import static wormguides.stories.Note.Display.BILLBOARD_FRONT;
import static wormguides.stories.Note.Display.CALLOUT_LOWER_LEFT;
import static wormguides.stories.Note.Display.CALLOUT_LOWER_RIGHT;
import static wormguides.stories.Note.Display.CALLOUT_UPPER_LEFT;
import static wormguides.stories.Note.Display.CALLOUT_UPPER_RIGHT;
import static wormguides.stories.Note.Display.OVERLAY;
import static wormguides.stories.Note.Display.SPRITE;

public class StoryEditorController extends AnchorPane implements Initializable {

    private static final String NEW_NOTE_TITLE = "New Note";
    private static final String NEW_NOTE_CONTENTS = "New note contents here";

    @FXML
    private TextField author;
    @FXML
    private TextField date;
    @FXML
    private Label activeCellLabel;
    private StringProperty activeCellProperty;
    private StringProperty sceneActiveCellProperty;
    private IntegerProperty timeProperty;
    @FXML
    private Button delete;
    // time stuff
    @FXML
    private Label timeRangeLabel;
    @FXML
    private Label timeRangeStartLabel;
    @FXML
    private Label timeRangeEndLabel;
    @FXML
    private ToggleGroup timeToggle;
    @FXML
    private RadioButton globalTimeRadioBtn;
    @FXML
    private RadioButton currentTimeRadioBtn;
    @FXML
    private RadioButton rangeTimeRadioBtn;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private Label currentTimeLabel;
    // attachment type stuff
    @FXML
    private ToggleGroup attachmentToggle;
    @FXML
    private RadioButton cellRadioBtn;
    @FXML
    private RadioButton globalRadioBtn;
    @FXML
    private RadioButton structureRadioBtn;
    @FXML
    private ToggleGroup subStructureToggle;
    @FXML
    private Label substructureLabel;
    @FXML
    private ComboBox<String> structuresComboBox;

    @FXML
    private RadioButton axonRadioBtn;

    @FXML
    private RadioButton dendriteRadioBtn;
    @FXML
    private RadioButton cellBodyRadioBtn;
    // display type stuff
    @FXML
    private ToggleGroup displayToggle;
    @FXML
    private RadioButton infoPaneRadioBtn;
    @FXML
    private RadioButton locationRadioBtn;
    @FXML
    private RadioButton billboardRadioBtn;
    @FXML
    private RadioButton calloutUpperLeftRadioBtn;
    @FXML
    private RadioButton calloutUpperRightRadioBtn;
    @FXML
    private RadioButton calloutLowerLeftRadioBtn;
    @FXML
    private RadioButton calloutLowerRightRadioBtn;
    private BooleanProperty noteCreated;

    // callout stuff
    @FXML
    private Label calloutHOffsetLabel;
    @FXML
    private Label calloutVOffsetLabel;
    @FXML
    private Slider calloutHOffsetSlider;
    @FXML
    private Slider calloutVOffsetSlider;

    @FXML
    private TextField storyTitle;
    @FXML
    private TextArea storyDescription;
    private Runnable storyFieldsUpdateRunnable;
    private Runnable noteFieldsUpdateRunnable;
    private Story activeStory;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;

    @FXML
    private Button createNoteColorSchemeButton;

    private final ObservableList<String> structureComboItems;
    private StringCellFactory.StringListCellFactory listCellFactory;

    private Note activeNote;

    private final LineageData lineageData;
    private final int frameOffset;

    /**
     * Constructor
     *
     * @param timeOffset
     *         time offset (in number of frames). This was loaded from production info on startup.
     * @param data
     *         underlying lineage data
     * @param multiCellStructuresList
     *         list of multicellular structures
     * @param nameProperty
     *         string property that changes when an entity is clicked in the subscene
     * @param cellClickedProperty
     *         true if a cell is clicked on, false otherwise
     * @param sceneTimeProperty
     *         the subscene time
     */
    public StoryEditorController(
            final int timeOffset,
            final LineageData data,
            final List<String> multiCellStructuresList,
            final StringProperty nameProperty,
            final BooleanProperty cellClickedProperty,
            final IntegerProperty sceneTimeProperty) {

        super();

        lineageData = data;

        frameOffset = timeOffset;

        noteCreated = new SimpleBooleanProperty(false);

        activeStory = null;
        activeNote = null;

        timeProperty = requireNonNull(sceneTimeProperty);
        timeProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && timeToggle != null) {
                final Toggle selected = timeToggle.getSelectedToggle();
                if (selected == null || selected.getUserData() != CURRENT) {
                    setCurrentTimeLabel(timeProperty.get() + frameOffset);
                }
            }
        });

        activeCellProperty = new SimpleStringProperty();

        sceneActiveCellProperty = requireNonNull(nameProperty);
        sceneActiveCellProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && lineageData.isCellName(newValue)) {
                activeCellProperty.set(newValue);
            }
        });

        cellClickedProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                activeCellProperty.set(sceneActiveCellProperty.get());
                cellClickedProperty.set(false);
            }
        });

        structureComboItems = observableArrayList();
        structureComboItems.addAll(requireNonNull(multiCellStructuresList));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // for story title field unselection/caret position
        storyFieldsUpdateRunnable = () -> {
            storyTitle.setText(activeStory.getTitle());
            storyDescription.setText(activeStory.getDescription());
            author.setText(activeStory.getAuthor());
            date.setText(activeStory.getDate());
            storyTitle.positionCaret(storyTitle.getText().length());
        };

        noteFieldsUpdateRunnable = () -> {
            if (activeNote != null) {
                titleField.setText(activeNote.getTagName());
                contentArea.setText(activeNote.getTagContents());
            } else {
                titleField.clear();
                contentArea.clear();
            }
            updateType();
            updateTime();
            updateDisplay();
        };

        // note fields
        runLater(noteFieldsUpdateRunnable);
        titleField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            // if field was previously focused and is not anymore
            if (!newValue && activeNote != null) {
                activeNote.setTagName(titleField.getText());
            }
        });
        contentArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && activeNote != null) {
                activeNote.setTagContents(contentArea.getText());
            }
        });

        // story fields
        updateStoryFields();
        storyTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && activeStory != null) {
                activeStory.setTitle(newValue);
            }
        });
        storyDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && activeStory != null) {
                activeStory.setDescription(newValue);
            }
        });
        author.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && activeStory != null) {
                activeStory.setAuthor(newValue);
            }
        });
        date.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && activeStory != null) {
                activeStory.setDate(newValue);
            }
        });

        // attachment type/note display
        initToggleData();

        updateType();
        updateTime();
        updateDisplay();

        timeToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (activeNote != null && newValue != null) {
                int start = MIN_VALUE;
                int end = MIN_VALUE;
                switch ((Time) newValue.getUserData()) {
                    case CURRENT:
                        setDisableTimeRangeOptions(true);
                        start = timeProperty.get();
                        end = start;
                        break;

                    case RANGE:
                        setDisableTimeRangeOptions(false);
                        try {
                            if (!startTimeField.getText().isEmpty()) {
                                start = parseInt(startTimeField.getText()) - frameOffset;
                            }
                            if (!endTimeField.getText().isEmpty()) {
                                end = parseInt(endTimeField.getText()) - frameOffset;
                            }
                        } catch (NumberFormatException e) {
                            // silently fail
                        }
                        break;

                    case GLOBAL:
                        // fall to default case

                    default:
                        setDisableTimeRangeOptions(true);
                        break;
                }
                activeNote.setStartAndEndTimes(start, end);
                activeNote.setChanged(true);
            }
        });

        startTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (activeNote != null) {
                Toggle selected = timeToggle.getSelectedToggle();
                if (selected != null && selected.getUserData() == RANGE) {
                    try {
                        activeNote.setStartTime(parseInt(newValue) - frameOffset);
                        activeNote.setChanged(true);
                    } catch (NumberFormatException e) {
                        // silently fail
                    }
                }
            }
        });

        endTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (activeNote != null) {
                Toggle selected = timeToggle.getSelectedToggle();
                if (selected != null && selected.getUserData() == RANGE) {
                    try {
                        activeNote.setEndTime(parseInt(newValue) - frameOffset);
                        activeNote.setChanged(true);
                    } catch (NumberFormatException e) {
                        // silently fail
                    }
                }
            }
        });

        attachmentToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (activeNote != null) {
                if (newValue != null) {
                    switch ((Attachment) newValue.getUserData()) {
                        case CELL:
                            setActiveNoteAttachmentType(CELL);
                            setActiveNoteCellName(activeCellProperty.get());
                            setDisableStructureOptions(true);
                            updateDisplay();
                            break;

                        case BLANK:
                            setActiveNoteAttachmentType(BLANK);
                            setActiveNoteDisplay(OVERLAY);
                            setCellLabelName(sceneActiveCellProperty.get());
                            setDisableStructureOptions(true);
                            updateDisplay();
                            break;

                        case STRUCTURE:
                            setActiveNoteAttachmentType(STRUCTURE);
                            setActiveNoteCellName(structuresComboBox.getSelectionModel().getSelectedItem());
                            setCellLabelName(sceneActiveCellProperty.get());
                            setDisableStructureOptions(false);
                            updateDisplay();
                            break;

                        default:
                            setCellLabelName(sceneActiveCellProperty.get());
                            break;

                    }
                } else {
                    setActiveNoteAttachmentType(BLANK);
                }
                activeNote.setChanged(true);
            }
        });

        displayToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (activeNote != null) {
                if (newValue != null) {
                    switch ((Display) newValue.getUserData()) {
                        case OVERLAY:
                            setActiveNoteDisplay(OVERLAY);
                            calloutHOffsetLabel.setDisable(true);
                            calloutHOffsetSlider.setDisable(true);
                            calloutVOffsetLabel.setDisable(true);
                            calloutVOffsetSlider.setDisable(true);
                            break;
                        case SPRITE:
                            setActiveNoteDisplay(SPRITE);
                            calloutHOffsetLabel.setDisable(true);
                            calloutHOffsetSlider.setDisable(true);
                            calloutVOffsetLabel.setDisable(true);
                            calloutVOffsetSlider.setDisable(true);
                            break;
                        case BILLBOARD_FRONT:
                            setActiveNoteDisplay(BILLBOARD_FRONT);
                            calloutHOffsetLabel.setDisable(true);
                            calloutHOffsetSlider.setDisable(true);
                            calloutVOffsetLabel.setDisable(true);
                            calloutVOffsetSlider.setDisable(true);
                            break;
                        case CALLOUT_UPPER_LEFT:
                            setActiveNoteDisplay(CALLOUT_UPPER_LEFT);
                            calloutHOffsetLabel.setDisable(false);
                            calloutHOffsetSlider.setDisable(false);
                            calloutVOffsetLabel.setDisable(false);
                            calloutVOffsetSlider.setDisable(false);
                            break;
                        case CALLOUT_LOWER_LEFT:
                            setActiveNoteDisplay(CALLOUT_LOWER_LEFT);
                            calloutHOffsetLabel.setDisable(false);
                            calloutHOffsetSlider.setDisable(false);
                            calloutVOffsetLabel.setDisable(false);
                            calloutVOffsetSlider.setDisable(false);
                            break;
                        case CALLOUT_UPPER_RIGHT:
                            setActiveNoteDisplay(CALLOUT_UPPER_RIGHT);
                            calloutHOffsetLabel.setDisable(false);
                            calloutHOffsetSlider.setDisable(false);
                            calloutVOffsetLabel.setDisable(false);
                            calloutVOffsetSlider.setDisable(false);
                            break;
                        case CALLOUT_LOWER_RIGHT:
                            setActiveNoteDisplay(CALLOUT_LOWER_RIGHT);
                            calloutHOffsetLabel.setDisable(false);
                            calloutHOffsetSlider.setDisable(false);
                            calloutVOffsetLabel.setDisable(false);
                            calloutVOffsetSlider.setDisable(false);
                            break;
                        default:
                            break;
                    }
                } else {
                    setActiveNoteDisplay(Display.BLANK);
                }
                activeNote.setChanged(true);
            }
        });

        calloutHOffsetSlider.valueChangingProperty().addListener((observable, wasChanging, isStillChanging) -> {
            if (!isStillChanging && activeNote != null && activeNote.isCallout()) {
                activeNote.setCalloutHorizontalOffset((int) calloutHOffsetSlider.getValue());
                activeNote.setChanged(true);
            }
        });

        calloutVOffsetSlider.valueChangingProperty().addListener((observable, wasChanging, isStillChanging) -> {
            if (!isStillChanging && activeNote != null && activeNote.isCallout()) {
                activeNote.setCalloutVerticalOffset((int) calloutVOffsetSlider.getValue());
                activeNote.setChanged(true);
            }
        });

        listCellFactory = new StringCellFactory.StringListCellFactory();
        structuresComboBox.setItems(structureComboItems);
        structuresComboBox.setButtonCell(listCellFactory.getNewStringListCell());
        structuresComboBox.setCellFactory(listCellFactory);
        structuresComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final Toggle selected = attachmentToggle.getSelectedToggle();
            if (selected != null && activeNote != null && selected.equals(structureRadioBtn)) {
                activeNote.setCellName(newValue);
            }
        });

        activeCellProperty.addListener((observable, oldValue, newValue) -> {
            // only change when active cell toggle is not selected and the currently active entity is a named cell
            final Toggle selected = attachmentToggle.getSelectedToggle();
            if (selected == null || !((Attachment) selected.getUserData()).equals(CELL)) {
                String activeCellName = "";
                if (lineageData.isCellName(newValue)) {
                    activeCellName = newValue;
                }
                setCellLabelName(activeCellName);
            }
        });
        if (activeNote != null && activeNote.attachedToCell()) {
            activeCellProperty.set(activeNote.getCellName());
        } else {
            activeCellProperty.set(sceneActiveCellProperty.get());
        }
    }

    /**
     * Disables/enables options for structures. Options include the the structions combo box and the substructure toggle
     *
     * @param disable
     *         true when structure options should be disabled, false otherwise
     */
    private void setDisableStructureOptions(final boolean disable) {
        structuresComboBox.setDisable(disable);
        substructureLabel.setDisable(disable);
    }

    /**
     * Disables/enables the labels and sliders for adjusting the horizontal and vertical offsets for callouts
     *
     * @param disable
     *         true when the callout offset adjustment components should be disabled, false otherwise
     */
    private void setDisableCalloutOffsetOptions(final boolean disable) {
        calloutHOffsetLabel.setDisable(disable);
        calloutHOffsetSlider.setDisable(disable);
        calloutVOffsetLabel.setDisable(disable);
        calloutVOffsetSlider.setDisable(disable);
    }

    /**
     * Disables/enables the fields for inputing a time range and its labels.
     *
     * @param disable
     *         true when the time range labels and inputs should be disabled, false otherwise
     */
    private void setDisableTimeRangeOptions(final boolean disable) {
        timeRangeLabel.setDisable(disable);
        timeRangeStartLabel.setDisable(disable);
        timeRangeEndLabel.setDisable(disable);
        startTimeField.setDisable(disable);
        endTimeField.setDisable(disable);
    }

    private void setActiveNoteCellName(final String name) {
        if (activeNote != null) {
            activeNote.setCellName(name);
        }
    }

    private void setActiveNoteDisplay(final Display display) {
        if (activeNote != null) {
            activeNote.setDisplay(display);
        }
    }

    private void setActiveNoteAttachmentType(final Attachment attachment) {
        if (activeNote != null) {
            activeNote.setAttachmentType(attachment);
            if (attachment.equals(CELL) && !activeCellProperty.get().isEmpty()) {
                activeNote.setCellName(activeCellProperty.get());
            }
        }
    }

    private String removeFunctionalName(String name) {
        if (name.contains("(")) {
            name = name.substring(0, name.indexOf("("));
        }
        name.trim();

        return name;
    }

    private void initToggleData() {
        // attachment type
        cellRadioBtn.setUserData(CELL);
        globalRadioBtn.setUserData(BLANK);
        structureRadioBtn.setUserData(STRUCTURE);

        // sub structure

        // time
        globalTimeRadioBtn.setUserData(GLOBAL);
        currentTimeRadioBtn.setUserData(CURRENT);
        rangeTimeRadioBtn.setUserData(RANGE);

        // display
        infoPaneRadioBtn.setUserData(OVERLAY);
        locationRadioBtn.setUserData(SPRITE);
        calloutUpperLeftRadioBtn.setUserData(CALLOUT_UPPER_LEFT);
        calloutUpperRightRadioBtn.setUserData(CALLOUT_UPPER_RIGHT);
        calloutLowerLeftRadioBtn.setUserData(CALLOUT_LOWER_LEFT);
        calloutLowerRightRadioBtn.setUserData(CALLOUT_LOWER_RIGHT);
        billboardRadioBtn.setUserData(BILLBOARD_FRONT);
    }

    public void setNoteCreated(boolean created) {
        noteCreated.set(created);
    }

    public BooleanProperty getNoteCreatedProperty() {
        return noteCreated;
    }

    private void updateTime() {
        if (timeToggle != null) {
            setCurrentTimeLabel(timeProperty.get() + frameOffset);

            if (activeNote != null) {
                int start = activeNote.getStartTime();
                int end = activeNote.getEndTime();

                if (start == MIN_VALUE || end == MIN_VALUE) {
                    timeToggle.selectToggle(globalTimeRadioBtn);
                    startTimeField.setText("");
                    endTimeField.setText("");
                } else if (start == end) {
                    timeToggle.selectToggle(currentTimeRadioBtn);
                    startTimeField.setText("");
                    endTimeField.setText("");
                } else if (start < end) {
                    timeToggle.selectToggle(rangeTimeRadioBtn);
                    startTimeField.setText(Integer.toString(start + frameOffset));
                    endTimeField.setText(Integer.toString(end + frameOffset));
                }
            } else {
                resetToggle(timeToggle);

                startTimeField.setText("");
                endTimeField.setText("");
            }
        }
    }

    /**
     * Updates the attachment type toggle (resets toggles if there is no active note)
     */
    private void updateType() {
        if (attachmentToggle != null) {
            if (activeNote != null) {
                switch (activeNote.getAttachmentType()) {
                    case CELL:
                        final String cellName = activeNote.getCellName();
                        activeCellProperty.set(cellName);
                        setCellLabelName(cellName);
                        attachmentToggle.selectToggle(cellRadioBtn);
                        resetToggle(subStructureToggle);
                        break;

                    case STRUCTURE:
                        final String name = activeNote.getCellName();
                        for (String structure : structureComboItems) {
                            if (structure.equalsIgnoreCase(name)) {
                                structuresComboBox.getSelectionModel().select(structure);
                                break;
                            }
                        }
                        attachmentToggle.selectToggle(structureRadioBtn);
                        // TODO read substructure toggle enum from note (to be added)
                        break;

                    case BLANK: // fall to default case

                    default:
                        globalRadioBtn.setSelected(true);
                        activeCellProperty.set(sceneActiveCellProperty.get());
                        resetToggle(subStructureToggle);
                        break;
                }
            } else {
                setCellLabelName(sceneActiveCellProperty.get());
                resetToggle(attachmentToggle);
                resetToggle(subStructureToggle);
                structuresComboBox.getSelectionModel().clearSelection();
            }
        }
    }

    private void setCurrentTimeLabel(final int time) {
        currentTimeLabel.setText("Current Time (" + time + ")");
    }

    private void setCellLabelName(final String name) {
        if (activeCellLabel != null) {
            if (name == null || name.isEmpty()) {
                activeCellLabel.setText("Active Cell (none)");
            } else {
                activeCellLabel.setText("Active Cell (" + removeFunctionalName(name) + ")");
            }
        }
    }

    /**
     * Resets a toggle group so that all toggle are unselected
     *
     * @param group
     *         the toggle group to reset
     */
    private void resetToggle(final ToggleGroup group) {
        if (group != null) {
            final Toggle current = group.getSelectedToggle();
            if (current != null) {
                current.setSelected(false);
            }
        }
    }

    /**
     * Updates display radio button toggle with the display type of the active note
     */
    private void updateDisplay() {
        if (displayToggle != null && activeNote != null) {
            switch (activeNote.getTagDisplay()) {
                case BLANK: // fall to overlay case
                case OVERLAY:
                    infoPaneRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(true);
                    break;
                case SPRITE:
                    locationRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(true);
                    break;
                case BILLBOARD_FRONT:
                    billboardRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(true);
                    break;
                case CALLOUT_UPPER_LEFT:
                    calloutUpperLeftRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(false);
                    calloutHOffsetSlider.setValue(activeNote.getCalloutHorizontalOffset());
                    calloutVOffsetSlider.setValue(activeNote.getCalloutVerticalOffset());
                    break;
                case CALLOUT_LOWER_LEFT:
                    calloutLowerLeftRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(false);
                    calloutHOffsetSlider.setValue(activeNote.getCalloutHorizontalOffset());
                    calloutVOffsetSlider.setValue(activeNote.getCalloutVerticalOffset());
                    break;
                case CALLOUT_UPPER_RIGHT:
                    calloutUpperRightRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(false);
                    calloutHOffsetSlider.setValue(activeNote.getCalloutHorizontalOffset());
                    calloutVOffsetSlider.setValue(activeNote.getCalloutVerticalOffset());
                    break;
                case CALLOUT_LOWER_RIGHT:
                    calloutLowerRightRadioBtn.setSelected(true);
                    setDisableCalloutOffsetOptions(false);
                    calloutHOffsetSlider.setValue(activeNote.getCalloutHorizontalOffset());
                    calloutVOffsetSlider.setValue(activeNote.getCalloutVerticalOffset());
                    break;
                default:
                    resetToggle(displayToggle);
                    break;
            }
        } else {
            resetToggle(displayToggle);
        }
    }

    private void updateStoryFields() {
        if (storyTitle != null && storyDescription != null) {
            if (activeStory != null) {
                runLater(storyFieldsUpdateRunnable);
            } else {
                storyTitle.clear();
                storyDescription.clear();
                author.clear();
                date.clear();
                setActiveNote(null);
            }
        }
    }

    public Note getActiveNote() {
        return activeNote;
    }

    public void setActiveNote(final Note note) {
        activeNote = note;
        if (titleField != null && contentArea != null) {
            runLater(noteFieldsUpdateRunnable);
            if (note == null || note.hasColorScheme()) {
                createNoteColorSchemeButton.setDisable(true);
            } else {
                createNoteColorSchemeButton.setDisable(false);
            }
        }
    }

    public Story getActiveStory() {
        return activeStory;
    }

    public void setActiveStory(final Story story) {
        activeStory = story;
        updateStoryFields();
    }

    public void addDeleteButtonListener(final EventHandler<ActionEvent> handler) {
        delete.setOnAction(handler);
    }

    // ----- Begin button actions -----

    /**
     * Creates a new note for the active story
     */
    @FXML
    protected void newNote() {
        if (activeStory != null) {
            setActiveNote(new Note(activeStory, NEW_NOTE_TITLE, NEW_NOTE_CONTENTS));
            setNoteCreated(true);
        }
    }

    /**
     * Creates a color scheme for the active note by copying the story's scheme to it. The user can then edit the
     * rules in the context of the note without affecting the color scheme of the story.
     */
    @FXML
    protected void createColorSchemeForActiveNote() {
        if (activeStory != null && activeNote != null) {
            activeNote.setColorUrl(activeStory.getColorUrl());
            createNoteColorSchemeButton.setDisable(true);
        }
    }

    public enum Time {
        GLOBAL, CURRENT, RANGE
    }
}