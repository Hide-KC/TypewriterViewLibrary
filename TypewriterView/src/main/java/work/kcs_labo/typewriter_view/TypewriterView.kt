package work.kcs_labo.typewriter_view

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.Dimension

class TypewriterView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs, 0) {

  private val typedArray: TypedArray = context.theme.obtainStyledAttributes(
    attrs, R.styleable.TypewriterView, 0, 0
  )

  private val delayMilliTime: Long
  private val angle: Int
  @AnimRes
  private val interpolator: Int
  @Dimension
  private val textSize: Float
  private val chars: CharSequence

  init {
    try {
      delayMilliTime = typedArray.getInteger(R.styleable.TypewriterView_delayMilliTime, 0).toLong()
      angle = typedArray.getInteger(R.styleable.TypewriterView_angle, 0)
      interpolator = typedArray.getResourceId(R.styleable.TypewriterView_android_interpolator, -1)
      textSize = typedArray.getDimension(R.styleable.TypewriterView_android_textSize, 12f)
      chars = typedArray.getString(R.styleable.TypewriterView_android_text) ?: ""
    } finally {
      typedArray.recycle()
    }
  }

  private val mHandler = Handler()
  private var index = 0
  private var charAdder = object : Runnable {
    override fun run() {
      val tv = TextView(context, attrs)
      tv.text = chars.subSequence(index, ++index)
      println(tv.text)
      tv.textSize = textSize
      this@TypewriterView.addView(tv)
      if (index < chars.length) {
        mHandler.postDelayed(this, delayMilliTime)
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    charAdder.run()
  }
}
