package com.company.carryon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform