package sheetView.components.commands.DynamicAnalysis;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class CellAnalysisParameters {
    private String cellId;
    private TextField cellIdField;
    private TextField minValueField;
    private TextField maxValueField;
    private TextField stepSizeField;
    private Slider valueSlider;

    public CellAnalysisParameters() {
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public TextField getCellIdField() {
        return cellIdField;
    }

    public void setCellIdField(TextField cellIdField) {
        this.cellIdField = cellIdField;
    }

    public TextField getMinValueField() {
        return minValueField;
    }

    public void setMinValueField(TextField minValueField) {
        this.minValueField = minValueField;
    }

    public TextField getMaxValueField() {
        return maxValueField;
    }

    public void setMaxValueField(TextField maxValueField) {
        this.maxValueField = maxValueField;
    }

    public TextField getStepSizeField() {
        return stepSizeField;
    }

    public void setStepSizeField(TextField stepSizeField) {
        this.stepSizeField = stepSizeField;
    }

    public Slider getValueSlider() {
        return valueSlider;
    }

    public void setValueSlider(Slider valueSlider) {
        this.valueSlider = valueSlider;
    }
}
