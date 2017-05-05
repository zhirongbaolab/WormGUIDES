/*
 * Bao Lab 2017
 */

package wormguides.view.graphicalrepresentations;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import wormguides.models.colorrule.Rule;

import static java.util.Objects.requireNonNull;

import static javafx.geometry.Insets.EMPTY;
import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.control.OverrunStyle.ELLIPSIS;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.layout.Priority.SOMETIMES;
import static javafx.scene.paint.Color.LIGHTGREY;

import static wormguides.loaders.ImageLoader.getCloseIcon;
import static wormguides.loaders.ImageLoader.getEditIcon;
import static wormguides.loaders.ImageLoader.getEyeIcon;
import static wormguides.loaders.ImageLoader.getEyeInvertIcon;
import static wormguides.util.AppFont.getFont;

/**
 * Graphical representation of a rule (used to display a rule in a list view)
 */
public class RuleGraphic extends HBox {

    /** Length and width (in pixels) of color rule UI buttons */
    public static final int UI_SIDE_LENGTH = 22;

    private final Label label;
    private final Rectangle colorRectangle;
    private final Button editBtn;
    private final Button visibleBtn;
    private final Button deleteBtn;
    private final Tooltip toolTip;
    private final ImageView eyeIcon;
    private final ImageView eyeIconInverted;

    public RuleGraphic(final Rule rule, final BooleanProperty ruleChanged) {
        super();

        requireNonNull(rule);
        requireNonNull(ruleChanged);

        label = new Label();
        colorRectangle = new Rectangle(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        editBtn = new Button();
        visibleBtn = new Button();
        deleteBtn = new Button();
        toolTip = new Tooltip();

        setSpacing(3);
        setPadding(new Insets(3));
        setPrefWidth(275);
        setMinWidth(getPrefWidth());

        label.setFont(getFont());
        label.setPrefHeight(UI_SIDE_LENGTH);
        label.setMaxHeight(UI_SIDE_LENGTH);
        label.setMinHeight(UI_SIDE_LENGTH);
        setHgrow(label, ALWAYS);
        label.textOverrunProperty().set(ELLIPSIS);

        final Region r = new Region();
        setHgrow(r, SOMETIMES);

        colorRectangle.setHeight(UI_SIDE_LENGTH);
        colorRectangle.setWidth(UI_SIDE_LENGTH);
        colorRectangle.setStroke(LIGHTGREY);

        editBtn.setPrefSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        editBtn.setMaxSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        editBtn.setMinSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        editBtn.setContentDisplay(GRAPHIC_ONLY);
        editBtn.setPadding(EMPTY);
        editBtn.setGraphic(getEditIcon());
        editBtn.setGraphicTextGap(0);
        editBtn.setOnAction(event -> rule.showEditStage(null));

        eyeIcon = getEyeIcon();
        eyeIconInverted = getEyeInvertIcon();

        visibleBtn.setPrefSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        visibleBtn.setMaxSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        visibleBtn.setMinSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        visibleBtn.setPadding(EMPTY);
        visibleBtn.setContentDisplay(GRAPHIC_ONLY);
        visibleBtn.setGraphic(eyeIcon);
        visibleBtn.setGraphicTextGap(0);
        visibleBtn.setOnAction(event -> {
            rule.setVisible(!rule.isVisible());
            blackOutVisibleButton(!rule.isVisible());
            ruleChanged.set(true);
        });

        deleteBtn.setPrefSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        deleteBtn.setMaxSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        deleteBtn.setMinSize(UI_SIDE_LENGTH, UI_SIDE_LENGTH);
        deleteBtn.setPadding(EMPTY);
        deleteBtn.setContentDisplay(GRAPHIC_ONLY);
        deleteBtn.setGraphic(getCloseIcon());

        toolTip.setFont(getFont());
        label.setTooltip(toolTip);

        getChildren().addAll(label, r, colorRectangle, editBtn, visibleBtn, deleteBtn);
    }

    /**
     * Sets the graphic for the visible eye icon
     *
     * @param isRuleInvisible
     *         true if the rule is visible, false otherwise
     */
    public void blackOutVisibleButton(final boolean isRuleInvisible) {
        if (isRuleInvisible) {
            visibleBtn.setGraphic(eyeIconInverted);
        } else {
            visibleBtn.setGraphic(eyeIcon);
        }
    }

    /**
     * @return the rule label text
     */
    public String getLabelText() {
        return label.getText();
    }

    /**
     * Changes the color of the rectangle displayed next to the rule name in the rule's graphical representation.
     *
     * @param color
     *         color that the rectangle in the graphical representation of the rule should be changed to
     */
    public void setColorButton(final Color color) {
        if (color != null) {
            colorRectangle.setFill(color);
        }
    }

    /**
     * Resets the label to the text
     *
     * @param labelText
     *         the text
     */
    public void resetLabel(final String labelText) {
        if (labelText != null) {
            label.setText(labelText);
        }
    }

    /**
     * Resets the tooltip to the text
     *
     * @param tooltipText
     *         the text
     */
    public void resetTooltip(final String tooltipText) {
        if (tooltipText != null) {
            toolTip.setText(tooltipText);
        }
    }

    public Button getDeleteButton() {
        return deleteBtn;
    }
}
