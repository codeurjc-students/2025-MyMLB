package com.mlb.mlbportal.services.ticket;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PaymentService {

    public void processPayment(PurchaseRequest cardData) {
        if (!this.isValidLuhn(cardData.cardNumber())) {
            throw new IllegalArgumentException("Cardnumber not valid");
        }

        if (this.isExpired(cardData.expirationDate())) {
            throw new IllegalArgumentException("The card has expired");
        }
        if (!cardData.cvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Invalid CVV.");
        }
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean isExpired(LocalDate date) {
        try {
            LocalDate expiry = date.plusMonths(1);
            return expiry.isBefore(LocalDate.now());
        }
        catch (Exception e) {
            return true;
        }
    }
}