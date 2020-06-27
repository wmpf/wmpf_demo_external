package com.tencent.wmpf.activate.util

import kotlin.test.Test


class SampleTestsJVM {
    @Test
    fun testSignUpDevice() {
        println("platform: " + getPlatformName())
        // 0. 设置该脚本的工作目录
        WmpfDeviceSignUpUtil.workingDir = System.getProperty("java.io.tmpdir")
        // 1. 生成公私钥
        // 如果你已经有一对公私钥，重命名为<wmpfPrivateKeyPath.key,wmpfPublicKeyPath.key>，放在workingDir下面即可
        // 公钥会被命名为：$workingDir/wmpfPrivateKeyPath.key
        // 私钥会被命名为：$workingDir/wmpfPublicKeyPath.key
        val (privateKey, publicKey) = WmpfDeviceSignUpUtil.getPrivateKeyPublicKeyPair()
        println("public key = [$publicKey]")
        val productId = "your-product-id-from-we-cooper"
        val deviceId = "your-customized-device-id"
        // 2. 获取Signature
        val signature = WmpfDeviceSignUpUtil.getSignature(
            productId,
            deviceId,
            privateKey
        )
        println("signature = [$signature]")
        val deviceInfo = DeviceInfo()
        deviceInfo.model_name = "your-model-name"
        deviceInfo.product_id = productId
        deviceInfo.device_id_list = Array(1) { deviceId }.toList()
        // 3. 获取accessToken
        // 入参：微信开放平台注册的移动应用appId，移动应用appSecret
        val accessTokenResp =
            WmpfDeviceSignUpUtil.getAccessToken("your-app-id", "your-app-secret")
        if (accessTokenResp.errcode == 0) {
            println("access token [${accessTokenResp.access_token}] expires = " + accessTokenResp.expires_in)
            // 4. 添加设备信息到微信后台
            val resp = WmpfDeviceSignUpUtil.addDevicesToWeChatServer(
                accessToken = accessTokenResp.access_token,
                deviceInfo = deviceInfo
            )
            println("addDevicesToWeChatServer resp = [$resp]")
            // 5. 校验结果和签名
            if (resp.errcode == 0 && WmpfDeviceSignUpUtil.verifySignature(
                    productId,
                    deviceId,
                    publicKey,
                    signature
                )
            ) {
                println("success")
            } else {
                println("fail: code = ${resp.errcode}, msg = ${resp.errmsg}")
            }
        } else {
            println("get access token fail code = ${accessTokenResp.errcode}, msg = ${accessTokenResp.errmsg}")
        }
    }
}