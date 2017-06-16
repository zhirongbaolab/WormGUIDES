/*
 * Bao Lab 2017
 */

package wormguides.models.subscenegeometry;

import static java.util.Objects.requireNonNull;

/**
 * Node in the structure tree (with headings)
 */
public class StructureTreeNode {

    /** True if the leaf node is a category heading, false otherwise */
    private final boolean isHeading;

    /**
     * Name shown on the tree node, whether it is a structure name on a leaf node or a category name on a parent node
     */
    private final String nodeText;

    /**
     * Scene name of the element. This is the lineage name for elements that are cell bodies with a corresponding
     * nuclei.
     */
    private final String sceneName;

    /**
     * Constructure
     *
     * @param isCategory
     *         true if this tree node is a category heading node, false otherwise
     * @param nodeText
     *         the text shown on the structure tree node in the tree view. This is either the category name or
     *         the shown structure name
     * @param sceneName
     *         the scene name  of the element. This is the lineage name for elements that are cell bodies with a
     *         correponding nuclei
     */
    public StructureTreeNode(final boolean isCategory, final String nodeText, final String sceneName) {
        this.isHeading = isCategory;
        this.nodeText = requireNonNull(nodeText);
        this.sceneName = requireNonNull(sceneName);
    }

    /**
     * @return the text in the structure tree node
     */
    public String getNodeText() {
        return nodeText;
    }

    /**
     * @return the scene name of the element specified by this tree node
     */
    public String getSceneName() {
        return sceneName;
    }

    /**
     * @return true if the node is a heading node (not a leaf node), false otherwise
     */
    public boolean isHeading() {
        return isHeading;
    }

    /**
     * @return true if the node is a leaf node (not a heading node), false otherwise
     */
    public boolean isLeafNode() {
        return !isHeading;
    }
}