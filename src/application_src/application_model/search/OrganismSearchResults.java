package application_src.application_model.search;

import application_src.application_model.data.OrganismDataType;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class OrganismSearchResults {
    private List<String> searchResults;
    private OrganismDataType searchResultsDataType;

    public OrganismSearchResults(AbstractMap.SimpleEntry<OrganismDataType, List<String>> results) {
        this.searchResultsDataType = results.getKey();
        this.searchResults = results.getValue();
    }

    public List<String> getSearchResults() { return this.searchResults; }
    public OrganismDataType getSearchResultsDataType() { return this.searchResultsDataType; }
}