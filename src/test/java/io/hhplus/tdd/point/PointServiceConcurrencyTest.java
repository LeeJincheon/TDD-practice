package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PointServiceConcurrencyTest {

    private PointService pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    private static final long USER_ID = 1L;
    private static final long INITIAL_POINT = 0L;
    private static final int THREAD_COUNT = 100;
    private static final long AMOUNT_PER_CHARGE = 100L;
    private static final long EXPECTED_TOTAL = THREAD_COUNT * AMOUNT_PER_CHARGE;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);

        userPointTable.insertOrUpdate(USER_ID, INITIAL_POINT);
    }

    @Test
    @DisplayName("동시에 100회 충전 시 최종 포인트는 10000이다.")
    void charge_concurrently_shouldBeCorrect() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    pointService.charge(USER_ID, AMOUNT_PER_CHARGE);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        UserPoint resultPoint = pointService.findPointByUserId(USER_ID);
        assertThat(resultPoint.point()).isEqualTo(EXPECTED_TOTAL);
        System.out.println("최종 포인트: " + resultPoint.point());
    }

    @Test
    @DisplayName("동시에 100회 사용 시 최종 포인트는 0이다.")
    void use_concurrently_shouldBeCorrect() throws InterruptedException {
        // given
        long initialPoint = THREAD_COUNT * AMOUNT_PER_CHARGE;
        userPointTable.insertOrUpdate(USER_ID, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    pointService.use(USER_ID, AMOUNT_PER_CHARGE);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        UserPoint resultPoint = pointService.findPointByUserId(USER_ID);
        assertThat(resultPoint.point()).isEqualTo(0L);
        System.out.println("최종 포인트: " + resultPoint.point());
    }
}
