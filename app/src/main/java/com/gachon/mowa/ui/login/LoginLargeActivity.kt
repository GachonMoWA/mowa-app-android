package com.gachon.mowa.ui.login

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.gachon.mowa.base.BaseActivity
import com.gachon.mowa.databinding.ActivityIntroLargeBinding

class LoginLargeActivity :
    BaseActivity<ActivityIntroLargeBinding>(ActivityIntroLargeBinding::inflate) {

    /**
     * onCreate 이후의 작업을 넣어줍니다.
     */
    override fun initAfterBinding() {

        // 앱 풀네임 이름 변경
        changeTextColor()
    }

    /**
     * 앱 풀네임 이름 변경
     */
    private fun changeTextColor() {
        val content = binding.introLargeDescriptionTv.text.toString()
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

        binding.introLargeDescriptionTv.text = spannable
    }
}
