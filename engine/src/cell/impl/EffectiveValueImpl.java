package cell.impl;


import cell.api.EffectiveValue;
import cell.api.CellType;

import java.io.Serializable;
import java.util.Objects;

public class EffectiveValueImpl implements EffectiveValue , Serializable {

    private final CellType cellType;
    private final Object value;

    //constructors
    public EffectiveValueImpl(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
    }
    //getters
    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public <T> T extractValueWithExpectation(Class<T> type) {
        if (cellType.isAssignableFrom(type)) {
            return type.cast(value);
        }
        return null;
    }
    //check if two effective values equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EffectiveValueImpl that = (EffectiveValueImpl) o;

        if (cellType != that.cellType) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = cellType != null ? cellType.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if(cellType == CellType.BOOLEAN){
            return value.toString().toUpperCase();
        }
        return value.toString();
    }
}