package com.IEGP3.bibliotecaEscolar.service;

import com.IEGP3.bibliotecaEscolar.model.Exemplar;
import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.StatusDisponibilidade;
import com.IEGP3.bibliotecaEscolar.repository.ExemplarRepository;
import com.IEGP3.bibliotecaEscolar.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BibliotecaService {

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private LivroRepository livroRepository;

    // 1. Buscar todos os exemplares disponíveis de um determinado livro
    public List<Exemplar> buscarExemplaresDisponiveis(Livro livro) {
        return exemplarRepository.findByLivroAndStatusDisponibilidade(
                livro,
                StatusDisponibilidade.DISPONIVEL
        );
    }

    // 2. Realizar Empréstimo (Muda o status do exemplar de DISPONIVEL para INDISPONIVEL)
    public boolean realizarEmprestimo(Long idExemplar) {
        Optional<Exemplar> exemplarOpt = exemplarRepository.findById(idExemplar);

        if (exemplarOpt.isPresent()) {
            Exemplar exemplar = exemplarOpt.get();

            // Verifica se o exemplar está realmente disponível para empréstimo
            if (exemplar.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL) {
                exemplar.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);
                exemplarRepository.save(exemplar);
                return true; // Empréstimo efetuado com sucesso
            }
        }
        return false; // Exemplar não encontrado ou indisponível
    }

    // 3. Realizar Devolução (Muda o status do exemplar de volta para DISPONIVEL)
    public boolean realizarDevolucao(Long idExemplar) {
        Optional<Exemplar> exemplarOpt = exemplarRepository.findById(idExemplar);

        if (exemplarOpt.isPresent()) {
            Exemplar exemplar = exemplarOpt.get();
            exemplar.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
            exemplarRepository.save(exemplar);
            return true; // Devolução efetuada com sucesso
        }
        return false;
    }

    // 4. Reservar Exemplar (Muda o status para AGUARDANDO_RETIRADA)
    public boolean reservarExemplar(Long idExemplar) {
        Optional<Exemplar> exemplarOpt = exemplarRepository.findById(idExemplar);

        if (exemplarOpt.isPresent()) {
            Exemplar exemplar = exemplarOpt.get();

            if (exemplar.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL) {
                exemplar.setStatusDisponibilidade(StatusDisponibilidade.AGUARDANDO_RETIRADA);
                exemplarRepository.save(exemplar);
                return true;
            }
        }
        return false;
    }
}