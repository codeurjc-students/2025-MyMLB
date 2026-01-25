package com.mlb.mlbportal.services.ticket;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.handler.PaymentException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PaymentService {

    public void processPayment(PurchaseRequest cardData) {
        if (!this.isValidLuhn(cardData.cardNumber())) {
            throw new PaymentException("Cardnumber not valid");
        }
        if (this.isExpired(cardData.expirationDate())) {
            throw new PaymentException("The card has expired");
        }
        if (!cardData.cvv().matches("\\d{3,4}")) {
            throw new PaymentException("Invalid CVV");
        }
    }

    private boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        String cleanNumber = cardNumber.trim().replaceAll("\\s+", "");
        int sum = 0;
        boolean shouldDouble = false;
        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(cleanNumber.charAt(i));

            if (n < 0 || n > 9) {
                return false;
            }
            if (shouldDouble) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            shouldDouble = !shouldDouble;
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