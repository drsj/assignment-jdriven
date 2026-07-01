# Product Catalog Service
A Spring Boot 4.1 (Java 25) microservice that stores products, synchronizes
prices from an external system, and indexes data in Elasticsearch for search.

## Features
- REST API for product CRUD
- PostgreSQL persistence
- Elasticsearch indexing and search
- Scheduled price synchronization using virtual threads
- Mock external pricing service
- Docker Compose environment
- Unit tests

## Architecture

                   +---------------------------+
                   |     pricingmock service   |
                   |  (mock external pricing)  |
                   |   GET /mock-prices/{sku}  |
                   +-------------+-------------+
                                 |
                                 | HTTP (WebClient)
                                 |
+--------------------+     +-----v---------------------+
|   PostgreSQL       |     |   product-service         |
|   productdb        |     |  Spring Boot 4.1          |
+---------+----------+     |                           |
                           | ProductController         |
                           | ProductService            |
                           | PriceSyncService (VT)     |
                           | ExternalPriceClient       |
+---------+----------+     |                           |
|   Elasticsearch    |     +-------------+-------------+
|   product-index    |                   |
+--------------------+                   |
                                         |
                                         |
                                REST API (8080)

## Requirements
Java 25
Maven 3.9+
PostgreSQL 16
Docker (Docker Desktop or Docker Engine)
Elasticsearch 7.x compatible system

All services run on a shared Docker network.

## Configuration
This project uses a .env file to configure database credentials.

## Building the applications
cd product or cd ../product
./mvnw clean package

cd ../pricingmock
./mvnw clean package

Both applications must be built before running Docker compose.

## Running the system
cd ../deployment
docker-compose up --build

## Stopping the system
docker-compose down

## Services
product-service     http://localhost:8080	Main API
pricingmock         http://localhost:9090	Mock external pricing
PostgreSQL          localhost:5432	        Database
Elasticsearch       http://localhost:9200	Search index

## API Endpoints
Product-service (port 8080)

    Create product:
        POST /api/products
        {
        "sku": "APL-IPH-18",
        "name": "iPhone18",
        "description": "Apple iPhone 18",
        "brand": "Apple",
        "category": "mobile",
        "price": 1300,
        "quantity": 20,
        "currency": "EUR"
        }

    Update product quantity by SKU:
        PATCH /api/products/APL-IPH-18/quantity
        {
            "quantity": -3
        }  

    Get product by SKU:
        GET /api/products?api/products/APL-IPH-18

    Get products with pagination:
        GET /api/products?page=2&size=10

    Get products sorted by price descending:
        GET /api/products?sort=price,desc

    Search products:
        GET /api/products/search?q=mobile
            or
        GET /api/products/search?q=Sony mobile

## pricingmock (port 9090)
    Get adjusted price
        GET /mock-prices/{sku}?basePrice=100

    Response:
        {
            "sku": "ABC123",
            "price": 107.32
        }

## Price Synchronization
    Every 60 seconds, the product-service:
    Loads all products
    Spawns a virtual thread per product
    Calls the mock pricing service
    Updates the product price
    Reindexes the product in Elasticsearch

## Health Checks
pricingmock and product-service: 
    GET /actuator/health

## Connect to PostgreSQL in Docker
Find the container name: docker ps (something like "product-postgres")
Open psql in the container:
docker exec -it product-postgres psql -U product -d productdb
Query: e.g. SELECT * FROM products LIMIT 10;

## Data seeding
ProductSeeder seeds the database with 1000 realistic demo products on application startup.
This data is indexed in Elasticsearch after the first scheduled price synchronization.