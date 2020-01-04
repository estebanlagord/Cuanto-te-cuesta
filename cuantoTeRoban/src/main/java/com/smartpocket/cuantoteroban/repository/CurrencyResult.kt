package com.smartpocket.cuantoteroban.repository

open class CurrencyResult(open val official: Double)

class DolarResult(override val official: Double, val blue: Double) : CurrencyResult(official)