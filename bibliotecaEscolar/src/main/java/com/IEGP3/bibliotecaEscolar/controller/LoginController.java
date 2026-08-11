package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.TipoUsuario;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Redireciona a raiz para a tela de login
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // Exibe a tela de login
    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    // Exibe a tela de cadastro
    @GetMapping("/cadastrar")
    public String paginaCadastro() {
        return "usuario/cadastrarUser";
    }

    // Processa o formulário de cadastro com a regra da senha especial
    @PostMapping("/cadastrar/salvar")
    public String salvarCadastro(Usuario usuario,
                                 @RequestParam("confirmarSenha") String confirmarSenha,
                                 Model model) {

        if (!usuario.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem!");
            return "usuario/cadastrarUser";
        }

        if (usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            model.addAttribute("erro", "CPF já cadastrado no sistema!");
            return "usuario/cadastrarUser";
        }

        String senhaDigitada = usuario.getSenha();

        if (senhaDigitada.startsWith("@adm") && senhaDigitada.length() >= 10) {
            usuario.setTipoUsuario(TipoUsuario.INSTRUTOR);
        } else {
            usuario.setTipoUsuario(TipoUsuario.ALUNO);
        }

        usuarioRepository.save(usuario);

        return "redirect:/login?sucessoCadastro";
    }

    // Processa a autenticação/login do usuário
    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("cpf") String cpf,
                             @RequestParam("senha") String senha,
                             HttpSession session,
                             Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpf);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getSenha().equals(senha)) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuarioLogado", usuario);

            if (usuario.getTipoUsuario() == TipoUsuario.BIBLIOTECARIO) {
                return "redirect:/admin/testAdm";
            } else {
                return "redirect:/usuario/testUser";
            }
        }

        model.addAttribute("erro", "CPF ou Senha incorretos!");
        return "login";
    }

    // ROTA DO USUÁRIO (Adicionada aqui para evitar criar outro arquivo Controller)
    @GetMapping("/usuario/testUser")
    public String testUser(HttpSession session) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) {
            return "redirect:/login";
        }
        return "usuario/testUser";
    }

    // Realiza o logout limpando a sessão
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}