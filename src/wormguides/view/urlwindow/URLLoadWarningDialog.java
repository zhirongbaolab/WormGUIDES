/*
 * Bao Lab 2016
 */

package wormguides.view.urlwindow;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import wormguides.util.AppFont;

public class URLLoadWarningDialog extends Dialog<ButtonType> {

	private final GridPane loadWarningPane;
	private final CheckBox checkBox;
	private final ButtonType buttonTypeOkay;
	private final ButtonType buttonTypeCancel;

	public URLLoadWarningDialog() {
		super();

		setTitle("Confirm");

		loadWarningPane = new GridPane();
		loadWarningPane.setHgap(10);
		loadWarningPane.setVgap(10);
		loadWarningPane.setPadding(new Insets(15, 15, 15, 15));

		checkBox = new CheckBox();
		checkBox.setText("Do not show warning again.");
		checkBox.setFont(AppFont.getFont());
		checkBox.setContentDisplay(ContentDisplay.TEXT_ONLY);

		final Label label = new Label(
				"Loading a URL erases all current color rules. " + "Are you sure you want to continue with loading?");
		label.setWrapText(true);
		label.setFont(AppFont.getFont());
		VBox.setVgrow(label, Priority.ALWAYS);
		HBox.setHgrow(label, Priority.ALWAYS);

		loadWarningPane.add(label, 0, 0);
		loadWarningPane.add(checkBox, 0, 1);

		buttonTypeOkay = new ButtonType("OK", ButtonData.OK_DONE);
		buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		getDialogPane().setContent(loadWarningPane);
		getDialogPane().getButtonTypes().addAll(buttonTypeOkay, buttonTypeCancel);

		loadWarningPane.setPrefSize(400, 100);
		loadWarningPane.setMinSize(400, 100);
		loadWarningPane.setMaxSize(400, 100);
	}

	public boolean doNotShowAgain() {
		return checkBox.isSelected();
	}

	public ButtonType getButtonTypeOkay() {
		return buttonTypeOkay;
	}
}
