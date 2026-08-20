package com.example.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptedFileManager {
    private const val TAG = "EncryptedFileManager"
    private const val KEY_ALIAS = "IntruderPhotoKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    // In-memory cache for decrypted thumbnails (max 15 bitmaps or ~15MB)
    private val bitmapCache = object : LruCache<String, Bitmap>(15 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun trimMemory(level: Int) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            bitmapCache.evictAll()
        } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            bitmapCache.trimToSize(7 * 1024 * 1024)
        }
    }

    fun clearCache() {
        bitmapCache.evictAll()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts plain photo file using AES-256 GCM and securely shreds the unencrypted source file.
     * Returns the encrypted File (.enc).
     */
    fun encryptAndShredSource(plainFile: File): File {
        if (!plainFile.exists() || plainFile.length() == 0L) {
            return plainFile
        }

        try {
            val plainBytes = plainFile.readBytes()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainBytes)

            val encFile = File(plainFile.parentFile, "${plainFile.nameWithoutExtension}.enc")
            FileOutputStream(encFile).use { fos ->
                fos.write(iv.size)
                fos.write(iv)
                fos.write(encryptedBytes)
            }

            // Securely shred unencrypted raw camera file
            shredAndDeleteFile(plainFile)
            Log.d(TAG, "Successfully encrypted intruder photo to AES-256: ${encFile.absolutePath}")
            return encFile
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed, falling back to plain file", e)
            return plainFile
        }
    }

    /**
     * Decrypts an encrypted file directly into an in-memory Bitmap with downsampling.
     * Decrypted bytes never touch physical disk storage.
     */
    fun decryptFileToBitmap(file: File, maxDimension: Int = 1080): Bitmap? {
        var activeFile = file
        if (!activeFile.exists()) {
            val altPath = if (file.name.endsWith(".enc")) {
                file.absolutePath.removeSuffix(".enc") + ".jpg"
            } else {
                file.absolutePath.substringBeforeLast(".") + ".enc"
            }
            val altFile = File(altPath)
            if (altFile.exists()) {
                activeFile = altFile
            } else {
                return null
            }
        }

        val cacheKey = "${activeFile.absolutePath}_$maxDimension"
        val cached = bitmapCache.get(cacheKey)
        if (cached != null && !cached.isRecycled) {
            return cached
        }

        // If it's an unencrypted .jpg file
        if (!activeFile.name.endsWith(".enc")) {
            val bmp = try {
                if (maxDimension > 0) {
                    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(activeFile.absolutePath, boundsOpts)
                    val maxSide = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
                    var sampleSize = 1
                    while (maxSide / sampleSize > maxDimension) {
                        sampleSize *= 2
                    }
                    val decodeOpts = BitmapFactory.Options().apply { 
                        inSampleSize = sampleSize 
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inMutable = false
                    }
                    BitmapFactory.decodeFile(activeFile.absolutePath, decodeOpts)
                } else {
                    BitmapFactory.decodeFile(activeFile.absolutePath)
                }
            } catch (e: Exception) {
                null
            }
            if (bmp != null) bitmapCache.put(cacheKey, bmp)
            return bmp
        }

        val decodedBmp = try {
            val bytes = activeFile.readBytes()
            if (bytes.isEmpty()) return null

            val ivSize = bytes[0].toInt()
            if (bytes.size < 1 + ivSize) return null

            val iv = bytes.copyOfRange(1, 1 + ivSize)
            val cipherText = bytes.copyOfRange(1 + ivSize, bytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), gcmSpec)

            val plainBytes = cipher.doFinal(cipherText)

            if (maxDimension > 0) {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(plainBytes, 0, plainBytes.size, boundsOpts)
                val maxSide = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
                var sampleSize = 1
                while (maxSide / sampleSize > maxDimension) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { 
                    inSampleSize = sampleSize 
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inMutable = false
                }
                BitmapFactory.decodeByteArray(plainBytes, 0, plainBytes.size, decodeOpts)
            } else {
                BitmapFactory.decodeByteArray(plainBytes, 0, plainBytes.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt photo: ${file.name}", e)
            null
        }

        if (decodedBmp != null) {
            bitmapCache.put(cacheKey, decodedBmp)
        }
        return decodedBmp
    }

    /**
     * Securely shreds a file by overwriting disk sectors with random garbage, then zeroes,
     * flushing the channel before deleting it.
     */
    fun shredAndDeleteFile(file: File?): Boolean {
        if (file == null || !file.exists()) return true

        try {
            bitmapCache.remove("${file.absolutePath}_1080")
            bitmapCache.remove("${file.absolutePath}_0")
            bitmapCache.remove("${file.absolutePath}_480")
            val length = file.length()
            if (length > 0) {
                RandomAccessFile(file, "rws").use { raf ->
                    val bufferSize = minOf(length.toInt(), 8192)
                    val buffer = ByteArray(bufferSize)
                    val random = SecureRandom()

                    // Pass 1: Overwrite with random cryptographically secure byte garbage
                    var written = 0L
                    while (written < length) {
                        random.nextBytes(buffer)
                        val toWrite = minOf(buffer.size.toLong(), length - written).toInt()
                        raf.write(buffer, 0, toWrite)
                        written += toWrite
                    }
                    raf.channel.force(true)

                    // Pass 2: Overwrite with zeroes
                    raf.seek(0)
                    Arrays.fill(buffer, 0.toByte())
                    written = 0L
                    while (written < length) {
                        val toWrite = minOf(buffer.size.toLong(), length - written).toInt()
                        raf.write(buffer, 0, toWrite)
                        written += toWrite
                    }
                    raf.channel.force(true)
                    raf.setLength(0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error shredding file ${file.name}", e)
        } finally {
            val deleted = file.delete()
            Log.d(TAG, "Shredded and deleted file: ${file.name} (success=$deleted)")
            return deleted
        }
    }

    /**
     * Decrypts encrypted file to temporary cache for user-initiated sharing.
     */
    fun getDecryptedTempFileForShare(context: Context, file: File): File? {
        val bitmap = decryptFileToBitmap(file, maxDimension = 0) ?: return null
        return try {
            val cacheDir = File(context.cacheDir, "shared_intruders").apply { if (!exists()) mkdirs() }
            val tempFile = File(cacheDir, "shared_snapshot_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            }
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error generating temp share file", e)
            null
        }
    }
}
