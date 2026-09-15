# Handle Null Values in GetPalmBatch Response

The `get_palm_batch` API response contains `null` values (both literal `null` and string `"null"`) for various fields. The current parsing logic in `RecognitionPresenter.kt` does not handle these cases, leading to potential `JSONException` or incorrect data (e.g., empty strings or `"null"` strings instead of `null` values).

## Proposed Changes

### [RecognitionPresenter.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionPresenter.kt)

#### [MODIFY] [RecognitionPresenter.kt](file:///home/dunamis/Documents/0_D/android-ls-palm/LS Palm/app/src/main/java/com/dunamis/world/lspalm/recog/RecognitionPresenter.kt)

1.  Update the `jsonArrayToByteArray` lambda to check for empty strings, `"null"`, and `"[]"`, returning `null` in those cases.
2.  Add a helper lambda `optStringOrNull` to safely extract strings from `JSONObject`, returning `null` if the value is missing, literal `null`, or the string `"null"`.
3.  Update the `Palm` object instantiation to use these helpers for all relevant fields.

## Verification Plan

### Automated Tests
- Build the project to ensure no syntax errors.

### Manual Verification
- Deploy the app and trigger the palm batch download.
- Verify that the app handles responses with `null` or `"null"` values without crashing.
- Inspect the database (if possible) or app behavior to ensure `null` values are correctly stored and handled.
