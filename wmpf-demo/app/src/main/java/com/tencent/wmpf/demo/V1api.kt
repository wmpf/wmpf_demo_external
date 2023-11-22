package com.tencent.wmpf.demo

import com.tencent.mm.ipcinvoker.IPCInvokeCallbackEx
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.cli.task.IPCInvokerTask_AuthorizeStatus
import com.tencent.wmpf.cli.task.TaskError
import com.tencent.wmpf.cli.task.pb.WMPFBaseRequestHelper
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker
import com.tencent.wmpf.cli.task.pb.proto.WMPFResponse
import com.tencent.wmpf.proto.WMPFAuthorizeStatusRequest
import com.tencent.wmpf.proto.WMPFAuthorizeStatusResponse
import io.reactivex.Single

object V1api {
    // 示例如何混用 v1 api
    private fun createTaskError(response: WMPFResponse?): TaskError {
        if (response == null) {
            return TaskError(TaskError.ErrType_NORMAL, -1, "response is null")
        }
        return TaskError(
            response.baseResponse.errType,
            response.baseResponse.errCode,
            response.baseResponse.errMsg
        )
    }

    private fun isSuccess(response: WMPFResponse?): Boolean {
        return response != null && response.baseResponse.errCode == TaskError.ErrType_OK
    }

    fun authorizeStatus(): Single<WMPFAuthorizeStatusResponse> {
        return Single.create {
            val request = WMPFAuthorizeStatusRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
                this.baseRequest.clientInvokeToken = WMPF.getInstance().deviceApi.invokeToken
            }

            val result =
                WMPFIPCInvoker.invokeAsync(
                    request,
                    IPCInvokerTask_AuthorizeStatus::class.java,
                    object : IPCInvokeCallbackEx<WMPFAuthorizeStatusResponse> {
                        override fun onBridgeNotFound() {
                            it.onError(WMPFApiException(TaskError.DISCONNECTED))
                        }

                        override fun onCallback(response: WMPFAuthorizeStatusResponse) {
                            if (isSuccess(response)) {
                                it.onSuccess(response)
                            } else {
                                it.onError(WMPFApiException(createTaskError(response)))
                            }
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(Exception("null"))
                            }
                        }
                    })

            if (!result) {
                it.onError(Exception("invoke authorizeStatus fail"))
            }
        }
    }
}