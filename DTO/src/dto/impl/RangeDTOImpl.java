package dto.impl;

import dto.api.RangeDTO;

public class RangeDTOImpl implements RangeDTO {
    private String name;
    private String from;
    private String to;

    public RangeDTOImpl(String name, String from, String to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    // Getters
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }
}
