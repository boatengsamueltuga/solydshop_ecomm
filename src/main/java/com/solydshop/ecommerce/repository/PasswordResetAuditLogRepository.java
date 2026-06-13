package com.solydshop.ecommerce.repository;

import com.solydshop.ecommerce.entity.PasswordResetAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetAuditLogRepository extends JpaRepository<PasswordResetAuditLog, Long> {
}
