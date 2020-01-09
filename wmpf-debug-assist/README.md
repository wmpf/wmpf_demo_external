# 微信小程序框架-小程序开发调试工具

### What
为方便小程序开发者在硬件上更方便的调试硬件小程序，现封装出一个小程序开发者无需关注的wmpf-cli apk，小程序开发者在该app内授权登陆后便可打开自己开发过的开发版小程序体验版小程序。(该app下称DebugAssist)

### How
打开DebugAssist后，按照界面内的文字按钮依次操作 设备激活-授权登录-打开小程序 便可打开小程序调试。

### Tips
- 设备激活需由厂商提供productId/keyVersion/deviceId/signature，目前DebugAssist只接受从以下文件路径读取:"/sdcard/wmpf_device_activate_params.json"，其中json格式如下:
    ```js
    {
        "productId": productId,
        "keyVersion": keyVersion,
        "deviceId": deviceId,
        "signature": signature
    }
    ```
 - apk下载地址将随后补充   