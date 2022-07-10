/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import com.example.springapp.dumpanalyzer.data.json.JsonOutputWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;
import java.util.concurrent.TimeUnit;
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
    = Pattern.compile("^([0-9.]+)[^\\[]+\\[([^\\]]+)\\]\\s\"[^\"]+\"\\s([0-9]+).*$");
  
  private static final Pattern DATE_REGEXP
    = Pattern.compile("^([0-9]+)/([a-zA-Z]+)/([0-9]+):([0-9]+):([0-9]+):([0-9]+)\\s([+-])([0-9]+)$");

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
  
  @Override
  public void process(InputStream in, OutputStream out) {
    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(
        in,
        Charset.forName("utf-8")
      )
    )) {
      try (JsonOutputWriter writer = new JsonOutputWriter(
        new OutputStreamWriter(
          out,
          Charset.forName("utf-8")
        )
      )) {
        String line;
        
        String source;
        String dateString;
        String date;
        short code;
        
        Map<String, Map<Short, Long>> sources = new HashMap<>();
        
        Map<Short, Long> codes;
        
        String lastDate = null;
        Map<String, Map<Short, Long>> sourcesForLastDate = null;
        
        writer.writeRaw("[");
        
        while ((line = reader.readLine()) != null) {
          Matcher matcher = ACCESS_LOG_REGEXP.matcher(line);
          matcher.matches();
          source = matcher.group(1);
          dateString = matcher.group(2);
          code = Short.parseShort(matcher.group(3));
          
          date = parseDate(dateString)
            .truncatedTo(
              ChronoUnit.MINUTES
            ).toString();
          
          if (
            nonNull(lastDate)
            && (!date.equals(lastDate))
          ) {
            writer.writeMap(
              Map.of(
                lastDate,
                sourcesForLastDate
              )
            );
            
            writer.writeRaw(",");
            
            sources = new HashMap<>();
          }
          
          System.out.println(line);
          System.out.println("V");
          System.out.println(source);
          System.out.println(dateString);
          System.out.println(date.toString());
          System.out.println(code);
          System.out.println("^");
          System.out.println("");
          
          // if (!data.containsKey(date)) {
//            sources = new HashMap<>();
            
//            data.put(
//              date,
//              sources
//            );
//          } else {
//            sources = data.get(date);
//          }
          
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
            ) + 1
          );
          
          lastDate = date;
          sourcesForLastDate = sources;
        }
        
        if (nonNull(sourcesForLastDate))
          writer.writeMap(
            Map.of(
              lastDate,
              sourcesForLastDate
            )
          );
        
        writer.writeRaw("]");
      }
    } catch (Throwable ex) {
      Logger.getLogger(IhsHttpAccessProcessor.class.getName()).log(Level.SEVERE, null, ex);
      ex.printStackTrace(System.err);
      return;
//    } catch (IOException ex) {
//      Logger.getLogger(IhsHttpAccessProcessor.class.getName()).log(Level.SEVERE, null, ex);
//      return;
    }
  }

  @Override
  public boolean accepts(String type) {
    return true;
  }
  
}
