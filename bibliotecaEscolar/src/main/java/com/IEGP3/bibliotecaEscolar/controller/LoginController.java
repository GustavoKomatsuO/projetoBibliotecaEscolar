package com.IEGP3.bibliotecaEscolar.controller;

import com.IEGP3.bibliotecaEscolar.model.*;
import com.IEGP3.bibliotecaEscolar.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ExemplarRepository exemplarRepository;

    @Autowired
    private ReservaRepository reservaRepository;


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


    /* MÉTODO AUXILIAR DE DESENVOLVIMENTO
     Evita a perda de sessão ao salvar alterações no código

     ele busca o primeiro usuário cadastrado no banco de dados e o injeta automaticamente na sessão,
     evitando que você precise ficar fazendo login manualmente o tempo todo durante os testes.
     */
    private Usuario obterOuInjetarUsuarioDev(HttpSession session) {
        Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
        if (logado == null) {
            logado = usuarioRepository.findAll().stream().findFirst().orElse(null);
            if (logado != null) {
                session.setAttribute("usuarioLogado", logado);
            }
        }
        return logado;
    }


    // Rotas de autentificação de cadastro

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    @GetMapping("/cadastrar")
    public String paginaCadastro() {
        return "usuario/cadastrarUser";
    }

    @PostMapping("/cadastrar/salvar")
    public String salvarCadastro(Usuario usuario,
                                 @RequestParam("confirmarSenha") String confirmarSenha,
                                 Model model) {

        if (usuario.getCpf() != null) {
            String cpfLimpo = usuario.getCpf().replaceAll("[^0-9]", "");
            usuario.setCpf(cpfLimpo);
        }

        if (!isCpfValido(usuario.getCpf())) {
            model.addAttribute("erro", "O CPF informado é inválido!");
            return "usuario/cadastrarUser";
        }

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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Caminhos do acervo do usuário

    @GetMapping("/usuario/testUser")
    public String testUser(HttpSession session, Model model) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        List<Livro> livros = livroRepository.findAll();
        List<String> categorias = livros.stream()
                .map(Livro::getCategoria)
                .distinct()
                .filter(c -> c != null && !c.isEmpty())
                .sorted()
                .toList();

        model.addAttribute("livros", livros);
        model.addAttribute("categorias", categorias);
        model.addAttribute("usuarioNome", logado.getNome());
        return "usuario/testUser";
    }

    @GetMapping("/usuario/livro/{isbn}")
    public String detalhesLivro(@PathVariable("isbn") Long isbn, HttpSession session, Model model) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Livro> livroOpt = livroRepository.findById(isbn);
        if (livroOpt.isEmpty()) return "redirect:/usuario/testUser";

        Livro livro = livroOpt.get();

        boolean disponivel = livro.getExemplares() != null && livro.getExemplares().stream()
                .anyMatch(e -> e.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL);

        model.addAttribute("livro", livro);
        model.addAttribute("status", disponivel ? "DISPONIVEL" : "INDISPONIVEL");
        model.addAttribute("usuarioNome", logado.getNome());
        model.addAttribute("isFavorito", false);

        return "usuario/layoutLivro";
    }


    // Regras de negócio de empréstimo aplicadas

    @GetMapping("/usuario/emprestimo/{isbn}")
    public String paginaEmprestimoUsuario(@PathVariable("isbn") Long isbn, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Livro> livroOpt = livroRepository.findById(isbn);
        if (livroOpt.isEmpty()) return "redirect:/usuario/testUser";
        Livro livro = livroOpt.get();

        if (logado.getStatusPenalidade() != StatusPenalidade.ATIVO) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Você está suspenso(a).");
            return "redirect:/usuario/livro/" + isbn;
        }

        List<Emprestimo> ativos = emprestimoRepository.findAll().stream()
                .filter(e -> e.getUsuario().getId().equals(logado.getId()) && (e.getStatus() == StatusEmprestimo.EM_ANDAMENTO || e.getStatus() == StatusEmprestimo.AGUARDANDO_RETIRADA))
                .collect(Collectors.toList());

        boolean temAtraso = emprestimoRepository.findAll().stream()
                .anyMatch(e -> e.getUsuario().getId().equals(logado.getId()) && e.getStatus() == StatusEmprestimo.EM_ANDAMENTO && e.getDataEstimada().isBefore(LocalDate.now()));
        if (temAtraso) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Você possui livros em atraso! Efetue as devoluções pendentes primeiro.");
            return "redirect:/usuario/livro/" + isbn;
        }

        int limite = (logado.getTipoUsuario() == TipoUsuario.INSTRUTOR) ? 5 : 3;
        if (ativos.size() >= limite) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Limite máximo de " + limite + " livros simultâneos atingido.");
            return "redirect:/usuario/livro/" + isbn;
        }

        boolean jaPossuiMesmoLivro = ativos.stream()
                .anyMatch(e -> e.getExemplar().getLivro().getIsbn().equals(isbn));
        if (jaPossuiMesmoLivro) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Você já possui um exemplar deste livro em andamento ou aguardando retirada.");
            return "redirect:/usuario/livro/" + isbn;
        }

        int dias = 7;
        if (logado.getTipoUsuario() == TipoUsuario.INSTRUTOR) dias = 15;
        if (livro.getCategoriaRestricao() == CategoriaRestricao.RESTRITO) dias = 1;

        model.addAttribute("livro", livro);
        model.addAttribute("dataHoje", LocalDate.now());
        model.addAttribute("dataEstimada", LocalDate.now().plusDays(dias));

        return "usuario/emprestimoUsuario";
    }

    @PostMapping("/usuario/efetuar-emprestimo")
    public String efetuarEmprestimo(@RequestParam("isbn") Long isbn, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Livro> livroOpt = livroRepository.findById(isbn);
        if (livroOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Erro: O livro selecionado não existe.");
            return "redirect:/usuario/testUser";
        }
        Livro livro = livroOpt.get();

        if (logado.getStatusPenalidade() != StatusPenalidade.ATIVO) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Você está suspenso(a).");
            return "redirect:/usuario/livro/" + isbn;
        }

        List<Emprestimo> ativos = emprestimoRepository.findAll().stream()
                .filter(e -> e.getUsuario().getId().equals(logado.getId()) && (e.getStatus() == StatusEmprestimo.EM_ANDAMENTO || e.getStatus() == StatusEmprestimo.AGUARDANDO_RETIRADA))
                .collect(Collectors.toList());

        boolean temAtraso = emprestimoRepository.findAll().stream()
                .anyMatch(e -> e.getUsuario().getId().equals(logado.getId()) && e.getStatus() == StatusEmprestimo.EM_ANDAMENTO && e.getDataEstimada().isBefore(LocalDate.now()));
        if (temAtraso) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Você possui livros em atraso! Efetue as devoluções pendentes primeiro.");
            return "redirect:/usuario/livro/" + isbn;
        }

        int limite = (logado.getTipoUsuario() == TipoUsuario.INSTRUTOR) ? 5 : 3;
        if (ativos.size() >= limite) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Limite máximo de " + limite + " livros simultâneos atingido.");
            return "redirect:/usuario/livro/" + isbn;
        }

        boolean jaPossuiMesmoLivro = ativos.stream()
                .anyMatch(e -> e.getExemplar().getLivro().getIsbn().equals(isbn));
        if (jaPossuiMesmoLivro) {
            redirectAttributes.addFlashAttribute("erro", "Empréstimo negado: Você já possui um exemplar deste livro em andamento ou aguardando retirada.");
            return "redirect:/usuario/livro/" + isbn;
        }

        Optional<Exemplar> exemplarDisponivel = livro.getExemplares().stream()
                .filter(e -> e.getStatusDisponibilidade() == StatusDisponibilidade.DISPONIVEL)
                .findFirst();

        if (exemplarDisponivel.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Ops! Parece que o último exemplar disponível já foi alugado ou reservado por outra pessoa.");
            return "redirect:/usuario/livro/" + isbn;
        }

        int dias = 7;
        if (logado.getTipoUsuario() == TipoUsuario.INSTRUTOR) dias = 15;
        if (livro.getCategoriaRestricao() == CategoriaRestricao.RESTRITO) dias = 1;

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(logado);
        emprestimo.setExemplar(exemplarDisponivel.get());
        emprestimo.setDataAlugada(LocalDate.now());
        emprestimo.setDataEstimada(LocalDate.now().plusDays(dias));
        emprestimo.setStatus(StatusEmprestimo.AGUARDANDO_RETIRADA);

        Exemplar exemplar = exemplarDisponivel.get();
        exemplar.setStatusDisponibilidade(StatusDisponibilidade.INDISPONIVEL);

        exemplarRepository.save(exemplar);
        emprestimoRepository.save(emprestimo);

        redirectAttributes.addFlashAttribute("sucesso", "Solicitação enviada com sucesso! Dirija-se à biblioteca para retirar o exemplar físico do livro: " + livro.getTitulo());
        return "redirect:/usuario/testUser";
    }


    // Regras de negócio de reservas aplicadas


    @GetMapping("/usuario/reserva/{isbn}")
    public String paginaReservaUsuario(@PathVariable("isbn") Long isbn, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Livro> livroOpt = livroRepository.findById(isbn);
        if (livroOpt.isEmpty()) return "redirect:/usuario/testUser";
        Livro livro = livroOpt.get();

        if (logado.getStatusPenalidade() != StatusPenalidade.ATIVO) {
            redirectAttributes.addFlashAttribute("erro", "Reserva negada: Você está suspenso(a).");
            return "redirect:/usuario/livro/" + isbn;
        }

        boolean temAtraso = emprestimoRepository.findAll().stream()
                .anyMatch(e -> e.getUsuario().getId().equals(logado.getId()) && e.getStatus() == StatusEmprestimo.EM_ANDAMENTO && e.getDataEstimada().isBefore(LocalDate.now()));

        if (temAtraso) {
            redirectAttributes.addFlashAttribute("erro", "Reserva negada: Você possui livros em atraso! Efetue as devoluções pendentes primeiro.");
            return "redirect:/usuario/livro/" + isbn;
        }

        boolean jaPossuiReserva = reservaRepository.findAll().stream()
                .anyMatch(r -> r.getUsuario().getId().equals(logado.getId())
                        && r.getLivro().getIsbn().equals(isbn)
                        && "PENDENTE".equals(r.getStatus()));
        if (jaPossuiReserva) {
            redirectAttributes.addFlashAttribute("erro", "Reserva negada: Você já possui uma reserva ativa para este livro.");
            return "redirect:/usuario/livro/" + isbn;
        }

        model.addAttribute("livro", livro);
        model.addAttribute("dataMinima", LocalDate.now().toString());

        return "usuario/reservaUsuario";
    }

    @PostMapping("/usuario/efetuar-reserva")
    public String efetuarReserva(@RequestParam("isbn") Long isbn,
                                 @RequestParam("dataReserva") String dataReservaStr,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Livro> livroOpt = livroRepository.findById(isbn);
        if (livroOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Erro: O livro selecionado não existe.");
            return "redirect:/usuario/testUser";
        }
        Livro livro = livroOpt.get();

        LocalDate dataEscolhida = LocalDate.parse(dataReservaStr);
        if (dataEscolhida.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("erro", "Erro: A data da reserva não pode ser no passado.");
            return "redirect:/usuario/reserva/" + isbn;
        }

        Reserva reserva = new Reserva(logado, livro, dataEscolhida);
        reserva.setStatus("PENDENTE");
        reservaRepository.save(reserva);

        DateTimeFormatter formatoBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        redirectAttributes.addFlashAttribute("sucesso", "Reserva efetuada com sucesso para o dia " + dataEscolhida.format(formatoBr) + ". O livro ficará aguardando retirada por 48h assim que devolvido.");
        return "redirect:/usuario/testUser";
    }


    // Rota de empréstimo, histórico e cancelamentos do usuário

    @GetMapping("/usuario/emprestimos")
    public String meusEmprestimos(HttpSession session, Model model) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        List<Emprestimo> emprestimos = emprestimoRepository.findAll().stream()
                .filter(e -> e.getUsuario().getId().equals(logado.getId()))
                .collect(Collectors.toList());

        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario().getId().equals(logado.getId()))
                .collect(Collectors.toList());

        // Calcula a posição na fila de espera para cada reserva pendente
        Map<Long, Integer> posicoesFila = new HashMap<>();
        List<Reserva> todasReservas = reservaRepository.findAll();

        for (Reserva r : reservas) {
            if ("PENDENTE".equals(r.getStatus())) {
                List<Reserva> filaLivro = todasReservas.stream()
                        .filter(res -> res.getLivro().getIsbn().equals(r.getLivro().getIsbn()) && "PENDENTE".equals(res.getStatus()))
                        .sorted(Comparator.comparing(Reserva::getDataReserva).thenComparing(Reserva::getIdReserva))
                        .collect(Collectors.toList());

                int index = 1;
                for (Reserva resFila : filaLivro) {
                    if (resFila.getIdReserva().equals(r.getIdReserva())) {
                        posicoesFila.put(r.getIdReserva(), index);
                        break;
                    }
                    index++;
                }
            }
        }

        model.addAttribute("emprestimos", emprestimos);
        model.addAttribute("reservas", reservas);
        model.addAttribute("posicoesFila", posicoesFila);
        model.addAttribute("usuarioNome", logado.getNome());
        return "usuario/meusEmprestimos";
    }

    @GetMapping("/usuario/historico")
    public String historicoLeitura(HttpSession session, Model model) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        List<Emprestimo> historico = emprestimoRepository.findAll().stream()
                .filter(e -> e.getUsuario().getId().equals(logado.getId()) && e.getStatus() == StatusEmprestimo.DEVOLVIDO)
                .collect(Collectors.toList());

        model.addAttribute("historicoEmprestimos", historico);
        model.addAttribute("usuarioNome", logado.getNome());
        return "usuario/historico";
    }

    @PostMapping("/usuario/emprestimo/cancelar")
    public String cancelarEmprestimo(@RequestParam("idEmprestimo") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Emprestimo> empOpt = emprestimoRepository.findById(id);
        if (empOpt.isPresent()) {
            Emprestimo e = empOpt.get();
            if (e.getUsuario().getId().equals(logado.getId()) && (e.getStatus() == StatusEmprestimo.EM_ANDAMENTO || e.getStatus() == StatusEmprestimo.AGUARDANDO_RETIRADA)) {
                e.setStatus(StatusEmprestimo.DEVOLVIDO);
                e.setDataDevolucao(LocalDate.now());
                Exemplar ex = e.getExemplar();
                ex.setStatusDisponibilidade(StatusDisponibilidade.DISPONIVEL);
                exemplarRepository.save(ex);
                emprestimoRepository.save(e);
                redirectAttributes.addFlashAttribute("sucesso", "Empréstimo cancelado/devolvido com sucesso.");
            }
        }
        return "redirect:/usuario/emprestimos";
    }

    @PostMapping("/usuario/reserva/cancelar")
    public String cancelarReserva(@RequestParam("idReserva") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        if (logado == null) return "redirect:/login";

        Optional<Reserva> resOpt = reservaRepository.findById(id);
        if (resOpt.isPresent()) {
            Reserva r = resOpt.get();
            if (r.getUsuario().getId().equals(logado.getId())) {
                reservaRepository.delete(r);
                redirectAttributes.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
            }
        }
        return "redirect:/usuario/emprestimos";
    }


    //(Ajax Polling)

    @GetMapping("/usuario/api/notificacoes")
    @ResponseBody
    public Map<String, Object> checkUserNotifications(HttpSession session) {
        Usuario logado = obterOuInjetarUsuarioDev(session);
        Map<String, Object> result = new HashMap<>();
        if (logado == null) return result;

        List<Emprestimo> emprestimos = emprestimoRepository.findAll().stream()
                .filter(e -> e.getUsuario().getId().equals(logado.getId()) && e.getStatus() != null)
                .collect(Collectors.toList());

        @SuppressWarnings("unchecked")
        Map<Long, String> estadoAnterior = (Map<Long, String>) session.getAttribute("estadoEmprestimos");

        if (estadoAnterior == null) {
            estadoAnterior = new HashMap<>();
            for (Emprestimo e : emprestimos) {
                estadoAnterior.put(e.getIdEmprestimo(), e.getStatus().name());
            }
            session.setAttribute("estadoEmprestimos", estadoAnterior);
            result.put("changed", false);
            return result;
        }

        List<String> mensagens = new ArrayList<>();
        Map<Long, String> estadoAtual = new HashMap<>();
        boolean changed = false;

        for (Emprestimo e : emprestimos) {
            String statusAtual = e.getStatus().name();
            estadoAtual.put(e.getIdEmprestimo(), statusAtual);

            if (estadoAnterior.containsKey(e.getIdEmprestimo())) {
                String statusAntigo = estadoAnterior.get(e.getIdEmprestimo());

                if ("AGUARDANDO_RETIRADA".equals(statusAntigo) && "EM_ANDAMENTO".equals(statusAtual)) {
                    mensagens.add("Sua requisição de empréstimo do livro '" + e.getExemplar().getLivro().getTitulo() + "' foi aceita!");
                    changed = true;
                } else if ("AGUARDANDO_DEVOLUCAO".equals(statusAntigo) && "DEVOLVIDO".equals(statusAtual)) {
                    mensagens.add("Sua devolução do livro '" + e.getExemplar().getLivro().getTitulo() + "' foi confirmada e processada!");
                    changed = true;
                } else if (!statusAntigo.equals(statusAtual)) {
                    changed = true;
                }
            } else {
                changed = true;
            }
        }

        session.setAttribute("estadoEmprestimos", estadoAtual);
        result.put("mensagens", mensagens);
        result.put("changed", changed);

        return result;
    }
}