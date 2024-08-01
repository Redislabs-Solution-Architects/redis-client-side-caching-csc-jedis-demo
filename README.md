# Jedis Client-Side Caching with Guava

This project demonstrates the use of client-side caching with Redis using Jedis and Guava. The example includes performance benchmarks that compare direct Redis access with local cache access, highlighting latency improvements.

## Features

- Simple GET/SET Operations: Measure the performance of basic Redis commands.
- Hash Operations: Demonstrate caching with Redis hash data structures.
- JSON Operations: Store and retrieve JSON data, showcasing caching benefits.
- Variadic and Multi-Key Commands: Evaluate performance for multi-key operations like MGET, SMEMBERS, and SUNION.

## Getting Started

These instructions will help you set up and run the project on your local machine for development and testing purposes.

### Prerequisites

- Java 22 or higher
- Maven for dependency management
- Redis running locally on port 6379

## Installation

Clone the repository:
```shell
git clone https://github.com/Redislabs-Solution-Architects/redis-client-side-caching-csc-jedis-demo.git
cd redis-client-side-caching-csc-jedis-demo
```

Install dependencies:
```shell
mvn clean install
```

## Usage

Run the application:
```shell
mvn exec:java -Dexec.mainClass="io.platformengineer.Main"
```

If you want to run the jar itself (I like this because it's easy for "dockerization" later):
```shell
mvn clean package
java -jar target/jedis-client-side-caching-redis-0.1.0-gabs-jar-with-dependencies.jar
```

Observe the output:

The application will execute various Redis commands and log latency improvements achieved with client-side caching.

```shell
INFO  Testing simple SET/GET operations...
INFO  SET command duration (no cache): 120000 ns
INFO  GET command duration (server): 50000 ns, value: bar
INFO  GET command duration (cache hit): 5000 ns, value: bar
INFO  GET operation latency improvement: 45000 ns (90.00%)
...
```

## Docker Image

The Docker image is available on Docker Hub and can be run with customizable Redis connection details.

I will automate the CI/CD with Harness, ok? Just asking for some free-tier license there with my friends.

### Prerequisites
- Docker must be installed on your system.
- You need access to a running Redis instance.

### Running the Docker Container

To run the Docker container, you need to specify the Redis connection details using environment variables. The container will connect to your specified Redis instance.

#### Command to Run the Container

```shell
docker run --rm \
  -e REDIS_HOST=host.docker.internal \  # Set the Redis host (default: localhost)
  -e REDIS_PORT=6379 \  # Set the Redis port (default: 6379)
  [-e REDIS_PASSWORD=your-password] \  # Optional: Set the Redis password if required
  gacerioni/jedis-client-side-caching-redis:0.1.0-gabs
```

## Key Components

- **Jedis:** A popular Redis client for Java that provides a straightforward API for interacting with Redis.
- **Guava Client-Side Cache:** A local cache implemented using Google's Guava library, improving performance by reducing the need for frequent network calls to Redis.
- **SLF4J and Log4j:** Used for logging performance metrics and application flow.

## Performance Benchmarking

The application measures the latency for Redis commands in two scenarios:

1. **Direct Redis Access:** Commands are executed directly against the Redis server.
2. **Client-Side Cache Access:** Commands are served from the local cache when possible, reducing latency.

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

