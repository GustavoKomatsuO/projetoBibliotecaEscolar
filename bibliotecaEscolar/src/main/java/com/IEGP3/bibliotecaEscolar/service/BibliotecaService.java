package com.IEGP3.bibliotecaEscolar.service;

import com.IEGP3.bibliotecaEscolar.model.*;
import com.IEGP3.bibliotecaEscolar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BibliotecaService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // 1. EMPRÉSTIMO
    public Emprestimo realizarEmprestimo(Usuario usuario, Livro livro) throws Exception {
        if (usuario.getStatusPenalidade() == StatusPenalidade.SUSPENSO) {
            if (usuario.getDataFimSuspensao() != null && LocalDate.now().isBefore(usuario.getDataFimSuspensao())) {
                throw new Exception("Usuário suspenso até " + usuario.getDataFimSuspensao());
            } else {
                usuario.setStatusPenalidade(StatusPenalidade.ATIVO);
                usuarioRepository.save(usuario);
            }
        }

        List<Emprestimo> ativos = emprestimoRepository.findByUsuarioAndStatus(usuario, StatusEmprestimo.EM_ANDAMENTO);
        if (ativos.size() >= 3) {
            throw new Exception("Limite máximo de 3 empréstimos simultâneos atingido!");
        }

        List<Exemplar> disponiveis = exemplarRepository.findByLivroAndStatusExemplar(livro, "DISPONIVEL");
        if (disponiveis.isEmpty()) {
            throw new Exception("Não há exemplares disponíveis no momento para este livro.");
        }

        Exemplar exemplar = disponiveis.get(0);
        exemplar.setStatusExemplar("EMPRESTADO");
        exemplarRepository.save(exemplar);

        Emprestimo emp = new Emprestimo();
        emp.setUsuario(usuario);
        emp.setExemplar(exemplar);
        emp.setDataAlugada(LocalDate.now());
        emp.setDataEstimada(LocalDate.now().plusDays(7));
        emp.setStatus(StatusEmprestimo.EM_ANDAMENTO);

        return emprestimoRepository.save(emp);
    }

    // 2. DEVOLUÇÃO
    public String realizarDevolucao(Long idEmprestimo) throws Exception {
        Emprestimo emp = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new Exception("Empréstimo não encontrado."));

        LocalDate hoje = LocalDate.now();
        emp.setDataDevolucao(hoje);

        Exemplar exemplar = emp.getExemplar();
        exemplar.setStatusExemplar("DISPONIVEL");
        exemplarRepository.save(exemplar);

        if (hoje.isAfter(emp.getDataEstimada())) {
            emp.setStatus(StatusEmprestimo.ATRASADO);
            long diasAtraso = ChronoUnit.DAYS.between(emp.getDataEstimada(), hoje);
            long diasSuspensao = diasAtraso * 2;

            Usuario u = emp.getUsuario();
            u.setStatusPenalidade(StatusPenalidade.SUSPENSO);
            u.setDataFimSuspensao(hoje.plusDays(diasSuspensao));
            usuarioRepository.save(u);

            emprestimoRepository.save(emp);
            return "Devolução realizada com atraso de " + diasAtraso + " dia(s). Usuário suspenso por " + diasSuspensao + " dias.";
        } else {
            emp.setStatus(StatusEmprestimo.DEVOLVIDO);
            emprestimoRepository.save(emp);
            return "Devolução realizada com sucesso dentro do prazo!";
        }
    }

    // 3. RESERVA
    public Reserva realizarReserva(Usuario usuario, Livro livro, LocalDate dataReserva) {
        Reserva reserva = new Reserva(usuario, livro, dataReserva);
        return reservaRepository.save(reserva);
    }

    // 4. RENOVAÇÃO
    public String renovarEmprestimo(Long idEmprestimo) throws Exception {
        Emprestimo emp = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new Exception("Empréstimo não encontrado."));

        if (emp.getStatus() == StatusEmprestimo.ATRASADO) {
            throw new Exception("Não é possível renovar um empréstimo em atraso!");
        }

        emp.setQuantidadeRenovacoes(emp.getQuantidadeRenovacoes() + 1);
        emp.setDataEstimada(emp.getDataEstimada().plusDays(3));

        emprestimoRepository.save(emp);
        return "Empréstimo renovado! Nova data de devolução: " + emp.getDataEstimada();
    }

    // 5. HISTÓRICO
    public List<Emprestimo> consultarHistoricoEmprestimos(Usuario usuario) {
        return emprestimoRepository.findByUsuario(usuario);
    }
    public List<Reserva> consultarHistoricoReservas(Usuario usuario) {
        return reservaRepository.findByUsuario(usuario);
    }
}