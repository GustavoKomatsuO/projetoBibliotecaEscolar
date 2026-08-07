package com.IEGP3.bibliotecaEscolar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // <- Essa anotação é OBRIGATÓRIA para o Spring reconhecer como executável
public class BibliotecaEscolarApplication {

	public static void main(String[] args) { // <- O método main é o que dá o botão de Play
		SpringApplication.run(BibliotecaEscolarApplication.class, args);
	}
}