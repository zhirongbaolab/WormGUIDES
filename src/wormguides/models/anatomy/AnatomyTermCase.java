/*
 * Bao Lab 2016
 */

package wormguides.models.anatomy;

/**
 * Defines a case i.e. an info window page type corresponding to an AnatomyTerm enum
 * <p>
 * This class holds in the information about the anatomy term to be used to generate
 * an HTML page for the info window
 * <p>
 * Class type for the Enum AnatomyTerm in wormguides top level directory
 *
 * @author bradenkatzman
 */
public abstract class AnatomyTermCase {

    private String name;
    private String description;

    /**
     * Sets the term based on the term in the enum
     *
     * @param term
     *         the defining term
     */
    public AnatomyTermCase(AnatomyTerm term) {
        this.name = term.getTerm();
        this.description = term.getDescription();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
