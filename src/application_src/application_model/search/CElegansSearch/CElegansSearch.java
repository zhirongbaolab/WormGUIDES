package application_src.application_model.search.CElegansSearch;


import application_src.application_model.data.CElegansData.AnalogousCells.EmbryonicAnalogousCells;
import application_src.application_model.data.CElegansData.AnalogousCells.EmbryonicHomology;
import application_src.application_model.data.CElegansData.Anatomy.Anatomy;
import application_src.application_model.data.CElegansData.CellDeaths.CellDeaths;
import application_src.application_model.data.CElegansData.Connectome.Connectome;
import application_src.application_model.data.CElegansData.Connectome.NeuronalSynapse;
import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.CElegansData.Gene.WormBaseQuery;
import application_src.application_model.data.CElegansData.SulstonLineage.SulstonLineage;
import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.OrganismSearch;
import javafx.scene.control.TreeItem;

import java.util.*;

public class CElegansSearch implements OrganismSearch, Runnable {


    ////////// LINEAGE SEARCH ////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @return
     */
    @Override
    public AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeLineageSearch(String searchString, boolean includeAncestors, boolean includeDescendants) {
        OrganismDataType lineageDataType = OrganismDataType.LINEAGE;
        ArrayList<String> searchResults = new ArrayList<>();

        // access the PartsList and CellDeaths arrays
        ArrayList<String> lineageNames = (ArrayList<String>)PartsList.getLineageNames();
        ArrayList<String> cellDeaths = (ArrayList<String>)CellDeaths.getCellDeaths();

        boolean found = false;
        String strMatch = "";

        // check for exact and prefix match in both lists
        for (String s : lineageNames) {
            if (s.toLowerCase().equals(searchString.toLowerCase()) || s.toLowerCase().startsWith(searchString.toLowerCase())) {
                strMatch = s;
                found = true;
                break;
            }
        }

        // check in the cell deaths
        if (!found) {
            for (String s : cellDeaths) {
                if (s.toLowerCase().equals(searchString.toLowerCase()) || s.toLowerCase().startsWith(searchString.toLowerCase())) {
                    strMatch = s;
                    found = true;
                    break;
                }
            }
        }

        // check in the special cases of the sulston lineage
        if (!found) {
            String[] specialCasesAsStringArray = SulstonLineage.getSpecialCasesAsStringArray();
            for (String s : specialCasesAsStringArray) {
                if (s.toLowerCase().equals(searchString.toLowerCase())) {
                    strMatch = s;
                    found = true;
                }
            }
        }

        if (found) { // found indicates that this is a valid lineage name
            searchResults = (ArrayList<String>)executeLineageSearch(searchString, strMatch, includeAncestors, includeDescendants);
        }

        AbstractMap.SimpleEntry<OrganismDataType, List<String>> results =
                new AbstractMap.SimpleEntry(lineageDataType, cleanResults(searchResults));
        return results;
    }

    ///// lineage search helper functions /////

    /**
     *
     * @param searchString
     * @param matchString
     * @param includeAncestors
     * @param includeDescendants
     * @return
     */
    private List<String> executeLineageSearch(String searchString, String matchString, boolean includeAncestors, boolean includeDescendants) {
        ArrayList<String> results = new ArrayList<>();

        // all combinations that require search
        if (includeAncestors) {
            /* peel off letters of the search string, and add each one to the results.
             * with each variation of the string, check if it is a special case in the
             * sulston lineage
             */
            TreeItem<String> P0_root = SulstonLineage.getP0_root();
            String[] specialCasesAsStringArray = SulstonLineage.getSpecialCasesAsStringArray();

            for (int i = searchString.length(); i >= 0; i--) {
                String substr = searchString.substring(0, i);
                if (Arrays.asList(specialCasesAsStringArray).contains(substr)) {
                    results.add(substr);
                    results.addAll(addSpecialCasesAncestors(substr));
                    break;
                }

                // add the substring to the results list
                results.add(substr);
            }
        }

        if (includeDescendants) {
            // access the PartsList and CellDeaths arrays
            ArrayList<String> lineageNames = (ArrayList<String>)PartsList.getLineageNames();
            ArrayList<String> cellDeaths = (ArrayList<String>)CellDeaths.getCellDeaths();

            // first check if this is a special case
            if (Arrays.asList(SulstonLineage.getSpecialCasesAsStringArray()).contains(searchString)) {
                // add all descendants in the special case tree. When a node with no leaf is reached, add all
                // of its descendants using the regular lineage naming paradigm
                TreeItem<String> matchTreeItem = findTreeItem(searchString, SulstonLineage.getP0_root());

                List<TreeItem<String>> children = findChildren(matchTreeItem);
                for (TreeItem<String> child : children) {
                    results.add(child.getValue());
                    if (child.isLeaf()) {
                        results.addAll(addAllDescendants(child.getValue()));
                    }

                }
            }

            // use the standard sulston descendant paradigm to add results (even in the special cases, there are
            // some post-embryonic cells which share the prefix of the special cases, and this will pick them up
            results.addAll(addAllDescendants(searchString));
        }

        // add the search string itself if it's not already there
        if (!results.contains(searchString)) {
            results.add(searchString);
        }

        return results;
    }

