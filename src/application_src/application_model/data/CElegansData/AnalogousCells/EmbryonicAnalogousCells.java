/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.AnalogousCells;

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
    }

    public static void init() {
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

    public static List<EmbryonicHomology> getHomologues() { return homologues; }
}
