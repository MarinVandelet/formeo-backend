package com.formeo.formeo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "utilisateur",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_utilisateur_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_utilisateur_pseudo", columnNames = {"pseudo"})
        })
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String nom;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String prenom;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String pseudo;

    @JsonIgnore
    @NotBlank
    @Column(nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(length = 30)
    private String telephone;

    @Column(length = 150)
    private String entreprise;

    @Column(length = 500)
    private String adressePostale;
    public Utilisateur() {}

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }

    public String getAdressePostale() { return adressePostale; }
    public void setAdressePostale(String adressePostale) { this.adressePostale = adressePostale; }
}
