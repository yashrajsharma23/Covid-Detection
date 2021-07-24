package com.matrix.aimlcapstone

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.matrix.aimlcapstone.databinding.ActivityTutorialsBinding
import com.matrix.aimlcapstone.utils.SharedPreferenceHelper
import javax.inject.Inject


class TutorialsActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding:ActivityTutorialsBinding
    private var mResources:IntArray? = null
    private var mHints:ArrayList<String>? = null
    private var ivArrayDotsPager: Array<ImageView>? = null

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.home_color)// SDK21
        binding = DataBindingUtil.setContentView(this,R.layout.activity_tutorials)
        init()
    }

    fun  init(){
        binding.tvSkip.setOnClickListener(this)
        binding.tvNext.setOnClickListener(this)
        sharedPreferenceHelper = SharedPreferenceHelper()

        val sliderTutorial = sharedPreferenceHelper.getFirstTimeTutorial(this, "SLIDER_TUTORIAL")
        /*if (!intent.getBooleanExtra("hasTutorial",false) && sliderTutorial){
            pushActivity()
            return
        }*/



            mResources = intArrayOf(
                R.raw.gif_1,
                R.raw.gif_2
            )
//            binding.viewPager.setRotationY(180F);

        ivArrayDotsPager = arrayOf(binding.img1,binding.img2)

        mHints = arrayListOf<String>("FYI", "Auto detect Covid-19 from X-ray")

        ivArrayDotsPager!![0].setImageResource(R.drawable.selected_dot);

        binding.viewPager.adapter = CustomPagerAdapter(this, mResources!!,sharedPreferenceHelper)

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until ivArrayDotsPager!!.size) {
                    ivArrayDotsPager!![i].setImageResource(R.drawable.default_dot)
                }
                ivArrayDotsPager!![position].setImageResource(R.drawable.selected_dot)
                binding.tvHint.text = mHints!![position]

                if (position == mResources!!.size - 1){
                    binding.tvNext.setTag("Done")
                    binding.tvNext.setText(getString(R.string.done))
                }else{
                    binding.tvNext.setTag("Next")
                    binding.tvNext.setText(getString(R.string.next))
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        });

        binding.tvNext.setTag("Next")

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.tvSkip->{
                pushActivity()
            }
            R.id.tvNext ->{
                val currentItem = binding.viewPager.currentItem
                if (binding.tvNext.tag.equals("Done")) {
                    pushActivity()
                }
                else{
                    binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1)
                }


            }
        }
    }

    private fun pushActivity() {
        sharedPreferenceHelper.setFirstTimeTutorial(this, "SLIDER_TUTORIAL")
        var intent = Intent(this@TutorialsActivity,MainActivity::class.java)// MainWebview::class.java)//

        // intent.putExtras(getIntent().extras!!)
            startActivity(intent)
            finish()

    }

    class CustomPagerAdapter(var context: Context,val mResources: IntArray, var sharedPreferenceHelper:SharedPreferenceHelper) :PagerAdapter(){
        override fun getCount(): Int {
            return mResources.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == (`object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
           val  mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val itemView = mLayoutInflater.inflate(R.layout.pafer_item, null)

            val imageView = itemView.findViewById(R.id.imageView) as ImageView
            /*val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true;
            //        gif = findViewById(R.id.gif);
            val bitmap = decodeSampledBitmapFromResource(context.resources, mResources[position], 800, 800)
            imageView.setImageBitmap(bitmap)*/

            if(position == 0){
                Glide.with(context).asGif()
                    .load(R.raw.gif_1)//"https://media.giphy.com/media/EpTuO1ZrLHzHi/giphy.gif")
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(imageView)
            }else{
                Glide.with(context).asGif()
                    .load(R.raw.gif_2)//"https://media.giphy.com/media/EpTuO1ZrLHzHi/giphy.gif")
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(imageView)
            }

            (container as ViewPager).addView(itemView)

            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                 (container as ViewPager).removeView( `object` as View);

        }

        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }

        fun decodeSampledBitmapFromResource(
            res: Resources,
            resId: Int,
            reqWidth: Int,
            reqHeight: Int
        ): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeResource(res, resId, this)

                // Calculate inSampleSize
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                BitmapFactory.decodeResource(res, resId, this)
            }
        }
    }


}
