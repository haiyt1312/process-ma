package com.jbsv.processma.process;

import com.jbsv.processma.model.EventSymbolInfo;
import com.jbsv.processma.model.SymbolDaily;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableAsync
@Slf4j
public class ProcessMA {
    private final MongoDatabase mongoDatabase;

    public ProcessMA(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Async
    @EventListener
    public void saveSymbol(EventSymbolInfo eventSymbolInfo) {
        MongoCollection<Document> collectionSymbolDaily = mongoDatabase.getCollection("c_symbol_daily");
        FindIterable<Document> listSymbolDaily = collectionSymbolDaily.find(
                Filters.and(
                        Filters.eq("s", eventSymbolInfo.getSymbol()),
                        Filters.lte("dt", eventSymbolInfo.getEndDate())
                )
        ).sort(Sorts.descending("dt"));

        List<SymbolDaily> symbolDailies = new ArrayList<>();

        for (Document document : listSymbolDaily) {
            SymbolDaily symbolDaily = new SymbolDaily();
            symbolDaily.setId(document.getString("_id"));
            symbolDaily.setDate(document.getDate("dt"));
            symbolDaily.setS(document.getString("s"));
            symbolDaily.setC(document.getDouble("c"));
            symbolDaily.setA5b(document.getDouble("a5b"));
            symbolDaily.setA10b(document.getDouble("a10b"));
            symbolDaily.setA20b(document.getDouble("a20b"));
            symbolDaily.setA60b(document.getDouble("a60b"));
            symbolDailies.add(symbolDaily);
        }

        List<SymbolDaily> processSymbol;
        if (eventSymbolInfo.getStartDate() != null) {
            processSymbol = processSymbol(eventSymbolInfo.getStartDate(), symbolDailies);
        } else {
            processSymbol = processSymbolAll(symbolDailies);
        }

        List<WriteModel<Document>> bulkOperations = new ArrayList<>();

        for (SymbolDaily symbolDaily : processSymbol) {
            String documentId = symbolDaily.getId();
            Document filter = new Document("_id", documentId);
            Document doc = setDocument(symbolDaily);
            Document update = new Document("$set", doc);

            UpdateManyModel<Document> updateModel = new UpdateManyModel<>(filter, update);

            bulkOperations.add(updateModel);
        }

        BulkWriteOptions options = new BulkWriteOptions().ordered(false);
        collectionSymbolDaily.bulkWrite(bulkOperations, options);

        log.info("save symbol: {}", eventSymbolInfo.getSymbol());
    }

    public Document setDocument(SymbolDaily symbolDaily) {
        Document setDoc = new Document();
        setDoc.put("a5b", symbolDaily.getA5b());
        setDoc.put("a10b", symbolDaily.getA10b());
        setDoc.put("a20b", symbolDaily.getA20b());
        setDoc.put("a60b", symbolDaily.getA60b());
        return setDoc;
    }

    public List<SymbolDaily> processSymbol(Date startDate, List<SymbolDaily> data) {

        List<Double> ma5List = calculateMA(data, 5);
        for (int i = 0; i < ma5List.size(); i++) {
            if (startDate.compareTo(data.get(i).getDate()) > 0)
                continue;
            data.get(i).setA5b(ma5List.get(i));
        }

        List<Double> ma10List = calculateMA(data, 10);
        for (int i = 0; i < ma10List.size(); i++) {
            if (startDate.compareTo(data.get(i).getDate()) > 0)
                continue;
            data.get(i).setA10b(ma10List.get(i));
        }

        List<Double> ma20List = calculateMA(data, 20);
        for (int i = 0; i < ma20List.size(); i++) {
            if (startDate.compareTo(data.get(i).getDate()) > 0)
                continue;
            data.get(i).setA20b(ma20List.get(i));
        }

        List<Double> ma60List = calculateMA(data, 60);
        for (int i = 0; i < ma60List.size(); i++) {
            if (startDate.compareTo(data.get(i).getDate()) > 0)
                continue;
            data.get(i).setA60b(ma60List.get(i));
        }

        return data;
    }

    public List<SymbolDaily> processSymbolAll(List<SymbolDaily> data) {
        List<Double> ma5List = calculateMA(data, 5);
        for (int i = 0; i < ma5List.size(); i++) {
            data.get(i).setA5b(ma5List.get(i));
        }

        List<Double> ma10List = calculateMA(data, 10);
        for (int i = 0; i < ma10List.size(); i++) {
            data.get(i).setA10b(ma10List.get(i));
        }

        List<Double> ma20List = calculateMA(data, 20);
        for (int i = 0; i < ma20List.size(); i++) {
            data.get(i).setA20b(ma20List.get(i));
        }

        List<Double> ma60List = calculateMA(data, 60);
        for (int i = 0; i < ma60List.size(); i++) {
            data.get(i).setA60b(ma60List.get(i));
        }

        return data;
    }

    public List<Double> calculateMA(List<SymbolDaily> symbolDailies, Integer maPeriod) {
        List<Double> listPrice = new ArrayList<>();

        for (int i = maPeriod - 1; i < symbolDailies.size(); i++) {
            List<Double> list = new ArrayList<>();

            for (int j = i; j >= i - maPeriod + 1; j--) {
                list.add(symbolDailies.get(j).getC());
            }

            double ma = list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            listPrice.add(ma);
        }

        return listPrice;
    }
}