    /**
     *
     * @param searchString
     * @return
     */
    private List<String> addAllDescendants(String searchString) {
        ArrayList<String> results = new ArrayList<>();

        // access the PartsList and CellDeaths arrays
        ArrayList<String> lineageNames = (ArrayList<String>)PartsList.getLineageNames();
        ArrayList<String> cellDeaths = (ArrayList<String>)CellDeaths.getCellDeaths();

        // for every match found, generate all descendants between the search string and the match
        for (String s : lineageNames) {
            if (s.toLowerCase().startsWith(searchString.toLowerCase())) {
                for (int i = searchString.length()+1; i <= s.length(); i++) {
                    results.add(s.substring(0, i));
                }
            }
        }

        for (String s : cellDeaths) {
            if (s.toLowerCase().startsWith(searchString.toLowerCase())) {
                for (int i = searchString.length()+1; i <= s.length(); i++) {
                    results.add(s.substring(0, i));
                }
            }
        }

        return results;
    }

    /**
     *
     * @param ancestor
     * @return
     */
    private List<String> addSpecialCasesAncestors(String ancestor) {
        ArrayList<String> specialCaseAncestors = new ArrayList<>();

        TreeItem<String> P0_root = SulstonLineage.getP0_root();

        // find the ancestor node
        TreeItem<String> ancestorTreeItem = findTreeItem(ancestor, P0_root);

        // add all ancestors of this item
        while (ancestorTreeItem.getParent() != null) {
            ancestorTreeItem = ancestorTreeItem.getParent();
            specialCaseAncestors.add(ancestorTreeItem.getValue());
        }

        return specialCaseAncestors;
    }

