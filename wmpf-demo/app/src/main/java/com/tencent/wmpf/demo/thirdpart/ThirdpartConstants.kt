package com.tencent.wmpf.demo.thirdpart

interface ThirdpartConstants {

    interface Printer {
        interface Key {
            companion object {
                val KEY_RAW_DATA = "raw_data"
                val KEY_TIME_STAMP = "key_time_stamp"
                val KEY_TOKEN = "key_token"

                val KEY_RESULT_CODE = "result_code"
            }
        }
        interface Code {
            companion object {
                val CODE_SUCCESS = 10000
                val CODE_PRINTER_STATE = 10001
                val CODE_PRINT = 10002
            }
        }
        interface Action {
            companion object {
                val ACTION_THIRDPART = "com.tencent.wmpf.action.WMPF_PRINTER"
            }
        }
    }
}