# Wallet Service

Wallet Service for managing user credits, transactions, and balance tracking.

## Features

✅ Wallet management and balance tracking  
✅ Credit transactions (charge/refund/adjustment)  
✅ Credit usage tracking  
✅ Balance validation  
✅ Transaction history  
✅ Hold/Release mechanism for SAGA pattern  
✅ Credit packages management  
✅ Kafka event integration  

## Tech Stack

- **Java 21**
- **Spring Boot 3.3.5**
- **PostgreSQL** (Neon)
- **Apache Kafka**
- **Spring Data JPA**
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **Eureka Client** for service discovery

## Database Schema

### Main Tables

- **wallets** - User wallet information and balances
- **wallet_transactions** - All credit transactions
- **credit_packages** - Available credit packages
- **credit_usage_logs** - Usage tracking by service type
- **wallet_holds** - Temporary holds for SAGA pattern
- **daily_wallet_stats** - Analytics and statistics

## API Endpoints

### Public APIs

```bash
# Get my wallet
GET /wallet/api/wallets/my

# Get my balance
GET /wallet/api/wallets/my/balance

# Get my transactions
GET /wallet/api/wallets/my/transactions?page=1&limit=20

# Get transaction detail
GET /wallet/api/wallets/my/transactions/{id}

# Get credit packages
GET /wallet/api/wallets/packages
GET /wallet/api/wallets/packages/{id}
```

### Internal APIs (Service-to-Service)

```bash
# Hold credits (SAGA pattern)
POST /wallet/internal/wallets/hold

# Release hold
POST /wallet/internal/wallets/release

# Charge credits
POST /wallet/internal/wallets/charge

# Refund credits
POST /wallet/internal/wallets/refund

# Top-up balance
POST /wallet/internal/wallets/top-up

# Get balance
GET /wallet/internal/wallets/{userId}/balance
```

### Admin APIs

```bash
# Get wallet by user ID
GET /wallet/api/admin/wallets/{userId}

# Manual adjustment
POST /wallet/api/admin/wallets/{userId}/adjustment

# Get all transactions
GET /wallet/api/admin/wallets/transactions?page=1&limit=20
```

## Events

### Consumed Events
- **user.registered** - Creates wallet when user registers

### Published Events
- **wallet.created** - Published when wallet is created

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8083

# Database
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=...
spring.datasource.password=...

# Kafka
spring.kafka.bootstrap-servers=localhost:9094

# Wallet Settings
app.wallet.initial-balance=0.00
app.wallet.default-currency=VND
app.wallet.hold-expiration-minutes=30
```

## Building and Running

### Build the project
```bash
mvn clean install
```

### Run the service
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/wallet_svc-0.0.1-SNAPSHOT.jar
```

## Transaction Types

- **TOP_UP** - Add credits to wallet
- **CHARGE** - Deduct credits from wallet
- **REFUND** - Return credits to wallet
- **ADJUSTMENT** - Manual adjustment by admin
- **HOLD** - Temporary hold for pending operations
- **RELEASE** - Release a hold

## Transaction Status

- **PENDING** - Transaction initiated
- **PROCESSING** - Being processed
- **SUCCESS** - Completed successfully
- **FAILED** - Failed to process
- **CANCELLED** - Cancelled by user/system
- **REVERSED** - Transaction reversed

## Wallet Status

- **ACTIVE** - Normal operation
- **SUSPENDED** - Temporarily suspended
- **CLOSED** - Permanently closed

## SAGA Pattern Support

The service supports distributed transactions through hold/release mechanism:

1. **Hold Credits** - Lock credits for pending operation
2. **Charge from Hold** - Complete the transaction
3. **Release Hold** - Cancel and unlock credits

## Service Integration

### Creating a Wallet
Wallets are automatically created when `user.registered` event is received from Auth Service.

### Charging Credits
```json
POST /wallet/internal/wallets/charge
{
  "user_id": 1,
  "amount": 100.00,
  "description": "AI Slide Generation",
  "reference_type": "AI_GENERATION",
  "reference_id": "lesson-123",
  "metadata": "{\"slides\":10}"
}
```

### Hold and Release Pattern
```json
// 1. Hold credits
POST /wallet/internal/wallets/hold
{
  "user_id": 1,
  "amount": 100.00,
  "reason": "AI_GENERATION_PENDING",
  "reference_type": "AI_GENERATION",
  "reference_id": "job-456"
}

// 2a. Charge from hold (success)
POST /wallet/internal/wallets/charge
{
  "user_id": 1,
  "amount": 100.00,
  "hold_id": 123
}

// 2b. OR release hold (failure)
POST /wallet/internal/wallets/release
{
  "hold_id": 123
}
```

## Dependencies

Service connects to:
- **PostgreSQL** - Main database
- **Kafka** - Event streaming
- **Eureka Server** - Service discovery

## Development Notes

- Uses MapStruct for automatic DTO mapping
- Spotless plugin for code formatting
- JaCoCo for code coverage
- Lombok for reducing boilerplate
- All monetary amounts use `BigDecimal` for precision

## Project Structure

```
src/main/java/com/wallet_svc/
├── WalletServiceApplication.java
├── wallet/
│   ├── constant/        # Constants (TransactionType, Status, etc.)
│   ├── controller/      # REST Controllers
│   ├── dto/            # Request/Response DTOs
│   ├── entity/         # JPA Entities
│   ├── exception/      # Exception handling
│   ├── mapper/         # MapStruct mappers
│   ├── repository/     # JPA Repositories
│   └── service/        # Business logic
└── event/              # Kafka events
```

## License

Copyright © 2025
