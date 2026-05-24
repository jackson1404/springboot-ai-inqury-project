package com.jack.springaiopenrouter.repository;

import com.jack.springaiopenrouter.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    @Query("""
            select p from ProductEntity p
            where lower(p.code) like lower(concat('%', :query, '%'))
               or lower(p.name) like lower(concat('%', :query, '%'))
               or lower(p.category) like lower(concat('%', :query, '%'))
            order by p.code asc
            """)
    List<ProductEntity> search(@Param("query") String query);
}
