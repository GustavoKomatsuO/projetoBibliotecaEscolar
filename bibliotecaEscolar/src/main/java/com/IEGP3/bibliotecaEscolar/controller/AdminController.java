package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.Exemplar;
import com.IEGP3.bibliotecaEscolar.model.Emprestimo;
import com.IEGP3.bibliotecaEscolar.model.Livro;
import com.IEGP3.bibliotecaEscolar.model.EstadoExemplar;
import com.IEGP3.bibliotecaEscolar.model.StatusDisponibilidade;
import com.IEGP3.bibliotecaEscolar.model.StatusEmprestimo;
import com.IEGP3.bibliotecaEscolar.model.TipoUsuario;
import com.IEGP3.bibliotecaEscolar.model.Usuario;
import com.IEGP3.bibliotecaEscolar.repository.EmprestimoRepository;
import com.IEGP3.bibliotecaEscolar.repository.ExemplarRepository;
import com.IEGP3.bibliotecaEscolar.repository.LivroRepository;
import com.IEGP3.bibliotecaEscolar.repository.ReservaRepository;
import com.IEGP3.bibliotecaEscolar.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping("/testAdm")
    public String testAdm(HttpSession session, Model model) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null || logado.getTipoUsuario() != TipoUsuario.BIBLIOTECARIO) {
            return "redirect:/login";
        }

        // Notificação Dinâmica: Soma retiradas pendentes + devoluções pendentes
        long qtdNotificacoes = emprestimoRepository.findAll().stream()
                .filter(e -> e.getStatus() == StatusEmprestimo.AGUARDANDO_RETIRADA || e.getStatus() == StatusEmprestimo.AGUARDANDO_DEVOLUCAO)
                .count();

        model.addAttribute("usuario", logado);
        model.addAttribute("notificacaoAguardando", qtdNotificacoes);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("emprestimos", emprestimoRepository.findAll());
        model.addAttribute("livros", livroRepository.findAll());
        model.addAttribute("exemplares", exemplarRepository.findAll());
        model.addAttribute("reservas", reservaRepository.findAll(Sort.by(Sort.Direction.ASC, "dataReserva")));

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

    @GetMapping("/deletar-livro/{isbn}")
    public String deletarLivro(@PathVariable("isbn") Long isbn) {
        livroRepository.deleteById(isbn);
        return "redirect:/admin/testAdm?sucessoExclusao=true";
    }

    @GetMapping("/deletar-usuario/{id}")
    public String deletarUsuarioPainel(@PathVariable("id") Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/testAdm?sucessoExclusao=true";
    }

    @PostMapping("/editar-livro")
    public String editarLivro(@ModelAttribute Livro livroAtualizado) {
        Livro livroExistente = livroRepository.findById(livroAtualizado.getIsbn())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        livroExistente.setTitulo(livroAtualizado.getTitulo());
        livroExistente.setAutor(livroAtualizado.getAutor());
        livroExistente.setCategoria(livroAtualizado.getCategoria());
        livroExistente.setEditora(livroAtualizado.getEditora());
        livroExistente.setAnoPublicacao(livroAtualizado.getAnoPublicacao());
        livroExistente.setCategoriaRestricao(livroAtualizado.getCategoriaRestricao());
        livroExistente.setUrlImagem(livroAtualizado.getUrlImagem());
        livroExistente.setSinopse(livroAtualizado.getSinopse());

        livroRepository.save(livroExistente);
        return "redirect:/admin/testAdm?sucessoEdicao=true";
    }

    @PostMapping("/editar-usuario")
    public String editarUsuario(@ModelAttribute Usuario usuarioAtualizado) {
        Usuario usuarioExistente = usuarioRepository.findById(usuarioAtualizado.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuarioExistente.setNome(usuarioAtualizado.getNome());
        usuarioExistente.setCpf(usuarioAtualizado.getCpf());
        usuarioExistente.setTipoUsuario(usuarioAtualizado.getTipoUsuario());
        usuarioExistente.setStatusPenalidade(usuarioAtualizado.getStatusPenalidade());

        usuarioRepository.save(usuarioExistente);
        return "redirect:/admin/testAdm?sucessoEdicao=true";
    }

    @PostMapping("/editar-exemplar")
    public String editarExemplar(@RequestParam("idExemplar") Long idExemplar,
                                 @RequestParam("estado") EstadoExemplar estado,
                                 @RequestParam("statusDisponibilidade") StatusDisponibilidade status) {
        Exemplar exemplar = exemplarRepository.findById(idExemplar)
                .orElseThrow(() -> new RuntimeException("Exemplar não encontrado"));

        exemplar.setEstado(estado);
        exemplar.setStatusDisponibilidade(status);
        exemplarRepository.save(exemplar);

        return "redirect:/admin/testAdm?sucessoEdicao=true";
    }

    @GetMapping("/deletar-exemplar/{id}")
    public String deletarExemplar(@PathVariable("id") Long id) {
        exemplarRepository.deleteById(id);
        return "redirect:/admin/testAdm?sucessoExclusao=true";
    }

    // NOVO ENDPOINT: Permite modificar o status do empréstimo pelo Gerenciamento
    @PostMapping("/editar-emprestimo")
    public String editarEmprestimo(@RequestParam("idEmprestimo") Long idEmprestimo,
                                   @RequestParam("status") StatusEmprestimo status) {
        Emprestimo emprestimo = emprestimoRepository.findById(idEmprestimo)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        emprestimo.setStatus(status);
        if (status == StatusEmprestimo.DEVOLVIDO) {
            Exemplar ex = emprestimo.getExemplar();
            ex.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
            exemplarRepository.save(ex);
        }
        emprestimoRepository.save(emprestimo);

        return "redirect:/admin/testAdm?sucessoEdicao=true";
    }
}