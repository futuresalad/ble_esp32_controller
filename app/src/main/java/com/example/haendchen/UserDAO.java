package com.example.haendchen;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// Access object for users in database
@Dao
public interface UserDAO {
    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User getUser(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findUserByUsername(String username);

}