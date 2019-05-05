package com.source.wally

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.source.wally.utils.*
import kotlinx.android.synthetic.main.selectable_layout.*
import java.io.File
import java.io.FileInputStream
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.source.wally.adapters.GeneralRecyclerAdapter
import com.source.wally.service.WallyWallpaperService


class MainActivity : AppCompatActivity() {

    private var mPath: String = ""
    private var sharedPreferenceHelper: SharedPreferenceHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    override fun onResume() {
        super.onResume()
        setWallpaperActionVisibility()
    }

    private fun initView() {
        sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context = applicationContext)
        setWallpaperActionVisibility()
        setInitialVisibility()
        setOnClickListener()
        setChoosenProfile()
        setColorAdapter()
    }


    /**
     * method to set the action view visibility (set up the wallpaper action)
     */
    private fun setWallpaperActionVisibility() {
        val walInfo = WallpaperManager.getInstance(this).wallpaperInfo
        if (walInfo != null && walInfo.packageName == packageName) {
            clAction.visibility = View.GONE
        } else {
            clAction.visibility = View.VISIBLE
        }
    }

    private fun setChoosenProfile() {
        val wallpaperChosen =
            sharedPreferenceHelper?.getString(SharedPreferenceHelper.SharedPrefKey.WALLPAPER_CHOICE, Constants.COLORS)
        if (wallpaperChosen == Constants.COLORS) {
            setCardClickAction(isSolidColorCard = true)
        } else {
            setCardClickAction(isSolidColorCard = false)
        }
    }

    private fun setInitialVisibility() {
        ivSolidColor.visibility = View.GONE
        ivColorCheck.visibility = View.GONE
        ivImageWallpaper.visibility = View.GONE
        ivImageCheck.visibility = View.GONE
    }

    private fun setOnClickListener() {
        clAction.setOnClickListener {
            openSetWallpaperScreen()
        }

        cardSolidColor.setOnClickListener {
            setCardClickAction(true)
        }

        cardImage.setOnClickListener {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
//            setCardClickAction(false)
        }

        ivOptions.setOnClickListener {
            onOptionsMenuClick()
        }
    }

    private fun onOptionsMenuClick() {
        val popupMenu = PopupMenu(this, ivOptions)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            when (id) {
            }
        }
        popupMenu.show()
    }

    private fun openSetWallpaperScreen() {
        val intent = Intent(
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(this, WallyWallpaperService::class.java)
        )
        startActivity(intent)
    }

//    private fun setImageAdapter() {
//        val bucketList = Utils.getImageBuckets(this)
//        val generalRecyclerAdapter = GeneralRecyclerAdapter(this, bucketList, Constants.IMAGES)
//        rvImages.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
//        rvImages.addItemDecoration(GridSpacingItemDecoration(2, 25, false, 0))
//        rvImages.adapter = generalRecyclerAdapter
//        rvImages.isNestedScrollingEnabled = false
//        rvImages.isFocusableInTouchMode = false
//    }

    private fun setColorAdapter() {
        val currentSelectedTile = sharedPreferenceHelper?.getString(
            SharedPreferenceHelper.SharedPrefKey.SELECTED_COLORS,
            Constants.RANDOM_COLORS
        )
        val colorBucketList = Utils.getColorBuckets(this)
        val generalRecyclerAdapter = GeneralRecyclerAdapter(this, colorBucketList, Constants.COLORS)
        generalRecyclerAdapter.setCurrentSelectedTile(currentSelectedTile!!)
        generalRecyclerAdapter.setOnClickItem(object : GeneralRecyclerAdapter.OnClickItem {
            override fun onClick(bucket: Bucket) {
                setSelectedBucket(bucket)
            }
        })
        rvColors.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        rvColors.addItemDecoration(GridSpacingItemDecoration(2, 25, false, 0))
        rvColors.adapter = generalRecyclerAdapter
        rvColors.isNestedScrollingEnabled = false
        rvColors.isFocusableInTouchMode = false
    }

    private fun setSelectedBucket(bucket: Bucket) {
        val isTimeVariable = bucket.firstImageContainedPath!! == Constants.TIME_BASED_COLORS
        sharedPreferenceHelper?.put(SharedPreferenceHelper.SharedPrefKey.IS_TIME_VARIABLE, isTimeVariable)
        sharedPreferenceHelper?.put(
            SharedPreferenceHelper.SharedPrefKey.SELECTED_COLORS,
            bucket.firstImageContainedPath!!
        )
        val broadcast = Intent(Constants.INTENT_ACTION_TIME_BASED)
        broadcast.putExtra(Constants.IS_TIME_VARIABLE, isTimeVariable)
        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(broadcast)
    }

    private fun setCardClickAction(isSolidColorCard: Boolean) {
        if (sharedPreferenceHelper == null) {
            sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context = applicationContext)
        }
        if (isSolidColorCard) {
            sharedPreferenceHelper?.put(SharedPreferenceHelper.SharedPrefKey.WALLPAPER_CHOICE, Constants.COLORS)
            tvSelectPath.text = resources.getString(R.string.select_the_color_list)
            ivColorCheck.visibility = View.VISIBLE
            ivSolidColor.visibility = View.VISIBLE
            ivImageCheck.visibility = View.INVISIBLE
            ivImageWallpaper.visibility = View.GONE
            rvColors.visibility = View.VISIBLE
            rvImages.visibility = View.GONE
        } else {
            tvSelectPath.text = resources.getString(R.string.select_the_image_folder)
            sharedPreferenceHelper?.put(SharedPreferenceHelper.SharedPrefKey.WALLPAPER_CHOICE, Constants.IMAGES)
            ivImageCheck.visibility = View.VISIBLE
            ivImageWallpaper.visibility = View.VISIBLE
            ivColorCheck.visibility = View.INVISIBLE
            ivSolidColor.visibility = View.GONE
            rvImages.visibility = View.VISIBLE
            rvColors.visibility = View.GONE
        }
    }

    private fun printFiles() {
        if (!mPath.isEmpty()) {
            sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context = applicationContext)
            sharedPreferenceHelper?.put(SharedPreferenceHelper.SharedPrefKey.FOLDER_PATH, mPath)
            val directory = File(mPath)
            val files = directory.listFiles()
            for (i in files.indices) {
                Log.d("Files", "FileName:" + files[i].name)
            }
        } else {
            Toast.makeText(this, "Please select the path", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setImageWallpaper() {
        if (sharedPreferenceHelper == null) {
            sharedPreferenceHelper = SharedPreferenceHelper.getInstance(context = applicationContext)
        }
        val path = sharedPreferenceHelper?.getString(SharedPreferenceHelper.SharedPrefKey.FOLDER_PATH, null)
        if (path != null) {
            val imageFolder = File(path)
            val files = imageFolder.listFiles()
            val imageFile = files.random()
            val fileInputStream = FileInputStream(imageFile)
            val wpManager = WallpaperManager.getInstance(this)
            wpManager.setStream(fileInputStream)
        }
    }
}
