package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.PasswordResetAuditLog;
import com.solydshop.ecommerce.repository.PasswordResetAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private PasswordResetAuditLogRepository auditLogRepository;

    public void log(String email, String ipAddress, String event) {
        auditLogRepository.save(new PasswordResetAuditLog(email, ipAddress, event));
    }
}
