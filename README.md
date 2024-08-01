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

