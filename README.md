# Spring AI MCP Server - Product Inventory

A Model Context Protocol (MCP) server built with Spring AI that exposes product inventory management tools to AI assistants like Claude.

This project was built as part of this YouTube video: https://www.youtube.com/watch?v=3rtZRKM39BI

## What is MCP?

The [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) is an open standard that allows AI assistants to securely interact with external tools and data sources. This server implements MCP to expose product inventory operations as tools that AI assistants can call.

## Features

- **Spring AI Integration**: Uses Spring AI's MCP server starter for seamless protocol implementation
- **STDIO Transport**: Communicates via standard input/output, allowing MCP clients to spawn it as a subprocess
- **H2 In-Memory Database**: Self-contained database with sample data, no external setup required
- **CRUD Operations**: Full create, read, update, delete functionality for products

## Available Tools

The server exposes the following tools to AI assistants:

| Tool | Description |
|------|-------------|
| `getAllProducts` | Retrieves all products from the inventory |
| `searchByCategory` | Finds products by category (Electronics, Books, Clothing, Appliances) |
| `findProductsUnderPrice` | Finds products below a specified price threshold |
| `addProduct` | Creates a new product in the inventory |
| `updateProduct` | Updates an existing product's details |
| `deleteProduct` | Removes a product from the inventory |

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)

## Building the Project

```bash
# Using Maven wrapper (recommended)
./mvnw clean package -DskipTests

# Or with installed Maven
mvn clean package -DskipTests
```

This creates an executable JAR at `target/MCP-Server-0.0.1-SNAPSHOT.jar`.

## Running the Server

The server is designed to be launched by an MCP client (like Claude Desktop). It communicates via STDIO, so running it directly in a terminal won't be very useful.

### Integration with Claude Desktop

Add the following to your Claude Desktop configuration file:

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`

**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "product-inventory": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/MCP-Server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

Replace `/absolute/path/to/` with the actual path to your built JAR file.

After updating the configuration, restart Claude Desktop. The product inventory tools will then be available in your conversations.

## Project Structure

```
src/main/java/com/ezcloud/mcp/server/
├── McpServerApplication.java      # Main application & tool registration
├── config/
│   └── DataInitializer.java       # Sample data initialization
├── entity/
│   └── Product.java               # JPA entity for products
├── repository/
│   └── ProductRepository.java     # Spring Data JPA repository
└── service/
    └── ProductService.java        # MCP tools implementation

src/main/resources/
├── application.properties         # Server configuration
└── logback.xml                    # Logging configuration
```

## Key Configuration

The server is configured in `application.properties`:

```properties
# MCP Server Settings
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.name=product-inventory-mcp-server
spring.ai.mcp.server.stdio=true
spring.ai.mcp.server.capabilities.tool=true
```

Important notes:
- **Web server is disabled** (`spring.main.web-application-type=none`) - MCP uses STDIO, not HTTP
- **Logging is disabled** - Any console output would corrupt the MCP JSON protocol
- **H2 in-memory database** - Data resets on each restart

## Sample Data

The server starts with 10 sample products across 4 categories:

| Category | Products |
|----------|----------|
| Electronics | Laptop ($999.99), Wireless Mouse ($29.99), Mechanical Keyboard ($89.99) |
| Books | Spring in Action ($45.99), Clean Code ($39.99) |
| Clothing | T-Shirt ($19.99), Jeans ($59.99) |
| Appliances | Coffee Maker ($79.99), Blender ($49.99), Toaster ($29.99) |

## Example Conversations with Claude

Once configured, you can ask Claude things like:

- "What products do you have in inventory?"
- "Show me all electronics under $100"
- "Add a new product: Gaming Headset in Electronics category, $79.99, 25 in stock"
- "Update the Laptop price to $899.99"
- "Delete the Toaster from inventory"

## How It Works

1. **Claude Desktop** launches the JAR as a subprocess
2. **MCP Handshake**: Client and server exchange capabilities via JSON over STDIO
3. **Tool Discovery**: Claude sees the available tools and their descriptions
4. **Tool Calls**: When appropriate, Claude calls tools with parameters
5. **Responses**: The server executes operations and returns results to Claude

## Dependencies

- Spring Boot 3.5.6
- Spring AI 1.0.3 (MCP Server Starter)
- Spring Data JPA
- H2 Database
- Lombok

## License

This work is licensed under the [Creative Commons Attribution-NonCommercial 4.0 International License](https://creativecommons.org/licenses/by-nc/4.0/).

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
