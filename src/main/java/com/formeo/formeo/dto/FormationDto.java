package com.formeo.formeo.dto;

import java.math.BigDecimal;

public class FormationDto {
    public Long id;
    public String titre;
    public String description;
    public BigDecimal prix;
    public int dureeJours;

    public CategorieDto categorie;

    public Long intervenantId;
    public String intervenantNom;
    public String intervenantPrenom;

    public FormationDto(Long id, String titre, String description, BigDecimal prix, int dureeJours,
                        CategorieDto categorie,
                        Long intervenantId,
                        String intervenantNom,
                        String intervenantPrenom) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.dureeJours = dureeJours;
        this.categorie = categorie;
        this.intervenantId = intervenantId;
        this.intervenantNom = intervenantNom;
        this.intervenantPrenom = intervenantPrenom;
    }
}
