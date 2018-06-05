package application_src.application_model.search.ModelSearch.ModelSpecificSearchOps;

import application_src.application_model.data.LineageData;
import application_src.application_model.threeD.subscenegeometry.SceneElement;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;

public class ModelSpecificSearchUtil {
    private static LineageData lineageData;
    private static SceneElementsList sceneElementsList;

    public ModelSpecificSearchUtil(LineageData lineageData, SceneElementsList sceneElementsList) {
        this.lineageData = lineageData;
        this.sceneElementsList = sceneElementsList;
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
