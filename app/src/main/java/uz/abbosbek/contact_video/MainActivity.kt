package uz.abbosbek.contact_video

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import uz.abbosbek.contact_video.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var TAG = "CONTACT_ADD_TAG"
    private lateinit var contactPermission: Array<String>
    private val WRITE_CONTACT_PERMISSION_CODE = 100

    /** image pick (gallery) intent contact (for contact image)*/
    private val IMAGE_PICK_GALLERY_CODE = 200

    /** image uri, picked image uri will be in this var*/
    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        contactPermission = arrayOf(Manifest.permission.WRITE_CONTACTS)

        binding.profileIv.setOnClickListener {
            openGalleryIntent()
        }

        binding.saveFab.setOnClickListener {
            /** first check is permission allowed or not */
            if (isWriteContactPermissionEnabled()) {
                /** permission already granted, save contact*/
                saveContact()
            } else {
                /** permission was not granted, save contact*/
                requestWriteCintactPermission()
            }
        }
    }

    private fun saveContact() {
        Log.d(TAG, "sevContact: ")
        /** input data */
        val firstName = binding.firstNameEt.text.toString().trim()
        val lastName = binding.lastNameEt.text.toString().trim()
        val phoneMobile = binding.phoneMobileEt.text.toString().trim()
        val phoneHome = binding.phoneHomeEt.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val address = binding.addressEt.text.toString().trim()

        Log.d(TAG, "saveContact: First Name $firstName")
        Log.d(TAG, "saveContact: Last Name $lastName")
        Log.d(TAG, "saveContact: Phone Mobile  $phoneMobile")
        Log.d(TAG, "saveContact: Phone Home $phoneHome")
        Log.d(TAG, "saveContact: Email $email")
        Log.d(TAG, "saveContact: Address $address")

        /** init arraylist of object ContentProvideOperation */
        val cpo = ArrayList<ContentProviderOperation>()

        /** contact id */
        val rawContactId = cpo.size
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        /** Add first name, last name */
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                .build()
        )

        /** add phone number (Mobile) */
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneMobile)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )

        /** add phone number (home) */
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneHome)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                )
                .build()
        )

        /** add email */
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                .withValue(
                    ContactsContract.CommonDataKinds.Email.TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                )
                .build()
        )

        /** add address */
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.DATA, address)
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
                )
                .build()
        )

        /** get image as bytes array to save as contact image */
        val imageBytes = imageUriToBytes()
        if (imageBytes != null) {
            /** contact with image */
            Log.d(TAG, "saveContact: contact with image ")
            /** add image */
            cpo.add(
                ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI
                )
                    .withValue(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                    .build()
            )

        } else {
            /** contact without image */
            Log.d(TAG, "saveContact: contact without image ")
        }

        /** save contact */
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, cpo)
            Log.d(TAG, "saveContact: saved")
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.d(TAG, "saveContact: failed to save due to ${e.message}")
            Toast.makeText(this, "failed to save due to ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun imageUriToBytes(): ByteArray? {
        val bitmap: Bitmap
        val baos: ByteArrayOutputStream

        return try {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, image_uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, image_uri!!)
                bitmap = ImageDecoder.decodeBitmap(source)
            }
            baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            Log.d(TAG, "imageUriToBytes: ${e.message}")
            null
        }
    }

    private fun isWriteContactPermissionEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteCintactPermission() {
        ActivityCompat.requestPermissions(this, contactPermission, WRITE_CONTACT_PERMISSION_CODE)
    }

    private fun openGalleryIntent() {
        /** intent to pick image from gallery */
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /** handle permission request result */
        if (grantResults.isNotEmpty()) {
            if (requestCode == WRITE_CONTACT_PERMISSION_CODE) {
                val haveWriteContactPermission =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                /** true if granted, false if not */
                if (haveWriteContactPermission) {
                    /** permission granted, save contact*/
                    saveContact()
                } else {
                    /**permission denied, can't save contact */
                    Toast.makeText(this, "Permission dened", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /** handle image pick result */
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                /** image picked, get eri of image */
                image_uri = data!!.data
                /** set to image view */
                binding.profileIv.setImageURI(image_uri)
            }
        } else {
            /** canclled */
            Toast.makeText(this, "Canclled", Toast.LENGTH_SHORT).show()

        }
    }
}