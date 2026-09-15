# Pass Recognized Student to SearchAdapter

The user wants to explicitly pass the `studentMatched` data to the `SearchAdapter` and dismiss the recognition dialog.

## Proposed Changes

### [SearchAdapter](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/SearchAdapter.kt)

#### [MODIFY] [SearchAdapter.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/SearchAdapter.kt)

I will add a new method `updateData(student: Students)` to the `SearchAdapter` class. This method will clear the existing list, add the recognized student, and notify the observer of the data change.

```kotlin
fun updateData(student: Students) {
    this.alStudents.clear()
    this.alStudents.add(student)
    notifyDataSetChanged()
}
```

### [RecognitionActivity](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionActivity.kt)

#### [MODIFY] [RecognitionActivity.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionActivity.kt)

I will update the success block of the palm recognition to call the new `updateData` method on `searchAdapter` and ensure the dialog is dismissed. I will keep the 2-second delay to allow the success animation to be visible.

```kotlin
mainHandler?.postDelayed({
    if (isFinishing || isDestroyed) return@postDelayed
    nfcStartOrStop(1)
    mCurrentWorkMode = WorkMode.NONE
    stopCapture()
    palmRecognitionDialog.dismiss()
    searchAdapter.updateData(studentMatched) // Explicitly passing the matched student
    viewHideRV(true)
}, 2000)
```

## Verification Plan

### Manual Verification
- Deploy the app.
- Perform a successful palm recognition.
- Verify that:
    1. The recognition dialog closes after 2 seconds.
    2. The main student list (`RecyclerView`) now displays ONLY the recognized student.
    3. The rest of the app state (NFC, Camera) is correctly restored.
