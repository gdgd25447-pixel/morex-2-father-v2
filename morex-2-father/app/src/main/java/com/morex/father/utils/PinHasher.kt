package com.morex.father.utils

import at.favre.lib.crypto.bcrypt.BCrypt

/** تجزئة PIN المحلي بـ bcrypt (cost = 12). لا يُخزَّن الـ PIN نفسه أبداً. */
object PinHasher {
    private const val COST = 12

    fun hash(pin: String): String =
        BCrypt.withDefaults().hashToString(COST, pin.toCharArray())

    fun verify(pin: String, hash: String): Boolean = try {
        BCrypt.verifyer().verify(pin.toCharArray(), hash).verified
    } catch (e: Exception) {
        false
    }
}
