package com.wallet_svc.wallet.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet_svc.wallet.dto.response.ApiResponse;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DatabaseHealthController {
    DataSource dataSource;

    @GetMapping("/db-health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(2); // 2 second timeout

            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikari = (HikariDataSource) dataSource;
                HikariPoolMXBean poolMXBean = hikari.getHikariPoolMXBean();

                health.put("status", isValid ? "UP" : "DOWN");
                health.put("database", "Neon PostgreSQL");
                health.put("poolName", hikari.getPoolName());
                health.put("activeConnections", poolMXBean.getActiveConnections());
                health.put("idleConnections", poolMXBean.getIdleConnections());
                health.put("totalConnections", poolMXBean.getTotalConnections());
                health.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
                health.put("maxPoolSize", hikari.getMaximumPoolSize());
                health.put("minIdle", hikari.getMinimumIdle());
            } else {
                health.put("status", isValid ? "UP" : "DOWN");
                health.put("database", "Neon PostgreSQL");
            }

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder().result(health).build());
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("errorType", e.getClass().getSimpleName());

            // Check if it's a Neon cold start
            if (e.getMessage() != null
                    && (e.getMessage().contains("connection refused")
                            || e.getMessage().contains("timeout")
                            || e.getMessage().contains("Connection reset"))) {
                health.put("possibleCause", "Neon compute cold start - retry in 2-5 seconds");
            }

            return ResponseEntity.status(503)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .code(503)
                            .message("Database unavailable")
                            .result(health)
                            .build());
        }
    }

    @GetMapping("/db-pool-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPoolStatistics() {
        Map<String, Object> stats = new HashMap<>();

        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikari.getHikariPoolMXBean();

            stats.put("poolName", hikari.getPoolName());
            stats.put("activeConnections", poolMXBean.getActiveConnections());
            stats.put("idleConnections", poolMXBean.getIdleConnections());
            stats.put("totalConnections", poolMXBean.getTotalConnections());
            stats.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());

            // Configuration
            stats.put("maxPoolSize", hikari.getMaximumPoolSize());
            stats.put("minIdle", hikari.getMinimumIdle());
            stats.put("maxLifetime", hikari.getMaxLifetime());
            stats.put("idleTimeout", hikari.getIdleTimeout());
            stats.put("connectionTimeout", hikari.getConnectionTimeout());
            stats.put("keepaliveTime", hikari.getKeepaliveTime());

            // Health indicators
            int utilizationPercent = (poolMXBean.getTotalConnections() * 100) / hikari.getMaximumPoolSize();
            stats.put("utilizationPercent", utilizationPercent);

            if (poolMXBean.getThreadsAwaitingConnection() > 0) {
                stats.put("warning", "Threads are waiting for connections - consider increasing pool size");
            }

            if (utilizationPercent > 80) {
                stats.put("warning", "Pool utilization above 80% - monitor for connection exhaustion");
            }

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder().result(stats).build());
        }

        stats.put("error", "DataSource is not HikariCP");
        return ResponseEntity.status(500)
                .body(ApiResponse.<Map<String, Object>>builder()
                        .code(500)
                        .result(stats)
                        .build());
    }

    @GetMapping("/db-config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabaseConfiguration() {
        Map<String, Object> config = new HashMap<>();

        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;

            config.put("poolName", hikari.getPoolName());
            config.put("jdbcUrl", maskPassword(hikari.getJdbcUrl()));
            config.put("driverClassName", hikari.getDriverClassName());
            config.put("maxPoolSize", hikari.getMaximumPoolSize());
            config.put("minIdle", hikari.getMinimumIdle());
            config.put("maxLifetimeMs", hikari.getMaxLifetime());
            config.put("idleTimeoutMs", hikari.getIdleTimeout());
            config.put("connectionTimeoutMs", hikari.getConnectionTimeout());
            config.put("keepaliveTimeMs", hikari.getKeepaliveTime());
            config.put("validationTimeoutMs", hikari.getValidationTimeout());
            config.put("leakDetectionThresholdMs", hikari.getLeakDetectionThreshold());
            config.put("connectionTestQuery", hikari.getConnectionTestQuery());

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder().result(config).build());
        }

        config.put("error", "DataSource is not HikariCP");
        return ResponseEntity.status(500)
                .body(ApiResponse.<Map<String, Object>>builder()
                        .code(500)
                        .result(config)
                        .build());
    }

    private String maskPassword(String jdbcUrl) {
        if (jdbcUrl == null) return null;
        // Mask password in JDBC URL for security
        return jdbcUrl.replaceAll("password=[^&]*", "password=***");
    }
}
