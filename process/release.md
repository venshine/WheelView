# 封板参数（release-seal）

通用步骤见全局 `release-seal` skill，本文件只写本项目的参数和例外。

## 版本号

- 语义化版本 `X.Y.Z`：只修 bug 升 Z，新增 API 或样式能力升 Y。
- **本项目在发版时才改版本号**，封板后不提前升到下一版（跳过 skill 的「升版本号」一步）。
- 发版提交里一起改这些位置（`grep -rn "<旧版本号>" wheelview/build.gradle sample/build.gradle README.md README-CN.md` 应全部命中）：
  - `wheelview/build.gradle` 的 `publishing` → `version`
  - `sample/build.gradle` 的 `versionName`（`versionCode` 不改）；示例 App 首页的版本标签和「复制依赖」坐标都从它读取，不用另改
  - `README.md`、`README-CN.md` 里的依赖坐标（Groovy、Kotlin 两处）和「下载示例 APK」链接

## 发版前验证

```bash
./gradlew :wheelview:testDebugUnitTest :wheelview:assembleRelease :sample:assembleDebug :sample:testDebugUnitTest
```

库的改动还需要在真机上复测受影响的场景。

## Tag

- 名称就是版本号，不加 `v`：`2.1.1`
- annotated tag，信息为 `WheelView X.Y.Z`，打在发版提交上，推送到 `origin`
- JitPack 按 tag 构建，坐标 `com.github.venshine:WheelView:X.Y.Z`；推送后检查 `https://jitpack.io/api/builds/com.github.venshine/WheelView/X.Y.Z`

## GitHub Release

- 标题为版本号，正文用中文，分「新增 / 修复 / 示例 App / 接入」小节，按实际内容取舍
- 附件：示例 APK，文件名 `WheelView-sample-X.Y.Z.apk`
  - 构建：`./gradlew :sample:assembleDebug`，产物在 `sample/build/outputs/apk/debug/sample-debug.apk`
  - 用本机 debug 证书签名（SHA-256 `bc4b28be…1433982`），与历史版本一致，用户可覆盖安装；换机器发版时先核对证书，不一致会导致无法覆盖安装

## 后续

- 没有产品文档和 ROADMAP，版本说明以 GitHub Release 为准，无需同步其他文档
