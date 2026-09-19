package com.dunamis.world.lspalm.recog

import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.api.stream.Device
import com.api.stream.Device.DeviceListener
import com.api.stream.Frame
import com.api.stream.ICapturePalmCallback
import com.api.stream.IDevice
import com.api.stream.IOpenCallback
import com.api.stream.StreamType
import com.api.stream.bean.BBox
import com.api.stream.bean.CaptureFrame
import com.api.stream.bean.ExtraFrameInfo
import com.api.stream.bean.ImageInstance
import com.api.stream.enumclass.Hint
import com.api.stream.enumclass.RecognizeMode
import com.api.stream.manager.DtUsbDevice
import com.api.stream.manager.DtUsbManager.DeviceStateListener
import com.api.stream.manager.UsbMapTable
import com.api.stream.veinshine.IVeinshine
import com.dunamis.world.lspalm.R
import com.dunamis.world.lspalm.custom.DtRectRoiView
import com.dunamis.world.lspalm.databinding.ActivityRecognitionBinding
import com.dunamis.world.lspalm.databinding.DbDownloadLayoutBinding
import com.dunamis.world.lspalm.databinding.DeletePalmDialogBinding
import com.dunamis.world.lspalm.databinding.ErrorDialogBinding
import com.dunamis.world.lspalm.databinding.LogoutDialogBinding
import com.dunamis.world.lspalm.databinding.PalmRecognitionDialogBinding
import com.dunamis.world.lspalm.databinding.PalmRegisterDialogBinding
import com.dunamis.world.lspalm.db.Attendance
import com.dunamis.world.lspalm.db.DatabaseHandler
import com.dunamis.world.lspalm.db.Palm
import com.dunamis.world.lspalm.db.Students
import com.dunamis.world.lspalm.login.LoginActivity
import com.dunamis.world.lspalm.statics.KotlinStatic
import com.dunamis.world.lspalm.statics.SharedPref
import com.dunamis.world.lspalm.util.FileUtils
import com.dunamis.world.lspalm.util.IOUtils
import com.dunamis.world.lspalm.util.ResourceUtils
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.palm.common.opengl.GLDisplay
import com.palm.common.opengl.GLFrameSurface
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class RecognitionActivity : AppCompatActivity(), RecognitionView, SearchAdapter.StudentTap,
    NdefReaderTask.ResultTag {

    private val TAG = "RecognitionActivity"
    private var mGLIrView: GLFrameSurface? = null
    private var mGLRgbView: GLFrameSurface? = null

    private var rgbDisPlay: GLDisplay? = null
    private var irDisPlay: GLDisplay? = null

    private var mBtnOpen: Button? = null
    private var mTvDeviceInfo: TextView? = null
    private var mSwitchStartStream: Switch? = null
    private var mSpinnerStreamMode: Spinner? = null

    private val deviceThread: ExecutorService = Executors.newSingleThreadExecutor()
    private val matchPool: ExecutorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @Volatile
    private var mDevice: IDevice? = null
    private var mainHandler: Handler? = null
    private var mAdapterStreamType: ArrayAdapter<StreamType>? = null
    private val mListStreamType: MutableList<StreamType> = ArrayList()
    private var currentStreamType: StreamType = StreamType.INVALID_STREAM_TYPE

    @Volatile
    private var mIsRunning = false
    private var mStreamThread: Thread? = null

    @Volatile
    private var mIsOpenCamera = false
    private var rgbFrameData1: ByteArray? = null
    private var irFrameData1: ByteArray? = null
    private var irFrameExtraInfo: ExtraFrameInfo? = null
    private var irFrameW1 = 0
    private var irFrameH1 = 0
    private var rgbFrameW1 = 0
    private var rgbFrameH1 = 0

    @Volatile
    private var lastStreamPalmState = 0 // 0: None, 1: Too Far, 2: Just Right

    protected var mRgbBitmap: Bitmap? = null
    protected var mIrBitmap: Bitmap? = null

    @Volatile
    private var algoStatus: EnableAlgorithmStatus = EnableAlgorithmStatus.DISABLE

    enum class EnableAlgorithmStatus { DISABLE, ENABLE, INITIALIZING }

    companion object {
        private const val TAG_ENGINE = "PalmEngine"
        private val REQUEST_PERMISSION = arrayOf(PermissionLists.getCameraPermission())
    }

    private val dir: String by lazy {
        getExternalFilesDir(null)?.absolutePath + File.separator + "HeyStar"
    }

    private var mPalmCache: MutableList<Palm> = ArrayList()

    enum class WorkMode { NONE, REGISTER, RECOGNIZE }

    private val mode = RecognizeMode.kBiModal

    @Volatile
    private var mCurrentWorkMode = WorkMode.NONE

    private var regLeftRgb: ByteArray? = null
    private var regLeftIr: ByteArray? = null
    private var regRightRgb: ByteArray? = null
    private var regRightIr: ByteArray? = null

    enum class RegHand { LEFT, RIGHT, NONE }

    private var mRegHandMode = RegHand.NONE

    private var PALM_DELAY_MILLIS: Long = 2000
    private lateinit var kotlinStatic: KotlinStatic

    private var colorSelected = 0
    private lateinit var c: Context
    private lateinit var sharedPref: SharedPref
    private lateinit var databaseHandler: DatabaseHandler

    private lateinit var homeBinding: ActivityRecognitionBinding
    private lateinit var recognitionPresenter: RecognitionPresenter

//    private val markAttendanceRunnable = object : Runnable {
//        override fun run() {
//
//            if (databaseHandler.getAttendanceCount() > 0) {
//
//                recognitionPresenter.markAttendance()
//            }
//            mainHandler?.postDelayed(this, 10000)
//        }
//    }

    private lateinit var tag: String
    private var apiCall: Boolean = false
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var alStudents: ArrayList<Students>
    private lateinit var errorDialog: Dialog
    private lateinit var errorBinding: ErrorDialogBinding

    private lateinit var addPalmDialog: Dialog
    private lateinit var addPalmBinding: PalmRegisterDialogBinding

    private var isPalmAddedToServerDBUpdated = false
    private var isPalmAddedToServer = false
    private var isLeftPalmAdded = false
    private var isRightPalmAdded = false
    private var palmIdentify = 0
    private var isSkipLongClicked = false
    private var isCaptureEligible = false

    private lateinit var dbDownloadDialog: Dialog
    private lateinit var dbDownloadBinding: DbDownloadLayoutBinding

    private lateinit var deletePalmDialog: Dialog
    private lateinit var deletePalmBinding: DeletePalmDialogBinding

    private lateinit var palmRecognitionDialog: Dialog
    private lateinit var palmRecognitionBinding: PalmRecognitionDialogBinding

    private var pageNo = 1
    private var pageRetries = 1
    private var palmPageNo = 1
    private var palmPageRetries = 1
    private var isStudentAPIRunning = false
    private var isPalmAPIRunning = false

    var mNfcAdapter: NfcAdapter? = null
    private val MIME_TEXT_PLAIN = "text/plain"

    private var currentChildId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        homeBinding = ActivityRecognitionBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clRecognition)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        c = this
        kotlinStatic = KotlinStatic(c)
        homeBinding.tvVersion.text = kotlinStatic.getVersionNo()
        sharedPref = SharedPref(c)
        databaseHandler = DatabaseHandler(c)
        recognitionPresenter = RecognitionPresenter(c as RecognitionActivity)

        val filter = IntentFilter()
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED)
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
        filter.addAction(Intent.CATEGORY_DEFAULT)

        if (sharedPref.getScanType().equals("text", ignoreCase = true)) {

            filter.addDataType(MIME_TEXT_PLAIN)
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(c)
        registerReceiver(nfcStateBroadcast, IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED))
        homeBinding.btnNfc.setOnClickListener { kotlinStatic.invalidCard(mNfcAdapter) }

        if (sharedPref.getEntryType()?.trim().equals("in", ignoreCase = true)) {

            homeBinding.tvEntryOrExit.text = resources.getString(R.string.entry)
            homeBinding.tvEntryOrExit.setTextColor(resources.getColor(R.color.green))
        } else {

            homeBinding.tvEntryOrExit.text = resources.getString(R.string.exit)
            homeBinding.tvEntryOrExit.setTextColor(resources.getColor(R.color.red))
        }
        homeBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        homeBinding.tvDBUpdatedDate.text = sharedPref.getStudentDBDownloadedTime()
        setPendingAttendanceCount()

        loadLogoImg()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showLogoutDialog()
            }
        })

        homeBinding.btnBack.setOnClickListener {

            showLogoutDialog()
        }

        tag = "0"
        apiCall = false
        alStudents = ArrayList()
        searchAdapter = SearchAdapter(c, alStudents, false, mIsOpenCamera, databaseHandler)
        homeBinding.rvStudents.layoutManager = LinearLayoutManager(c)

        palmRecognitionDialog = Dialog(c)
        palmRecognitionDialog.setCancelable(false)
        palmRecognitionBinding = PalmRecognitionDialogBinding.inflate(LayoutInflater.from(c))
        palmRecognitionDialog.setContentView(palmRecognitionBinding.root)

        val windowRecognition = palmRecognitionDialog.window
        windowRecognition?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        windowRecognition?.setLayout(kotlinStatic.getWidth() / 100 * 95,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lpRecognition = palmRecognitionDialog.window?.attributes
        lpRecognition?.dimAmount = 0.76f

        palmRecognitionBinding.llDeviceConnect.setOnClickListener { homeBinding.llDeviceConnect.performClick() }

        deletePalmDialog = Dialog(c)
        deletePalmDialog.setCancelable(false)
        deletePalmBinding = DeletePalmDialogBinding.inflate(LayoutInflater.from(c))
        deletePalmDialog.setContentView(deletePalmBinding.root)

        val deleteWindow = deletePalmDialog.window
        deleteWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteWindow?.setLayout(kotlinStatic.getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lpDelete = deletePalmDialog.window?.attributes
        lpDelete?.dimAmount = 0.76f

        errorDialog = Dialog(c)
        errorBinding = ErrorDialogBinding.inflate(layoutInflater)
        val window = errorDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(kotlinStatic.getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)
        errorDialog.setContentView(errorBinding.root)

        errorBinding.tvOkay.setOnClickListener {

            homeBinding.btnSearch.isEnabled = true
            errorDialog.dismiss()
        }

        errorDialog.setOnDismissListener {

            homeBinding.btnSearch.isEnabled = true
        }

        homeBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->

            val radioButton: RadioButton = group.findViewById(checkedId)
            tag = "" + radioButton.tag
            homeBinding.etSearch.hint = radioButton.text
            homeBinding.etSearch.error = null
        }
        homeBinding.rbName.isChecked = true

        homeBinding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.length?.let {
                    if (it > 0) {

                        homeBinding.ivTextClear.visibility = View.VISIBLE
                        homeBinding.ivTextClear.isEnabled = true
                    } else {

                        homeBinding.ivTextClear.visibility = View.GONE
                        homeBinding.ivTextClear.isEnabled = false

                        alStudents.clear()
                        searchAdapter.notifyDataSetChanged()
                        viewHideRV(false)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        homeBinding.ivTextClear.setOnClickListener {

            homeBinding.etSearch.setText("")
            homeBinding.ivTextClear.visibility = View.GONE
            homeBinding.ivTextClear.isEnabled = false
            this.alStudents.clear()
            searchAdapter.notifyDataSetChanged()

            viewHideRV(false)
        }

        homeBinding.btnSearch.setOnClickListener {

//            clearTempImgRegNo()
            val searchKeyword = homeBinding.etSearch.text.toString().trim()
            if (searchKeyword == "") {

                homeBinding.etSearch.error = "This field can't be empty!"
            } else if (tag == "3" && searchKeyword.length < 5) {

                homeBinding.etSearch.error = "Please enter at least 5 digits"
            } else {

//                makeAPICall(tag, searchKeyword)

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                val alStudents: ArrayList<Students> = when (tag) {
                    "0" -> databaseHandler.searchStudentsByName(searchKeyword)
                    "1" -> databaseHandler.searchStudentsByRegNo(searchKeyword)
                    "2" -> databaseHandler.searchStudentsByEmail(searchKeyword)
                    else -> databaseHandler.searchStudentsByPhoneNumber(searchKeyword)
                } as ArrayList<Students>

                if (alStudents.isNotEmpty()) {

                    this.alStudents.clear()
                    this.alStudents = alStudents
                    searchAdapter =
                        SearchAdapter(c, alStudents, false, mIsOpenCamera, databaseHandler)
                    homeBinding.rvStudents.adapter = searchAdapter
                    viewHideRV(true)
                } else {

                    kotlinStatic.goBackDisabledDialog(resources.getString(R.string.no_students_found))
                    viewHideRV(false)
                }
            }
        }

        homeBinding.etSearch.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                homeBinding.btnSearch.performClick()
//                return@OnKeyListener true
            }
            false
        }

        dbDownloadDialog = Dialog(c)
        dbDownloadDialog.setCancelable(false)
        dbDownloadBinding = DbDownloadLayoutBinding.inflate(LayoutInflater.from(c))
        dbDownloadDialog.setContentView(dbDownloadBinding.root)
        val dbWindow = dbDownloadDialog.window
        dbWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dbWindow?.setLayout(kotlinStatic.getWidth() / 100 * 98,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lpDB = dbDownloadDialog.window?.attributes
        lpDB?.dimAmount = 0.76f

        dbDownloadBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        dbDownloadBinding.tvDBUpdatedDate.text = sharedPref.getStudentDBDownloadedTime()

        dbDownloadBinding.tvDBDownload.isEnabled = true
        dbDownloadBinding.tvDBDownload.setOnClickListener {

            if (dbDownloadBinding.tvDBDownload.isEnabled) {

                dbDownloadBinding.tvDBDownload.isEnabled = false
                homeBinding.tvDBDownload.isEnabled = false

                dbDownloadBinding.tvDBCount.text = "DB Total: 0 | Palm Total: 0"
                homeBinding.tvDBCount.text = "DB Total: 0 | Palm Total: 0"
                dbDownloadBinding.tvDBDownload.text =
                    "Downloading... DB: 0 / ${sharedPref.getTotalStudent()} | PALM: 0 / ${sharedPref.getTotalPalm()}"
                homeBinding.tvDBDownload.text =
                    "Downloading... DB: 0 / ${sharedPref.getTotalStudent()} | PALM: 0 / ${sharedPref.getTotalPalm()}"
                dbDownloadBinding.tvDBUpdatedDate.text = "---"
                homeBinding.tvDBUpdatedDate.text = "---"

                isStudentAPIRunning = true
                isPalmAPIRunning = true

                pageNo = 1
                pageRetries = 1
                recognitionPresenter.getAttendanceDetails(pageNo.toString())

                getPalmDetailsAPI()
            }
        }
        homeBinding.tvDBDownload.setOnClickListener {

//            sharedPref.setPalmDBDownloadedTimeGMT("")
            dbDownloadBinding.tvDBDownload.performClick()
        }

        if (databaseHandler.getStudentCount() == 0 || databaseHandler.getPalmCount() == 0 || sharedPref.getDBRefreshMandatory() == true) {

            dbDownloadDialog.show()
            dbDownloadBinding.tvDBDownload.performClick()

            viewHideRV(true)
        } else {

            viewHideRV(false)
        }

        addPalmDialog = Dialog(c)
        addPalmDialog.setCancelable(false)
        addPalmBinding = PalmRegisterDialogBinding.inflate(LayoutInflater.from(c))
        addPalmDialog.setContentView(addPalmBinding.root)

        addPalmDialog.setOnShowListener {
            mGLRgbView = addPalmBinding.surfaceView.mGLRgbView
            mGLIrView = addPalmBinding.surfaceView.mGLIrView

            addPalmBinding.surfaceView.mGLIrView.post {
                mGLRgbView?.setDisplay(mGLRgbView!!.width, mGLRgbView!!.width * 1024 / 720)
                mGLIrView?.setDisplay(mGLIrView!!.width, mGLIrView!!.width * 1024 / 720)
            }
        }

        addPalmDialog.setOnDismissListener {
            mCurrentWorkMode = WorkMode.NONE
            stopCapture()

            mGLRgbView = homeBinding.surfaceView.mGLRgbView
            mGLIrView = homeBinding.surfaceView.mGLIrView

            mGLIrView?.post {
                mGLRgbView?.setDisplay(mGLRgbView!!.width, mGLRgbView!!.width * 1024 / 720)
                mGLIrView?.setDisplay(mGLIrView!!.width, mGLIrView!!.width * 1024 / 720)
            }
        }

        addPalmBinding.lavAnimSuccess.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {

                if (isPalmAddedToServer) {

                    if (addPalmDialog != null) {

                        addPalmDialog.dismiss()
                    }
                } else {

                    if (regLeftRgb != null && regLeftIr != null) {

                        isLeftPalmAdded = true
                        addPalmBinding.ivLeftCaptureStatus.setImageResource(R.drawable.ic_check_circle)
                        addPalmBinding.tvLeftCaptureStatus.text =
                            resources.getString(R.string.added)
                    }
                    if (regRightRgb != null && regRightIr != null) {

                        isRightPalmAdded = true
                        addPalmBinding.ivRightCaptureStatus.setImageResource(R.drawable.ic_check_circle)
                        addPalmBinding.tvRightCaptureStatus.text =
                            resources.getString(R.string.added)
                    }

                    addPalmBinding.llCaptureModule.visibility = View.GONE
                    addPalmBinding.llDoneRetake.visibility = View.VISIBLE
                    addPalmBinding.llOnePalmReason.visibility = View.GONE

                    if (isLeftPalmAdded && isRightPalmAdded) {

                        addPalmBinding.btnNextDone.text = getString(R.string.done)
                    } else {

                        addPalmBinding.btnNextDone.text = getString(R.string.next)
                        tvSkipHideShow(true)
                    }
                }
            }

            override fun onAnimationRepeat(p0: Animator) {

            }

            override fun onAnimationStart(p0: Animator) {

            }
        })

        val palmWindow = addPalmDialog.window
        palmWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        palmWindow?.setLayout(kotlinStatic.getWidth() / 100 * 98,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lp = addPalmDialog.window?.attributes
        lp?.dimAmount = 0.76f

        homeBinding.llNFC.setOnClickListener {

            homeBinding.btnNfc.performClick()
        }
        homeBinding.tvNFC.setOnClickListener {

            homeBinding.btnNfc.performClick()
        }

        homeBinding.btnSearchByPalm.setOnClickListener {

            palmRecognitionBinding.ivDismiss.setOnClickListener {

                nfcStartOrStop(1)
                mCurrentWorkMode = WorkMode.NONE
                stopCapture()
                palmRecognitionDialog.dismiss()
            }
            nfcStartOrStop(0)
            palmRecognitionDialog.show()

            if (checkReady()) {
                homeBinding.mBtnRecognize.performClick()
            }
        }

        loadPalmCache()
        checkPermission()
        initView()

        rgbDisPlay = GLDisplay()
        irDisPlay = GLDisplay()
        mainHandler = Handler(Looper.getMainLooper())

        // Proactively attempt to open the device when starting the activity
        openDevice()
    }

    override fun onResume() {
        super.onResume()

        if (!sharedPref.getRecognitionPageTime()
                .equals(kotlinStatic.fetchDate(), ignoreCase = true)) {

//            gotoHome()
        }
        if (sharedPref.getDBRefreshMandatory() == false) {

            nfcStartOrStop(1)
        }
//        mainHandler?.post(markAttendanceRunnable)
    }

    override fun onPause() {
        super.onPause()
        nfcStartOrStop(0)
//        mainHandler?.removeCallbacks(markAttendanceRunnable)
    }

    private fun viewHideInfoMsg(isTextVisible: Boolean) {

        if (isTextVisible) {

            addPalmBinding.lavAnimSuccess.visibility = View.VISIBLE
        } else {

            addPalmBinding.lavAnimSuccess.visibility = View.GONE
        }
        addPalmBinding.lavAnimFailed.visibility = View.GONE
        addPalmBinding.lavAnimLoading.visibility = View.GONE
    }

    private fun gotoLogin() {

        sharedPref.setLoggedIn(false)
        sharedPref.setEntryType("")
        sharedPref.setRecognitionPageTime("")
        startActivity(Intent(c, LoginActivity::class.java))
        finishAffinity()
    }

    override fun searchSuccess(alStudents: ArrayList<Students>, searchKeyword: String) {

        homeBinding.btnSearch.isEnabled = true
        errorBinding.lavAnim.cancelAnimation()

        this.alStudents.clear()
        this.alStudents = alStudents
        searchAdapter = SearchAdapter(c, this.alStudents, false, mIsOpenCamera, databaseHandler)
        homeBinding.rvStudents.adapter = searchAdapter

        errorDialog.dismiss()
        apiCall = false

        viewHideRV(true)
    }

    override fun searchFailed(msg: String) {

        homeBinding.btnSearch.isEnabled = true
        errorBinding.lavAnim.cancelAnimation()

        errorBinding.tvInfo.text = msg
        errorBinding.lavAnim.visibility = View.GONE
        errorBinding.llError.visibility = View.VISIBLE
        errorDialog.setCancelable(true)
        errorDialog.show()
        apiCall = false
    }

    private fun makeAPICall(tag: String, data: String) {

        if (!apiCall) {

//            isFromNfc = tag == "4"
            apiCall = true
            homeBinding.btnSearch.isEnabled = false

            errorBinding.lavAnim.playAnimation()
            errorDialog.setCancelable(false)
            errorDialog.show()
            errorBinding.lavAnim.visibility = View.VISIBLE
            errorBinding.llError.visibility = View.GONE

            this.alStudents.clear()
            homeBinding.etSearch.error = null
            recognitionPresenter.searchStudent(tag, data)
        }
    }

    override fun deletePalm(studentData: Students) {

        deletePalmBinding.tvInfoMsg.text = ""
        deletePalmBinding.lavAnimLoading.visibility = View.INVISIBLE
        deletePalmBinding.lavAnimSuccess.visibility = View.INVISIBLE
        deletePalmBinding.lavAnimFailed.visibility = View.INVISIBLE

        deletePalmBinding.lavAnimSuccess.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {

                deletePalmDialog.dismiss()
            }

            override fun onAnimationRepeat(p0: Animator) {

            }

            override fun onAnimationStart(p0: Animator) {

            }
        })

        if (studentData.childGender.equals("female", ignoreCase = true)) {

            deletePalmBinding.llImgBG.setBackgroundResource(R.drawable.bg_pink_female_21)
            Picasso.get().load(sharedPref.getImgUrl() + studentData.childPhoto)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.girl)
                .error(R.drawable.girl).fit().centerCrop().into(deletePalmBinding.ivStudent)
        } else {

            deletePalmBinding.llImgBG.setBackgroundResource(R.drawable.bg_blue_male_21)
            Picasso.get().load(sharedPref.getImgUrl() + studentData.childPhoto)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.boy)
                .error(R.drawable.boy).fit().centerCrop().into(deletePalmBinding.ivStudent)
        }

        deletePalmBinding.tvName.text = studentData.childName
        deletePalmBinding.tvClassSec.text =
            studentData.childClass + " - " + studentData.childSection + " (ID# " + studentData.childId + ")"

        deletePalmBinding.tvCancel.isEnabled = true
        deletePalmBinding.tvDelete.isEnabled = true
        deletePalmBinding.tvCancel.setTextColor(resources.getColor(R.color.black))
        deletePalmBinding.tvDelete.setTextColor(resources.getColor(R.color.black))

        deletePalmBinding.tvCancel.setOnClickListener { deletePalmDialog.dismiss() }
        deletePalmBinding.tvDelete.setOnClickListener {

            deletePalmBinding.tvInfoMsg.text = resources.getString(R.string.deleting)
            deletePalmBinding.lavAnimLoading.visibility = View.VISIBLE
            deletePalmBinding.lavAnimSuccess.visibility = View.GONE
            deletePalmBinding.lavAnimFailed.visibility = View.GONE
            deletePalmBinding.lavAnimLoading.playAnimation()

            deletePalmBinding.tvCancel.isEnabled = false
            deletePalmBinding.tvDelete.isEnabled = false
            deletePalmBinding.tvCancel.setTextColor(resources.getColor(R.color.gray))
            deletePalmBinding.tvDelete.setTextColor(resources.getColor(R.color.gray))

            recognitionPresenter.deletePalmAPI(studentData.childId)
        }
        deletePalmDialog.show()
    }

    override fun palmDeleteSuccess() {

        homeBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        searchAdapter.notifyDataSetChanged()

        deletePalmBinding.tvInfoMsg.text = resources.getString(R.string.deleted)
        deletePalmBinding.lavAnimLoading.visibility = View.GONE
        deletePalmBinding.lavAnimSuccess.visibility = View.VISIBLE
        deletePalmBinding.lavAnimFailed.visibility = View.GONE
        deletePalmBinding.lavAnimSuccess.playAnimation()

        isPalmAddedToServerDBUpdated = true
        getPalmDetailsAPI()
    }

    private fun getPalmDetailsAPI() {

        palmPageNo = 1
        palmPageRetries = 1
        recognitionPresenter.getPalmDetails(palmPageNo.toString())
    }

    override fun palmDeleteFailed() {

        deletePalmBinding.tvInfoMsg.text = resources.getString(R.string.delete_error_msg)
        deletePalmBinding.lavAnimLoading.visibility = View.GONE
        deletePalmBinding.lavAnimSuccess.visibility = View.GONE
        deletePalmBinding.lavAnimFailed.visibility = View.VISIBLE
        deletePalmBinding.lavAnimFailed.playAnimation()

        deletePalmBinding.tvCancel.isEnabled = true
        deletePalmBinding.tvDelete.isEnabled = true
        deletePalmBinding.tvCancel.setTextColor(resources.getColor(R.color.black))
        deletePalmBinding.tvDelete.setTextColor(resources.getColor(R.color.black))
    }

    override fun studentTapped(studentData: Students) {

        currentChildId = studentData.childId

        if (studentData.childGender.equals("female", ignoreCase = true)) {

            addPalmBinding.llImgBG.setBackgroundResource(R.drawable.bg_pink_female_21)
            Picasso.get().load(sharedPref.getImgUrl().toString() + studentData.childPhoto)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.girl)
                .error(R.drawable.girl).fit().centerCrop().into(addPalmBinding.ivStudent)
        } else {

            addPalmBinding.llImgBG.setBackgroundResource(R.drawable.bg_blue_male_21)
            Picasso.get().load(sharedPref.getImgUrl().toString() + studentData.childPhoto)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.boy)
                .error(R.drawable.boy).fit().centerCrop().into(addPalmBinding.ivStudent)
        }

        isPalmAddedToServer = false
        isLeftPalmAdded = false
        isRightPalmAdded = false
        addPalmBinding.tvInfoMsg.text = ""
        addPalmBinding.ivLeftRightPalm.setImageBitmap(null)
        addPalmBinding.ivLeftCaptureStatus.setImageResource(R.drawable.ic_outline_circle)
        addPalmBinding.tvLeftCaptureStatus.text = resources.getString(R.string.pending)
        addPalmBinding.ivRightCaptureStatus.setImageResource(R.drawable.ic_outline_circle)
        addPalmBinding.tvRightCaptureStatus.text = resources.getString(R.string.pending)
        mRgbBitmap = null
        mIrBitmap = null
        regLeftRgb = null
        regLeftIr = null
        regRightRgb = null
        regRightIr = null

        tvSkipHideShow(false)
        addPalmBinding.llSurfaceView.visibility = View.VISIBLE
        addPalmBinding.ivLeftRightPalm.visibility = View.GONE
        addPalmBinding.llCaptureModule.visibility = View.VISIBLE
        addPalmBinding.llDoneRetake.visibility = View.GONE
        addPalmBinding.llOnePalmReason.visibility = View.GONE
        addPalmBinding.tvInfoMsg.text = "SHOW LEFT OR RIGHT PALM"
        isCaptureEligible = true
        addPalmBinding.btnNextDone.isEnabled = true
        addPalmBinding.btnRetake.isEnabled = true
        addPalmBinding.btnNextDone.setTextColor(resources.getColor(R.color.black))
        addPalmBinding.btnRetake.setTextColor(resources.getColor(R.color.black))
        viewHideInfoMsg(false)

        addPalmBinding.tvName.text = studentData.childName
        addPalmBinding.tvClassSec.text =
            "${studentData.childClass} - ${studentData.childSection} (ID# ${studentData.childRegNo})"

        regLeftRgb = null.also { regRightIr = it }.also { regRightRgb = it }.also { regLeftIr = it }
        mRegHandMode = RegHand.NONE

        var isSkipClicked = false
        isSkipLongClicked = false
        addPalmBinding.tvSkip.setOnClickListener {

            if (!isSkipClicked) {
                isSkipClicked = true
                addPalmBinding.tvInfoMsg.text = resources.getString(R.string.skip_info)
                object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {

                        isSkipClicked = false
                        if (!isSkipLongClicked) {
                            when {
                                !isLeftPalmAdded && !isRightPalmAdded -> {
                                    addPalmBinding.tvInfoMsg.text = "SHOW LEFT OR RIGHT PALM"
                                }
                                isLeftPalmAdded -> {
                                    addPalmBinding.tvInfoMsg.text = "SHOW RIGHT PALM 🤚"
                                }
                                isRightPalmAdded -> {
                                    addPalmBinding.tvInfoMsg.text = "SHOW LEFT PALM ✋"
                                }
                            }
                        }
                    }
                }.start()
            }
        }

        addPalmBinding.tvSkip.setOnLongClickListener {

            isSkipLongClicked = true
            addPalmBinding.tvInfoMsg.text = resources.getString(R.string.skip_info_reason)
            addPalmBinding.llCaptureModule.visibility = View.GONE
            addPalmBinding.llDoneRetake.visibility = View.GONE
            addPalmBinding.llOnePalmReason.visibility = View.VISIBLE

            addPalmBinding.etOnePalmReason.isEnabled = true
            addPalmBinding.btnDoneOnePalm.isEnabled = true
            addPalmBinding.etOnePalmReason.setTextColor(resources.getColor(R.color.black))
            addPalmBinding.btnDoneOnePalm.setTextColor(resources.getColor(R.color.black))
            addPalmBinding.etOnePalmReason.setText("")
            true
        }

        addPalmBinding.btnDoneOnePalm.setOnClickListener {

            val reason = addPalmBinding.etOnePalmReason.text.toString().trim()
            if (reason.length > 30) {

                addPalmBinding.tvInfoMsg.text = resources.getString(R.string.skip_info_done)
            } else {

                addPalmBinding.tvInfoMsg.text = resources.getString(R.string.skip_info_reason_30)
            }
            addPalmBinding.lavAnimSuccess.visibility = View.GONE
            addPalmBinding.lavAnimFailed.visibility = View.GONE
            addPalmBinding.lavAnimLoading.visibility = View.GONE
        }

        val jsonArrayregLeftRgb = JSONArray()
        val jsonArrayregLeftIr = JSONArray()
        val jsonArrayregRightRgb = JSONArray()
        val jsonArrayregRightIr = JSONArray()
        addPalmBinding.btnDoneOnePalm.setOnLongClickListener {

            val reason = addPalmBinding.etOnePalmReason.text.toString().trim()
            if (reason.length > 30) {

                if (regLeftRgb != null) {
                    for (b in regLeftRgb) {
                        jsonArrayregLeftRgb.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }
                if (regLeftIr != null) {
                    for (b in regLeftIr) {
                        jsonArrayregLeftIr.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }
                if (regRightRgb != null) {
                    for (b in regRightRgb) {
                        jsonArrayregRightRgb.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }
                if (regRightIr != null) {
                    for (b in regRightIr) {
                        jsonArrayregRightIr.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }

                addPalmBinding.btnNextDone.isEnabled = false
                addPalmBinding.btnRetake.isEnabled = false
                addPalmBinding.btnNextDone.setTextColor(resources.getColor(R.color.gray))
                addPalmBinding.btnRetake.setTextColor(resources.getColor(R.color.gray))
                addPalmBinding.lavAnimLoading.visibility = View.VISIBLE
                addPalmBinding.lavAnimSuccess.visibility = View.GONE
                addPalmBinding.lavAnimFailed.visibility = View.GONE
                addPalmBinding.lavAnimLoading.playAnimation()
                addPalmBinding.tvInfoMsg.text = "Adding palm, Please wait."

                addPalmBinding.etOnePalmReason.isEnabled = false
                addPalmBinding.btnDoneOnePalm.isEnabled = false
                addPalmBinding.etOnePalmReason.setTextColor(resources.getColor(R.color.gray))
                addPalmBinding.btnDoneOnePalm.setTextColor(resources.getColor(R.color.gray))
                recognitionPresenter.addPalmAPI(studentData.childId,
                    jsonArrayregLeftRgb,
                    jsonArrayregLeftIr,
                    jsonArrayregRightRgb,
                    jsonArrayregRightIr)
            } else {

                addPalmBinding.tvInfoMsg.text = resources.getString(R.string.skip_info_reason_30)
            }
            true
        }

        addPalmBinding.btnRetake.setOnClickListener {

            addPalmBinding.llSurfaceView.visibility = View.VISIBLE
            addPalmBinding.ivLeftRightPalm.visibility = View.GONE
            if (palmIdentify == 0) {

                addPalmBinding.tvLeftCaptureStatus.text = resources.getString(R.string.pending)
                addPalmBinding.ivLeftCaptureStatus.setImageResource(R.drawable.ic_outline_circle)
                isLeftPalmAdded = false
                regLeftRgb = null
                regLeftIr = null
            } else {

                addPalmBinding.tvRightCaptureStatus.text = resources.getString(R.string.pending)
                addPalmBinding.ivRightCaptureStatus.setImageResource(R.drawable.ic_outline_circle)
                isRightPalmAdded = false
                regRightRgb = null
                regRightIr = null
            }
            when {
                !isLeftPalmAdded && !isRightPalmAdded -> {
                    addPalmBinding.tvInfoMsg.text = "SHOW LEFT OR RIGHT PALM"
                    tvSkipHideShow(false)
                    isSkipLongClicked = false
                }
                isLeftPalmAdded -> {
                    addPalmBinding.tvInfoMsg.text = "SHOW RIGHT PALM 🤚"
                    tvSkipHideShow(true)
                    isSkipLongClicked = false
                }
                isRightPalmAdded -> {
                    addPalmBinding.tvInfoMsg.text = "SHOW LEFT PALM ✋"
                    tvSkipHideShow(true)
                    isSkipLongClicked = false
                }
            }
            addPalmBinding.lavAnimSuccess.visibility = View.GONE
            addPalmBinding.lavAnimFailed.visibility = View.GONE
            addPalmBinding.lavAnimLoading.visibility = View.GONE

            isCaptureEligible = true
            addPalmBinding.llCaptureModule.visibility = View.VISIBLE
            addPalmBinding.llDoneRetake.visibility = View.GONE
            addPalmBinding.llOnePalmReason.visibility = View.GONE
        }

        addPalmBinding.btnNextDone.setOnClickListener {

            if (isLeftPalmAdded && isRightPalmAdded) {

                if (regLeftRgb != null) {
                    for (b in regLeftRgb) {
                        jsonArrayregLeftRgb.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }
                if (regLeftIr != null) {
                    for (b in regLeftIr) {
                        jsonArrayregLeftIr.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }
                if (regRightRgb != null) {
                    for (b in regRightRgb) {
                        jsonArrayregRightRgb.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }
                if (regRightIr != null) {
                    for (b in regRightIr) {
                        jsonArrayregRightIr.put(b.toInt() and 0xFF) // keeps unsigned value
                    }
                }

                addPalmBinding.btnNextDone.isEnabled = false
                addPalmBinding.btnRetake.isEnabled = false
                addPalmBinding.btnNextDone.setTextColor(resources.getColor(R.color.gray))
                addPalmBinding.btnRetake.setTextColor(resources.getColor(R.color.gray))
                addPalmBinding.lavAnimLoading.visibility = View.VISIBLE
                addPalmBinding.lavAnimSuccess.visibility = View.GONE
                addPalmBinding.lavAnimFailed.visibility = View.GONE
                addPalmBinding.lavAnimLoading.playAnimation()
                addPalmBinding.tvInfoMsg.text = "Adding palm, Please wait."
                recognitionPresenter.addPalmAPI(studentData.childId,
                    jsonArrayregLeftRgb,
                    jsonArrayregLeftIr,
                    jsonArrayregRightRgb,
                    jsonArrayregRightIr)
            } else {

                addPalmBinding.llSurfaceView.visibility = View.VISIBLE
                addPalmBinding.ivLeftRightPalm.visibility = View.GONE
                when {
                    !isLeftPalmAdded && !isRightPalmAdded -> {
                        addPalmBinding.tvInfoMsg.text = "SHOW LEFT OR RIGHT PALM"
                    }
                    isLeftPalmAdded -> {
                        addPalmBinding.tvInfoMsg.text = "SHOW RIGHT PALM 🤚"
                    }
                    isRightPalmAdded -> {
                        addPalmBinding.tvInfoMsg.text = "SHOW LEFT PALM ✋"
                    }
                }
                addPalmBinding.lavAnimSuccess.visibility = View.GONE
                addPalmBinding.lavAnimFailed.visibility = View.GONE
                addPalmBinding.lavAnimLoading.visibility = View.GONE

                isCaptureEligible = true
                addPalmBinding.llCaptureModule.visibility = View.VISIBLE
                addPalmBinding.llDoneRetake.visibility = View.GONE
                addPalmBinding.llOnePalmReason.visibility = View.GONE
            }
        }

        addPalmBinding.llCapture.setOnClickListener {

            tvSkipHideShow(false)
            addPalmBinding.lavAnimLoading.visibility = View.GONE
            addPalmBinding.lavAnimSuccess.visibility = View.GONE
            addPalmBinding.lavAnimFailed.visibility = View.GONE
            if (mIsOpenCamera) {

//                addPalmBinding.llSurfaceView.visibility = View.VISIBLE
//                addPalmBinding.ivLeftRightPalm.visibility = View.GONE
                mRegHandMode = RegHand.LEFT
                mCurrentWorkMode = WorkMode.REGISTER

                captureOnce()
            } else {

//                addPalmBinding.tvInfoMsg.text = resources.getString(R.string.device_disconnected)
                homeBinding.llDeviceConnect.performClick()
            }
        }

        addPalmBinding.llDeviceConnect.setOnClickListener { homeBinding.llDeviceConnect.performClick() }
        addPalmBinding.ivDismiss.setOnClickListener { addPalmDialog.dismiss() }
        addPalmDialog.show()
    }

    override fun palmSuccess() {

        isPalmAddedToServer = true

        isPalmAddedToServerDBUpdated = true
        getPalmDetailsAPI()
//        loadPalmCache()

        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                addPalmBinding.lavAnimLoading.visibility = View.GONE
                addPalmBinding.lavAnimSuccess.visibility = View.VISIBLE
                addPalmBinding.lavAnimFailed.visibility = View.GONE
                addPalmBinding.lavAnimSuccess.playAnimation()
                addPalmBinding.tvInfoMsg.text = "The palm has been added successfully"
            }
        }.start()
    }

    override fun palmFailed() {

        object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {

                addPalmBinding.btnNextDone.isEnabled = true
                addPalmBinding.btnRetake.isEnabled = true
                addPalmBinding.btnNextDone.setTextColor(resources.getColor(R.color.black))
                addPalmBinding.btnRetake.setTextColor(resources.getColor(R.color.black))
                addPalmBinding.lavAnimLoading.visibility = View.GONE
                addPalmBinding.lavAnimSuccess.visibility = View.GONE
                addPalmBinding.lavAnimFailed.visibility = View.VISIBLE
                addPalmBinding.lavAnimFailed.playAnimation()
                addPalmBinding.tvInfoMsg.text = "Palm could not be added. Please try again later."

                addPalmBinding.etOnePalmReason.isEnabled = true
                addPalmBinding.btnDoneOnePalm.isEnabled = true
                addPalmBinding.etOnePalmReason.setTextColor(resources.getColor(R.color.black))
                addPalmBinding.btnDoneOnePalm.setTextColor(resources.getColor(R.color.black))
            }
        }.start()
    }

    private fun startRegisterFlow(frame: CaptureFrame) {
        matchPool.execute(Runnable {
            val rgbIns = if (frame.rgbData != null) ImageInstance(frame.rgbCols,
                frame.rgbRows,
                frame.rgbData,
                ImageInstance.ImageFormat.IMG_3C8BIT) else null
            val irIns = ImageInstance(frame.irCols,
                frame.irRows,
                frame.irData,
                ImageInstance.ImageFormat.IMG_1C8BIT)
            val ext = (mDevice as IVeinshine).extractPalmFeaturesFromImg(rgbIns, irIns)

            if (ext == null || ext.result != 0) {
                showToast("Enrollment quality low. Try again.")
//                return@execute
            }

            // Hand Validation
            val detectedType = ext.palmType // 0: Left, 1: Right
//            if (mRegHandMode == RegHand.LEFT && detectedType != 0) {
//                runOnUiThread {
//                    addPalmBinding.tvInfoMsg.text = "Right hand detected! Please use LEFT hand."
//                }
//                return@Runnable
//            } else if (mRegHandMode == RegHand.RIGHT && detectedType != 1) {
//                runOnUiThread {
//                    addPalmBinding.tvInfoMsg.text = "Left hand detected! Please use RIGHT hand."
//                }
//                return@Runnable
//            }

            if (isLeftPalmAdded && detectedType == 0) {
                runOnUiThread {
                    addPalmBinding.tvInfoMsg.text = "Left hand detected! Please use RIGHT hand."
                }
                return@Runnable
            }

            if (isRightPalmAdded && detectedType == 1) {
                runOnUiThread {
                    addPalmBinding.tvInfoMsg.text = "Right hand detected! Please use LEFT hand."
                }
                return@Runnable
            }

            // Check if already registered (Duplicate check)
            for (candidate in mPalmCache) {
                val compRgb: ByteArray? =
                    if (detectedType == 0) candidate.palmDataRgbLeft else candidate.palmDataRgbRight
                val compIr: ByteArray? =
                    if (detectedType == 0) candidate.palmDataIrLeft else candidate.palmDataIrRight
                if (compIr == null || compIr.isEmpty()) continue

                val res = (mDevice as IVeinshine).compareFeatureScore(compRgb,
                    compIr,
                    ext.rgbFeature,
                    ext.irFeature, mode)
                if (res.irScore > 0.75) {
                    if (candidate.childId != currentChildId) {
                        val studentMatched = databaseHandler.getStudentByRegNo(candidate.childRegNo)
                        runOnUiThread {

                            kotlinStatic.duplicatePalmDialog(studentMatched, sharedPref.getImgUrl())
                            kotlinStatic.playPalmFailure()
                        }
                        return@Runnable
                    }
                }
            }
            if (detectedType == 0) {

                palmIdentify = 0
                regLeftRgb = ext.rgbFeature
                regLeftIr = ext.irFeature
                runOnUiThread {
                    if (mRgbBitmap != null) {

                        isCaptureEligible = false
                        addPalmBinding.ivLeftRightPalm.setImageBitmap(mRgbBitmap)

                        addPalmBinding.llSurfaceView.visibility = View.GONE
                        addPalmBinding.ivLeftRightPalm.visibility = View.VISIBLE

                        viewHideInfoMsg(true)
                        addPalmBinding.lavAnimSuccess.playAnimation()
                        addPalmBinding.tvInfoMsg.text = "LEFT PALM CAPTURED"
                        tvSkipHideShow(false)
                        isSkipLongClicked = false
                    }
                }
            } else {

                palmIdentify = 1
                regRightRgb = ext.rgbFeature
                regRightIr = ext.irFeature
                runOnUiThread {
                    if (mRgbBitmap != null) {

                        isCaptureEligible = false
                        addPalmBinding.ivLeftRightPalm.setImageBitmap(mRgbBitmap)

                        addPalmBinding.llSurfaceView.visibility = View.GONE
                        addPalmBinding.ivLeftRightPalm.visibility = View.VISIBLE

                        viewHideInfoMsg(true)
                        addPalmBinding.lavAnimSuccess.playAnimation()
                        addPalmBinding.tvInfoMsg.text = "RIGHT PALM CAPTURED"
                        tvSkipHideShow(false)
                        isSkipLongClicked = false
                    }
                }
            }

//            if (mRegHandMode == RegHand.LEFT) {
//                regLeftRgb = ext.rgbFeature
//                regLeftIr = ext.irFeature
//                runOnUiThread(Runnable {
////                    mRegBinding.tvRegStatus.setText("Status: Left Hand Captured")
////                    if (mRgbBitmap != null) mRegBinding.ivPalmRgb.setImageBitmap(mRgbBitmap)
//                    if (mRgbBitmap != null) {
//                        if (mRegHandMode == RegHand.LEFT && detectedType == 0) {
//
//                            addPalmBinding.ivLeftRightPalm.setImageBitmap(mRgbBitmap)
//
//                            addPalmBinding.llSurfaceView.visibility = View.GONE
//                            addPalmBinding.ivLeftRightPalm.visibility = View.VISIBLE
//
//                            viewHideInfoMsg(true)
//                            addPalmBinding.lavAnimSuccess.playAnimation()
//                            addPalmBinding.tvInfoMsg.text = "LEFT PALM CAPTURED"
//                        }
//                    }
////                    if (mIrBitmap != null) mRegBinding.ivPalmIr.setImageBitmap(mIrBitmap)
//                })
//            } else if (mRegHandMode == RegHand.RIGHT) {
//                regRightRgb = ext.rgbFeature
//                regRightIr = ext.irFeature
//                runOnUiThread(Runnable {
////                    mRegBinding.tvRegStatus.setText("Status: Right Hand Captured")
//                    if (mRgbBitmap != null) {
//
//                        if (mRegHandMode == RegHand.RIGHT && detectedType == 1) {
//
//                            addPalmBinding.ivLeftRightPalm.setImageBitmap(mRgbBitmap)
//
//                            addPalmBinding.llSurfaceView.visibility = View.GONE
//                            addPalmBinding.ivLeftRightPalm.visibility = View.VISIBLE
//
//                            viewHideInfoMsg(true)
//                            addPalmBinding.lavAnimSuccess.playAnimation()
//                            addPalmBinding.tvInfoMsg.text = "RIGHT PALM CAPTURED"
//                        }
//                    }
////                    if (mIrBitmap != null) mRegBinding.ivPalmIr.setImageBitmap(mIrBitmap)
//                })
//            }
        })
    }

    private fun loadPalmCache() {
        matchPool.execute {
            mPalmCache = databaseHandler.getPalms() as MutableList<Palm>
        }
    }

    private fun checkPermission() {
        XXPermissions.with(this).permissions(REQUEST_PERMISSION).request { granted, denied ->
            if (denied.isEmpty()) {
                IOUtils.createFolder(dir)
                IOUtils.createFolder(dir + File.separator + "models")
                copyAssetsFile()
            } else {
                if (XXPermissions.isDoNotAskAgainPermissions(this@RecognitionActivity, denied)) {
                    XXPermissions.startPermissionActivity(this@RecognitionActivity, denied)
                } else {
                    showToast("Permissions required")
                    finish()
                }
            }
        }
    }

    private fun initView() {

        // Access views correctly from bindings
        mGLRgbView = homeBinding.surfaceView.mGLRgbView
        mGLIrView = homeBinding.surfaceView.mGLIrView

        mGLIrView?.post {
            mGLRgbView?.setDisplay(mGLRgbView!!.width, mGLRgbView!!.width * 1024 / 720)
            mGLIrView?.setDisplay(mGLIrView!!.width, mGLIrView!!.width * 1024 / 720)
        }

        mAdapterStreamType = ArrayAdapter(c,
            android.R.layout.simple_spinner_item,
            mListStreamType) as ArrayAdapter<StreamType>?

        mBtnOpen = homeBinding.openViewGroup.mBtnOpen
        mTvDeviceInfo = homeBinding.openViewGroup.mTvDeviceInfo
        mSwitchStartStream = homeBinding.openViewGroup.mSwitchStartStream
        mSpinnerStreamMode = homeBinding.openViewGroup.mSpinnerStreamMode

        mSpinnerStreamMode?.adapter = mAdapterStreamType
        initListener()
    }

    private fun copyAssetsFile() {
        val model: String = dir + File.separator + "models/palm_models_1.3.5.bin"
        if (FileUtils.isFileExists(model)) return

//        showProgressDialog("Initializing models...")
        Executors.newSingleThreadExecutor().execute {
            try {
                ResourceUtils.copyFileFromAssets("models", dir + File.separator + "models")
            } catch (e: Exception) {
            }
//            runOnUiThread(Runnable { this.dismissProgressDialog() })
        }
    }

    private fun initListener() {

        homeBinding.llImgBG.setOnClickListener { openDevice() }
        homeBinding.llDeviceConnect.setOnClickListener { openDevice() }
        mBtnOpen?.setOnClickListener { openDevice() }
        mSwitchStartStream?.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                if (!mIsOpenCamera) {
//                    showToast("Connect sensor first")
                    mSwitchStartStream?.isChecked = false
                    return@setOnCheckedChangeListener
                }
                if (mSpinnerStreamMode != null) mSpinnerStreamMode?.isEnabled = false
                startStream()
            } else {
                if (mSpinnerStreamMode != null) mSpinnerStreamMode?.isEnabled = true
                mIsRunning = false
                mainHandler?.postDelayed({ this.clearFrame() }, 200)
            }
        }
        mSpinnerStreamMode?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                currentStreamType = mListStreamType[pos]
            }

            override fun onNothingSelected(p: AdapterView<*>?) {
            }
        }
        homeBinding.mBtnEnable.setOnClickListener { enableDimPalm() }
        homeBinding.mBtnRegister.setOnClickListener {
            if (checkReady()) {
//                showRegisterMainDialog()
            }
        }
        homeBinding.mBtnRecognize.setOnClickListener {
            if (checkReady()) {
                mCurrentWorkMode = WorkMode.RECOGNIZE
//                showToast("VERIFYING...")
                mainHandler?.post {

                    homeBinding.ivStudent.visibility = View.GONE
                    homeBinding.lavAnimInit.visibility = View.GONE
                    homeBinding.llLavBorder.visibility = View.VISIBLE
//                    homeBinding.lavAnimRecog.visibility = View.VISIBLE
                    homeBinding.lavAnimInit.cancelAnimation()
                    homeBinding.lavAnimRecog.playAnimation()

                    palmRecognitionBinding.ivStudent.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.visibility = View.GONE
                    palmRecognitionBinding.llLavBorder.visibility = View.VISIBLE
                    palmRecognitionBinding.lavAnimInit.cancelAnimation()
                    palmRecognitionBinding.lavAnimRecog.playAnimation()

                    colorSelected = 1
                    homeBinding.llLavBorder.setBackgroundResource(R.drawable.bg_dodger_blue_55)
                    homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_black_55)

                    palmRecognitionBinding.llLavBorder.setBackgroundResource(R.drawable.bg_dodger_blue_55)
                    palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_black_55)
//                    homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)

                    homeBinding.tvName.text = resources.getString(R.string.scanning_palm)
                    homeBinding.tvClassSec.text = resources.getString(R.string.scanning_palm_info)

                    palmRecognitionBinding.tvName.text = resources.getString(R.string.scanning_palm)
                    palmRecognitionBinding.tvClassSec.text =
                        resources.getString(R.string.scanning_palm_info)

                    homeBinding.lavAnimSuccess.visibility = View.GONE
                    homeBinding.lavAnimFail.visibility = View.GONE
                }
                captureOnce()
            }
        }
        homeBinding.mBtnStopCapture.setOnClickListener { stopCapture() }
    }

    private fun checkReady(): Boolean {
        if (!mIsOpenCamera) {
//            showToast("Open sensor first")
            return false
        }
        if (algoStatus != EnableAlgorithmStatus.ENABLE) {
//            showToast("Initialize engine first")
            return false
        }
        return true
    }

    private fun enableDimPalm() {

        if (algoStatus == EnableAlgorithmStatus.ENABLE) return
        val modelPath = dir + File.separator + "models" + File.separator
        matchPool.execute {
            if (mDevice != null) {
                algoStatus = EnableAlgorithmStatus.INITIALIZING
                if ((mDevice as IVeinshine).enableDimPalm(modelPath, mode) == 0) {
                    algoStatus = EnableAlgorithmStatus.ENABLE
//                    showToast("Algorithm Ready")

                    //new
                    mainHandler?.post {

                        if (mSwitchStartStream != null) mSwitchStartStream?.isChecked = true

                        mainHandler?.postDelayed({
                            homeBinding.mBtnRecognize.performClick()
                        }, 1000)
                    }
                    //new
                } else {
                    algoStatus = EnableAlgorithmStatus.DISABLE
//                    showToast("Init Failed")
                }
            }
        }
//        AlertDialog.Builder(this).setTitle("Biometric Setup").setView(input)
//            .setPositiveButton("Init"
//            ) { d: DialogInterface?, w: Int ->
//                matchPool.execute {
//                    if (mDevice != null) {
//                        algoStatus = EnableAlgorithmStatus.INITIALIZING
//                        if ((mDevice as IVeinshine).enableDimPalm(input.text
//                                .toString()) == 0) {
//                            algoStatus = EnableAlgorithmStatus.ENABLE
//                            showToast("Algorithm Ready")
//                        } else {
//                            algoStatus = EnableAlgorithmStatus.DISABLE
//                            showToast("Init Failed")
//                        }
//                    }
//                }
//            }.show()
    }

    private val recognizeRunnable = Runnable {
        if (!isFinishing && !isDestroyed && algoStatus == EnableAlgorithmStatus.ENABLE && mCurrentWorkMode == WorkMode.RECOGNIZE) {
            homeBinding.mBtnRecognize.performClick()
        }
    }

    private fun restartRecognitionWithDelay(delay: Long) {
        mainHandler?.removeCallbacks(recognizeRunnable)
        mainHandler?.postDelayed(recognizeRunnable, delay)
    }

    private fun performRecognition(frame: CaptureFrame) {

        val device = mDevice as? IVeinshine
        if (device == null || mPalmCache.isEmpty()) {
            if (mPalmCache.isEmpty()) showToast("Database empty")
            restartRecognitionWithDelay(PALM_DELAY_MILLIS)
            return
        }
        matchPool.execute {
            val rgbIns = if (frame.rgbData != null) ImageInstance(frame.rgbCols,
                frame.rgbRows,
                frame.rgbData,
                ImageInstance.ImageFormat.IMG_3C8BIT) else null
            val irIns = ImageInstance(frame.irCols,
                frame.irRows,
                frame.irData,
                ImageInstance.ImageFormat.IMG_1C8BIT)

            val live = device.extractPalmFeaturesFromImg(rgbIns, irIns)
            if (live == null || live.result != 0) {
                showToast("Capture quality low. Keep steady.")
                restartRecognitionWithDelay(PALM_DELAY_MILLIS)
                return@execute
            }

            var bestMatchCandidate: Palm? = null
            var maxScore = 0f
            val liveHandType = live.palmType // 0 for Left, 1 for Right

            for (candidate in mPalmCache) {
                val candidateRgb: ByteArray? =
                    if (liveHandType == 0) candidate.palmDataRgbLeft else candidate.palmDataRgbRight
                val candidateIr: ByteArray? =
                    if (liveHandType == 0) candidate.palmDataIrLeft else candidate.palmDataIrRight

                if (candidateIr == null || candidateIr.isEmpty()) continue

                val res = device.compareFeatureScore(candidateRgb,
                    candidateIr,
                    live.rgbFeature,
                    live.irFeature, mode)

                if (res.irScore > 0.75 && res.irScore > maxScore) {
                    maxScore = res.irScore
                    bestMatchCandidate = candidate
                }
            }

            if (kotlinStatic.isAutoTimeEnabled()) {

                if (bestMatchCandidate != null) {
                    val studentMatched =
                        databaseHandler.getStudentByRegNo(bestMatchCandidate.childRegNo)
                    if (studentMatched != null) {
//                    showToast("MATCH FOUND: " + studentMatched.childName)

                        val attendance = Attendance(id = 0,
                            latitude = "0.0",
                            longitude = "0.0",
                            dataType = "p",
                            busId = sharedPref.getBusId() ?: "",
                            timeStamp = kotlinStatic.gmtTimeRightNow(),
                            childId = studentMatched.childId,
                            pickUpDropOff = sharedPref.getEntryType(),
                            childStatus = "1",
                            childNfcId = studentMatched.childNfcId,
                            childForgotCard = "0",
                            childRegNo = studentMatched.childRegNo,
                            tripNoPickUp = studentMatched.tripNoPickUp,
                            tripNoDropOff = studentMatched.tripNoDropOff,
                            childName = studentMatched.childName,
                            childOrEmp = studentMatched.childOrEmp,
                            childAuthId = "0",
                            childClassAuth = studentMatched.childClass,
                            childSectionAuth = studentMatched.childSection)

                        val list = ArrayList<Attendance>()
                        list.add(attendance)
//                        databaseHandler.addBatchAttendance(list)

                        mainHandler?.post {
                            if (isFinishing || isDestroyed) return@post
                            setPendingAttendanceCount()
                            homeBinding.tvName.text = studentMatched.childName
                            homeBinding.tvClassSec.text =
                                "${studentMatched.childClass} - ${studentMatched.childSection} (ID# ${studentMatched.childRegNo})"

                            palmRecognitionBinding.tvName.text = studentMatched.childName
                            palmRecognitionBinding.tvClassSec.text =
                                "${studentMatched.childClass} - ${studentMatched.childSection} (ID# ${studentMatched.childRegNo})"

                            homeBinding.lavAnimSuccess.visibility = View.VISIBLE
                            homeBinding.lavAnimSuccess.playAnimation()
                            homeBinding.lavAnimFail.visibility = View.GONE

                            homeBinding.ivStudent.visibility = View.VISIBLE
                            homeBinding.lavAnimInit.visibility = View.GONE
                            homeBinding.llLavBorder.visibility = View.GONE
//                        homeBinding.lavAnimRecog.visibility = View.GONE
                            homeBinding.lavAnimInit.cancelAnimation()
                            homeBinding.lavAnimRecog.cancelAnimation()

                            palmRecognitionBinding.ivStudent.visibility = View.VISIBLE
                            palmRecognitionBinding.lavAnimInit.visibility = View.GONE
                            palmRecognitionBinding.llLavBorder.visibility = View.GONE
                            palmRecognitionBinding.lavAnimInit.cancelAnimation()
                            palmRecognitionBinding.lavAnimRecog.cancelAnimation()

                            mainHandler?.postDelayed({
                                if (isFinishing || isDestroyed) return@postDelayed
                                nfcStartOrStop(1)
                                mCurrentWorkMode = WorkMode.NONE
                                stopCapture()
                                palmRecognitionDialog.dismiss()

                                homeBinding.etSearch.setText(studentMatched.childName)
                                this.alStudents.clear()
                                this.alStudents =
                                    databaseHandler.searchStudentsByRegNo(studentMatched.childRegNo) as ArrayList<Students>
                                searchAdapter =
                                    SearchAdapter(c,
                                        alStudents,
                                        false,
                                        mIsOpenCamera,
                                        databaseHandler)
                                homeBinding.rvStudents.adapter = searchAdapter
                                viewHideRV(true)
                            }, 2000)

                            if (studentMatched.childGender.equals("Female", ignoreCase = true)) {

                                colorSelected = 4
                                homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_pink_female_55)
                                palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_pink_female_55)
                                Picasso.get()
                                    .load(sharedPref.getImgUrl() + studentMatched.childPhoto)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .placeholder(R.drawable.girl).error(R.drawable.girl).fit()
                                    .centerCrop().into(homeBinding.ivStudent)
                                Picasso.get()
                                    .load(sharedPref.getImgUrl() + studentMatched.childPhoto)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .placeholder(R.drawable.girl).error(R.drawable.girl).fit()
                                    .centerCrop().into(palmRecognitionBinding.ivStudent)
                            } else {

                                colorSelected = 3
                                homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_blue_male_55)
                                palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_blue_male_55)
                                Picasso.get()
                                    .load(sharedPref.getImgUrl() + studentMatched.childPhoto)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.boy)
                                    .error(R.drawable.boy).fit().centerCrop()
                                    .into(homeBinding.ivStudent)
                                Picasso.get()
                                    .load(sharedPref.getImgUrl() + studentMatched.childPhoto)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.boy)
                                    .error(R.drawable.boy).fit().centerCrop()
                                    .into(palmRecognitionBinding.ivStudent)
                            }
                        }
                        kotlinStatic.playPalmSuccess()
                    } else {

//                    showToast(resources.getString(R.string.not_recognized))
                        mainHandler?.post {
                            if (isFinishing || isDestroyed) return@post
                            setNameImageUnknown()
                        }
                        kotlinStatic.playPalmFailure()
                    }
                } else {

//                showToast(resources.getString(R.string.not_recognized))
                    mainHandler?.post {
                        if (isFinishing || isDestroyed) return@post
                        setNameImageUnknown()
                    }
                    kotlinStatic.playPalmFailure()
                }
            } else {

                mainHandler?.post {
                    if (isFinishing || isDestroyed) return@post
                    kotlinStatic.showAutoTimeZoneDialog()
                }
            }

            clearFrame()
            restartRecognitionWithDelay(PALM_DELAY_MILLIS)
        }
    }

    private fun setNameImageUnknown() {
        if (isFinishing || isDestroyed) return
        colorSelected = 0
        homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)
        homeBinding.ivStudent.setImageResource(R.drawable.question_mark)
        homeBinding.tvName.text = resources.getString(R.string.not_recognized)
        homeBinding.tvClassSec.text = ""

        palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)
        palmRecognitionBinding.ivStudent.setImageResource(R.drawable.question_mark)
        palmRecognitionBinding.tvName.text = resources.getString(R.string.not_recognized)
        palmRecognitionBinding.tvClassSec.text = ""

        homeBinding.lavAnimSuccess.visibility = View.GONE
        homeBinding.lavAnimFail.visibility = View.VISIBLE
        homeBinding.lavAnimFail.playAnimation()

        homeBinding.ivStudent.visibility = View.VISIBLE
        homeBinding.lavAnimInit.visibility = View.GONE
        homeBinding.llLavBorder.visibility = View.GONE
