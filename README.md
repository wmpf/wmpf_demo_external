<p align="center">
<a href="https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/">
    <img src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/logo.png" />
</a>
</p>

<h1 align="center">
  <a href="https://github.com/wmpf/wmpf_demo_external/">
   WMPF Client Demo
  </a>
</h1>

<p align="center">
  <strong>在没有安装微信客户端的设备</strong><br>
运行微信小程序
</p>

<p align="center">
	
<a href="https://img.shields.io/badge/license-MIT-blue.svg">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" />
</a>

<a href="https://github.com/wmpf/wmpf_demo_external/releases">
    <img src="https://img.shields.io/github/v/release/wmpf/wmpf_demo_external.svg" />
</a>
</p>


## 💡 WMPF是什么


<p align="center" >
<img  width = "600" src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/arch.png" alt="架构图" align=center />
</p>

WMPF是微信小程序硬件框架(WeChat Mini-Program Framework)的简称，该运行环境能让硬件在脱离微信客户端的情况下运行微信小程序，目前已支持Android，未来会支持到更多的平台

WMPF上运行的微信小程序，与手机客户端的微信小程序能力一致。通过WMPF，开发者可以将微信平台能力赋能到硬件设备上

想要顺利的运行小程序，需要两个apk

* **WMPF Service apk**：由微信定期打包发布，作为小程序框架的宿主环境
* **WMPF Client apk**：作为Service的调用方，需要你参考示例DEMO及文档进行定制


<p align="center" >
<img  width = "600" src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/seqDiag.png" alt="时序图" align=center />
</p>


本项目提供了WMPF Service的apk下载，WMPF Client示例DEMO以及说明文档

```
目录结构
 wmpf_demo_external
├── wmpf-demo 示例DEMO，帮助开发者快速构建自己的client，Android项目
├── wmpf-debug-assist 调试工具，方便小程序开发者在硬件上调试小程序
```

其中wmpf-demo有两个flavor，flavor可以在Android Studio中切换
* guide：在正式激活硬件后使用，可在此基础上做修改定制后正式上线使用
* experience：用于在不正式激活硬件的情况下体验WMPF，不能用于生产环境

## 💻 WMPF使用场景

WMPF可以应用在各行各业的安卓系统平板电脑、大屏设备等硬件，提供低成本屏幕互动解决方案，可接入设备包括但不限于

* 智慧零售：收银机 / 排号机 / 商场导航屏 / 自动贩卖机 / 点餐平板 / 互动广告屏幕等
* 家用及娱乐设备：智能冰箱 / 儿童平板 / 跑步机 / 电视机 / KTV点唱机等
* 公共服务：医院挂号机 / 图书租赁设备 / 美术馆办卡机等
* 办公设备：教育平板 / 会议终端 / 会议投屏等

## 🚀 正式接入流程

想要运行demo，首先需要在[wecooper平台](https://wecooper.weixin.qq.com/)注册

注册后完成后，在设备上运行小程序，只需要以下4步


1. 在[Releases Page](https://github.com/wmpf/wmpf_demo_external/releases)下载并安装最新版本的WMPF Service apk。alpha的WMPF apk有界面，方便调试，product的没有界面，可以用在正式使用（系统把应用保活即可）
2. 在Android Studio中导入运行[wmpf-client-demo](https://github.com/wmpf/wmpf_demo_external/tree/master/wmpf-demo)，切换flavor为experience，并在demo的`首页-快速体验`页面填写小程序的appId和ticket，appId和ticket的获取如下图所示。**注意：快速体验实际上是临时生成了设备信息，用于激活，这组信息权限较低且会失效，因此不应该用于开发环境，更不能用于生产环境。步骤4中有介绍如何注册激活硬件**
3. 在wmpf-client-demo中输入小程序的AppID，就可以运行和体验小程序了
4. [注册激活硬件](https://github.com/wmpf/wmpf_demo_external/wiki/%E7%A1%AC%E4%BB%B6%E6%B3%A8%E5%86%8C%E6%AD%A5%E9%AA%A4)，将获取到的信息填入[DeviceInfo.kt](https://github.com/wmpf/wmpf_demo_external/blob/master/wmpf-demo/app/src/main/java/com/tencent/luggage/demo/wxapi/DeviceInfo.kt)及[build.gradle](https://github.com/wmpf/wmpf_demo_external/blob/master/wmpf-demo/app/build.gradle)，设置`DeviceInfo.isInProductionEnv = true`，**切换项目的flavor为guide**，然后修改定制你的wmpf-client！

<p align="center">
<img src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/boost.png" width = "800" alt="boost" align=center />
</p>

<p align="center">
<img src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/wxa_demo.gif" width = "400" alt="gif" align=center />
</p>

##### 关于flavors构建变体
demo有两种变体，供开发者使用。注意两个变体不能同时安装，否则安装过程会出现INSTALL_FAILED_CONFLICTING_PROVIDE错误

* guide模式：提供API的使用示例，需要激活设备后使用   
* experience模式：供开发者进行体验与小程序的开发预览，不需要注册设备，只需要在[wecooper-快速体验-绑定小程序](https://wecooper.weixin.qq.com/)获取ticket即可运行    
* wxfacepay模式：提供给刷脸支付商户开发者的使用示例，需要激活设备后使用，主要演示商户应用调用刷脸支付和WMPF接口的流程和常见场景功能的代码实践    

<p align="center">
<img src="https://raw.githubusercontent.com/wmpf/wmpf_demo_external/master/assets/favor-example.png" width = "400" alt="png" align=center />
</p>

## 📖 文档

项目的[Wiki](https://github.com/wmpf/wmpf_demo_external/wiki)提供了更详细的接入指南，后台API，专有接口，硬件注册步骤等文档

## 🙋 帮助

* 阅读[微信小程序硬件框架接入文档](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/)和[Q&A](https://github.com/wmpf/wmpf_demo_external/wiki/Q&A)

* 阅读项目[Wiki](https://github.com/wmpf/wmpf_demo_external/wiki)

* 在[issues](https://github.com/wmpf/wmpf_demo_external/issues)中提出任何疑问与建议

## ⚖️ License

该项目在[MIT](https://github.com/wmpf/LICENSE)协议的许可下使用
