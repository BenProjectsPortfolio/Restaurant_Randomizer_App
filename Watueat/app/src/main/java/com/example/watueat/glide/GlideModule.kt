package com.example.watueat.glide

import android.content.res.Resources
import android.text.Html
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.watueat.R
import com.bumptech.glide.Glide

// Referenced: FC8 using glide for image binding
object Glide {

    private val glideOptions = RequestOptions().fitCenter().transform(RoundedCorners(20))
    private val width = Resources.getSystem().displayMetrics.widthPixels
    private val height = Resources.getSystem().displayMetrics.heightPixels

    private fun fromHtml(source: String): String {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    fun glideFetch(urlString: String, imageView: ImageView) {
        Glide.with(imageView.context)
            .asBitmap()
            .load(fromHtml(urlString))
            .apply(glideOptions)
            .error(R.color.black)
            .override(width, height)
            .error(
                Glide.with(imageView.context)
                    .asBitmap()
                    .load(fromHtml(urlString))
                    .apply(glideOptions)
                    .error(R.color.black)
                    .override(500, 500)
            )
            .into(imageView)
    }
}
