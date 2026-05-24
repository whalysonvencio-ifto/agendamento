package br.com.uab.sart.config;

import br.com.uab.sart.models.Role;
import br.com.uab.sart.models.Usuario;
import br.com.uab.sart.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminSeedConfig implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeedConfig.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    public AdminSeedConfig(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Optional<Usuario> adminOpt = usuarioRepository.findByEmail(adminEmail);
        
        if (adminOpt.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador do Sistema");
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminPassword));
            admin.setPerfil(Role.ROLE_ADMINISTRADOR);
            
            usuarioRepository.save(admin);
            logger.info("Administrador inicial criado com sucesso: {}", adminEmail);
        } else {
            logger.info("Administrador inicial já existe. Pulando seed.");
        }
    }
}
