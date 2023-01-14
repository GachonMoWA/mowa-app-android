package com.gachon.mowa.ui.main.phonebook.content

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenter
import com.gachon.mowa.databinding.ItemPhoneBookPublicBinding

class WelfareCenterRVAdapter(
    private val mContext: Context,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<WelfareCenterRVAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "RV/WELFARE-CENTER"
    }

    private var welfareCenters = ArrayList<WelfareCenter>()

    /**
     * 클릭 인터페이스
     */
    interface MyItemClickListener {
        fun onItemClick(view: View, itemPosition: Int)
    }

    /**
     * 뷰홀더를 생성해줘야 할 때 호출
     * 아이템 뷰 객체를 만들어서 뷰홀더에 넘겨준다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhoneBookPublicBinding =
            ItemPhoneBookPublicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * 뷰홀더에 데이터 바인딩을 할 때마다 호출
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(welfareCenters[position])

        // 아이템 중 하나를 클릭했을 때
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(holder.itemView, position)
        }
    }

    /**
     * 데이터셋의 크기 반환
     */
    override fun getItemCount(): Int = welfareCenters.size

    /**
     * 뷰홀더
     */
    inner class ViewHolder(val binding: ItemPhoneBookPublicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(welfareCenter: WelfareCenter) {
            binding.itemPhoneBookPublicNameTv.text = welfareCenter.name
            binding.itemPhoneBookPublicNumberTv.text = welfareCenter.telephoneNumber
        }
    }

    /**
     * RecyclerView에 데이터를 추가
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addData(publicInstitutions: List<WelfareCenter>) {
        this.welfareCenters.clear()
        this.welfareCenters.addAll(publicInstitutions as ArrayList)
        notifyDataSetChanged()
    }
}
