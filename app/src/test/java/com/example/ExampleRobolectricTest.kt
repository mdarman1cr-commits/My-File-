package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ocr.DocumentOcrEngine
import com.example.data.security.EncryptionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("My File Explorer", appName)
  }

  @Test
  fun `verify aes encryption and decryption`() {
    val plain = "Secret Confidential Data 12345"
    val passphrase = "MasterPassphrase"
    val encrypted = EncryptionManager.encrypt(plain, passphrase)
    val decrypted = EncryptionManager.decrypt(encrypted, passphrase)
    assertEquals(plain, decrypted)
  }

  @Test
  fun `verify ocr classification and auto tagging`() {
    val invoiceText = "TAX INVOICE Total Due: $194.85 Invoice #8492"
    val result = DocumentOcrEngine.analyzeAndExtract(invoiceText)
    assertEquals("Tax Invoice", result.detectedDocType)
    assertTrue(result.autoTags.contains("#Invoice"))
    assertTrue(result.autoTags.contains("#Tax"))
  }
}
