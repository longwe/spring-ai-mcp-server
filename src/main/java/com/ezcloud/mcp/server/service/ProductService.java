package com.ezcloud.mcp.server.service;

import com.ezcloud.mcp.server.entity.Product;
import com.ezcloud.mcp.server.repository.ProductRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Tool(description = "Retrieves all products from the inventory database. " +
            "Returns a formatted list of all products with their details " +
            "including ID, name, category, price, and stock quantity.")
    public String getAllProducts() {
        List<Product> products = productRepository.findAll();

        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d products:\n\n", products.size()));

        for (Product product : products) {
            result.append(String.format("- %s (ID: %d)\n", product.getName(), product.getId()));
            result.append(String.format("  Category: %s\n", product.getCategory()));
            result.append(String.format("  Price: $%.2f\n", product.getPrice()));
            result.append(String.format("  Stock: %d units\n\n", product.getStock()));
        }

        return result.toString();
    }

    @Tool(description = "Searches for products by category name. " +
            "Returns all products that match the specified category (case-sensitive). " +
            "Common categories include: Electronics, Books, Clothing, Appliances.")
    public String searchByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);

        if (products.isEmpty()) {
            return String.format("No products found in category '%s'.", category);
        }

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

    @Tool(description = "Finds all products priced below a specified maximum price. " +
            "Useful for finding budget-friendly options or products within a price range. " +
            "Price should be specified as a decimal number (e.g., 50.00).")
    public String findProductsUnderPrice(double maxPrice) {
        List<Product> products = productRepository.findByPriceLessThan(maxPrice);

        if (products.isEmpty()) {
            return String.format("No products found under $%.2f.", maxPrice);
        }

        StringBuilder result = new StringBuilder();
        result.append(String.format("Found %d products under $%.2f:\n\n", products.size(), maxPrice));

        for (Product product : products) {
            result.append(String.format("- %s - $%.2f (%s) - Stock: %d\n",
                    product.getName(), product.getPrice(), product.getCategory(), product.getStock()));
        }

        return result.toString();
    }

    @Tool(description = "Adds a new product to the inventory database. " +
            "Requires: name (product name), category (product category), price (decimal price), " +
            "and stock (integer quantity). Returns confirmation with the created product details.")
    public String addProduct(String name, String category, double price, int stock) {
        // Validation
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

        Product product = new Product(name, category, price, stock);
        Product saved = productRepository.save(product);

        return String.format("Product added successfully!\n" +
                        "ID: %d\n" +
                        "Name: %s\n" +
                        "Category: %s\n" +
                        "Price: $%.2f\n" +
                        "Stock: %d units",
                saved.getId(), saved.getName(),
                saved.getCategory(), saved.getPrice(), saved.getStock());
    }

    @Tool(description = "Updates an existing product's information in the inventory. " +
            "Requires the product ID and new values for name, category, price, and stock. " +
            "All fields are required even if only updating one field.")
    public String updateProduct(Long id, String name, String category, double price, int stock) {

        return productRepository.findById(id)
                .map(product -> {
                    product.setName(name);
                    product.setCategory(category);
                    product.setPrice(price);
                    product.setStock(stock);
                    Product updated = productRepository.save(product);

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

    @Tool(description = "Deletes a product from the inventory by its ID. " +
            "Returns confirmation of deletion or error if product not found.")
    public String deleteProduct(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return String.format("Product '%s' (ID: %d) deleted successfully.",
                            product.getName(), product.getId());
                })
                .orElse(String.format("Error: Product with ID %d not found.", id));
    }

}
