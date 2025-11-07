# Kafka Consumer Fix - Wallet Service

## Problem Summary
The wallet service was not consuming messages from the `user.registered` topic even though the auth service was successfully publishing events to it.

## Root Cause
The issue was in the `KafkaConsumerConfig.java` file. The consumer was configured with a **JsonDeserializer** but with an incorrect type configuration:

```java
config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.String");
```

This created a mismatch between:
- **Producer (Auth Service)**: Sending JSON objects using `JsonSerializer`
- **Consumer (Wallet Service)**: Trying to deserialize with `JsonDeserializer` configured for String type

## Solution Applied

### 1. Fixed Deserializer Configuration
Changed from `JsonDeserializer` to `StringDeserializer` in `KafkaConsumerConfig.java`:

**Before:**
```java
config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.String");
```

**After:**
```java
config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class.getName());
// Removed: JsonDeserializer.VALUE_DEFAULT_TYPE config
```

This allows the consumer to receive the raw JSON string, which is then manually deserialized in the consumer method using ObjectMapper.

### 2. Enhanced Logging
Added additional logging to `UserEventConsumer.java`:
- PostConstruct initialization log to verify consumer is ready
- Raw JSON logging before deserialization
- Clear message reception indicator

## Why This Works

1. **Auth Service** publishes events as JSON objects using Spring's `JsonSerializer`
2. **Wallet Service** now receives them as raw JSON strings using `StringDeserializer`
3. **UserEventConsumer** manually deserializes the JSON string to `UserRegisteredEvent` object using Jackson's `ObjectMapper`

This approach is actually more flexible as it:
- Provides better error handling
- Allows manual JSON validation before processing
- Makes debugging easier with raw JSON logging

## Testing Steps

1. Rebuild the wallet service:
   ```cmd
   cd D:\sigma\wallet_service\wallet_service
   mvnw.cmd clean package
   ```

2. Restart the wallet service

3. Check logs for initialization message:
   ```
   ==================================================
   UserEventConsumer initialized and ready to consume from topic: user.registered
   ==================================================
   ```

4. Register a new user in the auth service

5. Verify in wallet service logs:
   ```
   === RECEIVED MESSAGE FROM user.registered TOPIC ===
   Raw JSON: {...}
   Received user.registered event for user: {userId}
   Wallet created for user: {userId}
   ```

## Configuration Consistency Check
✅ Both services using same Kafka broker: `localhost:9094`
✅ Consumer group ID configured: `wallet-service-group`
✅ Auto offset reset set to `earliest` (won't miss messages)
✅ Error handling with retry mechanism (3 retries with 2s intervals)
✅ Dead Letter Queue configured for failed messages

## Additional Recommendations

1. **Monitor Dead Letter Queue**: Check `user.registered.DLT` topic for any failed messages

2. **Consumer Lag Monitoring**: Monitor consumer lag to ensure messages are being processed timely

3. **Add Integration Test**: Create a test that publishes a message and verifies wallet creation

4. **Consider Idempotency**: Ensure wallet creation is idempotent (handle duplicate events gracefully)

