package com.jack.springaiopenrouter.repository;

import com.jack.springaiopenrouter.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Query("""
            select o from OrderEntity o
            where lower(o.id) like lower(concat('%', :query, '%'))
               or lower(o.customerId) like lower(concat('%', :query, '%'))
               or lower(o.productCode) like lower(concat('%', :query, '%'))
               or lower(o.status) like lower(concat('%', :query, '%'))
            order by o.orderDate desc
            """)
    List<OrderEntity> search(@Param("query") String query);

    List<OrderEntity> findByCustomerIdIgnoreCaseOrderByOrderDateDesc(String customerId);

    @Query("select coalesce(sum(o.totalAmount), 0) from OrderEntity o where lower(o.customerId) = lower(:customerId)")
    BigDecimal totalSpendByCustomerId(@Param("customerId") String customerId);
}
