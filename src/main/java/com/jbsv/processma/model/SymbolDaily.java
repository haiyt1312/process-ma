package com.jbsv.processma.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolDaily {
    private String id;
    public Date date;
    private String s;
    private Double c;
    private Double a5b; // average price 5 days (khong tinh ngay hien tai)
    private Double a10b; // average price 10 days (khong tinh ngay hien tai)
    private Double a20b; // average price 20 days (khong tinh ngay hien tai)
    private Double a60b; // average price 60 days (khong tinh ngay hien tai)
}
