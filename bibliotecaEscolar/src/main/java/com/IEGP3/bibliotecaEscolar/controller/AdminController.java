package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.Exemplar;
import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.EstadoExemplar;
import com.IEGP3.bibliotecaEscolar.model.StatusDisponibilidade;
import com.IEGP3.bibliotecaEscolar.model.TipoUsuario;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.EmprestimoRepository;
import com.IEGP3.bibliotecaEscolar.repository.ExemplarRepository;
import com.IEGP3.bibliotecaEscolar.repository.LivroRepository;
import com.IEGP3.bibliotecaEscolar.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @GetMapping("/testAdm")
    public String testAdm(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null || logado.getTipoUsuario() != TipoUsuario.BIBLIOTECARIO) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", logado);

        // As listas abaixo alimentam as tabelas e os filtros construídos no HTML
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("emprestimos", emprestimoRepository.findAll());
        model.addAttribute("livros", livroRepository.findAll());

        return "admin/testAdm";
    }

    @PostMapping("/cadastrar-livro")
    public String cadastrarLivro(@ModelAttribute Livro livro) {
        livroRepository.save(livro);
        return "redirect:/admin/testAdm?sucessoLivro=true";
    }

    @PostMapping("/cadastrar-exemplar")
    public String cadastrarExemplar(@RequestParam("isbn") Long isbn,
                                    @RequestParam("quantidade") int quantidade,
                                    @RequestParam("estado") EstadoExemplar estado,
                                    @RequestParam("statusDisponibilidade") StatusDisponibilidade status) {

        Livro livro = livroRepository.findById(isbn).orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        for (int i = 0; i < quantidade; i++) {
            Exemplar novoExemplar = new Exemplar();
            novoExemplar.setLivro(livro);
            novoExemplar.setEstado(estado);
            novoExemplar.setStatusDisponibilidade(status);
            exemplarRepository.save(novoExemplar);
        }

        return "redirect:/admin/testAdm?sucessoExemplar=true";
    }
}