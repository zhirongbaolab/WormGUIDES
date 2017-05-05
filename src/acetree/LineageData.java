/*
 * Bao Lab 2016
 */

package acetree;

import java.util.List;

/**
 * Data structure interface with methods to query the underlying cell lineage data.
 */
public interface LineageData {

    /**
     * @return all cell names in the lineage, case-sensitive
     */
    List<String> getAllCellNames();

    /**
     * @param time
     *         time to check
     *
     * @return names of cells that exist at that time. The i-th element of the name, positions and diameter arrays
     * are information on the i-th cell at one timepoint.
     */
    String[] getNames(final int time);

    /**
     * @param time
     *         time to check
     *
     * @return size-3 integer arrays that specify the x-, y-, z-coordinates of the cell positions in 3D space. The i-th
     * element of the name, positions and diameter arrays are information on the i-th cell at one timepoint.
     */
    double[][] getPositions(final int time);

    /**
     * @param time
     *         time to check
     *
     * @return diameters at that time.  The i-th element of the name, positions and diameter arrays are information
     * on the i-th cell at one timepoint.
     */
    double[] getDiameters(final int time);

    /**
     * @return the x,y,z scaling values for the dataset
     */
    double[] getXYZScale();

    /**
     * @return number of total timepoints in the lineage
     */
    int getNumberOfTimePoints();

    /**
     * Retrieves the first occurence of a cell with a specified name
     *
     * @param name
     *         the name of the cell
     *
     * @return first point in time for which the cell exists
     */
    int getFirstOccurrenceOf(final String name);

    /**
     * Retrieves the last occurence of a cell with a specified name
     *
     * @param name
     *         the name of the cell
     *
     * @return final point in time for which the cell exists
     */
    int getLastOccurrenceOf(final String name);

    /**
     * @param name
     *         name to check
     *
     * @return true if the name is a case-insensitive cell name in the lineage data, false otherwise
     */
    boolean isCellName(final String name);

    /**
     * Shifts all the positions in all time frames by a specified x-, y- and z-offset.
     *
     * @param x
     *         Amount of offset the x-coordinates by
     * @param y
     *         Amount of offset the y-coordinates by
     * @param z
     *         Amount of offset the z-coordinates by
     */
    void shiftAllPositions(final int x, final int y, final int z);

    /**
     * @return true if the lineage is in Sulston mode, false otherwise
     */
    boolean isSulstonMode();

    /**
     * Sets the flag that speficies whether the lineage is in Sulston mode
     *
     * @param isSulston
     *         false if the lineage is in Sulston mode, false otherwise
     */
    void setIsSulstonModeFlag(final boolean isSulston);
}