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

    /* MÉTODO AUXILIAR DE VALIDAÇÃO OFICIAL DE CPF */
    private boolean isCpfValido(String cpf) {
        if (cpf == null) return false;
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");

        if (cpfLimpo.length() != 11) return false;

        // Verifica sequências repetidas (ex: 111.111.111-11)
        if (cpfLimpo.matches("(\\d)\\1{10}")) return false;

        try {
            // Cálculo do 1º Dígito Verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpfLimpo.charAt(i) - '0') * (10 - i);
            }
            int resto = 11 - (soma % 11);
            int digito1 = (resto >= 10) ? 0 : resto;

            if (digito1 != (cpfLimpo.charAt(9) - '0')) return false;

            // Cálculo do 2º Dígito Verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpfLimpo.charAt(i) - '0') * (11 - i);
            }
            resto = 11 - (soma % 11);
            int digito2 = (resto >= 10) ? 0 : resto;

            return digito2 == (cpfLimpo.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    //  Listas de todos os usuários (Painel Administrativo)
    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin/listar-usuarios";
    }

    //  Abre o formulário de cadastro de novo usuário
    @GetMapping("/novo")
    public String formularioNovoUsuario(@Nonnull Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/cadastrar-usuario";
    }

    //  Salva um novo usuário ou atualizar um existente
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario,
                                @RequestParam(value = "confirmarSenha", required = false) String confirmarSenha,
                                Model model) {

        //  Limpa o CPF deixando apenas números
        if (usuario.getCpf() != null) {
            String cpfLimpo = usuario.getCpf().replaceAll("[^0-9]", "");
            usuario.setCpf(cpfLimpo);
        }

        //  Validação criteriosa: Algoritmo oficial dos dígitos verificadores
        if (!isCpfValido(usuario.getCpf())) {
            model.addAttribute("erro", "O CPF informado é inválido!");
            return "admin/cadastrar-usuario";
        }

        //  Se for novo cadastro, valida se as senhas batem (nos campos senha e confirmar senha)
        if (usuario.getId() == null && confirmarSenha != null && !usuario.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem!");
            return "admin/cadastrar-usuario";
        }

        //  Se for novo cadastro, verifica se o CPF já está em uso
        if (usuario.getId() == null && usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            model.addAttribute("erro", "CPF já cadastrado no sistema!");
            return "admin/cadastrar-usuario";
        }

        // Persiste no banco de dados MySQL
        usuarioRepository.save(usuario);

        model.addAttribute("sucesso", "Usuário salvo com sucesso!");
        return "redirect:/admin/usuarios";
    }

    //  Deleta um usuário pelo ID
    @GetMapping("/deletar/{id}")
    public String deletarUsuario(@PathVariable("id") Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/usuarios";
    }
}