    /**
     *
     * @param queryStr
     * @param root
     * @return
     */
    private TreeItem<String> findTreeItem(String queryStr, TreeItem<String> root) {
        // check if valid
        if (root != null) {
            if (root.getValue().toLowerCase().equals(queryStr.toLowerCase())) {
                return root;
            } else if (root.getChildren().size() != 0) {
                TreeItem<String> foundNode = findTreeItem(queryStr, root.getChildren().get(0)); // 0 for left side child
                if (foundNode == null) {
                    foundNode = findTreeItem(queryStr, root.getChildren().get(1)); // 0 for right side child
                }
                return foundNode;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     *
     * @param root
     * @return
     */
    private List<TreeItem<String>> findChildren(TreeItem<String> root) {
        List<TreeItem<String>> children = new ArrayList<>();
        children = findChildren(children, root);
        return children;
    }

    /**
     *
     * @param children
     * @param root
     * @return
     */
    private List<TreeItem<String>> findChildren(List<TreeItem<String>> children, TreeItem<String> root) {
        if (root == null) {
            return children;
        }

        if (root.isLeaf()) {
            children.add(root);
            return children;
        }

        children = findChildren(children, root.getChildren().get(0));
        children = findChildren(children, root.getChildren().get(1));
        return children;
    }

    /**
     *
     * @param searchString
     * @return
     */
    public List<String> findTerminalDescendants(String searchString) {
        ArrayList<String> terminalDescendants = new ArrayList<>();



        return terminalDescendants;
    }
    ////////// END LINEAGE SEARCH /////////////////////////////////////////////////////////////////////////////////////





    ////////// FUNCTIONAL SEARCH ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeFunctionalSearch(String searchString, boolean includeAncestors, boolean includeDescendants, OrganismDataType intendedResultsType) {
        ArrayList<String> searchResults = new ArrayList<>();

        ArrayList<String> functionalNames = (ArrayList<String>)PartsList.getFunctionalNames();
        for (int i = 0; i < functionalNames.size(); i++) {
            String s = functionalNames.get(i);
            if (s.toLowerCase().startsWith(searchString.toLowerCase())
                    || s.toLowerCase().equals(searchString.toLowerCase())) {
                if (intendedResultsType.equals(OrganismDataType.FUNCTIONAL)) {
                    searchResults.add(s);
                } else if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
                    searchResults.add(PartsList.getLineageNameByIndex(i));
                }
            }
        }


        if (includeAncestors) {
            ArrayList<String> ancestorSearchResults = new ArrayList<>();
            searchResults.stream().forEach(s -> {
                // let's only allow ancestors search on results that are lineage-based, otherwise the
                // results will be mixed format and that's not good for anyone
                if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
                    ancestorSearchResults.addAll(executeLineageSearch(s, true, false).getValue());
                }});
            searchResults.addAll(ancestorSearchResults);
        }

        AbstractMap.SimpleEntry<OrganismDataType, List<String>> results =
                new AbstractMap.SimpleEntry(intendedResultsType, cleanResults(searchResults));
        return results;
    }

    ////////// FUNCTIONAL SEARCH ////////////////////////////////////////////////////////////////////////////////////




    ////////// DESCRIPTION SEARCH ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeDescriptionSearch(String searchString, boolean includeAncestors, boolean includeDescendants, OrganismDataType intendedResultsType) {
        ArrayList<String> searchResults = new ArrayList<>();


