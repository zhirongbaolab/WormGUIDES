/*
 * Bao Lab 2016
 */

/*
 * Bao Lab 2016
 */

package wormguides.models.cellcase;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Character.isDigit;

import static partslist.PartsList.getFunctionalNameByLineageName;

/**
 * The cell case in the info window which corresponds to a terminal or non-terminal cell. Subcases are specified by
 * {@link TerminalCellCase} and {@link NonTerminalCellCase}. This class defines the characteristics shared between
 * the two cell cases.
 */
public abstract class CellCase {

    protected static final String WORMATLAS_URL = "http://www.wormatlas.org/neurons/Individual%20Neurons/";

    private static final String WORMBASE_URL = "http://www.wormbase.org/db/get?name=";
    private static final String WORMBASE_EXT = ";class=Anatomy_term";

    private static final String GOOGLE_URL = "https://www.google.com/#q=";
    private static final String GOOGLE_WORMATLAS_URL = "https://www.google.com/#q=site:wormatlas.org+";

    private static final String TEXTPRESSO_URL = "http://textpresso-www.cacr.caltech"
            + ".edu/cgi-bin/celegans/search?searchstring=";
    private static final String TEXTPRESSO_URL_EXT = ";cat1=Select%20category%201%20from%20list%20above;"
            + "cat2=Select%20category%202%20from%20list%20above;cat3=Select%20category%203%20from%20list%20above;"
            + "cat4=Select%20category%204%20from%20list%20above;cat5=Select%20category%205%20from%20list%20above;"
            + "search=SearchLayer!;exactmatch=on;searchsynonyms=on;literature=C.%20elegans;target=abstract;target=body;"
            + "target=title;target=introduction;target=materials;target=results;target=discussion;target=conclusion;"
            + "target=acknowledgments;target=references;sentencerange=sentence;sort=score%20(hits);mode=boolean;"
            + "authorfilter=;journalfilter=;yearfilter=;docidfilter=;";
    private static final String TEXTPRESSO_TITLE = "Title: </span>";
    private static final String TEXTPRESSO_AUTHORS = "Authors: </span>";
    private static final String TEXTPRESSO_YEAR = "Year: </span>";

    private static final String ANCHOR_CLOSE = "</a>";
    private static final String HREF = "href=\"";

    private String lineageName;
    private List<String> geneExpression;
    private List<String> links;
    private List<String> references;
    private List<String> nuclearProductionInfo;
    private List<String> cellShapeProductionInfo;

    public CellCase(
            String lineageName,
            List<String> nuclearProductionInfo,
            List<String> cellShapeProductionInfo) {

        this.lineageName = lineageName;

        //initialize and populate links data structure
        buildLinks();

        //initialize and populate gene expression data structure
        setExpressionsFromWORMBASE();

        setReferences();

        this.nuclearProductionInfo = nuclearProductionInfo;
        this.cellShapeProductionInfo = cellShapeProductionInfo;

        // check if empty --> happens on run from AceTree or other non default embryo
        // add unknown label
        if (this.nuclearProductionInfo.size() == 0) {
            this.nuclearProductionInfo.add("Unknown - non-default embryo");
        }

        if (this.cellShapeProductionInfo.size() == 0) {
            this.cellShapeProductionInfo.add("Unknown - non-default embryo");
        }
    }

