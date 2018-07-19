package application_src.views.popups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.effect.Bloom;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import application_src.application_model.annotation.stories.Note;
import application_src.application_model.annotation.stories.Story;
import application_src.controllers.layers.StoriesLayer;
import application_src.application_model.resources.ProductionInfo;
import application_src.MainApp;


/**
 * Created by Ciara Mcmullin on 7/11/2017.
 */

/**
 *
 * This class is a graphical timeline of (@link Note)s. It displays the active Story and its notes for each cell.
 *
 */

public class TimelineChart<X,Y> extends XYChart<X,Y> {


    /**
     * Class that gets the length of each block
     */
    public static class ExtraData {
        public long length;
        public String styleClass;

        public ExtraData(long length, String styleClass) {
            super();
            this.length = length;
            this.styleClass = styleClass;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getStyleClass() {
            return styleClass;
        }
    }

    private double blockHeight = 10;

    /**
     *
     * @param xAxis
     * @param yAxis
     */

    public TimelineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        this(xAxis, yAxis, FXCollections.observableArrayList());

    }

    /**
     *
     * @param xAxis
     * @param yAxis
     * @param data
     */

    public TimelineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis);
        if (!(xAxis instanceof ValueAxis && yAxis instanceof CategoryAxis)) {
            throw new IllegalArgumentException("Axis type incorrect");
        }
        setData(data);
    }

    /**
     *
     * @param obj
     * @return
     */
    private static String getStyleClass(Object obj) {
        return ((ExtraData) obj).getStyleClass();
    }

    private static double getLength(Object obj) {
        return ((ExtraData) obj).getLength();
    }

    /**
     * method to plot the bars for each cell
     */
    @Override
    protected void layoutPlotChildren() {

        for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {

            Series<X, Y> series = getData().get(seriesIndex);

            Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
            while (iter.hasNext()) {
                Data<X, Y> item = iter.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node block = item.getNode();
                Rectangle ellipse;
                if (block != null) {
                    if (block instanceof StackPane) {
                        StackPane region = (StackPane) item.getNode();
                        if (region.getShape() == null) {
                            ellipse = new Rectangle(getLength(item.getExtraValue()), getBlockHeight());
                        } else if (region.getShape() instanceof Rectangle) {
                            ellipse = (Rectangle) region.getShape();
                        } else {
                            return;
                        }
                        ellipse.setWidth(getLength(item.getExtraValue()) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getXAxis()).getScale()) : 1));
                        ellipse.setHeight(getBlockHeight() * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getYAxis()).getScale()) : 1));
                        y -= getBlockHeight() / 2.0;


                        region.setShape(null);
                        region.setShape(ellipse);
                        region.setScaleShape(false);
                        region.setCenterShape(false);
                        region.setCacheShape(false);

                        block.setLayoutX(x);
                        block.setLayoutY(y);
                    }
                }
            }
        }
    }


    // variables for chart initialization and rebuild
    private static TimelineChart<Number, String> chart;
    private static int movieTimeOffset;
    private static int min = 250;
    private static int max = 0;
    private static List<String> cellNames;
    private static List<String> cellTags;
    private static HashMap<String, Series> cellSeries;
    private static HashMap<String, String> tagMap;
    private static HashMap<XYChart.Data, Note> noteDisplay;

    /**
     * Initializes timelinechart with activeStory from storiesLayer
     * If no active story- return blank chart
     * @param storiesLayer
     * @return
     */
    public static Scene initialize(StoriesLayer storiesLayer, ProductionInfo productionInfo) {
        movieTimeOffset = productionInfo.getMovieTimeOffset();
        cellNames = new ArrayList<String>();
        cellTags = new ArrayList<String>();
        cellSeries = new HashMap<String, XYChart.Series>();
        tagMap = new HashMap<String, String>();
        noteDisplay = new HashMap<XYChart.Data, Note>();
        return buildTimeline(storiesLayer);
    }

    /**
     * builds timeline when new note or story is active
     */
    public static Scene buildTimeline(StoriesLayer storiesLayer) {
        clearData();

        if (storiesLayer == null || storiesLayer.getActiveStory() == null) {
            cellNames = new ArrayList<String>();
            HashMap<String, Series> hmap = new HashMap<String, XYChart.Series>();

            final NumberAxis xAxis = new NumberAxis();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(250);
            xAxis.setUpperBound(400);
            xAxis.setLabel("Time (min)");
            xAxis.setMinorTickCount(0);

            final CategoryAxis yAxis = new CategoryAxis();
            yAxis.setLabel("Cell/Item");
            yAxis.setTickLabelGap(10);
            yAxis.setCategories(FXCollections.observableArrayList(cellNames));

            chart = new TimelineChart<Number, String>(xAxis, yAxis);
            chart.setTitle("Timeline of Story Notes");
            chart.setLegendVisible(false);
            chart.setBlockHeight(15);

            for (XYChart.Series thisSeries : hmap.values()) {
                chart.getData().addAll(thisSeries);
            }
        } else {
            Story activeStory = storiesLayer.getActiveStory();
            for (int i = 0; i < (activeStory.getNotes().size()); i++) {
                Note note = activeStory.getNotes().get(i);
                String cellName = activeStory.getNotes().get(i).getCellName();

                int start = activeStory.getNotes().get(i).getStartTime() + movieTimeOffset;
                int end = activeStory.getNotes().get(i).getEndTime() + movieTimeOffset;
                if (start < min && !(start < 0)) {
                    min = start;
                }
                if (end > max) {
                    max = end;
                }
                if (cellSeries.containsKey(cellName)) {
                    XYChart.Series currentSeries = cellSeries.get(cellName);
                    if (end - start == 0) {
                        XYChart.Data d = (new XYChart.Data(start, tagMap.get(cellName), new TimelineChart.ExtraData((end - start) + 1, "status-purple")));
                        currentSeries.getData().add(d);
                        noteDisplay.put(d, note);

                    } else {
                        XYChart.Data d = new XYChart.Data(start, tagMap.get(cellName), new TimelineChart.ExtraData((end - start) + 1, "status-lightPurple"));
                        currentSeries.getData().add(d);
                        noteDisplay.put(d, note);
                    }
                } else {
                    cellNames.add(cellName);
                    String cellTag = activeStory.getNotes().get(i).getTagName();
                    cellTags.add(cellTag);
                    tagMap.put(cellName, cellTag);
                    XYChart.Series currentSeries = new XYChart.Series();
                    currentSeries.getData().add(new XYChart.Data(200, tagMap.get(cellName), new TimelineChart.ExtraData(300, "status-lightGray")));
                    if (end - start == 0) {
                        XYChart.Data d = new XYChart.Data(start, tagMap.get(cellName), new TimelineChart.ExtraData((end - start) + 1, "status-purple"));
                        currentSeries.getData().add(d);
                        noteDisplay.put(d, note);

                    } else {
                        XYChart.Data d = new XYChart.Data(start, tagMap.get(cellName), new TimelineChart.ExtraData((end - start) + 1, "status-lightPurple"));
                        currentSeries.getData().add(d);
                        noteDisplay.put(d, note);
                    }
                    cellSeries.put(cellName, currentSeries);
                }
            }

            final NumberAxis xAxis = new NumberAxis();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(min - 10);
            xAxis.setUpperBound(max + 10);
            xAxis.setLabel("Time (min)");
            xAxis.setMinorTickCount(0);

            final CategoryAxis yAxis = new CategoryAxis();
            yAxis.setLabel("Cell/ Item");
            yAxis.setTickLabelGap(10);
            yAxis.setCategories(FXCollections.observableArrayList(cellTags));

            chart = new TimelineChart<Number, String>(xAxis, yAxis);
            chart.setTitle("Timeline of Story Notes");
            chart.setLegendVisible(false);
            chart.setBlockHeight(15);

            for (XYChart.Series thisSeries : cellSeries.values()) {
                chart.getData().addAll(thisSeries);
            }
            for (XYChart.Series<Number, String> s : chart.getData()) {
                for (XYChart.Data<Number, String> d : s.getData()) {
                    for (int i = 0; i < (activeStory.getNotes().size()); i++) {
                        final Note note = activeStory.getNotes().get(i);
                        int start = note.getStartTime() + movieTimeOffset;
                        int end = note.getEndTime() + movieTimeOffset;
                        if (noteDisplay.get(d) == note) {
                            Tooltip.install(d.getNode(), new Tooltip(
                                    Integer.valueOf(start) + "-" + Integer.valueOf(end) + "\n" + note.getTagName() + "\n" + note.getTagContents()));

                            // add effect
                            Bloom bloom = new Bloom();
                            d.getNode().setEffect(bloom);

                            //Adding class on hover
                            d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));


                            //Removing class on exit
                            d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));

                            // rebuild 3d window on node pressed
                            d.getNode().setOnMousePressed(event -> storiesLayer.setActiveNoteWithSubsceneRebuild(note));
                        }
                    }
                }
            }
        }


        chart.getStylesheets().add(MainApp.class.getResource("/application_src/views/style/TimelineChartStyle.css").toExternalForm());
        return new Scene(chart, 1000, 400);
    }

    private static void clearData() {
        cellNames.clear();
        cellTags.clear();
        cellSeries.clear();
        tagMap.clear();
        noteDisplay.clear();
    }


    /**
     * getter and setter for height of bars on graph
     * @return
     */
    public double getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(double blockHeight) {
        this.blockHeight = blockHeight;
    }

    /**
     *
     * @param series
     * @param itemIndex
     * @param item
     */
    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        Node block = createContainer(series, getData().indexOf(series), item, itemIndex);
        getPlotChildren().add(block);
    }

    /**
     *
     * @param item
     * @param series
     */
    @Override
    protected void dataItemRemoved(final Data<X, Y> item, final Series<X, Y> series) {
        final Node block = item.getNode();
        getPlotChildren().remove(block);
        removeDataItemFromDisplay(series, item);
    }

    /**
     *
     * @param item
     */
    @Override
    protected void dataItemChanged(Data<X, Y> item) {
    }

    /**
     *
     * @param series
     * @param seriesIndex
     */
    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            Node container = createContainer(series, seriesIndex, item, j);
            getPlotChildren().add(container);
        }
    }

    /**
     *
     * @param series
     */
    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        for (XYChart.Data<X, Y> d : series.getData()) {
            final Node container = d.getNode();
            getPlotChildren().remove(container);
        }
        removeSeriesFromDisplay(series);

    }

    /**
     *
     * @param series
     * @param seriesIndex
     * @param item
     * @param itemIndex
     * @return
     */
    private Node createContainer(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {

        Node container = item.getNode();

        if (container == null) {
            container = new StackPane();
            item.setNode(container);
        }

        container.getStyleClass().add(getStyleClass(item.getExtraValue()));


        return container;
    }

    /**
     * used to update x and y axis when new story/note is added
     */
    @Override
    protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> ya = getYAxis();
        List<X> xData = null;
        List<Y> yData = null;
        if (xa.isAutoRanging()) xData = new ArrayList<X>();
        if (ya.isAutoRanging()) yData = new ArrayList<Y>();
        if (xData != null || yData != null) {
            for (Series<X, Y> series : getData()) {
                for (Data<X, Y> data : series.getData()) {
                    if (xData != null) {
                        xData.add(data.getXValue());
                        xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }
                    if (yData != null) {
                        yData.add(data.getYValue());
                    }
                }
            }
            if (xData != null) xa.invalidateRange(xData);
            if (yData != null) ya.invalidateRange(yData);
        }
    }
}