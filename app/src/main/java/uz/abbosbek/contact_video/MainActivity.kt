package uz.abbosbek.contact_video

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import uz.abbosbek.contact_video.databinding.ActivityMainBinding

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
            if (isWriteContactPermissionEnabled()){
                /** permission already granted, save contact*/
                saveContact()
            }
        }
    }

    private fun saveContact() {

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
                val haveWriteContactPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED /** true if granted, false if not */
                if (haveWriteContactPermission){
                    /** permission granted, save contact*/
                    saveContact()
                }
                else{
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