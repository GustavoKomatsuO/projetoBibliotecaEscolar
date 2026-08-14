package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.*;
import com.IEGP3.bibliotecaEscolar.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/emprestimos")
public class EmprestimoController {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // 1. Listar todos os empréstimos ativos no painel do Bibliotecário
    @GetMapping
    public String listarEmprestimos(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null || logado.getTipoUsuario() != TipoUsuario.BIBLIOTECARIO) {
            return "redirect:/login";
        }

        List<Emprestimo> emprestimos = emprestimoRepository.findAll();
        model.addAttribute("emprestimos", emprestimos);
        return "admin/emprestimos";
    }

    // 2. Realizar empréstimo
    @PostMapping("/salvar")
    public String realizarEmprestimo(@RequestParam("cpfUsuario") String cpfUsuario,
                                     @RequestParam("idExemplar") Long idExemplar,
                                     RedirectAttributes redirectAttributes) {

        // Limpa o CPF para manter apenas numérico (11 dígitos)
        String cpfLimpo = (cpfUsuario != null) ? cpfUsuario.replaceAll("[^0-9]", "") : "";
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpfLimpo);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado!");
            return "redirect:/admin/testAdm";
        }

        Usuario usuario = usuarioOpt.get();

        // Verifica status de penalidade do usuário
        if (usuario.getStatusPenalidade() != StatusPenalidade.ATIVO) {
            redirectAttributes.addFlashAttribute("erro", "Usuário suspenso ou bloqueado de realizar novos empréstimos!");
            return "redirect:/admin/testAdm";
        }

        Optional<Exemplar> exemplarOpt = exemplarRepository.findById(idExemplar);
        if (exemplarOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Exemplar não encontrado!");
            return "redirect:/admin/testAdm";
        }

        Exemplar exemplar = exemplarOpt.get();

        if (exemplar.getStatusDisponibilidade() != StatusDisponibilidade.DISPONIVEL) {
            redirectAttributes.addFlashAttribute("erro", "Exemplar não está disponível para empréstimo!");
            return "redirect:/admin/testAdm";
        }

        // Regra de prazos: Aluno (7 dias), Instrutor (15 dias), Restritos/Didáticos (1 dia)
        int diasEmprestimo = 7;
        if (usuario.getTipoUsuario() == TipoUsuario.INSTRUTOR) {
            diasEmprestimo = 15;
        }

        if (exemplar.getLivro().getCategoriaRestricao() == CategoriaRestricao.RESTRITO) {
            diasEmprestimo = 1;
        }

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setExemplar(exemplar);
        emprestimo.setDataAlugada(LocalDate.now());
        emprestimo.setDataEstimada(LocalDate.now().plusDays(diasEmprestimo));
        emprestimo.setStatus(StatusEmprestimo.EM_ANDAMENTO);

        // Atualiza status do exemplar para INDISPONIVEL
        exemplar.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);
        exemplarRepository.save(exemplar);

        emprestimoRepository.save(emprestimo);

        redirectAttributes.addFlashAttribute("sucesso", "Empréstimo realizado com sucesso!");
        return "redirect:/admin/testAdm";
    }

    // 3. Processar devolução de exemplar
    @PostMapping("/devolver/{id}")
    public String devolverExemplar(@PathVariable("id") Long idEmprestimo, RedirectAttributes redirectAttributes) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(idEmprestimo);

        if (emprestimoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo não encontrado!");
            return "redirect:/admin/testAdm";
        }

        Emprestimo emprestimo = emprestimoOpt.get();
        LocalDate hoje = LocalDate.now();

        emprestimo.setDataDevolucao(hoje);
        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);

        // Regra de Penalidade: Cada dia de atraso gera suspensão de 2 dias
        if (hoje.isAfter(emprestimo.getDataEstimada())) {
            long diasAtraso = ChronoUnit.DAYS.between(emprestimo.getDataEstimada(), hoje);
            long diasSuspensao = diasAtraso * 2;

            Usuario usuario = emprestimo.getUsuario();
            usuario.setStatusPenalidade(StatusPenalidade.SUSPENSO);

            LocalDate inicioSuspensao = (usuario.getDataFimSuspensao() != null && usuario.getDataFimSuspensao().isAfter(hoje))
                    ? usuario.getDataFimSuspensao() : hoje;

            usuario.setDataFimSuspensao(inicioSuspensao.plusDays(diasSuspensao));
            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("erro", "Devolução concluída com " + diasAtraso + " dia(s) de atraso. Usuário suspenso por " + diasSuspensao + " dia(s) (até " + usuario.getDataFimSuspensao() + ").");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", "Exemplar devolvido com sucesso dentro do prazo!");
        }

        // Atualiza status do exemplar para DISPONIVEL
        Exemplar exemplar = emprestimo.getExemplar();
        exemplar.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
        exemplarRepository.save(exemplar);

        emprestimoRepository.save(emprestimo);

        return "redirect:/admin/testAdm";
    }
}