## AVIF Viewer

![Build](https://github.com/pdalfarr/avif-viewer/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/33976.svg)](https://plugins.jetbrains.com/plugin/33976)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/33976.svg)](https://plugins.jetbrains.com/plugin/33976)

**AVIF Viewer** is a lightweight, high-performance image viewer for IntelliJ platform IDEs dedicated to viewing AVIF images seamlessly.

<img src="doc/banner.png" alt="banner" width="300">

### Key Features

- **Dedicated AVIF Support:** Native-like rendering for modern high-efficiency AVIF images directly inside your IDE.
- **Flexible Zoom Controls:** Includes <i>Fit-to-Screen</i>, <i>Actual Size (1:1)</i>, and custom zoom-in/out scaling.
- **Image Rotation:** Easily rotate images clockwise or counterclockwise in 90° increments.
- **Transparency Grid:** Toggleable checkerboard background to inspect alpha transparency layers clearly.

## TMP README content From JetBrains Template

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [group](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml), [name](./src/main/resources/META-INF/plugin.xml), and [sources package](./src/main/kotlin).
- [X] Adjust the plugin [description](./src/main/resources/META-INF/plugin.xml) (see [Tips][docs:plugin-description]) and this README to describe what your plugin does.
- [X] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [X] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [X] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

This Fancy IntelliJ Platform Plugin is going to be your implementation of the brilliant ideas that you have.

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "avif-viewer"</kbd> >
  <kbd>Install</kbd>
  <p/>


- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/33976) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/33976/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
  <p/>


- Manually:

  Download the [latest release](https://github.com/pdalfarr/avif-viewer/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
