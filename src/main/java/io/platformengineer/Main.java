package io.platformengineer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.csc.GuavaClientSideCache;
import redis.clients.jedis.params.SetParams;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Configure the connection
        HostAndPort node = HostAndPort.from("localhost:6379");
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .resp3() // RESP3 protocol
                //.password("nhtuquVSLbh2<...>") // Redis password (optional)
                .build();

        // Configure client-side cache
        GuavaClientSideCache clientSideCache = GuavaClientSideCache.builder()
                .maximumSize(1000)
                .ttl(100)
                .build();

        // Configure pool
        GenericObjectPoolConfig<JedisPooled> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setTestWhileIdle(true); // Test connections while idle to prevent disconnections

        // Initialize the Jedis client with pool and cache
        JedisPooled client = new JedisPooled(node, clientConfig, clientSideCache);

        // Test simple GET/SET with caching
        testSimpleSetGet(client, clientSideCache);

        // Test Hash operations with caching
        testHashOperations(client, clientSideCache);

        // Test JSON operations with caching
        testJsonOperations(client);

        // Test variadic and multi-key commands with caching
        testVariadicAndMultiKeyCommands(client, clientSideCache);
    }

    private static void testSimpleSetGet(JedisPooled client, GuavaClientSideCache clientSideCache) {
        logger.info("Testing simple SET/GET operations...");

        long startTime = System.nanoTime();
        client.set("foo", "bar", new SetParams().ex(60)); // Cache "foo" with expiration
        long durationServer = System.nanoTime() - startTime;
        logger.info("SET command duration (no cache): {} ns", durationServer);

        // Read from the server
        startTime = System.nanoTime();
        String value = client.get("foo");
        durationServer = System.nanoTime() - startTime;
        logger.info("GET command duration (server): {} ns, value: {}", durationServer, value);

        // Cache hit
        startTime = System.nanoTime();
        value = client.get("foo");
        long durationCache = System.nanoTime() - startTime;
        logger.info("GET command duration (cache hit): {} ns, value: {}", durationCache, value);

        // Summarize latency improvement
        summarizeLatency("GET", durationServer, durationCache);
    }

    private static void testHashOperations(JedisPooled client, GuavaClientSideCache clientSideCache) {
        logger.info("Testing HASH operations...");

        Map<String, String> hash = new HashMap<>();
        hash.put("name", "John");
        hash.put("surname", "Smith");
        hash.put("company", "Redis");
        hash.put("age", "29");

        long startTime = System.nanoTime();
        client.hset("person:1", hash);
        long durationServer = System.nanoTime() - startTime;
        logger.info("HSET command duration (no cache): {} ns", durationServer);

        // Read from the server
        startTime = System.nanoTime();
        Map<String, String> result = client.hgetAll("person:1");
        durationServer = System.nanoTime() - startTime;
        logger.info("HGETALL command duration (server): {} ns, result: {}", durationServer, result);

        // Cache hit
        startTime = System.nanoTime();
        result = client.hgetAll("person:1");
        long durationCache = System.nanoTime() - startTime;
        logger.info("HGETALL command duration (cache hit): {} ns, result: {}", durationCache, result);

        // Summarize latency improvement
        summarizeLatency("HGETALL", durationServer, durationCache);

        // Clear the cache and read again
        clientSideCache.clear();
        startTime = System.nanoTime();
        result = client.hgetAll("person:1");
        durationServer = System.nanoTime() - startTime;
        logger.info("HGETALL command duration (after cache clear): {} ns, result: {}", durationServer, result);
    }

    private static void testJsonOperations(JedisPooled client) {
        logger.info("Testing JSON operations...");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "John");
        jsonObject.put("surname", "Smith");
        jsonObject.put("company", "Redis");
        jsonObject.put("age", "29");

        long startTime = System.nanoTime();
        client.jsonSet("session:1", jsonObject);
        long durationServer = System.nanoTime() - startTime;
        logger.info("JSON.SET command duration: {} ns", durationServer);

        // Read from the server
        startTime = System.nanoTime();
        Object jsonResult = client.jsonGet("session:1");
        durationServer = System.nanoTime() - startTime;
        logger.info("JSON.GET command duration (server): {} ns, result: {}", durationServer, jsonResult);

        // Cache hit
        startTime = System.nanoTime();
        jsonResult = client.jsonGet("session:1");
        long durationCache = System.nanoTime() - startTime;
        logger.info("JSON.GET command duration (cache hit): {} ns, result: {}", durationCache, jsonResult);

        // Summarize latency improvement
        summarizeLatency("JSON.GET", durationServer, durationCache);
    }

    private static void testVariadicAndMultiKeyCommands(JedisPooled client, GuavaClientSideCache clientSideCache) {
        logger.info("Testing variadic and multi-key commands...");

        client.set("hola", "mundo");
        client.set("hello", "world");

        long startTime = System.nanoTime();
        List<String> result = client.mget("hola", "hello");
        long durationServer = System.nanoTime() - startTime;
        logger.info("MGET command duration (server): {} ns, result: {}", durationServer, result);

        // Cache hit
        startTime = System.nanoTime();
        result = client.mget("hola", "hello");
        long durationCache = System.nanoTime() - startTime;
        logger.info("MGET command duration (cache hit): {} ns, result: {}", durationCache, result);

        // Summarize latency improvement
        summarizeLatency("MGET", durationServer, durationCache);

        // Test set operations
        client.sadd("coding:be", "Python", "C++");
        client.sadd("coding:fe", "TypeScript", "Javascript");

        startTime = System.nanoTime();
        client.smembers("coding:be");
        durationServer = System.nanoTime() - startTime;
        logger.info("SMEMBERS command duration (server): {} ns", durationServer);

        // Cache hit
        startTime = System.nanoTime();
        client.smembers("coding:be");
        durationCache = System.nanoTime() - startTime;
        logger.info("SMEMBERS command duration (cache hit): {} ns", durationCache);

        // Summarize latency improvement
        summarizeLatency("SMEMBERS", durationServer, durationCache);

        // Test SUNION with order dependence
        startTime = System.nanoTime();
        client.sunion("coding:be", "coding:fe");
        durationServer = System.nanoTime() - startTime;
        logger.info("SUNION command duration (server): {} ns", durationServer);

        // Cache hit
        startTime = System.nanoTime();
        client.sunion("coding:be", "coding:fe");
        durationCache = System.nanoTime() - startTime;
        logger.info("SUNION command duration (cache hit): {} ns", durationCache);

        // Summarize latency improvement
        summarizeLatency("SUNION", durationServer, durationCache);
    }

    private static void summarizeLatency(String operation, long durationServer, long durationCache) {
        long improvement = durationServer - durationCache;
        double percentageImprovement = ((double) improvement / durationServer) * 100;
        logger.info("{} operation latency improvement: {} ns ({}%)", operation, improvement, String.format("%.2f", percentageImprovement));
    }
}
