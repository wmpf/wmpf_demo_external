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

> 当前分支建议用于 WMPF Service APK >= 2.2（最低支持 2.1）。若使用低版本，请参考 apiv1 分支。
>
> WMPF 版本发布已迁移至[官方发布页](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/download.html)，github 仅提供 DEMO 代码，不再用做版本发布。

## 💡 WMPF是什么


<p align="center" >
<img  width = "600" src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/arch.png" alt="架构图" align=center />
</p>

WMPF是微信小程序硬件框架(WeChat Mini-Program Framework)的简称，该运行环境能让硬件在脱离微信客户端的情况下运行微信小程序，目前已支持Android，未来会支持到更多的平台。

WMPF 上运行的微信小程序，与手机客户端的微信小程序能力基本一致。通过 WMPF，开发者可以将微信平台能力赋能到硬件设备上。

想要顺利的运行小程序，需要两个apk

* **WMPF Service apk**：由微信定期打包发布，作为小程序框架的宿主环境
* **WMPF Client apk**：作为Service的调用方，需要你参考示例DEMO及文档进行定制

<p align="center" >
<img  width = "600" src="https://github.com/wmpf/wmpf_demo_external/blob/master/assets/seqDiag.png" alt="时序图" align=center />
</p>


本项目提供了 WMPF Service apk下 载，WMPF Client 示例 DEMO 以及说明文档

```
目录结构
wmpf_demo_external
├── wmpf-demo 示例 DEMO，帮助开发者快速构建自己的 client。（Android kotlin 项目）
├── wmpf-activate-util 设备签名工具，用来进行设备签名的生成和校验示例。（服务端 Java 项目）
```

#### 关于flavors构建变体

wmpf-demo 有两个flavor，flavor 可以在 Android Studio 中切换

* experience：用于在不正式激活硬件的情况下体验 WMPF 运行小程序，不需要注册设备，只需要在[wecooper-快速体验-绑定小程序](https://wecooper.weixin.qq.com/)获取 ticket 即可运行。不能用于生产环境。
* guide：提供API的使用示例，需要激活硬件后使用，可在此基础上做修改定制。

<p align="center">
<img src="https://raw.githubusercontent.com/wmpf/wmpf_demo_external/master/assets/favor-example.png" width = "400" alt="png" align=center />
</p>

## 💻 WMPF使用场景

WMPF可以应用在各行各业的安卓系统平板电脑、大屏设备等硬件，提供低成本屏幕互动解决方案，可接入设备包括但不限于：

* 智慧零售：收银机 / 排号机 / 商场导航屏 / 自动贩卖机 / 点餐平板 / 互动广告屏幕等
* 家用及娱乐设备：智能冰箱 / 儿童平板 / 跑步机 / 电视机 / KTV点唱机等
* 公共服务：医院挂号机 / 图书租赁设备 / 美术馆办卡机等
* 办公设备：教育平板 / 会议终端 / 会议投屏等


## 📖 文档

项目的[官方文档](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/)提供了更详细的接入指南，后台API，专有接口，硬件注册步骤等文档

## ⚡️ 快速体验

请阅读文档中[「快速体验」](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/demo.html)部分

## 🚀 正式接入流程

请阅读文档中[「接入指引」](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/process.html)和[「快速上手」](https://developers.weixin.qq.com/doc/oplatform/Miniprogram_Frame/quick-start.html)部分

## 🙋 帮助

可以在[微信开放社区「硬件服务」板块](https://developers.weixin.qq.com/community/minihome/mixflow/2351405025148862470)发帖反馈

## ⚖️ License

该项目在[MIT](https://github.com/wmpf/LICENSE)协议的许可下使用
