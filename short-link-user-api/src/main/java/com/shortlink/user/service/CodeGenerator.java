package com.shortlink.user.service;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.util.Base62;
import com.shortlink.user.entity.Segment;
import com.shortlink.user.mapper.SegmentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 短码发号器：从 segment 表按段预取，内存内递增后 Base62 编码。
 *
 * <p>为什么不用「哈希 + 冲突重试」：发号器天然不重复，不需要每次生成都去库里查重，
 * 也不存在冲突重试带来的延迟抖动；代价只是一次库写换 1000 个号。</p>
 *
 * <p>取号必须与推进 max_id 在同一个事务里，靠 {@code SELECT ... FOR UPDATE}
 * 保证多实例不会拿到重叠号段。</p>
 */
@Component
public class CodeGenerator {

    private static final String BIZ_TAG = "short_link";

    /** 62^7，定长 7 位的编码空间。 */
    private static final long CODE_SPACE = 3_521_614_606_208L;

    /**
     * 乘法混淆因子：与 62^7 互质，保证「自增 id -> 打散值」是一一映射，
     * 不会引入冲突，但短码看起来不再像连续序号。
     */
    private static final long MULTIPLIER = 8_314_927L;

    private final SegmentMapper segmentMapper;

    private final TransactionTemplate transactionTemplate;

    private final int codeLength;

    private long current;

    private long max;

    public CodeGenerator(SegmentMapper segmentMapper, TransactionTemplate transactionTemplate,
                         @Value("${shortlink.link.code-length:7}") int codeLength) {
        this.segmentMapper = segmentMapper;
        this.transactionTemplate = transactionTemplate;
        this.codeLength = codeLength;
    }

    public synchronized String nextCode() {
        if (current >= max) {
            loadSegment();
        }
        current++;
        return Base62.encode((current * MULTIPLIER) % CODE_SPACE, codeLength);
    }

    private void loadSegment() {
        transactionTemplate.execute(status -> {
            Segment segment = segmentMapper.selectForUpdate(BIZ_TAG);
            if (segment == null || segment.getStep() == null) {
                throw new BizException(ResultCode.SYSTEM_ERROR, "号段未初始化，请先执行 sql/schema.sql");
            }
            int step = segment.getStep();
            segmentMapper.increase(BIZ_TAG, step);
            current = segment.getMaxId();
            max = segment.getMaxId() + step;
            return null;
        });
    }
}
