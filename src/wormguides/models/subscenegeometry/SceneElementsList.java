/*
 * Bao Lab 2017
 */

package wormguides.models.subscenegeometry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javafx.scene.control.TreeItem;

import acetree.LineageData;
import wormguides.MainApp;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.parseInt;
import static java.util.Collections.sort;

import static partslist.PartsList.getFunctionalNameByLineageName;
import static wormguides.loaders.GeometryLoader.getEffectiveStartTime;

/**
 * Record of {@link SceneElement}s over the life of the embryo
 */
public class SceneElementsList {

    private static final String CELL_CONFIG_FILE_NAME = "CellShapesConfig.csv";
    private static final String ASTERISK = "*";

    private static final int NUM_OF_CSV_FIELDS = 8;
    private static final int DESCRIPTION_INDEX = 0;
    private static final int CELLS_INDEX = 1;
    private static final int MARKER_INDEX = 2;
    private static final int IMAGING_SOURCE_INDEX = 3;
    private static final int RESOURCE_LOCATION_INDEX = 4;
    private static final int START_TIME_INDEX = 5;
    private static final int END_TIME_INDEX = 6;
    private static final int COMMENTS_INDEX = 7;

    private final List<SceneElement> elementsList;
    private final TreeItem<StructureTreeNode> root;

    private final Map<String, List<String>> nameCellsMap;
    private final Map<String, String> nameCommentsMap;
    private final Map<String, String> nameToMarkerMap;

    public SceneElementsList(final LineageData lineageData) {
        elementsList = new ArrayList<>();
        root = new TreeItem<>(new StructureTreeNode(true, "root"));
        nameCellsMap = new HashMap<>();
        nameCommentsMap = new HashMap<>();
        nameToMarkerMap = new HashMap<>();
        buildListFromConfig(lineageData);
    }

    private void buildListFromConfig(final LineageData lineageData) {
        final URL url = MainApp.class.getResource("/wormguides/models/shapes_file/" + CELL_CONFIG_FILE_NAME);
        if (url != null) {
            try {
                final InputStream stream = url.openStream();
                processStream(stream, lineageData);
                processCells();
                stream.close();
            } catch (IOException e) {
                System.out.println("Config file '" + CELL_CONFIG_FILE_NAME + "' was not found.");
            }
        }
    }

