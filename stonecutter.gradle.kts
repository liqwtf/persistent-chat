plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "26.1"

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}

stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
