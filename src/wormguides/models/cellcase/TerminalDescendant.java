package wormguides.models.cellcase;

/**
 * Class which holds a cell name and parts list entry for each terminal descendant (neuron)
 */
public class TerminalDescendant {

    private final String cellName;
    private final String partsListEntry;

    public TerminalDescendant(final String cellName, final String partsListEntry) {
        if (cellName == null) {
            this.cellName = "N/A";
        } else {
            this.cellName = cellName;
        }
        if (partsListEntry == null) {
            this.partsListEntry = "N/A";
        } else {
            this.partsListEntry = partsListEntry;
        }
    }

    public String getCellName() {
        return cellName;
    }

    public String getPartsListEntry() {
        return partsListEntry;
    }
}