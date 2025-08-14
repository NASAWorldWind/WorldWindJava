# WorldWind Java Development Instructions

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

WorldWind Java is a 3D virtual globe API for desktop Java, developed by NASA. It provides a geographic context with high-resolution terrain for visualizing geographic information in 3D and 2D.

## Working Effectively

### Prerequisites and Setup
- Java 11+ is required (project targets Java 11, current environment uses Java 17)
- Apache Ant is required for building
- Apache NetBeans is the recommended IDE (project includes NetBeans configuration)
- Graphics card with current OpenGL drivers required for display

### Core Build Commands
Bootstrap and build the repository:
```bash
ant -f release-build.xml assemble
```
- **TIMING**: Build takes ~1 minute. NEVER CANCEL. Set timeout to 90+ minutes for safety.
- Creates `worldwind.jar` and `worldwindx.jar` in project root and `build/jar/release/`
- Also generates comprehensive Javadoc documentation in `build/doc/javadoc/`

Run unit tests:
```bash
ant -f release-build.xml unitTest
```
- **TIMING**: Tests take ~30 seconds. NEVER CANCEL. Set timeout to 45+ minutes for safety.
- Some tests may fail (particularly BasicOrbitViewTest) but this is expected
- Tests that require network access may fail in sandboxed environments
- Build marked as "BUILD SUCCESSFUL" even with test failures

Full build with tests:
```bash
ant -f release-build.xml build
```
- **TIMING**: Full build + tests take ~1 minute 45 seconds. NEVER CANCEL. Set timeout to 90+ minutes.
- May fail on test failures but core functionality builds successfully

Alternative NetBeans build (not recommended for CI):
```bash
ant -f build.xml
```
- **TIMING**: Takes ~45 seconds. Uses NetBeans-specific configuration.
- Fails more strictly on test failures

### Running Demos and Applications
Run the default demo application:
```bash
./run-demo.bash
# or on Windows: run-demo.bat
```

Run specific demo applications:
```bash
java -Xmx1024m -classpath ./worldwind.jar:./worldwindx.jar:./gdal.jar:./jogl-all.jar:./gluegen-rt.jar gov.nasa.worldwindx.examples.ApplicationTemplate
```

Run with offline/local data:
```bash
java -Xmx1024m -classpath ./worldwind.jar:./worldwindx.jar:./gdal.jar:./jogl-all.jar:./gluegen-rt.jar gov.nasa.worldwindx.examples.LocalDataOnly
```

**Note**: Demo applications attempt to connect to NASA WorldWind servers. Network errors are expected in sandboxed environments but applications will still start and display the 3D globe interface.

## Validation

### Build Validation
Always run these commands after making changes:
1. `ant -f release-build.xml clean` - Clean previous builds
2. `ant -f release-build.xml assemble` - Build core libraries (~25 seconds)
3. `ant -f release-build.xml unitTest` - Run unit tests (~30 seconds)

### Functional Validation
**ALWAYS** test at least one demo application after making changes:
```bash
# Test basic demo (will show network errors but should display UI)
timeout 30s java -Xmx1024m -classpath ./worldwind.jar:./worldwindx.jar:./gdal.jar:./jogl-all.jar:./gluegen-rt.jar gov.nasa.worldwindx.examples.ApplicationTemplate
```

The application should:
- Start without crashing
- Display a 3D globe interface (even without texture data)
- Show network error messages but continue running
- Accept mouse/keyboard input for navigation

**Critical**: Test changes that affect OpenGL rendering, geometry calculations, or core WorldWind classes with multiple demo applications.

### Display Requirements
- GUI applications require X11 display
- In headless environments, use: `export DISPLAY=:99 && xvfb-run -a -s "-screen 0 1024x768x24" [command]`
- Graphics drivers must support OpenGL (handled by JOGL library)

## Project Structure and Key Areas

### Core Packages
- `src/gov/nasa/worldwind/` - Core WorldWind functionality
  - `geom/` - Geometric classes (vectors, matrices, coordinates)
  - `globes/` - Globe and projection implementations
  - `layers/` - Data layer implementations
  - `render/` - Rendering engine and shapes
  - `view/` - Camera and view controls
  - `terrain/` - Elevation and terrain handling
  - `util/` - Utility classes

