/*
 * Bao Lab 2016
 */

package wormguides.models.colorrule;

/**
 * Options queried by the subscene entities to see if a color rule applies to it.
 */
public enum SearchOption {

    /**
     * SearchLayer for cells associated with the searched name. The {@link SearchOption#ANCESTOR} and {@link
     * SearchOption#DESCENDANT} searches are based off the list returned when this search is made.
     */
    CELL_NUCLEUS("cell nucleus"),

    /** Search for cell bodies that contain the cells in the search results list */
    CELL_BODY("cell body"),

    /** SearchLayer for ancestors of cells in the search results list */
    ANCESTOR("its ancestors"),

    /** SearchLayer for descendants of cells in the search results list */
    DESCENDANT("its descendants");

    private final String description;

    SearchOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
