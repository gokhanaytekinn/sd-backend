package com.sd.backend.config;

import com.sd.backend.model.enums.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
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
        converters.add(new UserTierStringReadingConverter());

        // CurrencyCode Converters
        converters.add(new CurrencyCodeWritingConverter());
        converters.add(new CurrencyCodeReadingConverter());
        converters.add(new CurrencyCodeStringReadingConverter());

        // SubscriptionStatus Converters
        converters.add(new SubscriptionStatusWritingConverter());
        converters.add(new SubscriptionStatusReadingConverter());
        converters.add(new SubscriptionStatusStringReadingConverter());

        // BillingCycle Converters
        converters.add(new BillingCycleWritingConverter());
        converters.add(new BillingCycleReadingConverter());
        converters.add(new BillingCycleStringReadingConverter());

        // TransactionStatus Converters
        converters.add(new TransactionStatusWritingConverter());
        converters.add(new TransactionStatusReadingConverter());

        // TransactionType Converters
        converters.add(new TransactionTypeWritingConverter());
        converters.add(new TransactionTypeReadingConverter());

        return new MongoCustomConversions(converters);
    }

    // UserTier
    @WritingConverter
    public static class UserTierWritingConverter implements Converter<UserTier, Integer> {
        @Override
        public Integer convert(UserTier source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public static class UserTierReadingConverter implements Converter<Integer, UserTier> {
        @Override
        public UserTier convert(Integer source) {
            return UserTier.fromValue(source);
        }
    }

    // CurrencyCode
    @WritingConverter
    public static class CurrencyCodeWritingConverter implements Converter<CurrencyCode, Integer> {
        @Override
        public Integer convert(CurrencyCode source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public static class CurrencyCodeReadingConverter implements Converter<Integer, CurrencyCode> {
        @Override
        public CurrencyCode convert(Integer source) {
            return CurrencyCode.fromValue(source);
        }
    }

    // SubscriptionStatus
    @WritingConverter
    public static class SubscriptionStatusWritingConverter implements Converter<SubscriptionStatus, Integer> {
        @Override
        public Integer convert(SubscriptionStatus source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public static class SubscriptionStatusReadingConverter implements Converter<Integer, SubscriptionStatus> {
        @Override
        public SubscriptionStatus convert(Integer source) {
            return SubscriptionStatus.fromValue(source);
        }
    }

    // BillingCycle
    @WritingConverter
    public static class BillingCycleWritingConverter implements Converter<BillingCycle, Integer> {
        @Override
        public Integer convert(BillingCycle source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public static class BillingCycleReadingConverter implements Converter<Integer, BillingCycle> {
        @Override
        public BillingCycle convert(Integer source) {
            return BillingCycle.fromValue(source);
        }
    }

    @ReadingConverter
    public static class UserTierStringReadingConverter implements Converter<String, UserTier> {
        @Override
        public UserTier convert(String source) {
            try {
                return UserTier.valueOf(source.toUpperCase());
            } catch (Exception e) {
                return UserTier.FREE;
            }
        }
    }

    @ReadingConverter
    public static class CurrencyCodeStringReadingConverter implements Converter<String, CurrencyCode> {
        @Override
        public CurrencyCode convert(String source) {
            try {
                return CurrencyCode.valueOf(source.toUpperCase());
            } catch (Exception e) {
                return CurrencyCode.values()[0];
            }
        }
    }

    @ReadingConverter
    public static class SubscriptionStatusStringReadingConverter implements Converter<String, SubscriptionStatus> {
        @Override
        public SubscriptionStatus convert(String source) {
            try {
                return SubscriptionStatus.valueOf(source.toUpperCase());
            } catch (Exception e) {
                return SubscriptionStatus.ACTIVE;
            }
        }
    }

    @ReadingConverter
    public static class BillingCycleStringReadingConverter implements Converter<String, BillingCycle> {
        @Override
        public BillingCycle convert(String source) {
            try {
                return BillingCycle.valueOf(source.toUpperCase());
            } catch (Exception e) {
                return BillingCycle.MONTHLY;
            }
        }
    }

    // TransactionStatus
    @WritingConverter
    public static class TransactionStatusWritingConverter implements Converter<TransactionStatus, Integer> {
        @Override
        public Integer convert(TransactionStatus source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public static class TransactionStatusReadingConverter implements Converter<Integer, TransactionStatus> {
        @Override
        public TransactionStatus convert(Integer source) {
            return TransactionStatus.fromValue(source);
        }
    }

    // TransactionType
    @WritingConverter
    public static class TransactionTypeWritingConverter implements Converter<TransactionType, Integer> {
        @Override
        public Integer convert(TransactionType source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public static class TransactionTypeReadingConverter implements Converter<Integer, TransactionType> {
        @Override
        public TransactionType convert(Integer source) {
            return TransactionType.fromValue(source);
        }
    }
}
