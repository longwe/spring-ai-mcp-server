package com.ezcloud.mcp.server.repository;

import com.ezcloud.mcp.server.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Product entities.
 *
 * Extends JpaRepository to provide standard CRUD operations (save, findById,
 * findAll, delete, etc.) without requiring any implementation code.
 *
 * Custom query methods are defined using Spring Data's method naming conventions,
 * which automatically generates the appropriate SQL queries.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products matching the specified category.
     *
     * Spring Data automatically implements this method based on the naming convention:
     * "findBy" + "Category" maps to: SELECT * FROM products WHERE category = ?
     *
     * @param category The category to search for (case-sensitive)
     * @return List of products in the specified category
     */
    List<Product> findByCategory(String category);

    /**
     * Finds all products with a price below the specified threshold.
     *
     * Spring Data automatically implements this method:
     * "findBy" + "Price" + "LessThan" maps to: SELECT * FROM products WHERE price < ?
     *
     * @param price The maximum price threshold (exclusive)
     * @return List of products priced below the threshold
     */
    List<Product> findByPriceLessThan(Double price);
}
