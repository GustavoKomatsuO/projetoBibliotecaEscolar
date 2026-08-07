package com.IEGP3.bibliotecaEscolar.repository;

import com.IEGP3.bibliotecaEscolar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCpf(String cpf);
}