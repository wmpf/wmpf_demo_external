package com.tencent.wmpf.activate.util

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit


actual object WmpfDeviceSignUpUtil {

    private val client = OkHttpClient().newBuilder()
        .build()

    var workingDir: String = System.getProperty("java.io.tmpdir")
        set(value) {
            if (!value.endsWith(File.separator)) {
                field = value + File.separator
            } else {
                field = value
            }
        }

    @JvmStatic
    actual fun getPrivateKeyPublicKeyPair(): Pair<String, String> {
        requestsCheck()
        var path = File(workingDir).canonicalPath
        if (!path.endsWith(File.separator)) {
            path += File.separator
        }
        workingDir = path
        val privateKeyFile = File(workingDir + "wmpfPrivateKeyPath.key")
        val publicKeyFile = File(workingDir + "wmpfPublicKeyPath.key")
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            return Pair(privateKeyFile.readText(), publicKeyFile.readText())
        }
        val ecParamFilePath = File("${workingDir}ec_param.pem.${secondsSinceEpoch()}").also { it.autoDelete() }

        "openssl ecparam -out ${ecParamFilePath.canonicalPath} -name prime256v1 -genkey".runCommand()
        "openssl genpkey -paramfile ${ecParamFilePath.canonicalPath} -out ${privateKeyFile.canonicalPath}".runCommand()
        "openssl pkey -in ${privateKeyFile.canonicalPath} -inform PEM -out ${publicKeyFile.canonicalPath} -outform PEM -pubout".runCommand()
        return Pair(privateKeyFile.readText(), publicKeyFile.readText())
    }

    @JvmStatic
    actual fun getSignature(productId: String, deviceId: String, privateKey: String): String {
        requestsCheck()
        val (ec_sign_info_file, ec_sign_info_sha256, ec_binary_sign_file) = createCommonFiles()
        val privateKeyFile = File("${workingDir}privateKey.${secondsSinceEpoch()}").also { it.autoDelete() }
        val signFile = File("${workingDir}sign.${secondsSinceEpoch()}").also { it.autoDelete() }

        privateKeyFile.writeText(privateKey)
        ec_sign_info_file.writeText("${productId}_$deviceId")

        "openssl dgst -sha256 -binary -out ${ec_sign_info_sha256.canonicalPath} ${ec_sign_info_file.canonicalPath}".runCommand()
        "openssl pkeyutl -sign -in ${ec_sign_info_sha256.canonicalPath} -out ${ec_binary_sign_file.canonicalPath} -inkey ${privateKeyFile.canonicalPath} -keyform PEM".runCommand()
        "openssl base64 -e -in ${ec_binary_sign_file.canonicalPath} -out ${signFile.canonicalPath}".runCommand()
        return signFile.readText()
    }

    @JvmStatic
    actual fun verifySignature(productId: String, deviceId: String, publicKey: String, signature: String): Boolean {
        requestsCheck()
        val (ec_sign_info_file, ec_sign_info_sha256, ec_binary_sign_file) = createCommonFiles()
        val signFile = File("${workingDir}signToCheck.${secondsSinceEpoch()}").also { it.autoDelete() }
        val publicFile = File("${workingDir}publicKeyToCheck.${secondsSinceEpoch()}").also { it.autoDelete() }

        publicFile.writeText(publicKey)
        signFile.writeText(signature)
        ec_sign_info_file.writeText("${productId}_$deviceId")

        "openssl dgst -sha256 -binary -out ${ec_sign_info_sha256.canonicalPath} ${ec_sign_info_file.canonicalPath}".runCommand()
        "openssl base64 -d -in ${signFile.canonicalPath} -out $ec_binary_sign_file".runCommand()
        val res =
            "openssl pkeyutl -verify -in ${ec_sign_info_sha256.canonicalPath} -sigfile ${ec_binary_sign_file.canonicalPath} -pubin -inkey ${publicFile.canonicalPath} -keyform PEM"
                .runCommandWithOutPut(redirect = ProcessBuilder.Redirect.PIPE)
        return res.contains("success", ignoreCase = true)
    }


    @JvmStatic
    actual fun getAccessToken(appId: String, appSecret: String): AccessTokenResp {
        val request = Request.Builder()
            .url("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${appId}&secret=${appSecret}")
            .build()
        val ret = AccessTokenResp()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                return try {
                    val respBody = response.body!!.source().readString(Charset.defaultCharset())
                    val accessTokenResp = Gson().fromJson(respBody, AccessTokenResp::class.java)
                    accessTokenResp.errmsg = ""
                    accessTokenResp.errcode = 0
                    accessTokenResp
                } catch (e: Exception) {
                    ret.errmsg = e.message.toString()
                    ret
                }
            } else {
                ret.errmsg = response.message
                ret.errcode = response.code
                return ret
            }
        } catch (e: Exception) {
            ret.errmsg = e.message.toString()
            return ret
        }
    }

    @JvmStatic
    actual fun addDevicesToWeChatServer(deviceInfo: DeviceInfo, accessToken: String): AddDeviceResp {
        val ret = AddDeviceResp()
        val gson = Gson()
        val reqData = gson.toJson(deviceInfo)
        try {
            val mediaType = "text/plain".toMediaTypeOrNull()
            val body: RequestBody = RequestBody.create(
                mediaType,
                reqData
            )
            val request: Request = Request.Builder()
                .url("https://api.weixin.qq.com/wxa/business/runtime/adddevice?access_token=${accessToken}")
                .method("POST", body)
                .addHeader("Content-Type", "text/plain")
                .build()
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val resp = response.body!!.source().readString(Charset.defaultCharset())
                return Gson().fromJson(resp, AddDeviceResp::class.java)
            } else {
                ret.errcode = response.code
                ret.errmsg = response.message
            }
        } catch (e: Exception) {
            ret.errmsg = e.message.toString()
        }
        return ret
    }


    private fun secondsSinceEpoch(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        calendar[2011, Calendar.OCTOBER] = 1
        return calendar.timeInMillis / 1000L
    }

    private fun File.autoDelete() {
        if (this.exists()) {
            this.delete()
        }
        this.createNewFile()
        this.deleteOnExit()
    }

    private fun createCommonFiles(): Triple<File, File, File> {
        val ecSignInfoFile = File("${workingDir}ec_sign_info_file.${secondsSinceEpoch()}").also { it.autoDelete() }
        val ecSignInfoSha256 = File("${workingDir}ec_sign_info_sha256.${secondsSinceEpoch()}").also { it.autoDelete() }
        val ecBinarySignFile = File("${workingDir}ec_binary_sign_file.${secondsSinceEpoch()}").also { it.autoDelete() }
        return Triple(ecSignInfoFile, ecSignInfoSha256, ecBinarySignFile)
    }

    private fun String.runCommandWithOutPut(workingDir: File = File("./"), redirect: ProcessBuilder.Redirect): String {
        return try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(redirect)
                .redirectError(redirect)
                .start()
            proc.waitFor(60, TimeUnit.MINUTES)
            val ret = proc.inputStream.bufferedReader().readText()
            ret
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    private fun String.runCommand(workingDir: File = File("./")) {
        try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
            proc.waitFor(60, TimeUnit.MINUTES)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun requestsCheck() {
//        if ("which openssl".runCommandWithOutPut(redirect = ProcessBuilder.Redirect.INHERIT)
//                .contains("not found", ignoreCase = true)
//        ) {
//            throw RuntimeException("dependencies not satisfied, openssl is requested")
//        }
    }
}

actual object Platform {
    actual val name: String = "JVM"
}