package com.lopezapp.movilpos.util

import android.content.Context
import android.content.ContextWrapper
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class ImageUtilsTest {

    private fun createTestContext(): Context {
        return object : ContextWrapper(null) {
            override fun getApplicationContext() = this
            override fun getFilesDir(): File {
                val dir = File("build/tmp/test_images_files_dir")
                dir.mkdirs()
                return dir
            }
        }
    }

    @Test
    fun testUriToBase64_blankUri_returnsNull() {
        val result = ImageUtils.uriToBase64(createTestContext(), "")
        assertNull(result)
    }

    @Test
    fun testBase64ToInternalFileUri_blankData_returnsNull() {
        val result = ImageUtils.base64ToInternalFileUri(createTestContext(), "", "test.jpg")
        assertNull(result)
    }

    @Test
    fun testProductSerializationWithImageUri() {
        val product = Product(
            id = "prod-100",
            name = "Test Product",
            price = 10.0,
            stock = 5,
            imageUri = "content://media/external/images/media/999"
        )
        // Without context (or null context) imageUri is preserved
        val serialized = FirebaseSyncService.serializeProduct(product, null)
        assertEquals("content://media/external/images/media/999", serialized["imageUri"])

        val deserialized = FirebaseSyncService.deserializeProduct(serialized, null)
        assertEquals("prod-100", deserialized.id)
        assertEquals("content://media/external/images/media/999", deserialized.imageUri)
    }

    @Test
    fun testBusinessInfoSerializationWithLogoUri() {
        val businessInfo = BusinessInfo(
            name = "Test Shop",
            logoUri = "content://media/external/images/media/888"
        )
        val serialized = FirebaseSyncService.serializeBusinessInfo(businessInfo, null)
        assertEquals("content://media/external/images/media/888", serialized["logoUri"])

        val deserialized = FirebaseSyncService.deserializeBusinessInfo(serialized, null)
        assertEquals("Test Shop", deserialized.name)
        assertEquals("content://media/external/images/media/888", deserialized.logoUri)
    }

    @Test
    fun testGetModel_nullOrBlank_returnsNull() {
        assertNull(ImageUtils.getModel(null))
        assertNull(ImageUtils.getModel(""))
        assertNull(ImageUtils.getModel("   "))
    }

    @Test
    fun testGetModel_fileUri_returnsFile() {
        val model = ImageUtils.getModel("file:///data/user/0/com.lopezapp.movilpos/files/app_images/products/img_123.jpg")
        org.junit.Assert.assertTrue(model is File)
        assertEquals(File("/data/user/0/com.lopezapp.movilpos/files/app_images/products/img_123.jpg"), model)
    }

    @Test
    fun testGetModel_otherUri_returnsStringOrUri() {
        val model = ImageUtils.getModel("content://media/external/images/media/123")
        org.junit.Assert.assertNotNull(model)
    }
}
