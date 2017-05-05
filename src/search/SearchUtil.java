/*
 * Bao Lab 2016
 */

/*
 * Bao Lab 2016
 */

package search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import acetree.LineageData;
import connectome.Connectome;
import partslist.PartsList;
import wormguides.models.cellcase.CasesLists;
import wormguides.models.cellcase.NonTerminalCellCase;
import wormguides.models.subscenegeometry.SceneElement;
import wormguides.models.subscenegeometry.SceneElementsList;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;

import static partslist.PartsList.getDescriptions;
import static partslist.PartsList.getFunctionalNames;
import static partslist.PartsList.getLineageNameByIndex;
import static partslist.PartsList.getLineageNames;
import static partslist.PartsList.getLineageNamesByFunctionalName;
import static search.WormBaseQuery.issueWormBaseQuery;
import static wormguides.models.LineageTree.isAncestor;
import static wormguides.models.LineageTree.isDescendant;

/**
 * Utility to search databases for cells that fit a certain criteria. Underlying databases queried include the
 * lineage data, parts list data, scene elements list, connectome, and list of cell cases.
 * <p>
 * See {@link LineageData}, {@link PartsList}, {@link SceneElementsList}, {@link Connectome}, {@link CasesLists}
 */
public class SearchUtil {

    /** Functional names taken from the {@link PartsList} */
    private static final List<String> functionalNames;

    /** Functional descriptions taken from the {@link PartsList} */
    private static final List<String> functionalDescriptions;

    /** Lineage names of cells that appear in the {@link LineageData} */
    private static List<String> activeLineageNames;

    private static SceneElementsList sceneElementsList;

    private static Connectome connectome;

    private static CasesLists casesList;

    private static LineageData lineageData;

    static {
        functionalNames = getFunctionalNames();
        functionalDescriptions = getDescriptions();
    }

    /**
     * Initializes the databases on which cells are queried
     *
     * @param lineageData
     *         lineage data of the embryo
     * @param sceneElementsList
     *         list of scene elements that exist in the embryo
     * @param connectome
     *         connectome information
     * @param casesList
     *         list of cell cases
     */
    public static void initDatabases(
            final LineageData lineageData,
            final SceneElementsList sceneElementsList,
            final Connectome connectome,
            final CasesLists casesList) {
        SearchUtil.lineageData = requireNonNull(lineageData);
        SearchUtil.activeLineageNames = lineageData.getAllCellNames();
        SearchUtil.sceneElementsList = requireNonNull(sceneElementsList);
        SearchUtil.connectome = requireNonNull(connectome);
        SearchUtil.casesList = requireNonNull(casesList);
    }

    /**
     * Returns the list of cells with a searched lineage name
     *
     * @param searched
     *         searched term, the lineage name
     *
     * @return lineage name that is searched
     */
    public static List<String> getCellsWithLineageName(final String searched) {
        final List<String> cells = new ArrayList<>();
        final String searchedLower = searched.toLowerCase();
        activeLineageNames.forEach(name -> {
            if (name.toLowerCase().equals(searchedLower)) {
                cells.add(name);
            }
        });
        return cells;
    }

    /**
     * @param searched
     *         searched term, the prefix functional names
     *         <p>
     *         ***Note: this method must use 'startsWith()' so as to find similar cells in different geospatial
     *         location
     *         e.g. 'siav' -> 'siavl', 'siavr'
     *
     * @return lineage names whose functional name has the searched prefix, in alphabetical order
     */
    public static List<String> getCellsWithFunctionalName(final String searched) {
        return getLineageNamesByFunctionalName(searched);
    }

