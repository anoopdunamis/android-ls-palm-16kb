# Walkthrough - Search Students by Name Alike

I have implemented a new function in `DatabaseHandler.kt` to allow searching for students by their name using a partial match (`LIKE` query).

## Changes Made

### [Database Component]

#### [DatabaseHandler.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/db/DatabaseHandler.kt)

- Added `searchStudentsByName(name: String): List<Students>`:
    - Performs a `SELECT` query on `STUDENTS_TABLE`.
    - Uses `$KEY_CHILD_NAME LIKE ?` with `"%name%"` to find matches.
    - Results are ordered by `child_name` in ascending order.
    - Returns a `List<Students>` containing all matching records.

```kotlin
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
        // ... cursor iteration ...
        return studentList
    }
```

## Verification Results

### Automated Tests
- Ran `analyze_file` on `DatabaseHandler.kt` to ensure no syntax errors were introduced.
- Verified that the mapping of columns to the `Students` object matches existing patterns in the file.
