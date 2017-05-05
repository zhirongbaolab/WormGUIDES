/*
 * Bao Lab 2016
 */

package wormguides.models.cellcase;

/**
 * Defines a single homology between two cells
 */
public class EmbryonicHomology {

    private final String cell1;
    private final String cell2;

    public EmbryonicHomology(final String cell1, final String cell2) {
        this.cell1 = cell1;
        this.cell2 = cell2;
    }

    public String getCell1() {
        if (this.cell1 != null) {
            return this.cell1;
        }
        return "";
    }

    public String getCell2() {
        if (this.cell2 != null) {
            return this.cell2;
        }
        return "";
    }

    public String getHomology() {
        return getCell1() + ":" + getCell2();
    }
}
