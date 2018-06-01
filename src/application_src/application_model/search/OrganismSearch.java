package application_src.application_model.search;


import application_src.application_model.data.OrganismDataType;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * Search interface with methods for searching organism data
 *
 */
public interface OrganismSearch {

    /**
     *
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @return
     */
    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeLineageSearch(String searchString,
                                                                      boolean includeAncestors,
                                                                      boolean includeDescendants);

    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeFunctionalSearch(String searchString,
                                                                boolean includeAncestors,
                                                                boolean includeDescendants);

    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeDescriptionSearch(String searchString,
                                                                  boolean includeAncestors,
                                                                  boolean includeDescendans);

    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeConnectomeSearch(String searchString,
                                                                boolean includeAncestors,
                                                                boolean includeDescendants,
                                                                boolean includePresynapticPartners,
                                                                boolean includePostsynapticPartners,
                                                                boolean includeElectricalPartners,
                                                                boolean includeNeuromuscularPartners);

    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeGeneSearch(String searchString,
                                                          boolean isSearchTermGene,
                                                          boolean isSearchTermAnatomy);
}
