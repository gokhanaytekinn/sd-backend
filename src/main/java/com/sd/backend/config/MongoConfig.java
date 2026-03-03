package com.sd.backend.config;

import com.sd.backend.model.enums.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();

        // UserTier Converters
        converters.add(new UserTierWritingConverter());
        converters.add(new UserTierReadingConverter());

        // CurrencyCode Converters
        converters.add(new CurrencyCodeWritingConverter());
        converters.add(new CurrencyCodeReadingConverter());

        // SubscriptionStatus Converters
        converters.add(new SubscriptionStatusWritingConverter());
        converters.add(new SubscriptionStatusReadingConverter());

        // BillingCycle Converters
        converters.add(new BillingCycleWritingConverter());
        converters.add(new BillingCycleReadingConverter());

        // TransactionStatus Converters
        converters.add(new TransactionStatusWritingConverter());
        converters.add(new TransactionStatusReadingConverter());

        // TransactionType Converters
        converters.add(new TransactionTypeWritingConverter());
        converters.add(new TransactionTypeReadingConverter());

        return new MongoCustomConversions(converters);
    }

    // UserTier
    public static class UserTierWritingConverter implements Converter<UserTier, Integer> {
        @Override
        public Integer convert(UserTier source) {
            return source.getValue();
        }
    }

    public static class UserTierReadingConverter implements Converter<Integer, UserTier> {
        @Override
        public UserTier convert(Integer source) {
            return UserTier.fromValue(source);
        }
    }

    // CurrencyCode
    public static class CurrencyCodeWritingConverter implements Converter<CurrencyCode, Integer> {
        @Override
        public Integer convert(CurrencyCode source) {
            return source.getValue();
        }
    }

    public static class CurrencyCodeReadingConverter implements Converter<Integer, CurrencyCode> {
        @Override
        public CurrencyCode convert(Integer source) {
            return CurrencyCode.fromValue(source);
        }
    }

    // SubscriptionStatus
    public static class SubscriptionStatusWritingConverter implements Converter<SubscriptionStatus, Integer> {
        @Override
        public Integer convert(SubscriptionStatus source) {
            return source.getValue();
        }
    }

    public static class SubscriptionStatusReadingConverter implements Converter<Integer, SubscriptionStatus> {
        @Override
        public SubscriptionStatus convert(Integer source) {
            return SubscriptionStatus.fromValue(source);
        }
    }

    // BillingCycle
    public static class BillingCycleWritingConverter implements Converter<BillingCycle, Integer> {
        @Override
        public Integer convert(BillingCycle source) {
            return source.getValue();
        }
    }

    public static class BillingCycleReadingConverter implements Converter<Integer, BillingCycle> {
        @Override
        public BillingCycle convert(Integer source) {
            return BillingCycle.fromValue(source);
        }
    }

    // TransactionStatus
    public static class TransactionStatusWritingConverter implements Converter<TransactionStatus, Integer> {
        @Override
        public Integer convert(TransactionStatus source) {
            return source.getValue();
        }
    }

    public static class TransactionStatusReadingConverter implements Converter<Integer, TransactionStatus> {
        @Override
        public TransactionStatus convert(Integer source) {
            return TransactionStatus.fromValue(source);
        }
    }

    // TransactionType
    public static class TransactionTypeWritingConverter implements Converter<TransactionType, Integer> {
        @Override
        public Integer convert(TransactionType source) {
            return source.getValue();
        }
    }

    public static class TransactionTypeReadingConverter implements Converter<Integer, TransactionType> {
        @Override
        public TransactionType convert(Integer source) {
            return TransactionType.fromValue(source);
        }
    }
}
