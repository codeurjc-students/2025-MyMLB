package com.mlb.mlbportal.unit.ticket;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.handler.PaymentException;
import com.mlb.mlbportal.services.ticket.PaymentService;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.paymentService = new PaymentService();
    }

    @Test
    @DisplayName("Should process payment successfully with valid card data")
    void testProcessPaymentSuccess() {
        PurchaseRequest validRequest = new PurchaseRequest(
                1L, 2, List.of(), USER1_USERNAME,
                "49927398716", "123", LocalDate.now().plusYears(1)
        );
        assertThatCode(() -> this.paymentService.processPayment(validRequest)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"49927398717", "not-a-number", " "})
    @DisplayName("Should throw PaymentException when Luhn check fails")
    void testInvalidLuhn(String invalidNumber) {
        PurchaseRequest request = new PurchaseRequest(
                1L, 1, List.of(), USER1_USERNAME,
                invalidNumber, "123", LocalDate.now().plusYears(1)
        );
        assertThatThrownBy(() -> this.paymentService.processPayment(request))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Cardnumber not valid");
    }

    @Test
    @DisplayName("Should throw PaymentException when card is expired")
    void testExpiredCard() {
        LocalDate expiredDate = LocalDate.now().minusMonths(2);
        PurchaseRequest request = new PurchaseRequest(
                1L, 1, List.of(), USER1_USERNAME,
                "49927398716", "123", expiredDate
        );
        assertThatThrownBy(() -> this.paymentService.processPayment(request))
                .isInstanceOf(PaymentException.class)
                .hasMessage("The card has expired");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "12345", "abc", ""})
    @DisplayName("Should throw PaymentException when CVV format is invalid")
    void testInvalidCvv(String invalidCvv) {
        PurchaseRequest request = new PurchaseRequest(
                1L, 1, List.of(), USER1_USERNAME,
                "49927398716", invalidCvv, LocalDate.now().plusYears(1)
        );
        assertThatThrownBy(() -> this.paymentService.processPayment(request))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Invalid CVV");
    }
}