package work.kcs_labo.typewriter_view

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AnimatorRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat


class TypewriterView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs, 0) {

  private val typedArray: TypedArray = context.theme.obtainStyledAttributes(
    attrs, R.styleable.TypewriterView, 0, 0
  )

  private val interval: Long
  private val delayTime: Long

  @AnimatorRes
  private val animatorRes: Int
  private val _animator: Animator
  private val animators: List<Animator>

  @Dimension
  private val textSize: Float
  private val chars: CharSequence

  @ColorInt
  private val textColorRes: Int

  @FontRes
  private val fontFamilyRes: Int

  init {
    try {
      interval = typedArray.getInt(R.styleable.TypewriterView_interval, 0).toLong()
      delayTime = typedArray.getInt(R.styleable.TypewriterView_delayTime, 0).toLong()
      animatorRes = typedArray.getResourceId(R.styleable.TypewriterView_animator, -1)
      _animator = AnimatorInflater.loadAnimator(context, animatorRes)

      textSize = typedArray.getDimension(R.styleable.TypewriterView_android_textSize, 12f)
      chars = typedArray.getString(R.styleable.TypewriterView_android_text) ?: ""

      textColorRes = typedArray.getColor(R.styleable.TypewriterView_android_textColor, Color.DKGRAY)

      animators = List(chars.length) { _animator.clone() }

      fontFamilyRes = typedArray.getResourceId(R.styleable.TypewriterView_android_fontFamily, -1)
    } finally {
      typedArray.recycle()
    }
  }

  private val mHandler = Handler()
  private var index = 0
  private var charAdder = object : Runnable {
    override fun run() {
      val tv = TextView(context)
      animators[index].setTarget(tv)
      tv.text = chars.subSequence(index, index + 1)
      tv.textSize = textSize
      tv.setTextColor(textColorRes)
      if (fontFamilyRes != -1) {
        ResourcesCompat.getFont(context, fontFamilyRes)?.also {
          tv.typeface = it
        }
      }
      this@TypewriterView.addView(tv)
      animators[index].start()
      if (++index < chars.length) {
        mHandler.postDelayed(this, interval)
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    val tv = TextView(context)
    tv.textSize = textSize
    this.addView(tv)
    Handler().postDelayed({ charAdder.run() }, delayTime)
  }

  private fun convertPx2Dp(context: Context, pixel: Int): Int {
    val metrics = context.resources.displayMetrics
    return (pixel / metrics.density).toInt()
  }
}
