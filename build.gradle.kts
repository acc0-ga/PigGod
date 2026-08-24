plugins {
    java
}

group = "com.example"
version = "1.3.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // 1.12 时代 Spigot API（仅作参考，实际尽量不依赖新方法）
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    // 仍用较新 API 编译（provided），运行时全部走反射/旧 API 兼容
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    // 关键：目标字节码 Java 8，才能在 1.12 服务器（常为 Java 8）上加载
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // 强制 Java 8 字节码（不使用 --release 17）
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