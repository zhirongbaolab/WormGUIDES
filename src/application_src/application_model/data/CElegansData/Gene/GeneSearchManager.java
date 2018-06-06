/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.Gene;

import java.util.*;

import application_src.application_model.data.OrganismDataType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import static java.util.Objects.requireNonNull;

/**
 * Service that returns the cells with a certain gene expression. This is a wrapper for the actual HTTP request method.
 */
public class GeneSearchManager {

    /** Map of previously fetched genes (in lower case) to their results:
     * The string will be the actual searched term, and the OrganismDataType
     * will indicate whether this is a gene and the results are anatomy terms,
     * or an anatomy term and the results are genes */
    private static Map<String, AbstractMap.SimpleEntry<OrganismDataType, List<String>>> geneResultsCache;

    private static String searchTerm;

    /** search options that are required by the gene search function defined in {@Link OrganismSearch} */
    private static boolean includeAncestors;
    private static boolean includeDescendants;
    private static boolean isSearchTermGene;
    private static boolean isSearchTermAnatomy;
    private static OrganismDataType intendedResultsType;

    private static BooleanProperty geneResultsUpdatedFlag;

    /**
     * Constructor
     */
    public static void init() {
        geneResultsCache = new HashMap<>();
        searchTerm = "";
        includeAncestors = false;
        includeDescendants = false;
        isSearchTermGene = false;
        isSearchTermAnatomy = false;
        intendedResultsType = OrganismDataType.GENE;

        geneResultsUpdatedFlag = new SimpleBooleanProperty();
        geneResultsUpdatedFlag.set(false);
    }

    /**
     * Inserts a gene and its expressing cells into the results cache. This is used when new results need to be
     * fetched using {@link } for ruled added during URL loading, without using this service as a
     * wrapper.
     *
     * @param gene the gene
     * @param results its expressing cells
     */
    public static void cacheGeneResults(final String gene, final AbstractMap.SimpleEntry<OrganismDataType, List<String>> results) {
        if (gene != null && results != null) {
            geneResultsCache.put(gene, results);
            geneResultsUpdatedFlag.set(true);
        }
        geneResultsUpdatedFlag.set(false);
    }

    public static void setSearchTerm(String term) { searchTerm = term; }

    public static void setSearchOptions(boolean includeAncestorsParam, boolean includeDescendantsParam, boolean isSearchTermGeneParam, boolean isSearchTermAnatomyParam, OrganismDataType intendedResultsTypeParam) {
        includeAncestors = includeAncestorsParam;
        includeDescendants = includeDescendantsParam;
        isSearchTermGene = isSearchTermGeneParam;
        isSearchTermAnatomy = isSearchTermAnatomyParam;
        intendedResultsType = intendedResultsTypeParam;
    }

    /**
     * Retrieves the cells with the specified gene expression if the results were previously fetched. The caller
     * should null-check the results in case that the queried has not been previously searched successfully.
     *
     * @param term
     *         the queried gene or anatomy term
     *
     * @return cells with that gene expression
     */
    public static List<String> getPreviouslyFetchedGeneResults(String term) {
        term = term.trim().toLowerCase();
        if (geneResultsCache.containsKey(term)) {
            return new ArrayList<>(geneResultsCache.get(term).getValue());
        }
        return null;
    }

    public static Map<String, AbstractMap.SimpleEntry<OrganismDataType, List<String>>> getGeneResultsCache() { return geneResultsCache; }
    public static String getSearchTerm() { return searchTerm; }
    public static boolean getIncludeAncestorsParam() { return includeAncestors; }
    public static boolean getIncludeDescendantsParam() { return includeDescendants; }
    public static boolean getIsSearchTermGene() { return isSearchTermGene; }
    public static boolean getIsSearchTermAnatomy() { return isSearchTermAnatomy; }
    public static OrganismDataType getIntendedResultsType() { return intendedResultsType; }

}