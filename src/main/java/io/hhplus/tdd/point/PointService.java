package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final Map<Long, Lock> locks = new ConcurrentHashMap<>();

    // 포인트 조회
    public UserPoint findPointByUserId(long id) {
        return userPointTable.selectById(id);
    }

    // 포인트 내역 조회
    public List<PointHistory> findAllHistoriesByUserId(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    // 포인트 충전
    public UserPoint charge(long id, long amount) {
        Lock userLock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        userLock.lock();
        try {
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            UserPoint currentUserPoint = userPointTable.selectById(id);
            long newPoint = currentUserPoint.point() + amount;

            return userPointTable.insertOrUpdate(id, newPoint);
        } finally {
            userLock.unlock();
        }
    }

    // 포인트 사용
    public UserPoint use(long id, long amount) {
        Lock userLock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        userLock.lock();
        try {
            UserPoint currentUserPoint = userPointTable.selectById(id);

            long newPoint = currentUserPoint.point() - amount;
            if (newPoint < 0) {
                throw new IllegalArgumentException("포인트가 부족합니다.");
            }

            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(id, newPoint);
        } finally {
            userLock.unlock();
        }
    }
}
