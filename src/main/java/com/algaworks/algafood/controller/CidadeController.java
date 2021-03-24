package com.algaworks.algafood.controller;

import java.util.List;

import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.dto.CidadeDTO;
import com.algaworks.algafood.service.CidadeService;

@Api(tags = "Cidades")
@RestController
@RequestMapping("/cidades")
public class CidadeController {

	@Autowired
	private CidadeService service;

	@ApiOperation("Lista as cidades")
	@GetMapping
	public List<CidadeDTO> listar() {
		return service.listar();
	}

	@ApiOperation("Busca uma cidade por id")
	@GetMapping("/{id}")
	public CidadeDTO buscarPorId(
			@ApiParam(value = "id de uma cidade", example = "1") @PathVariable Long id
	) {
		return service.buscarDtoPorId(id);
	}

	@ApiOperation("Cadastra uma cidade")
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public CidadeDTO salvar(
			@ApiParam(name = "corpo", value = "Representação de uma nova cidade") @RequestBody @Valid CidadeDTO dto
	) {
		return service.salvar(dto);
	}

	@ApiOperation("Atualiza uma cidade por id")
	@PutMapping("/{id}")
	public CidadeDTO atualizar(
			@ApiParam(value = "id de uma cidade", example = "1") @PathVariable Long id,
			@ApiParam(name = "corpo", value = "Representação de uma cidade atualizada") @RequestBody @Valid CidadeDTO dto
	) {
		return service.atualizar(id, dto);
	}

	@ApiOperation("Exclui uma cidade por ai")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void remover(
			@ApiParam(value = "id de uma cidade", example = "1") @PathVariable Long id
	) {
		service.remover(id);
	}

}
