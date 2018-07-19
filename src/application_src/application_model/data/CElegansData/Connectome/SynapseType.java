/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.Connectome;

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
    private boolean isPolyadic;
    private boolean isMonadic;

    SynapseType() {
        this("");
        this.isPolyadic = false;
        this.isMonadic = false;
    }

    SynapseType(final String description) {
        this.description = requireNonNull(description);
        this.isPolyadic = false;
        this.isMonadic = false;
    }

    public void setPolyadic() {
        this.isPolyadic = true;
        this.isMonadic = false;
    }

    public void setMonadic() {
        this.isPolyadic = false;
        this.isMonadic = true;
    }

    public boolean isMonadic() {
        return isMonadic;
    }

    public boolean isPolyadic() {
        return isPolyadic;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
