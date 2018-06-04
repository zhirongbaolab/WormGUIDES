/*
 * Bao Lab 2017
 */

package application_src.application_model.data.CElegansData.Connectome;
import java.util.List;

import static application_src.application_model.loaders.ConnectomeLoader.loadConnectome;

/**
 * Underlying model of the all neuronal connections. It holds a list of {@link NeuronalSynapse}s that define the
 * wiring between two terminal cells
 */
public class Connectome {

    // synapse types as strings for search logic
    public static final String S_PRESYNAPTIC_DESCRIPTION = "S presynaptic";
    public static final String R_POSTSYNAPTIC_DESCRIPTION = "R postsynaptic";
    public static final String EJ_ELECTRICAL_DESCRIPTION = "EJ electrical";
    public static final String NMJ_NEUROMUSCULAR_DESCRPITION = "Nmj neuromuscular";

    private static List<NeuronalSynapse> synapses;

    public static void init() {
        synapses = loadConnectome();
    }

    public static List<NeuronalSynapse> getSynapseList() {
        return synapses;
    }

 }