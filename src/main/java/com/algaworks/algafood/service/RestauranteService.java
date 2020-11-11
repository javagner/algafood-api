package com.algaworks.algafood.service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;

import com.algaworks.algafood.dto.CozinhaDTO;
import com.algaworks.algafood.dto.RestauranteEntradaDTO;
import com.algaworks.algafood.dto.RestauranteRetornoDTO;
import com.algaworks.algafood.entity.Cozinha;
import com.algaworks.algafood.entity.Restaurante;
import com.algaworks.algafood.exception.EntidadeEmUsoException;
import com.algaworks.algafood.exception.RestauranteNaoEncotradaException;
import com.algaworks.algafood.exception.ValidacaoException;
import com.algaworks.algafood.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RestauranteService {
	
	private static final String MSG_RESTAURANTE_EM_USO = "	Restaurante id %d não pode ser removida, está em uso!";

	@Autowired
	private RestauranteRepository restauranteRepository;
	
	@Autowired
	private CozinhaService cozinhaService;
	
	@Autowired
	private SmartValidator smartValidator; // Validador do spring.framework
	
	public Restaurante buscarPorId(Long restauranteId) {
		return restauranteRepository.findById(restauranteId)
				.orElseThrow(() -> new RestauranteNaoEncotradaException(restauranteId) );
	}
	
	@Transactional
	public Restaurante salvar(Restaurante restaurante) {
		Cozinha cozinha = cozinhaService.buscarPorId(restaurante.getCozinha().getId());
		restaurante.setCozinha(cozinha);
		return restauranteRepository.save(restaurante);
	}
	
	@Transactional
	public void remover(Long restauranteId) {
		try {
			restauranteRepository.deleteById(restauranteId);
			
		} catch (EmptyResultDataAccessException e) {
			throw new RestauranteNaoEncotradaException(restauranteId);
		
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoException(String.format(MSG_RESTAURANTE_EM_USO, restauranteId));
		
		}
	}	
	
	public void mergeCampos(Map<String, Object> camposAtualizados, Restaurante restauranteAtual, HttpServletRequest request) {
		
		ServletServerHttpRequest serverHttpRequest = new ServletServerHttpRequest(request);
		
		try {
			
			ObjectMapper objectMapper = new ObjectMapper(); // Converte JSON em Java e Java em JSON		
			objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true); // Confiruado para dar erro quando for passado algum objeto que está marcado com @Jsonignore
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true); // Falha caso a propriedade não exista
			Restaurante restauranteOrigem = objectMapper.convertValue(camposAtualizados, Restaurante.class);		
			
			System.out.println(restauranteOrigem);
			
			camposAtualizados.forEach((chaveCampo, valorCampo) -> {
				Field campo = ReflectionUtils.findField(Restaurante.class, chaveCampo); // Busca na classe Restaurante um campo com o nome passado na chaveCampo
				campo.setAccessible(true); // Torna a variável que é private acessível aqui.			
				Object novoValor = ReflectionUtils.getField(campo, restauranteOrigem);		
				ReflectionUtils.setField(campo, restauranteAtual, novoValor);			
			});
			
		} catch (IllegalArgumentException e) {
			Throwable causaRaiz = ExceptionUtils.getRootCause(e);
			throw new HttpMessageNotReadableException(e.getMessage(), causaRaiz, serverHttpRequest);
		}
		
	}
	
	public void valida(Restaurante restauranteAtual, String objectName) {
		/*
		 * Classe que implementa BidingResult que extende de Erros para poder ser usada
		 * como parâmetro no método validade abaixo.
		 * */
		BeanPropertyBindingResult beanPropertyBindingResult = new BeanPropertyBindingResult(restauranteAtual, objectName);		
		smartValidator.validate(restauranteAtual, beanPropertyBindingResult);
		
		if (beanPropertyBindingResult.hasErrors()) {
			throw new ValidacaoException(beanPropertyBindingResult);
		}
	}
	
	public RestauranteRetornoDTO converterParaDTO(Restaurante restaurante) {
		CozinhaDTO cozinhaDTO = new CozinhaDTO();
		cozinhaDTO.setId(restaurante.getCozinha().getId());
		cozinhaDTO.setNome(restaurante.getCozinha().getNome());
		
		RestauranteRetornoDTO restauranteDTO = new RestauranteRetornoDTO();
		restauranteDTO.setId(restaurante.getId());
		restauranteDTO.setNome(restaurante.getNome());
		restauranteDTO.setTaxaFrete(restaurante.getTaxaFrete());
		restauranteDTO.setCozinhaDTO(cozinhaDTO);
		return restauranteDTO;
	}
	
	public List<RestauranteRetornoDTO> converterListaParaDTO(List<Restaurante> restaurantes) {
		return restaurantes.stream()
							.map(restaurante -> converterParaDTO(restaurante))
							.collect(Collectors.toList());
	}
	
	public Restaurante converterParaObjeto(RestauranteEntradaDTO restauranteEntradaDTO) {
		// TODO: Refatorar para builder do lombok
		Restaurante restaurante = new Restaurante();
		restaurante.setNome(restauranteEntradaDTO.getNome());
		restaurante.setTaxaFrete(restauranteEntradaDTO.getTaxaFrete());
		
		Cozinha cozinha = new Cozinha();
		cozinha.setId(restauranteEntradaDTO.getCozinha().getId());
		
		restaurante.setCozinha(cozinha);
		
		return restaurante;
		
	}
	
}
