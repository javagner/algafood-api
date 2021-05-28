package com.algaworks.algafood.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.algaworks.algafood.enuns.TipoProblema;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;


/*
 * ResponseEntityExceptionHandler já é a classe padrão do Spring para tratativa de exceptions do Spring
 * 
 * Também Dentro deste componente podem ser adicionados ExceptionHandler para que todos os erros de controllers 
 * sejam tratados aqui
 * */

@Slf4j
@ControllerAdvice
public class TrataExcecoesDaAPI extends ResponseEntityExceptionHandler {
	private static final String MSG_ERRO_GENERICO_USUARIO_FINAL = "Ocorreu um erro interno inesperado. Tente novamente, se o problema persistir, entre em contato com o administrador do sistema";

	@Autowired
	private MessageSource messageSource;
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
	    
		Throwable causaRaiz = ExceptionUtils.getRootCause(ex); // Método do apache commons que vai em toda a stack da exceção e busca a causa raiz.
		
		if (causaRaiz instanceof InvalidFormatException) {
			return tratarInvalidFormatException((InvalidFormatException) causaRaiz, headers, status, request);
		}
		
		if (causaRaiz instanceof IgnoredPropertyException) {
			return tratarPropertyBindingException((IgnoredPropertyException) causaRaiz, headers, status, request);
		}
		
		if (causaRaiz instanceof UnrecognizedPropertyException) {
			return tratarUnrecognizedPropertyException((UnrecognizedPropertyException) causaRaiz, headers, status, request);
		}
		
