# Walkthrough - Student Contact Fields Integration

I have successfully added four new contact fields to the student database and integrated them into the API synchronization logic.

## Changes Made

### Configuration
- **[URLS.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/statics/URLS.kt)**: Incremented `DATABASE_VERSION` to `2`.

### Data Model
- **[Students.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/db/Students.kt)**: Added `childFatherEmail`, `childFatherTel`, `childMotherEmail`, and `childMotherTel` properties.

### Database
- **[DatabaseHandler.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/db/DatabaseHandler.kt)**:
    - Added constants for the new database keys.
    - Updated `CREATE_TABLE_STUDENTS` with the new columns.
    - Modified `onUpgrade` to recreate the database when the version changes.
    - Updated `addBatchStudents` to save the new fields.
    - Updated `getStudentByRegNo` and `searchStudentsByName` to retrieve the new fields.

### API / Presentation
- **[RecognitionPresenter.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionPresenter.kt)**:
    - Updated `getAttendanceDetails` to parse the new fields from the JSON API response and populate the `Students` objects.

## Verification

> [!NOTE]
> Database migration is handled by incrementing the version, which will drop and recreate the tables. This is suitable for this app's current state as students are synced from the server.

- Verified that all field names match the requirements:
    1. `child_father_email_id`
    2. `child_father_tel`
    3. `child_mother_email_id`
    4. `child_mother_tel`