### Example Applications
- `src/gov/nasa/worldwindx/examples/` - Demo applications
  - `ApplicationTemplate.java` - Basic template for new applications
  - `LocalDataOnly.java` - Offline-capable demo
  - `SimplestPossibleExample.java` - Minimal WorldWind usage

### Build Configuration
- `release-build.xml` - **Primary build script** (use this)
- `build.xml` - NetBeans build script (secondary)
- `release-build.properties` - Build configuration
- `nbproject/` - NetBeans IDE configuration

### Dependencies
- `jogl-all.jar` - Java OpenGL binding (JOGL v2.4)
- `gluegen-rt.jar` - JOGL runtime utilities
- `gdal.jar` - Geospatial Data Abstraction Library
- `junit-4.5.jar` - Unit testing framework
- `jogl-all-natives-*.jar` - Platform-specific native libraries
- `gluegen-rt-natives-*.jar` - Platform-specific native libraries

## Common Tasks

### Adding New Features
1. Build and test existing code first
2. Add new classes in appropriate `gov.nasa.worldwind` packages
3. Follow existing naming conventions and patterns
4. Add unit tests in `test/` directory
5. Test with multiple demo applications
6. Always validate with: `ant -f release-build.xml assemble && ant -f release-build.xml unitTest`

### Debugging Issues
1. Enable debug builds: `ant -f release-build.xml assembleDebug`
2. Use debug JARs: `build/jar/debug/worldwind.jar`, `build/jar/debug/worldwindx.jar`
3. Check logs - WorldWind uses `java.util.logging`
4. Test with `gov.nasa.worldwindx.examples.WorldWindDiagnostics`

### Working with Tests
- Unit tests in `test/gov/nasa/worldwind/`
- Functional tests in `testFunctional/gov/nasa/worldwind/`
- Some network-dependent tests may fail in sandboxed environments
- Focus on tests relevant to your changes

### Documentation
Generate Javadoc:
```bash
ant -f release-build.xml assembleJavadoc
```
- Output in `build/doc/javadoc/`
- **TIMING**: Documentation generation takes ~15 seconds
- May show warnings but completes successfully

## Important Notes

### Performance and Memory
- Use `-Xmx1024m` or higher for demo applications
- WorldWind is memory-intensive due to graphics and terrain data
- Avoid memory leaks in long-running applications

### Graphics Requirements
- Modern graphics card required
- Update graphics drivers if display problems occur
- WorldWind uses OpenGL through JOGL
- Some features may not work in headless/virtual environments

### Network and Offline Mode
- WorldWind can run in offline mode: `WorldWind.setOfflineMode(true)`
- Many examples attempt to load remote data from NASA servers
- Network failures are expected in sandboxed environments
- Local data sources can be configured for offline usage

### Platform Considerations
- Target platforms: Windows, macOS, Linux
- Native libraries included for all platforms
- Use appropriate shell scripts: `run-demo.bash` (Unix) or `run-demo.bat` (Windows)

### Development IDE
- Apache NetBeans is recommended (includes project configuration)
- NetBeans files checked into repository: `nbproject/`
- Can be developed in other IDEs but may require configuration

## Build Target Reference

Available ant targets (`ant -f release-build.xml -projecthelp`):
- `assemble` - Build JAR libraries and documentation (~1 minute)
- `assembleDebug` - Build debug versions
- `assembleRelease` - Build release versions  
- `assembleJavadoc` - Generate documentation only
- `test` - Run all tests (strict - fails on test failures)
- `unitTest` - Run unit tests (lenient - allows test failures)
- `build` - Full build and test (default target)
- `clean` - Remove build directory

## Troubleshooting

### Build Failures
- Ensure Java 11+ is available
- Check that Apache Ant is installed
- Verify graphics drivers for OpenGL support
- Clean build directory: `ant -f release-build.xml clean`

### Test Failures
- Network-dependent tests fail in sandboxed environments (expected)
- Graphics tests may fail without proper display setup
- Some XML parsing tests may fail (known issue with BasicOrbitViewTest)
- Use `unitTest` target instead of `test` for lenient testing

### Demo Application Issues
- Ensure graphics drivers support OpenGL
- Network errors are expected and don't indicate problems
- Applications may take 10-15 seconds to fully start
- Use virtual display in headless environments: `xvfb-run`

Remember: Always validate your changes by building and running at least one demo application to ensure the core functionality remains intact.