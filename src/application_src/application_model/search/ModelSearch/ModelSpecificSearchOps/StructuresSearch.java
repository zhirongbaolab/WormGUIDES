package application_src.application_model.search.ModelSearch.ModelSpecificSearchOps;

import application_src.application_model.annotation.AnnotationManager;
import application_src.application_model.annotation.color.Rule;
import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.application_model.threeD.subscenegeometry.SceneElement;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;
import application_src.application_model.threeD.subscenegeometry.StructureTreeNode;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

import java.util.*;

import static application_src.application_model.data.CElegansData.PartsList.PartsList.getLineageNamesByFunctionalName;
import static application_src.application_model.search.SearchConfiguration.SearchType.STRUCTURES_BY_HEADING;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;

public class StructuresSearch {

    private static SceneElementsList sceneElementsList;
    private TreeItem<StructureTreeNode> structureTreeRoot;
    private AnnotationManager annotationManager;
    private Map<String, List<String>> nameToCellsMap;
    private Map<String, String> nameToCommentsMap;
    private Map<String, String> nameToMarkerMap;

    public StructuresSearch(SceneElementsList sceneElementsList,
                            TreeItem<StructureTreeNode> structureTreeRoot,
                            AnnotationManager annotationManager) {
        this.sceneElementsList = sceneElementsList;
        this.structureTreeRoot = structureTreeRoot;
        this.annotationManager = annotationManager;
        this.nameToCellsMap = this.sceneElementsList.getNameToCellsMap();
        this.nameToCommentsMap = this.sceneElementsList.getNameToCommentsMap();
        this.nameToMarkerMap = this.sceneElementsList.getNameToMarkerMap();
    }


    /**
     * The main structures search. Performs string matching on the sceneElementsList
     *
     * @param searchString
     * @return
     */
    public List<String> searchStructures(String searchString) {
        List<String> searchResults = new ArrayList<>();
        if (searchString == null || searchString.isEmpty()) {
            return searchResults;
        }

        final String[] terms = searchString.toLowerCase().split(" ");

        String nameLower;
        for (String name : sceneElementsList.getAllSceneNames()) {
            if (!searchResults.contains(name)) {
                nameLower = name.toLowerCase();

                boolean appliesToName = false;
                boolean appliesToCell = false;
                boolean appliesToMarker = false;
                boolean appliesToComment = false;

                // search in structure scene names
                for (String term : terms) {
                    if (nameLower.contains(term)) {
                        appliesToName = true;
                        break;
                    }
                }

                // search in cells
                final List<String> cells = nameToCellsMap.get(nameLower);
                if (cells != null) {
                    for (String cell : cells) {
                        // use the first term
                        if (terms.length > 0) {
                            // check if search term is a functional name
                            final List<String> lineageNames = new ArrayList<>(
                                    getLineageNamesByFunctionalName(terms[0]));
                            for (String lineageName : lineageNames) {
                                if (lineageName != null) {
                                    if (cell.toLowerCase().startsWith(lineageName.toLowerCase())) {
                                        appliesToCell = true;
                                        break;
                                    }
                                }
                            }
                            if (cell.toLowerCase().startsWith(terms[0].toLowerCase())) {
                                appliesToCell = true;
                                break;
                            }
                        }
                    }
                }

                if (nameToMarkerMap.get(nameLower).startsWith(terms[0].toLowerCase())) {
                    appliesToMarker = true;
                }

                // search in comments if name does not already apply
                if (nameToCommentsMap.containsKey(nameLower)) {
                    final String commentLowerCase = nameToCommentsMap.get(nameLower).toLowerCase();
                    for (String term : terms) {
                        if (commentLowerCase.contains(term)) {
                            appliesToComment = true;
                        } else {
                            appliesToComment = false;
                            break;
                        }
                    }
                }

                if (appliesToName || appliesToCell || appliesToMarker || appliesToComment) {
                    // use functional name in structure search results instead of lineage names
                    String functionalName = PartsList.getFunctionalNameByLineageName(name);
                    if (functionalName == null) {
                        functionalName = name;
                    }
                    searchResults.add(functionalName);
                }
            }
        }

        return searchResults;
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
     * Retrieves all the scene elements that are the cell bodies of a list of cells
     *
     * @param cells
     *         the list of cells that may or may not have corresponding cell bodies
     *
     * @return the cell bodies of the cells in the list
     */
    public static List<String> getCellBodiesList(final List<String> cells) {
        final List<String> cellBodies = new ArrayList<>();
        if (sceneElementsList != null
                && cells != null
                && !cells.isEmpty()) {
            final List<String> cellsLowerCase = new ArrayList<>();
            for (String cell : cells) {
                cellsLowerCase.add(cell.toLowerCase());
            }
            for (String sceneName : sceneElementsList.getAllSceneNames()) {
                if (cellsLowerCase.contains(sceneName.toLowerCase())) {
                    cellBodies.add(sceneName);
                }
            }
        }
        return cellBodies;
    }

    /**
     *
     * @param heading
     * @return
     */
    public List<String> executeStructureSearchUnderHeading(final String heading) {
        final List<String> structuresToAdd = new ArrayList<>();
        final Queue<TreeItem<StructureTreeNode>> nodeQueue = new LinkedList<>();
        nodeQueue.add(structureTreeRoot);

        // find the node with the desired heading
        TreeItem<StructureTreeNode> headingNode = null;

        TreeItem<StructureTreeNode> treeItem;
        StructureTreeNode node;
        while (!nodeQueue.isEmpty()) {
            treeItem = nodeQueue.remove();
            if (treeItem != null) {
                node = treeItem.getValue();
                if (node.isHeading()) {
                    if (node.getNodeText().equalsIgnoreCase(heading)) {
                        headingNode = treeItem;
                        break;
                    } else {
                        nodeQueue.addAll(treeItem.getChildren());
                    }
                }
            }
        }

        // get all structures under this heading (structures in sub-headings are included as well)
        if (headingNode != null) {
            nodeQueue.clear();
            nodeQueue.add(headingNode);
            while (!nodeQueue.isEmpty()) {
                treeItem = nodeQueue.remove();
                node = treeItem.getValue();
                if (node.isHeading()) {
                    nodeQueue.addAll(treeItem.getChildren());
                } else {
                    structuresToAdd.add(node.getSceneName());
                }
            }
        }

        return structuresToAdd;
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
                    .forEach(se -> cellsSet.addAll(se.getAllCells()));
        }
        final List<String> cells = new ArrayList<>(cellsSet);
        sort(cells);
        return cells;
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

    public void setStructureTreeRoot(TreeItem<StructureTreeNode> root) {
        this.structureTreeRoot = root;
    }
}
