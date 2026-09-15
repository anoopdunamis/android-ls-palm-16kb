# Implementation Plan - Add Student Contact Fields

This plan outlines the changes required to add four new contact fields (`child_father_email_id`, `child_father_tel`, `child_mother_email_id`, `child_mother_tel`) to the student database and handle them in the API synchronization process.

## Proposed Changes

### Configuration
#### [MODIFY] [URLS.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/statics/URLS.kt)
- Increment `DATABASE_VERSION` from `1` to `2` to trigger a database upgrade and recreate the students table with the new schema.

### Data Model
#### [MODIFY] [Students.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/db/Students.kt)
- Add four new fields to the `Students` data class:
    - `childFatherEmail: String?`
    - `childFatherTel: String?`
    - `childMotherEmail: String?`
    - `childMotherTel: String?`

### Database
#### [MODIFY] [DatabaseHandler.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/db/DatabaseHandler.kt)
- Add constants for the new keys:
    - `KEY_CHILD_FATHER_EMAIL = "child_father_email_id"`
    - `KEY_CHILD_FATHER_TEL = "child_father_tel"`
    - `KEY_CHILD_MOTHER_EMAIL = "child_mother_email_id"`
    - `KEY_CHILD_MOTHER_TEL = "child_mother_tel"`
- Update `CREATE_TABLE_STUDENTS` to include these four new `VARCHAR` columns.
- Update `onUpgrade` to call `onCreate(db)` after dropping tables, ensuring they are recreated.
- Update `addBatchStudents` to include these fields in `ContentValues`.
- Update `getStudentByRegNo` and `searchStudentsByName` to retrieve these fields from the database `Cursor` and include them in the `Students` object.

### Presentation / API
#### [MODIFY] [RecognitionPresenter.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionPresenter.kt)
- Update `getAttendanceDetails` to extract the four new fields from the JSON response using `getSafe` and pass them to the `Students` constructor.
    - JSON keys assumed: `child_father_email_id`, `child_father_tel`, `child_mother_email_id`, `child_mother_tel`.

## Verification Plan

### Automated Tests
- Since there are no existing unit tests visible for database operations, I will perform manual verification of the build and code structure.
- I will check if `DatabaseHandler` correctly maps the new fields in `addBatchStudents` and retrieval methods.

### Manual Verification
- Deploy the app to a device/emulator.
- Trigger the student list update (which calls `getAttendanceDetails`).
- Verify (if possible via logs or debugger) that the new fields are being saved and retrieved correctly.
