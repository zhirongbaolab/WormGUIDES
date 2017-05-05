/*
 * Bao Lab 2016
 */

package wormguides.models.cellcase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which holds the database of embryonic analogous cells as defined in model/analogous_cell_file/
 */
public class EmbryonicAnalogousCells {

    private static List<EmbryonicHomology> homologues;

    static {
        homologues = new ArrayList<>();

        final URL url = EmbryonicAnalogousCells.class.getResource("analogous_cell_file/EmbryonicAnalogousCells.csv");

        if (url != null) {
            try (final InputStreamReader isr = new InputStreamReader(url.openStream());
                 final BufferedReader br = new BufferedReader(isr)){
                String line;
                String[] cells;
                while ((line = br.readLine()) != null) {
                    cells = line.split(",");
                    if (cells.length == 2
                            && cells[0].length() > 0
                            && cells[1].length() > 0) {
                        homologues.add(new EmbryonicHomology(cells[0], cells[1]));
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Finds a match in the database given a query cell Case 1: matches a homologous listing Case 2: descendant of a
     * listed homology
     *
     * @param cell
     *         the query cell
     *
     * @return the match
     */
    public static String findEmbryonicHomology(String cell) {
        for (EmbryonicHomology eh : homologues) {
            if (cell.startsWith(eh.getCell1())) {

                // check if case 1 i.e. complete match
                if (cell.equals(eh.getCell1())) {
                    return eh.getCell2();
                }

                // otherwise, case 1 i.e. descendant --> add suffix
                final String suffix = cell.substring(eh.getCell2().length());
                // list upstream parallel
                return new StringBuilder()
                        .append(eh.getCell2())
                        .append(suffix)
                        .append(" (")
                        .append(eh.getCell1())
                        .append(": ")
                        .append(eh.getCell2())
                        .append(")")
                        .toString();
            }

            if (cell.startsWith(eh.getCell2())) {
                // check if case 1 i.e. complete match
                if (cell.equals(eh.getCell2())) {
                    return eh.getCell1();
                }

                // otherwise, case 1 i.e. descendant --> add suffix
                final String suffix = cell.substring(eh.getCell1().length());
                // list upstream parallel
                return new StringBuilder()
                        .append(eh.getCell1())
                        .append(suffix)
                        .append(" (")
                        .append(eh.getCell2())
                        .append(": ")
                        .append(eh.getCell1())
                        .append(")")
                        .toString();
            }
        }
        return "N/A";
    }
}
