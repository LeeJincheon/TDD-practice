package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("table에 해당 id의 포인트 데이터가 없는 경우 입력된 amount로 설정")
    void charge_whenNoExistingPoint_shouldSetPointToAmount() {
        // given
        long userId = 1L;
        long amount = 500L;

        when(userPointTable.selectById(userId))
                .thenReturn(UserPoint.empty(userId));
        when(userPointTable.insertOrUpdate(userId, amount))
                .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(amount);
    }

    @Test
    @DisplayName("table에 해당 id의 포인트 데이터가 있는 경우 기존 amount에 입력된 amount 더함")
    void charge_whenExistingPoint_shouldAddAmount() {
        // given
        long userId = 1L;
        long currentPoint = 500L;
        long amount = 300L;
        long expectedPoint = currentPoint + amount;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, expectedPoint))
                .thenReturn(new UserPoint(userId, expectedPoint, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(expectedPoint);
    }
}