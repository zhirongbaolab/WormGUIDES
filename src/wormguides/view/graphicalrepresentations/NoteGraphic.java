package wormguides.view.graphicalrepresentations;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import wormguides.stories.Note;

import static java.util.Objects.requireNonNull;

import static javafx.geometry.Insets.EMPTY;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.layout.HBox.setHgrow;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;
import static javafx.scene.text.FontSmoothingType.LCD;

import static wormguides.layers.StoriesLayer.colorTexts;
import static wormguides.loaders.ImageLoader.getEyeIcon;
import static wormguides.loaders.ImageLoader.getEyeInvertIcon;
import static wormguides.util.AppFont.getBolderFont;
import static wormguides.util.AppFont.getFont;
import static wormguides.view.graphicalrepresentations.RuleGraphic.UI_SIDE_LENGTH;

/**
 * Graphical representation of a {@link Note} in the stories list in the application's left panel. When a note is
 * clicked, the time property is changed so that the 3D subscene navigates to the note's effective start time. This
 * graphical item is rendered in the {@link ListCell} of an active story in the  {@link ListView} in the 'Stories'
 * tab. Note titles are also expandable (making the notes description visible)  by clicking on the triangle rendered
 * to the left of the note's noteTitle.
 */
public class NoteGraphic extends VBox {

    private final HBox contentsContainer;
    private final Button visibleButton;
    private final Text expandIcon;
    private final Text noteTitle;
    private final Text noteContents;

    public NoteGraphic(final Note note, final BooleanProperty rebuildSubsceneFlag) {
        super();
        requireNonNull(note);
        requireNonNull(rebuildSubsceneFlag);

        setHgrow(this, ALWAYS);

        setPadding(new Insets(3));

        // note heading (its noteTitle) graphics
        final HBox titleContainer = new HBox(0);

        expandIcon = new Text("▶");
        expandIcon.setPickOnBounds(true);
        expandIcon.setFont(getFont());
        expandIcon.setFontSmoothingType(LCD);
        expandIcon.toFront();
        expandIcon.setOnMouseClicked(event -> {
            note.setListExpanded(!note.isListExpanded());
            expandNote(note.isListExpanded());
        });

        final Region r1 = new Region();
        r1.setPrefWidth(5);
        r1.setMinWidth(USE_PREF_SIZE);
        r1.setMaxWidth(USE_PREF_SIZE);

        visibleButton = new Button();
        final ImageView eyeIcon = getEyeIcon();
        final ImageView eyeIconInverted = getEyeInvertIcon();
        visibleButton.setPrefSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        visibleButton.setMinSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        visibleButton.setMaxSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        visibleButton.setPadding(EMPTY);
        visibleButton.setContentDisplay(GRAPHIC_ONLY);
        visibleButton.setOnAction(event -> {
            note.setVisible(!note.isVisible());
            rebuildSubsceneFlag.set(true);
        });
        if (note.isVisible()) {
            visibleButton.setGraphic(eyeIcon);
        } else {
            visibleButton.setGraphic(eyeIconInverted);
        }

        noteTitle = new Text(note.getTagName());
        noteTitle.setFont(getBolderFont());
        noteTitle.setFontSmoothingType(LCD);

        final Region r2 = new Region();
        setHgrow(r2, ALWAYS);

        titleContainer.getChildren().addAll(expandIcon, r1, noteTitle, r2, visibleButton);
        titleContainer.setAlignment(CENTER_LEFT);

        getChildren().add(titleContainer);

        // note contents graphics
        contentsContainer = new HBox(0);

        final Region r3 = new Region();
        r3.setPrefWidth(expandIcon.prefWidth(-1) + r1.prefWidth(-1));
        r3.setMinWidth(USE_PREF_SIZE);
        r3.setMaxWidth(USE_PREF_SIZE);

        noteContents = new Text(note.getTagContents());
        noteContents.setFont(getFont());
        noteContents.setFontSmoothingType(LCD);

        contentsContainer.getChildren().addAll(r3, noteContents);
        expandNote(note.isListExpanded());

        setPickOnBounds(false);

        note.getActiveProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highlightCell(true);
            } else {
                highlightCell(false);
            }
        });

        note.getVisibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                visibleButton.setGraphic(eyeIcon);
            } else {
                visibleButton.setGraphic(eyeIconInverted);
            }
        });

        highlightCell(note.isActive());

        // render note changes
        note.getChangedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                noteTitle.setText(note.getTagName());
                noteContents.setText(note.getTagContents());
            }
        });
    }

    /**
     * @param intersectedNode
     *         the clicked node
     *
     * @return true if the expand icon was clicked, false otherwise
     */
    public boolean isExpandIconClicked(final Node intersectedNode) {
        return intersectedNode == expandIcon;
    }

    public void setWidth(final ReadOnlyDoubleProperty storyWidthProperty) {
        storyWidthProperty.addListener((observable, oldValue, newValue) -> {
            final int newWidth = newValue.intValue();
            noteTitle.setWrappingWidth(newWidth - UI_SIDE_LENGTH - 25);
            noteContents.setWrappingWidth(newWidth - 25);
        });
    }

    public void setTagName(final String titleText) {
        if (titleText != null) {
            noteTitle.setText(titleText);
        }
    }

    public void setTagContents(final String contentText) {
        if (contentText != null) {
            noteContents.setText(contentText);
        }
    }

    /**
     * @return the expand triangle wedge icon
     */
    public Text getExpandIcon() {
        return expandIcon;
    }

    public void setClickedHandler(final EventHandler<MouseEvent> handler) {
        if (handler != null) {
            setOnMouseClicked(handler);
        }
    }

    public void setVisibleButtonClickedHandler(final EventHandler<MouseEvent> handler) {
        visibleButton.setOnMouseClicked(handler);
    }

    /**
     * Highlights/un-highlights a cell according to the input parameter.
     * When a cell is highlighted/un-highighted, its text and background
     * colors change.
     *
     * @param highlighted
     *         true when this note graphic is to be highlighted, false otherwise
     */
    private void highlightCell(final boolean highlighted) {
        if (highlighted) {
            setStyle("-fx-background-color: -fx-focus-color, -fx-cell-focus-inner-border, -fx-selection-bar; "
                    + "-fx-background: -fx-accent;");
            colorTexts(WHITE, expandIcon, noteTitle, noteContents);
        } else {
            setStyle("-fx-background-color: white;");
            colorTexts(BLACK, expandIcon, noteTitle, noteContents);
        }
    }

    /**
     * Expands/hides a notes description according to the input parameter.
     *
     * @param expanded
     *         true when the note should be expanded (showing the description), false otherwise
     */
    private void expandNote(boolean expanded) {
        if (expanded) {
            getChildren().add(contentsContainer);
            expandIcon.setText(expandIcon.getText().replace("▶", "▼"));
        } else {
            getChildren().remove(contentsContainer);
            expandIcon.setText(expandIcon.getText().replace("▼", "▶"));
        }
    }
}