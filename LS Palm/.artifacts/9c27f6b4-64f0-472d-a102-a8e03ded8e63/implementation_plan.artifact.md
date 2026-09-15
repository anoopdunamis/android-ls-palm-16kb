# Implementation Plan - Palm Duplicate Check during Registration

The goal is to prevent registering a palm if it is already assigned to another person in the system. If the palm belongs to the same person, the registration/update should proceed.

## User Review Required

> [!IMPORTANT]
> The duplicate check uses a score threshold of `0.75`, which is consistent with the existing `performRecognition` logic.

## Proposed Changes

### Recognition Activity

#### [MODIFY] [RecognitionActivity.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionActivity.kt)

- **Member Variable**: Ensure `currentChildId` is correctly used to track the student being registered.
- **`studentTapped`**: Set `currentChildId` when a student is selected.
- **`startRegisterFlow`**:
    - Iterate through `mPalmCache` to find matches using `device.compareFeatureScore`.
    - If a match is found (`irScore > 0.75`):
        - Check if `matchedPalm.childId != currentChildId`.
        - If it belongs to someone else:
            - Show an `AlertDialog` with the person's name.
            - Update `addPalmBinding.tvInfoMsg.text` with an error message.
            - Play a failure sound using `kotlinStatic.playPalmFailure()`.
            - Stop the registration flow (`return@Runnable`).
        - If it belongs to the same person, continue (allowing update).

## Verification Plan

### Automated Tests
- I will run a build to ensure no syntax errors were introduced.

### Manual Verification
1. Open the Registration Dialog for Student A.
2. Capture a palm that is already registered to Student B.
3. Verify that an "Already Registered" alert appears and registration stops.
4. Capture a palm that is already registered to Student A.
5. Verify that it proceeds with capture (allowing update).
6. Capture a new palm.
7. Verify that it proceeds with capture.
