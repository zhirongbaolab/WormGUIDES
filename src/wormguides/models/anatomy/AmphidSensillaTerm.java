/*
 * Bao Lab 2016
 */

package wormguides.models.anatomy;

import java.util.ArrayList;
import java.util.List;

import partslist.PartsList;

public class AmphidSensillaTerm extends AnatomyTermCase {

    private final static String AMPHID = "amphid";
    private final static String AMPHID_WORMATLAS_LINK_1 = "http://www.wormatlas.org/ver1/handbook/hypodermis/"
            + "Amphidimagegallery.htm";
    private final static String AMPHID_WORMATLAS_LINK_2 = "http://wormatlas.org/hermaphrodite/neuronalsupport/jump"
            + ".html?newLink=mainframe.htm&newAnchor=Amphidsensilla31";

    private final List<String> links;
    private final List<String> amphidCells;
    private final String wormatlasLink1;
    private final String wormatlasLink2;

    public AmphidSensillaTerm(final AnatomyTerm term) {
        super(term);
        links = buildSearchBasedLinks();
        amphidCells = findAmphidCells();
        wormatlasLink1 = AMPHID_WORMATLAS_LINK_1;
        wormatlasLink2 = AMPHID_WORMATLAS_LINK_2;
    }

    /**
     * Finds cells in the parts list whose descriptions contain 'Amphid'
     *
     * @return the cells with 'Amphid' hits
     */
    public List<String> findAmphidCells() {
        final List<String> cells = new ArrayList<>();
        final List<String> functionalNames = PartsList.getFunctionalNames();
        final List<String> lineageNames = PartsList.getLineageNames();
        final List<String> descriptions = PartsList.getDescriptions();
        for (int i = 0; i < descriptions.size(); i++) {
            if (descriptions.get(i).toLowerCase().contains(AMPHID)) {
                String cell = "";
                if (functionalNames.get(i) != null) {
                    cell += (functionalNames.get(i) + "*");
                }
                if (lineageNames.get(i) != null) {
                    cell += (lineageNames.get(i) + "*");
                }
                cell += descriptions.get(i);
                cells.add(cell);
            }
        }
        return cells;
    }

    /**
     * Sets up the wormbase, google search and textpresso links
     *
     * @return list of search links
     */
    public List<String> buildSearchBasedLinks() {
        final List<String> searchBasedLinks = new ArrayList<>();
        //add wormbase link
        searchBasedLinks.add("http://www.wormbase.org/species/all/anatomy_term/WBbt:0005391#01--10");
        //add google links
        searchBasedLinks.add("https://www.google.com/#q=amphid+sensillia");
        searchBasedLinks.add("https://www.google.com/#q=site:wormatlas.org+amphid+sensillia");
        //add textpresso link
        searchBasedLinks.add("http://textpresso-www.cacr.caltech.edu/cgi-bin/celegans/search?searchstring=amphid"
                + "+sensillia;cat1=Select%20category"
                + "%201%20from%20list%20above;cat2=Select%20category%202%20from%20list%20above;"
                + "cat3=Select%20category%203%20from%20list%20above;cat4=Select%20category%204%"
                + "20from%20list%20above;cat5=Select%20category%205%20from%20list%20above;search=SearchLayer!;"
                + "exactmatch=on;searchsynonyms=on;literature=C.%20elegans;target=abstract;"
                + "target=body;target=title;target=introduction;target=materials;target=results;"
                + "target=discussion;target=conclusion;"
                + "target=acknowledgments;target=references;sentencerange=sentence;sort=score%20(hits);"
                + "mode=boolean;authorfilter=;journalfilter=;yearfilter=;docidfilter=;");
        return searchBasedLinks;
    }

    public List<String> getAmphidCells() {
        return amphidCells;
    }

    public String getWormatlasLink1() {
        return wormatlasLink1;
    }

    public String getWormatlasLink2() {
        return wormatlasLink2;
    }

    public List<String> getLinks() {
        return links;
    }

}
