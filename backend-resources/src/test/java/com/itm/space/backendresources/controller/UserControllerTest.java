package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.RequestBuilder;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WithMockUser(authorities = "ROLE_MODERATOR", value = "515c3ab4-f966-11ed-be56-0242ac120002")
class UserControllerTest extends BaseIntegrationTest {

    @MockBean
    private Keycloak keycloakClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private MappingsRepresentation mappingsRepresentation;


    private final UUID testId = UUID.fromString("515c3ab4-f966-11ed-be56-0242ac120002");

    private final UserRequest testUserRequest = UserRequest.builder()
            .username("test")
            .firstName("test")
            .lastName("test")
            .email("test@mail.ru")
            .password("test")
            .build();

    private final UserResponse testUserResponse = UserResponse.builder()
            .firstName("test")
            .lastName("test")
            .email("test@mail.ru")
            .roles(List.of("test"))
            .groups(List.of("test"))
            .build();


    @Test
    void create() throws Exception {
        RequestBuilder postRequest = requestWithContent(post("/api/users"), testUserRequest);

        when(keycloakClient.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.created(new URI("515c3ab4-f966-11ed-be56-0242ac120002")).build());

        mvc.perform(postRequest)
                .andExpect(status().isOk());
    }

    @Test
    void getUserById() throws Exception {
        when(keycloakClient.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(testId))).thenReturn(userResource);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(String.valueOf(testId));
        userRepresentation.setFirstName("test");
        userRepresentation.setLastName("test");
        userRepresentation.setEmail("test@mail.ru");

        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("test");

        when(mappingsRepresentation.getRealmMappings()).thenReturn(List.of(roleRepresentation));

        GroupRepresentation groupRepresentation = new GroupRepresentation();
        groupRepresentation.setName("test");

        when(userResource.groups()).thenReturn(List.of(groupRepresentation));


        mvc.perform(get("/api/users/" + testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("test"))
                .andExpect(jsonPath("$.lastName").value("test"))
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.roles.[0]").value("test"))
                .andExpect(jsonPath("$.groups.[0]").value("test"));
    }

    @Test
    void hello() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("515c3ab4-f966-11ed-be56-0242ac120002"));
    }
}