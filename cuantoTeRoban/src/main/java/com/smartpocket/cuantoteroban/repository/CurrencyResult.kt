package com.smartpocket.cuantoteroban.repository

open class CurrencyResult(open val official: Double) {
    override fun toString() = "$official"
}

class DolarResult(override val official: Double, val blue: Double) : CurrencyResult(official) {
    override fun toString() = "DolarResult(official=$official, blue=$blue)"
}