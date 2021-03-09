package com.algaworks.algafood.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import com.algaworks.algafood.event.PedidoCanceladoEvent;
import com.algaworks.algafood.event.PedidoConfirmadoEvent;
import org.hibernate.annotations.CreationTimestamp;

import com.algaworks.algafood.enuns.StatusPedido;
import com.algaworks.algafood.exception.NegocioException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.AbstractAggregateRoot;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
public class Pedido extends AbstractAggregateRoot<Pedido> {
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String codigo;
	
	@Column(nullable = false)
	private BigDecimal subtotal;
	
	@Column(nullable = false)
	private BigDecimal taxaFrete;
	
	@Column(nullable = false)
	private BigDecimal valorTotal;
	
	@CreationTimestamp
	@Column(nullable = false, columnDefinition = "datetime")
	private OffsetDateTime dataCriacao;
	
	private OffsetDateTime dataConfirmacao;
	private OffsetDateTime dataCancelamento;
	private OffsetDateTime dataEntrega;
	
	@ManyToOne
	@JoinColumn(name = "usuario_cliente_id", nullable = false)
	private Usuario usuarioCliente;
	
	@Embedded
	@Column(nullable = false)
	private Endereco enderecoEntrega;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Restaurante restaurante;
		
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private FormaPagamento formaPagamento;
	
	@Enumerated(EnumType.STRING)
	private StatusPedido status = StatusPedido.CRIADO;
	
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
	private List<ItemPedido> itens = new ArrayList<>();
		
	public void calcularValorTotal() {
		
		getItens().forEach(ItemPedido::getPrecoTotal);
		
		// TODO: Verificar melhor como funciona o reduce
		this.subtotal = getItens().stream()
									.map(item -> item.getPrecoTotal())
									.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		this.valorTotal = this.subtotal.add(this.taxaFrete);
		
	}
	
	public void statusConfirmado() {
		setStatus(StatusPedido.CONFIRMADO);
		setDataConfirmacao(OffsetDateTime.now());

		// this é a instância atual do pedido que está sendo confirmado no momento
		registerEvent(new PedidoConfirmadoEvent(this));
	}
	
	public void stausEntregue() {
		setStatus(StatusPedido.ENTREGUE);
		setDataEntrega(OffsetDateTime.now());
	}
	
	public void stausCancelado() {
		setStatus(StatusPedido.CANCELADO);
		setDataCancelamento(OffsetDateTime.now());
		registerEvent(new PedidoCanceladoEvent(this));
	}
	
	/*
	 * É private para que apenas seja chamado dentro da classe, como a entidade é rica, as tratativas ficam nela mesma.
	 */
	private void setStatus(StatusPedido novoStatus) {
		if (!getStatus().podeAlterarPara(novoStatus)) {
			throw new NegocioException(String.format("Status do pedido %s não pode ser alterado de %s para %s", getCodigo(), getStatus().getDescricao(), novoStatus.getDescricao()));
		}
		
		this.status = novoStatus;
	}
	
	/*
	 * Método de callback do JPA, é executado em alguns eventos do ciclo de vida das entidades
	 * um dos eventos é o @PrePersist, ele vai fazer com que antes de persistir a entidade esse
	 * método seja executado, antes de fazer o insert no caso.
	 */
	@PrePersist
	public void gerarCodigo() {
		setCodigo(UUID.randomUUID().toString());
	}

}
