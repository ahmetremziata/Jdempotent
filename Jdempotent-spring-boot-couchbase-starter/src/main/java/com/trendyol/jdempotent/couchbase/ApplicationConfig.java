package com.trendyol.jdempotent.couchbase;

import com.couchbase.client.java.Collection;
import com.trendyol.jdempotent.core.aspect.IdempotentAspect;
import com.trendyol.jdempotent.core.callback.ErrorConditionalCallback;
import com.trendyol.jdempotent.couchbase.helper.DateHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix="jdempotent", name = "enable",
        havingValue = "true",
        matchIfMissing = true)
public class ApplicationConfig {

    private final CouchbaseConfig couchbaseConfig;

    public ApplicationConfig(CouchbaseConfig couchbaseConfig) {
        this.couchbaseConfig = couchbaseConfig;
    }

    @Bean
    @ConditionalOnProperty(
            prefix="jdempotent", name = "enable",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnClass(ErrorConditionalCallback.class)
    public IdempotentAspect getIdempotentAspect(Collection collection, ErrorConditionalCallback errorConditionalCallback, DateHelper dateHelper) {
        return new IdempotentAspect(new CouchbaseIdempotentRepository(couchbaseConfig, collection, dateHelper), errorConditionalCallback);
    }

    @Bean
    public IdempotentAspect getIdempotentAspect(Collection collection, DateHelper dateHelper) {
        return new IdempotentAspect(new CouchbaseIdempotentRepository(couchbaseConfig, collection, dateHelper));
    }

}
