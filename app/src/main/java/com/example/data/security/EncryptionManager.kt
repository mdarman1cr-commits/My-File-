package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {

  private const val PREFS_NAME = "mfe_security_prefs"
  private const val KEY_PIN_HASH = "master_pin_hash"
  private const val KEY_PIN_SALT = "master_pin_salt"
  private const val KEY_VAULT_TIMEOUT = "vault_auto_lock_timeout"

  private const val DEFAULT_PIN = "1234"
  private const val ITERATIONS = 10000
  private const val KEY_LENGTH = 256
  private const val GCM_TAG_LENGTH = 128
  private const val IV_LENGTH = 12

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun initializePinIfMissing(context: Context) {
    val prefs = getPrefs(context)
    if (!prefs.contains(KEY_PIN_HASH)) {
      setMasterPin(context, DEFAULT_PIN)
    }
  }

  fun verifyPin(context: Context, enteredPin: String): Boolean {
    val prefs = getPrefs(context)
    val saltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return enteredPin == DEFAULT_PIN
    val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    val calculatedHash = hashWithSalt(enteredPin, salt)
    return calculatedHash == storedHash
  }

  fun setMasterPin(context: Context, newPin: String) {
    val prefs = getPrefs(context)
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    val hash = hashWithSalt(newPin, salt)

    prefs.edit()
      .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
      .putString(KEY_PIN_HASH, hash)
      .apply()
  }

  private fun hashWithSalt(pin: String, salt: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(salt)
    val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
  }

  // Derive AES key using PBKDF2
  private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
    val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val keyBytes = factory.generateSecret(spec).encoded
    return SecretKeySpec(keyBytes, "AES")
  }

  /**
   * Encrypts plaintext using AES-256-GCM with custom passphrase or master key.
   * Format: salt(16) + iv(12) + ciphertext
   */
  fun encrypt(plainText: String, passphrase: String = "MyFileExplorerMasterKey2026"): String {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)

    val iv = ByteArray(IV_LENGTH)
    random.nextBytes(iv)

    val secretKey = deriveKey(passphrase, salt)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

    val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

    // Combined byte buffer: salt + iv + ciphertext
    val combined = ByteArray(salt.size + iv.size + cipherBytes.size)
    System.arraycopy(salt, 0, combined, 0, salt.size)
    System.arraycopy(iv, 0, combined, salt.size, iv.size)
    System.arraycopy(cipherBytes, 0, combined, salt.size + iv.size, cipherBytes.size)

    return Base64.encodeToString(combined, Base64.NO_WRAP)
  }

  /**
   * Decrypts ciphertext using AES-256-GCM.
   */
  fun decrypt(encryptedBase64: String, passphrase: String = "MyFileExplorerMasterKey2026"): String? {
    return try {
      val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
      if (combined.size < 16 + IV_LENGTH) return null

      val salt = ByteArray(16)
      val iv = ByteArray(IV_LENGTH)
      val cipherBytes = ByteArray(combined.size - 16 - IV_LENGTH)

      System.arraycopy(combined, 0, salt, 0, 16)
      System.arraycopy(combined, 16, iv, 0, IV_LENGTH)
      System.arraycopy(combined, 16 + IV_LENGTH, cipherBytes, 0, cipherBytes.size)

      val secretKey = deriveKey(passphrase, salt)
      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

      val decrypted = cipher.doFinal(cipherBytes)
      String(decrypted, Charsets.UTF_8)
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Computes SHA-256 checksum hex string.
   */
  fun calculateSha256(content: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(content.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
  }
}
