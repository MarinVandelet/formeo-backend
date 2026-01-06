package com.formeo.formeo.dto;

public class CategorieDto {
    public Long id;
    public String nom;
    public String description;

    public CategorieDto(Long id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }
}
