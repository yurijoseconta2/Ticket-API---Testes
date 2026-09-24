package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.CompraIngressoRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Cliente;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.model.Ingresso;
import com.example.ticktetapi.model.StatusIngresso;
import com.example.ticktetapi.repository.ClienteRepository;
import com.example.ticktetapi.repository.EventoRepository;
import com.example.ticktetapi.repository.IngressoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class IngressoServiceTest {

    @Mock
    private IngressoRepository ingressoRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private IngressoService ingressoService;


    @Test
    void deveListarTodosOsIngressos() {
        Ingresso ingresso1 = mock(Ingresso.class);
        Ingresso ingresso2 = mock(Ingresso.class);

        List<Ingresso> ingressos = Arrays.asList(ingresso1, ingresso2);

        when(ingressoRepository.findAll()).thenReturn(ingressos);

        List<Ingresso> resultado = ingressoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals(ingressos, resultado);

        verify(ingressoRepository).findAll();
    }


    @Test
    void deveBuscarIngressoPorId() {
        Long id = 1L;
        Ingresso ingresso = mock(Ingresso.class);

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.of(ingresso));

        Ingresso resultado = ingressoService.buscarPorId(id);

        assertEquals(ingresso, resultado);

        verify(ingressoRepository).findById(id);
    }


    @Test
    void deveLancarExcecaoQuandoIngressoNaoForEncontrado() {
        Long id = 1L;

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ingressoService.buscarPorId(id)
        );

        verify(ingressoRepository).findById(id);
    }


    @Test
    void deveListarIngressosPorEvento() {
        Long eventoId = 1L;

        Ingresso ingresso = mock(Ingresso.class);
        List<Ingresso> ingressos = Collections.singletonList(ingresso);

        when(ingressoRepository.findByEventoId(eventoId))
                .thenReturn(ingressos);

        List<Ingresso> resultado = ingressoService.listarPorEvento(eventoId);

        assertEquals(ingressos, resultado);

        verify(ingressoRepository).findByEventoId(eventoId);
    }


    @Test
    void deveListarIngressosPorCliente() {
        Long clienteId = 1L;

        Ingresso ingresso = mock(Ingresso.class);
        List<Ingresso> ingressos = Collections.singletonList(ingresso);

        when(ingressoRepository.findByClienteId(clienteId))
                .thenReturn(ingressos);

        List<Ingresso> resultado = ingressoService.listarPorCliente(clienteId);

        assertEquals(ingressos, resultado);

        verify(ingressoRepository).findByClienteId(clienteId);
    }


    @Test
    void deveComprarIngressosComSucesso() {
        CompraIngressoRequestDTO dto = mock(CompraIngressoRequestDTO.class);
        Evento evento = mock(Evento.class);
        Cliente cliente = mock(Cliente.class);

        when(dto.getEventoId()).thenReturn(1L);
        when(dto.getClienteId()).thenReturn(2L);
        when(dto.getQuantidade()).thenReturn(2);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        when(clienteRepository.findById(2L))
                .thenReturn(Optional.of(cliente));

        when(evento.getDataHora())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(evento.getQuantidadeDisponivel())
                .thenReturn(10);

       when(evento.getPreco())
        .thenReturn(new BigDecimal("50.00"));

        when(ingressoRepository.save(any(Ingresso.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Ingresso> resultado = ingressoService.comprar(dto);

        assertEquals(2, resultado.size());

        verify(ingressoRepository, times(2))
                .save(any(Ingresso.class));

        verify(eventoRepository)
                .save(evento);

        verify(evento)
                .setQuantidadeDisponivel(8);
    }


    @Test
    void deveLancarExcecaoQuandoEventoNaoForEncontradoNaCompra() {
        CompraIngressoRequestDTO dto = mock(CompraIngressoRequestDTO.class);

        when(dto.getEventoId()).thenReturn(1L);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ingressoService.comprar(dto)
        );

        verify(clienteRepository, never()).findById(anyLong());
        verify(ingressoRepository, never()).save(any(Ingresso.class));
    }


    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontradoNaCompra() {
        CompraIngressoRequestDTO dto = mock(CompraIngressoRequestDTO.class);
        Evento evento = mock(Evento.class);

        when(dto.getEventoId()).thenReturn(1L);
        when(dto.getClienteId()).thenReturn(2L);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        when(clienteRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ingressoService.comprar(dto)
        );

        verify(ingressoRepository, never()).save(any(Ingresso.class));
    }


    @Test
    void naoDevePermitirCompraDeEventoQueJaOcorreu() {
        CompraIngressoRequestDTO dto = mock(CompraIngressoRequestDTO.class);
        Evento evento = mock(Evento.class);
        Cliente cliente = mock(Cliente.class);

        when(dto.getEventoId()).thenReturn(1L);
        when(dto.getClienteId()).thenReturn(2L);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        when(clienteRepository.findById(2L))
                .thenReturn(Optional.of(cliente));

        when(evento.getDataHora())
                .thenReturn(LocalDateTime.now().minusDays(1));

        assertThrows(
                RegraNegocioException.class,
                () -> ingressoService.comprar(dto)
        );

        verify(ingressoRepository, never()).save(any(Ingresso.class));
    }


    @Test
    void naoDevePermitirCompraQuandoNaoHaIngressosSuficientes() {
        CompraIngressoRequestDTO dto = mock(CompraIngressoRequestDTO.class);
        Evento evento = mock(Evento.class);
        Cliente cliente = mock(Cliente.class);

        when(dto.getEventoId()).thenReturn(1L);
        when(dto.getClienteId()).thenReturn(2L);
        when(dto.getQuantidade()).thenReturn(5);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        when(clienteRepository.findById(2L))
                .thenReturn(Optional.of(cliente));

        when(evento.getDataHora())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(evento.getQuantidadeDisponivel())
                .thenReturn(2);

        assertThrows(
                RegraNegocioException.class,
                () -> ingressoService.comprar(dto)
        );

        verify(ingressoRepository, never()).save(any(Ingresso.class));
    }


    @Test
    void deveCancelarIngressoAtivo() {
        Long id = 1L;

        Ingresso ingresso = mock(Ingresso.class);
        Evento evento = mock(Evento.class);

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.of(ingresso));

        when(ingresso.getStatus())
                .thenReturn(StatusIngresso.ATIVO);

        when(ingresso.getEvento())
                .thenReturn(evento);

        when(evento.getQuantidadeDisponivel())
                .thenReturn(5);

        when(ingressoRepository.save(ingresso))
                .thenReturn(ingresso);

        Ingresso resultado = ingressoService.cancelar(id);

        assertEquals(ingresso, resultado);

        verify(ingresso).setStatus(StatusIngresso.CANCELADO);
        verify(ingressoRepository).save(ingresso);
        verify(evento).setQuantidadeDisponivel(6);
        verify(eventoRepository).save(evento);
    }


    @Test
    void naoDeveCancelarIngressoJaCancelado() {
        Long id = 1L;

        Ingresso ingresso = mock(Ingresso.class);

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.of(ingresso));

        when(ingresso.getStatus())
                .thenReturn(StatusIngresso.CANCELADO);

        assertThrows(
                RegraNegocioException.class,
                () -> ingressoService.cancelar(id)
        );

        verify(ingressoRepository, never()).save(ingresso);
    }


    @Test
    void naoDeveCancelarIngressoUtilizado() {
        Long id = 1L;

        Ingresso ingresso = mock(Ingresso.class);

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.of(ingresso));

        when(ingresso.getStatus())
                .thenReturn(StatusIngresso.UTILIZADO);

        assertThrows(
                RegraNegocioException.class,
                () -> ingressoService.cancelar(id)
        );

        verify(ingressoRepository, never()).save(ingresso);
    }


    @Test
    void deveMarcarIngressoComoUtilizado() {
        Long id = 1L;

        Ingresso ingresso = mock(Ingresso.class);

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.of(ingresso));

        when(ingresso.getStatus())
                .thenReturn(StatusIngresso.ATIVO);

        when(ingressoRepository.save(ingresso))
                .thenReturn(ingresso);

        Ingresso resultado = ingressoService.marcarComoUtilizado(id);

        assertEquals(ingresso, resultado);

        verify(ingresso).setStatus(StatusIngresso.UTILIZADO);
        verify(ingressoRepository).save(ingresso);
    }


    @Test
    void naoDeveMarcarIngressoNaoAtivoComoUtilizado() {
        Long id = 1L;

        Ingresso ingresso = mock(Ingresso.class);

        when(ingressoRepository.findById(id))
                .thenReturn(Optional.of(ingresso));

        when(ingresso.getStatus())
                .thenReturn(StatusIngresso.CANCELADO);

        assertThrows(
                RegraNegocioException.class,
                () -> ingressoService.marcarComoUtilizado(id)
        );

        verify(ingressoRepository, never()).save(ingresso);
    }
}