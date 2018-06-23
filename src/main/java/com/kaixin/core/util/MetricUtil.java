package com.kaixin.core.util;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhongshu on 2017/1/7.
 */
public class MetricUtil {
    private static MetricRegistry metrics = new MetricRegistry();
    private static boolean METRIC_USING_NANOTIME_ENABLE = PropsUtil.getBoolean(PropsKeys.METRIC_USING_NANOTIME_ENABLE);

    public static MetricRegistry getMetric() {
        return metrics;
    }


    /*
     * 获取当前时间，精度为微妙
     * 如果使用nanoTime，才能真的精确到微秒，否则精度通常为15ms（windows2008/Java7）
     * 这个函数本身的耗时：
     *  如果是nanoTime，耗时约 0.3微秒
     *  如果非nanoTime，耗时约0.03微秒
     * */
    public static long getCurrentTime() {

        if (METRIC_USING_NANOTIME_ENABLE)
            return System.nanoTime() / 1000L;
        else
            return System.currentTimeMillis() * 1000L;
    }

    /*
     * 更新timer，注意这个函数本身有耗时，大概为70微秒
     * 这个70微秒不会计入timer内，已经被移除，但这个70微秒会影响系统整体性能。
     * */
    public static void updateTimer(boolean enable, String name, long startTime) {
        if (enable) {

            //先取结束时间，避免下面的register消耗的时间被算进去
            long endTime = getCurrentTime();

            //缺省的Snapshot: ExponentiallyDecayingReservoir是按时间带权重的，最近的数据权重大，看起来不易理解，修改为最简单的SlidingWindowReservoir
            Timer timer = null;
            try {
                timer = metrics.register(name, new Timer(new SlidingWindowReservoir(PropsUtil.getInteger(PropsKeys.METRIC_SNAPSHOT_SIZE))));
            } catch (IllegalArgumentException e) {
                timer = metrics.timer(name);
            }

            //这里本来单位应该传微秒，但metric内部原来是转换为纳秒显示的，不方便，这里传纳秒，最终取回的就是微秒了
            timer.update(endTime - startTime, TimeUnit.NANOSECONDS);
        }
    }

}
