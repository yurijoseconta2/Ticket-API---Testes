package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.EventoRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.repository.EventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private EventoService eventoService;

    private EventoRequestDTO dto;
    private Evento evento;

    @BeforeEach
    void setUp() {
        dto = new EventoRequestDTO();
        dto.setNome("Show de Rock");
        dto.setDescricao("Show de rock ao vivo");
        dto.setDataHora(LocalDateTime.now().plusDays(10));
        dto.setLocal("Recife");
        dto.setPreco(new BigDecimal("50.00"));
        dto.setQuantidadeTotal(100);

        evento = Evento.builder()
                .id(1L)
                .nome("Show de Rock")
                .descricao("Show de rock ao vivo")
                .dataHora(dto.getDataHora())
                .local("Recife")
                .preco(new BigDecimal("50.00"))
                .quantidadeTotal(100)
                .quantidadeDisponivel(100)
                .build();
    }

    @Test
    void deveCriarEvento() {
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        Evento resultado = eventoService.criar(dto);

        assertNotNull(resultado);
        assertEquals("Show de Rock", resultado.getNome());
        assertEquals(100, resultado.getQuantidadeTotal());
        assertEquals(100, resultado.getQuantidadeDisponivel());

        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void deveListarTodosOsEventos() {
        when(eventoRepository.findAll()).thenReturn(List.of(evento));

        List<Evento> resultado = eventoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(evento, resultado.get(0));

        verify(eventoRepository).findAll();
    }

    @Test
    void deveBuscarEventoPorId() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        Evento resultado = eventoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Show de Rock", resultado.getNome());

        verify(eventoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoEventoNaoForEncontrado() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> eventoService.buscarPorId(1L)
        );

        verify(eventoRepository).findById(1L);
    }

    @Test
    void deveAtualizarEvento() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        dto.setNome("Novo Show");
        dto.setPreco(new BigDecimal("75.00"));
        dto.setQuantidadeTotal(120);

        Evento resultado = eventoService.atualizar(1L, dto);

        assertNotNull(resultado);
        assertEquals("Novo Show", resultado.getNome());
        assertEquals(new BigDecimal("75.00"), resultado.getPreco());
        assertEquals(120, resultado.getQuantidadeTotal());
        assertEquals(120, resultado.getQuantidadeDisponivel());

        verify(eventoRepository).findById(1L);
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void deveAtualizarQuantidadeMantendoIngressosVendidos() {
        evento.setQuantidadeTotal(100);
        evento.setQuantidadeDisponivel(70);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        dto.setQuantidadeTotal(120);

        Evento resultado = eventoService.atualizar(1L, dto);

        assertEquals(120, resultado.getQuantidadeTotal());
        assertEquals(90, resultado.getQuantidadeDisponivel());

        verify(eventoRepository).save(evento);
    }

    @Test
    void deveLancarExcecaoAoReduzirQuantidadeAbaixoDosIngressosVendidos() {
        evento.setQuantidadeTotal(100);
        evento.setQuantidadeDisponivel(70);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        dto.setQuantidadeTotal(20);

        assertThrows(
                RegraNegocioException.class,
                () -> eventoService.atualizar(1L, dto)
        );

        verify(eventoRepository, never()).save(any(Evento.class));
    }

    @Test
    void deveDeletarEventoSemIngressosVendidos() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        eventoService.deletar(1L);

        verify(eventoRepository).findById(1L);
        verify(eventoRepository).delete(evento);
    }

    @Test
    void deveLancarExcecaoAoDeletarEventoComIngressosVendidos() {
        evento.setQuantidadeTotal(100);
        evento.setQuantidadeDisponivel(90);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        assertThrows(
                RegraNegocioException.class,
                () -> eventoService.deletar(1L)
        );

        verify(eventoRepository, never()).delete(any(Evento.class));
    }

    @Test
    void deveLancarExcecaoAoDeletarEventoInexistente() {
        when(eventoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> eventoService.deletar(1L)
        );

        verify(eventoRepository, never()).delete(any(Evento.class));
    }
}