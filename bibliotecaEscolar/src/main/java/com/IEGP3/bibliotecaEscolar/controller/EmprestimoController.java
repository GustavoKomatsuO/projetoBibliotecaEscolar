package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.*;
import com.IEGP3.bibliotecaEscolar.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
// redirecionamento web automatico para /admin/emprestimos
@Controller
@RequestMapping("/admin/emprestimos")
public class EmprestimoController {

    //  executa automaticamente recursos
    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    //  Mostra a tela com a lista de todos os empréstimos
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

    //  Ativa um empréstimo
    @PostMapping("/confirmar-retirada/{id}")
    public String confirmarRetirada(@PathVariable("id") Long idEmprestimo, RedirectAttributes redirectAttributes) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(idEmprestimo);

        if (emprestimoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo não encontrado!");
            return "redirect:/admin/testAdm";
        }


        //  define a data de devoluçao
        Emprestimo emp = emprestimoOpt.get();
        Usuario usuario = emp.getUsuario();

        int diasEmprestimo = 7;
        if (usuario.getTipoUsuario() == TipoUsuario.INSTRUTOR) {
            diasEmprestimo = 15;
        }
        if (emp.getExemplar().getLivro().getCategoriaRestricao() == CategoriaRestricao.RESTRITO) {
            diasEmprestimo = 1;
        }

        emp.setDataAlugada(LocalDate.now());
        emp.setDataEstimada(LocalDate.now().plusDays(diasEmprestimo));
        emp.setStatus(StatusEmprestimo.EM_ANDAMENTO);

        emprestimoRepository.save(emp);

        redirectAttributes.addFlashAttribute("sucesso", "Retirada confirmada! Livro entregue ao aluno e empréstimo ativado.");
        return "redirect:/admin/testAdm";
    }

    //  Cria um empréstimo novo do zero
    @PostMapping("/salvar")
    public String realizarEmprestimo(@RequestParam("cpfUsuario") String cpfUsuario,
                                     @RequestParam("idExemplar") Long idExemplar,
                                     RedirectAttributes redirectAttributes) {

        String cpfLimpo = (cpfUsuario != null) ? cpfUsuario.replaceAll("[^0-9]", "") : "";
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpfLimpo);

        //  mensagens de erro
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado!");
            return "redirect:/admin/testAdm";
        }

        Usuario usuario = usuarioOpt.get();

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

        //  cria o registro do empréstimo e marca o livro como indisponivel
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

        exemplar.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);
        exemplarRepository.save(exemplar);
        emprestimoRepository.save(emprestimo);

        redirectAttributes.addFlashAttribute("sucesso", "Empréstimo realizado com sucesso!");
        return "redirect:/admin/testAdm";
    }

    //  Registra que o livro foi devolvido e aplica punições se houver atraso
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

        if (hoje.isAfter(emprestimo.getDataEstimada())) {
            long diasAtraso = ChronoUnit.DAYS.between(emprestimo.getDataEstimada(), hoje);
            long diasSuspensao = diasAtraso * 2;

            double valorMultaCalc = 2.00 + (diasAtraso * 0.50);
            emprestimo.setValorMulta(BigDecimal.valueOf(valorMultaCalc).setScale(2, RoundingMode.HALF_UP));

            Usuario usuario = emprestimo.getUsuario();
            usuario.setStatusPenalidade(StatusPenalidade.SUSPENSO);

            LocalDate inicioSuspensao = (usuario.getDataFimSuspensao() != null && usuario.getDataFimSuspensao().isAfter(hoje))
                    ? usuario.getDataFimSuspensao() : hoje;

            usuario.setDataFimSuspensao(inicioSuspensao.plusDays(diasSuspensao));
            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("erro", "Devolução concluída com " + diasAtraso + " dia(s) de atraso. Multa gerada: R$ " + emprestimo.getValorMulta() + ". Suspenso por " + diasSuspensao + " dia(s).");
        } else {
            redirectAttributes.addFlashAttribute("sucesso", "Exemplar devolvido com sucesso dentro do prazo!");
        }

        Exemplar exemplar = emprestimo.getExemplar();

        List<Reserva> filaReservas = reservaRepository.findAll().stream()
                .filter(r -> r.getLivro().getIsbn().equals(exemplar.getLivro().getIsbn()) && "PENDENTE".equals(r.getStatus()))
                .sorted(Comparator.comparing(Reserva::getDataReserva).thenComparing(Reserva::getIdReserva))
                .collect(Collectors.toList());

        if (!filaReservas.isEmpty()) {
            Reserva proximaReserva = filaReservas.get(0);
            proximaReserva.setStatus("AGUARDANDO_RETIRADA");
            reservaRepository.save(proximaReserva);

            redirectAttributes.addFlashAttribute("alertaReserva", " ATENÇÃO: Este livro está reservado para o aluno: " + proximaReserva.getUsuario().getNome() + ". Ele tem 48h para buscar.");
        } else {
            exemplar.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
        }

        exemplarRepository.save(exemplar);
        emprestimoRepository.save(emprestimo);

        return "redirect:/admin/testAdm";
    }

    //  permite que o aluno solicite a devoluçao de um livro
    @PostMapping("/solicitar-devolucao")
    public String solicitarDevolucaoUsuario(@RequestParam("idEmprestimo") Long idEmprestimo, RedirectAttributes redirectAttributes) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(idEmprestimo);
        if (emprestimoOpt.isPresent()) {
            Emprestimo emp = emprestimoOpt.get();
            emp.setStatus(StatusEmprestimo.AGUARDANDO_DEVOLUCAO);
            emprestimoRepository.save(emp);
            redirectAttributes.addFlashAttribute("sucesso", "Solicitação de devolução enviada com sucesso! Aguarde a confirmação no balcão.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo não encontrado!");
        }
        return "redirect:/usuario/emprestimos";
    }
}