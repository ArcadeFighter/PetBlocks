dependencies {
    implementation(project(":petblocks-api"))
    implementation(project(":petblocks-core"))
    implementation(project(":petblocks-bukkit-api"))

    compileOnly("org.spigotmc:spigot113R2:1.13.2-R2.0")
    compileOnly("com.google.inject:guice:4.1.0")

    testCompile("org.spigotmc:spigot113R2:1.13.2-R2.0")
}