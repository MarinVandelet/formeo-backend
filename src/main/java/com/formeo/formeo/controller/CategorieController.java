package com.formeo.formeo.controller;

import com.formeo.formeo.dto.CategorieDto;
import com.formeo.formeo.entity.Categorie;
import com.formeo.formeo.mapper.DtoMapper;
import com.formeo.formeo.repository.CategorieRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieRepository categorieRepository;

    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    // PUBLIC
    @GetMapping
    public List<CategorieDto> lister() {
        return categorieRepository.findAll()
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    // PUBLIC
    @GetMapping("/{id}")
    public CategorieDto get(@PathVariable Long id) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie introuvable"));
        return DtoMapper.toDto(categorie);
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieDto creer(@Valid @RequestBody Categorie categorie) {
        categorieRepository.findByNomIgnoreCase(categorie.getNom()).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une categorie avec ce nom existe deja");
        });

        Categorie saved = categorieRepository.save(categorie);
        return DtoMapper.toDto(saved);
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategorieDto modifier(@PathVariable Long id, @Valid @RequestBody Categorie body) {
        Categorie existing = categorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie introuvable"));

        existing.setNom(body.getNom());
        existing.setDescription(body.getDescription());

        Categorie saved = categorieRepository.save(existing);
        return DtoMapper.toDto(saved);
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie introuvable");
        }
        categorieRepository.deleteById(id);
    }
}