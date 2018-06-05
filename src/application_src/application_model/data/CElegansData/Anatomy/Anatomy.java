/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.Anatomy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.MainApp;

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
    }

    public static void init() {
        final URL url = MainApp.class.getResource("/application_src/application_model/data/CElegansData/Anatomy/anatomy.csv");
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

    public static List<String> getFunctionalNames() { return functionalNames; }
    public static List<String> getTypes() { return types; }
    public static List<String> getSomaLocations() { return somaLocations; }
    public static List<String> getNeuriteLocations() { return neuriteLocations; }
    public static List<String> getMorphologicalFeatures() { return morphologicalFeatures; }
    public static List<String> getFunctions() { return functions; }
    public static List<String> getNeurotransmitters() { return neurotransmitters; }
}