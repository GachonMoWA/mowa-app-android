package com.gachon.mowa.ui.main.phonebook.content

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gachon.mowa.data.local.phonebook.PhoneBook
import com.gachon.mowa.databinding.ItemPhoneBookPrivateBinding

class MyPhoneBookRVAdapter(
    private val mContext: Context,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<MyPhoneBookRVAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "RV/PRIVATE"
    }

    private var phoneNumbers = ArrayList<PhoneBook>()

    /**
     * 클릭 인터페이스
     */
    interface MyItemClickListener {
        fun onItemClick(view: View, itemPosition: Int)
        fun onCheckBoxClick(itemBinding: ItemPhoneBookPrivateBinding, itemPosition: Int)
        fun onCallClick(itemPosition: Int)
    }

    /**
     * 뷰홀더를 생성해줘야 할 때 호출됩니다.
     * 아이템 뷰 객체를 만들어서 뷰홀더에 넘겨줍니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhoneBookPrivateBinding =
            ItemPhoneBookPrivateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * 뷰홀더에 데이터 바인딩을 할 때마다 호출됩니다.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(phoneNumbers[position])

        // 아이템 중 하나를 클릭했을 때
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(holder.itemView, position)
        }

        // 해당 아이템의 체크 박스를 클릭했을 때
        holder.binding.itemPhoneBookPrivateIsEmergencyCb.setOnClickListener {
            // 비상연락처에 등록합니다.
            mItemClickListener.onCheckBoxClick(holder.binding, position)
            notifyItemChanged(position)
        }

        // 해당 아이템의 전화 아이콘을 클릭했을 때
        holder.binding.itemPhoneBookPrivateCallIv.setOnClickListener {
            // 전화 아이콘
            mItemClickListener.onCallClick(position)
        }
    }

    /**
     * 데이터셋의 크기를 반환합니다.
     */
    override fun getItemCount(): Int = phoneNumbers.size

    /**
     * 뷰홀더
     */
    inner class ViewHolder(val binding: ItemPhoneBookPrivateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(phoneBook: PhoneBook) {
            binding.itemPhoneBookPrivateNameTv.text = phoneBook.name

            // 만약 비상 연락처에 등록이 되어 있다면 체크 표시를 해주고,
            // 그렇지 않다면 체크 표시를 해제합니다.
            binding.itemPhoneBookPrivateIsEmergencyCb.isChecked = phoneBook.isChecked != 0
        }
    }

    /**
     * RecyclerView에 데이터를 추가합니다.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addData(phoneNumbers: List<PhoneBook>) {
        this.phoneNumbers.clear()
        this.phoneNumbers.addAll(phoneNumbers as ArrayList)
        notifyDataSetChanged()
    }
}
