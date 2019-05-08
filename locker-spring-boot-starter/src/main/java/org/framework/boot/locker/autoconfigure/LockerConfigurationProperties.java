package org.framework.boot.locker.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Locker configuration properties
 *
 * @author jiashuai.xie
 * @since 2019/4/28 13:38 1.0.0.RELEASE
 */
@ConfigurationProperties(prefix = "locker")
public class LockerConfigurationProperties {

    private Integer order;

    private String lockerKeyPrefix;

    private Long expireAfter;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getLockerKeyPrefix() {
        return lockerKeyPrefix;
    }

    public void setLockerKeyPrefix(String lockerKeyPrefix) {
        this.lockerKeyPrefix = lockerKeyPrefix;
    }

    public Long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(Long expireAfter) {
        this.expireAfter = expireAfter;
    }

    @Override
    public String toString() {
        return "LockerConfigurationProperties{" +
                "order=" + order +
                ", lockerKeyPrefix='" + lockerKeyPrefix + '\'' +
                ", expireAfter=" + expireAfter +
                '}';
    }
}
