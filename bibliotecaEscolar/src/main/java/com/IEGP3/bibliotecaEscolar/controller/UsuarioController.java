package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.UsuarioRepository;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Listar todos os usuários (Painel Administrativo)
    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin/listar-usuarios";
    }

    // 2. Abrir o formulário de cadastro de novo usuário
    @GetMapping("/novo")
    public String formularioNovoUsuario(@Nonnull Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/cadastrar-usuario";
    }

    // 3. Salvar um novo usuário ou atualizar um existente
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario,
                                @RequestParam(value = "confirmarSenha", required = false) String confirmarSenha,
                                Model model) {

        // 1. Limpa o CPF mantendo APENAS números
        if (usuario.getCpf() != null) {
            String cpfLimpo = usuario.getCpf().replaceAll("[^0-9]", "");
            usuario.setCpf(cpfLimpo);
        }

        // 2. Validação ESTRITA: Exige exatamente 11 dígitos
        if (usuario.getCpf() == null || usuario.getCpf().length() != 11) {
            model.addAttribute("erro", "O CPF é obrigatório e deve conter exatamente 11 dígitos numéricos!");
            return "admin/cadastrar-usuario";
        }

        // 3. Se for novo cadastro, valida se as senhas batem
        if (usuario.getId() == null && confirmarSenha != null && !usuario.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem!");
            return "admin/cadastrar-usuario";
        }

        // 4. Se for novo cadastro, verifica se o CPF já está em uso
        if (usuario.getId() == null && usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            model.addAttribute("erro", "CPF já cadastrado no sistema!");
            return "admin/cadastrar-usuario";
        }

        // Persiste no banco de dados MySQL
        usuarioRepository.save(usuario);

        model.addAttribute("sucesso", "Usuário salvo com sucesso!");
        return "redirect:/admin/usuarios";
    }

    // 4. Deletar um usuário pelo ID
    @GetMapping("/deletar/{id}")
    public String deletarUsuario(@PathVariable("id") Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/usuarios";
    }
}