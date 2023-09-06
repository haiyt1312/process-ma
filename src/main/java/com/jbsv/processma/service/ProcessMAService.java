package com.jbsv.processma.service;

import com.jbsv.processma.common.Utils;
import com.jbsv.processma.model.EventSymbolInfo;
import com.jbsv.processma.model.SymbolInfo;
import com.jbsv.processma.process.ProcessMA;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ProcessMAService {
    private final MongoDatabase mongoDatabase;
    private final ApplicationEventPublisher eventPublisher;
    private final ProcessMA processMA;

    @Value("${config.start-date}")
    private String stringStartDate;
    @Value("${config.end-date}")
    private String stringEndDate;
    @Value("${config.symbols}")
    private String symbols;

    public ProcessMAService(MongoDatabase mongoDatabase, ApplicationEventPublisher eventPublisher, ProcessMA processMA) {
        this.mongoDatabase = mongoDatabase;
        this.eventPublisher = eventPublisher;
        this.processMA = processMA;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        Date startDate = Utils.convertDate(stringStartDate);
        Date endDate = Utils.convertDate(stringEndDate);
        final Date finalEndDate = endDate;
        if (Utils.isEmpty(finalEndDate)) {
            endDate = new Date();
        }

        log.info("start date: {}", startDate);
        log.info("end date: {}", endDate);

        List<SymbolInfo> symbolInfos = new ArrayList<>();

        Arrays.stream(symbols.split(","))
                .map(SymbolInfo::new)
                .forEach(symbolInfos::add);

        if (Utils.isNull(symbols)) {
            symbolInfos = getListSymbol();
        }

        symbolInfos.forEach(symbolInfo -> {
            eventPublisher.publishEvent(new EventSymbolInfo(this, startDate, finalEndDate, symbolInfo.getId()));
        });
    }

    public List<SymbolInfo> getListSymbol() {
        MongoCollection<Document> collectionSymbolInfo = mongoDatabase.getCollection("c_symbol_info");

        FindIterable<Document> listSymbol = collectionSymbolInfo.find().projection(new Document("_id", 1));
        List<SymbolInfo> symbolInfos = new ArrayList<>();
        for (Document document : listSymbol) {
            var symbolInfo = SymbolInfo.builder().id(document.getString("_id")).build();
            symbolInfos.add(symbolInfo);
        }

        return symbolInfos;
    }
}