		Problema problema = criarUmProblema(status, TipoProblema.CORPO_ILEGIVEL, "O corpo da requisição é inválido, favor verificar erros de sintaxe")
									.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
									.build();
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}

	@ExceptionHandler(EntidadeNaoEncotradaException.class)
	public ResponseEntity<?> tratarEntidadeNaoEncotradaException( EntidadeNaoEncotradaException ex, WebRequest request ) {
	    HttpStatus status = HttpStatus.NOT_FOUND;  
	    Problema problema = criarUmProblema(status, TipoProblema.RECURSO_NAO_ENCONTRADO, ex.getMessage())
	    							.mensagemParaUsuario(ex.getMessage())
	    							.build();
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<?> tratarEntidadeEmUsoException( EntidadeEmUsoException ex, WebRequest request ) {
		HttpStatus status = HttpStatus.CONFLICT;
		Problema problema = criarUmProblema(status, TipoProblema.ENTIDADE_EM_USO, ex.getMessage())
									.mensagemParaUsuario(ex.getMessage())
									.build();		
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);		
	}
	
	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<?> tratarNegocioException( NegocioException ex, WebRequest request ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		Problema problema = criarUmProblema(status, TipoProblema.NEGOCIO, ex.getMessage())									
									.mensagemParaUsuario(ex.getMessage())
									.build();
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);		
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> tratarMethodArgumentTypeMismatchException( MethodArgumentTypeMismatchException ex, WebRequest request ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String detalhe = String.format("O parâmetro de URL '%s' recebeu um valor '%s' que é do tipo inválido, por gentileza informe um valor do tipo '%s' ", ex.getName(), ex.getValue(), ex.getRequiredType());
		Problema problema = criarUmProblema(status, TipoProblema.PARAMETRO_INVALIDO, detalhe)
									.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
									.build();			
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> trataExceptionGenerico(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		String detalhe = MSG_ERRO_GENERICO_USUARIO_FINAL;
		Problema problema = criarUmProblema(status, TipoProblema.ERRO_DE_SISTEMA, detalhe)
									.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
									.build();
		log.error("Erro não tratado! {}", ex.getMessage());
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,	HttpStatus status, WebRequest request) {
		String detalhe = String.format("O recurso '%s' que você tentou acessar é inexistente, por gentileza informe um recurso válido.", ex.getRequestURL());
		Problema problema = criarUmProblema(status, TipoProblema.RECURSO_NAO_ENCONTRADO, detalhe)
									.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
									.build();	
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {		
		/* Uma instância de BindingResult armazena as violações de constraints de validação
		 * dentro desse bindingResult podemos obter queis foram os campos violados */		
		return trataExcecao(ex.getBindingResult(), ex, headers, status, request);
	}
		
	@ExceptionHandler(ValidacaoException.class)
	protected ResponseEntity<Object> tratarValidacaoException(ValidacaoException ex, WebRequest request) {		
		return trataExcecao(ex.getBindingResult(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request); 
	}
	
	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return trataExcecao(ex.getBindingResult(), ex, headers, status, request);
	}
	
	private ResponseEntity<Object> trataExcecao(BindingResult bindingResult, Exception ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		List<Problema.Objeto> objetosComProblema = 
				bindingResult.getAllErrors().stream().map(objectError -> { 
					String mensagem = messageSource.getMessage(objectError, new Locale ("pt", "BR"));						
					String nome = objectError.getObjectName();
					
					if (objectError instanceof FieldError) {
						nome = ((FieldError) objectError).getField();
					}
																	
					return Problema.Objeto.builder()
									.nome(nome)
									.mensagemUsuario(mensagem)
								 .build();
				   }
				).collect(Collectors.toList());
		
		String detalhe = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente";
		Problema problema = criarUmProblema(status, TipoProblema.DADOS_INVALIDOS, detalhe)
									.mensagemParaUsuario(detalhe)
									.objetos(objetosComProblema)
									.build();
		
		return handleExceptionInternal(ex, problema, headers, status, request);
	}
		
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object object, HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		if (object == null) {
			
			object = Problema.builder()
					.titulo(status.getReasonPhrase())
					.status(status.value())
					.dataHoraAtual(OffsetDateTime.now())
					.build();
			
		} else if (object instanceof String) {
			
			object = Problema.builder()
					.titulo((String) object )
					.status(status.value())
					.dataHoraAtual(OffsetDateTime.now())
					.build();
			
		}
		
		return super.handleExceptionInternal(ex, object, headers, status, request);
	}
	
	/*
	 * O builder é criado automáticamente pelo Lombok dentro da classe marcada com @Builder
	 * */
	private Problema.ProblemaBuilder criarUmProblema(HttpStatus status, TipoProblema tipoProblema, String detalhe) {
		
		return Problema.builder()
					   .status(status.value())
					   .tipo(tipoProblema.getUri())
					   .titulo(tipoProblema.getTitulo())
					   .detalhe(detalhe)
					   .dataHoraAtual(OffsetDateTime.now());
	}
	
	private ResponseEntity<Object> tratarInvalidFormatException(InvalidFormatException causaRaiz, HttpHeaders headers, HttpStatus status, WebRequest request) {
		String caminho = formatarCaminho(causaRaiz.getPath());		
		String detalhe = String.format("O parâmetro '%s' recebeu um valor '%s' que não é compatível. Por gentileza verificar, o correto é o tipo '%s'",caminho, causaRaiz.getValue(), causaRaiz.getTargetType().getSimpleName());
		Problema problema = criarUmProblema(status, TipoProblema.CORPO_ILEGIVEL, detalhe)
								.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
								.build();		
		return handleExceptionInternal(causaRaiz, problema, headers, status, request);
	}
	
	private ResponseEntity<Object> tratarPropertyBindingException(PropertyBindingException causaRaiz, HttpHeaders headers, HttpStatus status, WebRequest request) {
		String caminho = formatarCaminho(causaRaiz.getPath());
		String detalhe = String.format("O campo '%s' está sendo ignorado e não deve ser enviado na requisição.", caminho);
		Problema problema = criarUmProblema(status, TipoProblema.CORPO_ILEGIVEL, detalhe)
									.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
									.build();	
		return handleExceptionInternal(causaRaiz, problema, headers, status, request);
	}

	private ResponseEntity<Object> tratarUnrecognizedPropertyException(UnrecognizedPropertyException causaRaiz,	HttpHeaders headers, HttpStatus status, WebRequest request) {
		String caminho = formatarCaminho(causaRaiz.getPath());
		String detalhe = String.format("O campo '%s' não existe, por gentileza verificar", caminho);
		Problema problema = criarUmProblema(status, TipoProblema.CORPO_ILEGIVEL, detalhe)
									.mensagemParaUsuario(MSG_ERRO_GENERICO_USUARIO_FINAL)
									.build();	
		return handleExceptionInternal(causaRaiz, problema, headers, status, request);
	}
	
	private String formatarCaminho(List<Reference> references) {
		/*
		 * O método getPath() retorna uma lista com os campos, caso seja um parâmetro aninhado como no caso de 
		 * cozinha: {id: ""} passado no JSON, vai retornar dois campos cozinha e id.
		 * No stream abaixo eu faço um map e tenho uma lista com esses fieldNames
		 * o Collectors.joining concatena os filedNames com o .
		 * */
		String caminho = references.stream()
								   .map(reference -> reference.getFieldName())
								   .collect(Collectors.joining("."));
		return caminho;
	}

}
