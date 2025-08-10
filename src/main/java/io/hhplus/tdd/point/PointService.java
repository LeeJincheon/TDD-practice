package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    // 포인트 충전
    public UserPoint charge(long id, long amount) {
        UserPoint currentUserPoint = userPointTable.selectById(id);
        long newPoint = currentUserPoint.point() + amount;
        return userPointTable.insertOrUpdate(id, newPoint);
    }
}
