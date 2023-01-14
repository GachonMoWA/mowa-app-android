package com.gachon.mowa.ui.main.phonebook

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.gachon.mowa.ui.main.phonebook.content.EmergencyPhoneBookFragment
import com.gachon.mowa.ui.main.phonebook.content.WelfareCenterFragment
import com.gachon.mowa.ui.main.phonebook.content.MyPhoneBookFragment

class PhoneBookVPAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {
    companion object {
        const val TAG = "VP/PHONEBOOK"
    }

    /**
     * ViewPager2를 이용해 fragment를 이동할 때 UI 업데이트를 위해 사용됩니다.
     */
    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val fragment = fragment.parentFragmentManager.findFragmentByTag("f$position")
        fragment?.let {
            if (it is EmergencyPhoneBookFragment) run {
                Log.d(TAG, "EmergencyFragment")
                it.updateRecyclerView()
            }
        }

        super.onBindViewHolder(holder, position, payloads)
    }

    /**
     * 3개의 프래그먼트가 있기 때문에 미리 지정해줍니다.
     */
    override fun getItemCount(): Int = 3

    /**
     * 각 뷰페이저마다 보여줄 프래그먼트를 지정합니다.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WelfareCenterFragment()
            1 -> MyPhoneBookFragment()
            else -> EmergencyPhoneBookFragment()
        }
    }
}