    /** Adds gene expressions from the wormbase page corresponding to this cell case to the class' 'list' variable */
    private void setExpressionsFromWORMBASE() {
        geneExpression = new ArrayList<>();

        final StringBuilder url = new StringBuilder();

        String functionalName = getFunctionalNameByLineageName(lineageName);
        if (functionalName != null) {
            //terminal cell case
            url.append(WORMBASE_URL).append(functionalName).append(WORMBASE_EXT);
        } else {
            //non terminal cell case
            url.append(WORMATLAS_URL).append(lineageName).append(WORMBASE_EXT);
        }
        final String urlString = url.toString();

        String content = "";
        URLConnection connection = null;

        try {
            connection = new URL(urlString).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
        } catch (Exception e) {
            //e.printStackTrace();
            //a page wasn't found on wormatlas
            System.out.println(this.lineageName + " page not found on Wormbase");
            return;
        }

        //add the link to the list before parsing with cytoshow snippet (first link is more human readable)
        links.add(urlString);

        // adapted from cytoshow
        String[] logLines = content.split("wname=\"associations\"");
        String restString = "";
        if (logLines != null && logLines.length > 1 && logLines[1].split("\"").length > 1) {
            restString = logLines[1].split("\"")[1];
        }

        try {
            connection = new URL("http://www.wormbase.org" + restString).openConnection();
            final Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();

        } catch (Exception e) {
            //a page wasn't found on wormatlas
            System.out.println(lineageName + " page not found on Wormbase (second URL)");

            //remove the link
            for (int i = 0; i < links.size(); i++) {
                if (links.get(i).startsWith(WORMBASE_URL)) {
                    links.remove(i);
                }
            }
            return;
        }

        //extract expressions
        String[] genes = content.split("><");
        for (String gene : genes) {
            if (gene.startsWith("span class=\"locus\"")) {
                gene = gene.substring(gene.indexOf(">") + 1, gene.indexOf("<"));
                geneExpression.add(gene);
            }
        }
    }

    private void buildLinks() {
        links = new ArrayList<>();
        links.add(addGoogleLink());
        links.add(addGoogleWormatlasLink());
    }

    private String addGoogleLink() {
        if (lineageName != null) {
            String cell = lineageName;
            final String functionalName = getFunctionalNameByLineageName(lineageName);
            if (functionalName != null) {
                cell = functionalName; //terminal cell case
            }
            return GOOGLE_URL + cell + "+c.+elegans";
        }
        return "";
    }

    private String addGoogleWormatlasLink() {
        if (lineageName != null) {
            String cell = lineageName;
            final String functionalName = getFunctionalNameByLineageName(lineageName);
            if (functionalName != null) {
                cell = functionalName; //terminal cell case
            }
            return GOOGLE_WORMATLAS_URL + cell;
        }
        return "";
    }

    /**
     * Adds a link to the class' 'links'
     *
     * @param link
     *         the link to add
     */
    protected void addLink(String link) {
        links.add(link);
    }

    /**
     * Finds the number of matches and documents for this cell on texpresso, and the first page of results. The
     * 'links' and 'references' variables are modified.
     */
    private void setReferences() {
        references = new ArrayList<>();

        final StringBuilder url = new StringBuilder();

        String functionalName = getFunctionalNameByLineageName(lineageName);
        if (functionalName != null) {
            //terminal cell case
            url.append(TEXTPRESSO_URL).append(functionalName).append(TEXTPRESSO_URL_EXT);
        } else {
            //non-terminal cell case
            url.append(TEXTPRESSO_URL).append(lineageName).append(TEXTPRESSO_URL_EXT);
        }
        final String urlString = url.toString();

        String content = "";
        try {
            URLConnection connection = new URL(urlString).openConnection();
            final Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();

        } catch (Exception e) {
            //a page wasn't found on wormatlas
            System.out.println(lineageName + " page not found on Textpresso");
            return;
        }

        int matchesIDX = content.indexOf(" matches found in </span><span style=\"font-weight:bold;\">");
        if (matchesIDX > 0) {
            matchesIDX--; //move back to the first digit
            //find the start of the number of matches
            String matchesStr = "";
            char matchesChar = content.charAt(matchesIDX);
            while (isDigit(matchesChar)) {
                matchesStr += matchesChar;
                matchesChar = content.charAt(--matchesIDX);
            }
            //reverse the string
            matchesStr = new StringBuffer(matchesStr).reverse().toString();

            //find the number of documents
            int documentsIDX = content.indexOf(" matches found in </span><span style=\"font-weight:bold;\">") + 57;
            String documentsStr = "";
            char documentChar = content.charAt(documentsIDX);
            while (isDigit(documentChar)) {
                documentsStr += documentChar;
                documentChar = content.charAt(++documentsIDX);
            }

            //add matches and documents to top of references list
            references.add("<em>Textpresso</em>: " + matchesStr + " matches found in " + documentsStr + " documents");
            // TODO add textpresso url to page with open in browser

            //parse the document for "Title: "
            int lastIDX = 0;
            while (lastIDX != -1) {
                lastIDX = content.indexOf(TEXTPRESSO_TITLE, lastIDX);

                if (lastIDX != -1) {
                    lastIDX += TEXTPRESSO_TITLE.length(); //skip the title just seen

                    //extract the title
                    String title = content.substring(lastIDX, content.indexOf("<br />", lastIDX));

                    //move the index past the authors section
                    while (!content.substring(lastIDX).startsWith(TEXTPRESSO_AUTHORS)) {
                        lastIDX++;
                    }

                    lastIDX += TEXTPRESSO_AUTHORS.length();

                    //extract the authors
                    String authors = content.substring(lastIDX, content.indexOf("<br />", lastIDX));

                    //move the index past the year section
                    while (!content.substring(lastIDX).startsWith(TEXTPRESSO_YEAR)) {
                        lastIDX++;
                    }

                    lastIDX += TEXTPRESSO_YEAR.length();

                    //extract the year
                    String year = content.substring(lastIDX, content.indexOf("<br />", lastIDX));

                    String reference = title + authors + ", " + year;

                    //update anchors
                    reference = updateAnchors(reference);

                    references.add(reference);
                }
            }
        }

        //add the source
        references.add(new StringBuilder()
                .append("<em>Source:</em> <a href=\"#\" name=\"")
                .append(url)
                .append("\" onclick=\"handleLink(this)\">")
                .append(url)
                .append("</a>")
                .toString());
        links.add(urlString);
    }

