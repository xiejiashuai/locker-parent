package org.framework.locker.aop.aop;

import org.framework.locker.aop.DistributedLockerPostProcessor;
import org.framework.locker.aop.annotation.DistributedLocker;
import org.framework.locker.aop.exception.DistributedLockUnObtainedException;
import org.framework.locker.aop.spel.LockEvaluationContext;
import org.framework.locker.aop.spel.LockOperationExpressionEvaluator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Locker aspect , add locker before invoke target method
 *
 * @author jiashuai.xie
 */
@Aspect
public class DistributedLockerAspect implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockerAspect.class);

    private static final Integer DEFAULT_LOCKER_ASPECT_ORDER = Ordered.LOWEST_PRECEDENCE;

    private final LockOperationExpressionEvaluator lockOperationExpressionEvaluator = new LockOperationExpressionEvaluator();

    public DistributedLockerAspect(RedisLockRegistry redisLockRegistry, List<DistributedLockerPostProcessor> distributedLockerPostProcessors) {
        this.redisLockRegistry = redisLockRegistry;
        this.distributedLockerPostProcessors = distributedLockerPostProcessors;
    }

    @Pointcut("@annotation(org.framework.locker.aop.annotation.DistributedLocker)")
    public void pointCut() {
    }

    /**
     * 切面顺序order
     */
    private Integer order = DEFAULT_LOCKER_ASPECT_ORDER;

    private final RedisLockRegistry redisLockRegistry;

    private final List<DistributedLockerPostProcessor> distributedLockerPostProcessors;

    @Around("pointCut()")
    public Object distributedLock(ProceedingJoinPoint pjp) throws Throwable {

        Object result;

        String distributedLockKey;

        Object[] args = pjp.getArgs();

        Method method = resolveMethod(pjp);

        Lock locker = null;

        try {

            DistributedLocker lockerAnnotation = AnnotationUtils.findAnnotation(method, DistributedLocker.class);

            // lock key
            distributedLockKey = exactLockerKey(pjp, args, method, lockerAnnotation);

            locker = redisLockRegistry.obtain(distributedLockKey);

            boolean lockable = locker.tryLock(lockerAnnotation.timeout(), lockerAnnotation.timeUnit());

            // failed to get lock
            if (!lockable) {
                LOGGER.error("failed to get lock,lockKey:{}", distributedLockKey);
                throw new DistributedLockUnObtainedException("get lock failed");
            }
            result = pjp.proceed(args);

        } catch (InterruptedException e) {
            // 清除中断标识
            Thread.interrupted();
            throw e;
        } finally {
            if (null != locker) {
                locker.unlock();
            }
        }

        // need to post processor
        if (!CollectionUtils.isEmpty(distributedLockerPostProcessors)) {

            for (DistributedLockerPostProcessor distributedLockerPostProcessor : distributedLockerPostProcessors) {
                distributedLockerPostProcessor.postProcessAfterInvoking(method, args, result, pjp);
            }

        }

        return result;

    }

    /**
     * 提取locker key
     *
     * @param pjp              切入点
     * @param args             目标方法参数
     * @param method           目标方法
     * @param lockerAnnotation 目标方法上的注解
     * @return locker key
     */
    private String exactLockerKey(ProceedingJoinPoint pjp, Object[] args, Method method, DistributedLocker lockerAnnotation) {

        String keyExpression = lockerAnnotation.key();

        LockEvaluationContext context = lockOperationExpressionEvaluator.createEvaluationContext(method, args, pjp.getTarget().getClass());

        String keyValue = lockOperationExpressionEvaluator.key(keyExpression, context.getOperationMetadata().getMethodKey(), context, String.class);

        return StringUtils.hasText(lockerAnnotation.lockerNamespace()) ? lockerAnnotation.lockerNamespace() + ":" + keyValue : keyValue;
    }

    /**
     * 获取要执行的方法
     *
     * @param joinPoint
     * @return
     */
    private Method resolveMethod(ProceedingJoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> targetClass = joinPoint.getTarget().getClass();

        Method method = getDeclaredMethodFor(targetClass, signature.getName(), signature.getMethod().getParameterTypes());
        if (method == null) {
            throw new IllegalStateException("Cannot resolve target method: " + signature.getMethod().getName());
        }
        return method;
    }

    private Method getDeclaredMethodFor(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethodFor(superClass, name, parameterTypes);
            }
        }
        return null;
    }


    public void setOrder(Integer order) {
        if (null != order) {
            this.order = order;
        }
    }

    @Override
    public int getOrder() {
        return order;
    }
}
