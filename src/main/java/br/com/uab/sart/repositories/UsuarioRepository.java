package br.com.uab.sart.repositories;

import br.com.uab.sart.models.Role;
import br.com.uab.sart.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByPerfilAndAtivo(Role perfil, Boolean ativo);
}
