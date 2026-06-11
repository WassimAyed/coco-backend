package tn.esprit.subspaymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.subspaymentservice.entity.SubscriptionPlan;
import tn.esprit.subspaymentservice.entity.SubscriptionStatus;
import tn.esprit.subspaymentservice.entity.UserSubscription;
import tn.esprit.subspaymentservice.repository.UserSubscriptionRepository;
import tn.esprit.subspaymentservice.service.SubscriptionService;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private UserSubscriptionRepository userSubRepository;

    @Test
    void getSubscriptionsByUserId_shouldReturnForbidden_whenHeaderMissing() throws Exception {
        mockMvc.perform(get("/user-subscriptions/user/10"))
                .andExpect(status().isForbidden());

        verify(userSubRepository, never()).findByUserId(anyLong());
    }

    @Test
    void getSubscriptionsByUserId_shouldCreateFreeSubscription_whenUserHasNone() throws Exception {
        SubscriptionPlan freePlan = SubscriptionPlan.builder()
                .id(1L)
                .name("FREE")
                .price(0.0)
                .postLimit(3)
                .type("FREE")
                .build();

        UserSubscription freeSub = UserSubscription.builder()
                .id(100L)
                .userId(10L)
                .plan(freePlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(new Date())
                .remainingPosts(3)
                .build();

        when(userSubRepository.findByUserId(10L)).thenReturn(Collections.emptyList());
        when(subscriptionService.createFreeSubscription(10L)).thenReturn(freeSub);

        mockMvc.perform(get("/user-subscriptions/user/10")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].plan.name").value("FREE"));
    }

    @Test
    void consume_shouldReturnBadRequest_whenBusinessRuleFails() throws Exception {
        doThrow(new RuntimeException("Quota dépassé")).when(subscriptionService).consumePost(10L);

        mockMvc.perform(post("/consume/10")
                        .header("X-User-Id", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQuota_shouldReturnPayload_whenRequesterMatches() throws Exception {
        when(subscriptionService.checkQuota(10L)).thenReturn(Map.of("remaining_posts", 2, "canPost", true));

        mockMvc.perform(get("/quota/10")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Map.of("remaining_posts", 2, "canPost", true))));
    }
}
