package com.IEGP3.bibliotecaEscolar.repository;

import com.IEGP3.bibliotecaEscolar.model.Reserva;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario);
}