/*
 * Bao Lab 2016
 */

package wormguides.view.urlwindow;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import wormguides.loaders.ImageLoader;

import static wormguides.util.AppFont.getFont;

public class URLLoadWindow extends AnchorPane {

    private Label label;
    private TextField field;
    private Button loadBtn;
    private Button cancelBtn;
    private Button clearBtn;

    public URLLoadWindow() {
        super();
        setPrefWidth(430);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        AnchorPane.setTopAnchor(vBox, 10.0);
        AnchorPane.setLeftAnchor(vBox, 10.0);
        AnchorPane.setRightAnchor(vBox, 10.0);
        AnchorPane.setBottomAnchor(vBox, 10.0);

        label = new Label("Paste URL here:");
        label.setFont(getFont());

        HBox fieldHBox = new HBox(10);
        field = new TextField();
        field.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        field.setFont(getFont());
        HBox.setHgrow(field, Priority.ALWAYS);
        Tooltip tt = new Tooltip("paste");
        Button pasteBtn = new Button();
        pasteBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        pasteBtn.setGraphic(ImageLoader.getPasteIcon());
        pasteBtn.maxWidthProperty().bind(field.heightProperty());
        pasteBtn.prefWidthProperty().bind(field.heightProperty());
        pasteBtn.minWidthProperty().bind(field.heightProperty());
        pasteBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        pasteBtn.setTooltip(tt);
        pasteBtn.setOnAction(arg0 -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            boolean hasTransferableText = (contents != null)
                    && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            if (hasTransferableText) {
                try {
                    field.setText((String) contents.getTransferData(DataFlavor.stringFlavor));
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        fieldHBox.getChildren().addAll(field, pasteBtn);

        loadBtn = new Button("Load");
        loadBtn.setFont(getFont());
        loadBtn.setPrefWidth(70);
        loadBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        clearBtn = new Button("Clear");
        clearBtn.setFont(getFont());
        clearBtn.setPrefWidth(70);
        clearBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        clearBtn.setOnAction(arg0 -> field.clear());
        cancelBtn = new Button("Cancel");
        cancelBtn.setFont(getFont());
        cancelBtn.setPrefWidth(70);
        cancelBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(loadBtn, clearBtn, cancelBtn);

        vBox.getChildren().addAll(label, fieldHBox, hBox);
        getChildren().add(vBox);
    }

    public String getInputURL() {
        return field.getText();
    }

    public Button getLoadButton() {
        return loadBtn;
    }

    public Button getCancelButton() {
        return cancelBtn;
    }

    public void clearField() {
        field.clear();
    }
}
