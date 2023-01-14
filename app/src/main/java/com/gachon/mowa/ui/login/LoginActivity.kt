package com.gachon.mowa.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.gachon.mowa.base.BaseActivity
import com.gachon.mowa.data.remote.user.User
import com.gachon.mowa.data.remote.user.UserService
import com.gachon.mowa.data.remote.user.UserView
import com.gachon.mowa.databinding.ActivityIntroBinding
import com.gachon.mowa.ui.main.MainActivity
import com.gachon.mowa.util.ApplicationClass.Companion.showToast
import com.gachon.mowa.util.setUserEmail
import com.gachon.mowa.util.setUserId
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * If login is successful, then start the MainActivity
 * @see MainActivity
 */
class LoginActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate), UserView {
    companion object {
        private const val TAG = "ACT/INTRO"
        private const val GOOGLE_SIGN_IN = 1000
        lateinit var contextLogin: Context
    }

    private lateinit var userService: UserService
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    /**
     * Binding 이후에 실행되며, onCreate() 이후 수행할 작업을 넣어줍니다.
     */
    override fun initAfterBinding() {
        userService = UserService()
        mAuth = FirebaseAuth.getInstance()

        // 앱 풀네임 이름 변경한다.
        changeTextColor()

        // 이후 자동 로그인 작동을 잘 안 하면 mAuth.uid != null 분기 처리하기

        initGoogleLogin()
        initClickListener()
    }

    override fun onResume() {
        super.onResume()
        contextLogin = this
    }

    /**
     * 앱 풀네임 이름 변경
     */
    private fun changeTextColor() {
        val content = binding.introDescriptionMowaTv.text.toString()
        val spannable: Spannable = SpannableString(content)

        // Mo
        var target = "Mo"
        var start: Int = content.indexOf(target)
        var end = start + target.length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#0AA66D")),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // W
        target = "W"
        start = content.indexOf(target)
        end = start + target.length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#0AA66D")),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // A
        target = "A"
        start = content.indexOf(target)
        end = start + target.length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#0AA66D")),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.introDescriptionMowaTv.text = spannable
    }

    /**
     * Google login 초기화
     */
    private fun initGoogleLogin() {
        // Google login API
        val mGoogleSignInOptions: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("213775306504-jokug2opffdumle28tb7ijv933spgakq.apps.googleusercontent.com")
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, mGoogleSignInOptions)
    }

    /**
     * Click listener 초기화
     */
    private fun initClickListener() {
        // 구글 로그인 버튼을 클릭했을 때
        binding.introGoogleLoginCl.setOnClickListener {
            startActivityForResult(mGoogleSignInClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    /**
     * Activity result 처리
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                Log.d(TAG, "onActivityResult")
                val account = task.getResult(ApiException::class.java)
                googleLoginAuth(account)
            } catch (e: ApiException) {
                Log.d(TAG, "Login failed")
                Log.d(TAG, "Login failed/$e")
            }
        }
    }

    /**
     * Google login auth
     */
    private fun googleLoginAuth(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    // 로그인이 성공했을 때
                    showToast(this, "로그인 성공")

                    // NOTE: SharedPreferences에 uid를 저장할 필요 있을까?
                    // TODO: User 맥 주소 & 시리얼 번호 추가해야 되는지 확인하기
                    val user = User(
                        mAuth.currentUser!!.email.toString(),
                        null,
                        null
                    )
                    userService.addUser(this, user)

                    setUserId(mAuth.uid!!)
                    setUserEmail(mAuth.currentUser?.email!!)

                    startNextActivityWithClear(MainActivity::class.java)
                } else {
                    showToast(this, "로그인 실패")
                }
//                finish()
            }
    }

    /**
     * 구글 로그아웃
     */
    fun googleSignOut(context: Context) {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener {
                // 설정을 통해 로그아웃 버튼을 누른 경우 로그아웃 되고, 로그인 페이지로 넘어간다.
                showToast(context, "로그아웃 성공")
                startNextActivityWithClear(LoginActivity::class.java)
            }
    }

    /**
     * User 등록 API 성공했을 때
     */
    override fun onUserSuccess() {
        // TODO
    }

    /**
     * User 등록 API 실패했을 때
     */
    override fun onUserFailure(message: String) {
//        showToast(this, message)
        Log.d(TAG, "onUserFailure/message: $message")
    }
}
