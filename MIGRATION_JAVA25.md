# WorldWindJava — Java 25 Migration & Technical Debt Tracker

**Project:** NASA WorldWind Java v2.2.1  
**Current:** Java 11 (Ant build), with Maven `pom.xml` created 2026-04-30  
**Target:** Full Java 25 LTS compliance — all items below resolved

Items are ordered roughly by risk and dependency. Check each box when the change is committed and verified.

---

## Quick-reference priority table

| ID | Item | Priority score | Status |
|----|------|---------------|--------|
| [§1 B1](#1-build-toolchain) | Migrate build system to Maven | — | ✅ Done |
| [§1 B2](#1-build-toolchain) | Set compiler target to Java 25 | — | ✅ Done |
| [§1 B3](#1-build-toolchain) | CI: Travis → GitHub Actions | 24 | ✅ Done |
| [§2](#2-jogl--gluegen-upgrade--highest-risk-item) | JOGL/GlueGen 2.4→2.6 + `--add-opens` | Blocker | ✅ JARs removed |
| [§3](#3-deprecated-wrapper-constructor-calls-74-occurrences) | Boxed-type `new` constructors (74×) | — | ✅ None in WWJ source |
| [§4](#4-browseropener--remove-comappleeiofilemanager) | `BrowserOpener` — remove Apple private API | — | ✅ Done |
| [§5](#5-gdalutils--remove-sunarchdatamodel-system-property) | `GDALUtils` — remove `sun.arch.data.model` | — | ✅ Done |
| [§6 TD-01](#6-jackson--replace-bundled-1x-source-with-jackson-2x-dependency) | Jackson 1.x → Jackson 2.x | 24 | ✅ Done |
| [§7](#7-apache-batik--upgrade-from-bundled-old-version) | Batik 1.7 → 1.17 (build-time rasterizer) | — | ✅ Done |
| [§8 TD-03](#8-junit--upgrade-from-45-to-junit-5) | JUnit 4.5 → JUnit 5 | 16 | ✅ Done |
| [§9](#9-native-webview-jni-binaries--recompile-for-current-platforms) | WebView JNI — recompile for JDK 25 | — | ⬜ |
| [§10](#10-gdal-native-binaries) | GDAL native binaries — validate / rebuild | — | ⬜ |
| [§11](#11-verify-no-remaining-uses-of-removeddeprecated-apis) | Final `jdeprscan` / `jdeps` pass | — | ⬜ |
| [TD-04](#td-04--resolve-todofixme-incomplete-features) | TODO/FIXME incomplete features | 16 | ⬜ |
| [TD-05](#td-05--audit-and-remove-deprecated-api-methods) | 131 deprecated API methods | 15 | ⬜ |
| [TD-07](#td-07--modernise-concurrency-primitives) | 239 raw `synchronized`/`volatile` | 12 | ⬜ |
| [TD-08](#td-08--decompose-god-classes) | God classes (GeometryBuilder 8.6K lines) | 10 | ⬜ |
| [TD-06](#td-06--resolve-suppresswarnings-suppressions) | 490 @SuppressWarnings suppressions | 10 | ⬜ |
| [TD-09](#2-jogl--gluegen-upgrade--highest-risk-item) | JOGL version update | 10 | ⬜ (see §2) |
| [TD-10](#td-10--expand-test-coverage) | Test coverage (~3% baseline) | 9 | ⬜ ongoing |
| [TD-11](#td-11--build-modernisation) | Build modernisation (Ant→Maven) | 8 | ✅ Done |

> **Priority score** = (Impact + Risk) × (6 − Effort), scale 1–25. Higher = fix sooner. Items without a score are Java 25 migration requirements rather than scored debt items.

---

## 1. Build toolchain

- [x] **Migrate build system from Ant to Maven**  
  `pom.xml` created 2026-04-30. Uses `maven-compiler-plugin` 3.13.0, `maven-surefire-plugin` 3.5.2,  
  `exec-maven-plugin` 3.4.1, and `build-helper-maven-plugin` 3.6.0.  
  Ant `build.xml` and `nbproject/` are retained for reference but superseded by Maven.  
  ⚠️ Pending: verify `mvn compile` succeeds end-to-end (requires Java 25 JDK installed locally).

- [x] **Set compiler source/target to Java 25**  
  `<maven.compiler.release>25</maven.compiler.release>` set in `pom.xml` properties.

- [x] **Update CI from Travis CI + openjdk11 to GitHub Actions with Java 25** (done 2026-04-30)  
  Created `.github/workflows/ci.yml` — triggers on push/PR to `master`/`develop`; runs
  `mvn compile` then `xvfb-run mvn test` on `ubuntu-latest` with Temurin 25; uploads Surefire
  reports as an artifact on failure.  
  Created `.github/workflows/release.yml` — triggers on `v*.*.*` tags; runs full test suite,
  packages the JAR, generates Javadoc, then publishes both as GitHub Release assets via
  `softprops/action-gh-release`.  
  `.travis.yml` renamed to `.travis.yml.archived` (history preserved, no longer active).

---

## 2. JOGL / GlueGen upgrade — HIGHEST RISK ITEM

**~~Current version:~~** ~~`2.4.0-rc-20200306` (a pre-release built with JDK 11 in March 2020)~~  
**Upgraded to:** `2.6.0` declared in `pom.xml` (2026-04-30)

JOGL 2.4 was built against Java 11. JDK module encapsulation changes in Java 17–25 break
native binding initialisation without explicit `--add-opens` flags.

### What changes

- [x] **Replace all bundled JOGL/GlueGen JARs with Maven Central 2.6.0 artifacts**  
  `pom.xml` now declares `org.jogamp.jogl:jogl-all:2.6.0` and
  `org.jogamp.gluegen:gluegen-rt:2.6.0`, each with a `natives-macosx-universal` classifier
  dependency.  
  ⚠️ The old bundled JARs (`jogl-all.jar`, `gluegen-rt.jar`, and all six native JARs) should be
  **deleted from the project root** once `mvn compile` is confirmed green. Keeping them now
  avoids breaking Ant users before the Maven build is validated.  
  To add Linux/Windows natives, duplicate the natives dependencies in `pom.xml` using classifiers
  `natives-linux-amd64` and `natives-windows-amd64`.

- [x] **Add required `--add-opens` JVM flags for JOGL on Java 25**  
  Flags declared in `pom.xml` in three places:  
  — `<jogl.jvm.args>` property (single source of truth)  
  — `maven-surefire-plugin` `<argLine>` (test execution)  
  — `exec-maven-plugin` `<arguments>` (application launch via `mvn exec:exec`)  
  `run-demo.bash` and `run-demo.bat` updated to delegate to `mvn exec:exec`.

- [x] **Remove legacy bundled JOGL/GlueGen JARs from project root** (done 2026-04-30)  
  Removed: `jogl-all.jar`, `jogl-all-natives-*.jar` (3), `gluegen-rt.jar`,
  `gluegen-rt-natives-*.jar` (3), `jogl.README.txt`. Retained: `jogl.LICENSE.txt`,
  `gluegen.LICENSE.txt` for compliance documentation.

- [ ] **Verify no breaking API changes between JOGL 2.4.0-rc and 2.6.0**  
  JOGL stays on 2.x so the public API is intended to be stable, but the rc→release transition
  may have changed some internal or deprecated APIs.  
  _Action:_ Run `mvn compile` with Java 25 installed and triage each compiler error against the
  2.6.0 changelog at https://github.com/sgothel/jogl

- [ ] **Update `lib-external/jogl-gluegen/` build scripts**  
  The scripts in this directory build JOGL from source. Update them to target the v2.6.0 tag if
  you ever need to rebuild from source.

---

## 3. Deprecated wrapper constructor calls (74 occurrences)

`new Integer(x)`, `new Long(x)`, `new Double(x)`, `new Float(x)`, `new Boolean(x)`,
`new Short(x)`, `new Byte(x)`, `new Character(x)` — removed in Java 16.

- [x] **Replace all boxed-type `new` constructors with static factory methods** — N/A  
  Audit (2026-04-30): zero occurrences in WorldWind source. The "74 occurrences" referenced in
  the migration doc were inside the vendored `src/org/codehaus/jackson/` source tree, which is
  tracked separately under §6 (Jackson replacement).

---

## 4. `BrowserOpener` — remove `com.apple.eio.FileManager`

File: `src/gov/nasa/worldwind/util/BrowserOpener.java`

- [x] **Replace `browseMacOS()` with `java.awt.Desktop.browse(URI)`** (done 2026-04-30)  
  Removed `com.apple.eio.FileManager` reflection, `browseWindows`, and `browseUnix`. The entire
  class now delegates to `Desktop.getDesktop().browse(url.toURI())` — one method, cross-platform.

- [x] **Replace `Runtime.getRuntime().exec(String)` calls in the same file** (done 2026-04-30)  
  Resolved by the Desktop.browse() rewrite above; all `Runtime.exec` calls eliminated.

---

## 5. `GDALUtils` — remove `sun.arch.data.model` system property

File: `src/gov/nasa/worldwind/util/gdal/GDALUtils.java` (line ~155)

- [x] **Replace `System.getProperty("sun.arch.data.model")` with `System.getProperty("os.arch")`** (done 2026-04-30)  
  `is32bitArchitecture()` now uses `os.arch` exclusively, matching on `x86`, `i386`, `i686`.
  The old `sun.arch.data.model` primary lookup and its GNU Java fallback comment are removed.

---

## 6. Jackson — replace bundled 1.x source with Jackson 2.x dependency

The entire Jackson 1.x library (circa 2009) is committed as Java source in
`src/org/codehaus/jackson/`. This is extremely stale and carries security risk. (See also
[TD-01](#td-01--replace-embedded-jackson-1x) in the tech debt register — score: 24.)

- [x] **Remove `src/org/codehaus/jackson/` source tree entirely** (done 2026-04-30)  
  75 vendored source files deleted. `src/org/` directory removed (was empty after deletion).

- [x] **Add Jackson 2.x as a Maven dependency** (done 2026-04-30)  
  Only `jackson-core` is needed — WorldWind uses the streaming API only, not ObjectMapper.
  ```xml
  <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
      <version>2.18.2</version>
  </dependency>
  ```

- [x] **Update all call sites** (done 2026-04-30)  
  5 files migrated: `BasicJSONEvent`, `BasicJSONEventParserContext`, `JSONDoc`,
  `GeoJSONDoc`, `GeoJSONEventParserContext`.  
  Changes: `org.codehaus.jackson.*` → `com.fasterxml.jackson.core.*`;
  `factory.createJsonParser()` → `factory.createParser()`;
  `parser.getCurrentName()` → `parser.currentName()` (deprecated in Jackson 2.12+).  
  `mvn compile` passes with zero Jackson-related warnings.

- [x] **Update `LICENSE.jackson.txt`** (done 2026-04-30)  
  Updated to reference FasterXML Jackson Core 2.18.2 (Apache 2.0).

---

## 7. Apache Batik — upgrade from bundled old version

> **Correction (2026-04-30):** Batik is **not** a compile or runtime dependency — zero
> `import org.apache.batik.*` statements exist in WorldWind source. It is used exclusively
> as a **CLI SVG rasterizer** in `release-build.xml` to convert 21,520 MIL-STD-2525 SVG
> symbol files into PNGs at release time. `batik-transcoder` and `batik-svggen` are not needed.

`lib-external/batik/` contained **Batik 1.7+r608262** (built ca. 2008 with Ant 1.6.5, 7.7 MB).
Batik 1.7 is incompatible with Java 9+ module encapsulation when run as a forked CLI process.

- [x] **Replace `lib-external/batik/` with a Maven profile using Batik 1.17** (done 2026-04-30)  
  Added profile `rasterize-milstd2525` to `pom.xml`. Uses `maven-antrun-plugin` with
  `batik-all:1.17` + `xml-apis-ext:1.3.04` as plugin dependencies; runs
  `org.apache.batik.apps.rasterizer.Main` in a forked JVM with the full Batik classpath
  via `maven.plugin.classpath`. Handles both the main 128×128 pass and the FEBA 16 px override.

  **Usage:**
  ```bash
  mvn -Prasterize-milstd2525 generate-resources   # SVG → PNG only
  mvn -Prasterize-milstd2525 package              # + zip into milstd2525-symbols.zip
  ```

  `lib-external/batik/` (7.7 MB) removed. Normal `mvn compile` / `mvn test` are unaffected.

---

## 8. JUnit — upgrade from 4.5 to JUnit 5

The bundled `junit-4.5.jar` is from 2008 and has no JDK 25 validation. (See also
[TD-03](#td-03--upgrade-junit-45--junit-5) in the tech debt register — score: 16.)

**✅ Completed 2026-04-30.**

- [x] **Replace `junit:junit:4.13.2` with JUnit 5 (Jupiter) via Maven** (done)
  ```xml
  <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.11.4</version>
      <scope>test</scope>
  </dependency>
  ```
  `junit-jupiter` is the aggregator artifact — it pulls in `junit-jupiter-api`,
  `junit-jupiter-engine` (for Surefire), and `junit-jupiter-params` (for
  `@ParameterizedTest`). `maven-surefire-plugin` 3.5.2 auto-discovers Jupiter tests.

- [x] **Migrate all 49 test files from JUnit 4 to JUnit 5** (done)
  - `@RunWith(JUnit4.class)` removed (47 files)
  - `@RunWith(Parameterized.class)` → `@ParameterizedTest @MethodSource` (2 files:
    `KMLExportTest`, `ShapeAttributesTest`; constructors removed, data method returns
    `Stream<List<Exportable>>` / `Stream<Arguments>`)
  - `import org.junit.*` / specific imports → `org.junit.jupiter.api.*`
  - `import static org.junit.Assert.*` → `import static org.junit.jupiter.api.Assertions.*`
  - `@Before`/`@After` → `@BeforeEach`/`@AfterEach` (8 files)
  - `@Ignore` → `@Disabled` (4 files)
  - `@Test(expected = UnsupportedOperationException.class)` → `assertThrows(...)` (1 occurrence)
  - **1,688 assert argument-order fixes** across 35 files (JUnit 4 puts message first;
    JUnit 5 puts message last)
  - Private `assertEquals(Iterable<T>,…)` helpers renamed `assertIterablesEqual` in
    3 layer test files to prevent method-name shadowing
  - `mvn test-compile` passes with zero errors after migration

---

## 9. Native WebView JNI binaries — recompile for current platforms

`lib-external/webview/` contains Objective-C (macOS) and C++ (Windows) JNI source code compiled
against old JDK headers.

- [ ] **Recompile macOS WebView native library** against JDK 25 headers on a macOS build machine  
  Source: `lib-external/webview/macosx/` — uses `WebKit.framework` (legacy `WebView`, not
  `WKWebView`). Note that `WebView` was deprecated by Apple and may need migrating to `WKWebView`.  
  _Build script:_ `lib-external/webview/macosx/build.sh`

- [ ] **Recompile Windows WebView native library** against JDK 25 headers  
  Source: `lib-external/webview/windows/` — uses IE's `IHTMLDocument2` / MSHTML COM interface,
  which is legacy on Windows 11.  
  _Build file:_ `lib-external/webview/windows/WebView.vcxproj`

- [ ] **Consider whether WebView is still needed**  
  Both the macOS and Windows native backends rely on deprecated/legacy browser engines (Apple
  WebView, MSHTML). If the embedded browser feature is not actively used, removing it would
  eliminate a significant maintenance burden.

---

## 10. GDAL native binaries

The native GDAL binaries in `lib-external/gdal/` are old builds. They may work fine at runtime
(JNI ABI is stable), but should be validated.

- [ ] **Verify `gdal.jar` + native GDAL binaries load correctly under Java 25**  
  Run the GDAL integration tests (`test/gov/nasa/worldwind/data/`) on each target platform. If
  the binaries fail to load, rebuild GDAL from source against JDK 25 headers.

---

## 11. Verify no remaining uses of removed/deprecated APIs

After all of the above changes, do a final pass with the Java migration analysis tooling.

- [ ] **Run `jdeprscan --release 25`** against the compiled classes to catch any remaining
  deprecated API usage  
  ```bash
  jdeprscan --release 25 --class-path <classpath> build/classes
  ```

- [ ] **Run `jdeps --jdk-internals`** to identify any remaining JDK internal API references  
  ```bash
  jdeps --jdk-internals -cp <classpath> build/WorldWindJava.jar
  ```

- [ ] **Ensure all tests pass on Java 25**: `mvn test`

---

## Technical Debt Register

Items below are pure tech debt — not Java 25 requirements — but should be addressed alongside
the migration to avoid compounding the maintenance burden. Scored on
**Priority = (Impact + Risk) × (6 − Effort)**; Impact/Risk/Effort each 1–5.

### TD-01 · Replace embedded Jackson 1.x *(score: 24)*

See full treatment in [§6](#6-jackson--replace-bundled-1x-source-with-jackson-2x-dependency).

---

### TD-02 · CI: Travis → GitHub Actions *(score: 24)*

See full treatment in [§1](#1-build-toolchain) (CI item).

---

### TD-03 · Upgrade JUnit 4.5 → JUnit 5 *(score: 16)*

**✅ Done 2026-04-30.** See full treatment in [§8](#8-junit--upgrade-from-45-to-junit-5).

---

### TD-04 · Resolve TODO/FIXME incomplete features *(score: 16)*

- **Status:** ⬜ Open
- **Scale:** ~30 TODO/FIXME comments across core modules
- **Key instances:**
  - `src/.../formats/nmea/NMEAReader.java` — checksum validation not implemented (data
    correctness bug, not cosmetic debt)
  - `src/.../render/StereoOptionSceneController.java` — "needs to be updated to implement correct
    stereo"
  - `src/.../wms/WMSTiledImageLayer.java` — "consolidate common code in URLBuilder"
  - `src/.../render/DrawContext*.java` — commented-out OrderedRenderable logging with TODO markers
- **Action:**
  1. `grep -rn "TODO\|FIXME\|HACK\|XXX" src/` for full inventory.
  2. File a GitHub Issue for each distinct item.
  3. Fix NMEA checksum first — it is a correctness bug.
- **Effort:** Triage ~0.5 day; NMEA fix ~1 day; stereo rendering ~2–3 days

---

### TD-05 · Audit and remove deprecated API methods *(score: 15)*

- **Status:** ⬜ Open
- **Scale:** 131 `@Deprecated` annotations
- **Action:**
  1. `grep -rn "@Deprecated" src/` to enumerate.
  2. Classify: (a) remove now — no known external callers; (b) schedule removal + replace
     internal callers; (c) retain for public API stability.
  3. Remove category (a) in a single clean-up PR.
- **Effort:** 2–3 days

---

### TD-06 · Resolve @SuppressWarnings suppressions *(score: 10)*

- **Status:** ⬜ Open
- **Scale:** 490 `@SuppressWarnings` annotations — masking raw-type and unchecked-cast warnings
- **Action:** Remove suppressions file-by-file; fix the resulting warnings. Target ~20/sprint as
  background work.
- **Effort:** Ongoing background cadence

---

### TD-07 · Modernise concurrency primitives *(score: 12)*

- **Status:** ⬜ Open
- **Scale:** 239 raw `synchronized`/`volatile` keywords; no apparent use of `ConcurrentHashMap`,
  `AtomicReference`, or `CompletableFuture`
- **Note:** `CONTRIBUTING.md` already mandates `java.util.concurrent` — existing code predates
  the rule.
- **Action:**
  1. Profile with VisualVM or async-profiler to find actual contention hotspots.
  2. Replace `synchronized` maps → `ConcurrentHashMap`; counters → `AtomicLong`.
  3. Do not blindly refactor rendering-thread code; validate under multi-window load.
- **Effort:** 1–2 weeks; profile first

---

### TD-08 · Decompose God classes *(score: 10)*

- **Status:** ⬜ Open
- **Candidates:**
  - `src/.../util/GeometryBuilder.java` — ~8,600 lines; logical sections: sphere, cylinder,
    polygon, extrusion, normal generation
  - `src/.../formats/shapefile/EntityMap.java` — ~4,400 lines
  - `src/.../util/WWXML.java` — ~3,900 lines
  - `src/.../util/WWIO.java` — ~2,500 lines
- **Action:** Start with `GeometryBuilder` — extract `SphereGeometry`, `CylinderGeometry`,
  `PolygonGeometry` as package-private classes, delegated to from `GeometryBuilder`. No behaviour
  change; requires full test run to confirm.
- **Effort:** 3–5 days per class

---

### TD-09 · JOGL version update *(score: 10)*

See full treatment in [§2](#2-jogl--gluegen-upgrade--highest-risk-item).

---

### TD-10 · Expand test coverage *(score: 9)*

- **Status:** ⬜ Open (ongoing)
- **Baseline:** 49 unit tests across 1,726 source files (~3% file ratio)
- **Strategy:** Target regression safety over a coverage number. Recommended sequence:
  1. `geom/` package — pure math, no rendering dependencies, easiest to test
  2. `formats/nmea/`, `formats/shapefile/` — data-in/data-out, deterministic
  3. `wms/` URL building — string manipulation
  4. Rendering integration tests last — requires `xvfb`, harder to assert correctness
- **Gate:** Soft requirement of at least one new test per non-trivial PR to `develop`.
- **Effort:** Ongoing; 2–3 tests/week compounds quickly

---

### TD-11 · Build modernisation (Ant → Maven) *(score: 8)*

- **Status:** ✅ Done — `pom.xml` created 2026-04-30. See [§1](#1-build-toolchain).

---

## Summary of removed/breaking changes by Java version

| Java version | Change | Impact here |
|---|---|---|
| Java 16 | Wrapper type constructors (`new Integer()` etc.) removed | 74 call sites |
| Java 17 | Strong encapsulation enforced — `--illegal-access` no-op | JOGL requires `--add-opens` |
| Java 17 | `SecurityManager` deprecated for removal | Audit needed |
| Java 18 | `Runtime.exec(String)` deprecated | `BrowserOpener` (3 calls) |
| Java 18 | Finalization deprecated for removal | No active `finalize()` overrides found — safe |
| Java 21 | `Thread.stop()` / `Thread.suspend()` removed | Audit: `grep -rn "\.stop()\|\.suspend()"` |
| Java 25 | (LTS target) | All of the above must be resolved |

---

## Audit history

| Date | Author | Notes |
|------|--------|-------|
| 2026-04-30 | warren | Initial tech debt audit. 1,726 source files, ~532K LOC. 17 debt items identified and scored. |
| 2026-04-30 | warren | Maven `pom.xml` created; JOGL upgraded to 2.6.0 in Maven; `--add-opens` flags added; `run-demo` scripts updated. Fixed invalid XML comment (`--release`, `--add-opens` inside `<!-- -->`). |
| 2026-04-30 | warren | Low-risk fixes: `BrowserOpener` rewritten to `Desktop.browse()`; `GDALUtils.is32bitArchitecture()` migrated to `os.arch`; 9 legacy JOGL/GlueGen JARs removed. `mvn compile` passes clean (1,726 files). |
| 2026-04-30 | warren | CI migration: `.github/workflows/ci.yml` and `release.yml` created; `.travis.yml` archived. |
| 2026-04-30 | warren | Jackson: 75 vendored 1.x source files removed; `jackson-core 2.18.2` added to pom.xml; 5 call-site files migrated to `com.fasterxml.jackson.core.*`. `mvn compile` clean. |
| 2026-04-30 | warren | Batik: corrected understanding (build-time rasterizer only, not a compile dep); `lib-external/batik/` (7.7 MB, Batik 1.7 ca. 2008) removed; `rasterize-milstd2525` Maven profile added using `batik-all:1.17`. |

---

_Last updated: 2026-04-30_
