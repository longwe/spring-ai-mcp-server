package com.ezcloud.mcp.server.config;

import com.ezcloud.mcp.server.entity.Product;
import com.ezcloud.mcp.server.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that initializes the database with sample product data.
 *
 * This class runs automatically when the application starts, populating the
 * in-memory H2 database with sample products across different categories.
 * This provides immediate data for testing and demonstrating the MCP tools.
 *
 * Since the database is in-memory (H2 with create-drop), this data is
 * recreated fresh each time the server starts.
 */
@Configuration
public class DataInitializer {

    /**
     * Creates a CommandLineRunner bean that populates the database on startup.
     *
     * CommandLineRunner is a Spring Boot interface that executes code after
     * the application context is loaded but before the application is fully started.
     *
     * Sample data includes products across four categories:
     * - Electronics: Laptop, Mouse, Keyboard
     * - Books: Programming books
     * - Clothing: T-Shirt, Jeans
     * - Appliances: Kitchen appliances
     *
     * @param repository The ProductRepository for saving products
     * @return A CommandLineRunner that initializes sample data
     */
    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            // Electronics category - tech products
            repository.save(new Product("Laptop", "Electronics", 999.99, 15));
            repository.save(new Product("Wireless Mouse", "Electronics", 29.99, 50));
            repository.save(new Product("Mechanical Keyboard", "Electronics", 89.99, 30));

            // Books category - programming books
            repository.save(new Product("Spring in Action", "Books", 45.99, 25));
            repository.save(new Product("Clean Code", "Books", 39.99, 20));

            // Clothing category - apparel items
            repository.save(new Product("T-Shirt", "Clothing", 19.99, 100));
            repository.save(new Product("Jeans", "Clothing", 59.99, 75));

            // Appliances category - kitchen appliances
            repository.save(new Product("Coffee Maker", "Appliances", 79.99, 40));
            repository.save(new Product("Blender", "Appliances", 49.99, 35));
            repository.save(new Product("Toaster", "Appliances", 29.99, 45));
        };
    }
}
