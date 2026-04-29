package com.campus.campus_system;

import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.upload.image-dir=target/test-uploads/images"
})
@AutoConfigureMockMvc
class ImageUploadFeatureTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void imageUploadRequiresLogin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                smallPng()
        );

        mockMvc.perform(multipart("/api/upload/image").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void authenticatedUserCanUploadImageAndReceiveUrl() throws Exception {
        TestUser user = registerAndLogin("upload");
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                smallPng()
        );

        String response = mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .header("Authorization", "Bearer " + user.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url").value(org.hamcrest.Matchers.startsWith("/uploads/images/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String url = response.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1");
        String filename = url.substring(url.lastIndexOf('/') + 1);
        Path storedFile = Path.of("C:\\Users\\dell\\IdeaProjects\\campus-system\\target\\test-uploads\\images", filename);
        assertThat(Files.exists(storedFile)).isTrue();
    }

    @Test
    void uploadRejectsNonImageFilesAndOversizedFiles() throws Exception {
        TestUser user = registerAndLogin("invalidupload");

        MockMultipartFile textFile = new MockMultipartFile(
                "image",
                "note.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not-an-image".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(textFile)
                        .header("Authorization", "Bearer " + user.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("Only image files are allowed"));

        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "large.png",
                MediaType.IMAGE_PNG_VALUE,
                largeBytes
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(largeFile)
                        .header("Authorization", "Bearer " + user.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("Image size must be 5MB or less"));
    }

    private byte[] smallPng() {
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00,
                0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0A,
                0x49, 0x44, 0x41, 0x54, 0x78,
                (byte) 0x9C, 0x63, 0x00, 0x01, 0x00,
                0x00, 0x05, 0x00, 0x01,
                0x0D, 0x0A, 0x2D, (byte) 0xB4,
                0x00, 0x00, 0x00, 0x00,
                0x49, 0x45, 0x4E, 0x44,
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
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
                                  "realName": "Uploader",
                                  "college": "Computer Science"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        User savedUser = userRepository.findByUsername(username).orElseThrow();
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
        return new TestUser(savedUser.getId(), token);
    }

    private record TestUser(Long id, String token) {
    }
}
