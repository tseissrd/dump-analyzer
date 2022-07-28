/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sovereign
 */
public class HttpAccessRecord {
  
  private static final Pattern ACCESS_LOG_REGEXP 
    = Pattern.compile("^([0-9.]++)[^\\[]++\\[([^\\]]++)\\]\\s\"(?:\\\\\"|[^\"])++\"\\s([0-9]++).*+$");
  
  private static final Pattern DATE_REGEXP
    = Pattern.compile("^([0-9]++)/([a-zA-Z]++)/([0-9]++):([0-9]++):([0-9]++):([0-9]++)\\s([+-])([0-9]++)$");
  
  private final short code;
  private final Instant date;
  private final String source;
  
  public HttpAccessRecord(String line) {
    Matcher matcher = ACCESS_LOG_REGEXP.matcher(line);
    matcher.matches();
    this.source = matcher.group(1);
    String dateString = matcher.group(2);
    String codeString = matcher.group(3);
    
    this.code = Short.parseShort(codeString);
    this.date = parseDate(dateString);
  }
  
  public short getCode() {
    return code;
  }
  
  public Instant getDate() {
    return date;
  }
  
  public String getSource() {
    return source;
  }

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
  
}
