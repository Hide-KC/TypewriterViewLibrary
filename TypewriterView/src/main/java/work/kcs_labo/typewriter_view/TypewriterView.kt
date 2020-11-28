package work.kcs_labo.typewriter_view

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
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
import androidx.core.view.get


class TypewriterView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
  LinearLayout(context, attrs, defStyleAttr) {
  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  private val typedArray: TypedArray =
    context.theme.obtainStyledAttributes(attrs, R.styleable.TypewriterView, 0, 0)

  private val interval: Long

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

  private var lastAnimator: Animator? = null

  init {
    try {
      interval = typedArray.getInt(R.styleable.TypewriterView_interval, 0).toLong()
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
      val addedTextView = createTextView().also {
        animators[index].setTarget(it)
        it.text = chars.subSequence(index, index + 1)
        it.tag = "addedTextView"
        this@TypewriterView.addView(it)
      }

      animators[index].addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
        override fun onAnimationRepeat(p0: Animator?) {}

        @SuppressLint("SetTextI18n")
        override fun onAnimationEnd(p0: Animator?) {
          (this@TypewriterView[0] as TextView).also {
            it.text = it.text.toString() + addedTextView.text.toString()
          }
          val removeTarget = this@TypewriterView.findViewWithTag<TextView>("addedTextView")
          this@TypewriterView.removeView(removeTarget)
        }
      })
      animators[index].start()
      if (++index < chars.length) {
        mHandler.postDelayed(this, interval)
      } else {
        onAllAnimationFinished()
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    this.addView(createTextView())
  }

  fun startTypewriter(delayMillis: Long = 0) {
    Handler().postDelayed({ charAdder.run() }, delayMillis)
  }

  fun setAnimationAfterTypeWriterAnimation(animator: Animator?) {
    lastAnimator = animator
  }

  private fun createTextView() = TextView(context).also {
    it.textSize = textSize
    it.setTextColor(textColorRes)
    if (fontFamilyRes != -1) {
      ResourcesCompat.getFont(context, fontFamilyRes)?.also { tf ->
        it.typeface = tf
      }
    }
  }

  private fun onAllAnimationFinished() {
    lastAnimator?.start()
  }
}
