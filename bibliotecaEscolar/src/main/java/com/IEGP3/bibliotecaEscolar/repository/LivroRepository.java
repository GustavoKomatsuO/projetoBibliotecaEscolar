package com.IEGP3.bibliotecaEscolar.repository;

import com.IEGP3.bibliotecaEscolar.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    List<Livro> findByTituloContainingIgnoreCase(String titulo);
}