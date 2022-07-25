/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import com.example.springapp.dumpanalyzer.data.filter.Filter;
import com.example.springapp.dumpanalyzer.data.filter.Filter.FilterMode;
import com.example.springapp.dumpanalyzer.data.json.JsonOutputWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
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
  
  private static final Pattern ACCESS_LOG_REGEXP 
//    = Pattern.compile("^([0-9.]+)[^\\[]+\\[([^\\]]+)\\]\\s\"[^\"]+\"\\s([0-9]+).*$");
    = Pattern.compile("^([0-9.]++)[^\\[]++\\[([^\\]]++)\\]\\s\"(?:\\\\\"|[^\"])++\"\\s([0-9]++).*+$");
  
  private static final Pattern DATE_REGEXP
    = Pattern.compile("^([0-9]++)/([a-zA-Z]++)/([0-9]++):([0-9]++):([0-9]++):([0-9]++)\\s([+-])([0-9]++)$");

  private String parseMonth(String month) {
    if (month.equals("Jan")) {
      return "01";
    } else if (month.equals("Feb")) {
      return "02";
    } else if (month.equals("Mar")) {
      return "03";
    } else if (month.equals("Apr")) {
      return "04";
    } else if (month.equals("May")) {
      return "05";
    } else if (month.equals("Jun")) {
      return "06";
    } else if (month.equals("Jul")) {
      return "07";
    } else if (month.equals("Aug")) {
      return "08";
    } else if (month.equals("Sep")) {
      return "09";
    } else if (month.equals("Oct")) {
      return "10";
    } else if (month.equals("Nov")) {
      return "11";
    } else if (month.equals("Dec")) {
      return "12";
    } else {
      return "00";
    }
  }
  
  private Instant parseDate(String httpDate) {
    Matcher matcher = DATE_REGEXP.matcher(httpDate);
    matcher.matches();
    String isoDate = new StringBuilder(
        matcher.group(3)
      ).append("-")
      .append(
        parseMonth(
          matcher.group(2)
        )
      )
      .append("-")
      .append(matcher.group(1))
      .append("T")
      .append(matcher.group(4))
      .append(":")
      .append(matcher.group(5))
      .append(":")
      .append(matcher.group(6))
      .append(".000Z")
      .toString();
    
    Instant date = Instant.parse(isoDate);
    
    Duration offset = Duration.ofHours(
        Long.parseLong(
          matcher.group(8)
            .substring(0, 2)
        )
      ).plus(
        Duration.ofMinutes(
          Long.parseLong(
            matcher.group(8)
              .substring(2)
          )
        )
      );
    
    if (
      matcher.group(7)
        .equals("-")
    ) {
      date = date.plus(offset);
    } else if (
      matcher.group(7)
        .equals("+")
    ) {
      date = date.minus(offset);
    }
    
    return date;
  }
  
  private void processCodeByTime(
    BufferedReader reader,
    JsonOutputWriter writer,
    Filter filter
  )
  throws IOException {
    String line;
        
    String dateString;
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
        
        Matcher matcher = ACCESS_LOG_REGEXP.matcher(line);
        matcher.matches();
        dateString = matcher.group(2);
        codeString = matcher.group(3);
        code = Short.parseShort(codeString);

        if (!tableHeaders.contains(
          codeString
        )) {
          tableHeaders.add(
            codeString
          );
        }

        date = parseDate(dateString)
          .truncatedTo(
            ChronoUnit.MINUTES
          )
          .toString();
      

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
    JsonOutputWriter writer,
    Filter filter
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
        
        Matcher matcher = ACCESS_LOG_REGEXP.matcher(line);
        matcher.matches();
        source = matcher.group(1);
        codeString = matcher.group(3);
        code = Short.parseShort(codeString);

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
      
      BufferedReader filteredReader = new BufferedReader(reader) {
        private boolean skippedToStart = false;
        private long linesRead = 0;
        
        private void skipToStart()
        throws IOException {
          if (
            filter.equals(
              FilterMode.NONE
            )
          ) {
            // nothing to do
          } else if (
            filter.equals(
              FilterMode.TIME
            )
          ) {
          } else if (
            filter.equals(
              FilterMode.LINES
            )
          ) {
            long skipTo = -1;
            
            if (
              nonNull(
                filter.getStart()
              )
            ) {
              skipTo = Long.parseLong(
                filter.getStart()
              );
            }
            
            long filterEnd = -1;
            
            if (
              nonNull(
                filter.getEnd()
              )
            ) {
              filterEnd = Long.parseLong(
                filter.getEnd()
              );
            }
            
            if (
              (skipTo > 0)
              && (
                (filterEnd == -1)
                || (filterEnd >= skipTo)
              )
            ) {
              for (long skipped = 0; skipped < skipTo; skipped += 1) {
                if (
                  nonNull(
                    reader.readLine()
                  )
                )
                reader.readLine();
              }
            }
            
            linesRead = skipTo;
          } else if (
            filter.equals(
              FilterMode.PERCENT
            )
          ) {
          }
          
          skippedToStart = true;
        }
        
        private boolean checkEndFilter(String line) {
        }
        
        @Override
        public String readLine()
        throws IOException {
          if (!skippedToStart)
            skipToStart();
          
          String line = super.readLine();
          
          checkEndFilter()
          
          linesRead += 1;
        }
      };
      
      try (JsonOutputWriter writer = new JsonOutputWriter(
        new OutputStreamWriter(
          out,
          Charset.forName("utf-8")
        )
      )) {
        if (mode.equals("time")) {
          processCodeByTime(
            filteredReader,
            writer,
            filter
          );
        } else if (mode.equals("ip")) {
          processCodeBySource(
            filteredReader,
            writer,
            filter
          );
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
