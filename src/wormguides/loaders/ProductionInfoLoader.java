/*
 * Bao Lab 2016
 */

package wormguides.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import wormguides.MainApp;

/**
 * Syntax rules for config file:
 * <p>
 * Replace ',' with ';' to support StringTokenizer with ',' delimeter (.csv file)
 */
public class ProductionInfoLoader {

    private static final int NUMBER_OF_FIELDS = 17;

    private static final String PRODUCTION_INFO_FILE_PATH = "/wormguides/model/production_info_file/"
            + "Production_Info.csv";

    private static final String PRODUCT_INFO_LINE = "Production Information,,,,,,,,,,,,,,,,";

    private static final String HEADER_LINE = "Cells,Image Series,Marker,Strain,Compressed Embryo?,Temporal "
            + "Resolution,Segmentation,cytoshow link,Movie start timeProperty (min),isSulstonMode?,Total Time Points,"
            + "X_SCALE,Y_SCALE,Z_SCALE,Key_Frames_Rotate,Key_Values_Rotate,Initial_Rotation";

    /**
     * Tokenizes each line in the config file and creates a 2D array of the file
     *
     * @return the 2D array
     */
    public static List<List<String>> buildProductionInfo() {
        final URL url = MainApp.class.getResource("/wormguides/models/production_info_file/Production_Info.csv");

        final List<List<String>> productionInfo = new ArrayList<>();
        final List<String> cells = new ArrayList<>();
        final List<String> imageSeries = new ArrayList<>();
        final List<String> markers = new ArrayList<>();
        final List<String> strains = new ArrayList<>();
        final List<String> compressedEmbryo = new ArrayList<>();
        final List<String> temporalResolutions = new ArrayList<>();
        final List<String> segmentations = new ArrayList<>();
        final List<String> cytoshowLinks = new ArrayList<>();
        final List<String> movieStartTime = new ArrayList<>();
        final List<String> isSulston = new ArrayList<>();
        final List<String> totalTimePoints = new ArrayList<>();
        final List<String> xScale = new ArrayList<>();
        final List<String> yScale = new ArrayList<>();
        final List<String> zScale = new ArrayList<>();
        final List<String> keyFramesRotate = new ArrayList<>();
        final List<String> keyValuesRotate = new ArrayList<>();
        final List<String> initialRotation = new ArrayList<>();

        try (final InputStreamReader streamReader = new InputStreamReader(url.openStream());
             final BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // skip product info line and header line
                if (line.equals(PRODUCT_INFO_LINE)) {
                    line = reader.readLine();
                    if (line.equals(HEADER_LINE)) {
                        line = reader.readLine();
                    }
                }
                // make sure valid line
                if (line.length() <= 1) {
                    break;
                }

                final StringTokenizer tokenizer = new StringTokenizer(line, ",");
                // check if valid line
                if (tokenizer.countTokens() == NUMBER_OF_FIELDS) {
                    cells.add(tokenizer.nextToken());
                    imageSeries.add(tokenizer.nextToken());
                    markers.add(tokenizer.nextToken());
                    strains.add(tokenizer.nextToken());
                    compressedEmbryo.add(tokenizer.nextToken());
                    temporalResolutions.add(tokenizer.nextToken());
                    segmentations.add(tokenizer.nextToken());
                    cytoshowLinks.add(tokenizer.nextToken());
                    movieStartTime.add(tokenizer.nextToken());
                    isSulston.add(tokenizer.nextToken());
                    totalTimePoints.add(tokenizer.nextToken());
                    xScale.add(tokenizer.nextToken());
                    yScale.add(tokenizer.nextToken());
                    zScale.add(tokenizer.nextToken());
                    keyFramesRotate.add(tokenizer.nextToken());
                    keyValuesRotate.add(tokenizer.nextToken());
                    initialRotation.add(tokenizer.nextToken());
                }
            }

            // add lists to production info
            productionInfo.add(cells);
            productionInfo.add(imageSeries);
            productionInfo.add(markers);
            productionInfo.add(strains);
            productionInfo.add(compressedEmbryo);
            productionInfo.add(temporalResolutions);
            productionInfo.add(segmentations);
            productionInfo.add(cytoshowLinks);
            productionInfo.add(movieStartTime);
            productionInfo.add(isSulston);
            productionInfo.add(totalTimePoints);
            productionInfo.add(xScale);
            productionInfo.add(yScale);
            productionInfo.add(zScale);
            productionInfo.add(keyFramesRotate);
            productionInfo.add(keyValuesRotate);
            productionInfo.add(initialRotation);
        } catch (IOException e) {
            System.out.println("The production info file "
                    + PRODUCTION_INFO_FILE_PATH
                    + " wasn't found on the system.");
        }
        return productionInfo;
    }
}