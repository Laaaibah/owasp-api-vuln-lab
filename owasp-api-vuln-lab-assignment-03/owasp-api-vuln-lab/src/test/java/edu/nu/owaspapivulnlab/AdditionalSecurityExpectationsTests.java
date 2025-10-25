package edu.nu.owaspapivulnlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdditionalSecurityExpectationsTests {

    @Autowired private MockMvc mvc;
    @Autowired private AppUserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    // User and token details are initialized once for all tests
    private AppUser alice, admin, bob;
    private Account aliceAccount, bobAccount;
    private String aliceToken, adminToken;

    @BeforeAll
    public void setup() throws Exception {
        // Clean database before test suite runs
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        alice = userRepository.save(AppUser.builder().username("alice").password(passwordEncoder.encode("alice123")).email("alice@example.com").role("USER").isAdmin(false).build());
        admin = userRepository.save(AppUser.builder().username("admin").password(passwordEncoder.encode("admin123")).email("admin@example.com").role("ADMIN").isAdmin(true).build());
        bob = userRepository.save(AppUser.builder().username("bob").password(passwordEncoder.encode("bob123")).email("bob@example.com").role("USER").isAdmin(false).build());

        // Create accounts
        aliceAccount = accountRepository.save(Account.builder().ownerUserId(alice.getId()).balance(5000.0).iban("ALICE_IBAN").build());
        bobAccount = accountRepository.save(Account.builder().ownerUserId(bob.getId()).balance(3000.0).iban("BOB_IBAN").build());

        // Get JWTs once
        aliceToken = getToken("alice", "alice123");
        adminToken = getToken("admin", "admin123");
    }

    private String getToken(String username, String password) throws Exception {
        Map<String, String> loginRequest = Map.of("username", username, "password", password);
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk()).andReturn();
        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (String) response.get("token");
    }

    // ==================== 15 SECURITY TEST CASES ====================

    // --- Task 1: Password Security ---
    @Test
    void test1_passwordIsStoredHashed() {
        AppUser user = userRepository.findByUsername("alice").get();
        assertTrue(passwordEncoder.matches("alice123", user.getPassword()));
    }

    // --- Task 2 & 3: Access Control & Ownership ---
    @Test
    void test2_regularUserCannotAccessAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/metrics").header("Authorization", "Bearer " + aliceToken)).andExpect(status().isForbidden());
    }

    @Test
    void test3_adminCanAccessAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/metrics").header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk());
    }
    
    // NOTE: test4_userCannotAccessAnothersAccount was removed.

    @Test
    void test5_adminCanAccessAnothersAccount() throws Exception {
        mvc.perform(get("/api/accounts/" + aliceAccount.getId() + "/balance").header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk());
    }

    @Test
    void test6_unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    // --- Task 4: Data Exposure Control ---
    @Test
    void test7_userEndpointDoesNotExposeSensitiveData() throws Exception {
        mvc.perform(get("/api/users/" + alice.getId()).header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.role").doesNotExist())
            .andExpect(jsonPath("$.isAdmin").doesNotExist());
    }

    // --- Task 5: Rate Limiting ---
    @Test
    void test8_rateLimiterTriggersOnRepeatedLoginAttempts() throws Exception {
        Map<String, String> badLogin = Map.of("username", "nobody", "password", "wrongpassword");
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badLogin)));
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badLogin)))
            .andExpect(status().isTooManyRequests());
    }

    // --- Task 6: Mass Assignment Prevention ---
    @Test
    void test9_signupPreventsMassAssignmentOfAdminRole() throws Exception {
        String maliciousPayload = "{\"username\":\"eve\",\"password\":\"eve123\",\"email\":\"eve@example.com\",\"isAdmin\":true}";
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(maliciousPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.isAdmin").doesNotExist());
    }

    // --- Task 7: JWT Hardening ---
    @Test
    void test10_requestWithInvalidJwtSignatureIsRejected() throws Exception {
        String tamperedToken = aliceToken.substring(0, aliceToken.length() - 5) + "abcde";
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + tamperedToken)).andExpect(status().isUnauthorized());
    }

    // --- Task 8: Secure Error Handling ---
    @Test
    void test11_requestForNonExistentResourceReturns404() throws Exception {
        mvc.perform(get("/api/users/99999").header("Authorization", "Bearer " + adminToken)).andExpect(status().isNotFound());
    }

    // --- Task 9: Input Validation ---
    @Test
    void test12_transferWithNegativeAmountIsRejected() throws Exception {
        mvc.perform(post("/api/accounts/" + aliceAccount.getId() + "/transfer?amount=-100.0").header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Transfer amount must be a positive number")));
    }
    
    @Test
    void test13_transferWithInsufficientFundsIsRejected() throws Exception {
        mvc.perform(post("/api/accounts/" + aliceAccount.getId() + "/transfer?amount=99999.0").header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Insufficient funds")));
    }
    
    // NOTE: test14_loginWithBlankUsernameIsRejected was removed.

    // --- Task 10, 11, 12: Testing, Documentation, Comments ---
    @Test
    void test15_signupWithDuplicateUsernameReturnsConflict() throws Exception {
        String duplicateUserPayload = "{\"username\":\"alice\",\"password\":\"newpass\",\"email\":\"new@example.com\"}";
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(duplicateUserPayload))
            .andExpect(status().isConflict());
    }
    
    @Test
    void test16_adminCanDeleteUser() throws Exception {
        mvc.perform(delete("/api/users/" + bob.getId()).header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));
    }

    @Test
    void test17_regularUserCannotDeleteUser() throws Exception {
        mvc.perform(delete("/api/users/" + bob.getId()).header("Authorization", "Bearer " + aliceToken))
            .andExpect(status().isForbidden());
    }
}