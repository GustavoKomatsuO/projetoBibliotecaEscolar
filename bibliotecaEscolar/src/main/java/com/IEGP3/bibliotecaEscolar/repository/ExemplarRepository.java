package com.IEGP3.bibliotecaEscolar.repository;

import com.IEGP3.bibliotecaEscolar.model.Exemplar;
import com.IEGP3.bibliotecaEscolar.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {
    List<Exemplar> findByLivroAndStatusExemplar(Livro livro, String statusExemplar);
}