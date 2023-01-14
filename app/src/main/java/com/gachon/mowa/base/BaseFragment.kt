package com.gachon.mowa.base

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.gachon.mowa.util.Inflate

abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) :
    Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initAfterBinding()
    }

    protected abstract fun initAfterBinding()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
