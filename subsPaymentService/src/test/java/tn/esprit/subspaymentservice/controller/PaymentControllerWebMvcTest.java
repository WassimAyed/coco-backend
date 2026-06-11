package tn.esprit.subspaymentservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.subspaymentservice.entity.Payment;
import tn.esprit.subspaymentservice.repository.PaymentRepository;
import tn.esprit.subspaymentservice.service.InvoiceService;
import tn.esprit.subspaymentservice.service.PaymentService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private InvoiceService invoiceService;

    @Test
    void createPaymentSession_shouldReturnForbidden_whenUserHeaderMissing() throws Exception {
        mockMvc.perform(post("/payments")
                        .param("userId", "10")
                        .param("planId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));

        verify(paymentService, never()).createCheckoutSession(anyLong(), anyLong());
    }

    @Test
    void createPaymentSession_shouldReturnOk_whenRequesterMatchesUserId() throws Exception {
        when(paymentService.createCheckoutSession(10L, 2L)).thenReturn("https://stripe.test/session");

        mockMvc.perform(post("/payments")
                        .param("userId", "10")
                        .param("planId", "2")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://stripe.test/session"));
    }

    @Test
    void getPaymentsByUser_shouldReturnForbidden_whenRequesterDifferent() throws Exception {
        mockMvc.perform(get("/payments/user/10")
                        .header("X-User-Id", "11"))
                .andExpect(status().isForbidden());

        verify(paymentRepository, never()).findByUserId(anyLong());
    }

    @Test
    void downloadInvoice_shouldReturnForbidden_whenRequesterNotOwner() throws Exception {
        Payment payment = Payment.builder()
                .id(1L)
                .userId(99L)
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/payments/1/invoice")
                        .header("X-User-Id", "10"))
                .andExpect(status().isForbidden());

        verify(invoiceService, never()).generateInvoicePdf(eq(payment));
    }
}
