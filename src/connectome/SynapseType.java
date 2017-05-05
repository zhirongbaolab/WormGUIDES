/*
 * Bao Lab 2016
 */

package connectome;

import static java.util.Objects.requireNonNull;

/**
 * The 4 categories of synapses
 */
public enum SynapseType {

    S_PRESYNAPTIC("S presynaptic"),
    R_POSTSYNAPTIC("R postsynaptic"),
    EJ_ELECTRICAL("EJ electrical"),
    NMJ_NEUROMUSCULAR("Nmj neuromuscular");

    private final String description;
    private boolean isPoyadic;
    private boolean isMonadic;

    SynapseType() {
        this("");
        this.isPoyadic = false;
        this.isMonadic = false;
    }

    SynapseType(final String description) {
        this.description = requireNonNull(description);
        this.isPoyadic = false;
        this.isMonadic = false;
    }

    public void setPoyadic() {
        this.isPoyadic = true;
        this.isMonadic = false;
    }

    public void setMonadic() {
        this.isPoyadic = false;
        this.isMonadic = true;
    }

    public boolean isMonadic() {
        return isMonadic;
    }

    public boolean isPoyadic() {
        return isPoyadic;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
