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
import static java.lang.Character.toLowerCase;

import static partslist.PartsList.getDescriptionByLineageName;
import static partslist.PartsList.getFunctionalNameByLineageName;
import static partslist.PartsList.getLineageNames;
import static wormguides.models.anatomy.Anatomy.getAnatomy;
import static wormguides.models.anatomy.Anatomy.hasAnatomy;

/**
 * Cell case for a terminal cell. The case contains information for the info window
 */
public class TerminalCellCase extends CellCase {

    private static final String GRAPHIC_URL = "http://www.wormatlas.org/neurons/Images/";
    private static final String GRAPHIC_URL_RANGE = "http://www.wormatlas.org/neurons/Images/";

    private static final String JPG_EXT = ".jpg";

    private static final String WORMATLAS_URL_EXT = "mainframe.htm";
    private static final String WORMWIRING_BASE_URL = "http://wormwiring.hpc.einstein.yu.edu/data/neuronData.php?name=";
    private static final String WORMWIRING_N_2_UEXT = "&db=N2U";

    private String functionalName;
    private String externalInfo;
    private String partsListDescription;
    private String imageURL;
    private String functionWORMATLAS;

    private List<String> presynapticPartners;
    private List<String> postsynapticPartners;
    private List<String> electricalPartners;
    private List<String> neuromuscularPartners;

    private boolean hasAnatomy;
    private List<String> anatomies;

    /** homologues[0] will contain L/R homologues, homologues[1] will contain additional symmetries */
    private List<List<String>> homologues;

    /**
     * @param lineageName
     *         lineage name of the cell
     * @param functionalName
     *         functional name of the cell
     * @param presynapticPartners
     *         list of presynaptic partners
     * @param postsynapticPartners
     *         list of postsynaptic partners
     * @param electricalPartners
     *         list of electrical partners
     * @param neuromuscularPartners
     *         list of neuromuscular partners
     * @param nuclearProductionInfo
     *         information from the production file under Nuclear
     * @param cellShapeProductionInfo
     *         information from the production file under Cell Shape
     */
    public TerminalCellCase(
            String lineageName,
            String functionalName,
            List<String> presynapticPartners,
            List<String> postsynapticPartners,
            List<String> electricalPartners,
            List<String> neuromuscularPartners,
            List<String> nuclearProductionInfo,
            List<String> cellShapeProductionInfo) {

        super(lineageName, nuclearProductionInfo, cellShapeProductionInfo);

        this.functionalName = functionalName;
        this.externalInfo = this.functionalName + " (" + lineageName + ")";

        this.partsListDescription = getDescriptionByLineageName(lineageName);

        if (isDigit(functionalName.charAt(functionalName.length() - 1))) {
            this.imageURL = GRAPHIC_URL + functionalName.toUpperCase() + JPG_EXT;
        } else {
            this.imageURL = GRAPHIC_URL + functionalName.toLowerCase() + JPG_EXT;
        }

        //parse wormatlas for the "Function" field, also set image field
        this.functionWORMATLAS = setFunctionFromWORMATLAS();

        //set the wiring partners from connectome
        this.presynapticPartners = presynapticPartners;
        this.postsynapticPartners = postsynapticPartners;
        this.electricalPartners = electricalPartners;
        this.neuromuscularPartners = neuromuscularPartners;

        this.hasAnatomy = hasAnatomy(this.functionalName);
        if (hasAnatomy) {
            setAnatomy();
        }

        this.homologues = setHomologues();

        //generate and add the wormwiring link
        addLink(addWormWiringLink());

        // TODO cytoshow stub
        //links.add("Cytoshow: [cytoshow link to this cell in EM data]");
    }

