package com.elima.installment_management.util

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.elima.installment_management.data.AppDatabase
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupManager {
    private const val DATABASE_NAME = "loan_database"

    fun exportDatabase(context: Context, destinationUri: Uri): Boolean {
        return try {
            val db = AppDatabase.getDatabase(context)
            try {
                db.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).close()
            } catch (e: Exception) {
            }
            
            AppDatabase.destroyInstance()

            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) return false

            val inputStream = FileInputStream(dbFile)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(context: Context, sourceUri: Uri): Boolean {
        return try {
            AppDatabase.destroyInstance()
            
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            val walFile = context.getDatabasePath("$DATABASE_NAME-wal")
            val shmFile = context.getDatabasePath("$DATABASE_NAME-shm")

            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
