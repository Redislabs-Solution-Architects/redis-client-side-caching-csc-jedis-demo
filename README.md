# Jedis Client-Side Caching with Redis

This project demonstrates the use of client-side caching with Redis using Jedis. The example includes performance benchmarks that compare direct Redis access with local cache access, highlighting latency improvements.

## Features

- Simple GET/SET Operations: Measure the performance of basic Redis commands.
- Hash Operations: Demonstrate caching with Redis hash data structures.
- JSON Operations: Store and retrieve JSON data, showcasing caching benefits.
- Variadic and Multi-Key Commands: Evaluate performance for multi-key operations like MGET, SMEMBERS, and SUNION.
- Then, we see a very easy way to implement our own CSC rules, by implementing the Cacheable interface from Jedis.

### You can use these env vars to connect to your own Redis instance

```bash
# Set the environment variables and run the jar
export REDIS_HOST=your-redis-cloud-host
export REDIS_PORT=your-redis-cloud-port
export REDIS_PASSWORD=your-redis-password
```

## Getting Started

These instructions will help you set up and run the project on your local machine for development and testing purposes.

### Prerequisites

- Java 17 or higher
- Maven for dependency management
- Redis running locally on port 6379

## Installation

Clone the repository:
```bash
git clone https://github.com/Redislabs-Solution-Architects/redis-client-side-caching-csc-jedis-demo.git
cd redis-client-side-caching-csc-jedis-demo
```

Install dependencies:

```bash
mvn clean install
```

## Usage

Run the application:

```bash
mvn exec:java -Dexec.mainClass="io.platformengineer.Main"
```

If you want to run the jar itself (I like this because it’s easy for “dockerization” later):

```bash
# Build the project
mvn clean package

# Set the environment variables and run the jar
export REDIS_HOST=your-redis-cloud-host
export REDIS_PORT=your-redis-cloud-port
export REDIS_PASSWORD=your-redis-password

# Run the jar with the environment variables
java -jar target/jedis-client-side-caching-redis-0.1.0-gabs-jar-with-dependencies.jar
```

Observe the output:

The application will execute various Redis commands and log latency improvements achieved with client-side caching.

```bash
INFO  Testing simple SET/GET operations...
INFO  SET command duration (no cache): 120000 ns
INFO  GET command duration (server): 50000 ns, value: bar
INFO  GET command duration (cache hit): 5000 ns, value: bar
INFO  GET operation latency improvement: 45000 ns (90.00%)
```

## Custom Cacheable Logic

### Overview

This project demonstrates how to use client-side caching in Redis with Jedis. We’ve implemented a custom caching strategy that allows for flexible control over which keys get cached. The caching logic supports both:

1. **Specific key-based caching**: Where only a predefined set of keys are cached.
2. **Prefix-based caching**: Where keys starting with specific prefixes are cached.

### Custom Cacheable Logic

To customize which keys get cached, we’ve extended the default Cacheable interface and implemented two strategies:

1. **SpecificKeysCacheable**: Caches a predefined set of keys.
2. **PrefixCacheable**: Caches keys that start with specific prefixes. The PrefixCacheable implementation can handle multiple prefixes, making it easy to manage and configure.

#### Specific Key Caching Example

In this method, you can specify exact keys that should be cached. For instance:

```java
CacheConfig cacheConfig = CacheConfig.builder()
    .maxSize(1000) // Cache size
    .cacheable(new SpecificKeysCacheable(Set.of("user:1001", "user:1002", "foo", "person:1", "session:1", "hola"))) // Cache only specific keys
    .build();
```

In this example, only the keys "user:1001", "user:1002", "foo", "person:1", "session:1", and "hola" will be cached.

#### Prefix-Based Caching Example

The **PrefixCacheable** class allows you to cache any key that starts with one or more specified prefixes.\
This is particularly useful if you have groups of keys that follow a common naming pattern.

```java
CacheConfig cacheConfig = CacheConfig.builder()
    .maxSize(1000) // Cache size
    .cacheable(new PrefixCacheable(Set.of("foo", "user", "session", "person"))) // Cache keys with any of these prefixes
    .build();
```

In this example, **all keys** starting with "foo", "user", "session", or "person" will be cached.

### How It Works
1. **Specific Key Caching**: Checks each key in the Redis command and caches it only if it matches a predefined list of keys.
2. **Prefix-Based Caching**: Checks each key in the Redis command and caches it if the key starts with any of the specified prefixes.


### Why Use Custom Cacheable Logic?

First of all, I deliberately decided to respect the default logic to only cache given **commands**, that are related to read operations in general.\
It is a default set that is going to be maintained by Jedis, so it feels safe to use it on our interface implementation.

So, before I evaluate the key prefix or match, I check if the command is cacheable.

- Flexibility: You can precisely control which keys are cached based on your application needs.
- Performance Optimization: Cache only the keys that benefit your application, minimizing memory usage while maximizing performance.

### Extending the Logic

Feel free to extend the logic to fit your use case. You can easily modify the `SpecificKeysCacheable` or `PrefixCacheable` classes to handle more complex conditions, such as caching based on suffixes, patterns, or specific data types.

This section can easily be added to your README to give your audience a clear understanding of what you’ve implemented and how they can extend it. Let me know if you’d like to adjust or add anything!

## Docker Image

The Docker image is available on Docker Hub and can be run with customizable Redis connection details.

I will automate the CI/CD with Harness, ok? Just asking for some free-tier license there with my friends.

### Prerequisites

- Docker must be installed on your system.
- You need access to a running Redis instance.

### Running the Docker Container

To run the Docker container, you need to specify the Redis connection details using environment variables. The container will connect to your specified Redis instance.

#### Command to Run the Container

```bash
docker run --rm \
  -e REDIS_HOST=host.docker.internal \  # Set the Redis host (default: localhost)
  -e REDIS_PORT=6379 \  # Set the Redis port (default: 6379)
  [-e REDIS_PASSWORD=your-password] \  # Optional: Set the Redis password if required
  gacerioni/jedis-client-side-caching-redis:1.0.1
```

## Key Components

- **Jedis**: A popular Redis client for Java that provides a straightforward API for interacting with Redis.
- **Client-Side Cache**: Implemented using Jedis’ built-in client-side caching, improving performance by reducing the need for frequent network calls to Redis.
- **SLF4J and Log4j**: Used for logging performance metrics and application flow.

## Performance Benchmarking

The application measures the latency for Redis commands in two scenarios:

- **Direct Redis Access**: Commands are executed directly against the Redis server.
- **Client-Side Cache Access**: Commands are served from the local cache when possible, reducing latency.

### Example Latency Improvement

Below is a sample comparison of latencies for different operations:

| Operation | Server Latency (ns) | Cache Latency (ns) | Improvement (%) |
|-----------|---------------------|--------------------|-----------------|
| GET       | 50,000              | 5,000              | 90.00%          |
| HGETALL   | 60,000              | 4,000              | 93.33%          |
| JSON.GET  | 55,000              | 6,000              | 89.09%          |
| MGET      | 70,000              | 7,000              | 90.00%          |

## Contributions

Contributions are welcome! Please feel free to submit a pull request or open an issue.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For further information, please contact [Gabriel Cerioni](mailto:gabriel.cerioni@redis.com).
