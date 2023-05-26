package com.itm.space.backendresources.service;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private Keycloak keycloakClient;

    @Mock
    UserMapper userMapper;

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

    private final UserResponse testUserResponse = UserResponse.builder()
            .firstName("test")
            .lastName("test")
            .email("test@mail.ru")
            .roles(List.of("test"))
            .groups(List.of("test"))
            .build();

    private final UserRequest testUserRequest = UserRequest.builder()
            .username("test")
            .firstName("test")
            .lastName("test")
            .email("test@mail.ru")
            .password("test")
            .build();


    private final UUID testId = UUID.fromString("515c3ab4-f966-11ed-be56-0242ac120002");


    @Test
    public void shouldCreateUser() throws Exception {
        when(keycloakClient.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.created(new URI("515c3ab4-f966-11ed-be56-0242ac120002")).build());

        userService.createUser(testUserRequest);

        verify(usersResource, times(1)).create(any());
    }

    @Test
    public void shouldGetUserById() {
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

        when(userMapper.userRepresentationToUserResponse(userRepresentation, List.of(roleRepresentation), List.of(groupRepresentation)))
                .thenReturn(testUserResponse);

        UserResponse userResponse = userService.getUserById(testId);

        assertThat(userResponse.getFirstName()).isEqualTo("test");
        assertThat(userResponse.getLastName()).isEqualTo("test");
        assertThat(userResponse.getEmail()).isEqualTo("test@mail.ru");
        assertThat(userResponse.getRoles()).isEqualTo(List.of("test"));
        assertThat(userResponse.getGroups()).isEqualTo(List.of("test"));
    }

    @Test
    public void shouldPreparePasswordRepresentation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = UserServiceImpl.class.getDeclaredMethod("preparePasswordRepresentation", String.class);
        method.setAccessible(true);
        CredentialRepresentation credentialRepresentation = (CredentialRepresentation) method.invoke(userService, "test");

        assertThat(credentialRepresentation.isTemporary()).isFalse();
        assertThat(credentialRepresentation.getType()).isEqualTo(CredentialRepresentation.PASSWORD);
        assertThat(credentialRepresentation.getValue()).isEqualTo("test");
    }

    @Test
    public void shouldPrepareUserRepresentation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue("test");

        Method method = UserServiceImpl.class.getDeclaredMethod("prepareUserRepresentation",
                UserRequest.class, CredentialRepresentation.class);
        method.setAccessible(true);
        UserRepresentation userRepresentation = (UserRepresentation) method.invoke(userService, testUserRequest, credentialRepresentation);

        assertThat(userRepresentation.getUsername()).isEqualTo("test");
        assertThat(userRepresentation.getEmail()).isEqualTo("test@mail.ru");
        assertThat(userRepresentation.getCredentials().get(0).isTemporary()).isFalse();
        assertThat(userRepresentation.getCredentials().get(0).getType()).isEqualTo(CredentialRepresentation.PASSWORD);
        assertThat(userRepresentation.getCredentials().get(0).getValue()).isEqualTo("test");
        assertThat(userRepresentation.isEnabled()).isTrue();
        assertThat(userRepresentation.getFirstName()).isEqualTo("test");
        assertThat(userRepresentation.getLastName()).isEqualTo("test");
    }
}