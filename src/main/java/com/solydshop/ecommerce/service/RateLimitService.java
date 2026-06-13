package com.solydshop.ecommerce.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, List<LocalDateTime>> attempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final int WINDOW_HOURS = 1;

    public boolean isRateLimited(String email) {
        List<LocalDateTime> times = attempts.getOrDefault(email, new ArrayList<>());
        LocalDateTime cutoff = LocalDateTime.now().minusHours(WINDOW_HOURS);
        times.removeIf(t -> t.isBefore(cutoff));
        attempts.put(email, times);
        return times.size() >= MAX_ATTEMPTS;
    }

    public void recordAttempt(String email) {
        attempts.computeIfAbsent(email, k -> new ArrayList<>()).add(LocalDateTime.now());
    }

    public long getMinutesUntilReset(String email) {
        List<LocalDateTime> times = attempts.getOrDefault(email, new ArrayList<>());
        if (times.isEmpty()) return 0;
        LocalDateTime oldest = times.stream().min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        long minutes = Duration.between(LocalDateTime.now(), oldest.plusHours(WINDOW_HOURS)).toMinutes() + 1;
        return Math.max(1, minutes);
    }
}
