/*
 * Bao Lab 2016
 */

package connectome;

/**
 * The Synapse between two cells
 */
public class NeuronalSynapse {

    private String cell1;
    private String cell2;

    private SynapseType synapseType;
    private int numberOfSynapses;

    /**
     * Class constructor
     *
     * @param cell1
     *         functional name of first cell
     * @param cell2
     *         functional name of other cell
     * @param synapseType
     *         the synapse type between the two cells
     * @param numberOfSynapses
     *         the number of synapses
     */
    public NeuronalSynapse(String cell1, String cell2, SynapseType synapseType, int numberOfSynapses) {
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.synapseType = synapseType;
        this.numberOfSynapses = numberOfSynapses;
    }

    public String getCell1() {
        return cell1;
    }

    public String getCell2() {
        return cell2;
    }

    public SynapseType getSynapseType() {
        return synapseType;
    }

    public int numberOfSynapses() {
        return numberOfSynapses;
    }
}