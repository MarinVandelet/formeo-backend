package com.formeo.formeo.service;

import com.formeo.formeo.entity.*;
import com.formeo.formeo.repository.InscriptionRepository;
import com.formeo.formeo.repository.UtilisateurRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaiementService {

    private final InscriptionRepository inscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public PaiementService(
            InscriptionRepository inscriptionRepository,
            UtilisateurRepository utilisateurRepository,
            @Value("${stripe.secret-key}") String secretKey
    ) {
        this.inscriptionRepository = inscriptionRepository;
        this.utilisateurRepository = utilisateurRepository;
        Stripe.apiKey = secretKey;
    }

    public record CheckoutSessionInfo(String sessionId, String url) {}

    public CheckoutSessionInfo createCheckoutSession(
            Long demandeurId,
            Long inscriptionId,
            String successUrl,
            String cancelUrl
    ) {
        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        // Seul le propriétaire ou un admin peut payer
        boolean estAdmin = demandeur.getRole() == Role.ADMIN;
        boolean estProprietaire = inscription.getUtilisateur().getId().equals(demandeurId);
        if (!(estAdmin || estProprietaire)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez payer que vos propres inscriptions");
        }

        if (inscription.getStatut() == StatutInscription.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible de payer une inscription annulee");
        }
        if (inscription.getStatut() == StatutInscription.PAYEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inscription deja payee");
        }

        Formation formation = inscription.getSession().getFormation();
        BigDecimal prix = formation.getPrix(); // supposé en euros
        long amountInCents = prix.multiply(BigDecimal.valueOf(100)).longValueExact();

        try {
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setCustomerEmail(demandeur.getEmail())
                            .setSuccessUrl(successUrl + "?inscriptionId=" + inscriptionId + "&session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl(cancelUrl)
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("eur")
                                                            .setUnitAmount(amountInCents)
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName(formation.getTitre())
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            Session session = Session.create(params);
            return new CheckoutSessionInfo(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la création de la session de paiement Stripe", e);
        }
    }

    /**
     * Marque une inscription comme PAYEE après retour Stripe.
     * On garde la logique existante de sécurité.
     */
    public Inscription marquerInscriptionPayee(Long demandeurId, Long inscriptionId) {
        Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        boolean estAdmin = demandeur.getRole() == Role.ADMIN;
        boolean estProprietaire = inscription.getUtilisateur().getId().equals(demandeurId);
        if (!(estAdmin || estProprietaire)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez confirmer que vos propres paiements");
        }

        if (inscription.getStatut() == StatutInscription.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inscription annulee");
        }

        inscription.setStatut(StatutInscription.PAYEE);
        return inscriptionRepository.save(inscription);
    }
}
