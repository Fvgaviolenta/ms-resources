package cl.catastrofescl.resources.service;

import cl.catastrofescl.resources.dto.request.ActualizarCategoriaRequest;
import cl.catastrofescl.resources.dto.request.CrearCategoriaRequest;
import cl.catastrofescl.resources.dto.response.CategoriaResponse;
import cl.catastrofescl.resources.entity.Categoria;
import cl.catastrofescl.resources.exception.CategoriaDuplicadaException;
import cl.catastrofescl.resources.exception.CategoriaNoEncontradaException;
import cl.catastrofescl.resources.repository.RepositorioCategorias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicioCategorias {

    private final RepositorioCategorias repositorioCategorias;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return repositorioCategorias.findByActivoTrueOrderByOrdenAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public CategoriaResponse crear(CrearCategoriaRequest solicitud) {
        if (repositorioCategorias.existsByCodigo(solicitud.codigo())) {
            throw new CategoriaDuplicadaException(solicitud.codigo());
        }
        Categoria categoria = Categoria.builder()
                .codigo(solicitud.codigo().toUpperCase())
                .nombre(solicitud.nombre())
                .orden(solicitud.orden())
                .activo(true)
                .build();
        return aResponse(repositorioCategorias.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(UUID id, ActualizarCategoriaRequest solicitud) {
        Categoria categoria = repositorioCategorias.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id.toString()));
        if (solicitud.nombre() != null) {
            categoria.setNombre(solicitud.nombre());
        }
        if (solicitud.activo() != null) {
            categoria.setActivo(solicitud.activo());
        }
        if (solicitud.orden() != null) {
            categoria.setOrden(solicitud.orden());
        }
        return aResponse(repositorioCategorias.save(categoria));
    }

    private CategoriaResponse aResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getCodigo(),
                categoria.getNombre(),
                categoria.isActivo(),
                categoria.getOrden(),
                categoria.getCreadoEn()
        );
    }
}
