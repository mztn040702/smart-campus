package com.campus.campus_system;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampusSystemApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerLoginAndProtectedApisRequireJwt() throws Exception {
        String username = "jwt_user_" + UUID.randomUUID().toString().replace("-", "");
        String password = "Secret123!";
        String registerBody = """
                {
                  "username": "%s",
                  "password": "%s",
                  "realName": "JWT Test",
                  "college": "Computer Science"
                }
                """.formatted(username, password);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        User savedUser = userRepository.findByUsername(username).orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo(password);
        assertThat(savedUser.getPassword()).startsWith("$2");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(savedUser.getStatus()).isEqualTo("ACTIVE");

        String loginBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        String loginResponse = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = loginResponse.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/product/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void loginAutoRepairsLegacyBlankOrNullStatusUsers() throws Exception {
        String blankStatusUsername = "legacy_blank_" + UUID.randomUUID().toString().replace("-", "");
        String nullStatusUsername = "legacy_null_" + UUID.randomUUID().toString().replace("-", "");
        String password = "Secret123!";

        registerUser(blankStatusUsername, password);
        registerUser(nullStatusUsername, password);

        User blankStatusUser = userRepository.findByUsername(blankStatusUsername).orElseThrow();
        blankStatusUser.setStatus("");
        userRepository.save(blankStatusUser);

        User nullStatusUser = userRepository.findByUsername(nullStatusUsername).orElseThrow();
        nullStatusUser.setStatus(null);
        userRepository.save(nullStatusUser);

        login(blankStatusUsername, password);
        login(nullStatusUsername, password);

        assertThat(userRepository.findByUsername(blankStatusUsername).orElseThrow().getStatus()).isEqualTo("ACTIVE");
        assertThat(userRepository.findByUsername(nullStatusUsername).orElseThrow().getStatus()).isEqualTo("ACTIVE");
    }

    private void registerUser(String username, String password) throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "realName": "Legacy User",
                                  "college": "Computer Science"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void login(String username, String password) throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.token").isString());
    }
}
