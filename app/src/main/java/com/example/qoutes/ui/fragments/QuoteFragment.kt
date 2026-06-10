package com.example.qoutes.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.qoutes.R
import com.example.qoutes.databinding.FragmentQuoteBinding
import com.example.qoutes.models.Quote
import com.example.qoutes.ui.QuotesActivity
import com.example.qoutes.util.Constants.Companion.MIN_SWIPE_DISTANCE
import com.example.qoutes.util.Resource
import com.example.qoutes.util.ShareUtils
import com.example.qoutes.viewmodels.QuoteViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class QuoteFragment : Fragment() {

    private val viewModel by activityViewModels<QuoteViewModel>()
    private val args: QuoteFragmentArgs by navArgs()

    private var quote: Quote? = null
    private var quoteShown = false
    private var isBookMarked = false
    private lateinit var binding: FragmentQuoteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = args.categoryId
        viewModel.loadQuotesForCategory(categoryId)

        viewModel.quote.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    showProgressBar()
                    with(binding) {
                        noQuote.visibility = View.GONE
                        fab.visibility = View.GONE
                        quoteShare.visibility = View.GONE
                    }
                    quoteShown = false
                    quote = null
                }

                is Resource.Success -> {
                    hideProgressBar()
                    with(binding) {
                        noQuote.visibility = View.GONE
                        fab.visibility = View.VISIBLE
                        quoteShare.visibility = View.VISIBLE

                        response.data?.let { quoteResponse ->
                            quote = quoteResponse
                            quoteTv.text = resources.getString(R.string.quote, quoteResponse.quote)
                            authorTv.text = resources.getString(R.string.author, quoteResponse.author)
                            showTextViews()
                        }
                    }
                    quoteShown = true
                }

                is Resource.Error -> {
                    hideProgressBar()
                    hideTextViews()
                    with(binding) {
                        noQuote.visibility = View.VISIBLE
                        fab.visibility = View.GONE
                        quoteShare.visibility = View.GONE
                        response.message?.let { noQuote.text = it }
                    }
                    quoteShown = false
                    quote = null
                }
            }
        }

        viewModel.bookmarked.observe(viewLifecycleOwner) {
            isBookMarked = it
            binding.fab.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isBookMarked) R.drawable.ic_bookmarked
                    else R.drawable.ic_unbookmarked
                )
            )
        }

        var dX = 0f
        var initialX = 0f

        binding.quoteCard.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = view.x
                    dX = view.x - event.rawX
                    view.animate().cancel()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    if (newX < initialX) {
                        view.animate()
                            .x(newX)
                            .setDuration(0)
                            .start()

                        if (view.x < MIN_SWIPE_DISTANCE) {
                            binding.extraText.text = getString(R.string.release_hint)
                            binding.extraText.setTextColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
                        } else {
                            binding.extraText.text = getString(R.string.swipe_hint)
                            binding.extraText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (view.x < MIN_SWIPE_DISTANCE) {
                        view.animate()
                            .x(-view.width.toFloat())
                            .setDuration(200)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    viewModel.showRandomQuoteFromList()

                                    view.x = initialX
                                    view.animate().setListener(null)

                                    binding.extraText.text = getString(R.string.swipe_hint)
                                }
                            }).start()
                    } else {
                        view.animate()
                            .x(initialX)
                            .setDuration(300)
                            .start()
                        binding.extraText.text = getString(R.string.swipe_hint)
                    }
                    true
                }
                else -> false
            }
        }

        binding.quoteTv.setOnClickListener {
            quote?.let { currentQuote ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("quote", "\"${currentQuote.quote}\"\n\n- ${currentQuote.author}")
                clipboard.setPrimaryClip(clip)
                makeSnackBar(view, "Quote Copied to Clipboard!")
            }
        }

        binding.fab.setOnClickListener {
            if (isBookMarked) {
                viewModel.deleteQuote(quote!!)
                if ((requireActivity() as QuotesActivity).atHome) Snackbar.make(
                    requireActivity().findViewById(R.id.quotesNavHostFragment),
                    "Removed Bookmark!", Snackbar.LENGTH_SHORT
                ).apply {
                    setAction("Undo") {
                        viewModel.saveQuote(quote!!)
                        if ((requireActivity() as QuotesActivity).atHome) makeSnackBar(view, "Re-saved!")
                    }
                    setActionTextColor(ContextCompat.getColor(view.context, R.color.light_blue))
                    show()
                }

                binding.fab.visibility = View.INVISIBLE
                lifecycleScope.launch(Dispatchers.Default) {
                    delay(3000)
                    withContext(Dispatchers.Main) {
                        binding.fab.visibility = View.VISIBLE
                    }
                }
            } else {
                viewModel.saveQuote(quote!!)
                makeSnackBar(view, "Successfully saved Quote!")
            }
        }

        binding.quoteShare.setOnClickListener {
            binding.quoteShare.visibility = View.GONE
            ShareUtils.share(binding.cardHolder, requireContext())
            binding.quoteShare.visibility = View.VISIBLE
        }
    }

    private fun showProgressBar() {
        binding.quoteLoading.visibility = View.VISIBLE
        hideTextViews()
    }

    private fun showTextViews() {
        with(binding) {
            quoteTv.visibility = View.VISIBLE
            authorTv.visibility = View.VISIBLE
            authorTvShareBtnParent.visibility = View.VISIBLE
        }
    }

    private fun hideTextViews() {
        with(binding) {
            quoteTv.visibility = View.GONE
            authorTv.visibility = View.GONE
            authorTvShareBtnParent.visibility = View.GONE
        }
    }

    private fun hideProgressBar() {
        binding.quoteLoading.visibility = View.GONE
    }

    private fun makeSnackBar(view: View, message: String) {
        if ((requireActivity() as QuotesActivity).atHome) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
            if (binding.fab.visibility == View.VISIBLE) {
                binding.fab.visibility = View.INVISIBLE
                lifecycleScope.launch(Dispatchers.Default) {
                    delay(3000)
                    withContext(Dispatchers.Main) {
                        binding.fab.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}