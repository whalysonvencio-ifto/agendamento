package br.com.uab.sart.config;

import br.com.uab.sart.models.Role;
import br.com.uab.sart.models.Usuario;
import br.com.uab.sart.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeedConfigTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminSeedConfig adminSeedConfig;

    @Test
    void deveCriarAdminQuandoNaoExiste() {
        ReflectionTestUtils.setField(adminSeedConfig, "adminEmail", "admin@teste.com");
        ReflectionTestUtils.setField(adminSeedConfig, "adminPassword", "123456");

        when(usuarioRepository.findByEmail("admin@teste.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("hashed_pass");

        adminSeedConfig.onApplicationEvent(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario salvo = captor.getValue();
        assertThat(salvo.getEmail()).isEqualTo("admin@teste.com");
        assertThat(salvo.getSenha()).isEqualTo("hashed_pass");
        assertThat(salvo.getPerfil()).isEqualTo(Role.ROLE_ADMINISTRADOR);
    }

    @Test
    void naoDeveCriarAdminQuandoJaExiste() {
        ReflectionTestUtils.setField(adminSeedConfig, "adminEmail", "admin@teste.com");

        Usuario existente = new Usuario();
        when(usuarioRepository.findByEmail("admin@teste.com")).thenReturn(Optional.of(existente));

        adminSeedConfig.onApplicationEvent(null);

        verify(usuarioRepository, never()).save(any());
    }
}
