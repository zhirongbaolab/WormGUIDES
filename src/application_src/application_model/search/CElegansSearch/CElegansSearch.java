package application_src.application_model.search.CElegansSearch;


import application_src.application_model.data.CElegansData.Connectome.Connectome;
import application_src.application_model.data.CElegansData.Gene.WormBaseQuery;
import application_src.application_model.data.CElegansData.SulstonLineage.SulstonLineage;
import application_src.application_model.data.CElegansData.PartsList.PartsList;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.OrganismSearch;

import java.util.List;
import java.util.Map;

public class CElegansSearch implements OrganismSearch {

    @Override
    public Map<OrganismDataType, List<String>> executeLineageSearch(String searchString, boolean includeAncestors, boolean includeDescendants) {
        return null;
    }

    @Override
    public Map<OrganismDataType, List<String>> executeFunctionalSearch(String searchString, boolean includeAncestors, boolean includeDescendants) {
        return null;
    }

    @Override
    public Map<OrganismDataType, List<String>> executeDescriptionSearch(String searchString, boolean includeAncestors, boolean includeDescendans) {
        return null;
    }

    @Override
    public Map<OrganismDataType, List<String>> executeConnectomeSearch(String searchString, boolean includeAncestors, boolean includeDescendants, boolean includePresynapticPartners, boolean includePostsynapticPartners, boolean includeElectricalPartners, boolean includeNeuromuscularPartners) {
        return null;
    }

    @Override
    public Map<OrganismDataType, List<String>> executeGeneSearch(String searchString, boolean isSearchTermGene, boolean isSearchTermAnatomy) {
        return null;
    }

    // analogous cells search

    // anatomy search

    // cell deaths search
}
