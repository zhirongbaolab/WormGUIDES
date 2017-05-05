/*
 * Bao Lab 2016
 */

package wormguides.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static java.util.Objects.requireNonNull;

import static search.SearchUtil.getCellsWithGeneExpression;

/**
 * Service that returns the cells with a certain gene expression. This is a wrapper for the actual HTTP request method.
 */
public class GeneSearchService extends Service<List<String>> {

    /** Map of previously fetched genes (in lower case) to their results */
    private final Map<String, List<String>> geneResultsCache;

    /** The searched gene */
    private String searchedGene;

    /**
     * Constructor
     */
    public GeneSearchService() {
        geneResultsCache = new HashMap<>();
        searchedGene = "";
    }

    /**
     * Inserts a gene and its expressing cells into the results cache. This is used when new results need to be
     * fetched using {@link search.WormBaseQuery} for ruled added during URL loading, without using this service as a
     * wrapper.
     *
     * @param gene the gene
     * @param results its expressing cells
     */
    public void cacheGeneResults(final String gene, final List<String> results) {
        if (gene != null && results != null) {
            geneResultsCache.put(gene, results);
        }
    }

    /**
     * Retrieves the cells with the specified gene expression if the results were previously fetched. The caller
     * should null-check the results in case that the queried has not been previously searched successfully.
     *
     * @param gene
     *         the queried gene
     *
     * @return cells with that gene expression
     */
    public List<String> getPreviouslyFetchedGeneResults(String gene) {
        gene = gene.trim().toLowerCase();
        if (geneResultsCache.containsKey(gene)) {
            return new ArrayList<>(geneResultsCache.get(gene));
        }
        return null;
    }

    /**
     * Resets the searched gene to an empty string
     */
    public void resetSearchedGene() {
        searchedGene = "";
    }

    /**
     * @return the previously searched gene
     */
    public String getSearchedGene() {
        return searchedGene;
    }

    /**
     * Issues a gene search for the specified gene
     *
     * @param searchedGene
     *         the gene to search, non null, non empty, and in the gene format SOME_STRING-SOME_NUMBER
     */
    public void setSearchedGene(final String searchedGene) {
        if (!requireNonNull(searchedGene).isEmpty()) {
            this.searchedGene = searchedGene.toLowerCase();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return cells with the searched gene expression
     */
    @Override
    protected final Task<List<String>> createTask() {
        return new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                if (geneResultsCache.containsKey(searchedGene)) {
                    return geneResultsCache.get(searchedGene);
                }
                final List<String> results = getCellsWithGeneExpression(searchedGene);
                // save results in cache
                geneResultsCache.put(searchedGene, results);
                return results;
            }
        };
    }
}