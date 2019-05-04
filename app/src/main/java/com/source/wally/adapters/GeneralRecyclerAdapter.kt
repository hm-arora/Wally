package com.source.wally.adapters

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.source.wally.Bucket
import com.source.wally.R
import com.source.wally.utils.Constants
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_layout_general.view.*
import java.io.File


class GeneralRecyclerAdapter(
    private val context: Context,
    private var mItemList: ArrayList<Bucket>,
    private val type: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OnClickItem {
        fun onClick(bucket: Bucket)
    }

    private var mOnClickItem: OnClickItem? = null
    private var lastSelectedPosition: Int = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(com.source.wally.R.layout.row_layout_general, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val customHolder = holder as ViewHolder
        customHolder.onBind(position)
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setOnClickItem(onClickItem: OnClickItem) {
        mOnClickItem = onClickItem
    }

    fun setCurrentSelectedTile(currentSelectedTile: String) {
        for (i in 0 until mItemList.size) {
            if (mItemList[i].firstImageContainedPath == currentSelectedTile) {
                lastSelectedPosition = i
                break
            }
        }
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var currentBucket: Bucket? = null
        fun onBind(pos: Int) {
            setOnClickListener(pos)
            itemView.llSelected.visibility = View.GONE
            val isCurrentCardSelected = pos == lastSelectedPosition
            if (isCurrentCardSelected) {
                itemView.llSelected.visibility = View.VISIBLE
            } else {
                itemView.llSelected.visibility = View.GONE
            }
            currentBucket = mItemList[pos]
            var bucketName = currentBucket?.name
            if (bucketName == "0") {
                bucketName = "Internal Storage"
            }
            itemView.tvFolderName.text = bucketName
            // for colors
            if (type == Constants.COLORS) {
                itemView.ivBackground.visibility = View.GONE
                itemView.cardSolidColor.setCardBackgroundColor(currentBucket?.colorCode!!)
            } else {
                val file = File(currentBucket?.firstImageContainedPath)
//            val bitmap = MediaStore.Images.Thumbnails.getThumbnail(
//                context.contentResolver, Uri.fromFile(file).,
//                MediaStore.Images.Thumbnails.MINI_KIND,
//                null as BitmapFactory.Options?
//            )
                if (file.exists()) {
//                val myBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    Picasso.get().load("file://" + file.absolutePath).resize(300, 300).into(itemView.ivBackground)
//                itemView.ivBackground.setImageURI(Uri.fromFile(file))
                }
            }
        }

        private fun setOnClickListener(pos: Int) {
            itemView.setOnClickListener {
                if (pos != lastSelectedPosition) {
                    lastSelectedPosition = pos
                    notifyDataSetChanged()
                    mOnClickItem?.onClick(currentBucket!!)
                }
            }
        }
    }
}