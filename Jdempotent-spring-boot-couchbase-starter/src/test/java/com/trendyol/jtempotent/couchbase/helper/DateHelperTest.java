package com.trendyol.jtempotent.couchbase.helper;

import com.trendyol.jdempotent.couchbase.helper.DateHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DateHelperTest {
  @InjectMocks
    private DateHelper dateHelper;

  @Test
  public void given_ttl_and_timeunit_when_time_unit_is_day_then_return_duration_with_day() {
    //Given
    Long ttl = 1L;
    TimeUnit timeUnit = TimeUnit.DAYS;

    //When
    Duration duration = dateHelper.getDurationByTtlAndTimeUnit(ttl, timeUnit);

    //Then
    assertEquals(Duration.ofDays(ttl), duration);
  }

  @Test
  public void given_ttl_and_timeunit_when_time_unit_is_hour_then_return_duration_with_day() {
    //Given
    Long ttl = 1L;
    TimeUnit timeUnit = TimeUnit.HOURS;

    //When
    Duration duration = dateHelper.getDurationByTtlAndTimeUnit(ttl, timeUnit);

    //Then
    assertEquals(Duration.ofHours(ttl), duration);
  }

  @Test
  public void given_ttl_and_timeunit_when_time_unit_is_milisecond_then_return_duration_with_day() {
    //Given
    Long ttl = 1L;
    TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    //When
    Duration duration = dateHelper.getDurationByTtlAndTimeUnit(ttl, timeUnit);

    //Then
    assertEquals(Duration.ofMillis(ttl), duration);
  }
}
