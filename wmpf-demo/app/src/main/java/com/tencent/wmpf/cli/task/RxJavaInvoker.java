package com.tencent.wmpf.cli.task;

import com.tencent.mm.ipcinvoker.IPCInvokeCallback;
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker;
import com.tencent.wmpf.proto.WMPFAuthorizeNoLoginRequest;
import com.tencent.wmpf.proto.WMPFAuthorizeNoLoginResponse;
import com.tencent.wmpf.proto.WMPFAuthorizeRequest;
import com.tencent.wmpf.proto.WMPFAuthorizeResponse;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

public class RxJavaInvoker {

    public static Single<WMPFAuthorizeResponse> invokeInitTask(final WMPFAuthorizeRequest request) {
        return Single.create(new SingleOnSubscribe<WMPFAuthorizeResponse>() {
            @Override
            public void subscribe(final SingleEmitter<WMPFAuthorizeResponse> emitter) {
                final boolean result = WMPFIPCInvoker.invokeAsync(
                        request,
                        IPCInvokerTask_Authorize.class,
                        new IPCInvokeCallback<WMPFAuthorizeResponse>() {
                            @Override
                            public void onCallback(WMPFAuthorizeResponse response) {
                                emitter.onSuccess(response);
                            }
                        });

                if (!result) {
                    emitter.onError(new Exception());
                }
            }
        });
    }
}
