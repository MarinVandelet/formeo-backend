package com.formeo.formeo.dto;

public class LoginResponse {
    public Long utilisateurId;
    public String email;
    public String pseudo;
    public String role;
    public String token;

    public LoginResponse(Long utilisateurId, String email, String pseudo, String role, String token) {
        this.utilisateurId = utilisateurId;
        this.email = email;
        this.pseudo = pseudo;
        this.role = role;
        this.token = token;
    }
}