    /**
     * Finds the wormatlas page corresponding to this cell and parses its html for the 'Function' section, which it
     * then
     * pulls
     *
     * @return the "Function" section of html from wormatlas.org
     */
    private String setFunctionFromWORMATLAS() {
        if (functionalName == null) {
            return "";
        }

        String content = "";
        URLConnection connection = null;

		/*
         * USING mainframe.htm EXT
		 * Leaving code for frameset.htm check
		 */

		/*
         * if R/L cell, find base name for URL
		 * e.g. ribr --> RIB
		 *
		 * if no R/L, leave as is
		 * e.g. AVG
		 */
        String cell = this.functionalName;
        Character lastChar = cell.charAt(cell.length() - 1);
        lastChar = toLowerCase(lastChar);
        if (lastChar == 'r' || lastChar == 'l') {
            cell = cell.substring(0, cell.length() - 1);

            //check if preceding d/v
            lastChar = cell.charAt(cell.length() - 1);
            lastChar = toLowerCase(lastChar);
            if (lastChar == 'd' || lastChar == 'v') {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (lastChar == 'd' || lastChar == 'v') { //will l/r ever come before d/v
            cell = cell.substring(0, cell.length() - 1);

            //check if preceding l/r
            lastChar = cell.charAt(cell.length() - 1);
            lastChar = toLowerCase(lastChar);
            if (lastChar == 'l' || lastChar == 'r') {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (isDigit(lastChar)) {
            cell = cell.substring(0, cell.length() - 1).toUpperCase() + "N";
        }

        String URL = WORMATLAS_URL + cell.toUpperCase() + WORMATLAS_URL_EXT;
        try {
            connection = new URL(URL).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
        } catch (Exception e) {
            //a page wasn't found on wormatlas
            return null;
        }

        //find the image src in the html and set the imageURL
        findImageURLInHTML(content);
        return findFunctionInHTML(content, URL);
    }

    private void findImageURLInHTML(String content) {
        //find all instances of '/Images/
        List<String> images = new ArrayList<>();
        String quotes = "\"";
        String findStr = "/Images/";
        int lastIdx = 0;
        int closeQuoteIdx = 0;

        while (lastIdx != -1) {
            lastIdx = content.indexOf(findStr, lastIdx);

            if (lastIdx != -1) {
                //find the index of the closing quotes for the image url
                closeQuoteIdx = content.indexOf(quotes, lastIdx);

                //add the url to the list
                images.add(content.substring(lastIdx, closeQuoteIdx));

                //move lastIdx past just processed image url
                lastIdx += findStr.length();
            }
        }

        //look for matches, first check if functionalName ends with number
        boolean cellWithNum = isDigit(functionalName.charAt(functionalName.length() - 1));
        for (String url : images) {
            String imageName = url.substring(findStr.length(), url.indexOf("."));

            if (imageName.toLowerCase().equals(functionalName.toLowerCase())) {
                if (cellWithNum) {
                    imageURL = GRAPHIC_URL + functionalName.toUpperCase() + JPG_EXT;
                    return;
                } else {
                    imageURL = GRAPHIC_URL + functionalName.toLowerCase() + JPG_EXT;
                    return;
                }

            }

            //if functionalName ends with number, check if the image has a range of numbers e.g. DA3-7, or if two
            // consecutive images form a range
            if (cellWithNum) {
                //find the base name of the cell
                String baseName = "";
                for (int i = 0; i < functionalName.length(); i++) {
                    if (!isDigit(functionalName.charAt(i))) {
                        baseName += functionalName.charAt(i);
                    }
                }

                //extract the number for this cell
                int num = Character.getNumericValue(functionalName.charAt(functionalName.length() - 1));

                //check if the image has a range
                if (imageName.contains("-")) {
                    int upperBound = Integer.parseInt(imageName.substring(imageName.indexOf("-") + 1));
                    int lowerBound = Character.getNumericValue(imageName.charAt(imageName.indexOf("-") - 1));

                    //check if the base name matches the image url and falls within the range
                    if (imageName.toLowerCase().startsWith(baseName.toLowerCase())) {

                        if (num >= lowerBound && num <= upperBound) {
                            imageURL = GRAPHIC_URL_RANGE + imageName + JPG_EXT;
                            return;
                        }
                    }
                }

                //check if the two images form a range which this cell falls in i.e. wormatlas will use da3.jpg to
                // represent da3-7 --> check for da3 and da8
                else if (images.indexOf(url) != images.size() - 1) { //make sure there is another entry in the list
                    String url2 = images.get(images.indexOf(url) + 1);
                    String imageName2 = url2.substring(findStr.length(), url2.indexOf("."));

                    //see if both images are for the same cell
                    if (imageName.toLowerCase().startsWith(baseName.toLowerCase()) && imageName2.toLowerCase()
                            .startsWith(baseName.toLowerCase())) {
                        //find the range formed by the two images
                        int lowerBound = Character.getNumericValue(imageName.charAt(imageName.length() - 1));
                        int upperBound = Character.getNumericValue(imageName2.charAt(imageName2.length() - 1));

                        //check if num is between the range
                        if (num > lowerBound && num < upperBound) {
                            imageURL = GRAPHIC_URL_RANGE + imageName + JPG_EXT;
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param content
     *         the full html page from wormatlas
     * @param URL
     *         the full url for the page to add to to this cell case's external link
     *
     * @return the 'Function' section from the wormatlas page
     */
    private String findFunctionInHTML(String content, String URL) {
        //parse the html for "Function"
        content = content.substring(content.indexOf("Function"));
        content = content.substring(content.indexOf(":") + 1, content.indexOf("</td>")); //skip the "Function:" text

        content = updateAnchors(content);

        //add the link to the list
        /* links.add(URL); */

        return content
                + "<br><em>Source: </em><a href=\"#\" name=\""
                + URL
                + "\" onclick=\"handleLink(this)\">"
                + URL
                + "</a>";
    }

    /**
     * Sets the anatomy information that is applicable to this cell
     */
    private void setAnatomy() {
        if (functionalName == null) {
            return;
        }
        if (hasAnatomy(functionalName)) {
            anatomies = getAnatomy(functionalName);
        }
    }

    /**
     * Searches the parts list lineage names and finds matching prefixes to the query cell
     *
     * @return homologues for this cell
     */
    private List<List<String>> setHomologues() {
        List<List<String>> homologues = new ArrayList<>();
        List<String> leftRightHomologues = new ArrayList<>();
        List<String> additionalSymmetries = new ArrayList<>();

        if (functionalName == null) {
            return homologues;
        }

        char lastChar = functionalName.charAt(functionalName.length() - 1);
        lastChar = toLowerCase(lastChar);

        String cell = functionalName;
        //check for left, right, dorsal, or ventral suffix --> update cell
        if (lastChar == 'l'
                || lastChar == 'r'
                || lastChar == 'd'
                || lastChar == 'v'
                || lastChar == 'a'
                || lastChar == 'p') {
            //check if multiple suffixes
            lastChar = functionalName.charAt(functionalName.length() - 2);
            lastChar = toLowerCase(lastChar);
            if (lastChar == 'l' || lastChar == 'r' || lastChar == 'd' || lastChar == 'v' || lastChar == 'a'
                    || lastChar == 'p') {
                cell = cell.substring(0, cell.length() - 2);
            } else {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (isDigit(lastChar)) { //check for # e.g. DD1 --> update cell
            //check if double digit
            if (isDigit(functionalName.length() - 2)) {
                cell = cell.substring(0, cell.length() - 2);
            } else {
                cell = cell.substring(0, cell.length() - 1);
            }

        } else { //if no suffix, no homologues e.g. AVG, M
            return homologues;
        }

        cell = cell.toLowerCase();

        //search parts list for matching prefix terms
        ArrayList<String> partsListHits = new ArrayList<>();
        for (String lineageName : getLineageNames()) {
            //GET BASE NAME FOR LINEAGE NAME AS DONE ABOVE WITH CELL NAME
            lineageName = getFunctionalNameByLineageName(lineageName);

            if (lineageName.toLowerCase().startsWith(cell)) {
                partsListHits.add(lineageName);
            }
        }

		/*
         * Add hits to categories:
		 * L/R: ends with l/r
		 * AdditionalSymm: ends with d/v
		 *
		 * NOTE:
		 * RIAL will show as L/R homologue to RIVL because there is not currently logic that remembers what was
		 * peeled off the original base name
		 */
        for (String lineageName : partsListHits) {

            //the base name of the cell
            String base = lineageName;

            lastChar = base.charAt(base.length() - 1);
            lastChar = toLowerCase(lastChar);
            //check for left, right, dorsal, or ventral suffix --> update cell
            if (lastChar == 'l' || lastChar == 'r' || lastChar == 'd' || lastChar == 'v' || lastChar == 'a'
                    || lastChar == 'p') {
                //check if multiple suffixes
                lastChar = base.charAt(base.length() - 2);
                lastChar = toLowerCase(lastChar);
                if (lastChar == 'l' || lastChar == 'r' || lastChar == 'd' || lastChar == 'v' || lastChar == 'a'
                        || lastChar == 'p') {
                    base = base.substring(0, base.length() - 2);
                } else {
                    base = base.substring(0, base.length() - 1);
                }
            } else if (isDigit(lastChar)) { //check for # e.g. DD1 --> update cell
                //check if double digit
                if (isDigit(base.length() - 2)) {
                    base = base.substring(0, base.length() - 2);
                } else {
                    base = base.substring(0, base.length() - 1);
                }

            } else {
            }

            lastChar = lineageName.charAt(lineageName.length() - 1);
            lastChar = toLowerCase(lastChar);
            if (lastChar == 'l' || lastChar == 'r') {
                if (base.toLowerCase().equals(cell)) {
                    leftRightHomologues.add(lineageName);
                }
            } else if (lastChar == 'd' || lastChar == 'v' || isDigit(lastChar)) {
                if (base.toLowerCase().equals(cell)) {
                    additionalSymmetries.add(lineageName);
                }
            }
        }

        //remove self from lists
        if (leftRightHomologues.contains(this.functionalName)) {
            leftRightHomologues.remove(this.functionalName);
        }

        if (additionalSymmetries.contains(this.functionalName)) {
            additionalSymmetries.remove(this.functionalName);
        }

        homologues.add(leftRightHomologues);
        homologues.add(additionalSymmetries);

        return homologues;
    }

    private String addWormWiringLink() {
        if (functionalName != null) {
            String cell = functionalName;
            // TODO fix something in this function (not sure what..)
            //check if N2U, n2y or n930 image series
//			boolean N2U = true;
//			boolean N2Y = false;
//			boolean N930 = false;

            //need to zero pad in link generation
            char lastChar = functionalName.charAt(functionalName.length() - 1);
            if (isDigit(lastChar)) {
                for (int i = 0; i < functionalName.length(); i++) {
                    if (isDigit(functionalName.charAt(i))) {
                        if (i != 0) { //error check
                            cell = functionalName.substring(0, i) + "0" + functionalName.substring(i);
                        }
                    }
                }
            }

            return WORMWIRING_BASE_URL + cell.toUpperCase() + WORMWIRING_N_2_UEXT;

//			if (N2U) {
//				return WORMWIRING_BASE_URL + cell.toUpperCase() + WORMWIRING_N_2_UEXT;
//			} else if (N2Y) {
//				return WORMWIRING_BASE_URL + cell.toUpperCase() + wormwiringN2YEXT;
//			} else if (N930) {
//				return WORMWIRING_BASE_URL + cell.toUpperCase() + wormwiringN930EXT;
//			}
        }
        return "";
    }

    public String getCellName() {
        if (functionalName != null) {
            return functionalName;
        }
        return "";
    }

    public String getExternalInfo() {
        if (externalInfo != null) {
            return externalInfo;
        }
        return "";
    }

    public String getPartsListDescription() {
        if (partsListDescription != null) {
            return partsListDescription;
        }
        return "";
    }

    public String getImageURL() {
        if (imageURL != null) {
            return imageURL;
        }
        return "";
    }

    public String getFunctionWORMATLAS() {
        if (functionWORMATLAS != null) {
            return functionWORMATLAS;
        }
        return "";
    }

    public boolean getHasAnatomyFlag() {
        return this.hasAnatomy;
    }

    public List<String> getAnatomies() {
        return anatomies;
    }

    public List<String> getPresynapticPartners() {
        return presynapticPartners;
    }

    public List<String> getPostsynapticPartners() {
        return postsynapticPartners;
    }

    public List<String> getElectricalPartners() {
        return electricalPartners;
    }

    public List<String> getNeuromuscularPartners() {
        return neuromuscularPartners;
    }

    public List<List<String>> getHomologues() {
        return homologues;
    }
}