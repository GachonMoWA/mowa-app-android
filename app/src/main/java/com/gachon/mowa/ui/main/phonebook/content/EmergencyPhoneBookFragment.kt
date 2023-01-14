package com.gachon.mowa.ui.main.phonebook.content

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.local.AppDatabase
import com.gachon.mowa.data.local.phonebook.PhoneBook
import com.gachon.mowa.databinding.FragmentEmergencyPhoneBookBinding

class EmergencyPhoneBookFragment :
    BaseFragment<FragmentEmergencyPhoneBookBinding>(FragmentEmergencyPhoneBookBinding::inflate) {
    companion object {
        const val TAG = "FRAG/EMERGENCY"
    }

    private var phoneNumbers = ArrayList<PhoneBook>()
    private lateinit var roomDatabase: AppDatabase
    private lateinit var emergencyRVAdapter: EmergencyPhoneBookRVAdapter

    override fun initAfterBinding() {
        roomDatabase = AppDatabase.getInstance(requireContext())!!
    }

    override fun onResume() {
        super.onResume()
        initData()
        initRecyclerView()
    }

    /**
     * 데이터베이스로부터 데이터를 받아 초기화합니다.
     */
    private fun initData() {
        phoneNumbers.clear()
        phoneNumbers.addAll(roomDatabase.phoneBookDao().getAll() as ArrayList)
    }

    /**
     * RecyclerView를 초기화합니다.
     * 이때, 안에 내용이 들어가게 됩니다.
     */
    private fun initRecyclerView() {
        emergencyRVAdapter =
            EmergencyPhoneBookRVAdapter(
                requireContext(),
                object : EmergencyPhoneBookRVAdapter.MyItemClickListener {

                    override fun onItemClick(view: View, itemPosition: Int) {
                        Log.d(TAG, "initRecyclerView/onItemClick")
                        val intent = Intent(
                            Intent.ACTION_CALL,
                            Uri.parse("tel:" + phoneNumbers[itemPosition])
                        )
                        startActivity(intent)
                    }
                })

        binding.phoneBookEmergencyRv.adapter = emergencyRVAdapter
        emergencyRVAdapter.addData(phoneNumbers)
    }

    /**
     * RecyclerView 업데이트 함수
     */
    fun updateRecyclerView() {
        initData()
        initRecyclerView()
    }
}
