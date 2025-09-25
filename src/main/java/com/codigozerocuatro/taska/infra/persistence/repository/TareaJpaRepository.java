package com.codigozerocuatro.taska.infra.persistence.repository;

import com.codigozerocuatro.taska.infra.persistence.model.TareaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TareaJpaRepository extends JpaRepository<TareaEntity, Long>, JpaSpecificationExecutor<TareaEntity> {

    /**
     * Encuentra todas las tareas hijas de una tarea padre
     */
    List<TareaEntity> findByIdTareaPadre(Long idTareaPadre);

    /**
     * Encuentra todas las tareas de una serie recurrente (padre e hijas)
     */
    @Query("SELECT t FROM TareaEntity t WHERE t.id = :idTareaPadre OR t.idTareaPadre = :idTareaPadre ORDER BY t.fecha")
    List<TareaEntity> findSerieRecurrente(@Param("idTareaPadre") Long idTareaPadre);

    /**
     * Encuentra tareas posteriores a una fecha específica en una serie recurrente
     * solo las tareas hijas (excluye la tarea padre)
     */
    @Query("SELECT t FROM TareaEntity t WHERE t.idTareaPadre = :idTareaPadre AND t.fecha >= :fecha ORDER BY t.fecha")
    List<TareaEntity> findTareasPosteriores(@Param("idTareaPadre") Long idTareaPadre, @Param("fecha") LocalDate fecha);

    /**
     * Encuentra todas las tareas en un rango de fechas ordenadas por fecha
     */
    @Query("SELECT t FROM TareaEntity t WHERE t.fecha >= :fechaInicio AND t.fecha <= :fechaFin ORDER BY t.fecha ASC")
    List<TareaEntity> findByFechaBetweenOrderByFechaAsc(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

}
