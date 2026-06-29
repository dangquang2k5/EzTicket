package huce.fit.myezticket.utils

import java.security.MessageDigest

object SecurityUtils {
    fun hashSHA256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
