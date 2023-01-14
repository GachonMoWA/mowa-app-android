package com.gachon.mowa.ui.main.phonebook.content

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gachon.mowa.data.local.phonebook.PhoneBook
import com.gachon.mowa.databinding.ItemPhoneBookEmergencyBinding

class EmergencyPhoneBookRVAdapter(
    private val mContext: Context,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<EmergencyPhoneBookRVAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "RV/EMERGENCY"
    }

    private var emergencyPhoneBooks = ArrayList<PhoneBook>()

    /**
     * 클릭 인터페이스
     */
    interface MyItemClickListener {
        fun onItemClick(view: View, itemPosition: Int)
    }

    /**
     * 뷰홀더를 생성해줘야 할 때 호출됩니다.
     * 아이템 뷰 객체를 만들어서 뷰홀더에 넘겨줍니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhoneBookEmergencyBinding =
            ItemPhoneBookEmergencyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    /**
     * 뷰홀더에 데이터 바인딩을 할 때마다 호출됩니다.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(emergencyPhoneBooks[position])

        // 아이템 중 하나를 클릭했을 때
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(holder.itemView, position)
        }
    }

    /**
     * 데이터셋의 크기를 반환합니다.
     */
    override fun getItemCount(): Int = emergencyPhoneBooks.size

    /**
     * 뷰홀더
     */
    inner class ViewHolder(val binding: ItemPhoneBookEmergencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(phoneBook: PhoneBook) {
            binding.itemPhoneBookEmergencyNameTv.text = phoneBook.name
            binding.itemPhoneBookEmergencyNumberTv.text = phoneBook.phoneNumber
        }
    }

    // RecyclerView에 데이터를 추가합니다.
    @SuppressLint("NotifyDataSetChanged")
    fun addData(emergencyPhoneNumbers: List<PhoneBook>) {
        this.emergencyPhoneBooks.clear()

        // 비상 연락처로 체크된 것만 가져온다.
        this.emergencyPhoneBooks.addAll(emergencyPhoneNumbers.filter { it.isChecked == 1 })

        notifyDataSetChanged()
    }
}
