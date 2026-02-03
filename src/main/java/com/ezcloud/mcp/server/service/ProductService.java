package com.ezcloud.mcp.server.service;

import com.ezcloud.mcp.server.entity.Product;
import com.ezcloud.mcp.server.repository.ProductRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class that exposes product inventory operations as MCP tools.
 *
 * Each public method annotated with @Tool becomes available as a callable tool
 * for AI assistants via the Model Context Protocol (MCP). The @Tool annotation's
 * description parameter provides context to the AI about when and how to use each tool.
 *
 * Available tools:
 * - getAllProducts: List all products in the inventory
 * - searchByCategory: Find products by category name
 * - findProductsUnderPrice: Find products below a price threshold
 * - addProduct: Create a new product
 * - updateProduct: Modify an existing product
 * - deleteProduct: Remove a product from inventory
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Constructor injection of the ProductRepository.
     * Spring automatically injects the JPA repository implementation.
     *
     * @param productRepository The repository for product data access
     */
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * MCP Tool: Retrieves all products from the inventory.
     *
     * This tool is useful when the AI needs to display the complete inventory
     * or when the user asks to see all available products.
     *
     * @return A formatted string listing all products with their details
     */
    @Tool(description = "Retrieves all products from the inventory database. " +
            "Returns a formatted list of all products with their details " +
            "including ID, name, category, price, and stock quantity.")
    public String getAllProducts() {
        // Fetch all products from the database
        List<Product> products = productRepository.findAll();

        // Build a human-readable response string
        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d products:\n\n", products.size()));

        // Format each product's details
        for (Product product : products) {
            result.append(String.format("- %s (ID: %d)\n", product.getName(), product.getId()));
            result.append(String.format("  Category: %s\n", product.getCategory()));
            result.append(String.format("  Price: $%.2f\n", product.getPrice()));
            result.append(String.format("  Stock: %d units\n\n", product.getStock()));
        }

        return result.toString();
    }

    /**
     * MCP Tool: Searches for products by category.
     *
     * Enables filtering products by their category. The search is case-sensitive,
     * so "Electronics" and "electronics" are treated as different categories.
     *
     * @param category The category name to search for (case-sensitive)
     * @return A formatted string listing matching products or a "not found" message
     */
    @Tool(description = "Searches for products by category name. " +
            "Returns all products that match the specified category (case-sensitive). " +
            "Common categories include: Electronics, Books, Clothing, Appliances.")
    public String searchByCategory(String category) {
        // Query products matching the specified category
        List<Product> products = productRepository.findByCategory(category);

        // Handle case when no products match
        if (products.isEmpty()) {
            return String.format("No products found in category '%s'.", category);
        }

        // Build response with matching products
        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d products in category '%s':\n\n",
                products.size(), category));

        for (Product product : products) {
            result.append(String.format("- %s (ID: %d) - $%.2f - Stock: %d\n",
                    product.getName(), product.getId(), product.getPrice(),
                    product.getStock()));
        }

        return result.toString();
    }

    /**
     * MCP Tool: Finds products under a specified price.
     *
     * Useful for budget-conscious queries like "show me products under $50"
     * or when the AI needs to recommend affordable options.
     *
     * @param maxPrice The maximum price threshold (exclusive)
     * @return A formatted string listing products below the price threshold
     */
    @Tool(description = "Finds all products priced below a specified maximum price. " +
            "Useful for finding budget-friendly options or products within a price range. " +
            "Price should be specified as a decimal number (e.g., 50.00).")
    public String findProductsUnderPrice(double maxPrice) {
        // Query products with price less than the specified maximum
        List<Product> products = productRepository.findByPriceLessThan(maxPrice);

        // Handle case when no products are under the price
        if (products.isEmpty()) {
            return String.format("No products found under $%.2f.", maxPrice);
        }

        // Build response with matching products
        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d products under $%.2f:\n\n", products.size(), maxPrice));

        for (Product product : products) {
            result.append(String.format("- %s - $%.2f (%s) - Stock: %d\n",
                    product.getName(), product.getPrice(), product.getCategory(), product.getStock()));
        }

        return result.toString();
    }

    /**
     * MCP Tool: Adds a new product to the inventory.
     *
     * Creates a new product with the specified details. Includes validation
     * to ensure data integrity before saving to the database.
     *
     * @param name     The product name (required, non-empty)
     * @param category The product category (required, non-empty)
     * @param price    The product price (must be non-negative)
     * @param stock    The initial stock quantity (must be non-negative)
     * @return A confirmation message with the created product details, or an error message
     */
    @Tool(description = "Adds a new product to the inventory database. " +
            "Requires: name (product name), category (product category), price (decimal price), " +
            "and stock (integer quantity). Returns confirmation with the created product details.")
    public String addProduct(String name, String category, double price, int stock) {
        // Validate input parameters before creating the product
        if (name == null || name.trim().isEmpty()) {
            return "Error: Product name cannot be empty.";
        }
        if (category == null || category.trim().isEmpty()) {
            return "Error: Product category cannot be empty.";
        }
        if (price < 0) {
            return "Error: Product price cannot be negative.";
        }
        if (stock < 0) {
            return "Error: Product stock cannot be negative.";
        }

        // Create and save the new product
        Product product = new Product(name, category, price, stock);
        Product saved = productRepository.save(product);

        // Return confirmation with the saved product details (includes generated ID)
        return String.format("Product added successfully!\n" +
                        "ID: %d\n" +
                        "Name: %s\n" +
                        "Category: %s\n" +
                        "Price: $%.2f\n" +
                        "Stock: %d units",
                saved.getId(), saved.getName(),
                saved.getCategory(), saved.getPrice(), saved.getStock());
    }

    /**
     * MCP Tool: Updates an existing product.
     *
     * Modifies all fields of an existing product. The product is identified by its ID.
     * If the product doesn't exist, an error message is returned.
     *
     * @param id       The ID of the product to update
     * @param name     The new product name
     * @param category The new category
     * @param price    The new price
     * @param stock    The new stock quantity
     * @return A confirmation message with updated details, or an error if not found
     */
    @Tool(description = "Updates an existing product's information in the inventory. " +
            "Requires the product ID and new values for name, category, price, and stock. " +
            "All fields are required even if only updating one field.")
    public String updateProduct(Long id, String name, String category, double price, int stock) {
        // Try to find the product and update it, or return error if not found
        return productRepository.findById(id)
                .map(product -> {
                    // Update all product fields
                    product.setName(name);
                    product.setCategory(category);
                    product.setPrice(price);
                    product.setStock(stock);

                    // Save the updated product
                    Product updated = productRepository.save(product);

                    // Return confirmation with updated details
                    return String.format("Product updated successfully!\n" +
                                    "ID: %d\n" +
                                    "Name: %s\n" +
                                    "Category: %s\n" +
                                    "Price: $%.2f\n" +
                                    "Stock: %d units",
                            updated.getId(), updated.getName(), updated.getCategory(),
                            updated.getPrice(), updated.getStock());
                })
                .orElse(String.format("Error: Product with ID %d not found.", id));
    }

    /**
     * MCP Tool: Deletes a product from the inventory.
     *
     * Permanently removes a product identified by its ID. This operation cannot be undone.
     *
     * @param id The ID of the product to delete
     * @return A confirmation message, or an error if the product was not found
     */
    @Tool(description = "Deletes a product from the inventory by its ID. " +
            "Returns confirmation of deletion or error if product not found.")
    public String deleteProduct(Long id) {
        // Try to find the product and delete it, or return error if not found
        return productRepository.findById(id)
                .map(product -> {
                    // Delete the product from the database
                    productRepository.delete(product);
                    return String.format("Product '%s' (ID: %d) deleted successfully.",
                            product.getName(), product.getId());
                })
                .orElse(String.format("Error: Product with ID %d not found.", id));
    }

}
