package com.example.iq300.repository;

import com.example.iq300.domain.TotalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface TotalDataRepository extends JpaRepository<TotalData, Long> {
	
	@Query("SELECT t FROM TotalData t WHERE " + "(:address IS NULL OR t.address LIKE %:address%) AND "
			+ "(:txType IS NULL OR t.transactionType LIKE %:txType%) " + "ORDER BY t.contractDate DESC")
	
	List<TotalData> findByDynamicQuery (
			@Param("address") String addressKeyword,
			@Param("txType") String transactionKeyword,
			Pageable pageable);
}
