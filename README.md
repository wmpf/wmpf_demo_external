<p align="center">
<a href="https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/">
    <img src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/logo.png" />
</a>
</p>

<h1 align="center">
  <a href="https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/">
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

WMPF是微信小程序框架(WeChat Mini-Program Framework)的简称，该运行环境能让硬件在脱离微信客户端的情况下运行微信小程序，目前已支持Android，未来会支持到更多的平台

WMPF上运行的微信小程序，与手机客户端的微信小程序能力一致。通过WMPF，开发者可以将微信平台能力赋能到硬件设备上

WMPF由两部分组成

* **WMPF Server apk**：由微信定期打包发布，作为Runtime的宿主环境
* **WMPF Client apk**：作为Server的调用方，需要你参考示例DEMO及文档进行定制


<p align="center" >
<img  width = "600" src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/seqDiag.png" alt="时序图" align=center />
</p>


本项目提供了WMPF Server的apk下载，WMPF Client示例DEMO以及说明文档

```
目录结构
 wmpf_demo_external
├── wmpf-demo 示例DEMO，帮助开发者快速构建自己的client，Android项目
├── wmpf-debug-assist 调试工具，方便小程序开发者在硬件上调试小程序
```

## 💻 WMPF使用场景

WMPF小程序框架，可以应用在各行各业的安卓系统平板电脑、大屏设备等硬件，提供低成本屏幕互动解决方案，可接入设备包括但不限于

* 智慧零售：收银机 / 排号机 / 商场导航屏 / 自动贩卖机 / 点餐平板 / 互动广告屏幕等
* 家用及娱乐设备：智能冰箱 / 儿童平板 / 跑步机 / 电视机 / KTV点唱机等
* 公共服务：医院挂号机 / 图书租赁设备 / 美术馆办卡机等
* 办公设备：教育平板 / 会议终端 / 会议投屏等

## 🚀 快速指南

想要快速在设备上运行小程序，只需要以下4步

1. 在[Releases Page](https://github.com/wmpf/wmpf_demo_external/releases)下载并安装最新版本的WMPF Server apk
2. 在Android Studio中导入运行[wmpf-client-demo](https://github.com/wmpf/wmpf_demo_external/tree/master/wmpf-demo)，并修改[Constants.kt](https://github.com/wmpf/wmpf_demo_external/blob/master/wmpf-demo/app/src/main/java/com/tencent/luggage/demo/wxapi/Constants.kt)
3. 在wmpf-client-demo中输入小程序的AppID，就可以运行和体验小程序了
4. 修改定制你的wmpf-client！

<p align="center">
<img src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/wxa_demo.gif" width = "400" alt="gif" align=center />
</p>

## 📖 文档

项目的[Wiki](https://github.com/wmpf/wmpf_demo_external/wiki)提供了更详细的接入指南，后台API，专有接口，硬件注册步骤等文档

## 🙋 帮助

* 阅读[小程序开放框架接入文档](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/)和[Q&A](https://github.com/wmpf/wmpf_demo_external/wiki/Q&A)

* 在[issues](https://github.com/wmpf/wmpf_demo_external/issues)中提出任何疑问与建议

## ⚖️ License

该项目在[MIT](https://github.com/wmpf/LICENSE)协议的许可下使用
