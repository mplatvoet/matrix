apply {
    plugin<JavaPlugin>()
}


configure<JavaPluginConvention> {
    setSourceCompatibility(1.7)
    setTargetCompatibility(1.7)
}