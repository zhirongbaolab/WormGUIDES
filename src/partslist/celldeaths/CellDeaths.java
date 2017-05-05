/*
 * Bao Lab 2016
 */

package partslist.celldeaths;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import partslist.PartsList;

/**
 * The list of cell deaths represented in internal memory and a DOM for external window viewing
 */
public class CellDeaths {

    private static final String CellDeathsFile = "/partslist/celldeaths/CellDeaths.csv";
    private static List<String> cellDeaths;

    public static void init() {
        cellDeaths = new ArrayList<>();
        final URL url = PartsList.class.getResource(CellDeathsFile);
        try (final InputStreamReader isr = new InputStreamReader(url.openStream());
             final BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                cellDeaths.add(line.toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean containsCell(String cell) {
        return cellDeaths != null && cellDeaths.contains(cell.toLowerCase());
    }
    
    public static Object[] getCellDeathsAsArray() {
    	return cellDeaths.toArray();
    }

    public static boolean isInCellDeaths(final String name) {
        return cellDeaths.contains(name);
    }
}