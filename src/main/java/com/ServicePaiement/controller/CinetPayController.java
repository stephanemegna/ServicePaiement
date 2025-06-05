package com.ServicePaiement.controller;

import com.ServicePaiement.service.CinetPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cinetpay")
public class CinetPayController {

    @Autowired
    private CinetPayService cinetPayService;

    @PostMapping("/init-payment")
    public Map<String, Object> initPayment(@RequestBody Map<String, Object> paymentData) {
        // Personnalisez les champs attendus dans paymentData selon vos besoins
        return cinetPayService.initPayment(paymentData);
    }

    @PostMapping("/check-payment")
    public Map<String, Object> checkPayment(@RequestBody Map<String, String> body) {
        String transactionId = body.get("transaction_id");
        return cinetPayService.checkPayment(transactionId);
    }
}
