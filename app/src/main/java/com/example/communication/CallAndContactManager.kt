package com.example.communication

import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceContact(
    val id: String,
    val name: String,
    val phoneNumber: String
)

/**
 * Two-Way Outgoing/Incoming Call & Contact Lifecycle Manager.
 * Operates on device ContactsContract ContentProvider & Telecom Intents.
 * Cross-platform adaptable abstraction layer for telephony & contact store operations.
 */
class CallAndContactManager(private val context: Context) {

    /**
     * Make an outgoing phone call directly using ACTION_CALL or ACTION_DIAL.
     */
    fun makePhoneCall(phoneNumber: String, directCall: Boolean = true): Boolean {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intentAction = if (directCall) Intent.ACTION_CALL else Intent.ACTION_DIAL
            val intent = Intent(intentAction, Uri.parse("tel:$cleanNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("CallAndContactManager", "Failed to initiate call: ${e.message}")
            // Fallback to ACTION_DIAL if ACTION_CALL lacks runtime permission
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    /**
     * Search contacts matching name query from device ContactsContract.
     */
    suspend fun searchContacts(query: String): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<DeviceContact>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = if (idIndex >= 0) it.getString(idIndex) else ""
                val name = if (nameIndex >= 0) it.getString(nameIndex) else ""
                val number = if (numIndex >= 0) it.getString(numIndex) else ""
                contacts.add(DeviceContact(id, name, number))
            }
        }
        return@withContext contacts
    }

    /**
     * Add a new contact into device ContactsStore using ContentProvider Operations.
     */
    suspend fun addNewContact(name: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Add Name
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            // Add Phone Number
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            return@withContext true
        } catch (e: Exception) {
            Log.e("CallAndContactManager", "Error adding contact: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Delete contact by name or phone from device store.
     */
    suspend fun deleteContactByName(name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(name))
            val cursor = resolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.LOOKUP_KEY), null, null, null)

            var deleted = false
            cursor?.use {
                while (it.moveToNext()) {
                    val lookupKey = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.LOOKUP_KEY))
                    val contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
                    resolver.delete(contactUri, null, null)
                    deleted = true
                }
            }
            return@withContext deleted
        } catch (e: Exception) {
            Log.e("CallAndContactManager", "Error deleting contact: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Answer incoming call autonomously via AccessibilityService / Telecom.
     */
    fun answerIncomingCallAutonomously(): Boolean {
        val service = AssistantAccessibilityService.getInstance() ?: return false
        // Search and click "Answer" or "Accept" button on incoming call screen with natural pacing
        return service.findAndClickText("Answer") || service.findAndClickText("Accept") || service.findAndClickText("Swipe up to answer")
    }
}
