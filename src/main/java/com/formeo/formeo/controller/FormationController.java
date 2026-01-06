package com.formeo.formeo.controller;

import com.formeo.formeo.dto.FormationDto;
import com.formeo.formeo.entity.Categorie;
import com.formeo.formeo.entity.Formation;
import com.formeo.formeo.entity.Role;
import com.formeo.formeo.entity.Utilisateur;
import com.formeo.formeo.mapper.DtoMapper;
import com.formeo.formeo.repository.CategorieRepository;
import com.formeo.formeo.repository.FormationRepository;
import com.formeo.formeo.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/formations")
public class FormationController {

    private final FormationRepository formationRepository;
    private final CategorieRepository categorieRepository;
    private final UtilisateurRepository utilisateurRepository;

    public FormationController(FormationRepository formationRepository,
                               CategorieRepository categorieRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.formationRepository = formationRepository;
        this.categorieRepository = categorieRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // PUBLIC
    @GetMapping
    public List<FormationDto> lister(@RequestParam(required = false) Long categorieId) {
        List<Formation> formations = (categorieId != null)
                ? formationRepository.findByCategorieId(categorieId)
                : formationRepository.findAll();

        return formations.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    // PUBLIC
    @GetMapping("/{id}")
    public FormationDto get(@PathVariable Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));
        return DtoMapper.toDto(formation);
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FormationDto creer(@Valid @RequestBody Formation formation) {
        Long categorieId = formation.getCategorie() != null ? formation.getCategorie().getId() : null;
        if (categorieId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categorie.id est obligatoire");
        }

        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categorie inexistante"));
        formation.setCategorie(categorie);

        // ✅ NOUVEAU : intervenant optionnel
        Long intervenantId = formation.getIntervenant() != null ? formation.getIntervenant().getId() : null;
        if (intervenantId != null) {
            Utilisateur intervenant = utilisateurRepository.findById(intervenantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intervenant inexistant"));

            if (intervenant.getRole() != Role.INTERVENANT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur choisi n'est pas un intervenant");
            }
            formation.setIntervenant(intervenant);
        }

        Formation saved = formationRepository.save(formation);
        return DtoMapper.toDto(saved);
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public FormationDto modifier(@PathVariable Long id, @Valid @RequestBody Formation body) {
        Formation existing = formationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));

        existing.setTitre(body.getTitre());
        existing.setDescription(body.getDescription());
        existing.setPrix(body.getPrix());
        existing.setDureeJours(body.getDureeJours());

        Long categorieId = body.getCategorie() != null ? body.getCategorie().getId() : null;
        if (categorieId != null) {
            Categorie categorie = categorieRepository.findById(categorieId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categorie inexistante"));
            existing.setCategorie(categorie);
        }

        // ✅ mise à jour de l'intervenant
        Long intervenantId = body.getIntervenant() != null ? body.getIntervenant().getId() : null;
        if (intervenantId != null) {
            Utilisateur intervenant = utilisateurRepository.findById(intervenantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intervenant inexistant"));

            if (intervenant.getRole() != Role.INTERVENANT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur choisi n'est pas un intervenant");
            }
            existing.setIntervenant(intervenant);
        } else {
            existing.setIntervenant(null);
        }

        Formation saved = formationRepository.save(existing);
        return DtoMapper.toDto(saved);
    }

    // ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        if (!formationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable");
        }
        formationRepository.deleteById(id);
    }
}
