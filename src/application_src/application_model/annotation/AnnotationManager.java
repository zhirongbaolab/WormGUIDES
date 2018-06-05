package application_src.application_model.annotation;


import application_src.application_model.annotation.color.Rule;

import application_src.application_model.search.SearchConfiguration.SearchOption;
import application_src.application_model.search.SearchConfiguration.SearchType;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

import java.util.*;

import static application_src.application_model.data.CElegansData.SulstonLineage.LineageTree.getCaseSensitiveName;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_BODY;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;
import static application_src.application_model.search.SearchConfiguration.SearchType.*;
import static java.util.Arrays.asList;
import static javafx.scene.paint.Color.DARKSEAGREEN;
import static javafx.scene.paint.Color.web;

/**
 * This class is one of two main interfaces between the model-agnostic Search pipeline
 * and the specifics of the model displayed in the window.
 *
 * The purpose of the class is to take the list of search-model correspondences
 * (established in {@Link application_src.application_model.search.ModelSearch.EstablishCorrespondence})
 * and other metadata about the search and dispatch it to the creation of the correct annotation.
 *
 * This result is then given back to the correct controller to be applied to the model.
 */
public class AnnotationManager {

    // primary location for rules list
    ObservableList<Rule> rulesList;

    // links to downstream annotation class


    public AnnotationManager(ObservableList<Rule> rulesList) {
        this.rulesList = rulesList;

    }

    ///////////////////////////////////////////// ANNOTATION METHODS ///////////////////////////////////
    /**
     * Adds a color rule to the currently active rules list. Adding a rule does not rebuild the subscene. In order
     * for any changes to be visible, the calling class must set the 'rebuildSubsceneFlag' to true or set a property
     * that triggers a subscene rebuild.
     *
     * @param searchType
     *         the search type
     * @param searched
     *         the searched term
     * @param color
     *         the color to apply to the cells in the search results
     * @param options
     *         the search options
     *
     * @return the rule added to the active rules list
     */
    public Rule addColorRule(
            final SearchType searchType,
            String searched,
            final Color color,
            final SearchOption... options) {
        return addColorRule(searchType, searched, color, new ArrayList<>(asList(options)));
    }
    /**
     * Adds a color rule to the currently active rules list. Adding a rule does not rebuild the subscene. In order
     * for any changes to be visible, the calling class must set the 'rebuildSubsceneFlag' to true or set a property
     * that triggers a subscene rebuild.
     *
     * @param searchType
     *         the search type
     * @param searched
     *         the searched term
     * @param color
     *         the color to apply to the cells in the search results
     * @param options
     *         the search options
     *
     * @return the rule added to the active rules list
     */
    public Rule addColorRule(
            final SearchType searchType,
            final String searched,
            final Color color,
            List<SearchOption> options) {

        // default search options is cell
        if (options == null) {
            options = new ArrayList<>();
            options.add(CELL_NUCLEUS);
        }

        final Rule rule = new Rule(
                rebuildSubsceneFlag,
                createRuleLabel(searched, searchType),
                color,
                searchType,
                options);
        rule.setCells(getCellsList(searchType, searched));
        rulesList.add(rule);
        searchResultsList.clear();
        return rule;
    }

    /**
     * Adds a color rule to the currently active rules list, specified only by URL.
     * This is not a searchable rule. It is a Manually Specified List (MSL) that can
     * only be defined in URL format. See documentation in code_README
     *
     * @param names
     * @param color
     * @param options
     * @return
     */
    public Rule addColorRule(
            final List<String> names,
            final Color color,
            final List<SearchOption> options) {

        //
        final Rule rule = new Rule(
                rebuildSubsceneFlag,
                createRuleLabel(names),
                color,
                SearchType.MSL,
                options
        );
        rule.setCells(getCellsList(names));
        rulesList.add(rule);
        return rule;

    }

    /**
     * Adds a color rule for a multicellular structure to the currently active rules list. Adding a rule does not
     * rebuild the subscene. In order for any changes to be visible, the calling class must set the
     * 'rebuildSubsceneFlag' to true or set a property that triggers a subscene rebuild.
     *
     * @param searched
     *         the searched structure
     * @param color
     *         the color to apply to the structure
     *
     * @return the multicellular structure rule added
     */
    public Rule addStructureRuleBySceneName(final String searched, final Color color) {
        return addColorRule(STRUCTURE_BY_SCENE_NAME, searched, color, new ArrayList<>());
    }

    public Rule addGeneColorRuleFromUrl(final String searched, final Color color, final SearchOption... options) {
        return addGeneColorRuleFromUrl(searched, color, new ArrayList<>(asList(options)));
    }

    public Rule addGeneColorRuleFromUrl(final String searched, final Color color, List<SearchOption> options) {
        if (options == null) {
            options = new ArrayList<>();
            options.add(CELL_NUCLEUS);
        }
        final String label = createRuleLabel(searched, GENE);
        final Rule rule = new Rule(rebuildSubsceneFlag, searched, color, GENE, options);
        final List<String> cells = geneSearchService.getPreviouslyFetchedGeneResults(searched);
        if (cells != null) {
            rule.setCells(cells);
        } else {
            final Service<List<String>> queryService = new Service<List<String>>() {
                public Task<List<String>> createTask() {
                    return new Task<List<String>>() {
                        public List<String> call() {
                            return issueWormBaseGeneQuery(searched);
                        }
                    };
                }
            };
            queryService.setOnSucceeded(event -> {
                final List<String> results = queryService.getValue();
                rule.setCells(results);
                rebuildSubsceneFlag.set(true);
                geneSearchService.cacheGeneResults(searched, results);
            });
            queryService.start();
        }
        rulesList.add(rule);
        return rule;
    }

