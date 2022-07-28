/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import com.example.springapp.dumpanalyzer.data.filter.Filter;
import com.example.springapp.dumpanalyzer.data.json.JsonOutputWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Objects.nonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sovereign
 */
public class IhsHttpAccessProcessor
implements Processor {
  
  private static final IhsHttpAccessProcessor singleton;
  
  static {
    singleton = new IhsHttpAccessProcessor();
  }
  
  protected IhsHttpAccessProcessor() {}
  
  public static IhsHttpAccessProcessor getInstance() {
    return singleton;
  }
  
  private void processCodeByTime(
    BufferedReader reader,
    JsonOutputWriter writer
  )
  throws IOException {
    String line;
        
    String date;
    short code;
    String codeString;

    List<String> tableHeaders = new ArrayList<>();
    List<Map<String, String>> tableRows = new ArrayList<>();
    
    Map<String, String> parsingErrors = new HashMap<>();
    long parsingErrorsCount = 0;
    tableRows.add(parsingErrors);

    Map<Short, Long> codes = new HashMap<>();

    String lastDate = null;
    Map<Short, Long> codesForLastDate = null;
    
    tableHeaders.add("time");
    
    long rowTotal = 0;

    while ((line = reader.readLine()) != null) {
      try {
        HttpAccessRecord record = new HttpAccessRecord(line);
        date = record.getDate()
          .truncatedTo(
            ChronoUnit.MINUTES
          )
          .toString();
        
        code = record.getCode();
        codeString = Short.toString(code);

        if (!tableHeaders.contains(
          codeString
        )) {
          tableHeaders.add(
            codeString
          );
        }

        if (
          nonNull(lastDate)
          && (!date.equals(lastDate))
        ) {
          Map<String, String> row = new HashMap<>();

          row.put(
            "time",
            lastDate
          );

          row.put(
            "total",
            Long.toString(rowTotal)
          );

          codesForLastDate.forEach(
            (codeRecord, countRecord) -> {
              row.put(
                codeRecord.toString(),
                countRecord.toString()
              );
            }
          );

          tableRows.add(row);

          codes = new HashMap<>();
          rowTotal = 0;
        }

        codes.put(
          code,
          codes.getOrDefault(
            code,
            Long.valueOf(0)
          )
            + 1
        );

        lastDate = date;
        codesForLastDate = codes;
        rowTotal += 1;
      
      } catch (IllegalStateException ex) {
        parsingErrorsCount += 1;
        Logger.getLogger(
          IhsHttpAccessProcessor.class
            .getName()
        ).log(Level.SEVERE, "Could not parse line:\n{0}", line);
      }
    }
    
    if (nonNull(codesForLastDate)) {
      Map<String, String> row = new HashMap<>();
        
      row.put(
        "time",
        lastDate
      );
      
      row.put(
        "total",
        Long.toString(rowTotal)
      );

      codesForLastDate.forEach(
        (codeRecord, countRecord) -> {
          row.put(
            codeRecord.toString(),
            countRecord.toString()
          );
        }
      );

      tableRows.add(row);
    }
    
    tableHeaders.add("total");
    
    parsingErrors.put(
      "total",
      Long.toString(parsingErrorsCount)
    );
    parsingErrors.put(
      "time",
      "не удалось прочитать (записей)"
    );
    
    // JDK 8 FIX
    Map<String, Object> mapToWrite = new HashMap<>();
    mapToWrite.put("headers", tableHeaders);
    mapToWrite.put("rows", tableRows);
    
    writer.writeMap(
//      Map.of(
//        "headers", tableHeaders,
//        "rows", tableRows
//      )
      mapToWrite
    );
  }
  
  private void processCodeBySource(
    BufferedReader reader,
    JsonOutputWriter writer
  )
  throws IOException {
    String line;
        
    String source;
    short code;
    String codeString;

    List<String> tableHeaders = new ArrayList<>();
    List<Map<String, String>> tableRows = new ArrayList<>();
    
    Map<String, String> parsingErrors = new HashMap<>();
    long parsingErrorsCount = 0;
    tableRows.add(parsingErrors);

    Map<String, Map<Short, Long>> sources = new HashMap<>();
    
    Map<Short, Long> codes;
    
    tableHeaders.add("source");

    while ((line = reader.readLine()) != null) {
      try {
        HttpAccessRecord record = new HttpAccessRecord(line);
        
        code = record.getCode();
        codeString = Short.toString(code);
        source = record.getSource();

        if (!tableHeaders.contains(
          codeString
        )) {
          tableHeaders.add(
            codeString
          );
        }

        if (!sources.containsKey(source)) {
          codes = new HashMap<>();
          sources.put(source, codes);
        } else {
          codes = sources.get(source);
        }

        codes.put(
          code,
          codes.getOrDefault(
            code,
            Long.valueOf(0)
          )
            + 1
        );
      
      } catch (IllegalStateException ex) {
        parsingErrorsCount += 1;
        Logger.getLogger(
          IhsHttpAccessProcessor.class
            .getName()
        ).log(Level.SEVERE, "Could not parse line:\n{0}", line);
      }
    }
    
    sources.entrySet()
      .stream()
      .parallel()
      .map(entry -> {
        Map<String, String> row = new HashMap<>();
        long rowTotal = 0;
        
        row.put(
          "source",
          entry.getKey()
        );
        
        for (
          Entry<Short, Long> codeEntry : entry.getValue()
            .entrySet()
        ) {
          rowTotal += codeEntry.getValue();

          row.put(
            codeEntry.getKey()
              .toString(),
            codeEntry.getValue()
              .toString()
          );
        }
        
        row.put(
          "total",
          Long.toString(rowTotal)
        );
        
        return row;
      })
      .sequential()
      .forEach(row -> tableRows.add(row));
    
    parsingErrors.put(
      "total",
      Long.toString(parsingErrorsCount)
    );
    parsingErrors.put(
      "source",
      "не удалось прочитать (записей)"
    );
    
    tableHeaders.add("total");
    
    // JDK 8 FIX
    Map<String, Object> mapToWrite = new HashMap<>();
    mapToWrite.put("headers", tableHeaders);
    mapToWrite.put("rows", tableRows);
    
    writer.writeMap(
      mapToWrite
//      Map.of(
//        "headers", tableHeaders,
//        "rows", tableRows
//      )
    );
  }
  
  @Override
  public void process(
    InputStream in,
    OutputStream out,
    String type,
    String mode,
    Filter filter
  ) {
    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(
        in,
        Charset.forName("utf-8")
      )
    )) {
      
      BufferedReader filteredReader = filter.filteredReader(reader);
      
      try (
        Writer rawWriter = new OutputStreamWriter(
          out,
          Charset.forName("utf-8")
        );
        JsonOutputWriter writer = new JsonOutputWriter(
          rawWriter
        )
      ) {
        if (mode.equals("time")) {
          processCodeByTime(
            filteredReader,
            writer
          );
        } else if (mode.equals("ip")) {
          processCodeBySource(
            filteredReader,
            writer
          );
        } else if (mode.equals("text")) {
          String line;
          
          while (
            nonNull(
              line = filteredReader.readLine()
            )
          ) {
            rawWriter.write(line);
            rawWriter.write("\n");
          }
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(IhsHttpAccessProcessor.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }
  }

  @Override
  public boolean accepts(String type, String mode) {
    return true;
  }
  
}
