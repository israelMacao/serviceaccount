package com.prueba.cuenta.repository;

import com.prueba.cuenta.entity.Account;
import com.prueba.cuenta.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByNumeroCuenta(Integer numeroCuenta);

    List<Account> findByTipoCuenta(AccountType tipoCuenta);

    List<Account> findByStatus(boolean status);
}
