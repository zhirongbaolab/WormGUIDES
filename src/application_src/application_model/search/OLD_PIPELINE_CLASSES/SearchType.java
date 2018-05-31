/*
 * Bao Lab 2017
 */

package application_src.application_model.search.OLD_PIPELINE_CLASSES;

/**
 * Types of search being made. The type defines the database that the application queries, whether it is the lineage
 * data, parts list, connectome, list of scene elements, or list of cell cases.
 */
public enum SearchType {

    /** Search for cells with a specified lineage name */
    LINEAGE("SulstonLineage"),

    /** Search for cells with the functional name with a specified prefix */
    FUNCTIONAL("Functional"),

    /** Search for cells with a specified {@link partslist.PartsList} description */
    DESCRIPTION("\"PartsList\" Description"),

    /** Search for cells with a specified gene expression using WormBase */
    GENE("Gene"),

    /**
     * Search for cells with wirings (pre-synaptic, post-synaptic, electrical, or neuromuscular) to the
     * cell with a specified lineage name
     */
    CONNECTOME("Connectome"),

    /** Search for cells contained in specified multicellular structure(s) */
    MULTICELLULAR_STRUCTURE_CELLS("Multicellular Structure Cells"),

    /** Search for structure(s) with a specified scene name */
    STRUCTURE_BY_SCENE_NAME("Structure Scene Name"),

    /** Search for structure(s) under a specified heading */
    STRUCTURES_BY_HEADING("Structures Heading"),

    /** Search for the neighboring cells of the cell with a specified lineage name */
    NEIGHBOR("Neighbor"),

    /** Manually Specified List. Non-searchable. The search type denoted in a {@link wormguides.models.colorrule.Rule}
     * that is created from a URL and is not searchable */
    MSL("Manually Specified List");

    private final String description;

    SearchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

	@Override
	public String toString() {
        return getDescription();
    }
}