package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.TipoUsuario;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Listar todos os usuários (Para o Painel do Administrador)
    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin/listar-usuarios"; // Renderiza a lista de usuários no painel admin
    }

    // 2. Abrir o formulário para criar um novo usuário (Balcão do Admin)
    @GetMapping("/novo")
    public String formularioNovoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/cadastrar-usuario";
    }

    // 3. Salvar um novo usuário ou atualizar um existente
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario,
                                @RequestParam(value = "confirmarSenha", required = false) String confirmarSenha,
                                Model model) {

        // Se for um novo cadastro (sem ID), valida se as senhas batem
        if (usuario.getId() == null && confirmarSenha != null && !usuario.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem!");
            return "admin/cadastrar-usuario";
        }

        // Se for um novo cadastro, verifica se o CPF já está registrado
        if (usuario.getId() == null && usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            model.addAttribute("erro", "CPF já cadastrado no sistema!");
            return "admin/cadastrar-usuario";
        }

        // Salva ou atualiza no banco de dados MySQL
        usuarioRepository.save(usuario);

        model.addAttribute("sucesso", "Usuário salvo com sucesso!");
        return "redirect:/admin/usuarios"; // Redireciona para a lista para atualizar a tela
    }

    // 4. Deletar um usuário pelo ID
    @GetMapping("/deletar/{id}")
    public String deletarUsuario(@PathVariable("id") Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/usuarios";
    }
}