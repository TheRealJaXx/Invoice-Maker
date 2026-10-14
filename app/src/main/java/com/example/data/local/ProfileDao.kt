package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FreelancerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM freelancer_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<FreelancerProfile?>

    @Query("SELECT * FROM freelancer_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): FreelancerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: FreelancerProfile)

    @Update
    suspend fun update(profile: FreelancerProfile)
}
