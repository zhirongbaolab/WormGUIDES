package application_src.application_model.search.CElegansSearch;

import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.OrganismSearchResults;

import java.util.AbstractMap;
import java.util.List;

public class CElegansSearchResults extends OrganismSearchResults {
    public CElegansSearchResults(AbstractMap.SimpleEntry<OrganismDataType, List<String>> results) {
        super(results);
    }

    public boolean hasResults() { return !getSearchResults().isEmpty(); }
}
