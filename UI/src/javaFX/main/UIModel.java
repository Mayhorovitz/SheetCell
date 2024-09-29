package javaFX.main;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class UIModel {

    // Properties for columns and rows dimensions
    private DoubleProperty colWidth;
    private DoubleProperty rowHeight;

    public UIModel() {
        // Initialize with default values
        this.colWidth = new SimpleDoubleProperty(200);  // Default column width
        this.rowHeight = new SimpleDoubleProperty(200);  // Default row height
    }

    // Column Width property
    public DoubleProperty colWidthProperty() {
        return colWidth;
    }

    public void setColWidth(double width) {
        this.colWidth.set(width);
    }

    public double getColWidth() {
        return colWidth.get();
    }

    // Row Height property
    public DoubleProperty rowHeightProperty() {
        return rowHeight;
    }

    public void setRowHeight(double height) {
        this.rowHeight.set(height);
    }

    public double getRowHeight() {
        return rowHeight.get();
    }
}
