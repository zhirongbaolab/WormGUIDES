/*
 * Bao Lab 2016
 */

package wormguides.view.popups;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static java.lang.Integer.MAX_VALUE;

import static javafx.scene.layout.HBox.setHgrow;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.text.TextAlignment.CENTER;

import static wormguides.util.AppFont.getBoldFont;
import static wormguides.util.AppFont.getFont;

/**
 * This class is a popup dialog that contains some prompt text and two buttons, yes and no. The Strings for these
 * three components are set upon initialization.
 */
public class StorySavePane extends AnchorPane {

    private final Button yesBtn;
    private final Button noBtn;
    private final Button cancelBtn;

    private Text promptText;

    public StorySavePane(
            final EventHandler<ActionEvent> yesHandler,
            final EventHandler<ActionEvent> noHandler,
            final EventHandler<ActionEvent> cancelHandler) {

        super();

        final String prompt = "Save active story before quitting?";
        final String yesButtonText = "Save";
        final String noButtonText = "Don't Save";
        final String cancelButtonText = "Cancel";

        final VBox mainVBox = new VBox(10);
        setTopAnchor(mainVBox, 10.0);
        setLeftAnchor(mainVBox, 10.0);
        setRightAnchor(mainVBox, 10.0);
        setBottomAnchor(mainVBox, 10.0);

        mainVBox.setStyle("-fx-background-color: white; -fx-border-color: black;");

        // initialize prompt text
        promptText = new Text();
        promptText.setFont(getFont());
        promptText.wrappingWidthProperty().bind(mainVBox.widthProperty().subtract(10));
        promptText.setTextAlignment(CENTER);
        promptText.setText(prompt);

        mainVBox.getChildren().add(promptText);

        // initialize buttons
        yesBtn = new Button();
        yesBtn.setText(yesButtonText);
        yesBtn.setFont(getBoldFont());
        yesBtn.setPrefWidth(70);
        yesBtn.setMaxHeight(MAX_VALUE);
        yesBtn.setOnAction(yesHandler);

        noBtn = new Button();
        noBtn.setText(noButtonText);
        noBtn.setFont(getFont());
        noBtn.setPrefWidth(100);
        noBtn.setMaxHeight(MAX_VALUE);
        noBtn.setOnAction(noHandler);

        cancelBtn = new Button();
        cancelBtn.setText(cancelButtonText);
        cancelBtn.setFont(getFont());
        cancelBtn.setPrefWidth(70);
        cancelBtn.setMaxHeight(MAX_VALUE);
        cancelBtn.setOnAction(cancelHandler);

        final Region r1 = new Region();
        sizeRegion(r1);
        final Region r2 = new Region();
        sizeRegion(r2);
        final Region r3 = new Region();
        sizeRegion(r3);
        final Region r4 = new Region();
        sizeRegion(r4);

        final HBox btnHBox = new HBox(10);
        btnHBox.getChildren().addAll(r1, yesBtn, r2, noBtn, r3, cancelBtn, r4);
        for (Node child : btnHBox.getChildrenUnmodifiable()) {
            child.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        }
        mainVBox.getChildren().add(btnHBox);

        mainVBox.setPadding(new Insets(10, 0, 10, 0));
//        mainVBox.setMinHeight(115.0);
        getChildren().add(mainVBox);
    }

    private void sizeRegion(final Region r) {
        setHgrow(r, ALWAYS);
    }
}
