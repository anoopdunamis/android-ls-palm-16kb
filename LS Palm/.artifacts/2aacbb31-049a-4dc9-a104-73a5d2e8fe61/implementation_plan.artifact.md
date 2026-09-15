# Implementation Plan - Search Students by Name Alike

The goal is to add a search function to `DatabaseHandler.kt` that allows searching for students in the `TABLE_STUDENTS` table where the name partially matches a given string (case-insensitive `LIKE` query).

## Proposed Changes

### [Database Component]

#### [MODIFY] [DatabaseHandler.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/db/DatabaseHandler.kt)

- Add a new function `searchStudentsByName(name: String): List<Students>`.
- Use the `LIKE` operator in the SQL query: `KEY_CHILD_NAME + " LIKE ?"` with `"%name%"` as the argument.
- Iterate through the `Cursor` to populate and return a `List<Students>`.

## Verification Plan

### Manual Verification
- This change adds a database helper method. It can be verified by calling it from an Activity or Fragment where student searching is needed.
- I will ensure the code compiles and correctly maps database columns to the `Students` data class properties, matching the pattern used in `getStudentByRegNo`.
