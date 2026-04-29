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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileFeatureTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void authenticatedUserCanReadAndUpdateOwnProfileWithoutSendingUserId() throws Exception {
        TestUser user = registerAndLogin("profile");

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + user.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(user.id))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + user.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "Profile Nick",
                                  "email": "profile@example.com",
                                  "phone": "13800138000",
                                  "bio": "hello campus",
                                  "avatar": "https://example.com/avatar.png",
                                  "role": "ADMIN",
                                  "status": "DISABLED",
                                  "username": "hacker"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.nickname").value("Profile Nick"))
                .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.bio").value("hello campus"))
                .andExpect(jsonPath("$.data.avatar").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.data.username").value(user.username))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        User saved = userRepository.findById(user.id).orElseThrow();
        assertThat(saved.getNickname()).isEqualTo("Profile Nick");
        assertThat(saved.getEmail()).isEqualTo("profile@example.com");
        assertThat(saved.getPhone()).isEqualTo("13800138000");
        assertThat(saved.getBio()).isEqualTo("hello campus");
        assertThat(saved.getUsername()).isEqualTo(user.username);
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void passwordChangeRequiresCorrectOldPasswordAndStoresNewBcryptHash() throws Exception {
        TestUser user = registerAndLogin("password");

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer " + user.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "Wrong123!",
                                  "newPassword": "NewPass123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("Old password incorrect"));

        String oldHash = userRepository.findById(user.id).orElseThrow().getPassword();

        mockMvc.perform(put("/api/user/password")
                        .header("Authorization", "Bearer " + user.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "Secret123!",
                                  "newPassword": "NewPass123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        User updated = userRepository.findById(user.id).orElseThrow();
        assertThat(updated.getPassword()).startsWith("$2");
        assertThat(updated.getPassword()).isNotEqualTo(oldHash);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "Secret123!"
                                }
                                """.formatted(user.username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "NewPass123!"
                                }
                                """.formatted(user.username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.token").isString());
    }

    private TestUser registerAndLogin(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String password = "Secret123!";

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "realName": "Profile User",
                                  "college": "Computer Science"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Long id = userRepository.findByUsername(username).orElseThrow().getId();
        String response = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = response.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        return new TestUser(id, username, token);
    }

    private record TestUser(Long id, String username, String token) {
    }
}