    protected String updateAnchors(String content) {
        /*
         * find the anchor tags and change to:
		 *  "<a href=\"#\" name=\"" + link + "\" onclick=\"handleLink(this)\">"
		 *  paradigm
		 */
        String findStr = "<a ";
        int lastIdx = 0;

        while (lastIdx != -1) {
            lastIdx = content.indexOf(findStr, lastIdx);

            //check if another anchor found
            if (lastIdx != -1) {
                //save the string preceding the anchor
                String precedingStr = content.substring(0, lastIdx);

                //find the end of the anchor and extract the anchor
                int anchorEndIdx = content.indexOf(ANCHOR_CLOSE, lastIdx);
                String anchor = content.substring(lastIdx, anchorEndIdx + ANCHOR_CLOSE.length());

                //extract the source href --> "href=\""
                boolean isLink = true;
                int startSrcIdx = anchor.indexOf(HREF) + HREF.length();

                String src = "";
                //make sure not a citation i.e. first character is '#'
                if (anchor.charAt(startSrcIdx) == '#') {
                    isLink = false;
                } else {
                    src = anchor.substring(startSrcIdx, anchor.indexOf("\"", startSrcIdx));
                }

                if (isLink) {
                    //check if relative src
                    if (!src.contains("www.") && !src.contains("http")) {
                        //remove path
                        if (src.contains("..")) {
                            src = src.substring(src.lastIndexOf("/") + 1);
                        }
                        src = WORMATLAS_URL + src;
                    }

                    //extract the anchor text --> skip over the first <
                    String text = anchor.substring(anchor.indexOf(">") + 1, anchor.substring(1).indexOf("<") + 1);

                    // build new anchor
                    String newAnchor = new StringBuilder()
                            .append("<a href=\"#\" name=\"")
                            .append(src)
                            .append("\" onclick=\"handleLink(this)\">")
                            .append(text)
                            .append("</a>")
                            .toString();

                    //replace previous anchor
                    content = precedingStr + newAnchor + content.substring(anchorEndIdx + ANCHOR_CLOSE.length());
                } else {
                    //remove anchor
                    String txt = anchor.substring(anchor.indexOf(">") + 1, anchor.substring(1).indexOf("<") + 1);
                    content = precedingStr + txt + content.substring(anchorEndIdx + ANCHOR_CLOSE.length());
                }

                //move lastIdx past just processed anchor
                lastIdx += findStr.length();
            }
        }
        return content;
    }

    public String getLineageName() {
        return this.lineageName;
    }

    public List<String> getExpressesWORMBASE() {
        return geneExpression;
    }

    public List<String> getReferences() {
        return references;
    }

    public List<String> getLinks() {
        return this.links;
    }

    public List<String> getNuclearProductionInfo() {
        return this.nuclearProductionInfo;
    }

    public List<String> getCellShapeProductionInfo() {
        return this.cellShapeProductionInfo;
    }
}