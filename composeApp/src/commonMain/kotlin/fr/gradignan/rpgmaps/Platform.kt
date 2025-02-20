package fr.gradignan.rpgmaps

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform