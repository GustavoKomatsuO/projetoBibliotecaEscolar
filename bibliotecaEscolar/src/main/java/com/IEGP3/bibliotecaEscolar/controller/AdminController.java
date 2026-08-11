package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.TipoUsuario;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
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

    // Rota para a página de testes do Admin (templates/admin/testAdm.html)
    @GetMapping("/testAdm")
    public String testAdm(HttpSession session) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null || logado.getTipoUsuario() != TipoUsuario.BIBLIOTECARIO) {
            return "redirect:/login";
        }
        return "admin/testAdm";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null || logado.getTipoUsuario() != TipoUsuario.BIBLIOTECARIO) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", logado);
        return "admin/dashboard";
    }

    @PostMapping("/cadastrar-livro")
    public String cadastrarLivro(@ModelAttribute Livro livro) {
        livroRepository.save(livro);
        return "redirect:/admin/dashboard?sucessoLivro";
    }

    @PostMapping("/cadastrar-usuario")
    public String cadastrarUsuario(@ModelAttribute Usuario novoUsuario) {
        usuarioRepository.save(novoUsuario);
        return "redirect:/admin/dashboard?sucessoUsuario";
    }
}