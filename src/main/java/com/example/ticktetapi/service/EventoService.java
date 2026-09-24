package com.example.ticktetapi.service;

import com.example.ticktetapi.dto.EventoRequestDTO;
import com.example.ticktetapi.exception.RecursoNaoEncontradoException;
import com.example.ticktetapi.exception.RegraNegocioException;
import com.example.ticktetapi.model.Evento;
import com.example.ticktetapi.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional
    public Evento criar(EventoRequestDTO dto) {
        Evento evento = new Evento();

        evento.setNome(dto.getNome());
        evento.setDescricao(dto.getDescricao());
        evento.setDataHora(dto.getDataHora());
        evento.setLocal(dto.getLocal());
        evento.setPreco(dto.getPreco());
        evento.setQuantidadeTotal(dto.getQuantidadeTotal());
        evento.setQuantidadeDisponivel(dto.getQuantidadeTotal());

        return eventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Evento não encontrado com o ID: " + id));
    }

    @Transactional
    public Evento atualizar(Long id, EventoRequestDTO dto) {
        Evento evento = buscarPorId(id);

        evento.setNome(dto.getNome());
        evento.setDescricao(dto.getDescricao());
        evento.setDataHora(dto.getDataHora());
        evento.setLocal(dto.getLocal());
        evento.setPreco(dto.getPreco());

        if (dto.getQuantidadeTotal() < evento.getQuantidadeTotal() - evento.getQuantidadeDisponivel()) {
            throw new RegraNegocioException(
                    "A quantidade total não pode ser menor que a quantidade de ingressos já vendidos."
            );
        }

        int ingressosVendidos =
                evento.getQuantidadeTotal() - evento.getQuantidadeDisponivel();

        evento.setQuantidadeTotal(dto.getQuantidadeTotal());
        evento.setQuantidadeDisponivel(dto.getQuantidadeTotal() - ingressosVendidos);

        return eventoRepository.save(evento);
    }

    @Transactional
    public void deletar(Long id) {
        Evento evento = buscarPorId(id);

        if (evento.getQuantidadeDisponivel() < evento.getQuantidadeTotal()) {
            throw new RegraNegocioException(
                    "Não é possível excluir um evento que já possui ingressos vendidos."
            );
        }

        eventoRepository.delete(evento);
    }
}