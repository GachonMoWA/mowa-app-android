package com.gachon.mowa.data.local.conversation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.gachon.mowa.ui.main.MainActivity
import java.net.URL
import java.util.concurrent.Executors

data class Conversation(
    var text: String = "",
    var speakerType: Int = SpeakerType.SPEAKER,
    var thumbnailURL: String = "",
    var youtubeURL: String =""
)

object SpeakerType {
    const val SPEAKER = 0
    const val USER = 1
}

object ImageLoader {
    fun load(url:String, view: ImageView){
        val executors = Executors.newSingleThreadExecutor()
        var bitmap: Bitmap

        executors.execute {
            try{
                bitmap=BitmapFactory.decodeStream(URL(url).openStream())
                view.setImageBitmap(bitmap)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}