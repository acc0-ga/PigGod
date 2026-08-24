plugins {
    java
}

group = "com.example"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // 编译使用较新 API，运行时靠反射/兼容层尽量覆盖多版本
    // 26.2 需要 Java 25；这里仍用 1.21.11 编译以兼容更多构建环境
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    // 1.16.5~1.21.11 常用 Java 17/21；26.x 需要 Java 25
    // 编译目标设为 17，可在更多环境运行（26.x 服务器需自行用 Java 25 运行）
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
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