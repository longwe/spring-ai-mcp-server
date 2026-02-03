package com.ezcloud.mcp.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test class for the MCP Server application.
 *
 * This test class verifies that the Spring application context
 * loads correctly with all beans properly configured.
 */
@SpringBootTest
class McpServerApplicationTests {

	/**
	 * Verifies that the Spring application context loads successfully.
	 *
	 * This test will fail if:
	 * - There are missing bean dependencies
	 * - Configuration properties are invalid
	 * - Database connection fails
	 * - Any other startup error occurs
	 */
	@Test
	void contextLoads() {
		// Test passes if context loads without exceptions
	}

}
