# GuSMP Resources

This mod provides integration between AerialHell totems and the Accessories API, allowing totems to work in dedicated accessory slots.

## Setup

For setup instructions please see the [fabric documentation page](https://docs.fabricmc.net/develop/getting-started/setting-up) that relates to the IDE that you are using.

## Using Locally Built Dependencies

This project supports using locally built versions of its dependencies (particularly Accessories API) for development and testing.

### Using a Locally Built Accessories

1. Clone and build the Accessories repository:
   ```bash
   git clone https://github.com/wisp-forest/accessories.git
   cd accessories
   ./gradlew publishToMavenLocal
   ```

2. The locally built version will be automatically used by this project because `mavenLocal()` is checked first in the repository list.

3. To verify which version is being used, run:
   ```bash
   ./gradlew dependencies --configuration runtimeClasspath | grep accessories
   ```

### Switching Back to Remote Version

To use the remote version again, either:
- Clear your local maven cache: `rm -rf ~/.m2/repository/io/wispforest/accessories-fabric/`
- Or temporarily comment out `mavenLocal()` in `build.gradle`

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
