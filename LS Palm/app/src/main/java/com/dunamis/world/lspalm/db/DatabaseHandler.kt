package com.dunamis.world.lspalm.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dunamis.world.lspalm.statics.URLS

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, URLS().DATABASE_NAME, null, URLS().DATABASE_VERSION) {

    private val TABLE_STUDENTS = "STUDENTS_TABLE"
    private val TABLE_PALM = "PALM_TABLE"
    private val TABLE_ATTENDANCE = "ATTENDANCE_TABLE"

    /* Table Students, Start */
    private val KEY_ID: String = "id"
    private val KEY_CHILD_ID: String = "child_id"
    private val KEY_CHILD_NAME: String = "child_name"
    private val KEY_CHILD_CLASS: String = "child_class"

    private val KEY_CHILD_SECTION: String = "child_section"
    private val KEY_CHILD_NFC_ID: String = "nfc_id"
    private val KEY_CHILD_REG_NO: String = "child_reg_no"
    private val KEY_CHILD_FATHER_NAME: String = "child_father_name"

    private val KEY_TRIP_NO_PICK_UP: String = "at_child_trip_no_pickup"
    private val KEY_TRIP_NO_DROP_OFF: String = "at_child_trip_no_drop_off"
    private val KEY_CHILD_OR_EMP: String = "child_or_employee"
    private val CHILD_PCR_VALIDITY_END_DATE: String = "child_pcr_validity_end_date"

    private val CHILD_PARENT_NFC_ID: String = "child_parent_nfc_id"
    private val PARENT_AC_NO: String = "parent_ac_no"
    private val HANDICAPPED_TYPE: String = "handicapped_type"
    private val SUSPENSION_DATE: String = "suspension_date"

    private val MOTHER_NFC: String = "mother_nfc_id"
    private val ENTRY_RESTRICTION: String = "entry_restriction"
    private val CHILD_AUTHORIZED_PICKUP: String = "child_authorized_pickup"
    private val KEY_CHILD_GENDER: String = "child_gender"
    private val KEY_CHILD_PHOTO: String = "child_photo"

    private val KEY_CHILD_FATHER_EMAIL: String = "child_father_email_id"
    private val KEY_CHILD_FATHER_TEL: String = "child_father_tel"
    private val KEY_CHILD_MOTHER_EMAIL: String = "child_mother_email_id"
    private val KEY_CHILD_MOTHER_TEL: String = "child_mother_tel"
    /* Table Students, End */

    /* Table Palm, Start */
    private val KEY_PALM_ID: String = "palm_id"
    private val KEY_PALM_DATA_ONE: String = "palm_data_one"
    private val KEY_PALM_DATA_RGB_LEFT: String = "palm_data_rgb_left"
    private val KEY_PALM_DATA_IR_LEFT: String = "palm_data_ir_left"
    private val KEY_PALM_DATA_RGB_RIGHT: String = "palm_data_rgb_right"
    private val KEY_PALM_DATA_IR_RIGHT: String = "palm_data_ir_right"
    /* Table Palm, End */

    /* Table Attendance, Start */
    private val KEY_LATITUDE: String = "latitude"
    private val KEY_LONGITUDE: String = "longitude"
    private val KEY_DATATYPE: String = "data_type"
    private val KEY_BUS_ID: String = "bus_id"
    private val KEY_TIME_STAMP: String = "time_stamp"
    private val KEY_PICK_UP_DROP_OFF: String = "at_in_or_out"
    private val KEY_CHILD_STATUS: String = "at_child_status"
    private val KEY_CHILD_FORGOT_CARD: String = "child_forgot_card"
    private val KEY_CHILD_AUTH_ID: String = "parent_auth_id"
    private val KEY_CHILD_CLASS_AUTH: String = "auth_class"
    private val KEY_CHILD_SECTION_AUTH: String = "auth_section"
    /* Table Attendance, End */

    private val CREATE_TABLE_STUDENTS = ("CREATE TABLE " + TABLE_STUDENTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CHILD_ID + " VARCHAR," + KEY_CHILD_NAME + " VARCHAR," + KEY_CHILD_CLASS + " VARCHAR," + KEY_CHILD_SECTION + " VARCHAR,"
            + KEY_CHILD_NFC_ID + " VARCHAR," + KEY_CHILD_REG_NO + " VARCHAR," + KEY_CHILD_FATHER_NAME + " VARCHAR," + KEY_TRIP_NO_PICK_UP + " VARCHAR," + KEY_TRIP_NO_DROP_OFF + " VARCHAR,"
            + KEY_CHILD_OR_EMP + " VARCHAR," + CHILD_PCR_VALIDITY_END_DATE + " VARCHAR," + CHILD_PARENT_NFC_ID + " VARCHAR," + PARENT_AC_NO + " VARCHAR," + HANDICAPPED_TYPE + " VARCHAR,"
            + SUSPENSION_DATE + " VARCHAR," + MOTHER_NFC + " VARCHAR," + ENTRY_RESTRICTION + " VARCHAR," + CHILD_AUTHORIZED_PICKUP + " VARCHAR," + KEY_CHILD_GENDER + " VARCHAR," + KEY_CHILD_PHOTO + " VARCHAR,"
            + KEY_CHILD_FATHER_EMAIL + " VARCHAR," + KEY_CHILD_FATHER_TEL + " VARCHAR," + KEY_CHILD_MOTHER_EMAIL + " VARCHAR," + KEY_CHILD_MOTHER_TEL + " VARCHAR,"
            + "UNIQUE (" + KEY_CHILD_ID + ") ON CONFLICT ROLLBACK)")

    private val CREATE_TABLE_PALM_DATA =
        ("CREATE TABLE " + TABLE_PALM + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_PALM_ID + " VARCHAR," + KEY_PALM_DATA_ONE + " VARCHAR," + KEY_CHILD_ID + " VARCHAR," + KEY_CHILD_REG_NO + " VARCHAR,"
                + KEY_PALM_DATA_RGB_LEFT + " BLOB," + KEY_PALM_DATA_IR_LEFT + " BLOB," + KEY_PALM_DATA_RGB_RIGHT + " BLOB," + KEY_PALM_DATA_IR_RIGHT + " BLOB)")

    private val CREATE_TABLE_ATTENDANCE = ("CREATE TABLE " + TABLE_ATTENDANCE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LATITUDE + " VARCHAR," + KEY_LONGITUDE + " VARCHAR," + KEY_DATATYPE + " VARCHAR," + KEY_BUS_ID + " VARCHAR,"
            + KEY_TIME_STAMP + " VARCHAR," + KEY_CHILD_ID + " VARCHAR," + KEY_PICK_UP_DROP_OFF + " VARCHAR," + KEY_CHILD_STATUS + " VARCHAR,"
            + KEY_CHILD_NFC_ID + " VARCHAR," + KEY_CHILD_FORGOT_CARD + " VARCHAR," + KEY_CHILD_REG_NO + " VARCHAR," + KEY_TRIP_NO_PICK_UP + " VARCHAR,"
            + KEY_TRIP_NO_DROP_OFF + " VARCHAR," + KEY_CHILD_NAME + " VARCHAR," + KEY_CHILD_OR_EMP + " VARCHAR," + KEY_CHILD_AUTH_ID + " VARCHAR,"
            + KEY_CHILD_CLASS_AUTH + " VARCHAR," + KEY_CHILD_SECTION_AUTH + " VARCHAR)")

    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL(CREATE_TABLE_STUDENTS)
        db?.execSQL(CREATE_TABLE_PALM_DATA)
        db?.execSQL(CREATE_TABLE_ATTENDANCE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        db?.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PALM")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        onCreate(db)
    }

    fun addBatchStudents(studentList: List<Students>): Boolean {

        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (student in studentList) {

                val values = ContentValues().apply {
                    put(KEY_CHILD_ID, student.childId)
                    put(KEY_CHILD_NAME, student.childName)
                    put(KEY_CHILD_CLASS, student.childClass)
                    put(KEY_CHILD_SECTION, student.childSection)
                    put(KEY_CHILD_NFC_ID, student.childNfcId)
                    put(KEY_CHILD_REG_NO, student.childRegNo)
                    put(KEY_CHILD_FATHER_NAME, student.childFatherName)
                    put(KEY_TRIP_NO_PICK_UP, student.tripNoPickUp)
                    put(KEY_TRIP_NO_DROP_OFF, student.tripNoDropOff)
                    put(KEY_CHILD_OR_EMP, student.childOrEmp)
                    put(CHILD_PCR_VALIDITY_END_DATE, student.pcrValidityEndDate)
                    put(CHILD_PARENT_NFC_ID, student.parentNfcId)
                    put(PARENT_AC_NO, student.parentAcNo)
                    put(HANDICAPPED_TYPE, student.handicappedType)
                    put(SUSPENSION_DATE, student.suspensionDate)
                    put(MOTHER_NFC, student.motherNfc)
                    put(ENTRY_RESTRICTION, student.entryRestriction)
                    put(CHILD_AUTHORIZED_PICKUP, student.childAuthorizedPickup)
                    put(KEY_CHILD_GENDER, student.childGender)
                    put(KEY_CHILD_PHOTO, student.childPhoto)
                    put(KEY_CHILD_FATHER_EMAIL, student.childFatherEmail)
                    put(KEY_CHILD_FATHER_TEL, student.childFatherTel)
                    put(KEY_CHILD_MOTHER_EMAIL, student.childMotherEmail)
                    put(KEY_CHILD_MOTHER_TEL, student.childMotherTel)
                }
                db.insert(TABLE_STUDENTS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            try {
                db.endTransaction()
            } catch (e: Throwable) {
                db.close()
            }
        }
        db.close()
        return true
    }

    fun getStudentByRegNo(regNo: String?): Students? {

        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$KEY_CHILD_REG_NO = ?",
            arrayOf(regNo),
            null,
            null,
            null,
            "1" // limit 1
        )

        val student = if (cursor.moveToFirst()) {
            Students(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                childName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NAME)),
                childClass = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_CLASS)),
                childSection = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_SECTION)),
                childNfcId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NFC_ID)),
                childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                childFatherName = cursor.getString(cursor.getColumnIndexOrThrow(
                    KEY_CHILD_FATHER_NAME)),
                tripNoPickUp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_PICK_UP)),
                tripNoDropOff = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_DROP_OFF)),
                childOrEmp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_OR_EMP)),
                pcrValidityEndDate = cursor.getString(cursor.getColumnIndexOrThrow(
                    CHILD_PCR_VALIDITY_END_DATE)),
                parentNfcId = cursor.getString(cursor.getColumnIndexOrThrow(CHILD_PARENT_NFC_ID)),
                parentAcNo = cursor.getString(cursor.getColumnIndexOrThrow(PARENT_AC_NO)),
                handicappedType = cursor.getString(cursor.getColumnIndexOrThrow(HANDICAPPED_TYPE)),
                suspensionDate = cursor.getString(cursor.getColumnIndexOrThrow(SUSPENSION_DATE)),
                motherNfc = cursor.getString(cursor.getColumnIndexOrThrow(MOTHER_NFC)),
                entryRestriction = cursor.getString(cursor.getColumnIndexOrThrow(ENTRY_RESTRICTION)),
                childAuthorizedPickup = cursor.getString(cursor.getColumnIndexOrThrow(
                    CHILD_AUTHORIZED_PICKUP)),
                childGender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_GENDER)),
                childPhoto = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_PHOTO)),
                childFatherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                    KEY_CHILD_FATHER_EMAIL)),
                childFatherTel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_FATHER_TEL)),
                childMotherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                    KEY_CHILD_MOTHER_EMAIL)),
                childMotherTel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_MOTHER_TEL))
            )
        } else null

        cursor.close()
        db.close()
        return student
    }

    fun searchStudentsByName(name: String): List<Students> {
        val studentList = mutableListOf<Students>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$KEY_CHILD_NAME LIKE ?",
            arrayOf("%$name%"),
            null,
            null,
            "$KEY_CHILD_NAME ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val student = Students(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                    childName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NAME)),
                    childClass = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_CLASS)),
                    childSection = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_SECTION)),
                    childNfcId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NFC_ID)),
                    childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                    childFatherName = cursor.getString(
                        cursor.getColumnIndexOrThrow(KEY_CHILD_FATHER_NAME)
                    ),
                    tripNoPickUp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_PICK_UP)),
                    tripNoDropOff = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_TRIP_NO_DROP_OFF)),
                    childOrEmp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_OR_EMP)),
                    pcrValidityEndDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_PCR_VALIDITY_END_DATE)
                    ),
                    parentNfcId = cursor.getString(cursor.getColumnIndexOrThrow(CHILD_PARENT_NFC_ID)),
                    parentAcNo = cursor.getString(cursor.getColumnIndexOrThrow(PARENT_AC_NO)),
                    handicappedType = cursor.getString(cursor.getColumnIndexOrThrow(HANDICAPPED_TYPE)),
                    suspensionDate = cursor.getString(cursor.getColumnIndexOrThrow(SUSPENSION_DATE)),
                    motherNfc = cursor.getString(cursor.getColumnIndexOrThrow(MOTHER_NFC)),
                    entryRestriction = cursor.getString(cursor.getColumnIndexOrThrow(
                        ENTRY_RESTRICTION)),
                    childAuthorizedPickup = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_AUTHORIZED_PICKUP)
                    ),
                    childGender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_GENDER)),
                    childPhoto = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_PHOTO)),
                    childFatherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_EMAIL)),
                    childFatherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_TEL)),
                    childMotherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_EMAIL)),
                    childMotherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_TEL))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun searchStudentsByRegNo(regNo: String?): List<Students> {
        val studentList = mutableListOf<Students>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$KEY_CHILD_REG_NO LIKE ?",
            arrayOf("%$regNo%"),
            null,
            null,
            "$KEY_CHILD_REG_NO ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val student = Students(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                    childName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NAME)),
                    childClass = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_CLASS)),
                    childSection = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_SECTION)),
                    childNfcId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NFC_ID)),
                    childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                    childFatherName = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_NAME)),
                    tripNoPickUp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_PICK_UP)),
                    tripNoDropOff = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_TRIP_NO_DROP_OFF)),
                    childOrEmp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_OR_EMP)),
                    pcrValidityEndDate = cursor.getString(cursor.getColumnIndexOrThrow(
                        CHILD_PCR_VALIDITY_END_DATE)),
                    parentNfcId = cursor.getString(cursor.getColumnIndexOrThrow(CHILD_PARENT_NFC_ID)),
                    parentAcNo = cursor.getString(cursor.getColumnIndexOrThrow(PARENT_AC_NO)),
                    handicappedType = cursor.getString(cursor.getColumnIndexOrThrow(HANDICAPPED_TYPE)),
                    suspensionDate = cursor.getString(cursor.getColumnIndexOrThrow(SUSPENSION_DATE)),
                    motherNfc = cursor.getString(cursor.getColumnIndexOrThrow(MOTHER_NFC)),
                    entryRestriction = cursor.getString(cursor.getColumnIndexOrThrow(
                        ENTRY_RESTRICTION)),
                    childAuthorizedPickup = cursor.getString(cursor.getColumnIndexOrThrow(
                        CHILD_AUTHORIZED_PICKUP)),
                    childGender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_GENDER)),
                    childPhoto = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_PHOTO)),
                    childFatherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_EMAIL)),
                    childFatherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_TEL)),
                    childMotherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_EMAIL)),
                    childMotherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_TEL))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun searchStudentsByPhoneNumber(phoneNumber: String): List<Students> {
        val studentList = mutableListOf<Students>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$KEY_CHILD_FATHER_TEL LIKE ? OR $KEY_CHILD_MOTHER_TEL LIKE ?",
            arrayOf("%$phoneNumber%", "%$phoneNumber%"),
            null,
            null,
            "$KEY_CHILD_NAME ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val student = Students(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                    childName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NAME)),
                    childClass = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_CLASS)),
                    childSection = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_SECTION)),
                    childNfcId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NFC_ID)),
                    childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                    childFatherName = cursor.getString(
                        cursor.getColumnIndexOrThrow(KEY_CHILD_FATHER_NAME)
                    ),
                    tripNoPickUp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_PICK_UP)),
                    tripNoDropOff = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_TRIP_NO_DROP_OFF)),
                    childOrEmp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_OR_EMP)),
                    pcrValidityEndDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_PCR_VALIDITY_END_DATE)
                    ),
                    parentNfcId = cursor.getString(cursor.getColumnIndexOrThrow(CHILD_PARENT_NFC_ID)),
                    parentAcNo = cursor.getString(cursor.getColumnIndexOrThrow(PARENT_AC_NO)),
                    handicappedType = cursor.getString(cursor.getColumnIndexOrThrow(HANDICAPPED_TYPE)),
                    suspensionDate = cursor.getString(cursor.getColumnIndexOrThrow(SUSPENSION_DATE)),
                    motherNfc = cursor.getString(cursor.getColumnIndexOrThrow(MOTHER_NFC)),
                    entryRestriction = cursor.getString(cursor.getColumnIndexOrThrow(
                        ENTRY_RESTRICTION)),
                    childAuthorizedPickup = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_AUTHORIZED_PICKUP)
                    ),
                    childGender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_GENDER)),
                    childPhoto = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_PHOTO)),
                    childFatherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_EMAIL)),
                    childFatherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_TEL)),
                    childMotherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_EMAIL)),
                    childMotherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_TEL))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun searchStudentsByEmail(email: String): List<Students> {
        val studentList = mutableListOf<Students>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$KEY_CHILD_FATHER_EMAIL LIKE ? OR $KEY_CHILD_MOTHER_EMAIL LIKE ?",
            arrayOf("%$email%", "%$email%"),
            null,
            null,
            "$KEY_CHILD_NAME ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val student = Students(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                    childName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NAME)),
                    childClass = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_CLASS)),
                    childSection = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_SECTION)),
                    childNfcId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NFC_ID)),
                    childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                    childFatherName = cursor.getString(
                        cursor.getColumnIndexOrThrow(KEY_CHILD_FATHER_NAME)
                    ),
                    tripNoPickUp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_PICK_UP)),
                    tripNoDropOff = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_TRIP_NO_DROP_OFF)),
                    childOrEmp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_OR_EMP)),
                    pcrValidityEndDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_PCR_VALIDITY_END_DATE)
                    ),
                    parentNfcId = cursor.getString(cursor.getColumnIndexOrThrow(CHILD_PARENT_NFC_ID)),
                    parentAcNo = cursor.getString(cursor.getColumnIndexOrThrow(PARENT_AC_NO)),
                    handicappedType = cursor.getString(cursor.getColumnIndexOrThrow(HANDICAPPED_TYPE)),
                    suspensionDate = cursor.getString(cursor.getColumnIndexOrThrow(SUSPENSION_DATE)),
                    motherNfc = cursor.getString(cursor.getColumnIndexOrThrow(MOTHER_NFC)),
                    entryRestriction = cursor.getString(cursor.getColumnIndexOrThrow(
                        ENTRY_RESTRICTION)),
                    childAuthorizedPickup = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_AUTHORIZED_PICKUP)
                    ),
                    childGender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_GENDER)),
                    childPhoto = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_PHOTO)),
                    childFatherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_EMAIL)),
                    childFatherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_TEL)),
                    childMotherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_EMAIL)),
                    childMotherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_TEL))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun searchStudentsByNfc(nfcId: String): List<Students> {
        val studentList = mutableListOf<Students>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$KEY_CHILD_NFC_ID = ?",
            arrayOf(nfcId),
            null,
            null,
            "$KEY_CHILD_NAME ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val student = Students(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                    childName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NAME)),
                    childClass = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_CLASS)),
                    childSection = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_SECTION)),
                    childNfcId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_NFC_ID)),
                    childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                    childFatherName = cursor.getString(
                        cursor.getColumnIndexOrThrow(KEY_CHILD_FATHER_NAME)
                    ),
                    tripNoPickUp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRIP_NO_PICK_UP)),
                    tripNoDropOff = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_TRIP_NO_DROP_OFF)),
                    childOrEmp = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_OR_EMP)),
                    pcrValidityEndDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_PCR_VALIDITY_END_DATE)
                    ),
                    parentNfcId = cursor.getString(cursor.getColumnIndexOrThrow(CHILD_PARENT_NFC_ID)),
                    parentAcNo = cursor.getString(cursor.getColumnIndexOrThrow(PARENT_AC_NO)),
                    handicappedType = cursor.getString(cursor.getColumnIndexOrThrow(HANDICAPPED_TYPE)),
                    suspensionDate = cursor.getString(cursor.getColumnIndexOrThrow(SUSPENSION_DATE)),
                    motherNfc = cursor.getString(cursor.getColumnIndexOrThrow(MOTHER_NFC)),
                    entryRestriction = cursor.getString(cursor.getColumnIndexOrThrow(
                        ENTRY_RESTRICTION)),
                    childAuthorizedPickup = cursor.getString(
                        cursor.getColumnIndexOrThrow(CHILD_AUTHORIZED_PICKUP)
                    ),
                    childGender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_GENDER)),
                    childPhoto = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_PHOTO)),
                    childFatherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_EMAIL)),
                    childFatherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_FATHER_TEL)),
                    childMotherEmail = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_EMAIL)),
                    childMotherTel = cursor.getString(cursor.getColumnIndexOrThrow(
                        KEY_CHILD_MOTHER_TEL))
                )
                studentList.add(student)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun getStudentCount(): Int {

        val countQuery = "SELECT  * FROM $TABLE_STUDENTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)

        val rowCount = cursor.count
        db.close()
        cursor.close()

        // return row count
        return rowCount
    }

    fun resetStudentsTable() {

        val db = this.writableDatabase
        // Delete All Rows
        db.delete(TABLE_STUDENTS, null, null)
        db.close()
    }

    fun addBatchPalm(palmList: List<Palm>): Boolean {

        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (palm in palmList) {

                val values = ContentValues().apply {
                    put(KEY_PALM_ID, palm.palmId)
                    put(KEY_PALM_DATA_ONE, palm.palmDataOne)
                    put(KEY_CHILD_ID, palm.childId)
                    put(KEY_CHILD_REG_NO, palm.childRegNo)

                    put(KEY_PALM_DATA_RGB_LEFT, palm.palmDataRgbLeft)
                    put(KEY_PALM_DATA_IR_LEFT, palm.palmDataIrLeft)
                    put(KEY_PALM_DATA_RGB_RIGHT, palm.palmDataRgbRight)
                    put(KEY_PALM_DATA_IR_RIGHT, palm.palmDataIrRight)
                }
                db.insert(TABLE_PALM, null, values)
            }
            db.setTransactionSuccessful()
        } finally {

            try {
                db.endTransaction()
            } catch (e: Throwable) {
                db.close()
            }
        }
        db.close()
        return true
    }

    fun getPalmByRegNo(regNo: String): Palm? {

        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PALM,
            null,
            "$KEY_CHILD_REG_NO = ?",
            arrayOf(regNo),
            null,
            null,
            null,
            "1"
        )
        val palmData = if (cursor.moveToFirst()) {
            Palm(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                palmId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PALM_ID)),
                palmDataOne = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PALM_DATA_ONE)),
                childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),

                palmDataRgbLeft = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_PALM_DATA_RGB_LEFT)),
                palmDataIrLeft = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_PALM_DATA_IR_LEFT)),
                palmDataRgbRight = cursor.getBlob(cursor.getColumnIndexOrThrow(
                    KEY_PALM_DATA_RGB_RIGHT)),
                palmDataIrRight = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_PALM_DATA_IR_RIGHT))
            )
        } else null

        cursor.close()
        db.close()
        return palmData
    }

    fun getPalms(): List<Palm> {

        val palmList = mutableListOf<Palm>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_PALM"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val palm = Palm(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    palmId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PALM_ID)),
                    palmDataOne = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PALM_DATA_ONE)),
                    childId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_ID)),
                    childRegNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHILD_REG_NO)),
                    palmDataRgbLeft = cursor.getBlob(cursor.getColumnIndexOrThrow(
                        KEY_PALM_DATA_RGB_LEFT)),
                    palmDataIrLeft = cursor.getBlob(cursor.getColumnIndexOrThrow(
                        KEY_PALM_DATA_IR_LEFT)),
                    palmDataRgbRight = cursor.getBlob(cursor.getColumnIndexOrThrow(
                        KEY_PALM_DATA_RGB_RIGHT)),
                    palmDataIrRight = cursor.getBlob(cursor.getColumnIndexOrThrow(
                        KEY_PALM_DATA_IR_RIGHT))
                )
                palmList.add(palm)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return palmList
    }

    fun getPalmCount(): Int {

        val countQuery = "SELECT  * FROM $TABLE_PALM"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)

        val rowCount = cursor.count
        db.close()
        cursor.close()

        // return row count
        return rowCount
    }

    fun resetPalmTable() {

        val db = this.writableDatabase
        // Delete All Rows
        db.delete(TABLE_PALM, null, null)
        db.close()
    }

    fun deletePalmByChildId(childId: String?) {

        val db = this.writableDatabase
        db.delete(TABLE_PALM, "$KEY_CHILD_ID = ?", arrayOf(childId))
        db.close()
    }

    fun deletePalmsByChildIds(childIds: List<String?>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (childId in childIds) {
                db.delete(TABLE_PALM, "$KEY_CHILD_ID = ?", arrayOf(childId))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun addBatchAttendance(attendanceList: List<Attendance>): Boolean {

        val db = this.writableDatabase
        db.beginTransaction()

        try {
            for (attendance in attendanceList) {

                val values = ContentValues().apply {
                    put(KEY_LATITUDE, attendance.latitude)
                    put(KEY_LONGITUDE, attendance.longitude)
                    put(KEY_DATATYPE, attendance.dataType)
                    put(KEY_BUS_ID, attendance.busId)
                    put(KEY_TIME_STAMP, attendance.timeStamp)
                    put(KEY_CHILD_ID, attendance.childId)
                    put(KEY_PICK_UP_DROP_OFF, attendance.pickUpDropOff)
                    put(KEY_CHILD_STATUS, attendance.childStatus)
                    put(KEY_CHILD_NFC_ID, attendance.childNfcId)
                    put(KEY_CHILD_FORGOT_CARD, attendance.childForgotCard)
                    put(KEY_CHILD_REG_NO, attendance.childRegNo)
                    put(KEY_TRIP_NO_PICK_UP, attendance.tripNoPickUp)
                    put(KEY_TRIP_NO_DROP_OFF, attendance.tripNoDropOff)
                    put(KEY_CHILD_NAME, attendance.childName)
                    put(KEY_CHILD_OR_EMP, attendance.childOrEmp)
                    put(KEY_CHILD_AUTH_ID, attendance.childAuthId)
                    put(KEY_CHILD_CLASS_AUTH, attendance.childClassAuth)
                    put(KEY_CHILD_SECTION_AUTH, attendance.childSectionAuth)
                }
                db.insert(TABLE_ATTENDANCE, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            try {
                db.endTransaction()
            } catch (e: Throwable) {
                db.close()
            }
        }
        db.close()
        return true
    }

    fun getAllAttendanceDetails(): List<Attendance> {

        val attendanceList = mutableListOf<Attendance>()
        val selectQuery = "SELECT * FROM $TABLE_ATTENDANCE ORDER BY $KEY_ID ASC LIMIT 10"

        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val attendance = Attendance(
                        id = it.getInt(0),
                        latitude = it.getString(1),
                        longitude = it.getString(2),
                        dataType = it.getString(3),
                        busId = it.getString(4),
                        timeStamp = it.getString(5),
                        childId = it.getString(6),
                        pickUpDropOff = it.getString(7),
                        childStatus = it.getString(8),
                        childNfcId = it.getString(9),
                        childForgotCard = it.getString(10),
                        childRegNo = it.getString(11),
                        tripNoPickUp = it.getString(12),
                        tripNoDropOff = it.getString(13),
                        childName = it.getString(14),
                        childOrEmp = it.getString(15),
                        childAuthId = it.getString(16),
                        childClassAuth = it.getString(17),
                        childSectionAuth = it.getString(18)
                    )
                    attendanceList.add(attendance)
                } while (it.moveToNext())
            }
        }
        db.close()
        return attendanceList
    }

    fun deleteAttendanceUpToId(lastId: Int) {
        val db = writableDatabase
        if (lastId != -1) {
            val query = "DELETE FROM $TABLE_ATTENDANCE WHERE $KEY_ID <= $lastId"
            db.execSQL(query)
        }
        db.close()
    }

    fun getAttendanceCount(): Int {

        val countQuery = "SELECT  * FROM $TABLE_ATTENDANCE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)

        val rowCount = cursor.count
        db.close()
        cursor.close()

        // return row count
        return rowCount
    }

    fun resetAttendanceTable() {

        val db = this.writableDatabase
        // Delete All Rows
        db.delete(TABLE_ATTENDANCE, null, null)
        db.close()
    }
}