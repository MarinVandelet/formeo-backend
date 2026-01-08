package com.formeo.formeo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Min(1)
    @Max(12)
    @Column(nullable = false)
    private int capacite = 12;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String ville;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String adresse;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String salle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;

    public Session() {}

    public Long getId() { return id; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public Formation getFormation() { return formation; }
    public void setFormation(Formation formation) { this.formation = formation; }
}
