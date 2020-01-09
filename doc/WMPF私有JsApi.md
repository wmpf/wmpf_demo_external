# WMPF私有JSAPI

## 推送消息

- [获取PushToken](#getWmpfPushToken)


### getWmpfPushToken

获取消息PushToken
`see also: ./接口文档.md#应用推送消息后台接口`

### 调用参数
无参数

### 返回参数

| 参数   | 类型           | 说明 |
| ---- | -------------- | -------- |
|  token   | String |  可用于推送消息的token        |
|errMsg|string|成功：ok，错误：详细信息|


## 硬件接口

- [打印机](#printer)
- [设备SN码](#getDeviceSerialNumber)

### printer

传递文本内容到设备打印机服务

#### 调用参数

| 参数  | 类型           | 说明 |
| ---- | -------------- | -------- |
| data | string         | 要打印的内容，格式为json，具体含义见:./printer_jsondata.json |


#### 返回参数

| 参数    | 类型           | 说明 |
| ------ | -------------- | -------- |
| errMsg | 成功: ok, 错误：fail |
| errCode | 失败时有效，透传打印机服务的返回值 |


### getDeviceSerialNumber
异步返回

#### 调用参数

无

#### 返回参数

| 参数    | 类型           | 说明 |
| ------ | -------------- | -------- |
| errMsg | 成功: ok, 错误：fail |
| serialNumber | string |sn码|

#### 示例代码

```js
wmpf.getDeviceSerialNumber({
    success: function(res) {
        console.log(res.serialNumber)
    },
    fail: function(res) {
        console.log(JSON.stringify(res))
    }
})
```

### getDeviceSerialNumberSync
同步返回

#### 调用参数

无

#### 返回参数

| 参数    | 类型           | 说明 |
| ------ | -------------- | -------- |
| serialNumber | string | sn码,失败时接口会throw error |

#### 示例代码

```js
try {
    let res = wmpf.getDeviceSerialNumberSync()
    console.log(res.serialNumber)
} catch (e) {
    console.log(e)
}
```