package application_src.application_model.search;

import application_src.application_model.data.OrganismDataType;

import java.util.List;
import java.util.Map;

public class OrganismSearchResults {
    private List<String> searchResults;
    private OrganismDataType searchResultsDataType;

    public OrganismSearchResults(OrganismDataType searchResultsDataType, List<String> searchResults) {
        this.searchResultsDataType = searchResultsDataType;
        this.searchResults = searchResults;
    }
}