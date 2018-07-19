package application_src.application_model.search;


import application_src.application_model.data.OrganismDataType;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * Search interface with methods for searching organism data
 *
 * The underlying data type for WormGUIDES is lineage-based and it
 * is therefore usually the case that search results must be in lineage form
 * to be able to passed onto the model annotation pipeline. However, sometimes
 * it is necessary or desirable to return search results in other formats
 * (functional names, gene names), so most methods have a parameter indicating
 * the desired type of results
 */
public interface OrganismSearch {

    /**
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @return
     */
    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeLineageSearch(String searchString,
                                                                                 boolean includeAncestors,
                                                                                 boolean includeDescendants);

    /**
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @param intendedResultsType
     * @return
     */
    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeFunctionalSearch(String searchString,
                                                                                    boolean includeAncestors,
                                                                                    boolean includeDescendants,
                                                                                    OrganismDataType intendedResultsType);

    /**
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @param intendedResultsType
     * @return
     */
    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeDescriptionSearch(String searchString,
                                                                                     boolean includeAncestors,
                                                                                     boolean includeDescendants,
                                                                                     OrganismDataType intendedResultsType);

    /**
     *
     * @param searchString
     * @param includeAncestors
     * @param includeDescendants
     * @param includePresynapticPartners
     * @param includePostsynapticPartners
     * @param includeElectricalPartners
     * @param includeNeuromuscularPartners
     * @param intendedResultsType
     * @return
     */
    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeConnectomeSearch(String searchString,
                                                                                    boolean includeAncestors,
                                                                                    boolean includeDescendants,
                                                                                    boolean includePresynapticPartners,
                                                                                    boolean includePostsynapticPartners,
                                                                                    boolean includeElectricalPartners,
                                                                                    boolean includeNeuromuscularPartners,
                                                                                    OrganismDataType intendedResultsType);

    /**
     *
     * @param searchString
     * @param isSearchTermGene
     * @param isSearchTermAnatomy
     * @param intendedResultsType
     * @return
     */
    AbstractMap.SimpleEntry<OrganismDataType, List<String>> executeGeneSearch(String searchString,
                                                                              boolean includeAncestors,
                                                                              boolean includeDescendants,
                                                                              boolean isSearchTermGene,
                                                                              boolean isSearchTermAnatomy,
                                                                              OrganismDataType intendedResultsType);
}
