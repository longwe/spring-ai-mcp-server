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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(McpServerApplication.class);
		app.setLogStartupInfo(false);
		app.setBannerMode(Banner.Mode.OFF);

		ConfigurableApplicationContext context = app.run(args);

		// Add shutdown hook for graceful shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			context.close();
		}));

		// CRITICAL: Keep the application running
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	@Bean
	public ToolCallbackProvider productTools(ProductService productService) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(productService)
				.build();
	}

}
