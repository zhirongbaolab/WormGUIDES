/*
 * Bao Lab 2016
 */

package wormguides.view.urlwindow;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import wormguides.loaders.ImageLoader;
import wormguides.models.colorrule.Rule;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.util.Objects.requireNonNull;

import static javafx.geometry.Pos.CENTER;
import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.layout.HBox.setHgrow;
import static javafx.scene.layout.Priority.ALWAYS;

import static wormguides.util.AppFont.getFont;
import static wormguides.util.colorurl.UrlGenerator.generateAndroid;

public class URLShareWindow extends AnchorPane {

    private final TextField urlField;
    private final Button resetBtn;
    private final Button closeBtn;
    private final Clipboard cb;

    private final ObservableList<Rule> rulesList;
    private final IntegerProperty timeProperty;
    private final DoubleProperty rotateXAngleProperty;
    private final DoubleProperty rotateYAngleProperty;
    private final DoubleProperty rotateZAngleProperty;
    private final DoubleProperty translateXProperty;
    private final DoubleProperty translateYProperty;
    private final DoubleProperty zoomProperty;
    private final DoubleProperty othersOpacityProperty;

    private String urlString;

    public URLShareWindow(
            final ObservableList<Rule> rulesList,
            final IntegerProperty timeProperty,
            final DoubleProperty rotateXAngleProperty,
            final DoubleProperty rotateYAngleProperty,
            final DoubleProperty rotateZAngleProperty,
            final DoubleProperty translateXProperty,
            final DoubleProperty translateYProperty,
            final DoubleProperty zoomProperty,
            final DoubleProperty othersOpacityProperty) {

        super();
        setPrefWidth(430);

        this.rulesList = requireNonNull(rulesList);
        this.timeProperty = requireNonNull(timeProperty);
        this.rotateXAngleProperty = requireNonNull(rotateXAngleProperty);
        this.rotateYAngleProperty = requireNonNull(rotateYAngleProperty);
        this.rotateZAngleProperty = requireNonNull(rotateZAngleProperty);
        this.translateXProperty = requireNonNull(translateXProperty);
        this.translateYProperty = requireNonNull(translateYProperty);
        this.zoomProperty = requireNonNull(zoomProperty);
        this.othersOpacityProperty = requireNonNull(othersOpacityProperty);

        cb = getDefaultToolkit().getSystemClipboard();
        final Tooltip tooltip = new Tooltip("copy");

        final VBox vBox = new VBox();
        vBox.setSpacing(10);
        setTopAnchor(vBox, 10.0);
        setLeftAnchor(vBox, 10.0);
        setRightAnchor(vBox, 10.0);
        setBottomAnchor(vBox, 10.0);
        getChildren().add(vBox);

        final HBox androidHBox = new HBox(10);
        urlField = new TextField();
        urlField.setFont(getFont());
        urlField.setPrefHeight(28);
        urlField.setEditable(false);
        urlField.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        setHgrow(urlField, ALWAYS);
        Button androidCopyBtn = new Button();
        androidCopyBtn.setPrefSize(28, 28);
        androidCopyBtn.setMinSize(28, 28);
        androidCopyBtn.setTooltip(tooltip);
        androidCopyBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        androidCopyBtn.setContentDisplay(GRAPHIC_ONLY);
        androidCopyBtn.setGraphic(ImageLoader.getCopyIcon());
        androidCopyBtn.setOnAction(event -> {
            final StringSelection ss = new StringSelection(urlField.getText());
            cb.setContents(ss, null);
        });
        androidHBox.getChildren().addAll(urlField, androidCopyBtn);

        resetBtn = new Button("Generate");
        resetBtn.setPrefWidth(100);
        resetBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        resetBtn.setFont(getFont());
        resetBtn.setOnAction(event -> resetURLs());

        closeBtn = new Button("Close");
        closeBtn.setPrefWidth(100);
        closeBtn.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        closeBtn.setFont(getFont());

        final HBox hBox = new HBox();
        hBox.setSpacing(20);
        hBox.setAlignment(CENTER);
        hBox.getChildren().addAll(resetBtn, closeBtn);

        vBox.getChildren().addAll(androidHBox, hBox);
    }

    public void resetURLs() {
        urlString = generateAndroid(
                rulesList,
                timeProperty.get(),
                rotateXAngleProperty.get(),
                rotateYAngleProperty.get(),
                rotateZAngleProperty.get(),
                translateXProperty.get(),
                translateYProperty.get(),
                zoomProperty.get(),
                othersOpacityProperty.get());
        urlField.setText(urlString);
    }

    public Button getCloseButton() {
        return closeBtn;
    }
}
