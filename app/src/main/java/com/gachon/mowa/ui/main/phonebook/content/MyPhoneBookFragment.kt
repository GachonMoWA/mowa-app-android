package com.gachon.mowa.ui.main.phonebook.content

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.gachon.mowa.R
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.local.AppDatabase
import com.gachon.mowa.data.local.phonebook.PhoneBook
import com.gachon.mowa.databinding.FragmentMyPhoneBookBinding
import com.gachon.mowa.databinding.ItemPhoneBookPrivateBinding

class MyPhoneBookFragment :
    BaseFragment<FragmentMyPhoneBookBinding>(FragmentMyPhoneBookBinding::inflate) {
    companion object {
        const val TAG = "FRAG/PRIVATE"
        const val SORT_ORDER = ContactsContract.Data.MIMETYPE
    }

    private var phoneNumbers = ArrayList<PhoneBook>()

    private lateinit var cursor: Cursor
    private lateinit var roomDatabase: AppDatabase
    private lateinit var privateRVAdapter: MyPhoneBookRVAdapter

    override fun initAfterBinding() {
        roomDatabase = AppDatabase.getInstance(requireContext())!!
        phoneNumbers.clear()
        phoneNumbers.addAll(roomDatabase.phoneBookDao().getAll() as ArrayList)
    }

    override fun onResume() {
        super.onResume()

        initPhoneBook()
        initRecyclerView()
        initClickListener()
    }

    @SuppressLint("Range")
    private fun initPhoneBook() {
        // FIXME: 전화번호부가 초기화되면 비상연락처도 초기화 된다. (데이터베이스 테이블을 따로 만들거나 비교를 하거나)
        if (phoneNumbers.size == 0 || roomDatabase.phoneBookDao().getAll().size != phoneNumbers.size) {
            // 데이터베이스에 있는 전화번호 개수와 전화번호부의 전화번호 개수가 다른 경우 데이터 업데이트

            // 전화번호부를 가져오기 위한 초기화
            cursor = requireActivity().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                SORT_ORDER
            )!!

            if (phoneNumbers.size != 0) phoneNumbers.clear()

            while (cursor.moveToNext()) {
                val contactId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val tempPhoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val match = "[^0-9]"
                val phoneNumber = tempPhoneNumber.replace(match, "")

                Log.d(TAG, "initPhoneBook/name: $name, phoneNumber: $phoneNumber")
                phoneNumbers.add(PhoneBook(name, phoneNumber, 0))
            }

            cursor.close()
            initData()
        }
    }

    /**
     * Room 라이브러리를 이용해 내장 데이터베이스에 데이터를 저장한다.
     */
    private fun initData() {
        if (roomDatabase.phoneBookDao().getAll().size != phoneNumbers.size) {
            // 데이터베이스에 있는 내용을 초기화하고,
            roomDatabase.phoneBookDao().clearAll()

            // 다시 새로 데이터를 넣어준다.
            for (i in 0 until phoneNumbers.size) {
                roomDatabase.phoneBookDao().insert(phoneNumbers[i])
            }

            phoneNumbers.clear()
            phoneNumbers.addAll(roomDatabase.phoneBookDao().getAll() as ArrayList)
        }
    }

    /**
     * RecyclerView를 초기화합니다.
     */
    private fun initRecyclerView() {
        privateRVAdapter =
            MyPhoneBookRVAdapter(
                requireContext(),
                object : MyPhoneBookRVAdapter.MyItemClickListener {

                    override fun onItemClick(view: View, itemPosition: Int) {
                        Log.d(TAG, "initRecyclerView/onItemClick")
                    }

                    override fun onCheckBoxClick(
                        itemBinding: ItemPhoneBookPrivateBinding,
                        itemPosition: Int
                    ) {
                        phoneNumbers[itemPosition].isChecked = 1
                        Log.d(
                            TAG,
                            "initRecyclerView/onCheckBoxClick/phoneNumbers[itemPosition]: ${phoneNumbers[itemPosition]}"
                        )
                        roomDatabase.phoneBookDao().update(phoneNumbers[itemPosition])
                    }

                    override fun onCallClick(itemPosition: Int) {
                        showCallDialog(itemPosition)
                    }
                })

        binding.phoneBookPrivateRv.adapter = privateRVAdapter
        privateRVAdapter.addData(phoneNumbers)
    }

    /**
     * 로그아웃 확인 팝업창을 띄운다.
     */
    private fun showCallDialog(position: Int) {
        AlertDialog.Builder(requireContext(), R.style.dialog)
            .setTitle("전화 걸기")
            .setMessage("해당 번호로 전화를 거시겠습니까?")
            .setIcon(R.drawable.ic_mowa_not_title)
            .setPositiveButton("예") { _, _ ->
                // 전화 걸기 '예'를 클릭한 경우
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumbers[position].phoneNumber))
                startActivity(intent)
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    /**
     * 클릭 리스너 초기화
     */
    private fun initClickListener() {
        binding.phoneBookPrivateUpdateIv.setOnClickListener {
            initPhoneBook()
        }
    }
}
