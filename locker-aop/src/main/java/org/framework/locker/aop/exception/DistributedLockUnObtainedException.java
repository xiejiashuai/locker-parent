package org.framework.locker.aop.exception;

/**
 *
 *
 * @author jiashuai.xie
 */
public class DistributedLockUnObtainedException extends RuntimeException {

    public DistributedLockUnObtainedException() {
        super();
    }


    public DistributedLockUnObtainedException(String message) {
        super(message);
    }


    public DistributedLockUnObtainedException(String message, Throwable cause) {
        super(message, cause);
    }


    public DistributedLockUnObtainedException(Throwable cause) {
        super(cause);
    }


}
