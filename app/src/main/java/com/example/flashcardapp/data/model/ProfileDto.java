package com.example.flashcardapp.data.model;

public class ProfileDto {
    public final String id;
    public final String name;
    public final String email;
    public final String dob;
    public final String gender;

    public ProfileDto(String id, String name, String email, String dob, String gender) {
        this.id = id; this.name = name; this.email = email;
        this.dob = dob != null ? dob : "";
        this.gender = gender != null ? gender : "";
    }
}