    /**
     * Adds the app's internal color rules. These rules are used when the active story does not have its own color
     * scheme.
     */
    public void addDefaultInternalColorRules() {
        addColorRule(FUNCTIONAL, "ash", DARKSEAGREEN, CELL_BODY);
        addColorRule(FUNCTIONAL, "rib", web("0x663366"), CELL_BODY);
        addColorRule(FUNCTIONAL, "avg", web("0xb41919"), CELL_BODY);

        addColorRule(FUNCTIONAL, "dd", web("0x4a24c1", 0.60), CELL_BODY);
        addColorRule(FUNCTIONAL, "da", web("0xc56002"), CELL_BODY);

        addColorRule(FUNCTIONAL, "rivl", web("0xff9966"), CELL_BODY);
        addColorRule(FUNCTIONAL, "rivr", web("0xffe6b4"), CELL_BODY);
        addColorRule(FUNCTIONAL, "sibd", web("0xe6ccff"), CELL_BODY);
        addColorRule(FUNCTIONAL, "siav", web("0x99b3ff"), CELL_BODY);

        addColorRule(FUNCTIONAL, "dd1", web("0xb30a95"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "dd2", web("0xb30a95"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "dd3", web("0xb30a95"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "dd4", web("0xb30a95"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "dd5", web("0xb30a95"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "dd6", web("0xb30a95"), CELL_NUCLEUS);

        addColorRule(FUNCTIONAL, "da2", web("0xe6b34d"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "da3", web("0xe6b34d"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "da4", web("0xe6b34d"), CELL_NUCLEUS);
        addColorRule(FUNCTIONAL, "da5", web("0xe6b34d"), CELL_NUCLEUS);

        addStructureRuleBySceneName("lim4_bundle_left", web("0xe6ccff"));
        addStructureRuleBySceneName("lim4_bundle_left", web("0x99b3ff"));
        addStructureRuleBySceneName("lim4_bundle_right", web("0xe6ccff"));
        addStructureRuleBySceneName("lim4_bundle_right", web("0x99b3ff"));
        addStructureRuleBySceneName("lim4_nerve_ring", web("0xff9966"));
        addStructureRuleBySceneName("lim4_nerve_ring", web("0xffe6b4"));
        addStructureRuleBySceneName("Amphid Commissure Right", DARKSEAGREEN);
        addStructureRuleBySceneName("Amphid Commissure Right", web("0x663366"));
        addStructureRuleBySceneName("Amphid Commissure Left", DARKSEAGREEN);
        addStructureRuleBySceneName("Amphid Commissure Left", web("0x663366"));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////




    ////////////////////////////////////////////// UTILITY METHODS //////////////////////////////////////
    /**
     *
     *
     * @param funcName
     * @param isPresynapticTicked
     * @param isPostsynapticTicked
     * @param isElectricalTicked
     * @param isNeuromuscularTicked
     * @return
     */
    private StringBuilder createLabelForConnectomeRule(
            String funcName,
            final boolean isPresynapticTicked,
            final boolean isPostsynapticTicked,
            final boolean isElectricalTicked,
            final boolean isNeuromuscularTicked) {

        final StringBuilder sb = new StringBuilder("'");
        sb.append(funcName.toLowerCase()).append("' Connectome");

        final List<String> types = new ArrayList<>();
        if (isPresynapticTicked) {
            types.add("presynaptic");
        }
        if (isPostsynapticTicked) {
            types.add("postsynaptic");
        }
        if (isElectricalTicked) {
            types.add("electrical");
        }
        if (isNeuromuscularTicked) {
            types.add("neuromuscular");
        }
        if (!types.isEmpty()) {
            sb.append(" - ");

            for (int i = 0; i < types.size(); i++) {
                sb.append(types.get(i));
                if (i != types.size() - 1) {
                    sb.append(", ");
                }
            }
        }

        return sb;
    }

    private String createRuleLabel(List<String> names) {
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append("'");
        for (String name : names) {
            labelBuilder.append(name);
            labelBuilder.append("; ");
        }
        // remove last two characters after last name
        labelBuilder.deleteCharAt(labelBuilder.length()-1);
        labelBuilder.deleteCharAt(labelBuilder.length()-1);

        labelBuilder.append("' ").append(SearchType.MSL.toString());
        return labelBuilder.toString();
    }

    private String createRuleLabel(String searched, final SearchType searchType) {
        searched = searched.trim();
        StringBuilder labelBuilder = new StringBuilder();
        if (searchType != null) {
            if (searchType == LINEAGE) {
                labelBuilder.append(getCaseSensitiveName(searched));
                if (labelBuilder.toString().isEmpty()) {
                    labelBuilder.append(searched);
                }
            } else if (searchType == CONNECTOME) {
                labelBuilder = createLabelForConnectomeRule(
                        searched,
                        presynapticCheckBox.isSelected(),
                        postsynapticCheckBox.isSelected(),
                        neuromuscularCheckBox.isSelected(),
                        electricalCheckBox.isSelected());
            } else {
                labelBuilder.append("'").append(searched).append("' ").append(searchType.toString());
            }
        } else {
            labelBuilder.append(searched);
        }
        return labelBuilder.toString();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
} //end class
