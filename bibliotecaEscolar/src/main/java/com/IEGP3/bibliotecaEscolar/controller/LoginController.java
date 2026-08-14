package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.StatusDisponibilidade;
import com.IEGP3.bibliotecaEscolar.model.TipoUsuario;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.LivroRepository;
import com.IEGP3.bibliotecaEscolar.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // INJEÇÃO NECESSÁRIA: Para carregar os livros no catálogo do usuário
    @Autowired
    private LivroRepository livroRepository;

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

    // Processa o formulário de cadastro com a regra do CPF estrito (11 dígitos numéricos)
    @PostMapping("/cadastrar/salvar")
    public String salvarCadastro(Usuario usuario,
                                 @RequestParam("confirmarSenha") String confirmarSenha,
                                 Model model) {

        // 1. Limpa o CPF mantendo APENAS os caracteres numéricos
        if (usuario.getCpf() != null) {
            String cpfLimpo = usuario.getCpf().replaceAll("[^0-9]", "");
            usuario.setCpf(cpfLimpo);
        }

        // 2. Valida obrigatoriamente se possui EXATAMENTE 11 dígitos numéricos
        if (usuario.getCpf() == null || usuario.getCpf().length() != 11) {
            model.addAttribute("erro", "O CPF é obrigatório e deve ter exatamente 11 dígitos numéricos!");
            return "usuario/cadastrarUser";
        }

        // 3. Valida se as senhas coincidem
        if (!usuario.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não coincidem!");
            return "usuario/cadastrarUser";
        }

        // 4. Verifica se o CPF já está cadastrado no banco de dados
        if (usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            model.addAttribute("erro", "CPF já cadastrado no sistema!");
            return "usuario/cadastrarUser";
        }

        // 5. Regra para atribuição do tipo de utilizador/usuário
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

        String cpfLimpo = (cpf != null) ? cpf.replaceAll("[^0-9]", "") : "";
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpfLimpo);

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

    // CORREÇÃO 1: Rota do catálogo do usuário enviando os livros com Thymeleaf
    @GetMapping("/usuario/testUser")
    public String testUser(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) {
            return "redirect:/login";
        }

        // Passa a lista de todos os livros para renderizar os cards no HTML
        model.addAttribute("livros", livroRepository.findAll());
        return "usuario/testUser";
    }

    // CORREÇÃO 2: Rota para abrir os detalhes do livro
    @GetMapping("/usuario/livro/{isbn}")
    public String detalhesLivro(@PathVariable("isbn") Long isbn, HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) {
            return "redirect:/login";
        }

        Optional<Livro> livroOpt = livroRepository.findById(isbn);
        if (livroOpt.isEmpty()) {
            return "redirect:/usuario/testUser";
        }

        Livro livro = livroOpt.get();

        // Lógica simples para verificar se há algum exemplar disponível
        boolean disponivel = livro.getExemplares() != null && livro.getExemplares().stream()
                .anyMatch(e -> e.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL);

        model.addAttribute("livro", livro);
        model.addAttribute("status", disponivel ? "DISPONIVEL" : "INDISPONIVEL");

        // Retorna o HTML que criamos em src/main/resources/templates/usuario/detalhesLivro.html
        return "usuario/detalhesLivro";
    }

    // Realiza o logout limpando a sessão
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}