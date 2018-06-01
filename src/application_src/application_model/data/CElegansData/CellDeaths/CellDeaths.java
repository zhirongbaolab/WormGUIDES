/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.CellDeaths;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import application_src.MainApp;

/**
 * The list of cell deaths represented in internal memory and a DOM for external window viewing
 */
public class CellDeaths {

    private static final String CellDeathsFile = "/application_src/application_model/data/CElegansData/CellDeaths/CellDeaths.csv";
    private static List<String> cellDeaths;

    public static void init() {
        cellDeaths = new ArrayList<>();
        final URL url = MainApp.class.getResource(CellDeathsFile);
        try (final InputStreamReader isr = new InputStreamReader(url.openStream());
             final BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                cellDeaths.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO remove this
    public static boolean isInCellDeaths(String str) { return false;}
    
    public static List<String> getCellDeathsAsArray() { return cellDeaths; }
}