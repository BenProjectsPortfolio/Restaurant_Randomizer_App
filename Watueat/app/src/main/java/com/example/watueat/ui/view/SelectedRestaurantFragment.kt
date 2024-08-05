package com.example.watueat.ui.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.watueat.R
import com.example.watueat.databinding.SelectedRestaurantFragmentBinding
import com.example.watueat.glide.Glide
import com.example.watueat.model.RestaurantViewModel

class SelectedRestaurantFragment: Fragment() {

    private val viewModel: RestaurantViewModel by activityViewModels()
    private var _binding: SelectedRestaurantFragmentBinding? = null
    private val binding get() = _binding!!
    private val args: SelectedRestaurantFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SelectedRestaurantFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenu()

        binding.nameOfRestaurant.text = args.restaurant.name
        Glide.glideFetch(args.restaurant.image, binding.pictureOfRestaurant)
        binding.ratingBar.rating = args.restaurant.rating.toFloat()
        binding.numberOfReviews.text = args.restaurant.reviewCount.toString()
        binding.priceRating.text = args.restaurant.price

        val fullAddress = StringBuilder()
        for (word in args.restaurant.location.fullAddress) {
            fullAddress.append(word)
            fullAddress.append(" ")
        }
        binding.fullAddress.text = fullAddress

        // Referenced: FC7 hyperlink phone number and website
        binding.phoneNumber.text = args.restaurant.displayPhoneNumber
        binding.phoneNumber.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", args.restaurant.phoneNumber, null))
            it.context.startActivity(intent)
        }

        binding.website.text = Html.fromHtml("<a href=\"${args.restaurant.website}\">Go to website</a>", Html.FROM_HTML_MODE_LEGACY)
        binding.website.movementMethod = LinkMovementMethod.getInstance()

        val gettingCategories = StringBuilder()
        for (category in args.restaurant.category) {
            gettingCategories.append(category.title)
            gettingCategories.append(", ")
        }
        val category = gettingCategories.substring(0, gettingCategories.length - 2)
        binding.categories.text = category

        checkForCommentSection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Referenced: FC9 hiding soft input keyboard
    // https://stackoverflow.com/questions/1109022/how-can-i-close-hide-the-android-soft-keyboard-programmatically
    // https://stackoverflow.com/questions/7789514/how-to-get-activitys-windowtoken-without-view
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

    private fun checkForCommentSection() {
        // check if restaurant is favorite and if its already in the database
        if (viewModel.isFavorite(args.restaurant) && args.restaurant.firestoreId.isNotEmpty()) {

            // set layout to evenly appear with other sections
            binding.commentSectionLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

            // create imageView
            val commentImageView = ImageView(context)
            commentImageView.setImageResource(R.drawable.baseline_comment_24)
            val commentIVParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            commentIVParams.marginEnd = 16

            // create editView
            val commentSection = EditText(context)
            commentSection.setSingleLine()
            commentSection.imeOptions = EditorInfo.IME_ACTION_DONE
            val commentSectionParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            commentSectionParams.gravity = Gravity.CENTER_VERTICAL
            // check if comments has default comment or is empty
            if (args.restaurant.comments.isEmpty() || args.restaurant.comments == "\u2764") {
                commentSection.hint = "Comment here."
            } else {
                val comment = args.restaurant.comments
                commentSection.text = Editable.Factory.getInstance().newEditable(comment)
            }

            // create save button
            val saveComment = Button(context)
            saveComment.id = View.generateViewId()
            commentSection.nextFocusForwardId = saveComment.id       // enable tab to button
            val buttonParams = LinearLayout.LayoutParams(100,100)
            buttonParams.marginStart = 16
            buttonParams.gravity = Gravity.CENTER_VERTICAL
            saveComment.setBackgroundResource(R.drawable.baseline_check_circle_24)

            // set all view to layout
            binding.commentSectionLayout.addView(commentImageView, commentIVParams)
            binding.commentSectionLayout.addView(commentSection, commentSectionParams)
            binding.commentSectionLayout.addView(saveComment, buttonParams)

            // when enter in comment box, save button is executed
            commentSection.setOnEditorActionListener { _, actionId, event ->
                if ((event != null
                            && (event.action == KeyEvent.ACTION_DOWN)
                            && (event.keyCode == KeyEvent.KEYCODE_ENTER))
                    || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard()
                    saveComment.callOnClick()
                }
                false
            }

            saveComment.setOnClickListener {
                Toast.makeText(context, "Your comment has been submitted", Toast.LENGTH_SHORT).show()
                args.restaurant.comments = commentSection.text.toString()
                viewModel.updateComments(args.restaurant)
                hideKeyboard()
            }
        }
    }
}
