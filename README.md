# Interactions

![Minestom version](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2FBlueDragonMC%2FInteractions%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.minestom&label=Minestom)


A Minestom library for block placement and interactions.

## Installation

![Latest version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Freposilite.bluedragonmc.com%2Freleases%2Fcom%2Fbluedragonmc%2Finteractions%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Flatest&label=Latest%20Version)

```kotlin
repositories {
    maven(url = "https://reposilite.bluedragonmc.com/releases")
}

dependencies {
    // Use the latest version number from the badge above
    implementation("com.bluedragonmc:interactions:$version")
}
```

## Usage

TBD

## Why another placement library?

In their current form, block handlers are a finicky system. They require cooperation from an instance's chunk loader, and they need to be manually applied when a player places a block. This library uses events instead, which makes block behavior easier to reason about, with the additional benefit of allowing behaviors to be scoped using Minestom's powerful event system.
