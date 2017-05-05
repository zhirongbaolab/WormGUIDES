/*
 * Bao Lab 2017
 */

package search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

import static search.SearchUtil.isLineageName;

/**
 * Utility that queries the WormBase http://www.wormbase.org.
 */
public class WormBaseQuery {

    /** The WormBase URL */
    private static final String WORMBASE_URL = "http://www.wormbase.org";

    /** Time to wait, in millis, for WormBase to respond */
    private static final int CONNECTION_TIMEOUT_MILLIS = 15000;

    /** The HTTP protocol issued to WormBase */
    private static final String GET_PROTOCOL = "GET";

    /**
     * Issues a query to WormBase with the searched gene
     *
     * @param searchedGene
     *         the searched term, non null, non-empty, and in the gene format SOME_STRING-SOME_NUMBER
     *
     * @return cells with that gene
     */
    public static List<String> issueWormBaseQuery(String searchedGene) {
        final List<String> results = new ArrayList<>();

        if (!requireNonNull(searchedGene).isEmpty()) {
            searchedGene = searchedGene.trim().toLowerCase();
            // only processUrl the first searched term
            final String[] tokens = searchedGene.split(" ");
            if (tokens.length != 0) {
                searchedGene = tokens[0];
            }

            // do actual search if result was not cached
            try (final BufferedReader pageStream = openUrl("/db/get?name=" + searchedGene + ";class=gene")) {
                if (pageStream != null) {
                    String restString = "";
                    String firstQueryLine;
                    while ((firstQueryLine = pageStream.readLine()) != null && restString.isEmpty()) {
                        if (firstQueryLine.contains("wname=\"expression\"")) {
                            final String[] restChunks = pageStream.readLine().split("\"");
                            restString = restChunks[1];
                        }
                    }

                    try (final BufferedReader restPageStream = openUrl(restString)) {
                        if (restPageStream != null) {
                            String wbGeneLine;

                            while ((wbGeneLine = restPageStream.readLine()) != null) {
                                final Matcher m = compile("class=\"anatomy_term-link\" title=\"\">(\\S+)</a>")
                                        .matcher(wbGeneLine);
                                while (m.find()) {
                                    final String name = m.group(1);
                                    if (isLineageName(name)) {
                                        results.add(name);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sort(results);
        return new ArrayList<>(new HashSet<>(results));
    }

    /**
     * Eastablishes a connection to the WormBase and returns a reader for the results
     *
     * @param target
     *         the wormbase target link without the http://www.wormbase.org prefix
     *
     * @return reader for the results fetched, null if there was an error in reaching WormBase
     */
    private static BufferedReader openUrl(String target) {
        target = WORMBASE_URL + target;
        final HttpURLConnection connection;
        try {
            final URL url = new URL(target);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
            connection.setRequestMethod(GET_PROTOCOL);

            final InputStream is = connection.getInputStream();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            return rd;

        } catch (SocketTimeoutException ste) {
            System.out.println("HTTP connection timed out for " + target);
        } catch (MalformedURLException mue) {
            System.out.println("Invalid URL " + target);
        } catch (ProtocolException pe) {
            System.out.println("Invalid protocol " + GET_PROTOCOL);
        } catch (IOException ioe) {
            System.out.println("Error in connecting to " + target);
        }
        return null;
    }
}