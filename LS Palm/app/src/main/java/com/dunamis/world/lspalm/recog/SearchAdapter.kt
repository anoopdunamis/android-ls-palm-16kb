package com.dunamis.world.lspalm.recog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dunamis.world.lspalm.R
import com.dunamis.world.lspalm.databinding.StudentListLayoutBinding
import com.dunamis.world.lspalm.db.DatabaseHandler
import com.dunamis.world.lspalm.db.Students
import com.dunamis.world.lspalm.statics.SharedPref
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class SearchAdapter(c: Context,
    alStudents: ArrayList<Students>,
    isFromNfc: Boolean = false,
    mIsOpenCamera: Boolean = false,
    databaseHandler: DatabaseHandler) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val c: Context
    private val sharedPref: SharedPref
    private val alStudents: ArrayList<Students>
    private val isFromNfc: Boolean
    private val mIsOpenCamera: Boolean
    private val studentTap: StudentTap
    private val databaseHandler: DatabaseHandler

    init {
        studentTap = c as StudentTap
        this.c = c
        this.sharedPref = SharedPref(c)
        this.alStudents = alStudents
        this.isFromNfc = isFromNfc
        this.mIsOpenCamera = mIsOpenCamera
        this.databaseHandler = databaseHandler
    }

    internal interface StudentTap {

        fun studentTapped(studentData: Students)
        fun deletePalm(studentData: Students)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StudentListLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.b.tvName.text = alStudents[position].childName ?: ""
        holder.b.tvClassSec.text =
            (alStudents[position].childClass ?: "") + " - " + (alStudents[position].childSection
                ?: "") + " (ID# " + (alStudents[position].childRegNo ?: "") + ")"

        if (alStudents[position].childGender.equals("female", ignoreCase = true)) {

            holder.b.llImgBG.setBackgroundResource(R.drawable.bg_pink_female_21)
            Picasso.get()
                .load(sharedPref.getImgUrl().toString() + (alStudents[position].childPhoto ?: ""))
                .networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(R.drawable.girl)
                .error(R.drawable.girl).fit().centerCrop().into(holder.b.ivStudent)
        } else {

            holder.b.llImgBG.setBackgroundResource(R.drawable.bg_blue_male_21)
            Picasso.get()
                .load(sharedPref.getImgUrl().toString() + (alStudents[position].childPhoto ?: ""))
                .networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(R.drawable.boy)
                .error(R.drawable.boy).fit().centerCrop().into(holder.b.ivStudent)
        }

        holder.b.clStudent.setOnClickListener { studentTap.studentTapped(alStudents[position]) }

        if (databaseHandler.getPalmByRegNo(alStudents[position].childRegNo ?: "") == null) {

            holder.b.llPalmDelete.visibility = View.GONE
        } else {

            holder.b.llPalmDelete.visibility = View.VISIBLE
        }
        holder.b.llPalmDelete.setOnClickListener { studentTap.deletePalm(alStudents[position]) }

        if (itemCount == 1 && isFromNfc && mIsOpenCamera) {
            holder.b.clStudent.performClick()
        }
    }

    override fun getItemCount(): Int {
        return alStudents.size
    }

    class ViewHolder(b: StudentListLayoutBinding) : RecyclerView.ViewHolder(b.root) {
        val b: StudentListLayoutBinding

        init {
            this.b = b
        }
    }
}