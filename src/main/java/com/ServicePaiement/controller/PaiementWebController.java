package com.ServicePaiement.controller;

import com.ServicePaiement.model.Transaction;
import com.ServicePaiement.service.CinetPayService;
import com.ServicePaiement.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PaiementWebController {

    private static final Logger logger = LoggerFactory.getLogger(PaiementWebController.class);

    @Autowired
    private CinetPayService cinetPayService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/payer")
    public String showPaiementPage(Model model) {
        // Vous pouvez pré-remplir des valeurs ici si besoin
        return "paiement";
    }

    @PostMapping("/payer")
    public ModelAndView lancerPaiement(@RequestParam double amount,
                                       @RequestParam String description,
                                       @RequestParam String customer_name,
                                       @RequestParam String customer_email) {
        logger.info("Début du processus de paiement : amount={}, description={}, customer_name={}, customer_email={}",
                amount, description, customer_name, customer_email);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", (int) amount); // <-- conversion en entier
        paymentData.put("currency", "XAF"); // <-- Utilisez la devise de votre compte
        paymentData.put("transaction_id", "TXN" + System.currentTimeMillis());
        paymentData.put("description", description);
        paymentData.put("return_url", "http://localhost:8080/paiement-retour");
        paymentData.put("notify_url", "http://localhost:8080/cinetpay/notify");
        paymentData.put("customer_name", customer_name);
        paymentData.put("customer_email", customer_email);

        logger.info("Données envoyées à CinetPay : {}", paymentData);

        Map<String, Object> result = cinetPayService.initPayment(paymentData);

        if (result == null) {
            logger.error("Réponse nulle reçue de CinetPay");
            ModelAndView mav = new ModelAndView("paiement");
            mav.addObject("error", "Erreur interne : aucune réponse de CinetPay.");
            return mav;
        }

        if (result.containsKey("error")) {
            logger.error("Erreur retournée par CinetPay : {}", result.get("error"));
            ModelAndView mav = new ModelAndView("paiement");
            mav.addObject("error", result.get("error"));
            return mav;
        }

        logger.info("Réponse reçue de CinetPay : {}", result);

        // Correction : extraire payment_url depuis data
        Object dataObj = result.get("data");
        String redirectUrl = null;
        if (dataObj instanceof Map) {
            Object paymentUrlObj = ((Map<?, ?>) dataObj).get("payment_url");
            if (paymentUrlObj != null) {
                redirectUrl = paymentUrlObj.toString();
            }
        }

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            logger.info("Redirection vers l'URL de paiement : {}", redirectUrl);
            return new ModelAndView(new RedirectView(redirectUrl));
        } else {
            logger.error("Aucune URL de paiement reçue dans la réponse CinetPay : {}", result);
            ModelAndView mav = new ModelAndView("paiement");
            mav.addObject("error", "Erreur lors de la génération du lien de paiement CinetPay.");
            return mav;
        }
    }

    /**
     * Endpoint pour tester le paiement en mode test (sandbox).
     * Utilise un numéro spécial pour simuler un paiement réussi.
     */
    @PostMapping("/payer-test")
    public ModelAndView lancerPaiementTest(@RequestParam int amount,
                                           @RequestParam String description,
                                           @RequestParam String customer_name,
                                           @RequestParam String customer_email) {
        logger.info("Début du paiement TEST : amount={}, description={}, customer_name={}, customer_email={}",
                amount, description, customer_name, customer_email);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", amount);
        paymentData.put("currency", "XAF"); // ou XOF selon votre compte
        paymentData.put("transaction_id", "TEST" + System.currentTimeMillis());
        paymentData.put("description", description);
        paymentData.put("return_url", "http://localhost:8080/paiement-retour");
        paymentData.put("notify_url", "http://localhost:8080/cinetpay/notify");
        paymentData.put("customer_name", customer_name);
        paymentData.put("customer_email", customer_email);

        // Numéro spécial pour simuler un paiement réussi en mode test CinetPay
        paymentData.put("customer_phone_number", "01010101");

        // Optionnel : forcer le mode test si votre compte le permet (sinon, le numéro suffit)
        paymentData.put("test", "YES");

        logger.info("Données envoyées à CinetPay (TEST) : {}", paymentData);

        Map<String, Object> result = cinetPayService.initPayment(paymentData);

        if (result == null) {
            logger.error("Réponse nulle reçue de CinetPay (TEST)");
            ModelAndView mav = new ModelAndView("paiement");
            mav.addObject("error", "Erreur interne : aucune réponse de CinetPay (TEST).");
            return mav;
        }

        if (result.containsKey("error")) {
            logger.error("Erreur retournée par CinetPay (TEST) : {}", result.get("error"));
            ModelAndView mav = new ModelAndView("paiement");
            mav.addObject("error", result.get("error"));
            return mav;
        }

        logger.info("Réponse reçue de CinetPay (TEST) : {}", result);

        Object dataObj = result.get("data");
        String redirectUrl = null;
        if (dataObj instanceof Map) {
            Object paymentUrlObj = ((Map<?, ?>) dataObj).get("payment_url");
            if (paymentUrlObj != null) {
                redirectUrl = paymentUrlObj.toString();
            }
        }

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            logger.info("Redirection vers l'URL de paiement (TEST) : {}", redirectUrl);
            return new ModelAndView(new RedirectView(redirectUrl));
        } else {
            logger.error("Aucune URL de paiement reçue dans la réponse CinetPay (TEST) : {}", result);
            ModelAndView mav = new ModelAndView("paiement");
            mav.addObject("error", "Erreur lors de la génération du lien de paiement CinetPay (TEST).");
            return mav;
        }
    }

    @GetMapping("/paiement-retour")
    public String retourPaiement(@RequestParam Map<String, String> params, Model model) {
        logger.info("Retour utilisateur depuis CinetPay (return_url) avec paramètres : {}", params);

        // Loggez tous les paramètres pour analyse complète
        for (Map.Entry<String, String> entry : params.entrySet()) {
            logger.info("Paramètre retour CinetPay : {} = {}", entry.getKey(), entry.getValue());
        }

        String transactionId = params.get("transaction_id");
        String paymentStatus = params.get("cpm_trans_status");
        String message = params.get("cpm_error_message");
        logger.info("Transaction ID: {}", transactionId);
        logger.info("Statut du paiement (cpm_trans_status): {}", paymentStatus);
        logger.info("Message CinetPay (cpm_error_message): {}", message);

        model.addAttribute("etat", paymentStatus);
        model.addAttribute("message", message);
        model.addAttribute("transactionId", transactionId);

        return "etat-paiement"; // Créez une page Thymeleaf pour afficher le résultat à l'utilisateur
    }

    @PostMapping("/cinetpay/notify")
    public void notificationCinetPay(@RequestBody Map<String, Object> payload) {
        logger.info("Notification reçue de CinetPay (notify_url) : {}", payload);

        // Loggez tous les champs reçus pour analyse complète
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            logger.info("Champ notification CinetPay : {} = {}", entry.getKey(), entry.getValue());
        }

        // Exemple de récupération des infos (adaptez selon la structure exacte du POST CinetPay)
        String transactionId = (String) payload.get("transaction_id");
        String status = (String) payload.get("status");
        int amount = Integer.parseInt(payload.get("amount").toString());
        String currency = (String) payload.get("currency");
        String description = (String) payload.get("description");
        String customerName = (String) payload.get("customer_name");
        String customerEmail = (String) payload.get("customer_email");
        String customerPhoneNumber = (String) payload.get("customer_phone_number");

        logger.info("Détail transaction : transactionId={}, status={}, amount={}, currency={}, description={}, customerName={}, customerEmail={}, customerPhoneNumber={}",
                transactionId, status, amount, currency, description, customerName, customerEmail, customerPhoneNumber);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setCustomerName(customerName);
        transaction.setCustomerEmail(customerEmail);
        transaction.setCustomerPhoneNumber(customerPhoneNumber);
        transaction.setStatus(status);
        transaction.setDate(LocalDateTime.now());

        transactionService.save(transaction);

        logger.info("Transaction enregistrée en base avec statut : {}", status);
    }
}
