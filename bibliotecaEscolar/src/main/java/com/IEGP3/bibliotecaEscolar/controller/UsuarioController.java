package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.LivroRepository;
import com.IEGP3.bibliotecaEscolar.service.BibliotecaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private BibliotecaService bibliotecaService;

    @Autowired
    private LivroRepository livroRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) return "redirect:/login";

        model.addAttribute("usuario", logado);
        model.addAttribute("acervo", livroRepository.findAll());
        return "usuario/dashboard";
    }

    @PostMapping("/emprestimo")
    public String solicitarEmprestimo(@RequestParam("isbn") Long isbn, HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        try {
            Livro livro = livroRepository.findById(isbn).orElseThrow();
            bibliotecaService.realizarEmprestimo(logado, livro);
            return "redirect:/usuario/historico?sucessoEmprestimo";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "usuario/dashboard";
        }
    }

    @PostMapping("/devolucao")
    public String realizarDevolucao(@RequestParam("idEmprestimo") Long idEmprestimo, Model model) {
        try {
            String mensagem = bibliotecaService.realizarDevolucao(idEmprestimo);
            return "redirect:/usuario/historico?msg=" + mensagem;
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "usuario/historico";
        }
    }

    @PostMapping("/reserva")
    public String realizarReserva(@RequestParam("isbn") Long isbn,
                                  @RequestParam("data") String data,
                                  HttpSession session) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        Livro livro = livroRepository.findById(isbn).orElseThrow();
        bibliotecaService.realizarReserva(logado, livro, LocalDate.parse(data));
        return "redirect:/usuario/historico?sucessoReserva";
    }

    @PostMapping("/renovacao")
    public String renovarEmprestimo(@RequestParam("idEmprestimo") Long idEmprestimo, Model model) {
        try {
            bibliotecaService.renovarEmprestimo(idEmprestimo);
            return "redirect:/usuario/historico?sucessoRenovacao";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "usuario/historico";
        }
    }

    @GetMapping("/historico")
    public String verHistorico(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) return "redirect:/login";

        model.addAttribute("emprestimos", bibliotecaService.consultarHistoricoEmprestimos(logado));
        model.addAttribute("reservas", bibliotecaService.consultarHistoricoReservas(logado));
        return "usuario/historico";
    }
}