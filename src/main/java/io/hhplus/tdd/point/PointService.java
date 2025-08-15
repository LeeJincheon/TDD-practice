package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // 포인트 조회
    public UserPoint findPointByUserId(long id) {
        return userPointTable.selectById(id);
    }

    // 포인트 충전
    public UserPoint charge(long id, long amount) {
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        UserPoint currentUserPoint = userPointTable.selectById(id);
        long newPoint = currentUserPoint.point() + amount;

        return userPointTable.insertOrUpdate(id, newPoint);
    }

    // 포인트 사용
    public UserPoint use(long id, long amount) {
        UserPoint currentUserPoint = userPointTable.selectById(id);

        long newPoint = currentUserPoint.point() - amount;
        if (newPoint < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, newPoint);
    }
}
