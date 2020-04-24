package com.tencent.wmpf.demo.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MD5Util {
    companion object {
        fun getMD5String(text: String): String {
            try {
                val instance: MessageDigest = MessageDigest.getInstance("MD5")
                val digest:ByteArray = instance.digest(text.toByteArray())
                var sb = StringBuilder()
                for (b in digest) {
                    val i :Int = b.toInt() and 0xff
                    var hexString = Integer.toHexString(i)
                    if (hexString.length < 2) {
                        hexString = "0$hexString"
                    }
                    sb.append(hexString)
                }
                return sb.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ""
        }
    }
}