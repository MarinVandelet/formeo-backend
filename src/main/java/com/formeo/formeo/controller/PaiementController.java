package com.formeo.formeo.controller;

import com.formeo.formeo.dto.InscriptionDto;
import com.formeo.formeo.entity.Inscription;
import com.formeo.formeo.mapper.InscriptionMapper;
import com.formeo.formeo.security.CustomUserDetails;
import com.formeo.formeo.service.PaiementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paiement")
public class PaiementController {

    private final PaiementService paiementService;
    private final String frontendBaseUrl;

    public PaiementController(PaiementService paiementService,
                              @Value("${frontend.base-url}") String frontendBaseUrl) {
        this.paiementService = paiementService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUtilisateur().getId();
        }
        throw new IllegalStateException("Utilisateur non authentifie correctement");
    }

    // 1) Création de la session Stripe Checkout
    @PostMapping("/checkout/{inscriptionId}")
    public Map<String, String> createCheckoutSession(Authentication authentication,
                                                     @PathVariable Long inscriptionId) {
        Long userId = getUserId(authentication);

        String successUrl = frontendBaseUrl + "/paiement/success";
        String cancelUrl = frontendBaseUrl + "/profil?payment=cancel";

        PaiementService.CheckoutSessionInfo info = paiementService.createCheckoutSession(
                userId, inscriptionId, successUrl, cancelUrl
        );

        Map<String, String> response = new HashMap<>();
        response.put("sessionId", info.sessionId());
        response.put("url", info.url());
        return response;
    }

    // 2) Confirmation du paiement (appelée par le front APRES retour Stripe)
    @PostMapping("/confirm/{inscriptionId}")
    @ResponseStatus(HttpStatus.OK)
    public InscriptionDto confirmer(Authentication authentication,
                                    @PathVariable Long inscriptionId) {
        Long userId = getUserId(authentication);
        Inscription inscription = paiementService.marquerInscriptionPayee(userId, inscriptionId);
        return InscriptionMapper.toDto(inscription);
    }
}
