package com.tencent.wmpf.demo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.tencent.mm.ipcinvoker.IPCInvokeCallback
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFDeviceApi
import com.tencent.wmpf.cli.event.AbstractOnAgentInvokeEventHandler
import com.tencent.wmpf.cli.event.WMPFAgentInvokeData
import com.tencent.wmpf.cli.event.WMPFAgentInvokeRespData
import com.tencent.wmpf.demo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AgentActivity : ApiActivity() {
    companion object {
        const val TAG = "AgentActivity"
    }

    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
//    private lateinit var voiceButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var messageRecyclerView: RecyclerView
    private val messageList = mutableListOf<String>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent)

        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
//        voiceButton = findViewById(R.id.voiceButton)
        refreshButton = findViewById(R.id.refresh)

        adapter = MessageAdapter(messageList)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter

        sendButton.setOnClickListener { onSend() }
//        voiceButton.setOnClickListener { /* TODO: Implement voice input */ }
        invokeWMPFApi("registerInvokeEventHander") {
            WMPF.getInstance().agentApi.registerInvokeEventHandler(
                mAgentInvokeEventHandler
            )
        }
        refreshButton.setOnClickListener {
            cleanupAgentContext()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        invokeWMPFApi("unregisterInvokeEventHander") {
            WMPF.getInstance().agentApi.unregisterInvokeEventHandler(
                mAgentInvokeEventHandler
            )
        }
    }


    private fun onSend() {
        val message = messageEditText.text.toString()
        onSendMessage(message)
    }

    private fun onSendMessage(message: String) {
        if (message.isNotEmpty()) {
            runOnUiThread {
                messageEditText.isEnabled = false
                messageEditText.hint = "等待回复中"
                messageEditText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
            messageList.add("You: $message")
            adapter.notifyItemInserted(messageList.size - 1)
            messageEditText.text.clear()
            messageRecyclerView.scrollToPosition(messageList.size - 1)
            // Simulate receiving a response after 1 second
//            messageRecyclerView.postDelayed({ onReceiveMessage("Bot: This is a reply to '$message'") }, 1000)
            if (!isInAgent) {
                invokeWMPFApi("startAgentByIntent") {
                    WMPF.getInstance().deviceApi.wakeupWMPFIfNeed()
                    agentRequestId = WMPF.getInstance().agentApi.startAgentByIntent(
                        "order", message, "{\"showForeground\":true}",1
                    )
                    isInAgent = true
                }
            } else {
                Log.i(TAG, "callback feedback $message")
                if (mAgentFeedbackCallback == null) {
                    Log.w(TAG, "feedback when callback is null")
                }
                mAgentFeedbackCallback?.onCallback(message)
                mAgentFeedbackCallback = null
            }

        }
    }

    private fun onReceiveMessage(message: String) {
        Log.i(TAG, "onReceiveMessage$message")
        runOnUiThread {
            messageEditText.isEnabled = true
            messageEditText.hint = ""
            messageEditText.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            messageList.add("小程序: $message")
            adapter.notifyItemInserted(messageList.size - 1)
            messageRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private inner class MessageAdapter(private val messages: List<String>) :
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.textView.text = messages[position]
        }

        override fun getItemCount() = messages.size

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cleanupAgentContext() {
        isInAgent = false
        mAgentFeedbackCallback = null
        invokeWMPFApi("stopAgent") {
            WMPF.getInstance().agentApi.stopAgent(agentRequestId)
        }
        agentRequestId = null
        messageList.clear()
        adapter.notifyDataSetChanged()
    }

    //Agent corresponding fields

    private var isInAgent = false

    private var agentRequestId: String? = null

    private var mAgentFeedbackCallback: AgentInvokeEventHandler.AgentInvokeListener.AgentInvokeFeedbackCallback? =
        null

    private val mAgentInvokeEventHandler =
        AgentInvokeEventHandler(object : AgentInvokeEventHandler.AgentInvokeListener {
            override fun onFeedback(
                message: AgentInvokeEventHandler.FeedbackData,
                callback: AgentInvokeEventHandler.AgentInvokeListener.AgentInvokeFeedbackCallback
            ) {
                this@AgentActivity.mAgentFeedbackCallback = callback
                this@AgentActivity.onReceiveMessage(message.content)
            }
        })

    private class AgentInvokeEventHandler(val listener: AgentInvokeListener) :
        AbstractOnAgentInvokeEventHandler() {
        companion object {
            const val NAME_STATUS_CHANGE = "statusChange"
            const val NAME_FEEDBACK = "feedback"
        }

        interface AgentInvokeListener {
            interface AgentInvokeFeedbackCallback {
                fun onCallback(feedbackMessage: String)
            }

            fun onFeedback(
                message: FeedbackData, callback: AgentInvokeFeedbackCallback
            )
        }

        data class StatusChangeData(val status: String)

        data class FeedbackData(
            val content: String, val intentUnrelated: Boolean, val preUserAnswer: String
        )

        override fun onInvoke(
            data: WMPFAgentInvokeData, callback: IPCInvokeCallback<WMPFAgentInvokeRespData>
        ) {
            val gson = Gson()
            Log.i(TAG, "agentOnInvoke $data")
            if (data.name == NAME_FEEDBACK) {
                val innerData = gson.fromJson(data.args, FeedbackData::class.java)
                listener.onFeedback(
                    innerData,
                    object : AgentInvokeListener.AgentInvokeFeedbackCallback {
                        override fun onCallback(feedbackMessage: String) {
                            callback.onCallback(WMPFAgentInvokeRespData("{\"feedback\":\"${feedbackMessage}\"}"))
                        }
                    })

            } else if (data.name == NAME_STATUS_CHANGE) {
                val innerData = gson.fromJson(data.args, StatusChangeData::class.java)
                Log.i(TAG, "on status change ${innerData.status} id ${data.requestId}")
            }
        }

    }
}
