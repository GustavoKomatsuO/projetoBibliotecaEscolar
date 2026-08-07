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

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    // --- NOVA ROTA: Exibe a tela de cadastro ---
    @GetMapping("/cadastrar")
    public String paginaCadastro() {
        return "usuario/cadastrarUser"; // Ajustado com a pasta usuario/
    }

    // --- NOVA ROTA: Processa o formulário de cadastro ---
    @PostMapping("/cadastrar/salvar")
    public String salvarCadastro(Usuario usuario,
                                 @RequestParam("confirmarSenha") String confirmarSenha,
                                 Model model) {

        // 1. Verifica se as senhas batem
        if (!usuario.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem!");
            return "cadastrar-usuario";
        }

        // 2. Verifica se o CPF já está cadastrado
        if (usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            model.addAttribute("erro", "CPF já cadastrado no sistema!");
            return "cadastrar-usuario";
        }

        // 3. Salva o novo usuário no MySQL
        usuarioRepository.save(usuario);

        model.addAttribute("sucesso", "Conta criada com sucesso! Faça login abaixo.");
        return "login";
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("cpf") String cpf,
                             @RequestParam("senha") String senha,
                             HttpSession session,
                             Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpf);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getSenha().equals(senha)) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuarioLogado", usuario);

            if (usuario.getTipoUsuario() == TipoUsuario.FUNCIONARIO) {
                return "admin/testAdm";
            } else {
                return "usuario/testUser";
            }
        }

        model.addAttribute("erro", "CPF ou Senha incorretos!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}