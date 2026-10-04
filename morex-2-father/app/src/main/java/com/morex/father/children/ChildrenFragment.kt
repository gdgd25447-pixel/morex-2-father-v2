package com.morex.father.children

import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.launch
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.FragmentChildrenBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChildrenFragment : Fragment() {

    private var _binding: FragmentChildrenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChildrenViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentChildrenBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvChildren.layoutManager = LinearLayoutManager(requireContext())
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddChildActivity::class.java))
        }

        viewModel.children.observe(viewLifecycleOwner) { children ->
            binding.rvChildren.adapter = ChildCardAdapter(children) { child ->
                val i = Intent(requireContext(), ChildDetailActivity::class.java)
                i.putExtra("child_id", child.id)
                startActivity(i)
            }
            binding.emptyView.visibility = if (children.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loadChildren()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                com.morex.father.network.SocketManager.events.collect { e ->
                    if (e.name == com.morex.father.network.SocketManager.Events.DEVICE_ONLINE ||
                        e.name == com.morex.father.network.SocketManager.Events.DEVICE_OFFLINE) {
                        viewModel.loadChildren()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChildren()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
