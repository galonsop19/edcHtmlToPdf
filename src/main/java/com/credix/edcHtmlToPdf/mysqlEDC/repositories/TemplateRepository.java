package com.credix.edcHtmlToPdf.mysqlEDC.repositories;

import com.credix.edcHtmlToPdf.mysqlEDC.entities.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    @Query("SELECT c.text FROM Template c WHERE  (c.category = :category  AND c.cutDate = :cutDate AND c.section= :section AND c.status= 1)")
    String findTemplateByFilter(@Param("cutDate") String cutDate, @Param("category") Integer category,@Param("section") Integer section);
}
