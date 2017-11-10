/*
 * Bao Lab 2017
 */

package application_src.controllers.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.NumberStringConverter;

import static java.lang.Double.parseDouble;
import static java.util.Objects.requireNonNull;

public class RotationController extends AnchorPane implements Initializable {

    private final static String DoublePropStr = "DoubleProperty [value: ";

    @FXML
    private Slider xRotationSlider;

    @FXML
    private Slider yRotationSlider;

    @FXML
    private Slider zRotationSlider;

    @FXML
    private TextField rotateXAngleField;

    @FXML
    private TextField rotateYAngleField;

    @FXML
    private TextField rotateZAngleField;

    private DoubleProperty xRotationAngle;

    private DoubleProperty yRotationAngle;

    private DoubleProperty zRotationAngle;

    public RotationController(
            DoubleProperty xRotationAngle,
            DoubleProperty yRotationAngle,
            DoubleProperty zRotationAngle) {
        super();
        this.xRotationAngle = requireNonNull(xRotationAngle);
        this.yRotationAngle = requireNonNull(yRotationAngle);
        this.zRotationAngle = requireNonNull(zRotationAngle);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assertFXMLNodes();

        xRotationAngle.addListener(getXRotationListener());
        yRotationAngle.addListener(getYRotationListener());
        zRotationAngle.addListener(getZRotationListener());

        rotateXAngleField.textProperty().addListener(getRotateXAngleFieldListener());
        rotateYAngleField.textProperty().addListener(getRotateYAngleFieldListener());
        rotateZAngleField.textProperty().addListener(getRotateZAngleFieldListener());

        // set initial values
        xRotationSlider.setValue(xRotationAngle.get());
        yRotationSlider.setValue(yRotationAngle.get());
        zRotationSlider.setValue(zRotationAngle.get());

        rotateXAngleField.setText(this.xRotationAngle.toString());
        rotateYAngleField.setText(this.yRotationAngle.toString());
        rotateZAngleField.setText(this.zRotationAngle.toString());

        rotateXAngleField.textProperty().bindBidirectional(xRotationAngle, new NumberStringConverter());
        rotateYAngleField.textProperty().bindBidirectional(yRotationAngle, new NumberStringConverter());
        rotateZAngleField.textProperty().bindBidirectional(zRotationAngle, new NumberStringConverter());

        xRotationSlider.valueProperty().addListener(getXRotationSliderListener());
        yRotationSlider.valueProperty().addListener(getYRotationSliderListener());
        zRotationSlider.valueProperty().addListener(getZRotationSliderListener());

    }

    private ChangeListener<Number> getXRotationListener() {
        return (observable, oldValue, newValue) -> xRotationSlider.setValue(xRotationAngle.get());
    }

    private ChangeListener<Number> getYRotationListener() {
        return (observable, oldValue, newValue) -> yRotationSlider.setValue(yRotationAngle.get());
    }

    private ChangeListener<Number> getZRotationListener() {
        return (observable, oldValue, newValue) -> zRotationSlider.setValue(zRotationAngle.get());
    }

    public ChangeListener<Number> getXRotationSliderListener() {
        return (observable, oldValue, newValue) -> xRotationAngle.set(xRotationSlider.getValue());
    }

    public ChangeListener<Number> getYRotationSliderListener() {
        return (observable, oldValue, newValue) -> yRotationAngle.set(yRotationSlider.getValue());
    }

    public ChangeListener<Number> getZRotationSliderListener() {
        return (observable, oldValue, newValue) -> zRotationAngle.set(zRotationSlider.getValue());
    }

    /*
     * WHY CAN"T THESE BE HANDLED WITH CATCH PARSEEXCEPTION ??
     */
    private ChangeListener<String> getRotateXAngleFieldListener() {
        return (observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                // check if initialization
                if (newValue.startsWith(DoublePropStr)) {
                    return;
                }

                try {
                    double rotateXAngleVal = parseDouble(newValue);

                    if (rotateXAngleVal > -360. && rotateXAngleVal < 360.) {
                        xRotationAngle.set(rotateXAngleVal);
                    }
                } catch (Exception e) {
                    xRotationAngle.set(0);
                }
            }
        };
    }

    private ChangeListener<String> getRotateYAngleFieldListener() {
        return (observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                // check if initialization
                if (newValue.startsWith(DoublePropStr)) {
                    return;
                }

                try {
                    double rotateYAngleVal = parseDouble(newValue);

                    if (rotateYAngleVal > -360. && rotateYAngleVal < 360.) {
                        yRotationAngle.set(rotateYAngleVal);
                    }
                } catch (Exception e) {
                    yRotationAngle.set(0);
                }
            }
        };
    }

    private ChangeListener<String> getRotateZAngleFieldListener() {
        return (observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                // check if initialization
                if (newValue.startsWith(DoublePropStr)) {
                    return;
                }

                try {
                    double rotateZAngleVal = parseDouble(newValue);

                    if (rotateZAngleVal > -360. && rotateZAngleVal < 360.) {
                        zRotationAngle.set(rotateZAngleVal);
                    }
                } catch (Exception e) {
                    zRotationAngle.set(0);
                }
            }
        };
    }

    private void assertFXMLNodes() {
        assert (xRotationSlider != null);
        assert (yRotationSlider != null);
        assert (zRotationSlider != null);
    }
}
