package range.api;

import cell.api.Cell;
import cell.api.EffectiveValue;
import sheet.api.Sheet;

import java.util.List;

public interface Range {

    String getName();

    String getFrom();

    String getTo();

    List<Cell> getCells();
}