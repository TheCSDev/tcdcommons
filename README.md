<h1 align="center">
	<a href="https://modrinth.com/project/Eldc1g37" target="_blank">
		<img width="1366" height="728" alt="image" src="https://github.com/user-attachments/assets/f298a18c-9e74-40cf-9b54-823469fd0a0e"/>
	</a>
</h1>

<p align="center">
	<a href="https://github.com/sponsors/TheCSDev/" target="_blank">
		<img alt="Sponsors" src="https://img.shields.io/github/sponsors/TheCSDev?style=for-the-badge"/>
	</a>
	<a href="https://github.com/TheCSDev/tcdcommons/issues" target="_blank">
		<img alt="Issues" src="https://img.shields.io/github/issues/TheCSDev/tcdcommons?style=for-the-badge"/>
	</a>
	<a href="https://curseforge.com/projects/711539" target="_blank">
		<img alt="Issues" src="https://img.shields.io/curseforge/dt/711539?style=for-the-badge&label=CurseForge"/>
	</a>
	<a href="https://modrinth.com/project/Eldc1g37" target="_blank">
		<img alt="Issues" src="https://img.shields.io/modrinth/dt/Eldc1g37?style=for-the-badge&label=Modrinth"/>
	</a>
</p>

<p align="center">This repository contains the source-code of "TCDCommons API", TheCSDev's personal Minecraft modding library.</p>

<p align="center">(The thumbnail is a screenshot of source code for one of the main GUI API classes; <code>TScreen.java</code>, shown in IntelliJ IDEA editor.)</p>

> [!IMPORTANT]
> This repository contains versions starting from `v5.0`. If you’re looking for legacy releases (`v4.0` and earlier), please see the archived repository: https://github.com/TheCSDev/tcdcommons-v4

## Introduction

TCDCommons is a Minecraft modding API library that features its own GUI system and various events and hooks for the game, as well as utilities that may be useful to mod developers. This API's main purpose is to help optimize and speed up the development of mods, as well as to avoiding re-writing the same code for every mod that needs to do similar things. Please note that because this mod is a library, it may not offer useful front-end features to the user.

**Key features**
- 🖥️ Robust GUI framework
- ⚙ JSON-based config system
- 🎣 Additional 'hooks' useful for modding the game

## Dependencies

This mod depends on some other mods that first need to be installed before this mod can be installed. Those dependencies are as follows:
- 🏗 [Architectury API](https://github.com/architectury/architectury-api) - Allows this mod to run on `Fabric` and `NeoForge`

## Building

Follow these steps to build the project from source.

### Prerequisites
* **Java 21**: Ensure you have the Java Development Kit (JDK) 21 installed.
* **Git**: Required to clone this repository.

### Instructions
1. **Clone the repository**
```bash
git clone https://github.com/TheCSDev/tcdcommons.git
cd tcdcommons
```

2. **Build the mod**
```bash
./gradlew clean build
```
*Note: Use `gradlew.bat clean build` **if** you are on Windows.*

The compiled `.jar` file will be located in `build/libs/`.
