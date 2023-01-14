package com.gachon.mowa.ui.policy

import com.gachon.mowa.R
import com.gachon.mowa.base.BaseActivity
import com.gachon.mowa.data.local.policy.Policy
import com.gachon.mowa.databinding.ActivityPolicyBinding

class PolicyActivity : BaseActivity<ActivityPolicyBinding>(ActivityPolicyBinding::inflate) {
    private var policies: ArrayList<Policy> = ArrayList()
    private lateinit var policyRVAdapter: PolicyRVAdapter

    override fun initAfterBinding() {
        initData()
        initRecyclerView()
        initClickListener()
    }

    /**
     * 개인정보 보호방침에 대한 내용을 담는다.
     */
    private fun initData() {
        policies.add(
            Policy(
                this.resources.getString(R.string.policy_a_title),
                this.resources.getString(R.string.policy_a_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_b_title),
                this.resources.getString(R.string.policy_b_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_c_title),
                this.resources.getString(R.string.policy_c_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_d_title),
                this.resources.getString(R.string.policy_d_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_e_title),
                this.resources.getString(R.string.policy_e_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_f_title),
                this.resources.getString(R.string.policy_f_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_g_title),
                this.resources.getString(R.string.policy_g_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_h_title),
                this.resources.getString(R.string.policy_h_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_i_title),
                this.resources.getString(R.string.policy_i_description)
            )
        )

        policies.add(
            Policy(
                this.resources.getString(R.string.policy_j_title),
                this.resources.getString(R.string.policy_j_description)
            )
        )

    }

    /**
     * 리사이클러뷰 초기화
     */
    private fun initRecyclerView() {
        policyRVAdapter = PolicyRVAdapter(policies)
        binding.policyRv.adapter = policyRVAdapter
    }

    /**
     * 클릭 리스너 초기화
     */
    private fun initClickListener() {
        binding.policyExitBtnIv.setOnClickListener {
            // 우측 상단의 X 아이콘을 클릭하면 해당 화면을 닫는다.
            finish()
        }
    }

}