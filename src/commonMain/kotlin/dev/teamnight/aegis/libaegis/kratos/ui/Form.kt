package dev.teamnight.aegis.libaegis.kratos.ui

data class Form(
    val action: String,
    val method: String,
    val nodes: List<Element>,
    val messages: List<Text>,
)