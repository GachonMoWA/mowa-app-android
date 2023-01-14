package com.gachon.mowa.ui.main.speaker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gachon.mowa.data.local.conversation.Conversation
import com.gachon.mowa.data.local.conversation.SpeakerType
import com.gachon.mowa.data.local.conversation.ImageLoader
import com.gachon.mowa.databinding.ItemSpeakerLeftBinding
import com.gachon.mowa.databinding.ItemSpeakerRightBinding
import com.gachon.mowa.ui.main.MainActivity

class SpeakerRVAdapter(
    private val mContext: MainActivity,
    private val size: Point
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG = "RV/SPEAKER"
    }

    private var conversations = ArrayList<Conversation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SpeakerType.SPEAKER -> {
                Log.d(TAG, "onCreateViewHolder/SpeakerType.SPEAKER")
                SpeakerViewHolder(
                    ItemSpeakerLeftBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            else -> {
                Log.d(TAG, "onCreateViewHolder/SpeakerType.USER")
                UserViewHolder(
                    ItemSpeakerRightBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder/conversations[position]: ${conversations[position]}")

        when (conversations[position].speakerType) {
            SpeakerType.SPEAKER -> {
                (holder as SpeakerViewHolder).bind(conversations[position])
            }
            SpeakerType.USER -> {
                (holder as UserViewHolder).bind(conversations[position])
            }
            else -> {
                Log.d(TAG, "Not speaker and user type")
            }
        }
    }

    override fun getItemCount(): Int = conversations.size

    @SuppressLint("NotifyDataSetChanged")
    fun initData(conversations: List<Conversation>) {
        this.conversations.clear()
        this.conversations.addAll(conversations as ArrayList)
        notifyDataSetChanged()
    }

    /**
     * ViewHolder - Speaker (left side)
     */
    inner class SpeakerViewHolder(private val binding: ItemSpeakerLeftBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.itemSpeakerLeftTv.text = conversation.text

            if(conversation.thumbnailURL!="" && conversation.youtubeURL!=""){   //basic card

                //thumbnail
                binding.itemSpeakerLeftBasicCard.setVisibility(View.VISIBLE)
                ImageLoader.load(conversation.thumbnailURL,binding.itemSpeakerLeftThumbnail)

                //button
                binding.itemSpeakerLeftButton.setOnClickListener(){
                    val openURL= Intent(Intent.ACTION_VIEW)
                    openURL.data= Uri.parse(conversation.youtubeURL)
                    mContext.startActivity(openURL)
                }
            }
        }
    }

    /**
     * ViewHolder - User (right side)
     */
    inner class UserViewHolder(private val binding: ItemSpeakerRightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.itemSpeakerRightTv.text = conversation.text
        }
    }

    /**
     * 직접 설정한 뷰타입으로 설정되게 만든다.
     */
    override fun getItemViewType(position: Int): Int = conversations[position].speakerType
}