        ArrayList<String> descriptions = (ArrayList<String>)PartsList.getDescriptions();
        for (int i = 0; i < descriptions.size(); i++) {
            String d = descriptions.get(i);
            if (d.toLowerCase().contains(searchString.toLowerCase())
                    || d.toLowerCase().equals(searchString.toLowerCase())) {
                if (intendedResultsType.equals(OrganismDataType.FUNCTIONAL)) {
                    searchResults.add(PartsList.getFunctionalNameByIndex(i));
                } else if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
                    searchResults.add(PartsList.getLineageNameByIndex(i));
                }
            }
        }


        if (includeAncestors) {
            ArrayList<String> ancestorSearchResults = new ArrayList<>();
            searchResults.stream().forEach(s -> {
                // let's only allow ancestors search on results that are lineage-based, otherwise the
                // results will be mixed format and that's not good for anyone
                if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
                    ancestorSearchResults.addAll(executeLineageSearch(s, true, false).getValue());
                }});
            searchResults.addAll(ancestorSearchResults);
        }


        AbstractMap.SimpleEntry<OrganismDataType, List<String>> results =
                new AbstractMap.SimpleEntry(intendedResultsType, cleanResults(searchResults));
        return results;
    }

    ////////// END DESCRIPTION SEARCH ////////////////////////////////////////////////////////////////////////////////////


    ////////// CONNECTOME SEARCH ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeConnectomeSearch(String searchString, boolean includeAncestors, boolean includeDescendants, boolean includePresynapticPartners, boolean includePostsynapticPartners, boolean includeElectricalPartners, boolean includeNeuromuscularPartners, OrganismDataType intendedResultsType) {
        ArrayList<String> searchResults = new ArrayList<>();

        searchString = checkQueryCell(searchString);
        // //iterate over synapses
        for (NeuronalSynapse ns : Connectome.getSynapseList()) {
            // check if synapse contains query cell
            if (ns.getCell1().toLowerCase().contains(searchString.toLowerCase())
                    || ns.getCell2().toLowerCase().contains(searchString.toLowerCase())) {

                String cell1 = ns.getCell1();
                String cell2 = ns.getCell2();

                // processUrl type code
                String synapseTypeDescription = ns.getSynapseType().getDescription();

                // find synapse type code for connection, compare to toggle ticks
                switch (synapseTypeDescription) {
                    case Connectome.S_PRESYNAPTIC_DESCRIPTION:
                        if (includePresynapticPartners) {
                            // don't add duplicates
                            if (!searchResults.contains(cell1)) {
                                searchResults.add(cell1);
                            }
                            if (!searchResults.contains(cell2)) {
                                searchResults.add(cell2);
                            }
                        }
                        break;
                    case Connectome.R_POSTSYNAPTIC_DESCRIPTION:
                        if (includePostsynapticPartners) {
                            // don't add duplicates
                            if (!searchResults.contains(cell1)) {
                                searchResults.add(cell1);
                            }
                            if (!searchResults.contains(cell2)) {
                                searchResults.add(cell2);
                            }
                        }
                        break;
                    case Connectome.EJ_ELECTRICAL_DESCRIPTION:
                        if (includeElectricalPartners) {
                            // don't add duplicates
                            if (!searchResults.contains(cell1)) {
                                searchResults.add(cell1);
                            }
                            if (!searchResults.contains(cell2)) {
                                searchResults.add(cell2);
                            }
                        }
                        break;
                    case Connectome.NMJ_NEUROMUSCULAR_DESCRPITION:
                        if (includeNeuromuscularPartners) {
                            // don't add duplicates
                            if (!searchResults.contains(cell1)) {
                                searchResults.add(cell1);
                            }
                            if (!searchResults.contains(cell2)) {
                                searchResults.add(cell2);
                            }
                        }
                        break;
                }
            }
        }

        if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
            ArrayList<String> lineageNameResults = new ArrayList<>();
            for (String result : searchResults) {
                lineageNameResults.addAll(PartsList.getLineageNamesByFunctionalName(result));
            }

            ArrayList<String> ancestorSearchResults = new ArrayList<>();
            if (includeAncestors) {
                lineageNameResults.stream().forEach(s -> {
                    // let's only allow ancestors search on results that are lineage-based, otherwise the
                    // results will be mixed format and that's not good for anyone
                    if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
                        ancestorSearchResults.addAll(executeLineageSearch(s, true, false).getValue());
                    }});
            }
            searchResults.clear();
            searchResults.addAll(lineageNameResults);
            searchResults.addAll(ancestorSearchResults);
        }


        AbstractMap.SimpleEntry<OrganismDataType, List<String>> results =
                new AbstractMap.SimpleEntry(intendedResultsType, cleanResults(searchResults));
        return results;
    }

    /**
     * Retrieves the functional name of an input cell name, whether it is a lineage or functional name.
     *
     * @param searchString
     *         the cell to check
     *
     * @return the functional name of that cell
     */
    public static String checkQueryCell(String searchString) {
            if (PartsList.isLineageName(searchString)) {
                searchString = PartsList.getFunctionalNameByLineageName(searchString);
            }
            return searchString;
    }

    ////////// END CONNECTOME SEARCH ////////////////////////////////////////////////////////////////////////////////////


    ////////// GENE SEARCH SEARCH ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Search either for the anatomy that expresses a given gene, or the gene expression of a given anatomical entity
     *
     * This is a unique search mode, because it makes an http request to WormBase. It requires a dedicated thread to
     * avoid locking up the program. To call this search, the SearchLayer sets the desires gene search term to the
     * static variable in {@Link GeneSearchManager}, and then the
     *
     * Pass this a lineage name
     *
     * @param searchString
     * @param isSearchTermGene
     * @param isSearchTermAnatomy
     * @param intendedResultsType
     * @return
     */
    @Override
    public AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeGeneSearch(String searchString, boolean includeAncestors, boolean includeDescendants, boolean isSearchTermGene, boolean isSearchTermAnatomy, OrganismDataType intendedResultsType) {
        ArrayList<String> searchResults = new ArrayList<>();

        if (isSearchTermGene && !isSearchTermAnatomy) {
            // search term is a gene -> find the cells (anatomy) that express this gene
            searchResults.addAll(WormBaseQuery.issueWormBaseGeneQuery(searchString));

            if (intendedResultsType.equals(OrganismDataType.FUNCTIONAL)) {
                // convert the lineage names to functional
                ArrayList<String> searchResultsAsFunctionalNames = new ArrayList<>();
                searchResults.stream().forEach(s -> {
                    searchResultsAsFunctionalNames.add(PartsList.getFunctionalNameByLineageName(s));
                });

                searchResults.clear();
                searchResults.addAll(searchResultsAsFunctionalNames);
            } else if (intendedResultsType.equals(OrganismDataType.LINEAGE)) {
                ArrayList<String> ancestorsSearchResults = new ArrayList<>();
                if (includeAncestors) {
                    searchResults.stream().forEach(s -> {
                       ancestorsSearchResults.addAll(executeLineageSearch(s, true, false).getValue());
                    });
                }

                ArrayList<String> descendantsSearchResults = new ArrayList<>();
                if (includeDescendants) {
                    searchResults.stream().forEach(s -> {
                       descendantsSearchResults.addAll(executeLineageSearch(s, false, true).getValue());
                    });
                }

                searchResults.addAll(ancestorsSearchResults);
                searchResults.addAll(descendantsSearchResults);
            }
        } else if (!isSearchTermGene && isSearchTermAnatomy) {
            // search term is an anatomy term -> find the genes that are expresses in this piece of anatomy
            searchResults.addAll(WormBaseQuery.issueWormBaseAnatomyTermQuery(searchString));
            intendedResultsType = OrganismDataType.GENE; // it probably will already be this, but this is just a safe measure
        }

        AbstractMap.SimpleEntry<OrganismDataType, List<String>> results =
                new AbstractMap.SimpleEntry(intendedResultsType, cleanResults(searchResults));
        return results;
    }

    /**
     * This the method that is called to initiate gene search. It sets the seach term and options in the manager class,
     * and then begins the search by running the thread that the search takes place on.
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @param isSearchTermGene
     * @param isSearchTermAnatomy
     * @param intendedResultsType
     */
    public void startGeneSearch(String searchString, boolean includeAncestors, boolean includeDescendants, boolean isSearchTermGene, boolean isSearchTermAnatomy, OrganismDataType intendedResultsType) {
        GeneSearchManager.setSearchTerm(searchString.toLowerCase());
        GeneSearchManager.setSearchOptions(includeAncestors, includeDescendants, isSearchTermGene, isSearchTermAnatomy, intendedResultsType);
        if (!GeneSearchManager.getGeneResultsCache().containsKey(searchString.toLowerCase())) {
            run();
        }
    }

    /**
     * The thread that makes the request to WormBase and stores the results in the cache
     */
    @Override
    public void run() {
        GeneSearchManager.cacheGeneResults(GeneSearchManager.getSearchTerm(),
                executeGeneSearch(GeneSearchManager.getSearchTerm(),
                        GeneSearchManager.getIncludeAncestorsParam(),
                        GeneSearchManager.getIncludeDescendantsParam(),
                        GeneSearchManager.getIsSearchTermGene(),
                        GeneSearchManager.getIsSearchTermAnatomy(),
                        GeneSearchManager.getIntendedResultsType()));
    }

    /**
     * Checks whether a name is a gene name with the format SOME_STRING-SOME_NUMBER.
     *
     * @param name
     *         the name to check
     *
     * @return true if the name is a gene name, false otherwise
     */
    public static boolean isGeneFormat(String name) {
        name = name.trim();
        final int hyphenIndex = name.indexOf("-");
        // check that there is a hyphen and there is a string preceeding it
        return hyphenIndex > 1 && name.substring(hyphenIndex + 1).matches("^-?\\d+$");
    }

    ////////// END GENE SEARCH ////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////// EMBRYONIC HOMOLOGY - ANALOGOUS CELLS SEARCH ////////////////////////////////
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
        for (EmbryonicHomology eh : EmbryonicAnalogousCells.getHomologues()) {
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
    ///////////////////////////////// END EMBRYONIC HOMOLOGY - ANALOGOUS CELLS SEARCH ////////////////////////////////

    //////////////////////////// ANATOMY SEARCH /////////////////////////////////////////
    /**
     * Provides anatomy info for a given cell
     *
     * @param cellName
     *         name of the cell
     *
     * @return the anatomy information for the given cell
     */
    public static ArrayList<String> getAnatomy(String cellName) {
        cellName = cellName.toUpperCase();
        ArrayList<String> anatomy = new ArrayList<>();

        int idx = -1;

        ArrayList<String> functionalNames = (ArrayList<String>)Anatomy.getFunctionalNames();
        ArrayList<String> types = (ArrayList<String>)Anatomy.getTypes();
        ArrayList<String> somaLocations = (ArrayList<String>)Anatomy.getSomaLocations();
        ArrayList<String> neuriteLocations = (ArrayList<String>)Anatomy.getNeuriteLocations();
        ArrayList<String> morphologicalFeatures = (ArrayList<String>)Anatomy.getMorphologicalFeatures();
        ArrayList<String> functions = (ArrayList<String>)Anatomy.getFunctions();
        ArrayList<String> neurotransmitters = (ArrayList<String>)Anatomy.getNeurotransmitters();
        //exact match

        for (int i = 0; i < functionalNames.size(); i++) {
            if (functionalNames.get(i).equals(cellName)) {
                idx = i;
                break;
            }
        }

        //if no exact match, update cell and search again
        if (idx == -1) {
            cellName = findRootOfCell(cellName);

            //check for match with updated cell name
            for (int i = 0; i < functionalNames.size(); i++) {
                if (functionalNames.get(i).equals(cellName)) {
                    idx = i;
                }
            }
        }

        if (idx != -1) {
            //add functional name
            anatomy.add(functionalNames.get(idx));

            //add type
            if (types.get(idx) != null) {
                anatomy.add(types.get(idx));
            } else {
                anatomy.add("*");
            }

            //add soma location
            if (somaLocations.get(idx) != null) {
                anatomy.add(somaLocations.get(idx));
            } else {
                anatomy.add("*");
            }

            //add neurite location
            if (neuriteLocations.get(idx) != null) {
                anatomy.add(neuriteLocations.get(idx));
            } else {
                anatomy.add("*");
            }

            //add morphological features
            if (morphologicalFeatures.get(idx) != null) {
                anatomy.add(morphologicalFeatures.get(idx));
            } else {
                anatomy.add("*");
            }

            //add function
            if (functions.get(idx) != null) {
                anatomy.add(functions.get(idx));
            } else {
                anatomy.add("*");
            }

            //add neurotransmitter
            if (neurotransmitters.get(idx) != null) {
                anatomy.add(neurotransmitters.get(idx));
            } else {
                anatomy.add("*");
            }
        }
        return anatomy;
    }

    /**
     * Checks if the supplied cell has an anatomy description. If a lineage name is given, it is translated to a
     * functional name first.
     *
     * @param cellName
     *         name of the cell, lineage or functional
     *
     * @return true if cell has anatomy, false otherwise
     */
    public static boolean hasAnatomy(String cellName) {
        if (cellName == null) return false;
        cellName = checkQueryCell(cellName).toUpperCase();

        ArrayList<String> functionalNames = (ArrayList<String>)Anatomy.getFunctionalNames();

        //check for exact match
        for (String funcName : functionalNames) {
            if (funcName.equals(cellName)) {
                return true;
            }
        }

        cellName = findRootOfCell(cellName);

        //check for match with updated cell name
        for (String funcName : functionalNames) {
            if (funcName.equals(cellName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds the base name of a cell i.e. the cell without dorsal, ventral,
     * left, right, etc. classifiers
     *
     * @param cell
     *         the cell to find the base of
     *
     * @return the base name of the cell
     */
    private static String findRootOfCell(String cell) {
        //remove number suffixes, l/r, d/v
        Character lastChar = cell.charAt(cell.length() - 1);
        lastChar = Character.toLowerCase(lastChar);
        if (lastChar == 'r' || lastChar == 'l') {
            cell = cell.substring(0, cell.length() - 1);

            // check if preceding d/v
            lastChar = cell.charAt(cell.length() - 1);
            lastChar = Character.toLowerCase(lastChar);
            if (lastChar == 'd' || lastChar == 'v') {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (lastChar == 'd' || lastChar == 'v') { // will l/r ever come
            // before d/v
            cell = cell.substring(0, cell.length() - 1);

            // check if preceding l/r
            lastChar = cell.charAt(cell.length() - 1);
            lastChar = Character.toLowerCase(lastChar);
            if (lastChar == 'l' || lastChar == 'r') {
                cell = cell.substring(0, cell.length() - 1);
            }
        } else if (Character.isDigit(lastChar)) {
            cell = cell.substring(0, cell.length() - 1).toUpperCase();
        }

        return cell;
    }

    //////////////////////////// END ANATOMY SEARCH /////////////////////////////////////////





    ///////////////////////// CELL DEATHS SEARCH /////////////////////////////////////////////
    public static boolean isCellDeath(String searchString) {
        for (String s : CellDeaths.getCellDeaths()) {
            if (s.toLowerCase().equals(searchString.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    ///////////////////////// END CELL DEATHS SEARCH /////////////////////////////////////////////




    /////////////////// GENERAL UTILITIES /////////////////////////////////////////////////

    /**
     *
     *
     * Used by:
     *
     * @param results
     * @return
     */
    private List<String> cleanResults(List<String> results) {
        // remove duplicates
        Set<String> hs = new HashSet<>();
        hs.addAll(results);
        results.clear();
        results.addAll(hs);

        // remove names with '.' at end
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).endsWith(".")) {
                results.remove(i);
            }

            if (results.get(i).isEmpty()) {
                results.remove(i);
            }

            if (results.get(i).length() <= 1) {
                results.remove(i);
            }
        }

        return results;
    }

    /**
     *
     * Used by:
     * WormBaseQuery.java
     *
     * @param searchString
     * @return
     */
    public static boolean isValidLineageSearchTerm(String searchString) {
        ArrayList<String> lineageNames = (ArrayList<String>)PartsList.getLineageNames();
        // check for exact and prefix match in both lists
        boolean found = false;
        for (String s : lineageNames) {
            if (s.toLowerCase().equals(searchString.toLowerCase()) || s.toLowerCase().startsWith(searchString.toLowerCase())) {
                return true;
            }
        }

        // check in the cell deaths
        ArrayList<String> cellDeaths = (ArrayList<String>)CellDeaths.getCellDeaths();
        if (!found) {
            for (String s : cellDeaths) {
                if (s.toLowerCase().equals(searchString.toLowerCase()) || s.toLowerCase().startsWith(searchString.toLowerCase())) {
                    return true;
                }
            }
        }

        // check in the special cases of the sulston lineage
        if (!found) {
            String[] specialCasesAsStringArray = SulstonLineage.getSpecialCasesAsStringArray();
            for (String s : specialCasesAsStringArray) {
                if (s.toLowerCase().equals(searchString.toLowerCase())) {
                    return true;
                }
            }
        }

        return found;
    }

    /**
     *
     *
     * Used by:
     * WormBaseQuery.java
     *
     * @param searchString
     * @return
     */
    public static boolean isValidFunctionalSearchTerm(String searchString) {
        ArrayList<String> functionalNames = (ArrayList<String>)PartsList.getFunctionalNames();
        for (String s : functionalNames) {
            if (s.toLowerCase().startsWith(searchString.toLowerCase())
                    || s.toLowerCase().equals(searchString.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /////////////////// END GENERAL UTILITIES /////////////////////////////////////////////////


    /////////////////// UNIT TESTS FOR SEARCH PIPELINE ////////////////////////////////////////

    /**
     * unit testing main
     */
    public static void main(String[] args) {
        SulstonLineage.init();
        PartsList.init();
        CellDeaths.init();
        Connectome.init();
        Anatomy.init();
        EmbryonicAnalogousCells.init();
        GeneSearchManager.init();

        CElegansSearch search = new CElegansSearch();
        AbstractMap.SimpleEntry<OrganismDataType, List<String>> results;

        /////////////// INTERFACE METHODS //////////////////
        // /* LINEAGE TESTING */
        // test exact/prefix lineage match, no ancestors, no descendants
//        results = search.executeLineageSearch("ABar", false, false);
//        System.out.println("Results for lineage search on 'ABar' (a=false, d=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        // test exact/prefix lineage match, no ancestors, yes descendants
//        results = search.executeLineageSearch("ABplaapa", false, true);
//        System.out.println("Results for lineage search on 'ABplaapa' (a=false, d=true) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        // test exact/prefix lineage match, yes ancestors, no descendants
//        results = search.executeLineageSearch("ABpla", true, false);
//        System.out.println("Results for lineage search on 'ABpla' (a=true, d=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        // test exact/prefix lineage match, yes ancestors, yes descendants
//        results = search.executeLineageSearch("P3", true, true);
//        System.out.println("Results for lineage search on 'P3' (a=true, d=true) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");



        /* FUNCTIONAL TESTING */
//        results = search.executeFunctionalSearch("AI", false, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for functionall search on 'AI' (a=false, d=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

//        results = search.executeFunctionalSearch("AIAL", true, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for functionall search on 'AIAL' (a=true, d=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

//        results = search.executeFunctionalSearch("hyp7", false, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for functionall search on 'hyp7' (a=false, d=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        /* DESCRIPTION TESTING */
//        results = search.executeDescriptionSearch("Cephalic neurons", true, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for description search on 'Cephalic neurons' (a=true, d=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        /* CONNECTOME TESTING */
//        results = search.executeConnectomeSearch("DA5", false, false, true, false, false, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for connectome search on 'DA5' (a=false, d=false, pre=true, post=false, elec=false, neuromusc=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

//        results = search.executeConnectomeSearch("DA5", false, false, false, true, false, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for connectome search on 'DA5' (a=false, d=false, pre=false, post=true, elec=false, neuromusc=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

//        results = search.executeConnectomeSearch("DA5", false, false, false, false, true, false, OrganismDataType.LINEAGE);
//        System.out.println("Results for connectome search on 'DA5' (a=false, d=false, pre=false, post=true, elec=false, neuromusc=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

//        results = search.executeConnectomeSearch("DA5", false, false, false, false, false, true, OrganismDataType.FUNCTIONAL);
//        System.out.println("Results for connectome search on 'DA5' (a=false, d=false, pre=false, post=true, elec=false, neuromusc=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

//        results = search.executeConnectomeSearch("DA5", true, false, true, false, false, true, OrganismDataType.LINEAGE);
//        System.out.println("Results for connectome search on 'DA5' (a=false, d=false, pre=false, post=true, elec=false, neuromusc=false) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        /* GENE TESTING */
        search.startGeneSearch("lim-4", true, false, true, false, OrganismDataType.LINEAGE);
        results = GeneSearchManager.getGeneResultsCache().get("lim-4");
        System.out.println("Results for gene search on 'lim-4' (a=true, d=false, gene=true, anatomy=false) -> DataType: " + results.getKey().getDescription());
        System.out.println("size of results: " + results.getValue().size());
        for (String s : results.getValue()) {
            System.out.println(s);
        }
        System.out.println("");

//        results = search.executeGeneSearch("SAAVL", false, false, false, true, OrganismDataType.GENE);
//        System.out.println("Results for gene search on 'SAAVL' (a=false, d=false, gene=false, anatomy=true) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");
//
//        results = search.executeGeneSearch("SAAVL", false, false, false, true, OrganismDataType.GENE);
//        System.out.println("Results for gene search on 'SAAVL' (a=true, d=false, gene=false, anatomy=true) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");
//
//        results = search.executeGeneSearch("ABpl", false, false, false, true, OrganismDataType.GENE);
//        System.out.println("Results for gene search on 'ABpl' (a=false, d=false, gene=false, anatomy=true) -> DataType: " + results.getKey().getDescription());
//        System.out.println("size of results: " + results.getValue().size());
//        for (String s : results.getValue()) {
//            System.out.println(s);
//        }
//        System.out.println("");

        //////////// OTHER C ELEGANS METHODS ///////////////

        // embryonic homology
        ArrayList<String> resultsList = search.getAnatomy("ASH");
        System.out.println("Results for anatomy search on 'ASH'");
        System.out.println("size of results: " + resultsList.size());
        for (String s : resultsList) {
            System.out.println(s);
        }
        System.out.println("");

        // anatomy

        // cell deaths


    }

    /////////////////// END UNIT TESTS FOR SEARCH PIPELINE ////////////////////////////////////////
} // end of class