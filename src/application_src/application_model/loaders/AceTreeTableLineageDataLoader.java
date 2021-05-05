/*
 * Bao Lab 2017
 */

package application_src.application_model.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import application_src.application_model.data.LineageData;
import application_src.MainApp;
import application_src.application_model.resources.ProductionInfo;
import application_src.application_model.data.TableLineageData;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Loader that reads the nuclei files located in the same package and creates a {@link LineageData} from the data.
 * This class instantiates a {@link TableLineageData} and creates a lineage using Frame objects defined as an private
 * inner class.
 */
public class AceTreeTableLineageDataLoader {

    private static String CONFIG_FILE_PATH = "/atlas_model/configurations/nuclei_config/";
    private static String CONFIG_FILE_NAME = "NucleiConfig.csv";
    private static String HEADER_LINE = "Resource Location";

    private static String ENTRY_PREFIX;

    private static final String T = "t";

    private static final String ENTRY_EXT = "-nuclei";

    private static final int NUMBER_OF_TOKENS = 21;

    private static final int VALID_INDEX = 1,
            XCOR_INDEX = 5,
            YCOR_INDEX = 6,
            ZCOR_INDEX = 7,
            DIAMETER_INDEX = 8,
            ID_INDEX = 9,
            RWT = 11,
            RWRAW = 15,
            RWCORR1 = 16,
            RWCORR2 = 17,
            RWCORR3 = 18,
            RWCORR4 = 19;

    private static final String ONE_ZERO_PAD = "0";

    private static final String TWO_ZERO_PAD = "00";

    /** Index of the x-coordinate in the position array for a nucleus in a time frame */
    private static final int X_POS_INDEX = 0;

    /** Index of the y-coordinate in the position array for a nucleus in a time frame */
    private static final int Y_POS_INDEX = 1;

    /** Index of the z-coordinate in the position array for a nucleus in a time frame */
    private static final int Z_POS_INDEX = 2;

    private static final List<String> allCellNames = new ArrayList<>();

    private static double avgX;
    private static double avgY;
    private static double avgZ;

