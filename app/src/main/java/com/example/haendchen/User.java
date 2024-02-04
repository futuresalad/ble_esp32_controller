package com.example.haendchen;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


// Userclass for database
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String password;
}