package io.platformengineer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.csc.CacheConfig;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    // Data structure to store latency results
    private static final Map<String, LatencyResult> LATENCY_RESULTS = new LinkedHashMap<>();

    public static void main(String[] args) {

        // Fetch Redis connection details from environment variables, with defaults
        String redisHost = System.getenv("REDIS_HOST");
        if (redisHost == null || redisHost.isEmpty()) {
            //redisHost = "localhost";
            redisHost = "redis-18443.c309.us-east-2-1.ec2.redns.redis-cloud.com";
        }

        String redisPort = System.getenv("REDIS_PORT");
        //int port = 6379; // Default port
        int port = 18443; // Redis Cloud Port
        if (redisPort != null && !redisPort.isEmpty()) {
            try {
                port = Integer.parseInt(redisPort);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid REDIS_PORT value. Using default port 6379.", e);
            }
        }

        //String redisPassword = System.getenv("REDIS_PASSWORD");
        String redisPassword = "blablabla";

        // Configure the connection
        HostAndPort node = HostAndPort.from(redisHost + ":" + port);
        System.out.println("Connecting to Redis at: " + redisHost + ":" + port);
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .resp3()
                .password(redisPassword != null && !redisPassword.isEmpty() ? redisPassword : null)
                .build();

        // Configure client-side cache using CacheConfig (native to Jedis)
        CacheConfig cacheConfig = CacheConfig.builder()
                .maxSize(1000) // Cache size
                .build();

        // Configure connection pool
        GenericObjectPoolConfig<JedisPooled> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setTestWhileIdle(true);

        // Initialize the Jedis client with pool and native cache
        try (UnifiedJedis client = new UnifiedJedis(node, clientConfig, cacheConfig)) {

            testSimpleSetGet(client);
            testHashOperations(client);
            testJsonOperations(client);
            testVariadicAndMultiKeyCommands(client);

            printLatencySummary();
        }
    }

    private static void testSimpleSetGet(UnifiedJedis client) {
        LOGGER.info("##### Testing simple SET/GET operations... #####");

        long startTime = System.nanoTime();
        client.set("foo", "bar", new SetParams().ex(60)); // Cache "foo" with expiration
        long durationServer = System.nanoTime() - startTime;
        LOGGER.info("SET command duration (no cache): {} ns", durationServer);

        // Read from the server
        startTime = System.nanoTime();
        String value = client.get("foo");
        durationServer = System.nanoTime() - startTime;
        LOGGER.info("GET command duration (server): {} ns, value: {}", durationServer, value);

        // Cache hit
        startTime = System.nanoTime();
        value = client.get("foo");
        long durationCache = System.nanoTime() - startTime;
        LOGGER.info("GET command duration (cache hit): {} ns, value: {}", durationCache, value);

        summarizeLatency("GET", durationServer, durationCache);
    }

    private static void testHashOperations(UnifiedJedis client) {
        LOGGER.info("##### Testing HASH operations... #####");

        Map<String, String> hash = new HashMap<>();
        hash.put("name", "John");
        hash.put("surname", "Smith");
        hash.put("company", "Redis");
        hash.put("age", "29");

        long startTime = System.nanoTime();
        client.hset("person:1", hash);
        long durationServer = System.nanoTime() - startTime;
        LOGGER.info("HSET command duration (no cache): {} ns", durationServer);

        // Read from the server
        startTime = System.nanoTime();
        Map<String, String> result = client.hgetAll("person:1");
        durationServer = System.nanoTime() - startTime;
        LOGGER.info("HGETALL command duration (server): {} ns, result: {}", durationServer, result);

        // Cache hit
        startTime = System.nanoTime();
        result = client.hgetAll("person:1");
        long durationCache = System.nanoTime() - startTime;
        LOGGER.info("HGETALL command duration (cache hit): {} ns, result: {}", durationCache, result);

        summarizeLatency("HGETALL", durationServer, durationCache);
    }

    private static void testJsonOperations(UnifiedJedis client) {
        LOGGER.info("##### Testing JSON operations... #####");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "John");
        jsonObject.put("surname", "Smith");
        jsonObject.put("company", "Redis");
        jsonObject.put("age", "29");

        long startTime = System.nanoTime();
        client.jsonSet("session:1", jsonObject);
        long durationServer = System.nanoTime() - startTime;
        LOGGER.info("JSON.SET command duration: {} ns", durationServer);

        // Read from the server
        startTime = System.nanoTime();
        Object jsonResult = client.jsonGet("session:1");
        durationServer = System.nanoTime() - startTime;
        LOGGER.info("JSON.GET command duration (server): {} ns, result: {}", durationServer, jsonResult);

        // Cache hit
        startTime = System.nanoTime();
        jsonResult = client.jsonGet("session:1");
        long durationCache = System.nanoTime() - startTime;
        LOGGER.info("JSON.GET command duration (cache hit): {} ns, result: {}", durationCache, jsonResult);

        summarizeLatency("JSON.GET", durationServer, durationCache);
    }

    private static void testVariadicAndMultiKeyCommands(UnifiedJedis client) {
        LOGGER.info("##### Testing variadic and multi-key commands... #####");

        client.set("hola", "mundo");
        client.set("hello", "world");

        long startTime = System.nanoTime();
        List<String> result = client.mget("hola", "hello");
        long durationServer = System.nanoTime() - startTime;
        LOGGER.info("MGET command duration (server): {} ns, result: {}", durationServer, result);

        // Cache hit
        startTime = System.nanoTime();
        result = client.mget("hola", "hello");
        long durationCache = System.nanoTime() - startTime;
        LOGGER.info("MGET command duration (cache hit): {} ns, result: {}", durationCache, result);

        summarizeLatency("MGET", durationServer, durationCache);
    }

    private static void summarizeLatency(String operation, long durationServerNs, long durationCacheNs) {
        long durationServerUs = durationServerNs / 1_000; // Convert to microseconds
        long durationCacheUs = durationCacheNs / 1_000; // Convert to microseconds
        long improvementUs = durationServerUs - durationCacheUs;
        double percentageImprovement = ((double) improvementUs / durationServerUs) * 100;

        LOGGER.info("{} operation latency improvement: {} µs ({}%)", operation, improvementUs, String.format("%.2f", percentageImprovement));

        // Store the result for summary
        LATENCY_RESULTS.put(operation, new LatencyResult(durationServerUs, durationCacheUs, improvementUs, percentageImprovement));
    }

    private static void printLatencySummary() {
        LOGGER.info("\n\n##### Latency Summary #####");
        LOGGER.info(String.format("%-10s | %-15s | %-15s | %-15s | %-10s", "Operation", "Server Latency", "Cache Latency", "Improvement", "Percent"));
        LOGGER.info(String.format("%s", "-".repeat(75)));

        for (Map.Entry<String, LatencyResult> entry : LATENCY_RESULTS.entrySet()) {
            String operation = entry.getKey();
            LatencyResult result = entry.getValue();
            LOGGER.info(String.format("%-10s | %-15d µs | %-15d µs | %-15d µs | %-10.2f%%",
                    operation, result.durationServer, result.durationCache, result.improvement, result.percentageImprovement));
        }

        LOGGER.info(String.format("%s", "-".repeat(75)));
        LOGGER.info("##### End of Latency Summary #####\n");
    }

    // Inner class to hold latency results
    private static class LatencyResult {
        long durationServer;
        long durationCache;
        long improvement;
        double percentageImprovement;

        LatencyResult(long durationServer, long durationCache, long improvement, double percentageImprovement) {
            this.durationServer = durationServer;
            this.durationCache = durationCache;
            this.improvement = improvement;
            this.percentageImprovement = percentageImprovement;
        }
    }
}