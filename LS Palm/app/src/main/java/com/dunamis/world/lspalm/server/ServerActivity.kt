package com.dunamis.world.lspalm.server

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.dunamis.world.lspalm.R
import com.dunamis.world.lspalm.databinding.ActivityServerBinding
import com.dunamis.world.lspalm.databinding.ExitDialogBinding
import com.dunamis.world.lspalm.databinding.PermissionLayoutBinding
import com.dunamis.world.lspalm.databinding.SchoolListDialogBinding
import com.dunamis.world.lspalm.login.LoginActivity
import com.dunamis.world.lspalm.statics.KotlinStatic
import com.dunamis.world.lspalm.statics.SharedPref
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists

class ServerActivity : AppCompatActivity(), ServerView, ServerDisplayAdapter.ServerTap {

    private lateinit var homeBinding: ActivityServerBinding
    private lateinit var c: Context

    private lateinit var serverDialog: Dialog
    private lateinit var listBinding: SchoolListDialogBinding

    private lateinit var sharedPref: SharedPref
    private lateinit var kotlinStatic: KotlinStatic

    private lateinit var permissionDialog: Dialog
    private lateinit var permissionBinding: PermissionLayoutBinding

    companion object {
        private val REQUEST_PERMISSION = arrayOf(
            PermissionLists.getCameraPermission(),
            PermissionLists.getManageExternalStoragePermission()
        )
    }

    private fun checkPermission() {
        XXPermissions.with(this)
            .permissions(REQUEST_PERMISSION)
            .request { granted, denied ->
                if (denied.isNotEmpty()) {
                    if (XXPermissions.isDoNotAskAgainPermissions(this@ServerActivity, denied)) {
                        // Permanently denied
                        permissionBinding.tvInfo.text =
                            getString(R.string.permission_info_denied)
                        permissionBinding.btnOkay.text = getString(R.string.go_to_settings)

                        permissionBinding.btnOkay.setOnClickListener {
                            permissionDialog.dismiss()
                            XXPermissions.startPermissionActivity(this@ServerActivity, denied)
                        }
                    } else {
                        // Temporarily denied
                        permissionBinding.tvInfo.text = getString(R.string.permission_info)
                        permissionBinding.btnOkay.text = getString(R.string.okay)

                        permissionBinding.btnOkay.setOnClickListener {
                            permissionDialog.dismiss()
                            checkPermission()
                        }
                    }
                    permissionDialog.show()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        homeBinding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clServer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        c = this
        kotlinStatic = KotlinStatic(c)
        homeBinding.tvVersion.text = kotlinStatic.getVersionNo()
        sharedPref = SharedPref(c)

        if (!sharedPref.getBaseUrl().equals("", ignoreCase = true)) {

            gotoLogin()
            return
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

        homeBinding.ivSelectSchool.setOnClickListener {

            homeBinding.ivSelectSchool.isEnabled = false
            serverDialog = Dialog(c)
            val window = serverDialog.window
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            window?.setLayout(
                kotlinStatic.getWidth() / 100 * 90,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            listBinding = SchoolListDialogBinding.inflate(layoutInflater)
            serverDialog.setContentView(listBinding.root)
            serverDialog.setCancelable(false)

            listBinding.lavAnim.visibility = View.VISIBLE
            listBinding.llSchoolList.visibility = View.GONE
            listBinding.llNoInternet.visibility = View.GONE
            listBinding.lavAnim.repeatCount = LottieDrawable.INFINITE

            serverDialog.show()

            ServerPresenter(c as ServerActivity).getSchools()
        }

        permissionDialog = Dialog(c)
        permissionBinding = PermissionLayoutBinding.inflate(layoutInflater)
        permissionDialog.setContentView(permissionBinding.root)
        permissionDialog.setCancelable(false)

        val permissionWindow = permissionDialog.window
        permissionWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        permissionWindow?.setLayout(kotlinStatic.getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        checkPermission()
    }

    override fun success(
        alKeysList: ArrayList<String>,
        alValueList: ArrayList<String>,
        alImageList: ArrayList<String>
    ) {

        homeBinding.ivSelectSchool.isEnabled = true
        val window = serverDialog.window
        window?.setLayout(
            kotlinStatic.getWidth() / 100 * 90,
            kotlinStatic.getHeight() / 100 * 90
        )
        listBinding.llSchoolList.visibility = View.VISIBLE
        listBinding.lavAnim.visibility = View.GONE
        listBinding.llNoInternet.visibility = View.GONE
        serverDialog.setCancelable(true)

        val serverDisplayAdapter = ServerDisplayAdapter(c, alKeysList, alValueList, alImageList)
        listBinding.rvServerList.layoutManager = LinearLayoutManager(c)
        listBinding.rvServerList.adapter = serverDisplayAdapter
    }

    override fun serverTapped(baseUrlRaw: String?,
        baseUrl: String?,
        imgUrl: String?,
        logoImgUrl: String?,
        imgUrlFace: String?) {

        serverDialog.dismiss()
        sharedPref.setBaseUrls(baseUrlRaw, baseUrl, imgUrl, logoImgUrl, imgUrlFace)
        gotoLogin()
    }

    override fun failed() {

        serverDialog.setOnDismissListener {

            homeBinding.ivSelectSchool.isEnabled = true
        }

        val window = serverDialog.window
        window?.setLayout(
            kotlinStatic.getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        listBinding.llNoInternet.visibility = View.VISIBLE
        listBinding.lavAnim.visibility = View.GONE
        listBinding.llSchoolList.visibility = View.GONE
        serverDialog.setCancelable(true)
    }

    private fun gotoLogin() {

        startActivity(Intent(c, LoginActivity::class.java))
        finish()
    }

    private fun showExitDialog() {

        val exitDialog = Dialog(c)
        val exitBinding = ExitDialogBinding.inflate(layoutInflater)
        exitDialog.setContentView(exitBinding.root)
        val exitWindow = exitDialog.window
        exitWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        exitWindow?.setLayout(kotlinStatic.getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)
        exitDialog.setCancelable(true)

        exitBinding.tvNo.setOnClickListener {

            exitDialog.dismiss()
        }
        exitBinding.tvYes.setOnClickListener {

            exitDialog.dismiss()
            finishAffinity()
        }
        exitDialog.show()
    }
}