package com.gachon.mowa.ui.main.speaker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleObserver
import com.gachon.mowa.R
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.local.conversation.Conversation
import com.gachon.mowa.data.local.conversation.SpeakerType
import com.gachon.mowa.data.remote.weather.field.Main
import com.gachon.mowa.databinding.FragmentSpeakerBinding
import com.gachon.mowa.databinding.FragmentSpeakerLargeBinding
import com.gachon.mowa.dialogflow.DialogflowRequestService
import com.gachon.mowa.dialogflow.DialogflowResponseAPI
import com.gachon.mowa.ui.main.MainActivity
import com.gachon.mowa.util.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.gachon.mowa.util.ApplicationClass.Companion.sessionName
import com.gachon.mowa.util.ApplicationClass.Companion.sessionsClient
import com.gachon.mowa.util.ApplicationClass.Companion.showToast
import com.google.cloud.dialogflow.v2.*
import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.google.protobuf.Value.KindCase.*
import org.json.JSONArray
import java.util.*
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Dialogflow와 연결되어 대화하기 기능을 이용할 수 있는 화면
 */
class SpeakerLargeFragment : BaseFragment<FragmentSpeakerLargeBinding>(FragmentSpeakerLargeBinding::inflate),
    DialogflowResponseAPI, LifecycleObserver {
    companion object {
        const val TAG = "FRAG/SPEAKER"
    }

    private var params: Bundle = Bundle()
    private var ttsId: HashMap<String, String> = HashMap()
    private var conversations = ArrayList<Conversation>()
//    private var dialogflowReplies = ArrayList<DialogflowMessage>()

    private lateinit var speakerRVAdapter: SpeakerRVAdapter

    // TTS
    private lateinit var start: Conversation
    private lateinit var again: Conversation
    private lateinit var end: Conversation
    private lateinit var error: Conversation
    private lateinit var tts: TextToSpeech

    // STT
    private lateinit var mRecognizer: SpeechRecognizer
    private lateinit var intent: Intent
    private lateinit var mRecognitionListener: RecognitionListener

    override fun initAfterBinding() {
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        // Permission 체크
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO
                ), 1
            )
        }

        rotate()
        initData()

        initSTT()
        initTTS()

        initRecyclerView()
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
//        speakOut(start)

        conversations.clear()
        conversations.add(start)
        speakerRVAdapter.initData(conversations)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()

        mRecognizer.destroy()
