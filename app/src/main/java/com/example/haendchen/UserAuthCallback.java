package com.example.haendchen;

// Returns if user authentication was successful
interface UserAuthCallback {
    void onResult(boolean success);
}