###打印数据格式定义

* Json格式定义


```
{
  "type":"base_string", //打印类型 base_string, ext_string, both_side,barcode,qrcode,image
  "content":"print sample", //打印的内容: both_side类型时为json string {"left","text on the left","right":"text on the right"}。image类型时为图片的base64编码
  "style":{
      "fontSize":25, //文字大小,int型
      "align": "center", //文字位置，可选值: left/center/right. 不传值默认为center
      "bold": 0, //是否加粗，int型：0否，1是.不传值默认为0
      "italic": 0, //是否斜体，int型：0否，1是.不传值默认为0
      "underline": 0, //是否下划线，int型：0否，1是.不传值默认为0。ext_string, both_side类型适用
      "leftOffset": 0, //左侧偏移，int型：不传值默认为0。ext_string类型适用
      "letterWidth": 0, //字母宽度，float型：不传值默认为0。ext_string类型适用
      "lineSpacing": 1.0, //行间距，float型：不传值默认为1.0。ext_string, both_side类型适用
      "font": "default", //字体，可选值：default/default_bold/monospace/serif/sans_serif。不传值默认为default。ext_string, both_side类型适用
  }, //文字的样式，适用于base_string，ext_string, both_side文字样式设定
  
  "barCodeOption": {
      "barCodeType": "code_128", //barcode类型：aztec/codabar/code_39/code_93/code_128/data_matrix/ean_8/ean_13/itf/maxicode/pdf_417/qr_code/rss_14/rss_expended/upc_a/upc_e/upc_ean_extension。不传值默认为code_128
      "barWidth": "large", //barcode宽度：small/normal/large/huge。不传值默认为large
      "height": 50, //高度，int类型。不传值默认为50
      "offset": 20, //偏移值，int类型。不传值默认为20
  }, // barcode设定，适用barcode类型
  
  "qrCodeOption": {
      "width": 400 //宽度，int类型。不传值默认为400
  }, //qrcode设定
  
  "imageOption": {
      "width": 300 //宽度，int类型。
      "height": 500 //高度，int类型
      "align": "center", //图片位置，可选值: left/center/right. 不传值默认为center
      "offset": 0, //偏移值，int类型。不传值默认为0
  }, //图片类型设定。宽度如超出打印纸的宽度，打印不会完整
  
}
  
  


例子:
[
  {
    "type":"base_string",
    "content":"北京微智全景信息技术有限公司",
    "style": {
        "fontSize":30,
        "bold": 1
    }
    
  },
  {
    "type":"base_string",
    "content":"顾客信息",
    "style": {
        "fontSize":25,
        "bold": 1
    }
    
  },
  {
    "type":"both_side",
    "content":{
        "left": "顾客姓名",
        "right": "Kommi"
    },
    "style": {
        "fontSize":20,
        "bold": 0
    }
    
  },
  {
    "type":"base_string",
    "content":"其他信息",
    "style": {
        "fontSize":25,
        "bold": 1
    }
    
  },
  {
    "type":"both_side",
    "content":{
        "left": "核销门店",
        "right": "为你客407国道店"
    },
    "style": {
        "fontSize":20,
        "bold": 0
    }
    
  },
  {
    "type":"both_side",
    "content":{
        "left": "核销码",
        "right": "5665470281092409"
    },
    "style": {
        "fontSize":20,
        "bold": 0
    }
    
  },
  {
    "type":"barcode",
    "content": "5665470281092409",
    "barCodeOption": {
        "barWidth":"normal",
    }
    
  }

]
```

* 打印结果返回值
  1. error_none : 10000 。无错误，正常打印
  2. error_printer_state: 10001。打印机状态异常。如无打印纸，打印机不可用等
  3. error_print: 10002。打印异常。打印过程中的异常


