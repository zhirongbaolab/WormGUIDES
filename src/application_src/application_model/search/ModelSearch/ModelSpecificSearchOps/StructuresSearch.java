package application_src.application_model.search.ModelSearch.ModelSpecificSearchOps;

import application_src.application_model.threeD.subscenegeometry.SceneElement;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;

import java.util.*;

import static java.util.Collections.sort;

public class StructuresSearch {

    private static SceneElementsList sceneElementsList;

    public StructuresSearch(SceneElementsList sceneElementsList) {
        this.sceneElementsList = sceneElementsList;
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
}
