package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Test
    @DisplayName("유저가 존재하는 경우 해당 유저의 포인트 조회")
    void findPointByUserId_whenUserExists_shouldReturnUserPoint() {
        // given
        long userId = 1L;
        UserPoint expected = new UserPoint(userId, 1000L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(expected);

        // when
        UserPoint result = pointService.findPointByUserId(userId);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("유저가 존재하지 않는 경우 empty 포인트를 반환")
    void findPointByUserId_whenUserNotExist_shouldReturnEmptyUserPoint() {
        // given
        long userId = 2L;
        UserPoint emptyPoint = UserPoint.empty(userId);
        when(userPointTable.selectById(userId)).thenReturn(emptyPoint);

        // when
        UserPoint result = pointService.findPointByUserId(userId);

        // then
        assertThat(result).isEqualTo(emptyPoint);
    }
}