/*
 * Bao Lab 2016
 */

package wormguides.models.anatomy;

/**
 * Anatomy terms which constitute special cases in the info window
 */
public enum AnatomyTerm {

    AMPHID_SENSILLA(
            "Amphid Sensilla",
            "The amphids are a pair of laterally located sensilla in the head and are the worms primary chemosensory "
                    + "organs.");

    private final String term;

    private final String description;

    AnatomyTerm(String term, String description) {
        this.term = term;
        this.description = description;
    }

    public String getTerm() {
        return term;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return new StringBuilder(term).append(": ")
                .append(description)
                .toString();
    }
}
