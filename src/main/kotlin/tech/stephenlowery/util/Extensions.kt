package tech.stephenlowery.util

fun <T> T?.takeIfNotNull(): T? = this.takeIf { it != null }