package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(new PointHistory(1L, userId, amount, TransactionType.CHARGE, System.currentTimeMillis()));
        when(userPointTable.selectById(userId))
                .thenReturn(UserPoint.empty(userId));
        when(userPointTable.insertOrUpdate(userId, amount))
                .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(amount);
        verify(pointHistoryTable).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("table에 해당 id의 포인트 데이터가 있는 경우 기존 amount에 입력된 amount 더함")
    void charge_whenExistingPoint_shouldAddAmount() {
        // given
        long userId = 1L;
        long currentPoint = 500L;
        long amount = 300L;
        long expectedPoint = currentPoint + amount;

        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(new PointHistory(1L, userId, amount, TransactionType.CHARGE, System.currentTimeMillis()));
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, expectedPoint))
                .thenReturn(new UserPoint(userId, expectedPoint, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.charge(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(expectedPoint);
        verify(pointHistoryTable).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
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

    @Test
    @DisplayName("남은 포인트가 사용할 포인트보다 많은 경우 사용할 포인트만큼 차감")
    void use_whenPointIsEnough_shouldDeduct() {
        // given
        long userId = 1L;
        long currentPoint = 500L;
        long amount = 200L;
        long expectedPoint = currentPoint - amount;

        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.USE), anyLong()))
                .thenReturn(new PointHistory(1L, userId, amount, TransactionType.USE, System.currentTimeMillis()));
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, expectedPoint))
                .thenReturn(new UserPoint(userId, expectedPoint, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.use(userId, amount);

        // then
        assertThat(result.point()).isEqualTo(expectedPoint);
        verify(pointHistoryTable).insert(eq(userId), eq(amount), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("남은 포인트가 사용할 포인트보다 작은 경우 예외 발생")
    void use_whenPointIsLessThanAmount_shouldThrowException() {
        // given
        long userId = 1L;
        long currentPoint = 100L;
        long amount = 200L;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));

        // when & then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트가 부족합니다.");

        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("유저가 존재하지 않는 경우 예외 발생")
    void use_whenUserNotExist_shouldThrowException() {
        // given
        long userId = 1L;
        long amount = 100L;

        when(userPointTable.selectById(userId))
                .thenReturn(UserPoint.empty(userId));

        // when & then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트가 부족합니다.");

        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }
}