package com.jbsv.processma.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

@Getter
public class EventSymbolInfo extends ApplicationEvent {
    private final Date startDate;
    private final Date endDate;
    private final String symbol;

    public EventSymbolInfo(Object source, Date startDate, Date endDate, String symbol) {
        super(source);
        this.startDate = startDate;
        this.endDate = endDate;
        this.symbol = symbol;
    }
}
