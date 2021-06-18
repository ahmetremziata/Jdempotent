package com.trendyol.jtempotent.couchbase;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.ExistsResult;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.UpsertOptions;
import com.trendyol.jdempotent.core.model.IdempotencyKey;
import com.trendyol.jdempotent.core.model.IdempotentRequestResponseWrapper;
import com.trendyol.jdempotent.core.model.IdempotentRequestWrapper;
import com.trendyol.jdempotent.core.model.IdempotentResponseWrapper;
import com.trendyol.jdempotent.couchbase.CouchbaseConfig;
import com.trendyol.jdempotent.couchbase.CouchbaseIdempotentRepository;
import com.trendyol.jdempotent.couchbase.helper.DateHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CouchbaseIdempotentRepositoryTest {
  @InjectMocks
  private CouchbaseIdempotentRepository couchbaseIdempotentRepository;
  @Mock
  private CouchbaseConfig couchbaseConfig;
  @Mock
  private Collection collection;
  @Captor
  private ArgumentCaptor<IdempotentRequestResponseWrapper> captor;
  @Captor
  private ArgumentCaptor<UpsertOptions> upsertOptionCaptor;
  @Mock
  private DateHelper dateHelper;

  @BeforeEach
  public void setUp() {
    couchbaseIdempotentRepository = new CouchbaseIdempotentRepository(couchbaseConfig,
        collection, dateHelper);
  }

  @Test
  public void given_an_available_object_when_couchbase_contains_then_return_true() {
    //Given
    IdempotencyKey idempotencyKey = new IdempotencyKey("key");
    ExistsResult existsResult = mock(ExistsResult.class);
    when(existsResult.exists()).thenReturn(true);
    when(collection.exists(idempotencyKey.getKeyValue())).thenReturn(existsResult);

    //When
    Boolean isContain = couchbaseIdempotentRepository.contains(idempotencyKey);

    //Then
    verify(collection, times(1)).exists(idempotencyKey.getKeyValue());
    assertTrue(isContain);
  }

  @Test
  public void given_an_available_object_when_couchbase_contains_then_return_false() {
    //Given
    IdempotencyKey idempotencyKey = new IdempotencyKey("key");
    ExistsResult existsResult = mock(ExistsResult.class);
    when(existsResult.exists()).thenReturn(false);
    when(collection.exists(idempotencyKey.getKeyValue())).thenReturn(existsResult);

    //When
    Boolean isContain = couchbaseIdempotentRepository.contains(idempotencyKey);

    //Then
    verify(collection, times(1)).exists(idempotencyKey.getKeyValue());
    assertFalse(isContain);
  }

  @Test
  public void given_an_available_object_when_couchbase_get_response_then_return_expected_idempotent_response_wrapper() {
    //Given
    IdempotencyKey idempotencyKey = new IdempotencyKey("key");
    IdempotentRequestResponseWrapper wrapper = new IdempotentRequestResponseWrapper();
    GetResult getResult = mock(GetResult.class);
    when(getResult.contentAs(IdempotentRequestResponseWrapper.class)).thenReturn(wrapper);
    when(collection.get(idempotencyKey.getKeyValue())).thenReturn(getResult);

    //When
    IdempotentResponseWrapper result = couchbaseIdempotentRepository.getResponse(idempotencyKey);

    //Then
    verify(collection, times(1)).get(idempotencyKey.getKeyValue());
    assertEquals(result, wrapper.getResponse());
  }

  @Test
  public void given_an_available_object_when_couchbase_store_then_collection_insert_once_time() {
    //Given
    IdempotencyKey idempotencyKey = new IdempotencyKey("key");
    IdempotentRequestWrapper wrapper = new IdempotentRequestWrapper();
    IdempotentRequestResponseWrapper responseWrapper = new IdempotentRequestResponseWrapper(wrapper);

    //When
    couchbaseIdempotentRepository.store(idempotencyKey, wrapper);

    //Then
    verify(collection, times(1)).insert(eq(idempotencyKey.getKeyValue()), captor.capture());
    IdempotentRequestResponseWrapper idempotentRequestResponseWrapper = captor.getValue();
    assertEquals(idempotentRequestResponseWrapper.getResponse(), responseWrapper.getResponse());
  }

  @Test
  public void given_an_available_object_when_couchbase_store_with_ttl_and_time_unit_is_days_then_collection_insert_once_time() {
    //Given
    IdempotencyKey idempotencyKey = new IdempotencyKey("key");
    IdempotentRequestWrapper wrapper = new IdempotentRequestWrapper();
    Long ttl = 1L;
    TimeUnit timeUnit = TimeUnit.DAYS;
    Duration duration = Duration.ofDays(1L);
    when(dateHelper.getDurationByTtlAndTimeUnit(any(), any())).thenReturn(duration);

    IdempotentRequestResponseWrapper responseWrapper = new IdempotentRequestResponseWrapper(wrapper);

    //When
    couchbaseIdempotentRepository.store(idempotencyKey, wrapper, ttl, timeUnit);

    //Then
    verify(collection, times(1)).upsert(eq(idempotencyKey.getKeyValue()),
        captor.capture(),
        upsertOptionCaptor.capture());
    IdempotentRequestResponseWrapper idempotentRequestResponseWrapper = captor.getValue();
    assertEquals(idempotentRequestResponseWrapper.getResponse(), responseWrapper.getResponse());
    verify(dateHelper, times(1)).getDurationByTtlAndTimeUnit(ttl, timeUnit);
  }

  @Test
  public void given_an_available_object_when_couchbase_remove_then_collection_insert_once_time() {
    //Given
    IdempotencyKey idempotencyKey = new IdempotencyKey("key");
    IdempotentRequestWrapper wrapper = new IdempotentRequestWrapper();

    //When
    couchbaseIdempotentRepository.remove(idempotencyKey);

    //Then
    verify(collection, times(1)).remove(eq(idempotencyKey.getKeyValue()));
  }

  @Test
  public void given_idempotency_key_and_request_object_when_set_response_then_set_value_to_couchbase() {
    //Given
    IdempotencyKey key = new IdempotencyKey("key");
    IdempotentRequestWrapper request = new IdempotentRequestWrapper(123L);
    IdempotentResponseWrapper response = new IdempotentResponseWrapper("response");
    ExistsResult existsResult = mock(ExistsResult.class);
    when(existsResult.exists()).thenReturn(true);
    when(collection.exists(key.getKeyValue())).thenReturn(existsResult);
    IdempotentRequestResponseWrapper wrapper = new IdempotentRequestResponseWrapper();
    GetResult getResult = mock(GetResult.class);
    when(getResult.contentAs(IdempotentRequestResponseWrapper.class)).thenReturn(wrapper);
    when(collection.get(key.getKeyValue())).thenReturn(getResult);

    //When
    couchbaseIdempotentRepository.setResponse(key, request, response);

    //Then
    var argumentCaptor = ArgumentCaptor.forClass(IdempotentRequestResponseWrapper.class);
    verify(collection).upsert(eq(key.getKeyValue()), argumentCaptor.capture());
    IdempotentRequestResponseWrapper value = argumentCaptor.getValue();
    assertEquals(wrapper.getResponse().getResponse(), "response");
    assertEquals(value.getResponse().getResponse(), "response");
  }

  @Test
  public void given_idempotency_key_and_request_object_when_key_not_contain_then_not_set_value_to_couchbase() {
    //Given
    IdempotencyKey key = new IdempotencyKey("key");
    IdempotentRequestWrapper request = new IdempotentRequestWrapper(123L);
    IdempotentResponseWrapper response = new IdempotentResponseWrapper("response");
    ExistsResult existsResult = mock(ExistsResult.class);
    when(existsResult.exists()).thenReturn(false);
    when(collection.exists(key.getKeyValue())).thenReturn(existsResult);

    //When
    couchbaseIdempotentRepository.setResponse(key, request, response);

    //Then
    verify(collection, times(0)).upsert(any(), any());
  }

  @Test
  public void given_idempotency_key_and_request_object_when_set_response_then_set_value_to_couchbase_with_ttl() {
    //Given
    IdempotencyKey key = new IdempotencyKey("key");
    IdempotentRequestWrapper request = new IdempotentRequestWrapper(123L);
    IdempotentResponseWrapper response = new IdempotentResponseWrapper("response");
    ExistsResult existsResult = mock(ExistsResult.class);
    when(existsResult.exists()).thenReturn(true);
    when(collection.exists(key.getKeyValue())).thenReturn(existsResult);
    IdempotentRequestResponseWrapper wrapper = new IdempotentRequestResponseWrapper();
    GetResult getResult = mock(GetResult.class);
    when(getResult.contentAs(IdempotentRequestResponseWrapper.class)).thenReturn(wrapper);
    when(collection.get(key.getKeyValue())).thenReturn(getResult);
    Long ttl = 1L;
    TimeUnit timeUnit = TimeUnit.DAYS;
    Duration duration = Duration.ofDays(1L);
    when(dateHelper.getDurationByTtlAndTimeUnit(any(), any())).thenReturn(duration);

    //When
    couchbaseIdempotentRepository.setResponse(key, request, response, ttl, timeUnit);

    //Then
    verify(collection, times(1)).upsert(eq(key.getKeyValue()),
        captor.capture(),
        upsertOptionCaptor.capture());
    verify(dateHelper, times(1)).getDurationByTtlAndTimeUnit(ttl, timeUnit);
  }

  @Test
  public void given_idempotency_key_and_request_object_and_duration_when_key_not_contain_then_not_set_value_to_couchbase() {
    //Given
    IdempotencyKey key = new IdempotencyKey("key");
    IdempotentRequestWrapper request = new IdempotentRequestWrapper(123L);
    IdempotentResponseWrapper response = new IdempotentResponseWrapper("response");
    ExistsResult existsResult = mock(ExistsResult.class);
    when(existsResult.exists()).thenReturn(false);
    when(collection.exists(key.getKeyValue())).thenReturn(existsResult);
    Long ttl = 1L;
    TimeUnit timeUnit = TimeUnit.DAYS;

    //When
    couchbaseIdempotentRepository.setResponse(key, request, response, ttl, timeUnit);

    //Then
    verify(collection, times(0)).upsert(any(), any(), any());
    verify(dateHelper, times(0)).getDurationByTtlAndTimeUnit(any(), any());
  }
}