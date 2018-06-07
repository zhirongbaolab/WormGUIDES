package application_src.application_model.search.ModelSearch;

import application_src.application_model.data.LineageData;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
import application_src.application_model.search.OrganismSearchResults;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is one of two main interfaces between the model-agnostic Search pipeline
 * and the specifics of the model displayed in the window.
 *
 * The purpose of the class is to establish the correspondence between the results
 * of search and the underlying model. This correspondence (in the form of a list
 * of names) is then used by the annotation pipeline to annotate the model. After
 * completing the correspondence routine, EstablishCorrespondence dispatches the
 * results to {@Link application_src.annotation_model.annotation.AnnotationManager}
 */
public class EstablishCorrespondence {
    // representations of the underlying model
    private static LineageData lineageData;
    private static SceneElementsList sceneElementsList;

    public EstablishCorrespondence(LineageData lineageData, SceneElementsList sceneElementsList) {
        this.lineageData = lineageData;
        this.sceneElementsList = sceneElementsList;
    }

    public List<String> establishCorrespondence(OrganismSearchResults searchResults,
                                                boolean includeCellNuc, boolean includeCellBody) {
        ArrayList<String> correspondenceList = new ArrayList<>();

        // iterate over the search results, and find the entities that have overlap in the underlying model. Add these
        // to the correspondence list
        List<String> names = lineageData.getAllCellNames();
        for(String name : names) {
            if (searchResults.getSearchResults().contains(name)) {
                correspondenceList.add(name);
            }
        }

        correspondenceList.addAll(StructuresSearch.getCellBodiesList(searchResults.getSearchResults()));

        return correspondenceList;
    }

}
