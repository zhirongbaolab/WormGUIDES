/*
 * Bao Lab 2016
 */

package wormguides.view.popups;

import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

/**
 * Anchor pane that contains the info for the 'About WormGUIDES' window
 */
public class AboutPane extends AnchorPane {

    public AboutPane() {
        super();

        setPrefHeight(420.0);
        setPrefWidth(300.0);

        final TextArea area = new TextArea();
        setBottomAnchor(area, 0.0);
        setLeftAnchor(area, 0.0);
        setRightAnchor(area, 0.0);
        setTopAnchor(area, 0.0);

        area.setEditable(false);
        area.setText("WormGUIDES is a collaboration led by Drs. Zhirong Bao (MSKCC), "
                + "Daniel Colon-Ramos (Yale), William Mohler (UConn) and Hari Shroff (NIH). "
                + "For more information, visit our website at http://wormguides.org.\n\n"
                + "The WormGUIDES app is developed and maintained by the laboratories of Dr. Zhirong Bao "
                + "and Dr. William Mohler. Major contributors of the desktop app include "
                + "Doris Tang (New York University), Braden Katzman (Columbia University), and Dr. Anthony Santella "
                + "of the Bao Laboratory.\n\n"
                + "For questions or comments contact support@wormguides.org.");
        area.setWrapText(true);
        area.setStyle("-fx-border-radius: 0; -fx-background-radius: 0; "
                + "-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
        getChildren().add(area);
    }

}