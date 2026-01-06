package com.formeo.formeo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank public String nom;
    @NotBlank public String prenom;
    @Email @NotBlank public String email;

    @NotBlank public String pseudo;
    @NotBlank public String motDePasse;

    public String telephone;
    public String entreprise;
    public String adressePostale;
}
