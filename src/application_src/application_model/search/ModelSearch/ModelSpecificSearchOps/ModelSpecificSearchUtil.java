package application_src.application_model.search.ModelSearch.ModelSpecificSearchOps;

import application_src.application_model.data.LineageData;
import application_src.application_model.threeD.subscenegeometry.SceneElement;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;

import java.util.ArrayList;
import java.util.List;

public class ModelSpecificSearchUtil {
    private static LineageData lineageData;
    private static SceneElementsList sceneElementsList;

    public static void init(LineageData ld, SceneElementsList sel) {
        lineageData = ld;
        sceneElementsList = sel;
    }

    /**
     * A model specific non-sulston lineage search. This is used as a fallthrough mode in
     * a non-sulston embryo when the static lineage search returns no results. First, we
     * determine if the cellName has an exact match, then we do a crude ancestor and descendant
     * search by checking if there is an exact match using a starting prefix checkk. This
     * crude search assumes that non-sulston entities will be named according to a similar
     * strategy to the sulston names i.e. appending characters to names as divisions occur
     * @param cellName
     * @param includeAncestors
     * @param includeDescendants
     * @return
     */
    public static List<String> nonSulstonLineageSearch(final String cellName, boolean includeAncestors, boolean includeDescendants) {
        List<String> nonSulstonLineageSearchResults = new ArrayList<String>();
        

        for (String entityName : lineageData.getAllCellNames()) {
            if (cellName.toLowerCase().equals(entityName.toLowerCase())) { // exact match
                nonSulstonLineageSearchResults.add(entityName);
            } else { // not exact match, see if ancestor or descendant
                if (includeAncestors && cellName.toLowerCase().startsWith(entityName.toLowerCase())) { // ancestor match
                    nonSulstonLineageSearchResults.add(entityName);
                }
                if (includeDescendants && entityName.toLowerCase().startsWith(cellName.toLowerCase())) {
                    nonSulstonLineageSearchResults.add(entityName);
                }
            }
        }

        return nonSulstonLineageSearchResults;
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

    public static boolean isMulticellularStructureByName(final String name) {
        for (SceneElement se : sceneElementsList.getElementsList()) {
            if (se.getSceneName().toLowerCase().equals(name.toLowerCase())
                    && se.isMulticellular()) {
                return true;
            }
        }

        return false;
    }
}
