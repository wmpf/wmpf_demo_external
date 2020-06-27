package com.tencent.wmpf.activate.util

actual object WmpfDeviceSignUpUtil {
    actual fun getSignature(
        productId: String,
        deviceId: String,
        privateKey: String
    ): String {
        TODO("Not yet implemented")
    }

    actual fun verifySignature(
        productId: String,
        deviceId: String,
        privateKey: String,
        signature: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    actual fun addDevicesToWeChatServer(
        accessToken: String,
        productId: String,
        deviceIds: Array<String>,
        modelName: String
    ): String {
        TODO("Not yet implemented")
    }

    actual fun getPrivateKeyPublicKeyPair(keysStorageDir: String): Pair<String, String> {
        TODO("Not yet implemented")
    }
}

actual object Platform {
    actual val name: String = "JS"
}