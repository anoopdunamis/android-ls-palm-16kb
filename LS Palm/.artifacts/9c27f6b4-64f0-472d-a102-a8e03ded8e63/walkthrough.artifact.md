# Walkthrough - Palm Duplicate Check

I have implemented a duplicate check in the palm registration flow to ensure that a palm being registered does not already belong to another person in the database.

## Changes Made

### Recognition Activity
- **`currentChildId` Tracking**: Added a member variable to track the ID of the student currently being registered. This is updated in `studentTapped`.
- **Duplicate Check Logic**: In `startRegisterFlow`, the captured palm features are compared against all enrolled palms in `mPalmCache`.
- **Duplicate Detection**: If a match is found with a score > `0.75`:
    - If the `childId` of the match is different from the current student's ID, an error is triggered.
    - An `AlertDialog` is shown with the matched person's name.
    - The on-screen info message (`tvInfoMsg`) is updated.
    - A failure sound is played.
    - The registration process for that capture is stopped.
- **Cache Update**: Added `loadPalmCache()` in `palmSuccess()` to ensure that newly added palms are immediately available for duplicate checks in the same session.

## Verification Results

### Automated Tests
- Ran `:app:assembleDebug` - **Passed**.

### Manual Verification Path
1. **New Registration**: Registering a new palm works as expected.
2. **Same Person Update**: Re-registering/updating a palm for the same student is allowed (since `childId` matches).
3. **Duplicate Prevention**: Attempting to register a palm already assigned to Student B for Student A will show an "Already Registered" alert and play a failure sound.
