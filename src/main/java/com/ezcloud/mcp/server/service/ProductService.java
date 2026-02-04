package com.ezcloud.mcp.server.service;

import com.ezcloud.mcp.server.entity.Product;
import com.ezcloud.mcp.server.repository.ProductRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
        var products = productRepository.findAll();
        var format = """
                - %s (ID: %d)
                  Category: %s
                  Price: $%.2f
                  Stock: %d units
                """;
        return products.stream()
                .map(p -> format.formatted(p.getName(), p.getId(), p.getCategory(), p.getPrice(), p.getStock()))
                .collect(Collectors.joining("%n".formatted(), "Found %d products:%n%n".formatted(products.size()), ""));
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
        var products = productRepository.findByCategory(category);

        if (products.isEmpty()) {
            return "No products found in category '%s'.".formatted(category);
        }

        return products.stream()
                .map(p -> "- %s (ID: %d) - $%.2f - Stock: %d".formatted(
                        p.getName(), p.getId(), p.getPrice(), p.getStock()))
                .collect(Collectors.joining("%n".formatted(),
                        "Found %d products in category '%s':%n%n".formatted(products.size(), category),
                        "%n".formatted()));
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
        var products = productRepository.findByPriceLessThan(maxPrice);

        if (products.isEmpty()) {
            return "No products found under $%.2f.".formatted(maxPrice);
        }

        return products.stream()
                .map(p -> "- %s - $%.2f (%s) - Stock: %d".formatted(
                        p.getName(), p.getPrice(), p.getCategory(), p.getStock()))
                .collect(Collectors.joining("%n".formatted(),
                        "Found %d products under $%.2f:%n%n".formatted(products.size(), maxPrice),
                        "%n".formatted()));
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
        if (name == null || name.isBlank()) {
            return "Error: Product name cannot be empty.";
        }
        if (category == null || category.isBlank()) {
            return "Error: Product category cannot be empty.";
        }
        if (price < 0) {
            return "Error: Product price cannot be negative.";
        }
        if (stock < 0) {
            return "Error: Product stock cannot be negative.";
        }

        var product = new Product(name, category, price, stock);
        var saved = productRepository.save(product);

        return """
                Product added successfully!
                ID: %d
                Name: %s
                Category: %s
                Price: $%.2f
                Stock: %d units""".formatted(
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
        var format = """
                Product updated successfully!
                ID: %d
                Name: %s
                Category: %s
                Price: $%.2f
                Stock: %d units""";
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(name);
                    product.setCategory(category);
                    product.setPrice(price);
                    product.setStock(stock);

                    var updated = productRepository.save(product);

                    return format.formatted(
                            updated.getId(), updated.getName(), updated.getCategory(),
                            updated.getPrice(), updated.getStock());
                })
                .orElse("Error: Product with ID %d not found.".formatted(id));
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
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return "Product '%s' (ID: %d) deleted successfully.".formatted(
                            product.getName(), product.getId());
                })
                .orElse("Error: Product with ID %d not found.".formatted(id));
    }

}
