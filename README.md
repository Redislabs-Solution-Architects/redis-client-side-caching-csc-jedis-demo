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
git clone https://github.com/yourusername/gabs-jedis-client-side-caching-redis.git
cd gabs-jedis-client-side-caching-redis
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
