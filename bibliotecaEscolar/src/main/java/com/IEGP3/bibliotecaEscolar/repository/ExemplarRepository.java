package com.IEGP3.bibliotecaEscolar.repository;

import com.IEGP3.bibliotecaEscolar.model.Exemplar;
import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.StatusDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    List<Exemplar> findByLivroAndStatusDisponibilidade(Livro livro, StatusDisponibilidade statusDisponibilidade);
}