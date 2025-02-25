package com.prueba.cuenta.repository;

import com.prueba.cuenta.entity.Movement;
import com.prueba.cuenta.entity.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, String> {

    List<Movement> findByCuenta_NumeroCuenta(Integer numeroCuenta);

    List<Movement> findByCuenta_NumeroCuentaAndFechaBetween(Integer cuenta, LocalDate fechaInicio, LocalDate fechaFin);

}
