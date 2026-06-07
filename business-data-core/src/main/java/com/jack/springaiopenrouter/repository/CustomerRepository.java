package com.jack.springaiopenrouter.repository;

import com.jack.springaiopenrouter.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    @Query("""
            select c from CustomerEntity c
            where lower(c.id) like lower(concat('%', :query, '%'))
               or lower(c.name) like lower(concat('%', :query, '%'))
               or lower(c.email) like lower(concat('%', :query, '%'))
               or lower(c.tier) like lower(concat('%', :query, '%'))
               or lower(c.region) like lower(concat('%', :query, '%'))
            order by c.id asc
            """)
    List<CustomerEntity> search(@Param("query") String query);
}
