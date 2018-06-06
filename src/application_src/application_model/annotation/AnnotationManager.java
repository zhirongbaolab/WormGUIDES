package application_src.application_model.annotation;


import application_src.application_model.annotation.color.Rule;

import application_src.application_model.data.CElegansData.Gene.GeneSearchManager;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.SearchConfiguration.SearchOption;
import application_src.application_model.search.SearchConfiguration.SearchType;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

import java.util.*;

import static application_src.application_model.data.CElegansData.Gene.WormBaseQuery.issueWormBaseGeneQuery;
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

    private BooleanProperty rebuildSubsceneFlag;

    // links to downstream annotation class


    public AnnotationManager(ObservableList<Rule> rulesList, BooleanProperty rebuildSubsceneFlag) {
        this.rulesList = rulesList;
        this.rebuildSubsceneFlag = rebuildSubsceneFlag;

    }

    ///////////////////////////////////////////// ANNOTATION METHODS ///////////////////////////////////
    public void updateAnnotation(List<String> entityNames) {

    }

    //////////////////////////////// CREATE COLOR RULES /////////////////////////////////
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
            final List<String> searchResults,
            List<SearchOption> options) {


        // default search options is cell
        if (options == null ) {
            options = new ArrayList<>();
        }

        if (options.isEmpty()) {
            options.add(CELL_NUCLEUS);
        }



        final Rule rule = new Rule(
                rebuildSubsceneFlag,
                createRuleLabel(searched, searchType),
                color,
                searchType,
                options);
        rule.setCells(searchResults);
        rulesList.add(rule);
        return rule;
    }

    public Rule addConnectomeColorRule(
            final String searched,
            final Color color,
            final List<String> searchResults,
            final boolean presynapticTicked,
            final boolean postsynapticTicked,
            final boolean electricalTicked,
            final boolean neuromuscularTicked,
            List<SearchOption> options) {

        // default search options is cell
        if (options == null ) {
            options = new ArrayList<>();
        }

        if (options.isEmpty()) {
            options.add(CELL_NUCLEUS);
        }

        final StringBuilder sb = createLabelForConnectomeRule(
                searched,
                presynapticTicked, postsynapticTicked, electricalTicked, neuromuscularTicked);
        final Rule rule = new Rule(rebuildSubsceneFlag, sb.toString(), color, CONNECTOME, options);
        rule.setCells(searchResults);
        rule.setSearchedText(sb.toString());
        rule.resetLabel(sb.toString());
        rulesList.add(rule);
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
    public Rule addMSLColorRule(
            final List<String> names,
            final Color color,
            final List<String> searchResults,
            final List<SearchOption> options) {


        final Rule rule = new Rule(
                rebuildSubsceneFlag,
                createRuleLabel(names),
                color,
                SearchType.MSL,
                options
        );
        rule.setCells(searchResults);
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
        return addColorRule(STRUCTURE_BY_SCENE_NAME, searched, color, new ArrayList<>(), new ArrayList<>());
    }


    /**
     *
     * @param searched
     * @param color
     * @param options
     * @return
     */
    public Rule addGeneColorRuleFromUrl(final String searched, final Color color,
                                        List<String> searchResults, List<SearchOption> options) {
        // default search options is cell
        if (options == null) {
            options = new ArrayList<>();
        }

        if (options.isEmpty()) {
            options.add(CELL_NUCLEUS);
        }

        final String label = createRuleLabel(searched, GENE);
        final Rule rule = new Rule(rebuildSubsceneFlag, label, color, GENE, options);
        rule.setCells(searchResults);
        return rule;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////




    ////////////////////////////////////////////// LABEL BUILDING METHODS //////////////////////////////////////
    /**
     * Used for Manually Specified List rules
     *
     * @param names
     * @return
     */
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

    /**
     * Used for all non-special case rules
     *
     * @param searched
     * @param searchType
     * @return
     */
    private String createRuleLabel(String searched, final SearchType searchType) {
        searched = searched.trim();
        StringBuilder labelBuilder = new StringBuilder();
        if (searchType != null) {
            if (searchType == LINEAGE) {
                labelBuilder.append(getCaseSensitiveName(searched));
                if (labelBuilder.toString().isEmpty()) {
                    labelBuilder.append(searched);
                }
            } else {
                labelBuilder.append("'").append(searched).append("' ").append(searchType.toString());
            }
        } else {
            labelBuilder.append(searched);
        }
        return labelBuilder.toString();
    }

    /**
     * Used strictly for connectome rules
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
    //////////////////////////////////////////////////////////////////////////////////////////////

    public ObservableList<Rule> getRulesList() { return this.rulesList; }
} //end class





/////////////////// This may be useful at some point. It fell out of use and was moved here for storage 06/18
//    /**
//     * Adds the app's internal color rules. These rules are used when the active story does not have its own color
//     * scheme.
//     */
//    public void addDefaultInternalColorRules() {
//        addColorRule(FUNCTIONAL, "ash", DARKSEAGREEN, new ArrayList<>(), CELL_BODY);
//        addColorRule(FUNCTIONAL, "rib", web("0x663366"), new ArrayList<>(), CELL_BODY);
//        addColorRule(FUNCTIONAL, "avg", web("0xb41919"), new ArrayList<>(), CELL_BODY);
//
//        addColorRule(FUNCTIONAL, "dd", web("0x4a24c1", 0.60), new ArrayList<>(), CELL_BODY);
//        addColorRule(FUNCTIONAL, "da", web("0xc56002"), new ArrayList<>(), CELL_BODY);
//
//        addColorRule(FUNCTIONAL, "rivl", web("0xff9966"), new ArrayList<>(), CELL_BODY);
//        addColorRule(FUNCTIONAL, "rivr", web("0xffe6b4"), new ArrayList<>(), CELL_BODY);
//        addColorRule(FUNCTIONAL, "sibd", web("0xe6ccff"), new ArrayList<>(), CELL_BODY);
//        addColorRule(FUNCTIONAL, "siav", web("0x99b3ff"), new ArrayList<>(), CELL_BODY);
//
//        addColorRule(FUNCTIONAL, "dd1", web("0xb30a95"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "dd2", web("0xb30a95"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "dd3", web("0xb30a95"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "dd4", web("0xb30a95"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "dd5", web("0xb30a95"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "dd6", web("0xb30a95"), new ArrayList<>(), CELL_NUCLEUS);
//
//        addColorRule(FUNCTIONAL, "da2", web("0xe6b34d"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "da3", web("0xe6b34d"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "da4", web("0xe6b34d"), new ArrayList<>(), CELL_NUCLEUS);
//        addColorRule(FUNCTIONAL, "da5", web("0xe6b34d"), new ArrayList<>(), CELL_NUCLEUS);
//
//        addStructureRuleBySceneName("lim4_bundle_left", web("0xe6ccff"));
//        addStructureRuleBySceneName("lim4_bundle_left", web("0x99b3ff"));
//        addStructureRuleBySceneName("lim4_bundle_right", web("0xe6ccff"));
//        addStructureRuleBySceneName("lim4_bundle_right", web("0x99b3ff"));
//        addStructureRuleBySceneName("lim4_nerve_ring", web("0xff9966"));
//        addStructureRuleBySceneName("lim4_nerve_ring", web("0xffe6b4"));
//        addStructureRuleBySceneName("Amphid Commissure Right", DARKSEAGREEN);
//        addStructureRuleBySceneName("Amphid Commissure Right", web("0x663366"));
//        addStructureRuleBySceneName("Amphid Commissure Left", DARKSEAGREEN);
//        addStructureRuleBySceneName("Amphid Commissure Left", web("0x663366"));
//    }