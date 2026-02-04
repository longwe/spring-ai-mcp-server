/*
 * Copyright (C) 2025 LeetJourney
 * Licensed under the CC BY-NC 4.0 License.
 * See LICENSE file for details.
 */

package com.ezcloud.mcp.server;

import com.ezcloud.mcp.server.service.ProductService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main application class for the Spring AI MCP (Model Context Protocol) Server.
 *
 * This application exposes product inventory management tools via the MCP protocol,
 * allowing AI assistants (like Claude) to interact with a product database through
 * standardized tool calls.
 *
 * The server runs in STDIO mode, communicating via standard input/output streams,
 * making it compatible with MCP clients that spawn the server as a subprocess.
 */
@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		var app = new SpringApplication(McpServerApplication.class);
		app.setLogStartupInfo(false);
		app.setBannerMode(Banner.Mode.OFF);

		var context = app.run(args);

		Runtime.getRuntime().addShutdownHook(new Thread(context::close));

		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	/**
	 * Registers the ProductService methods as MCP tools.
	 *
	 * The MethodToolCallbackProvider scans the ProductService for methods annotated
	 * with @Tool and exposes them as callable tools via the MCP protocol.
	 *
	 * This enables AI assistants to:
	 * - Query products from the database
	 * - Search by category or price
	 * - Add, update, and delete products
	 *
	 * @param productService The service containing tool methods
	 * @return A ToolCallbackProvider that exposes the service methods as MCP tools
	 */
	@Bean
	public ToolCallbackProvider productTools(ProductService productService) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(productService)
				.build();
	}

}
