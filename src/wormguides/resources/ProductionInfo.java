/*
 * Bao Lab 2016
 */

package wormguides.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.lang.Integer.parseInt;

import static wormguides.loaders.ProductionInfoLoader.buildProductionInfo;

/**
 * Class which holds the database of production info defined in /wormguides/model/production_info_file/
 */
public class ProductionInfo {

    private final String TRUE = "TRUE";

    private final int DEFAULT_START_TIME = 1;

    private List<List<String>> productionInfoData;

    public ProductionInfo() {
        productionInfoData = buildProductionInfo();
    }

    public List<String> getNuclearInfo() {
        final List<String> nuclearInfo = new ArrayList<>();
        if (productionInfoData.get(0).get(0).equals("all-nuclear positions")) {
            // store, strain, marker, data
            nuclearInfo.add(productionInfoData.get(3).get(0)
                    + ", "
                    + productionInfoData.get(2).get(0));
            // store image, series data
            nuclearInfo.add(productionInfoData.get(1).get(0));
        }
        return nuclearInfo;
    }

    public boolean getIsSulstonFlag() {
        return TRUE.equalsIgnoreCase(productionInfoData.get(9).get(0));
    }

    public int getTotalTimePoints() {
        return parseInt(productionInfoData.get(10).get(0));
    }

    public int getXScale() {
        return parseInt(productionInfoData.get(11).get(0));
    }

    public int getYScale() {
        return parseInt(productionInfoData.get(12).get(0));
    }

    public int getZScale() {
        return parseInt(productionInfoData.get(13).get(0));
    }

    public int getDefaultStartTime() {
        return DEFAULT_START_TIME;
    }

    public int getMovieTimeOffset() {
        String input = productionInfoData.get(8).get(0);
        try {
            int startTime = parseInt(input);
            return startTime - DEFAULT_START_TIME;
        } catch (NumberFormatException e) {
            System.out.println("Input: '" + input + "'");
            System.out.println("Invalid input for movie start time. Using default start time of " + DEFAULT_START_TIME);
        }
        return 0;
    }

    public List<String> getCellShapeData(String queryCell) {
        final List<String> cellShapeData = new ArrayList<>();
        String cells;
        StringTokenizer tokenizer;
        String token;
        for (int i = 0; i < productionInfoData.get(0).size(); i++) {
             cells = productionInfoData.get(0).get(i);
            //delimit cells by ';'
            tokenizer = new StringTokenizer(cells, ";");
            while (tokenizer.hasMoreTokens()) {
                 token = tokenizer.nextToken().trim();
                if (token.equalsIgnoreCase(queryCell)) {
                    // store strain, marker data
                    cellShapeData.add(productionInfoData.get(3).get(i) + ", " + productionInfoData.get(2).get(i));
                    // store image series data
                    cellShapeData.add(productionInfoData.get(1).get(i));
                    break;
                }
            }
        }
        return cellShapeData;
    }

    public double[] getKeyFramesRotate() {
        // idx 14
        String keyFramesRotateStr = productionInfoData.get(14).get(0);
        StringTokenizer st = new StringTokenizer(keyFramesRotateStr, " ");

        double[] keyFramesRotate = new double[st.countTokens()];
        int numTokens = st.countTokens();
        for (int i = 0; i < numTokens; i++) {
            keyFramesRotate[i] = Double.parseDouble(st.nextToken());
        }

        return keyFramesRotate;
    }

    public double[] getKeyValuesRotate() {
        // idx 15
        String keyValuesRotateStr = productionInfoData.get(15).get(0);
        StringTokenizer st = new StringTokenizer(keyValuesRotateStr, " ");

        double[] keyValuesRotate = new double[st.countTokens()];
        int numTokens = st.countTokens();
        for (int i = 0; i < numTokens; i++) {
            keyValuesRotate[i] = Double.parseDouble(st.nextToken());
        }

        return keyValuesRotate;
    }

    public double[] getInitialRotation() {
        // idx 16
        String initialRotationStr = productionInfoData.get(16).get(0);
        StringTokenizer st = new StringTokenizer(initialRotationStr, " ");

        if (st.countTokens() != 3) {
            System.err.println("Incorrect number of values specified in"
                    + " ProductionInfo for initial rotation. Should be 3 i.e. x,y,z");
            double[] noRot = {0., 0., 0.};
            return noRot;
        }

        double[] initialRotation = new double[3];
        for (int i = 0; i < 3; i++) {
            initialRotation[i] = Double.parseDouble(st.nextToken());
        }

        return initialRotation;
    }

    public List<List<String>> getProductionInfoData() {
        return productionInfoData;
    }
}