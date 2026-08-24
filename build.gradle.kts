plugins {
    java
}

group = "com.example"
version = "1.3.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// 强制依赖解析按 JVM 21，避免 release/target 8 导致无法解析 Paper API
configurations.configureEach {
    attributes {
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // 不使用 options.release（会触发 TargetJvm 8 解析）
    // 用 source/target 输出 1.8 字节码
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("PigGod")
}