//        homeBinding.lavAnimRecog.visibility = View.GONE
        homeBinding.lavAnimInit.cancelAnimation()
        homeBinding.lavAnimRecog.cancelAnimation()

        palmRecognitionBinding.ivStudent.visibility = View.VISIBLE
        palmRecognitionBinding.lavAnimInit.visibility = View.GONE
        palmRecognitionBinding.llLavBorder.visibility = View.GONE
        palmRecognitionBinding.lavAnimInit.cancelAnimation()
        palmRecognitionBinding.lavAnimRecog.cancelAnimation()
    }

    @Volatile
    private var isPalmDetected = false

    private val mCapturePalmCallback: ICapturePalmCallback = object : ICapturePalmCallback {
        override fun onCaptureFrame(frame: CaptureFrame?) {
            isPalmDetected = false
            mainHandler?.post {
                if (::addPalmBinding.isInitialized && addPalmDialog.isShowing) {
                    addPalmBinding.llCapture.isEnabled = false
                    addPalmBinding.llCaptureSub.setBackgroundResource(R.drawable.bg_gray_55)
                }
            }
            if (frame == null) {
                restartRecognitionWithDelay(PALM_DELAY_MILLIS)
                return
            }
            mainHandler?.post {
                if (isFinishing || isDestroyed) return@post
                if (frame.rgbData != null) buildRgbBitmap(frame.rgbData,
                    frame.rgbCols,
                    frame.rgbRows)
                if (frame.irData != null) buildIrBitmap(frame.irData, frame.irCols, frame.irRows)
                if (mRgbBitmap != null) homeBinding.rgbImage.setImageBitmap(mRgbBitmap)
                if (mIrBitmap != null) homeBinding.irImage.setImageBitmap(mIrBitmap)
            }

            if (mCurrentWorkMode == WorkMode.REGISTER) {
                mCurrentWorkMode = WorkMode.NONE
                startRegisterFlow(frame)
            } else if (mCurrentWorkMode == WorkMode.RECOGNIZE) {
                performRecognition(frame)
            }
        }

        override fun onCapturePalmHint(hint: Hint?, map: HashMap<Int?, Float?>?) {

            if (hint != null && hint != Hint.NO_PALM_DETECTED && hint != Hint.TIMEOUT) {
                if (!isPalmDetected) {

                    isPalmDetected = true
                    palmDetected()
                }
            }

            if (hint == Hint.NO_PALM_DETECTED) {

                isPalmDetected = false
            }

            if (hint == Hint.TIMEOUT) {

                mainHandler?.post {
                    tvSkipHideShow(true)
                }
                isPalmDetected = false
                if (mCurrentWorkMode != WorkMode.RECOGNIZE) {
                    mCurrentWorkMode = WorkMode.NONE
                }
                restartRecognitionWithDelay(PALM_DELAY_MILLIS)
            }

            mainHandler?.post {
                if (::addPalmBinding.isInitialized && addPalmDialog.isShowing) {
                    if (isPalmDetected) {
                        if (isCaptureEligible) {

                            addPalmBinding.llCaptureSub.setBackgroundResource(R.drawable.ripple_effect_red_55)
                            addPalmBinding.llCapture.isEnabled = true
                        }
                    } else {
                        addPalmBinding.llCaptureSub.setBackgroundResource(R.drawable.bg_gray_55)
                        addPalmBinding.llCapture.isEnabled = false
                    }
                }
            }
        }

        override fun onCapturePalmQualityPass() {

        }
    }

    private fun tvSkipHideShow(show: Boolean) {

        val shouldShow = show && (isLeftPalmAdded || isRightPalmAdded)
        addPalmBinding.tvSkip.apply {
            visibility = if (shouldShow) View.VISIBLE else View.INVISIBLE
            isEnabled = shouldShow
        }
    }

    private fun openDevice() {
        if (mIsOpenCamera) return
        Device.create(this, object : DeviceListener {
            override fun onDeviceCreatedSuccess(d: IDevice,
                i: Int,
                r: MutableMap<Long?, IDevice?>?,
                t: UsbMapTable.DeviceType?) {
                deviceThread.execute { open(d) }
            }

            override fun onDeviceCreateFailed(d: IDevice?) {
            }

            override fun onDeviceDestroy(d: IDevice?) {
                mIsRunning = false
                mDevice = null
                mIsOpenCamera = false

                mainHandler?.post {
                    if (isFinishing || isDestroyed) return@post
                    homeBinding.ivStudent.visibility = View.GONE
                    homeBinding.lavAnimInit.visibility = View.GONE
                    homeBinding.llLavBorder.visibility = View.GONE
//                    homeBinding.lavAnimRecog.visibility = View.GONE
                    homeBinding.lavAnimInit.cancelAnimation()
                    homeBinding.lavAnimRecog.cancelAnimation()
                    colorSelected = 0
                    homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)

                    palmRecognitionBinding.ivStudent.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.visibility = View.GONE
                    palmRecognitionBinding.llLavBorder.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.cancelAnimation()
                    palmRecognitionBinding.lavAnimRecog.cancelAnimation()
                    palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)

                    homeBinding.tvName.text = resources.getString(R.string.device_disconnected)
                    homeBinding.tvClassSec.text =
                        resources.getString(R.string.palm_detection_device_info)

                    palmRecognitionBinding.tvName.text =
                        resources.getString(R.string.device_disconnected)
                    palmRecognitionBinding.tvClassSec.text =
                        resources.getString(R.string.palm_detection_device_info)

                    homeBinding.lavAnimSuccess.visibility = View.GONE
                    homeBinding.lavAnimFail.visibility = View.GONE

//                    addPalmBinding.tvInfoMsg.text =
//                        resources.getString(R.string.device_disconnected)
                    homeBinding.tvDeviceStatus.text =
                        resources.getString(R.string.device_disconnected)
                    addPalmBinding.tvDeviceStatus.text =
                        resources.getString(R.string.device_disconnected)
                    homeBinding.tvDeviceInfo.text =
                        resources.getString(R.string.tap_here_to_connect)
                    addPalmBinding.tvDeviceInfo.text =
                        resources.getString(R.string.tap_here_to_connect)
                    homeBinding.ivDeviceStatus.setImageResource(R.drawable.device_disconnected)
                    addPalmBinding.ivDeviceStatus.setImageResource(R.drawable.device_disconnected)

                    addPalmBinding.llDeviceConnect.visibility = View.VISIBLE
                    addPalmBinding.scrollView.visibility = View.GONE

                    palmRecognitionBinding.llDeviceConnect.visibility = View.VISIBLE
                    palmRecognitionBinding.llMain.visibility = View.GONE
                }
            }
        }, object : DeviceStateListener {
            override fun onDevicePermissionGranted(d: DtUsbDevice?) {
            }

            override fun onDevicePermissionDenied(d: DtUsbDevice?) {
            }

            override fun onAttached(d: DtUsbDevice?) {

                algoStatus = EnableAlgorithmStatus.DISABLE
            }

            override fun onDetached(d: DtUsbDevice?) {

                mIsRunning = false
                mIsOpenCamera = false
                mainHandler?.post {
                    if (isFinishing || isDestroyed) return@post
                    homeBinding.ivStudent.visibility = View.GONE
                    homeBinding.lavAnimInit.visibility = View.GONE
                    homeBinding.llLavBorder.visibility = View.GONE
//                    homeBinding.lavAnimRecog.visibility = View.GONE
                    homeBinding.lavAnimInit.cancelAnimation()
                    homeBinding.lavAnimRecog.cancelAnimation()
                    colorSelected = 0
                    homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)

                    palmRecognitionBinding.ivStudent.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.visibility = View.GONE
                    palmRecognitionBinding.llLavBorder.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.cancelAnimation()
                    palmRecognitionBinding.lavAnimRecog.cancelAnimation()
                    palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_plam_not_recognized_55)

                    homeBinding.tvName.text = resources.getString(R.string.device_disconnected)
                    homeBinding.tvClassSec.text =
                        resources.getString(R.string.palm_detection_device_info)

                    palmRecognitionBinding.tvName.text =
                        resources.getString(R.string.device_disconnected)
                    palmRecognitionBinding.tvClassSec.text =
                        resources.getString(R.string.palm_detection_device_info)

                    homeBinding.lavAnimSuccess.visibility = View.GONE
                    homeBinding.lavAnimFail.visibility = View.GONE

//                    addPalmBinding.tvInfoMsg.text =
//                        resources.getString(R.string.device_disconnected)
                    homeBinding.tvDeviceStatus.text =
                        resources.getString(R.string.device_disconnected)
                    addPalmBinding.tvDeviceStatus.text =
                        resources.getString(R.string.device_disconnected)
                    homeBinding.tvDeviceInfo.text =
                        resources.getString(R.string.tap_here_to_connect)
                    addPalmBinding.tvDeviceInfo.text =
                        resources.getString(R.string.tap_here_to_connect)
                    homeBinding.ivDeviceStatus.setImageResource(R.drawable.device_disconnected)
                    addPalmBinding.ivDeviceStatus.setImageResource(R.drawable.device_disconnected)

                    addPalmBinding.llDeviceConnect.visibility = View.VISIBLE
                    addPalmBinding.scrollView.visibility = View.GONE

                    palmRecognitionBinding.llDeviceConnect.visibility = View.VISIBLE
                    palmRecognitionBinding.llMain.visibility = View.GONE

                    if (mTvDeviceInfo != null) mTvDeviceInfo!!.text = "DISCONNECTED"
                    if (mSwitchStartStream != null) mSwitchStartStream?.isChecked = false
                }
            }
        })
    }

    private fun open(device: IDevice) {
        device.open(object : IOpenCallback {
            override fun onDownloadPrepare() {
            }

            override fun onDownloadProgress(p: Int) {
            }

            override fun onDownloadSuccess() {
            }

            override fun onOpenSuccess() {

                mIsOpenCamera = true
                mDevice = device
                val info = (device as IVeinshine).deviceInfo
                val types = device.deviceSupportStreamType
                if (types != null && types.isNotEmpty()) {
                    currentStreamType = types[0]
                }
                mainHandler?.post {
                    if (isFinishing || isDestroyed) return@post
                    homeBinding.ivStudent.visibility = View.GONE
                    homeBinding.lavAnimInit.visibility = View.VISIBLE
                    homeBinding.llLavBorder.visibility = View.GONE
//                    homeBinding.lavAnimRecog.visibility = View.GONE
                    homeBinding.lavAnimInit.playAnimation()
                    homeBinding.lavAnimRecog.cancelAnimation()
//                    homeBinding.llImgBG.setBackgroundColor(Color.TRANSPARENT)

                    homeBinding.tvName.text = resources.getString(R.string.initializing)
                    homeBinding.tvClassSec.text = resources.getString(R.string.please_wait)

                    palmRecognitionBinding.ivStudent.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.visibility = View.VISIBLE
                    palmRecognitionBinding.llLavBorder.visibility = View.GONE
                    palmRecognitionBinding.lavAnimInit.playAnimation()
                    palmRecognitionBinding.lavAnimRecog.cancelAnimation()

                    palmRecognitionBinding.tvName.text = resources.getString(R.string.initializing)
                    palmRecognitionBinding.tvClassSec.text =
                        resources.getString(R.string.please_wait)

                    homeBinding.lavAnimSuccess.visibility = View.GONE
                    homeBinding.lavAnimFail.visibility = View.GONE

//                    addPalmBinding.tvInfoMsg.text = resources.getString(R.string.device_connected)
                    homeBinding.tvDeviceStatus.text = resources.getString(R.string.device_connected)
                    addPalmBinding.tvDeviceStatus.text =
                        resources.getString(R.string.device_connected)
//                    homeBinding.tvDeviceStatus.text = resources.getString(R.string.device_connected) + " (${info.device_name})"
                    homeBinding.tvDeviceInfo.text = resources.getString(R.string.connection_stable)
                    addPalmBinding.tvDeviceInfo.text =
                        resources.getString(R.string.connection_stable)
                    homeBinding.ivDeviceStatus.setImageResource(R.drawable.device_connected)
                    addPalmBinding.ivDeviceStatus.setImageResource(R.drawable.device_connected)

                    addPalmBinding.llDeviceConnect.visibility = View.GONE
                    addPalmBinding.scrollView.visibility = View.VISIBLE

                    palmRecognitionBinding.llDeviceConnect.visibility = View.GONE
                    palmRecognitionBinding.llMain.visibility = View.VISIBLE

//                    if (mSwitchStartStream != null) mSwitchStartStream?.isChecked = true
//                    enableDimPalm()

                    if (mTvDeviceInfo != null) mTvDeviceInfo!!.text = "READY: " + info.device_name
                    mListStreamType.clear()
                    mListStreamType.addAll(types)
                    if (mAdapterStreamType != null) mAdapterStreamType?.notifyDataSetChanged()
                }

//                if (mSwitchStartStream != null) mSwitchStartStream?.isChecked = true
                enableDimPalm()
            }

            override fun onOpenFail(e: Int) {
                showToast("Open error: $e")
            }
        })
    }

    private fun startStream() {
        val device = mDevice ?: return
        if (mIsRunning) return
        mIsRunning = true
        mStreamThread = Thread(Runnable {
            try {
                val type = currentStreamType
                if (type == StreamType.INVALID_STREAM_TYPE) {
                    mIsRunning = false
                    return@Runnable
                }
                val stream = device.createStream(type) ?: run {
                    mIsRunning = false
                    return@Runnable
                }
                val frames = stream.allocateFrames()
                if (stream.start() == 0) {
                    while (mIsRunning && mIsOpenCamera) {
                        if (stream.getFrames(frames, 2000) == 0) {
                            onDrawFrame(frames.getFrame(0), frames.getFrame(1))
                        } else {
                        }
                    }
                    if (mIsOpenCamera) {
                        try {
                            stream.stop()
                        } catch (e: Exception) {
                        }
                    }
                }
                if (mIsOpenCamera) {
                    try {
                        device.destroyStream(stream)
                    } catch (e: Exception) {
                    }
                }
            } catch (e: Exception) {
            } finally {
                mIsRunning = false
            }
        }, "StreamThread")
        mStreamThread?.start()
    }

    private fun onDrawFrame(f1: Frame?, f2: Frame?) {
        if (f1 != null) updateFrameData(f1)
        if (f2 != null) updateFrameData(f2)
        if (irFrameData1 != null && irDisPlay != null && mGLIrView != null) irDisPlay!!.render(
            mGLIrView,
            0,
            false,
            irFrameData1,
            irFrameW1,
            irFrameH1,
            2,
            if (irFrameExtraInfo != null) irFrameExtraInfo!!.palmRoi else null)
        if (rgbFrameData1 != null && rgbDisPlay != null && mGLRgbView != null) rgbDisPlay!!.render(
            mGLRgbView,
            0,
            false,
            rgbFrameData1,
            rgbFrameW1,
            rgbFrameH1,
            1,
            if (irFrameExtraInfo != null) irFrameExtraInfo!!.palmRoi else null)
    }

    private fun updateFrameData(f: Frame) {
        val type = f.frameType.name
        if (type.contains("RGB")) {
            rgbFrameW1 = f.width
            rgbFrameH1 = f.height
            rgbFrameData1 = f.rawData
        } else if (type.contains("IR")) {
            irFrameW1 = f.width
            irFrameH1 = f.height
            irFrameData1 = f.rawData
            irFrameExtraInfo = f.extraInfo

            val roi = irFrameExtraInfo?.palmRoi
            val hasPalm = roi != null && roi[2] > 0
            val isTooFar = hasPalm && roi!![2] < 180
            val currentState = if (!hasPalm) 0 else if (isTooFar) 1 else 2

            if (currentState != lastStreamPalmState) {
                lastStreamPalmState = currentState
                mainHandler?.post {
                    if (::addPalmBinding.isInitialized && addPalmDialog.isShowing && isCaptureEligible) {
                        when (currentState) {
                            0 -> {
                                addPalmBinding.llCaptureSub.setBackgroundResource(R.drawable.bg_gray_55)
                                addPalmBinding.llCapture.isEnabled = false
                                when {
                                    !isLeftPalmAdded && !isRightPalmAdded -> addPalmBinding.tvInfoMsg.text =
                                        "SHOW LEFT OR RIGHT PALM"
                                    isLeftPalmAdded -> addPalmBinding.tvInfoMsg.text =
                                        "SHOW RIGHT PALM 🤚"
                                    isRightPalmAdded -> addPalmBinding.tvInfoMsg.text =
                                        "SHOW LEFT PALM ✋"
                                }
                            }
                            1 -> {
                                addPalmBinding.llCaptureSub.setBackgroundResource(R.drawable.bg_gray_55)
                                addPalmBinding.llCapture.isEnabled = false
                                addPalmBinding.tvInfoMsg.text = "PLEASE MOVE CLOSER"
                            }
                            2 -> {
                                addPalmBinding.llCaptureSub.setBackgroundResource(R.drawable.ripple_effect_red_55)
                                addPalmBinding.llCapture.isEnabled = true
                                when {
                                    !isLeftPalmAdded && !isRightPalmAdded -> addPalmBinding.tvInfoMsg.text =
                                        "SHOW LEFT OR RIGHT PALM"
                                    isLeftPalmAdded -> addPalmBinding.tvInfoMsg.text =
                                        "SHOW RIGHT PALM 🤚"
                                    isRightPalmAdded -> addPalmBinding.tvInfoMsg.text =
                                        "SHOW LEFT PALM ✋"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun captureOnce() {
        val device = mDevice as? IVeinshine ?: return
        device.capturePalmOnce(mCapturePalmCallback, 5000, false)
    }

    private fun buildRgbBitmap(data: ByteArray, w: Int, h: Int) {
        val bits = ByteArray(data.size / 3 * 4)
        for (i in 0..<data.size / 3) {
            bits[i * 4] = data[i * 3 + 2]
            bits[i * 4 + 1] = data[i * 3 + 1]
            bits[i * 4 + 2] = data[i * 3]
            bits[i * 4 + 3] = -1
        }
        if (mRgbBitmap == null || mRgbBitmap?.width != w || mRgbBitmap?.height != h) mRgbBitmap =
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mRgbBitmap?.copyPixelsFromBuffer(ByteBuffer.wrap(bits))
    }

    private fun buildIrBitmap(data: ByteArray, w: Int, h: Int) {
        val bits = ByteArray(data.size * 4)
        for (i in data.indices) {
            bits[i * 4 + 2] = data[i]
            bits[i * 4 + 1] = bits[i * 4 + 2]
            bits[i * 4] = bits[i * 4 + 1]
            bits[i * 4 + 3] = -1
        }
        if (mIrBitmap == null || mIrBitmap?.width != w || mIrBitmap?.height != h) mIrBitmap =
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mIrBitmap!!.copyPixelsFromBuffer(ByteBuffer.wrap(bits))
    }

    private fun showCompareDialog() {
        val builder = AlertDialog.Builder(this)
        val v: View? = LayoutInflater.from(this).inflate(R.layout.dialog_compare_feature, null)
        builder.setView(v).setPositiveButton("Compare", null).show()
    }

    private fun showToast(text: String?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(c, text, Toast.LENGTH_SHORT).show()
        } else {
            runOnUiThread(Runnable { Toast.makeText(c, text, Toast.LENGTH_SHORT).show() })
        }
    }

    protected fun hideRect(v: DtRectRoiView) {
        v.setRect(BBox(0, 0, 0, 0), 0)
    }

    private fun stopCapture() {
        val device = mDevice as? IVeinshine ?: return
        device.stopPalmCapture()
    }

    private fun clearFrame() {
        irFrameData1 = null
        rgbFrameData1 = irFrameData1
    }

//    fun showProgressDialog(m: String?) {
//        progressDialog = DialogUtils.createLoadingDialog(this, m, false)
//        if (progressDialog != null) progressDialog.show()
//    }
//
//    fun dismissProgressDialog() {
//        if (progressDialog != null) progressDialog.dismiss()
//    }

    override fun onDestroy() {
        unregisterReceiver(nfcStateBroadcast)
        mIsRunning = false
        mainHandler?.removeCallbacksAndMessages(null)

        // Wait for StreamThread to finish BEFORE closing device or releasing displays
        // This avoids native crashes where the thread tries to use released resources.
        mStreamThread?.let {
            try {
                it.join(2500) // Wait up to 2.5s (getFrames has a 2s timeout)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        mStreamThread = null

        // Stop any active capture and close the device handle to save power
        stopCapture()

        mDevice?.let { device ->
            deviceThread.execute {
                try {
                    android.util.Log.d(TAG, "Closing device on activity destroy")
                    device.close()
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error closing device: ${e.message}")
                }
            }
        }
        mDevice = null
        mIsOpenCamera = false

        // Release displays only after StreamThread has finished
        rgbDisPlay?.release()
        rgbDisPlay = null
        irDisPlay?.release()
        irDisPlay = null

        // Properly shutdown executors
        matchPool.shutdownNow()
        deviceThread.shutdown()

        super.onDestroy()
    }

    override fun attendanceSuccess() {

        setPendingAttendanceCount()
    }

    override fun attendanceFailed() {

    }

    override fun palmDetected() {

        /* colorSelected: 0 = Gray, 1 = Black, 2 = Gold, 3 = Blue, 4 = Pink */
        if (mIsRunning) {
            mainHandler?.post {
                if (isFinishing || isDestroyed) return@post
                colorSelected = 2
                homeBinding.llLavBorder.setBackgroundResource(R.drawable.bg_gold_55)
                homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_gold_55)

                palmRecognitionBinding.llLavBorder.setBackgroundResource(R.drawable.bg_gold_55)
                palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_gold_55)
                mainHandler?.postDelayed({

                    if (colorSelected == 2 && !isFinishing && !isDestroyed) {

                        homeBinding.llLavBorder.setBackgroundResource(R.drawable.bg_dodger_blue_55)
                        homeBinding.llImgBG.setBackgroundResource(R.drawable.bg_black_55)

                        palmRecognitionBinding.llLavBorder.setBackgroundResource(R.drawable.bg_dodger_blue_55)
                        palmRecognitionBinding.llImgBG.setBackgroundResource(R.drawable.bg_black_55)
                    }
                }, 500)
            }
        }
    }

    private fun setPendingAttendanceCount() {

//        val attendanceCount = databaseHandler.getAttendanceCount()
//        homeBinding.tvPendingCount.text = " | Pending: $attendanceCount"
//        if (attendanceCount > 0) {
//
//            homeBinding.tvPendingCount.setTextColor(resources.getColor(R.color.red))
//        } else {
//
//            homeBinding.tvPendingCount.setTextColor(resources.getColor(R.color.black))
//        }
    }

    override fun getAttendanceRes(data: Boolean) {
        if (data) {

            dbDownloadBinding.tvDBCount.text =
                "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
            homeBinding.tvDBCount.text =
                "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
            dbDownloadBinding.tvDBDownload.text =
                "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"
            homeBinding.tvDBDownload.text =
                "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"

            pageNo++
            pageRetries = 1
            recognitionPresenter.getAttendanceDetails(pageNo.toString())
        } else {

            pageNo = 1
            pageRetries++
            recognitionPresenter.getAttendanceDetails(pageNo.toString())
        }
    }

    override fun attendanceDownloaded() {

        dbDownloadBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        homeBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        dbDownloadBinding.tvDBDownload.text =
            "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"
        homeBinding.tvDBDownload.text =
            "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"

        sharedPref.setStudentDBDownloadedTime(kotlinStatic.getDBDownloadedTime())
        sharedPref.setDBRefreshMandatory(false)

        pageNo = 1
        pageRetries = 1

        isStudentAPIRunning = false
        if (!isPalmAPIRunning) {

            textViewChangeDownload()
        }
    }

    override fun attendanceDownloadFailed() {

        if (pageRetries > 4) {

            dbDownloadBinding.tvDBDownload.text = "DB DOWNLOADED FAILED"
            homeBinding.tvDBDownload.text = "DB DOWNLOADED FAILED"
            pageNo = 1
            pageRetries = 1

            isStudentAPIRunning = false
            if (isPalmAPIRunning) {

            } else {

                dbDownloadBinding.tvDBUpdatedDate.text =
                    resources.getString(R.string.db_download_partial)
                homeBinding.tvDBUpdatedDate.text =
                    resources.getString(R.string.db_download_partial)
            }
            sharedPref.setDBRefreshMandatory(true)
            if (!isPalmAPIRunning) {

                textViewChangeDownload()
            }
        } else {

            recognitionPresenter.getAttendanceDetails(pageNo.toString())
        }
        pageRetries++
    }

    override fun getPalmRes(data: Boolean) {

        if (data) {

            dbDownloadBinding.tvDBCount.text =
                "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
            homeBinding.tvDBCount.text =
                "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
            dbDownloadBinding.tvDBDownload.text =
                "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"
            homeBinding.tvDBDownload.text =
                "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"

            palmPageNo++
            palmPageRetries = 1
            recognitionPresenter.getPalmDetails(palmPageNo.toString())
        } else {

            palmPageNo = 1
            palmPageRetries++
            recognitionPresenter.getPalmDetails(palmPageNo.toString())
        }
    }

    override fun palmDownloaded() {

        searchAdapter.notifyDataSetChanged()

        dbDownloadBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        homeBinding.tvDBCount.text =
            "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
        dbDownloadBinding.tvDBDownload.text =
            "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"
        homeBinding.tvDBDownload.text =
            "Downloading... DB: ${databaseHandler.getStudentCount()} / ${sharedPref.getTotalStudent()} | PALM: ${databaseHandler.getPalmCount()} / ${sharedPref.getTotalPalm()}"

        sharedPref.setPalmDBDownloadedTimeGMT(kotlinStatic.gmtTimeRightNow())
//        sharedPref.setStudentDBDownloadedTime(KotlinStatic().getDBDownloadedTime())
//        homeBinding.tvDBUpdatedDate.text = sharedPref.getStudentDBDownloadedTime()

        sharedPref.setDBRefreshMandatory(false)

        palmPageNo = 1
        palmPageRetries = 1

        isPalmAPIRunning = false
        if (!isStudentAPIRunning) {

            textViewChangeDownload()
        }
    }

    override fun palmDownloadFailed() {

        if (palmPageRetries > 4) {

            dbDownloadBinding.tvDBDownload.text = "DB DOWNLOADED FAILED"
            homeBinding.tvDBDownload.text = "DB DOWNLOADED FAILED"
            palmPageNo = 1
            palmPageRetries = 1

            isPalmAPIRunning = false
            if (isStudentAPIRunning) {

            } else {

                dbDownloadBinding.tvDBUpdatedDate.text =
                    resources.getString(R.string.db_download_partial)
                homeBinding.tvDBUpdatedDate.text =
                    resources.getString(R.string.db_download_partial)
            }
            sharedPref.setDBRefreshMandatory(true)
            if (!isStudentAPIRunning) {

                textViewChangeDownload()
            }
        } else {

            recognitionPresenter.getPalmDetails(palmPageNo.toString())
        }
        palmPageRetries++
    }

    private fun textViewChangeDownload() {

        object : CountDownTimer(2000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                homeBinding.tvDBCount.text =
                    "DB Total: ${databaseHandler.getStudentCount()} | Palm Total: ${databaseHandler.getPalmCount()}"
                dbDownloadBinding.tvDBDownload.isEnabled = true
                homeBinding.tvDBDownload.isEnabled = true
                dbDownloadBinding.tvDBDownload.text =
                    resources.getString(R.string.tap_here_to_download_db)
                homeBinding.tvDBDownload.text =
                    resources.getString(R.string.tap_here_to_download_db)

                if (sharedPref.getDBRefreshMandatory() == false) {

                    if (isPalmAddedToServerDBUpdated) {

                        isPalmAddedToServerDBUpdated = false
                        viewHideRV(true)
                    } else {

                        viewHideRV(false)
                    }
//                    viewHideRV(false)
                    homeBinding.tvDBUpdatedDate.text = sharedPref.getStudentDBDownloadedTime()
                    dbDownloadBinding.tvDBUpdatedDate.text = sharedPref.getStudentDBDownloadedTime()
                    dbDownloadDialog.dismiss()
                    nfcStartOrStop(1)
                }
                loadPalmCache()
            }
        }.start()
    }

    private fun viewHideRV(rvVisible: Boolean) {

        if (rvVisible) {

            homeBinding.rvStudents.visibility = View.VISIBLE
            homeBinding.llDBDetails.visibility = View.GONE
        } else {

            homeBinding.rvStudents.visibility = View.GONE
            homeBinding.llDBDetails.visibility = View.VISIBLE
        }
    }

    private fun loadLogoImg() {

        Picasso.get()
            .load(sharedPref.getLogoImgBaseUrl().toString() + sharedPref.getSchoolLogo().toString())
            .networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE)
            .error(R.drawable.lokate_student).fit().centerCrop().into(homeBinding.imgLogo)
    }

    private fun showLogoutDialog() {

        if (isStudentAPIRunning || isPalmAPIRunning) {

            kotlinStatic.goBackDisabledDialog(resources.getString(R.string.back_gesture_info))
        } else {

            val logoutDialog = Dialog(c)
            val logoutBinding = LogoutDialogBinding.inflate(layoutInflater)
            logoutDialog.setContentView(logoutBinding.root)
            val exitWindow = logoutDialog.window
            exitWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            exitWindow?.setLayout(kotlinStatic.getWidth() / 100 * 90,
                ConstraintLayout.LayoutParams.WRAP_CONTENT)
            logoutDialog.setCancelable(true)

            logoutBinding.tvNo.setOnClickListener {

                logoutDialog.dismiss()
            }
            logoutBinding.tvYes.setOnClickListener {

                logoutDialog.dismiss()
                sharedPref.clearUpToLogin()
                databaseHandler.resetPalmTable()
                databaseHandler.resetStudentsTable()
                databaseHandler.resetAttendanceTable()
                sharedPref.setPalmDBDownloadedTimeGMT("")
                gotoLogin()
            }
            logoutDialog.show()
        }
    }

    private val nfcStateBroadcast: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent?.action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {

                val intValue =
                    intent?.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_ON)
                when (intValue) {
                    NfcAdapter.STATE_ON -> {
                        if (sharedPref.getDBRefreshMandatory() == false) {

                            nfcStartOrStop(1)
                        }
                    }
                    NfcAdapter.STATE_OFF -> {
                        nfcStartOrStop(0)
                    }
                }
            }
        }
    }

    private fun nfcStartOrStop(num: Int) {

        if (mNfcAdapter != null) {
            if (num == 1) {

                try {
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(c,
                        0,
                        Intent(c, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_MUTABLE)
                    val filter: IntentFilter = IntentFilter()
                    filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED)
                    filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
                    filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
                    filter.addCategory(Intent.CATEGORY_DEFAULT)

                    mNfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)
                } catch (e: Exception) {

                }
            } else {

                mNfcAdapter!!.disableForegroundDispatch(c as Activity?)
            }
        }
        if (mNfcAdapter == null) {

            homeBinding.btnNfc.text = resources.getString(R.string.nfc)
            homeBinding.ivNFC.setImageResource(R.drawable.nfc_large_b_w)
            homeBinding.tvNFC.text = resources.getString(R.string.nfc_not_support_info)
            homeBinding.llNFC.isEnabled = false
            homeBinding.tvNFC.isEnabled = false
        } else {

            if (kotlinStatic.isNfcEnabled(this)) {

                homeBinding.btnNfc.text = resources.getString(R.string.nfc_on)
                homeBinding.ivNFC.setImageResource(R.drawable.nfc_large)
                homeBinding.tvNFC.text = resources.getString(R.string.nfc_on_info)
                homeBinding.llNFC.isEnabled = false
                homeBinding.tvNFC.isEnabled = false
            } else {

                homeBinding.btnNfc.text = resources.getString(R.string.nfc_off)
                homeBinding.ivNFC.setImageResource(R.drawable.nfc_large_b_w)
                homeBinding.tvNFC.text = resources.getString(R.string.nfc_off_info)
                homeBinding.llNFC.isEnabled = true
                homeBinding.tvNFC.isEnabled = true
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val action: String? = intent.action
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_TECH_DISCOVERED) || action.equals(
                NfcAdapter.ACTION_NDEF_DISCOVERED)) {

            kotlinStatic.invalidCardDismiss()
            if (sharedPref.getScanType() == "tag" || sharedPref.getScanType() == "reversed_dec") {

                val value = if (sharedPref.getScanType().equals("tag", ignoreCase = true)) {

                    toDec(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)!!)
                } else {

                    toReversedDec(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)!!)
                }
//                makeAPICall("4", value.toString().trim())
                nfcSearchDB(value.toString().trim())
            } else {

                if (intent != null) {

                    handleIntent(intent)
                }
            }
        }
    }

    private fun nfcSearchDB(data: String) {

        val alStudents =
            databaseHandler.searchStudentsByNfc(data.toString().trim()) as ArrayList<Students>
        if (alStudents.isNotEmpty()) {

            kotlinStatic.goBackDisabledDialogDismiss()
            homeBinding.etSearch.setText(alStudents[0].childName)
            this.alStudents.clear()
            this.alStudents = alStudents
            searchAdapter =
                SearchAdapter(c, alStudents, true, mIsOpenCamera, databaseHandler)
            homeBinding.rvStudents.adapter = searchAdapter
            viewHideRV(true)
        } else {

            homeBinding.etSearch.setText("")
            kotlinStatic.goBackDisabledDialog(resources.getString(R.string.no_students_found))
            viewHideRV(false)
        }
    }

    private fun handleIntent(intent: Intent) {

        val action: String? = intent.action
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action, ignoreCase = true)) {

            val type: String? = intent.type
            if (MIME_TEXT_PLAIN.equals(type, ignoreCase = true)) {

                NdefReaderTask(c).execute(tag)
            } else {

//                kotlinStatic.invalidCard(mNfcAdapter)
                kotlinStatic.goBackDisabledDialog(resources.getString(R.string.invalid_card))
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action, ignoreCase = true)) {

            val techList = tag!!.techList
            val searchedTech = Ndef::class.java.name

            for (tech in techList) {
                if (searchedTech.equals(tech, ignoreCase = true)) {
                    NdefReaderTask(c).execute(tag)
                    break
                }
            }
        } else {

            val type: String? = intent.type
            if (MIME_TEXT_PLAIN == type) {

                NdefReaderTask(c).execute(tag)
            } else {

//                kotlinStatic.invalidCard(mNfcAdapter)
                kotlinStatic.goBackDisabledDialog(resources.getString(R.string.invalid_card))
            }
        }
    }

    override fun result(data: String?) {

        if (data != null) {

//            makeAPICall("4", data.trim())
            nfcSearchDB(data.toString().trim())
        }
    }

    private fun toDec(bytes: ByteArray): Long {
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices.reversed()) {
            val value = bytes[i].toLong() and 0xffL
            result += value * factor
            factor *= 256L
        }
        return result
    }

    private fun toReversedDec(bytes: ByteArray): Long {
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices.reversed()) {
            val value = bytes[i].toLong() and 0xffL
            result += value * factor
            factor *= 256L
        }
        return result
    }
}