    public static LineageData loadNucFiles(final ProductionInfo productionInfo) {
        final TableLineageData tableLineageData = new TableLineageData(
                allCellNames,
                productionInfo.getXScale(),
                productionInfo.getYScale(),
                productionInfo.getZScale());

        // first find the ENTRY_PREFIX
        final URL config_url = MainApp.class.getResource(CONFIG_FILE_PATH + CONFIG_FILE_NAME);
        try (final InputStreamReader streamReader = new InputStreamReader(config_url.openStream());
             final BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // skip header line
                if (line.equals(HEADER_LINE)) {
                        line = reader.readLine();

                }
                // make sure valid line
                if (line.length() <= 1) {
                    break;
                }

                // save the entry prefix, removing the appended comma
                ENTRY_PREFIX = line.substring(0, line.length());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // accounts for first tld.addFrame() added when reading from JAR --> from dir name first entry match
            tableLineageData.addTimeFrame();
            URL url;
            String urlString;
            for (int i = 1; i <= productionInfo.getTotalTimePoints(); i++) {
                urlString = getResourceAtTime(i);
                if (urlString != null) {
                    url = MainApp.class.getResource(urlString);
                    if (url != null) {
                        process(tableLineageData, i, url.openStream());
                    } else {
                        System.out.println("Could not find file: "
                                + urlString);
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // translate all cells to center around (0,0,0)
        setOriginToZero(tableLineageData, true);
        return tableLineageData;
    }

    /**
     * @param i
     *         the time of the nuc file
     *
     * @return the URL for the resource of that nuc file
     */
    private static String getResourceAtTime(final int i) {
        String resourceUrlString = null;
        if (i >= 1) {
            if (i < 10) {
                resourceUrlString = ENTRY_PREFIX
                        + T
                        + TWO_ZERO_PAD
                        + i
                        + ENTRY_EXT;
            } else if (i < 100) {
                resourceUrlString = ENTRY_PREFIX
                        + T
                        + ONE_ZERO_PAD
                        + i
                        + ENTRY_EXT;
            } else {
                resourceUrlString = ENTRY_PREFIX
                        + T
                        + i
                        + ENTRY_EXT;
            }
        }
        return resourceUrlString;
    }

    public static double getAvgXOffsetFromZero() {
        return avgX;
    }

    public static double getAvgYOffsetFromZero() {
        return avgY;
    }

    public static double getAvgZOffsetFromZero() {
        return avgZ;
    }

    public static void setOriginToZero(final LineageData lineageData, final boolean defaultEmbryoFlag) {
        int totalPositions = 0;
        double sumX = 0d;
        double sumY = 0d;
        double sumZ = 0d;

        // sum up all x-, y- and z-coordinates of nuclei - we start at 1 because an empty
        // is appended to the front of lineagedata
        for (int i = 1; i < lineageData.getNumberOfTimePoints(); i++) {
            double[][] positionsArray = lineageData.getPositions(i);
            for (int j = 0; j < positionsArray.length; j++) {
//                System.out.println(positionsArray[j][X_POS_INDEX] + ", " + positionsArray[j][Y_POS_INDEX] + ", " + positionsArray[j][Z_POS_INDEX]);
                sumX += positionsArray[j][X_POS_INDEX];
                sumY += positionsArray[j][Y_POS_INDEX];
                sumZ += positionsArray[j][Z_POS_INDEX];
                totalPositions++;
            }
        }

        // find average of x-, y- and z-coordinates
        if (totalPositions != 0) {
            avgX = sumX / totalPositions;
            avgY = sumY / totalPositions;
            avgZ = sumZ / totalPositions;
        } else {
            avgX = avgY = avgZ = 0.;
        }


        System.out.println("Average nuclei position offsets from origin (0, 0, 0): ("
                + avgX
                + ", "
                + avgY
                + ", "
                + avgZ
                + ")");

        // offset all nuclei x-, y- and z- positions by x, y and z averages
        lineageData.shiftAllPositions(avgX, avgY, avgZ);
    }

    private static void process(final TableLineageData tableLineageData, final int time, final InputStream input) {
        tableLineageData.addTimeFrame();

        try (InputStreamReader isr = new InputStreamReader(input);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = new String[NUMBER_OF_TOKENS];
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                int k = 0;
                while (tokenizer.hasMoreTokens()) {
                    tokens[k++] = tokenizer.nextToken().trim();
                }

                if (parseInt(tokens[VALID_INDEX]) == 1) {
                    makeNucleus(tableLineageData, time, tokens);
                }
            }
        } catch (IOException e) {
            System.out.println("Error in processing input stream");
        }
    }

    private static void makeNucleus(final TableLineageData tableLineageData, final int time, final String[] tokens) {
        try {
            tableLineageData.addNucleus(
                    time,
                    tokens[ID_INDEX],
                    parseDouble(tokens[XCOR_INDEX]),
                    parseDouble(tokens[YCOR_INDEX]),
                    parseDouble(tokens[ZCOR_INDEX]),
                    parseDouble(tokens[DIAMETER_INDEX]),
                    computeRWeight(tokens, "blot")); //express computation method default as bolt for now
        } catch (NumberFormatException nfe) {
            System.out.println("Incorrect format in nucleus file for time " + time + ".");
        }
    }

    private static int computeRWeight(final String[] tokens, String method) {
        if (Integer.parseInt(tokens[RWRAW]) <= 0) return Integer.parseInt(tokens[RWT]);
        int rweight = Integer.parseInt(tokens[RWRAW]);
        switch(method) {
            case "global":
                rweight -= Integer.parseInt(tokens[RWCORR1]);
                break;
            case "local":
                rweight -= Integer.parseInt(tokens[RWCORR2]);
                break;
            case "blot":
                rweight -= Integer.parseInt(tokens[RWCORR3]);
                break;
            case "cross":
                rweight -= Integer.parseInt(tokens[RWCORR4]);
                break;
        }
        return rweight;
    }
}