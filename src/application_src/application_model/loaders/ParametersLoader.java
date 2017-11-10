/*
 * Bao Lab 2017
 */

package application_src.application_model.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import application_src.MainApp;

public class ParametersLoader {

	public static final String PARAMETERS_FILE_PATH = "/application_src/application_model/threeD/subsceneparameters/parameters.txt";

	public static Map<String, String> loadParameters() {
		final URL url = MainApp.class.getResource(PARAMETERS_FILE_PATH);
		final Map<String, String> param_map = new HashMap<>();

		try (final InputStreamReader isr = new InputStreamReader(url.openStream());
	             final BufferedReader br = new BufferedReader(isr)) {

			String line;
			while((line = br.readLine()) != null) {
				final StringTokenizer st = new StringTokenizer(line, " ");

				if (st.countTokens() == 2) {
					param_map.put(st.nextToken(), st.nextToken());
				}
			}

		} catch (IOException e) {
            System.out.println("The parameters file "
                    + PARAMETERS_FILE_PATH
                    + " wasn't found on the system.");
        }

		return param_map;
	}
}