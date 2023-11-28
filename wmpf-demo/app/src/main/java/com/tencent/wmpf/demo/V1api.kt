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
import java.util.concurrent.CompletableFuture

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

    fun authorizeStatus(): CompletableFuture<WMPFAuthorizeStatusResponse> {
        val future = CompletableFuture<WMPFAuthorizeStatusResponse>()

        val request = WMPFAuthorizeStatusRequest().apply {
            this.baseRequest = WMPFBaseRequestHelper.checked()
            this.baseRequest.clientInvokeToken = WMPF.getInstance().deviceApi.invokeToken
        }

        val result = WMPFIPCInvoker.invokeAsync(request,
            IPCInvokerTask_AuthorizeStatus::class.java,
            object : IPCInvokeCallbackEx<WMPFAuthorizeStatusResponse> {
                override fun onBridgeNotFound() {
                    future.completeExceptionally(WMPFApiException(TaskError.DISCONNECTED))
                }

                override fun onCallback(response: WMPFAuthorizeStatusResponse) {
                    if (isSuccess(response)) {
                        future.complete(response)
                    } else {
                        future.completeExceptionally(WMPFApiException(createTaskError(response)))
                    }
                }

                override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                    if (exception != null) {
                        future.completeExceptionally(exception)
                    } else {
                        future.completeExceptionally(Exception("null"))
                    }
                }
            })

        if (!result) {
            future.completeExceptionally(Exception("invoke authorizeStatus fail"))
        }
        return future
    }
}