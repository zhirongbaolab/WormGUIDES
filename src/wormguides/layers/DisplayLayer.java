/*
 * Bao Lab 2016
 */

package wormguides.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import wormguides.models.colorrule.Rule;

import static java.util.Objects.requireNonNull;

/**
 * This class is the controller for the 'Display' tab where the list of rules are shown. It contains an
 * {@link ArrayList} of color rules that are internal to the application as well as an {@link ObservableList} of
 * color rules that display the rules used at that time in the 3d subscene.
 * <p>
 * The current list of rules changes on story change. If no story is active, then the internal rules are copied to
 * the current list and used. All changes made to the rules displayed in the tab are reflected in the current rules
 * list. On context change (making a story active/inactive), the rules in the current list are stored back into the
 * item that no longer has context (whether it is the internal rules or the story's rules).
 * <p>
 * The internal rules are the rules used when no story is active. On startup, the internal rules are the default
 * rules added by {@link SearchLayer#addDefaultInternalColorRules()}.
 *
 * @see Rule
 */
public class DisplayLayer {

    private final List<Rule> internalRulesList;
    private final ObservableList<Rule> currentRulesList;
    private final Map<Rule, Button> buttonMap;

    /**
     * Constructor
     *
     * @param useInternalRulesFlag
     *         true when the application should use the program's internal color rules (such as in the case where
     *         no story is active), false otherwise
     */
    public DisplayLayer(
            final ObservableList<Rule> rulesList,
            final BooleanProperty useInternalRulesFlag,
            final BooleanProperty rebuildSubsceneFlag) {
        requireNonNull(rulesList);
        requireNonNull(useInternalRulesFlag);
        requireNonNull(rebuildSubsceneFlag);

        internalRulesList = new ArrayList<>();
        buttonMap = new HashMap<>();


        this.currentRulesList = rulesList;
        this.currentRulesList.addListener((ListChangeListener<Rule>) change -> {
            while (change.next()) {
                if (!change.wasUpdated()) {
                    // added to current list
                    for (Rule rule : change.getAddedSubList()) {
                        buttonMap.put(rule, rule.getDeleteButton());
                        rule.getDeleteButton().setOnAction(event -> {
                            currentRulesList.remove(rule);
                            buttonMap.remove(rule);
                            rebuildSubsceneFlag.set(true);
                        });
                    }
                }
            }
        });

        useInternalRulesFlag.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // using internal rules, copy all internal rules to current list
                currentRulesList.clear();
                currentRulesList.addAll(internalRulesList);
            } else {
                // not using internal rules, copy all current rule changes back to internal list
                internalRulesList.clear();
                internalRulesList.addAll(currentRulesList);
            }
        });
    }

    /**
     * Renderer for rules in ListView's in Layers tab
     */
    public Callback<ListView<Rule>, ListCell<Rule>> getRuleCellFactory() {
        return new Callback<ListView<Rule>, ListCell<Rule>>() {
            @Override
            public ListCell<Rule> call(ListView<Rule> param) {
                final ListCell<Rule> cell = new ListCell<Rule>() {
                    @Override
                    protected void updateItem(final Rule item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            setGraphic(item.getGraphic());
                        } else {
                            setGraphic(null);
                        }
                        setPickOnBounds(false);
                    }
                };
                return cell;
            }
        };
    }
}
