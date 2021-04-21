package com.lepu.anxin.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
public interface UserDao {

    @Query("SELECT * FROM user_table LIMIT 1")
    fun findOrNull(): UserInfoRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userInfoRoom: UserInfoRoom)

    @Query("DELETE FROM user_table")
    fun deleteAll()
}