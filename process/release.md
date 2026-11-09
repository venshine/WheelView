# Release parameters (release-seal)

The generic steps live in the global `release-seal` skill. This file only records the parameters and exceptions for this project.

## Version

- Semantic versioning `X.Y.Z`: bump Z for bug fixes only, bump Y for new APIs or style options.
- **The version is bumped at release time.** Do not bump to the next version after sealing (skip the skill's "bump version" step).
- Update all of these in the release commit (`grep -rn "<old version>" wheelview/build.gradle sample/build.gradle README.md README-CN.md` should hit every one):
  - `version` under `publishing` in `wheelview/build.gradle`
  - `versionName` in `sample/build.gradle` (leave `versionCode` unchanged). The sample's version badge and "copy dependency" coordinate read from it, so nothing else in the sample needs editing.
  - The dependency coordinates (Groovy and Kotlin) and the "download sample APK" link in `README.md` and `README-CN.md`

## Pre-release checks

```bash
./gradlew :wheelview:testDebugUnitTest :wheelview:assembleRelease :sample:assembleDebug :sample:testDebugUnitTest
```

Library changes must also be re-tested on a real device for the affected scenarios.

## Tag

- The tag name is the bare version, without a `v` prefix: `2.1.1`
- Annotated tag with the message `WheelView X.Y.Z`, created on the release commit and pushed to `origin`
- JitPack builds from the tag; the coordinate is `com.github.venshine:WheelView:X.Y.Z`. After pushing, check `https://jitpack.io/api/builds/com.github.venshine/WheelView/X.Y.Z`

## GitHub Release

- Title is the version number. Notes are written in English, with sections such as "Added / Fixed / Sample app / Installation", kept or dropped as the release requires
- Attachment: the sample APK, named `WheelView-sample-X.Y.Z.apk`
  - Build: `./gradlew :sample:assembleDebug`; output at `sample/build/outputs/apk/debug/sample-debug.apk`
  - Signed with the local debug certificate (SHA-256 `bc4b28be…1433982`), the same as previous releases, so users can install it over an older version. When releasing from another machine, verify the certificate first; a different one prevents in-place upgrades

## Afterwards

- There is no product doc or ROADMAP; the GitHub Release is the source of truth for release notes, so no other docs need syncing