//        mRecognizer.cancel()
    }

    /**
     * Speaker 애니메이션 및 view 초기화
     */
    private fun rotate() {
        val largeAnim = AnimationUtils.loadAnimation(context, R.anim.anim_speaker_rotate)
        val middleAnim = AnimationUtils.loadAnimation(context, R.anim.anim_speaker_rotate)
        binding.speakerCircleLargeV.startAnimation(largeAnim)
        binding.speakerCircleMiddleV.startAnimation(middleAnim)
    }

    /**
     * 초기 데이터 세팅
     */
    private fun initData() {
        conversations.clear()
        start = Conversation("안녕하세요. 무엇을 도와드릴까요?", SpeakerType.SPEAKER)
        again = Conversation("잘 알아듣지 못했어요. 다시 말씀해 주세요.", SpeakerType.SPEAKER)
        end = Conversation("확인했습니다. 감사합니다.", SpeakerType.SPEAKER)
        error = Conversation("에러가 발생했습니다. 다시 시도해 주세요.", SpeakerType.SPEAKER)
    }

    /**
     * TTS(Text to Speech) 기능을 위한 초기화
     */
    private fun initTTS() {
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null)
        ttsId[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "Start"

        tts = TextToSpeech(context) { p0 ->
            // onInit
            if (p0 == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.KOREA)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, result.toString())
                }
            } else {
                Log.e(TAG, p0.toString())
            }
        }

        // TTS callback 함수
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(p0: String?) {
                Log.d(TAG, "UtteranceProgressListener/onStart/p0: $p0")
            }

            override fun onDone(p0: String?) {

                (requireActivity() as MainActivity).runOnUiThread {
                    showToast(requireContext(), "음성 인식을 시작합니다.")
                    mRecognizer.startListening(intent)
                }

                Log.d(TAG, "UtteranceProgressListener/onDone/p0: $p0")

//                mRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
//                mRecognizer.setRecognitionListener(mRecognitionListener)
                Log.d(TAG, "UtteranceProgressListener/onDone/mRecognizer: $mRecognizer")
            }

            @Deprecated("Deprecated in Java")
            override fun onError(p0: String?) {
            }
        })
    }

    /**
     * STT(speech -> text) 기능을 위한 초기화
     * - RecognizerIntent 초기화
     * - RecognitionListener 초기화
     */
    private fun initSTT() {
        // RecognizerIntent 초기화
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireActivity().packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        // RecognitionListener 초기화
        mRecognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                Log.d(TAG, "initSTT/RecognitionListener/onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "initSTT/RecognitionListener/onBeginningOfSpeech")
            }

            override fun onRmsChanged(p0: Float) {
//                Log.d(TAG, "initSTT/RecognitionListener/onRmsChanged")
                // 들어오는 입력을 UI에서 다이나믹하게 보여줄 수 있다.
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.d(TAG, "initSTT/RecognitionListener/onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "initSTT/RecognitionListener/onEndOfSpeech")
            }

            override fun onError(p0: Int) {
                Log.d(TAG, "initSTT/RecognitionListener/onError/p0: $p0")

                if (p0 != 8) {
                    conversations.add(error)
                    speakerRVAdapter.initData(conversations)
                }
            }

            override fun onResults(p0: Bundle?) {
                Log.d(TAG, "initSTT/RecognitionListener/onResults")

                // 말을 하면 ArrayList에 단어를 넣고, textView에 단어를 이어준다.
                val results = p0!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = arrayOfNulls<String>(results!!.size)
                results.toArray(text)
                Log.d(TAG, "initSTT/RecognitionListener/onResults/text[0]: ${text[0]!!} ")

                sendMessage(text[0]!!)

//                for (i in 0 until results!!.size) {
//                    text = results[i]
//                }

                val stt = Conversation(text[0]!!, SpeakerType.USER)
                conversations.add(stt)
                speakerRVAdapter.initData(conversations)

                Log.d(TAG, "initSTT/RecognitionListener/onResults/conversations: $conversations")

                // 만약 특정 동작을 진행하게 하려면 (예를 들면 서버에 데이터 전송 등) 아래에 함수를 호출하면 된다.
                // 만약 계속 음성 인식을 이어나가려면 아래 코드를 추가하면 된다.
//                mRecognizer.startListening(intent)
            }

            override fun onPartialResults(p0: Bundle?) {
                Log.d(TAG, "initSTT/RecognitionListener/onPartialResults")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.d(TAG, "initSTT/RecognitionListener/onEvent")
            }
        }
    }

    /**
     * RecyclerView 초기화
     */
    private fun initRecyclerView() {
        val size = requireActivity().windowManager.currentWindowMetricsPointCompat()
        speakerRVAdapter = SpeakerRVAdapter(activity as MainActivity, size)
        binding.speakerRv.adapter = speakerRVAdapter
        binding.speakerRv.scrollToPosition(speakerRVAdapter.itemCount - 1)
    }

    /**
     * Click listener 초기화
     */
    private fun initClickListener() {

        // 스피커 아이콘을 클릭했을 때 음성 인식을 시작한다.
        binding.speakerCl.setOnClickListener {
            speakOut(start)
//            aiService.startListening()

            mRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            mRecognizer.setRecognitionListener(mRecognitionListener)
        }
    }

    /**
     * TTS(text -> speech) speack out
     */
    private fun speakOut(text: Conversation) {
        if (text.speakerType == SpeakerType.SPEAKER) {
            Log.d(TAG, "speakOut/SpeakerType.SPEAKER")

            val charSequence: CharSequence = text.text
            tts.setPitch((0.6).toFloat())
            tts.setSpeechRate((1).toFloat())    // 아마 속도 조절

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(charSequence.toString(), TextToSpeech.QUEUE_FLUSH, ttsId)
                Log.d(TAG, "speakOut/speak/charSequence.toString(): $charSequence")

//                mRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
//                mRecognizer.setRecognitionListener(mRecognitionListener)
//                mRecognizer.startListening(intent)
            } else {
                tts.speak(charSequence.toString(), TextToSpeech.QUEUE_FLUSH, ttsId)
            }
        } else {
            Log.d(TAG, "speakOut/SpeakerType.USER")
            return
        }
    }

    /**
     * Dialogflow를 이용해 메시지를 보낸다.
     */
    private fun sendMessage(message: String) {
        // 안에 인자로 STT로 변환한 텍스트를 넣어준다.
        val queryInput = QueryInput.newBuilder()
            .setText(TextInput.newBuilder().setText(message).setLanguageCode("ko")).build()

        DialogflowRequestService(this, sessionName, sessionsClient, queryInput).execute()
    }

    /**
     * Dialogflow에서 받은 response의 webhook payload를 deserialize
     */
    private fun Struct.toMap(): Map<String, Any?> =
        this.fieldsMap.mapValues { it.value.toAny() }

    private fun Value.toAny(): Any? =
        when (kindCase) {
            NULL_VALUE -> null
            NUMBER_VALUE -> numberValue
            STRING_VALUE -> stringValue
            BOOL_VALUE -> boolValue
            STRUCT_VALUE -> structValue.toMap()
            LIST_VALUE -> listValue.valuesList.map { it.toAny() }
            else -> error("Incorrect protobuf value $this")
        }

    /**
     * deserailze 후 richresponse의 items JSON Object 추출
     */
    private fun getItems(webhookpayload: Struct): JSONArray {
        val deserialized = webhookpayload.toMap()
        return JSONObject(deserialized).getJSONObject("google").getJSONObject("richResponse")
            .getJSONArray("items")
    }

    /**
     * Dialogflow에서 받은 응답이 콜백 함수를 통해 전달된다.
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun dialogflowCallback(detectIntentResponse: DetectIntentResponse?) {
        //현재는 단순 텍스트, basic card 두 경우만
        var responseType: Int = 0

        if (detectIntentResponse != null) {
            var reply = ""
            var thumbnailURL = ""
            var youtubeURL = ""
            val webhookPayload = detectIntentResponse.queryResult.webhookPayload
            val items = getItems(webhookPayload)
            responseType = items.length()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                Log.d(TAG, "item: $item")
                try {
                    if (item.has("simpleResponse")) {
                        val simpleResponse = item.getJSONObject("simpleResponse")
                        reply = simpleResponse.getString("textToSpeech")
                        continue
                    }
                    if (item.has("basicCard")) {
                        val basicCard = item.getJSONObject("basicCard")
                        thumbnailURL = basicCard.getJSONObject("image").getString("url")
                        youtubeURL = basicCard.getJSONArray("buttons").getJSONObject(0)
                            .getJSONObject("openUrlAction").getString("url")
                        continue
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "error: $e")
                }
            }
            if (reply.isNotEmpty()) {
//                dialogflowReplies.add(DialogflowMessage(reply, true))
//                conversations.add(Conversation(reply.toString(), SpeakerType.SPEAKER))

//                val temp = Conversation("안녕하세요, MoWA 입니다.", SpeakerType.SPEAKER)
//                conversations.add(temp)
//                speakerRVAdapter.initData(conversations)
                if (responseType == 1) {                    //text response
                    conversations.add(Conversation(reply, SpeakerType.SPEAKER))
                    speakerRVAdapter.initData(conversations)
                } else {               //basic card
                    conversations.add(
                        Conversation(
                            reply,
                            SpeakerType.SPEAKER,
                            thumbnailURL,
                            youtubeURL
                        )
                    )
                    speakerRVAdapter.initData(conversations)
                }

                Log.d(TAG, "dialogflowCallback/conversations: $conversations")

                // FIXME: 이후 채팅 메시지 올라오는 순서 변경하기 (아래 참고)
                // Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
            } else {
                Log.d(TAG, "dialogflowCallback/reply.isEmpty(): ${reply.isEmpty()}")
            }
        } else {
            Log.d(TAG, "dialogflowCallback/fail")
        }
    }
}
