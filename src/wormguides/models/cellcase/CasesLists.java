/*
 * Bao Lab 2017
 */

package wormguides.models.cellcase;

import java.util.ArrayList;
import java.util.List;

import wormguides.models.anatomy.AmphidSensillaTerm;
import wormguides.models.anatomy.AnatomyTerm;
import wormguides.models.anatomy.AnatomyTermCase;
import wormguides.view.infowindow.InfoWindow;

import static java.util.Objects.requireNonNull;

import static partslist.PartsList.getLineageNamesByFunctionalName;
import static wormguides.models.anatomy.AnatomyTerm.AMPHID_SENSILLA;

/**
 * List of terminal cell cases (neurons) or non-terminal cell cases. These cases generated for view in the info
 * window are stored here.
 */
public class CasesLists {

    private final List<CellCase> cellCases;
    private final List<AnatomyTermCase> anatomyTermCases;

    private InfoWindow infoWindow;

    public CasesLists() {
        this.cellCases = new ArrayList<>();
        this.anatomyTermCases = new ArrayList<>();
    }

    public void setInfoWindow(final InfoWindow infoWindow) {
        this.infoWindow = requireNonNull(infoWindow);
    }

    /**
     * Creates a terminal case for a cell and adds to this class' list of cases.
     *
     * @param lineageName
     *         lineage name of the cell
     * @param cellName
     *         functional name of the cell
     * @param presynapticPartners
     *         list of presynaptic partners
     * @param postsynapticPartners
     *         list of postsynaptic partners
     * @param electricalPartners
     *         list of electrical partners
     * @param neuromuscularPartners
     *         list of neuromuscular partners
     * @param nuclearProductionInfo
     *         production information under Nuclear
     * @param cellShapeProductionInfo
     *         production information under Cell Shape
     */
    public void makeTerminalCase(
            String lineageName,
            String cellName,
            List<String> presynapticPartners,
            List<String> postsynapticPartners,
            List<String> electricalPartners,
            List<String> neuromuscularPartners,
            List<String> nuclearProductionInfo,
            List<String> cellShapeProductionInfo) {

        addTerminalCase(new TerminalCellCase(
                lineageName,
                cellName,
                presynapticPartners,
                postsynapticPartners,
                electricalPartners,
                neuromuscularPartners,
                nuclearProductionInfo,
                cellShapeProductionInfo));
    }

    /**
     * Adds a terminal case to the list of terminal cases, updates the view with the new case
     *
     * @param terminalCase
     *         the case to be added
     */
    private void addTerminalCase(TerminalCellCase terminalCase) {
        cellCases.add(terminalCase);
        if (infoWindow != null) {
            // add dom(tab) to InfoWindow
            infoWindow.addTab(terminalCase);
        }
    }

    /**
     * Creates a non terminal case for a cell and adds the case to the list
     *
     * @param cellName
     *         name of the cell
     * @param nuclearProductionInfo
     *         the production information under Nuclear
     * @param cellShapeProductionInfo
     *         the production information under Cell Shape
     */
    public void makeNonTerminalCase(
            final String cellName,
            final List<String> nuclearProductionInfo,
            final List<String> cellShapeProductionInfo) {
        addNonTerminalCase(new NonTerminalCellCase(cellName, nuclearProductionInfo, cellShapeProductionInfo));
    }

    /**
     * Adds the given non terminal case to the list
     *
     * @param nonTerminalCase
     *         the case to be added
     */
    private void addNonTerminalCase(final NonTerminalCellCase nonTerminalCase) {
        cellCases.add(nonTerminalCase);
        // add dom(tab) to InfoWindow
        if (infoWindow != null) {
            infoWindow.addTab(nonTerminalCase);
        }
    }

    public void makeAnatomyTermCase(final AnatomyTerm term) {
        if (term.equals(AMPHID_SENSILLA)) {
            AmphidSensillaTerm amphidSensillaCase = new AmphidSensillaTerm(term);
            addAmphidSensillaTermCase(amphidSensillaCase);
        }
    }

    private void addAmphidSensillaTermCase(final AmphidSensillaTerm amphidSensillaTermCase) {
        if (amphidSensillaTermCase != null) {
            anatomyTermCases.add(amphidSensillaTermCase);

            //add dom to InfoWindow
            if (infoWindow != null) {
                infoWindow.addTab(amphidSensillaTermCase);
            }

        }
    }

    /**
     * Retrieves the FIRST cell case (as seen in the parts list) with a name. The name can be a lineage or functional
     * name.
     * <p>
     * TODO fix algorithm to return multiple cell cases since the input 'cellName' could be a functional name with
     * multiple lineage names
     *
     * @param cellName
     *         the cell to check
     *
     * @return cell case for that cell
     */
    public CellCase getCellCase(final String cellName) {
        // attempt to translate name into lineage name(s) in case the name is a functional name
        final List<String> cells = new ArrayList<>(getLineageNamesByFunctionalName(cellName));
        if (cells.isEmpty()) {
            cells.add(cellName);
        }
        for (String cell : cells) {
            for (CellCase cellCase : cellCases) {
                if (cellCase.getLineageName().equalsIgnoreCase(cell)) {
                    return cellCase;
                }
            }
        }
        return null;
    }

    public boolean containsCellCase(final String cellName) {
        // attempt to translate name into lineage name(s) in case the name is a functional name
        final List<String> cells = getLineageNamesByFunctionalName(cellName);
        if (cells.isEmpty()) {
            cells.add(cellName);
        }
        for (String cell : cells) {
            for (CellCase cellCase : cellCases) {
                if (cellCase.getLineageName().equalsIgnoreCase(cell)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAnatomyTermCase(String term) {
        if (anatomyTermCases != null) {
            for (AnatomyTermCase atc : anatomyTermCases) {
                if (atc.getName().toLowerCase().equals(term.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a cell name has a corresponding case in the terminal cases OR non terminal cases
     *
     * @param cellName
     *         name of the cell
     *
     * @return true if a cell case was found for the cell, false otherwise
     */
    public boolean hasCellCase(final String cellName) {
        //TODO refactor this to just be name
        return containsCellCase(cellName) || containsAnatomyTermCase(cellName);
    }

    /**
     * Removes the cell case from the internal lists (when the tab is closed)
     *
     * @param cellName
     *         the cell to remove
     */
    public void removeCellCase(String cellName) {
        final List<String> cells = new ArrayList<>(getLineageNamesByFunctionalName(cellName));
        if (cells.isEmpty()) {
            cells.add(cellName);
        }
        for (String cell : cells) {
            if (containsCellCase(cell)) {
                cellCases.stream()
                        .filter(cellCase -> cellCase.getLineageName().toLowerCase().equals(cell.toLowerCase()))
                        .forEach(cellCases::remove);
            }
            if (containsAnatomyTermCase(cellName)) {
                for (int i = 0; i < anatomyTermCases.size(); i++) {
                    if (anatomyTermCases.get(i).getName().toLowerCase().equals(cellName.toLowerCase())) {
                        anatomyTermCases.remove(i);
                        return;
                    }
                }
            }
        }
    }
}
