package com.algaworks.algafood.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestauranteRetornoDTO {
	
	private Long id;
	private String nome;
	private BigDecimal taxaFrete;
	private CozinhaEntradaDTO cozinha;

}
