package org.framework.locker.aop.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotation indicating that a method (or all methods)  need add lock
 *
 * @author jiashuai.xie
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DistributedLocker {

    /**
     * namespace of the lock to use
     */
    String lockerNamespace() default "";

    /**
     * Spring Expression Language (SpEL) expression for computing the key dynamically.
     */
    String key();

    /**
     * when failed to get lock and timeout > 0 ,will retry get lock
     * use default value means that when get lock failed,will fail fast
     */
    int timeout() default 1000;

    /**
     * {@link DistributedLocker#timeout() unit
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
