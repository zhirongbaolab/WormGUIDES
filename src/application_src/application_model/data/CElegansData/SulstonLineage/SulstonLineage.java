/**
 * Bao Lab 06/2018
 * Author: Braden Katzman
 */

package application_src.application_model.data.CElegansData.SulstonLineage;

import javafx.scene.control.TreeItem;

/**
 * The class represents the necessary information for the SulstonLineage. The Sulston Lineage tree
 * can be used in an optimized and efficient way by relying on the correspondence between terminal
 * cell names (in the PartsList) and cell death names (in the CellDeaths.csv) and the root of the lineage
 * tree, P0. Terminal and cell deaths names are the results of iteratively appending characters (which
 * correspond to the axis of division) to mother cells names in their daughter cells. Thus, any name in
 * the lineage tree can be identified, and its ancestors and descendants queried, by using a method of
 * appending and peeling characters in the direction of a root ancestor cell or a terminal descendant. There
 * are, however, special cases where lineage names do not follow this paradigm. These occur in the first 5 rounds
 * of division and are confined largely to the right side of the tree (see below for details). Thus, we've
 * represented these special cases and their lineage relationships here to be used by the CElegansSearch.java
 * class in addition to leveraging the PartsList and CellDeaths as described above.
 *
 * This paradigm allows us to avoid building an explicit tree data structure, leveraging the format of the underlying
 * names themselves instead.
 */
public class SulstonLineage {

    /**
     * The sulston lineage contains 'special cases' that can't be queried using the method of
     * string parsing that's possible throughout the rest of the sulston lineage. The special cases start with P0
     * and extend through the 5th round of divisions, with the exception of P0-AB, these special cases are
     * all confined to the right side of the Sulston Lineage Tree. They are as follows, order by PARENT-DAUGHTER1/DAUGHTER2
     * P0-AB/P1
     * P1-EMS/P2
     * EMS-E/MS
     * P2-C/P3
     * P3-D/P4
     * P4-Z3/Z2
     */
    private static final TreeItem<String> P0_root = new TreeItem<String>("P0");
    private static final TreeItem<String> AB = new TreeItem<String>("AB");
    private static final TreeItem<String> P1 = new TreeItem<String>("P1");
    private static final TreeItem<String> P2 = new TreeItem<String>("P2");
    private static final TreeItem<String> EMS = new TreeItem<String>("EMS");
    private static final TreeItem<String> E = new TreeItem<String>("E");
    private static final TreeItem<String> MS = new TreeItem<String>("MS");
    private static final TreeItem<String> C = new TreeItem<String>("C");
    private static final TreeItem<String> P3 = new TreeItem<String>("P3");
    private static final TreeItem<String> D = new TreeItem<String>("D");
    private static final TreeItem<String> P4 = new TreeItem<String>("P4");
    private static final TreeItem<String> Z3 = new TreeItem<String>("Z3");
    private static final TreeItem<String> Z2 = new TreeItem<String>("Z2");

    public static void init() {
        // initialize the special cases tree
        P4.getChildren().add(Z3);
        P4.getChildren().add(Z2);

        P3.getChildren().add(D);
        P3.getChildren().add(P4);

        P2.getChildren().add(C);
        P2.getChildren().add(P3);

        EMS.getChildren().add(E);
        EMS.getChildren().add(MS);

        P1.getChildren().add(EMS);
        P1.getChildren().add(P2);

        P0_root.getChildren().add(AB);
        P0_root.getChildren().add(P1);
    }
}