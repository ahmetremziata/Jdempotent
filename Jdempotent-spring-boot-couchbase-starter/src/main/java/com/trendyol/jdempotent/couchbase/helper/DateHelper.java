package com.trendyol.jdempotent.couchbase.helper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DateHelper {
  public Duration getDurationByTtlAndTimeUnit(Long ttl, TimeUnit timeUnit) {
    if (TimeUnit.DAYS.equals(timeUnit)) {
      return Duration.ofDays(ttl);
    } else if (TimeUnit.HOURS.equals(timeUnit)) {
      return Duration.ofHours(ttl);
    } else {
      return Duration.ofMillis(ttl);
    }
  }
}
