package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FileExplorerDao {

  @Query("""
    SELECT * FROM file_items 
    WHERE (:parentId IS NULL AND parentFolderId IS NULL OR parentFolderId = :parentId)
      AND isTrash = 0 
      AND isInVault = 0
    ORDER BY isDirectory DESC, name ASC
  """)
  fun getItemsInFolder(parentId: Long?): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE isInVault = 1 AND isTrash = 0 ORDER BY isDirectory DESC, name ASC")
  fun getVaultItems(): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE isTrash = 1 ORDER BY updatedAt DESC")
  fun getTrashItems(): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE isFavorite = 1 AND isTrash = 0 AND isInVault = 0 ORDER BY updatedAt DESC")
  fun getFavoriteItems(): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE category = :category AND isTrash = 0 AND isInVault = 0 ORDER BY updatedAt DESC")
  fun getItemsByCategory(category: String): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE isDirectory = 0 AND isTrash = 0 AND isInVault = 0 ORDER BY updatedAt DESC LIMIT :limit")
  fun getRecentFiles(limit: Int): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE isTrash = 0 AND isInVault = 0")
  fun getAllActiveFiles(): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items")
  suspend fun getAllFilesSync(): List<FileItemEntity>

  @Query("""
    SELECT * FROM file_items 
    WHERE (name LIKE '%' || :query || '%' 
       OR content LIKE '%' || :query || '%' 
       OR ocrTags LIKE '%' || :query || '%')
      AND isTrash = 0
      AND isInVault = 0
    ORDER BY isDirectory DESC, updatedAt DESC
  """)
  fun searchFiles(query: String): Flow<List<FileItemEntity>>

  @Query("SELECT * FROM file_items WHERE id = :id LIMIT 1")
  suspend fun getItemById(id: Long): FileItemEntity?

  @Query("SELECT * FROM file_items WHERE parentFolderId = :folderId")
  suspend fun getChildrenOf(folderId: Long): List<FileItemEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: FileItemEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<FileItemEntity>): List<Long>

  @Update
  suspend fun update(item: FileItemEntity)

  @Delete
  suspend fun delete(item: FileItemEntity)

  @Query("DELETE FROM file_items WHERE isTrash = 1")
  suspend fun emptyTrash()

  @Query("DELETE FROM file_items")
  suspend fun clearAllFiles()

  @Query("DELETE FROM file_items WHERE path = '' AND isInVault = 0")
  suspend fun clearDemoFiles()

  // Backups
  @Query("SELECT * FROM backup_entries ORDER BY createdAt DESC")
  fun getAllBackups(): Flow<List<BackupEntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBackup(entry: BackupEntryEntity): Long

  @Delete
  suspend fun deleteBackup(entry: BackupEntryEntity)

  @Query("SELECT COUNT(*) FROM file_items")
  suspend fun getTotalFileCount(): Int
}
