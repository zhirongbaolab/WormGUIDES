/*
 * Bao Lab 2016
 */

package wormguides.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import wormguides.models.colorrule.SearchOption;

import static wormguides.models.colorrule.SearchOption.ANCESTOR;
import static wormguides.models.colorrule.SearchOption.CELL_BODY;
import static wormguides.models.colorrule.SearchOption.CELL_NUCLEUS;
import static wormguides.models.colorrule.SearchOption.DESCENDANT;

public class RuleEditorController extends AnchorPane implements Initializable {

    @FXML
    Text heading;

    @FXML
    CheckBox cellCheckBox;

    @FXML
    Label cellLabel;

    @FXML
    CheckBox cellBodyCheckBox;

    @FXML
    Label cellBodyLabel;

    @FXML
    CheckBox ancestorsCheckBox;

    @FXML
    Label ancestorsLabel;

    @FXML
    CheckBox descendantsCheckBox;

    @FXML
    Label descendantsLabel;

    @FXML
    ColorPicker colorPicker;

    @FXML
    Button submitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // no need to do anything here
    }

    public void disableDescendantOption() {
        descendantsLabel.setDisable(true);
        descendantsCheckBox.setDisable(true);
    }

    public void disableOptionsForStructureRule() {
        cellBodyLabel.setDisable(true);
        cellBodyCheckBox.setDisable(true);
        cellLabel.setDisable(true);
        cellCheckBox.setDisable(true);
        ancestorsLabel.setDisable(true);
        ancestorsCheckBox.setDisable(true);
        descendantsLabel.setDisable(true);
        descendantsCheckBox.setDisable(true);
    }

    public void setHeading(String name) {
        heading.setText(name);
    }

    public void setSubmitHandler(final EventHandler<ActionEvent> handler) {
        submitButton.setOnAction(handler);
    }

    public boolean isCellTicked() {
        return cellCheckBox.isSelected();
    }

    public void setCellTicked(boolean ticked) {
        cellCheckBox.setSelected(ticked);
    }

    public boolean isCellBodyTicked() {
        return cellBodyCheckBox.isSelected();
    }

    public void setCellBodyTicked(boolean ticked) {
        cellBodyCheckBox.setSelected(ticked);
    }

    public boolean isAncestorsTicked() {
        return ancestorsCheckBox.isSelected();
    }

    public void setAncestorsTicked(boolean ticked) {
        ancestorsCheckBox.setSelected(ticked);
    }

    public boolean isDescendantsTicked() {
        return descendantsCheckBox.isSelected();
    }

    public void setDescendantsTicked(boolean ticked) {
        descendantsCheckBox.setSelected(ticked);
    }

    public Color getColor() {
        return colorPicker.getValue();
    }

    public void setColor(Color color) {
        colorPicker.setValue(color);
    }

    public ArrayList<SearchOption> getOptions() {
        ArrayList<SearchOption> options = new ArrayList<>();
        if (isCellTicked()) {
            options.add(CELL_NUCLEUS);
        }
        if (isCellBodyTicked()) {
            options.add(CELL_BODY);
        }
        if (isAncestorsTicked()) {
            options.add(ANCESTOR);
        }
        if (isDescendantsTicked()) {
            options.add(DESCENDANT);
        }
        return options;
    }

}
