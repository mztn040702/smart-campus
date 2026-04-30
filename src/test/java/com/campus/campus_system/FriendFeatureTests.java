package com.campus.campus_system;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.repository.MessageRepository;
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
class FriendFeatureTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void friendRequestAcceptFlowControlsFriendListAndChatAccess() throws Exception {
        TestUser alice = registerAndLogin("alice");
        TestUser bob = registerAndLogin("bob");

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + alice.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/message/conversation")
                        .header("Authorization", "Bearer " + alice.token)
                        .param("userId1", String.valueOf(alice.id))
                        .param("userId2", String.valueOf(bob.id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", "Bearer " + alice.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"receiverId": %d}
                                """.formatted(bob.id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String incomingResponse = mockMvc.perform(get("/api/friends/requests/incoming")
                        .header("Authorization", "Bearer " + bob.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String requestId = incomingResponse.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(post("/api/friends/requests/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + bob.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + alice.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(bob.id));

        mockMvc.perform(get("/api/message/conversation")
                        .header("Authorization", "Bearer " + alice.token)
                        .param("userId1", String.valueOf(alice.id))
                        .param("userId2", String.valueOf(bob.id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void searchExcludesCurrentUserAndExistingFriends() throws Exception {
        TestUser alice = registerAndLogin("searcher");
        TestUser bob = registerAndLogin("buddy");
        registerAndLogin("other");

        mockMvc.perform(post("/api/friends/request")
                        .header("Authorization", "Bearer " + alice.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"receiverId": %d}
                                """.formatted(bob.id)))
                .andExpect(status().isOk());

        String incomingResponse = mockMvc.perform(get("/api/friends/requests/incoming")
                        .header("Authorization", "Bearer " + bob.token))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String requestId = incomingResponse.replaceAll(".*\"id\":(\\d+).*", "$1");
        mockMvc.perform(post("/api/friends/requests/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + bob.token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/friends/search")
                        .header("Authorization", "Bearer " + alice.token)
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[*].id").isNotEmpty())
                .andExpect(jsonPath("$.data[?(@.id==" + alice.id + ")]").doesNotExist())
                .andExpect(jsonPath("$.data[?(@.id==" + bob.id + ")]").doesNotExist());
    }

    private TestUser registerAndLogin(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String password = "Secret123!";
        String registerBody = """
                {
                  "username": "%s",
                  "password": "%s",
                  "realName": "%s",
                  "college": "Computer Science"
                }
                """.formatted(username, password, prefix);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        String loginResponse = mockMvc.perform(post("/api/user/login")
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

        String token = loginResponse.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        return new TestUser(userId, username, token);
    }

    private record TestUser(Long id, String username, String token) {
    }
}
