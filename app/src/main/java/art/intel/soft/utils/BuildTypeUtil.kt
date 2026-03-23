package art.intel.soft.utils

import art.intel.soft.BuildConfig

fun isDevelopBuild(): Boolean = BuildConfig.BUILD_TYPE == BuildTypes.DEVELOP.name.toLowerCase()
fun isDebugBuild(): Boolean = BuildConfig.BUILD_TYPE == BuildTypes.DEBUG.name.toLowerCase()
fun isReleaseBuild(): Boolean = BuildConfig.BUILD_TYPE == BuildTypes.RELEASE.name.toLowerCase()

private enum class BuildTypes {
    RELEASE,
    DEBUG,
    DEVELOP,
}

