/*
 * Bao Lab 2016
 */

package wormguides.models.anatomy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import partslist.PartsList;

/**
 * Contains anatomy information for a select number of cells
 */
public class Anatomy {

    private static final List<String> functionalNames;
    private static final List<String> types;
    private static final List<String> somaLocations;
    private static final List<String> neuriteLocations;
    private static final List<String> morphologicalFeatures;
    private static final List<String> functions;
    private static final List<String> neurotransmitters;

    static {
        functionalNames = new ArrayList<>();
        types = new ArrayList<>();
        somaLocations = new ArrayList<>();
        neuriteLocations = new ArrayList<>();
        morphologicalFeatures = new ArrayList<>();
        functions = new ArrayList<>();
        neurotransmitters = new ArrayList<>();

        final URL url = PartsList.class.getResource("/wormguides/models/anatomy_file/anatomy.csv");
        try (InputStream input = url.openStream();
             InputStreamReader isr = new InputStreamReader(input);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                //valid line has 7 entires
                if (tokenizer.countTokens() == 7) {
                    functionalNames.add(tokenizer.nextToken());
                    types.add(tokenizer.nextToken());
                    somaLocations.add(tokenizer.nextToken());
                    neuriteLocations.add(tokenizer.nextToken());
                    morphologicalFeatures.add(tokenizer.nextToken());
                    functions.add(tokenizer.nextToken());
                    neurotransmitters.add(tokenizer.nextToken());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Checks if the supplied cell has an anatomy description. If a lineage name is given, it is translated to a
     * functional name first.
     *
     * @param cellName
     *         name of the cell, lineage or functional
     *
     * @return true if cell has anatomy, false otherwise
     */
    public static boolean hasAnatomy(String cellName) {
        cellName = checkQueryCell(cellName);

        //check for exact match
        for (String funcName : functionalNames) {
            if (funcName.equals(cellName)) {
                return true;
            }
        }
        cellName = findRootOfCell(cellName);
        //check for match with updated cell name
        for (String funcName : functionalNames) {
            if (funcName.equals(cellName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds the base name of a cell i.e. the cell without dorsal, ventral,
     * left, right, etc. classifiers
     *
     * @param cell
     *         the cell to find the base of
     *
     * @return the base name of the cell
     */
    private static String findRootOfCell(String cell) {
        //remove number suffixes, l/r, d/v
        Character lastChar = cell.charAt(cell.length() - 1);
        lastChar = Character.toLowerCase(lastChar);
        if (lastChar == 'r' || lastChar == 'l') {
            cell = cell.substring(0, cell.length() - 1);

            // check if preceding d/v
            lastChar = cell.charAt(cell.length() - 1);
            lastChar = Character.toLowerCase(lastChar);
            if (lastChar == 'd' || lastChar == 'v') {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (lastChar == 'd' || lastChar == 'v') { // will l/r ever come
            // before d/v
            cell = cell.substring(0, cell.length() - 1);

            // check if preceding l/r
            lastChar = cell.charAt(cell.length() - 1);
            lastChar = Character.toLowerCase(lastChar);
            if (lastChar == 'l' || lastChar == 'r') {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (Character.isDigit(lastChar)) {
            cell = cell.substring(0, cell.length() - 1).toUpperCase();
        }

        return cell;
    }

    /**
     * Provides name translation from systematic to functional (originally used in Connectome.java)
     *
     * @param queryCell
     *         the cell to be checked
     *
     * @return the resultant translated or untranslated cell name
     */
    private static String checkQueryCell(String queryCell) {
        if (PartsList.isLineageName(queryCell)) {
            queryCell = PartsList.getFunctionalNameByLineageName(queryCell).toUpperCase();
        }

        return queryCell;
    }

    /**
     * Provides anatomy info for a given cell
     *
     * @param cellName
     *         name of the cell
     *
     * @return the anatomy information for the given cell
     */
    public static ArrayList<String> getAnatomy(String cellName) {
        ArrayList<String> anatomy = new ArrayList<>();

        if (hasAnatomy(cellName)) {
            int idx = -1;

            //exact match
            for (int i = 0; i < functionalNames.size(); i++) {
                if (functionalNames.get(i).equals(cellName)) {
                    idx = i;
                    break;
                }
            }

            //if no exact match, update cell and search again
            if (idx == -1) {
                cellName = findRootOfCell(cellName);

                //check for match with updated cell name
                for (int i = 0; i < functionalNames.size(); i++) {
                    if (functionalNames.get(i).equals(cellName)) {
                        idx = i;
                    }
                }
            }

            if (idx != -1) {
                //add functional name
                anatomy.add(functionalNames.get(idx));

                //add type
                if (types.get(idx) != null) {
                    anatomy.add(types.get(idx));
                } else {
                    anatomy.add("*");
                }

                //add soma location
                if (somaLocations.get(idx) != null) {
                    anatomy.add(somaLocations.get(idx));
                } else {
                    anatomy.add("*");
                }

                //add neurite location
                if (neuriteLocations.get(idx) != null) {
                    anatomy.add(neuriteLocations.get(idx));
                } else {
                    anatomy.add("*");
                }

                //add morphological features
                if (morphologicalFeatures.get(idx) != null) {
                    anatomy.add(morphologicalFeatures.get(idx));
                } else {
                    anatomy.add("*");
                }

                //add function
                if (functions.get(idx) != null) {
                    anatomy.add(functions.get(idx));
                } else {
                    anatomy.add("*");
                }

                //add neurotransmitter
                if (neurotransmitters.get(idx) != null) {
                    anatomy.add(neurotransmitters.get(idx));
                } else {
                    anatomy.add("*");
                }
            }
        }
        return anatomy;
    }
}