    /**
     * @param searched
     *         the search string, words that are part of a cell's functional description
     *
     * @return lineage names whose function description contains all parts of the search string, in alphabetical order
     */
    public static List<String> getCellsWithFunctionalDescription(final String searched) {
        final Set<String> cellsSet = new HashSet<>();
        final String[] keywords = searched.split(" ");

        String description;
        boolean isValidDescription;
        String cell;
        for (int i = 0; i < functionalDescriptions.size(); i++) {
            description = functionalDescriptions.get(i).toLowerCase();
            isValidDescription = true;
            for (String keyword : keywords) {
                if (!description.contains(keyword)) {
                    isValidDescription = false;
                    break;
                }
            }
            if (isValidDescription) {
                cellsSet.add(getLineageNameByIndex(i));
            }
        }
        final List<String> cells = new ArrayList<>(cellsSet);
        sort(cells);
        return cells;
    }

    public static boolean isMulticellularStructureByName(final String name) {
        for (SceneElement se : sceneElementsList.getElementsList()) {
            if (se.getSceneName().toLowerCase().equals(name.toLowerCase())
                    && se.isMulticellular()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param searched
     *         the search string, terms that are either a structure scene name or part of its comment
     *
     * @return lineage names of cells that belong to the searched structure, in alphabetical order
     */
    public static List<String> getCellsInMulticellularStructure(final String searched) {
        final Set<String> cellsSet = new HashSet<>();
        if (sceneElementsList != null) {
            sceneElementsList.getElementsList()
                    .stream()
                    .filter(SceneElement::isMulticellular)
                    .filter(se -> isMulticellularStructureSearched(se.getSceneName(), searched))
                    .forEach(se -> se.getAllCells().forEach(cellsSet::add));
        }
        final List<String> cells = new ArrayList<>(cellsSet);
        sort(cells);
        return cells;
    }

    /**
     * Calls the {@link WormBaseQuery} method to issue a web search to WormBase
     *
     * @param searched
     *         the searched gene
     *
     * @return cells with the searched gene expression
     *
     * @see WormBaseQuery#issueWormBaseQuery(String)
     */
    public static List<String> getCellsWithGeneExpression(final String searched) {
        return issueWormBaseQuery(searched);
    }

    /**
     * Tests if a structure was searched based on its scene name and comment
     *
     * @param structureName
     *         name of the structure to check
     * @param searched
     *         the search string
     *
     * @return true if structure's scene name or comment contains all searched terms, false otherwise
     */
    private static boolean isMulticellularStructureSearched(String structureName, final String searched) {
        if (structureName == null || searched == null) {
            return false;
        }
        // search in structure scene names
        structureName = structureName.trim()
                .toLowerCase();
        final String[] terms = searched.trim()
                .toLowerCase()
                .split(" ");
        boolean appliesToName = true;
        for (String term : terms) {
            if (!structureName.contains(term)) {
                appliesToName = false;
                break;
            }
        }
        // search in comments if name does not already apply
        return appliesToName || appliesToStructureWithComment(structureName, terms);
    }

    private static boolean appliesToStructureWithComment(final String structureName, final String[] searchTerms) {
        boolean appliesToComment = true;
        final Map<String, String> commentsMap = sceneElementsList.getNameToCommentsMap();
        if (commentsMap.containsKey(structureName)) {
            final String comment = commentsMap
                    .get(structureName)
                    .toLowerCase();
            for (String term : searchTerms) {
                if (!comment.contains(term)) {
                    appliesToComment = false;
                    break;
                }
            }
            // search does not apply to scene name, return whether it applies to the comment
            return appliesToComment;
        }
        return false;
    }

    /**
     * @param searched
     *         the search string
     * @param isPresynapticQueried
     *         true if presynaptic partners are queried, false otherwise
     * @param isPostsynapticQueried
     *         true if postsynaptic partners are queried, false otherwise
     * @param isNeuromuscularQueried
     *         true if neuromuscular partners are queried, false otherwise
     * @param isElectricalQueried
     *         true if electrical partners are queried, false otherwise
     *
     * @return lineage names of cells thave have the specified connectivity
     */
    public static List<String> getCellsWithConnectivity(
            final String searched,
            final boolean isPresynapticQueried,
            final boolean isPostsynapticQueried,
            final boolean isNeuromuscularQueried,
            final boolean isElectricalQueried) {
        final List<String> cells = new ArrayList<>(connectome.queryConnectivity(
                searched,
                isPresynapticQueried,
                isPostsynapticQueried,
                isNeuromuscularQueried,
                isElectricalQueried,
                true));
        cells.remove(searched);
        sort(cells);
        return cells;
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

    /**
     * Searches for neighbors of a given cell. For each time point that the cell exists, the distance d of a cell
     * closest to it.
     * <p>
     * d = square-root((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)
     * <p>
     * Multiply d by 1.5 and get the maximum spherical radius that another cell must be within in order for that cell
     * to be considered a neighbor to the queried cell.
     *
     * @param cellName
     *         lineage name of the queried cell
     *
     * @return lineage names of all neighboring cells
     */
    public static List<String> getNeighboringCells(final String cellName) {
        final Set<String> cellsSet = new HashSet<>();

        if (cellName == null || !lineageData.isCellName(cellName)) {
            return new ArrayList<>();
        }

        // get time range for cell
        int firstOccurence = lineageData.getFirstOccurrenceOf(cellName);
        int lastOccurence = lineageData.getLastOccurrenceOf(cellName);

        for (int i = firstOccurence; i <= lastOccurence; i++) {
            String[] names = lineageData.getNames(i);
            double[][] positions = lineageData.getPositions(i);

            // find the coordinates of the query cell
            int queryIDX = -1;
            double x = -1;
            double y = -1;
            double z = -1;
            for (int j = 0; j < names.length; j++) {
                if (names[j].toLowerCase().equals(cellName.toLowerCase())) {
                    queryIDX = j;
                    x = positions[j][0];
                    y = positions[j][1];
                    z = positions[j][2];
                }
            }

            // find nearest neighbor
            if (x != -1 && y != -1 && z != -1) {
                double maxSphericalRadius = Double.MAX_VALUE;
                for (int k = 0; k < positions.length; k++) {
                    if (k != queryIDX) {
                        final double distanceFromQuery = distance(
                                x,
                                positions[k][0],
                                y,
                                positions[k][1],
                                z,
                                positions[k][2]);
                        if (distanceFromQuery < maxSphericalRadius) {
                            maxSphericalRadius = distanceFromQuery;
                        }
                    }
                }

                // multiple distance by 1.5
                if (maxSphericalRadius != Double.MAX_VALUE) {
                    maxSphericalRadius *= 1.5;
                }

                // find all cells within d*1.5 range
                for (int n = 0; n < positions.length; n++) {
                    // compute distance from each cell to query cell
                    if (distance(x, positions[n][0], y, positions[n][1], z, positions[n][2]) <= maxSphericalRadius) {
                        // only add new entries
                        if (!names[n].equalsIgnoreCase(cellName)) {
                            cellsSet.add(names[n]);
                        }
                    }
                }
            }
        }
        final List<String> cells = new ArrayList<>(cellsSet);
        sort(cells);
        return cells;
    }

    /**
     * @param x1
     *         x-coordinate of first element
     * @param x2
     *         x-coordinate of second element
     * @param y1
     *         y-coordinate of first element
     * @param y2
     *         y-coordinate of second element
     * @param z1
     *         z-coordinate of first element
     * @param z2
     *         z-coordinate of second element
     *
     * @return the distance between the two elements
     */
    private static double distance(double x1, double x2, double y1, double y2, double z1, double z2) {
        return sqrt(pow((x2 - x1), 2) + pow((y2 - y1), 2) + pow((z2 - z1), 2));
    }

    /**
     * Retrieves the terminal descendants for a cell. This is called by {@link NonTerminalCellCase}.
     *
     * @param queryCell
     *         the cell queried
     *         ß
     *
     * @return list of terminal descendants for the query cell
     */
    public static List<String> getDescendantsList(final String queryCell) {
        final Set<String> descendantsSet = new HashSet<>();
        if (queryCell != null) {
            getLineageNames()
                    .stream()
                    .filter(name -> isDescendant(name, queryCell))
                    .forEachOrdered(descendantsSet::add);
        }
        return new ArrayList<>(descendantsSet);
    }

    /**
     * Retrieves the descendants for all the cells in the input list
     *
     * @param cells
     *         list of cells to check
     *
     * @return list of descendants of all the cells, with no repeats
     */
    public static List<String> getDescendantsList(final List<String> cells, final String searchedText) {
        final Set<String> descendantsSet = new HashSet<>();

        if (cells == null) {
            return new ArrayList<>();
        }

        // special cases for 'ab' and 'p0' because the input list of cells would be empty
        final String searched = searchedText.trim().toLowerCase();
        if (cells.isEmpty()) {
            if (searched.equals("ab")) {
                cells.add("ab");
            } else if (searched.equals("p0")) {
                cells.add("p0");
            }
        }

        for (String cell : cells) {
            activeLineageNames.stream()
                    .filter(name -> isDescendant(name, cell))
                    .forEach(descendantsSet::add);
        }
        return new ArrayList<>(descendantsSet);
    }

    /**
     * Retrieves the ancestors for all the cells in the input list
     *
     * @param cells
     *         list of cells to check
     *
     * @return list of ancestors of all the cells, with no repeats
     */
    public static List<String> getAncestorsList(final List<String> cells, final String searchedText) {
        final Set<String> ancestorsSet = new HashSet<>();
        if (cells == null) {
            return new ArrayList<>();
        }
        // special cases for 'ab' and 'p0' because the input list of cells would be empty
        final String searched = searchedText.trim().toLowerCase();
        if (cells.isEmpty()) {
            if (searched.equals("ab")) {
                cells.add("ab");
            } else if (searched.equals("p0")) {
                cells.add("p0");
            }
        }
        for (String cell : cells) {
            activeLineageNames.stream()
                    .filter(name -> isAncestor(name, cell))
                    .forEach(ancestorsSet::add);
        }
        return new ArrayList<>(ancestorsSet);
    }

    /**
     * @param cellName
     *         name to check
     *
     * @return the first time point for which the cell with this name exists, -1 if the name is invalid
     */
    public static int getFirstOccurenceOf(final String cellName) {
        if (lineageData != null && lineageData.isCellName(cellName)) {
            return lineageData.getFirstOccurrenceOf(cellName);
        } else if (sceneElementsList != null && sceneElementsList.isStructureSceneName(cellName)) {
            return sceneElementsList.getFirstOccurrenceOf(cellName);
        }
        return -1;
    }

    /**
     * @param cellName
     *         name to check
     *
     * @return the last time point for which the cell with this name exists, -1 if the name is invalid
     */
    public static int getLastOccurenceOf(final String cellName) {
        if (lineageData != null && lineageData.isCellName(cellName)) {
            return lineageData.getLastOccurrenceOf(cellName);
        } else if (sceneElementsList != null && sceneElementsList.isStructureSceneName(cellName)) {
            return sceneElementsList.getLastOccurrenceOf(cellName);
        }
        return -1;
    }

    /**
     * @param structureName
     *         name of the queried structure
     *
     * @return comment for that structure
     */
    public static String getStructureComment(String structureName) {
        return sceneElementsList.getCommentByName(structureName);
    }

    /**
     * @param structureName
     *         name of the queried structure
     *
     * @return true if the structure has a comment, false otherwise
     */
    public static boolean isStructureWithComment(String structureName) {
        return sceneElementsList != null && sceneElementsList.isMulticellStructureName(structureName);
    }

    /**
     * @param cellName
     *         queried cell name
     *
     * @return true if the name is a lineage name, false otherwise
     */
    public static boolean isLineageName(String cellName) {
        return activeLineageNames.contains(cellName);
    }
}
