package com.ServicePaiement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CinetPayService {

    private static final Logger logger = LoggerFactory.getLogger(CinetPayService.class);

    @Value("${cinetpay.api-key}")
    private String apiKey;

    @Value("${cinetpay.site-id}")
    private String siteId;

    @Value("${cinetpay.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> initPayment(Map<String, Object> paymentData) {
        String url = baseUrl + "/payment";
        paymentData.put("apikey", apiKey);
        paymentData.put("site_id", siteId);

        logger.info("Appel à l'API CinetPay pour initier le paiement. URL: {}, Données: {}", url, paymentData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentData, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            logger.info("Réponse brute de CinetPay: {}", response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Erreur HTTP lors de l'appel à CinetPay: {}", e.getResponseBodyAsString());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur CinetPay: " + e.getResponseBodyAsString());
            return error;
        } catch (Exception e) {
            logger.error("Exception inattendue lors de l'appel à CinetPay: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur interne lors de l'appel à CinetPay: " + e.getMessage());
            return error;
        }
    }

    public Map<String, Object> checkPayment(String transactionId) {
        String url = baseUrl + "/payment/check";
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", apiKey);
        payload.put("site_id", siteId);
        payload.put("transaction_id", transactionId);

        logger.info("Appel à l'API CinetPay pour vérifier le paiement. URL: {}, Données: {}", url, payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            logger.info("Réponse brute de CinetPay (check): {}", response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Erreur HTTP lors de la vérification CinetPay: {}", e.getResponseBodyAsString());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur CinetPay: " + e.getResponseBodyAsString());
            return error;
        } catch (Exception e) {
            logger.error("Exception inattendue lors de la vérification CinetPay: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur interne lors de la vérification CinetPay: " + e.getMessage());
            return error;
        }
    }
}
