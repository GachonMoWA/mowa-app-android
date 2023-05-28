package com.gachon.mowa.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.gachon.mowa.util.Inflate

abstract class BaseFragment<T : ViewBinding>(private val inflate: Inflate<T>) :
    Fragment() {
    private var _binding: T? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initAfterBinding()
    }

    protected abstract fun initAfterBinding()

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