    /**
     * @param name
     *         the name to check
     *
     * @return true if the name is the scene name of a structure, false otherwise
     */
    public boolean isStructureSceneName(String name) {
        name = name.trim().toLowerCase();
        for (SceneElement se : elementsList) {
            if (se.getSceneName().toLowerCase().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void processStream(final InputStream stream, final LineageData lineageData) {
        try {
            final InputStreamReader streamReader = new InputStreamReader(stream);
            final BufferedReader reader = new BufferedReader(streamReader);

            // skip csv file heading
            reader.readLine();

            String line;
            String name;
            String resourceLocation;
            int startTime;
            int endTime;
            List<String> cellNames;
            StringTokenizer cellNamesTokenizer;
            TreeItem<StructureTreeNode> currentCategoryNode = root;
            // process each line
            while ((line = reader.readLine()) != null) {
                final String[] tokens = line.split(",", NUM_OF_CSV_FIELDS);
                name = tokens[DESCRIPTION_INDEX];
                if (isCategoryLine(tokens)) {
                    // add cetegory to tree
                    if (name.equalsIgnoreCase(currentCategoryNode.getValue().getText())) {
                        // the ending of a category
                        currentCategoryNode = currentCategoryNode.getParent();
                    } else {
                        final TreeItem<StructureTreeNode> newTreeNode = new TreeItem<>(new StructureTreeNode(
                                true,
                                name));
                        currentCategoryNode.getChildren().add(newTreeNode);
                        currentCategoryNode = newTreeNode;
                    }
                } else {
                    // add structure (leaf node) to tree
                    // add scene element only if resource exists in /wormguides/models/obj_files
                    try {
                        resourceLocation = tokens[RESOURCE_LOCATION_INDEX];
                        startTime = parseInt(tokens[START_TIME_INDEX]);
                        endTime = parseInt(tokens[END_TIME_INDEX]);

                        // check for the first time that the .obj resource exists in the shape files archive
                        int effectiveStartTime = getEffectiveStartTime(resourceLocation, startTime, endTime);
                        if (effectiveStartTime != startTime) {
                            startTime = effectiveStartTime;
                        }
                        // vector of cell names
                        cellNames = new ArrayList<>();
                        cellNamesTokenizer = new StringTokenizer(tokens[CELLS_INDEX]);
                        while (cellNamesTokenizer.hasMoreTokens()) {
                            cellNames.add(cellNamesTokenizer.nextToken());
                        }

                        if (name.contains("(")
                                && name.contains(")")
                                && (name.indexOf("(") < name.indexOf(")"))) {
                            name = name.substring(0, name.indexOf("(")).trim();
                        }
                        if (lineageData.isCellName(name)) {
                            effectiveStartTime = lineageData.getFirstOccurrenceOf(name);
                            int effectiveEndTime = lineageData.getLastOccurrenceOf(name);
                            // use the later one of the config start time and the effective lineage start time
                            startTime = effectiveStartTime > startTime ? effectiveStartTime : startTime;
                            // use the earlier one of the config start time and the effective lineage start time
                            endTime = effectiveEndTime < endTime ? effectiveEndTime : endTime;
                            final String functionalName;
                            if ((functionalName = getFunctionalNameByLineageName(name)) != null) {
                                name = functionalName;
                            }
                        }

                        final SceneElement element = new SceneElement(
                                name,
                                cellNames,
                                tokens[MARKER_INDEX],
                                tokens[IMAGING_SOURCE_INDEX],
                                resourceLocation,
                                startTime,
                                endTime,
                                tokens[COMMENTS_INDEX]);
                        addSceneElement(element);
                        if (!element.getAllCells().isEmpty()) {
                            nameCellsMap.put(element.getSceneName().toLowerCase(), element.getAllCells());
                        }

                        if (!element.getMarkerName().isEmpty()) {
                            nameToMarkerMap.put(element.getSceneName().toLowerCase(), element.getMarkerName());
                        }

                        if (!element.getComments().isEmpty()) {
                            nameCommentsMap.put(element.getSceneName().toLowerCase(), element.getComments());
                        }
                        // insert structure into tree
                        currentCategoryNode.getChildren().add(
                                new TreeItem<>(new StructureTreeNode(false, element.getSceneName())));
                    } catch (NumberFormatException e) {
                        System.out.println("error in reading scene element time for line " + line);
                    }
                }
            }
            streamReader.close();
        } catch (IOException e) {
            System.out.println("Invalid file: '" + CELL_CONFIG_FILE_NAME);
        }
    }

    /**
     * @param tokens
     *         tokens read from one line of the CSV config file
     *
     * @return true if the line is a category header/footer, false otherwise
     */
    private boolean isCategoryLine(final String[] tokens) {
        if (tokens.length == NUM_OF_CSV_FIELDS && !tokens[DESCRIPTION_INDEX].isEmpty()) {
            boolean isCategoryLine = true;
            // check that all other fields are empty
            for (int i = 1; i < NUM_OF_CSV_FIELDS; i++) {
                isCategoryLine &= tokens[i].isEmpty();
            }
            return isCategoryLine;
        }
        return false;
    }

    /*
     * the start of our recursive algorithm to unpack entries in cell names
     * which reference other scene elements' cell list
     */
    private void processCells() {
        for (SceneElement se : elementsList) {
            List<String> cells = se.getAllCells();
            for (int i = 0; i < cells.size(); i++) {
                if (cells.get(i).startsWith(ASTERISK)) {
                    se.setNewCellNames(unpackCells(cells));
                }
            }
        }
    }

    private List<String> unpackCells(final List<String> cells) {
        final List<String> unpackedCells = new ArrayList<>();
        for (String cell : cells) {
            // if cell starts with ASTERISK, recurse. else, add cell
            if (cell.startsWith(ASTERISK)) {
                // find the matching resource location
                elementsList.stream()
                        .filter(se -> se.getResourceLocation().endsWith(cell.substring(1)))
                        .forEachOrdered(se -> {
                            // recursively unpack matching location's cell list
                            unpackedCells.addAll(unpackCells(se.getAllCells()));
                        });
            } else {
                // only add cell name entry if not already added
                if (!unpackedCells.contains(cell)) {
                    unpackedCells.add(cell);
                }
            }
        }
        return unpackedCells;
    }

    /**
     * Returns the biological time (without frame offset) of the first occurrence of element with scene name, name
     */
    public int getFirstOccurrenceOf(String name) {
        int time = MIN_VALUE;
        for (SceneElement element : elementsList) {
            if (element.getSceneName().equalsIgnoreCase(name)) {
                time = element.getStartTime();
            }
        }
        return time + 1;
    }

    /**
     * Returns the biological time (without frame offset) of the last occurrence of element with scene name, name
     */
    public int getLastOccurrenceOf(String name) {
        int time = MIN_VALUE;
        for (SceneElement element : elementsList) {
            if (element.getSceneName().equalsIgnoreCase(name)) {
                time = element.getEndTime();
            }
        }
        return time + 1;
    }

    /**
     * Adds a scene element to the data store.
     *
     * @param element
     *         the scene element to add
     */
    public void addSceneElement(final SceneElement element) {
        if (element != null) {
            elementsList.add(element);
        }
    }

    public String[] getSceneElementNamesAtTime(final int time) {
        // Add lineage names of all structures at time
        final List<String> list = new ArrayList<>();
        elementsList.stream().filter(se -> se.existsAtTime(time)).forEachOrdered(se -> {
            if (se.isMulticellular() || se.getAllCells().size() == 0) {
                list.add(se.getSceneName());
            } else {
                list.add(se.getAllCells().get(0));
            }
        });
        return list.toArray(new String[list.size()]);
    }

    public List<SceneElement> getSceneElementsAtTime(final int time) {
        final List<SceneElement> elements = new ArrayList<>();
        elementsList.forEach(element -> {
            if (element.existsAtTime(time)) {
                elements.add(element);
            }
        });
        return elements;
    }

    public List<String> getAllSceneNames() {
        final Set<String> namesSet = new HashSet<>();
        elementsList.forEach(se -> namesSet.add(se.getSceneName()));
        final List<String> namesSorted = new ArrayList<>(namesSet);
        sort(namesSorted);
        return namesSorted;
    }

    public List<String> getAllMulticellSceneNames() {
        final Set<String> namesSet = new HashSet<>();
        elementsList.stream()
                .filter(SceneElement::isMulticellular)
                .forEachOrdered(se -> namesSet.add(se.getSceneName()));
        final List<String> namesSorted = new ArrayList<>(namesSet);
        sort(namesSorted);
        return namesSorted;
    }

    public boolean isMulticellStructureName(String name) {
        name = name.trim();
        for (String cellName : getAllMulticellSceneNames()) {
            if (cellName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public String getCommentByName(final String name) {
        final String comment = nameCommentsMap.get(name.trim().toLowerCase());
        if (comment == null) {
            return "";
        }
        return comment;
    }

    public Map<String, String> getNameToCommentsMap() {
        return nameCommentsMap;
    }

    public Map<String, List<String>> getNameToCellsMap() {
        return nameCellsMap;
    }

    public Map<String, String> getNameToMarkerMap() {
        return nameToMarkerMap;
    }

    public List<SceneElement> getElementsList() {
        return elementsList;
    }

    /**
     * @return the root of the hierarchy structures tree
     */
    public TreeItem<StructureTreeNode> getTreeRoot() {
        return root;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Scene elements list:\n");
        for (SceneElement se : elementsList) {
            sb.append(se.getSceneName()).append("\n");
        }
        return sb.toString();
    }
}