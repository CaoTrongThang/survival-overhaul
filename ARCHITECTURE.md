# Survival Overhaul - Project Architecture

This document outlines the project structure and architectural decisions for the Survival Overhaul mod (Fabric 1.20.1). Use this as a reference or a prompt for other AI/human developers.

## Package Structure Overview

The project is organized into functional packages to ensure scalability and maintain clean separation between common logic and client-only rendering.

### 1. Root Package: `com.trongthang.survivaloverhaul`
- `SurvivalOverhaul.java`: Main entrypoint (`ModInitializer`). Handles generic registration (Items, Blocks).
- `SurvivalOverhaulClient.java`: Client entrypoint (`ClientModInitializer`). Calls client-only setup.
- `SurvivalOverhaulDataGenerator.java`: Entrypoint for Fabric Data Generation.

### 2. Registration Packages
- `item`: Contains `ModItems` (Item registration) and `ModItemGroup` (Creative Tab).
- `block`: Contains `ModBlocks` (Block registration).

### 3. Mechanics Package: `mechanics`
This is where the core survival logic resides. Each mechanic is isolated in its own sub-package to prevent cross-contamination.
- `thirst`: Thirst-related logic (`ThirstManager`).
- `temperature`: Temperature-related logic (`TemperatureManager`).
- `bodyparts`: Body parts health logic (`BodyPartsManager`).
- **Internal Logic**: Logic in these folders should be "Logical Server" first (common code) but may trigger events or sync data to the client.

### 4. Client Package: `client`
- **Purpose**: Strictly for code that imports `net.minecraft.client.*`.
- Contains HUD rendering, custom GUI screens, and particle effects.
- `ModClient.java`: Central registration for client-only features.

### 5. Datagen Package: `datagen`
- Contains all `FabricDataGenerator` providers (`Models`, `Recipes`, `LootTables`, `Tags`).
- Always use Datagen for repetitive tasks like adding item models or translation strings.

### 6. Networking: `networking`
- Registration and handling of S2C (Server-to-Client) and C2S (Client-to-Server) packets.

---

## Architectural Rules

1.  **Server Protection**: NEVER import `net.minecraft.client.*` outside of the `client` package. Even in common code, use a proxy-like approach or check `world.isClient`.
2.  **Modular Mechanics**: Keep `thirst`, `temperature`, and `bodyparts` as independent as possible. Use events or packets to communicate between them.
3.  **Registration Priority**: Items and Blocks are registered first in `ModInitializer`, followed by ItemGroups.
4.  **Data Generation**: Always prefer Datagen over manual JSON creation for assets and data.
