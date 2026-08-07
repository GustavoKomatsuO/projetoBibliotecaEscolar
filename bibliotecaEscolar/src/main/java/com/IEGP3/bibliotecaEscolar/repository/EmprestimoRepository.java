package com.IEGP3.bibliotecaEscolar.repository;

import com.IEGP3.bibliotecaEscolar.model.Emprestimo;
import com.IEGP3.bibliotecaEscolar.model.StatusEmprestimo;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    List<Emprestimo> findByUsuario(Usuario usuario);
    List<Emprestimo> findByUsuarioAndStatus(Usuario usuario, StatusEmprestimo status);
}