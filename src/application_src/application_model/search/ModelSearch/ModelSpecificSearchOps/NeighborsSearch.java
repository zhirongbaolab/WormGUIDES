package application_src.application_model.search.ModelSearch.ModelSpecificSearchOps;

import application_src.application_model.data.LineageData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Collections.sort;

public class NeighborsSearch {

    private static LineageData lineageData;

    public NeighborsSearch(LineageData lineageData) {
        this.lineageData = lineageData;
    }

    /**
     * Searches for neighbors of a given cell. For each time point that the cell exists, the distance d of a cell
     * closest to it.
     * <p>
     * d = square-root((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)
     * <p>
     * Multiply d by 1.5 and get the maximum spherical radius that another cell must be within in order for that cell
     * to be considered a neighbor to the queried cell.
     *
     * @param cellName
     *         lineage name of the queried cell
     *
     * @return lineage names of all neighboring cells
     */
    public static List<String> getNeighboringCells(final String cellName) {
        final Set<String> cellsSet = new HashSet<>();

        if (cellName == null || !lineageData.isCellName(cellName)) {
            return new ArrayList<>();
        }

        // get time range for cell
        int firstOccurence = lineageData.getFirstOccurrenceOf(cellName);
        int lastOccurence = lineageData.getLastOccurrenceOf(cellName);

        for (int i = firstOccurence; i <= lastOccurence; i++) {
            String[] names = lineageData.getNames(i);
            double[][] positions = lineageData.getPositions(i);

            // find the coordinates of the query cell
            int queryIDX = -1;
            double x = -1;
            double y = -1;
            double z = -1;
            for (int j = 0; j < names.length; j++) {
                if (names[j].toLowerCase().equals(cellName.toLowerCase())) {
                    queryIDX = j;
                    x = positions[j][0];
                    y = positions[j][1];
                    z = positions[j][2];
                }
            }

            // find nearest neighbor
            if (x != -1 && y != -1 && z != -1) {
                double maxSphericalRadius = Double.MAX_VALUE;
                for (int k = 0; k < positions.length; k++) {
                    if (k != queryIDX) {
                        final double distanceFromQuery = distance(
                                x,
                                positions[k][0],
                                y,
                                positions[k][1],
                                z,
                                positions[k][2]);
                        if (distanceFromQuery < maxSphericalRadius) {
                            maxSphericalRadius = distanceFromQuery;
                        }
                    }
                }

                // multiple distance by 1.5
                if (maxSphericalRadius != Double.MAX_VALUE) {
                    maxSphericalRadius *= 1.5;
                }

                // find all cells within d*1.5 range
                for (int n = 0; n < positions.length; n++) {
                    // compute distance from each cell to query cell
                    if (distance(x, positions[n][0], y, positions[n][1], z, positions[n][2]) <= maxSphericalRadius) {
                        // only add new entries
                        if (!names[n].equalsIgnoreCase(cellName)) {
                            cellsSet.add(names[n]);
                        }
                    }
                }
            }
        }
        final List<String> cells = new ArrayList<>(cellsSet);
        sort(cells);
        return cells;
    }

    /**
     * @param x1
     *         x-coordinate of first element
     * @param x2
     *         x-coordinate of second element
     * @param y1
     *         y-coordinate of first element
     * @param y2
     *         y-coordinate of second element
     * @param z1
     *         z-coordinate of first element
     * @param z2
     *         z-coordinate of second element
     *
     * @return the distance between the two elements
     */
    private static double distance(double x1, double x2, double y1, double y2, double z1, double z2) {
        return sqrt(pow((x2 - x1), 2) + pow((y2 - y1), 2) + pow((z2 - z1), 2));
    }
}
