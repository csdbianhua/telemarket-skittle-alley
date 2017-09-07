package telemarketer.skittlealley.persist;

import java.time.Clock;
import java.time.Instant;

/**
 * Chen Yijie on 2016/11/25 13:00.
 */
class Data {
    private Object content;
    private Long expireAt;
    private long createAt;

    public Data(Object content, Long expire) {
        this.content = content;
        this.createAt = Instant.now().toEpochMilli();
        this.expireAt = expire == null ? null : this.createAt + expire;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public boolean isNotExpire() {
        return expireAt == null || Clock.systemUTC().millis() <= expireAt;
    }
}
