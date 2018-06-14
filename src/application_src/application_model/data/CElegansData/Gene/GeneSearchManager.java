/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.Gene;

import java.util.*;

import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import static java.util.Objects.requireNonNull;

/**
 * Service that returns the cells with a certain gene expression. This is a wrapper for the actual HTTP request method.
 */
public class GeneSearchManager extends Service<Void> {

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

    private CElegansSearch cElegansSearchPipeline;

    /**
     * Constructor
     */
    public GeneSearchManager(CElegansSearch cElegansSearchPipeline) {
        this.cElegansSearchPipeline = cElegansSearchPipeline;

        geneResultsCache = new HashMap<>();
        searchTerm = "";
        includeAncestors = false;
        includeDescendants = false;
        isSearchTermGene = false;
        isSearchTermAnatomy = false;
        intendedResultsType = OrganismDataType.GENE;
    }

    public void setSearchTerm(String term) { searchTerm = term; }

    public void setSearchOptions(boolean includeAncestorsParam, boolean includeDescendantsParam, boolean isSearchTermGeneParam, boolean isSearchTermAnatomyParam, OrganismDataType intendedResultsTypeParam) {
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
    public AbstractMap.SimpleEntry<OrganismDataType, List<String>> getPreviouslyFetchedGeneResults(String term) {
        term = term.trim().toLowerCase();
        if (geneResultsCache.containsKey(term)) {
            return geneResultsCache.get(term);
        }
        return null;
    }

    @Override
    protected final Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (!geneResultsCache.containsKey(searchTerm)) {

                    final AbstractMap.SimpleEntry<OrganismDataType, List<String>> results = cElegansSearchPipeline.executeGeneSearch(searchTerm,
                            includeAncestors,
                            includeDescendants,
                            isSearchTermGene,
                            isSearchTermAnatomy,
                            intendedResultsType);

                    // save results in cache
                    System.out.println("caching results of size:" + results.getValue().size() + " with search term: " + searchTerm);
                    geneResultsCache.put(searchTerm.toLowerCase(), results);
                    System.out.println("returning results from gene search thread");
                }
                return null;
            }
        };
    }
}