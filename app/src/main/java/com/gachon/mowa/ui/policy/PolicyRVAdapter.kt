package com.gachon.mowa.ui.policy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gachon.mowa.data.local.policy.Policy
import com.gachon.mowa.databinding.ItemPolicyBinding

class PolicyRVAdapter(private val policies: ArrayList<Policy>) :
    RecyclerView.Adapter<PolicyRVAdapter.ViewHolder>() {
    private lateinit var binding: ItemPolicyBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPolicyBinding =
            ItemPolicyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(policies[position])
        binding = holder.binding
    }

    override fun getItemCount(): Int = policies.size

    inner class ViewHolder(val binding: ItemPolicyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(policy: Policy) {
            if (binding.itemPolicyTitleTv.text.equals(" ")) {
                binding.itemPolicyTitleTv.visibility = View.GONE
            } else {
                binding.itemPolicyTitleTv.text = policy.title
            }
            binding.itemPolicyContentTv.text = policy.content
        }
    }
}