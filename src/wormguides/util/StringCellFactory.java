/*
 * Bao Lab 2016
 */

/*
 * Bao Lab 2016
 */

package wormguides.util;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import static javafx.scene.control.ContentDisplay.GRAPHIC_ONLY;
import static javafx.scene.paint.Color.BLACK;

import static wormguides.util.AppFont.getFont;

/**
 * Cell factories for cells containing a string
 */
public class StringCellFactory {

    /** Height in pixels of the  cell */
    public static final double UI_HEIGHT = 26.0;

    /**
     * Creates the graphic for a ListCell in structures ListView's
     *
     * @param cellText
     *         text for the cell
     *
     * @return the {@link HBox} that is the cell graphic
     */
    private static HBox createCellGraphic(final String cellText) {
        final HBox hbox = new HBox();
        final Label label = new Label(cellText);
        label.setFont(getFont());
        label.setPrefHeight(UI_HEIGHT);
        label.setMinHeight(UI_HEIGHT);
        label.setStyle("-fx-fill-color: black;");
        label.setTextFill(BLACK);
        hbox.getChildren().add(label);
        return hbox;
    }

    /**
     * Callback for ListCell<String> so that fonts are uniform
     */
    public static class StringListCellFactory implements Callback<ListView<String>, ListCell<String>> {

        @Override
        public ListCell<String> call(ListView<String> param) {
            return new StringListCell();
        }

        public ListCell<String> getNewStringListCell() {
            return new StringListCell();
        }

        private class StringListCell extends ListCell<String> {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(GRAPHIC_ONLY);
                setFocusTraversable(false);
                if (item != null && !empty) {
                    setGraphic(createCellGraphic(item));
                } else {
                    setGraphic(null);
                }
            }
        }
    }
}
