package application_src.application_model.data.GenericData;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import javafx.scene.control.TreeItem;


/**
 * This class is used to build the underlying tree structure for lineage tree window with generic cell names.
 */
public class GenericLineageTree {
    private static final String PLACEHOLDER_START = "Pl";
    private static final String UNLINEAGED_START = "Nuc";
    private static final String UNDERSCORE = "_";

    /** Maps a lower case cell name to its tree node */
    protected static final Map<String, TreeItem<String>> nameNodeHash = new HashMap<>();
    private static boolean isSulstonMode;
    private static String[] cellsInFirstFrame;
    private final String[] allCellNames;

    protected TreeItem<String> root;

    public GenericLineageTree(final String[] allCellNames, final String[] cellsInFirstFrame, final boolean isSulstonMode) {
        if (allCellNames != null) {
            this.allCellNames = allCellNames;
        } else {
            this.allCellNames = new String[1];
        }

        GenericLineageTree.cellsInFirstFrame = cellsInFirstFrame;
        GenericLineageTree.isSulstonMode = isSulstonMode;

        if (!isSulstonMode) {
            //create root "fake" cell
            root = new TreeItem<>( PLACEHOLDER_START + UNDERSCORE + "r");
            nameNodeHash.put(root.getValue().toLowerCase(), root);

            //create more layers of "fake" cells such that all subtrees can be connect together
            int numStartingCell = cellsInFirstFrame.length;
            if (numStartingCell > 2) {
                ArrayDeque<TreeItem<String>> parentLayer = new ArrayDeque<>();
                ArrayList<TreeItem<String>> childLayer = new ArrayList<>();
                int pl_cell_index = 1;
                parentLayer.add(root);
                while (childLayer.size() * 2 < numStartingCell) {
                    //if parentLayer is empty, put current childLayer as parentLayer and clear childLayer
                    if (parentLayer.size() == 0) {
                        for (TreeItem<String> t : childLayer) {
                            parentLayer.add(t);
                        }
                        childLayer.clear();
                    }
                    TreeItem<String> parent = parentLayer.poll();
                    TreeItem<String> child1 = makeTreeItem(PLACEHOLDER_START + UNDERSCORE + pl_cell_index++);
                    parent.getChildren().add(child1);
                    childLayer.add(child1);
                    if (childLayer.size() * 2 >= numStartingCell) {
                        break;
                    }
                    TreeItem<String> child2 = makeTreeItem(PLACEHOLDER_START + UNDERSCORE + pl_cell_index++);
                    parent.getChildren().add(child2);
                    childLayer.add(child2);
                }

                //adding the roots of all subtrees
                for (int i = 0; i < cellsInFirstFrame.length; i++) {
                    childLayer.get(i/2).getChildren().add(makeTreeItem(cellsInFirstFrame[i]));
                }
            } else {
                for (String name:cellsInFirstFrame) {
                    root.getChildren().add(makeTreeItem(name));
                }
            }
        }
        addAllCells();
    }


    //defualt constructor
    public GenericLineageTree() {
        this.allCellNames = new String[1];
    };


    /**
     * Adds tree nodes for all cells in 'allCellNames'
     */
    private void addAllCells() {
        for (String name : allCellNames) {
            //check if the cell belong to one of the subtrees
            boolean isContinue = true;
            for (String matchName:cellsInFirstFrame) {
                if (name.contains(matchName)) {
                    isContinue = false;
                    break;
                }
            }
            //if not continue
            if (isContinue) {
                continue;
            }
            addCell(name);
        }
    }


    /**
     * Adds a node to the tree specified by a cell name
     *
     * @param cellName
     *         the cell name
     */
    private void addCell(String cellName) {
        String rootname = "";
        for (String matchName:cellsInFirstFrame){
            if (cellName.contains(matchName)) {
                rootname = matchName.toLowerCase();
                break;
            }
        }
        TreeItem<String> startingNode = nameNodeHash.get(rootname);
        TreeItem<String> parent;

        if (startingNode != null) {
            parent = addCellHelper(cellName, startingNode);
            if (parent != null) {
                parent.getChildren().add(makeTreeItem(cellName));
            }
        }
    }

    /**
     * @param cellName
     *         name of the node we want to fetch the parent for
     * @param node
     *         the node to check, may be the parent node
     *
     * @return parent of the node specified by the name
     */
    protected TreeItem<String> addCellHelper(String cellName, TreeItem<String> node) {
        String currName = node.getValue().toLowerCase();
        cellName = cellName.toLowerCase();

        if (cellName.length() == currName.length() + 1 && cellName.startsWith(currName)) {
            return node;
        }

        for (TreeItem<String> child : node.getChildren()) {
            String childName = child.getValue().toLowerCase();
            if (cellName.startsWith(childName)) {
                return addCellHelper(cellName, child);
            }
        }

        return null;
    }

    /**
     * @param ancestor
     *         the cell name to check
     * @param descendant
     *         the potential descendant
     *
     * @return true if 'ancestor' is the ancestor of 'descendant', false otherwise
     */
    public static boolean isAncestor(final String ancestor, final String descendant) {
        return isDescendant(descendant, ancestor);
    }

    /**
     * @param descendant
     *         the cell name to check
     * @param ancestor
     *         the potential ancestor
     *
     * @return true if 'descendant' is a descendant of 'ancestor', false otherwise
     */
    public static boolean isDescendant(String descendant, String ancestor) {
        descendant = descendant.trim().toLowerCase();
        ancestor = ancestor.trim().toLowerCase();

        if (!nameNodeHash.containsKey(descendant) || !nameNodeHash.containsKey(ancestor)) {
            return false;
        }

        //check if it is placeholder cells;
        if (ancestor.startsWith(PLACEHOLDER_START) || descendant.startsWith(PLACEHOLDER_START)) {
            return false;
        }

        //check if the descendant is the root of subtree
        for (String matchName:cellsInFirstFrame) {
            if (descendant.equals(matchName.toLowerCase())) {
                return false;
            }
        }

        // try to decipher lineage from names
        if (descendant.startsWith(ancestor)
                && descendant.length() > ancestor.length()) {
            return true;
        }

        return false;
    }

    /**
     * @param name
     *         the name to check
     *
     * @return the case-sensitive name of that name
     */
    public static String getCaseSensitiveName(String name) {
        name = name.toLowerCase();
        if (nameNodeHash.get(name) == null) {
            return "'" + name + "' Systematic";
        }
        return nameNodeHash.get(name).getValue();
    }

    /**
     * @param name
     *         the name to check
     *
     * @return null or the node associate with the name
     */
    public static TreeItem<String> getNodeWithName(String name) {
        name = name.toLowerCase();
        if (nameNodeHash.get(name) == null) {
            return null;
        }
        return nameNodeHash.get(name);
    }

    public TreeItem<String> getRoot() {
        return root;
    }

    public TreeItem<String> makeTreeItem(String name) {
        final TreeItem<String> node = new TreeItem<>(name);
        nameNodeHash.put(name.toLowerCase(), node);
        return node;
    }

    public static boolean isLineageNameInTree(String name) {
        return nameNodeHash.containsKey(name.toLowerCase());
    }


}
