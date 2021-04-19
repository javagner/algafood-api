package com.algaworks.algafood.service;

import java.util.List;

import com.algaworks.algafood.dto.input.CidadeInputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algaworks.algafood.dto.CidadeDTO;
import com.algaworks.algafood.dto.conversor.CidadeConversor;
import com.algaworks.algafood.entity.Cidade;
import com.algaworks.algafood.entity.Estado;
import com.algaworks.algafood.exception.CidadeNaoEncotradaException;
import com.algaworks.algafood.exception.EntidadeEmUsoException;
import com.algaworks.algafood.exception.EntidadeNaoEncotradaException;
import com.algaworks.algafood.exception.EstadoNaoEncotradaException;
import com.algaworks.algafood.exception.NegocioException;
import com.algaworks.algafood.repository.CidadeRepository;
import com.algaworks.algafood.repository.EstadoRepository;

@Service
public class CidadeService {
	
	private static final String MSG_CIDADE_EM_USO = "Cidade com id %d não pode ser removida, está em uso!";

	@Autowired
	private CidadeRepository repository;
	
	@Autowired
	private EstadoRepository estadoRepository;
	
	@Autowired
	private CidadeConversor conversor;
	
	public CollectionModel<CidadeDTO> listar() {
		List<CidadeDTO> cidadesDTO = conversor.converterListaParaDTO(repository.findAll());
		return new CollectionModel<>(cidadesDTO);
	}
	
	public Cidade buscarPorId(Long id) {
		return repository.findById(id).orElseThrow(() -> new CidadeNaoEncotradaException(id));
	}
	
	public CidadeDTO buscarDtoPorId(Long id) {
		Cidade cidade = buscarPorId(id);	
		return conversor.converterParaDTO(cidade);
	}
	
	@Transactional
	public CidadeDTO salvar(CidadeInputDTO dto) {
		
		try {				
			Long estadoId = dto.getEstadoId();
			Estado estado = estadoRepository.findById(estadoId).orElseThrow(() -> new EstadoNaoEncotradaException(estadoId));
			Cidade cidade = new Cidade();
			cidade.setNome(dto.getNome());
			cidade.setEstado(estado);
			return conversor.converterParaDTO(repository.save(cidade));
		} catch (EntidadeNaoEncotradaException e) {
			throw new NegocioException(e.getMessage());
		}		
		
	}
	
	@Transactional
	public CidadeDTO atualizar(Long id, CidadeDTO dto) {
		try {
			Cidade cidadeAtual = repository.findById(id).orElseThrow(() -> new CidadeNaoEncotradaException(id));
			
			// Se quiser alterar o estado, basta passar o novo estado.id no JSON de CidadeDTO
			if (dto.getEstado() != null) {
				Long estadoId = dto.getEstado().getId();
				Estado novoEstado = estadoRepository.findById(estadoId).orElseThrow(() -> new EstadoNaoEncotradaException(estadoId));
				cidadeAtual.setEstado(novoEstado);
			}
			
			conversor.copiarParaObjeto(dto, cidadeAtual);
			return conversor.converterParaDTO(repository.save(cidadeAtual));
		} catch (EstadoNaoEncotradaException e) {
			throw new NegocioException(e.getMessage());
		}
	}
	
	@Transactional
	public void remover(Long id) {
		try {
			repository.deleteById(id);
			repository.flush();
		} catch (EmptyResultDataAccessException e) {
			throw new CidadeNaoEncotradaException(id);
		
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoException(String.format(MSG_CIDADE_EM_USO, id) );			
		}
	}

}
