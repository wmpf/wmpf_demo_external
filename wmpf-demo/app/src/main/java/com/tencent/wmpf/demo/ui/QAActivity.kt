package com.tencent.wmpf.demo.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tencent.mm.ipcinvoker.annotation.NonNull
import com.tencent.wmpf.demo.R

class QAActivity : AppCompatActivity() {

    private var recycleView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_qa)

        recycleView = findViewById(R.id.rv)

        recycleView?.adapter = QAItemAdapter(this, createQAList())
        recycleView?.layoutManager = LinearLayoutManager(this)
    }

    private fun createQAList(): ArrayList<QAItem> {
        val list = ArrayList<QAItem>()
        list.add(QAItem("如何在小程序中登出", "小程序中调用wx.logout即可"))
        list.add(QAItem("小程序运行过程中，调用wx.login无效", "需要授权运行中登录,详细看AuthorizeNoLogin"))
        list.add(QAItem("调用接口报invokeToken err", "首先需要调用activateDevice接口，并利用InvokeTokenHelper.initInvokeToken保存在本地,每次请求需要带上"))
        list.add(QAItem("二维码登录页面，二维码显示不出来", "检查AuthorizeNoLogin传入的参数是否正确"))
        list.add(QAItem("二维码登录页面，二维码显示出来，但扫码后不能正确获得相关信息", "一般是移动应用AppId没有权限，按照接入流程发邮件申请"))
        list.add(QAItem("IPCInvoker not initialize", "请继承WMPFApplication"))
        list.add(QAItem("预载时机", "预载一次即可，若重复调用会重新预载，结合自身业务调用即可，建议一般使用完一个小程序即可在空闲时机去做预载"))
        return list
    }

    data class QAItem(val question: String, val answer: String)
    
    class QAItemAdapter(val context: Context, private var items:ArrayList<QAItem>): RecyclerView.Adapter<QAItemAdapter.ViewHolder>() {
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val qAItem = items[position]
            holder.fillItem(position, qAItem)
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_qa_item, parent, false)
            return ViewHolder(v)
        }

        class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

            private val number: TextView = view.findViewById(R.id.number)
            private val question: TextView = view.findViewById(R.id.question)
            private val answer: TextView = view.findViewById(R.id.answer)

            fun fillItem(position: Int, @NonNull qaItem: QAItem) {
                number.text = position.toString()
                question.text = qaItem.question
                answer.text = qaItem.answer
            }
        }

    }


}