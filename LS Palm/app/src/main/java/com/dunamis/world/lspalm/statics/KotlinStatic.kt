package com.dunamis.world.lspalm.statics

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.dunamis.world.lspalm.R
import com.dunamis.world.lspalm.databinding.DuplicatePalmDialogBinding
import com.dunamis.world.lspalm.databinding.InfoDialogBinding
import com.dunamis.world.lspalm.databinding.InvalidCardDialogBinding
import com.dunamis.world.lspalm.databinding.TimeZoneDialogBinding
import com.dunamis.world.lspalm.db.Students
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class KotlinStatic(c: Context) {

    private var c: Context = c
    fun getVersionNo(): String {

        val packageInfo = c.packageManager.getPackageInfo(c.packageName, 0)
        return "v" + packageInfo.versionName
    }

    fun getWidth(): Int {

        val displayMetrics = DisplayMetrics()
        val windowManager = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getHeight(): Int {

        val displayMetrics = DisplayMetrics()
        val windowManager = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getDBDownloadedTime(): String {

        val cal = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd/MMM/yyyy 'at' hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(cal.time)
        return "DB Downloaded on $formattedDate"
    }

    fun gmtTimeRightNow(): String {

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return sdf.format(cal.getTime())
    }

    fun fetchDate(): String {

        val cal = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(cal.time)
        return formattedDate
    }

    fun isNfcEnabled(context: Context): Boolean {

        val manager = context.getSystemService(Context.NFC_SERVICE) as NfcManager
        val adapter = manager.defaultAdapter
        return adapter != null && adapter.isEnabled
    }

    fun isInternetAvailable(): Boolean {

        val connectivityManager =
            c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray? {

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun permissionCheck(): Boolean {

        return ContextCompat.checkSelfPermission(c,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun isAutoTimeEnabled(): Boolean {

        val dateTime = Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME, 0)
        val timeZone =
            Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0)
        return dateTime == 1 && timeZone == 1
    }

    val timeZoneDialog: Dialog = Dialog(c)
    fun showAutoTimeZoneDialog() {

        if (timeZoneDialog != null) {

            timeZoneDialog.dismiss()
        }
//        timeZoneDialog = Dialog(c)
        timeZoneDialog.setCancelable(false)
        val timeZoneBinding = TimeZoneDialogBinding.inflate(LayoutInflater.from(c))
        timeZoneDialog.setContentView(timeZoneBinding.root)

        val window = timeZoneDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lp = timeZoneDialog.window?.attributes
        lp?.dimAmount = 0.76f

        timeZoneBinding.tvSettings.setOnClickListener {

            timeZoneDialog.dismiss()
            c.startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
        }
        timeZoneDialog.show()
    }

    val backDisabledDialog: Dialog = Dialog(c)
    fun goBackDisabledDialog(info: String) {

        if (backDisabledDialog != null) {

            backDisabledDialog.dismiss()
        }
//        backDisabledDialog = Dialog(c)
        backDisabledDialog.setCancelable(false)
        val infoDialogBinding = InfoDialogBinding.inflate(LayoutInflater.from(c))
        backDisabledDialog.setContentView(infoDialogBinding.root)

        val window = backDisabledDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lp = backDisabledDialog.window?.attributes
        lp?.dimAmount = 0.76f

        infoDialogBinding.tvInfo.text = info
        infoDialogBinding.tvDismiss.setOnClickListener { backDisabledDialog.dismiss() }
        backDisabledDialog.show()
    }

    fun goBackDisabledDialogDismiss() {

        if (backDisabledDialog != null) {

            backDisabledDialog.dismiss()
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    fun playPalmSuccess() {

        mediaPlayer = MediaPlayer.create(c, R.raw.face_id_succes)
        mediaPlayer?.start()
    }

    fun playPalmFailure() {

        mediaPlayer = MediaPlayer.create(c, R.raw.face_id_error)
        mediaPlayer?.start()
    }

    val invalidCardDialog: Dialog = Dialog(c)
    fun invalidCard(nfcAdapter: NfcAdapter?) {

        if (invalidCardDialog != null) {

            invalidCardDialog.dismiss()
        }
//        timeZoneDialog = Dialog(c)
        invalidCardDialog.setCancelable(true)
        val invalidCardBinding = InvalidCardDialogBinding.inflate(LayoutInflater.from(c))
        invalidCardDialog.setContentView(invalidCardBinding.root)

        val window = invalidCardDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lp = invalidCardDialog.window?.attributes
        lp?.dimAmount = 0.76f

        if (nfcAdapter == null) {

            invalidCardBinding.tvInfo.text = c.resources.getString(R.string.nfc_not_support_info)
            invalidCardBinding.tvDismiss.text = c.resources.getString(R.string.dismiss)
        } else {
            if (isNfcEnabled(c)) {

                invalidCardBinding.tvInfo.text = c.resources.getString(R.string.nfc_on_info)
                invalidCardBinding.tvDismiss.text = c.resources.getString(R.string.dismiss)
            } else {

                invalidCardBinding.tvInfo.text = c.resources.getString(R.string.nfc_off_info)
                invalidCardBinding.tvDismiss.text = c.resources.getString(R.string.open_settings)
            }
        }
        invalidCardBinding.tvDismiss.setOnClickListener {

            invalidCardDialog.dismiss()
            if (nfcAdapter != null && !isNfcEnabled(c)) {

                c.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
        }
        invalidCardDialog.show()
    }

    fun invalidCardDismiss() {

        if (invalidCardDialog != null) {

            invalidCardDialog.dismiss()
        }
    }

    fun duplicatePalmDialog(studentData: Students?, baseImageUrl: String?) {

        val duplicatePalmDialog = Dialog(c)
        duplicatePalmDialog.setCancelable(false)
        val duplicatePalmBinding = DuplicatePalmDialogBinding.inflate(LayoutInflater.from(c))
        duplicatePalmDialog.setContentView(duplicatePalmBinding.root)

        val window = duplicatePalmDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(getWidth() / 100 * 90,
            ConstraintLayout.LayoutParams.WRAP_CONTENT)

        val lp = duplicatePalmDialog.window?.attributes
        lp?.dimAmount = 0.76f

        if (studentData?.childGender.equals("female", ignoreCase = true)) {

            duplicatePalmBinding.llImgBG.setBackgroundResource(R.drawable.bg_pink_female_21)
            Picasso.get().load(baseImageUrl + studentData?.childPhoto)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.girl)
                .error(R.drawable.girl).fit().centerCrop().into(duplicatePalmBinding.ivStudent)
        } else {

            duplicatePalmBinding.llImgBG.setBackgroundResource(R.drawable.bg_blue_male_21)
            Picasso.get().load(baseImageUrl + studentData?.childPhoto)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.boy)
                .error(R.drawable.boy).fit().centerCrop().into(duplicatePalmBinding.ivStudent)
        }

        duplicatePalmBinding.tvName.text = studentData?.childName
        duplicatePalmBinding.tvClassSec.text =
            studentData?.childClass + " - " + studentData?.childSection + " (ID# " + studentData?.childId + ")"
        duplicatePalmBinding.tvDismiss.setOnClickListener { duplicatePalmDialog.dismiss() }
        duplicatePalmDialog.show()
    }
}