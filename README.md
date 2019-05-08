# 注解分布式锁Quick Start

> 说明:
>
> 本工程只是集成分布式锁实现，支持注解实现，减少代码量。
>
>
> 水平有限，使用过程若有不爽，欢迎提出意见，将最大满足您的心愿。



1. 引入依赖

   ```xml
     		<dependency>
                <groupId>org.framework.locker</groupId>
                <artifactId>locker-spring-boot-starter</artifactId>
                <version>1.0.0.SNAPSHOT</version>
           </dependency>
   ```

2. 配置类请参考`LockerConfigurationProperties`
 

3.  在代码中使用，实例如下

   ```java
   @DistributedLock(namespace = "pickup-payment-bill", key = "#model.dispatchBillNo")
       @Transactional(rollbackFor = Exception.class)
       @Override
       public List<PaymentBill> createPaymentBills(PaymentBillsCreateModel model) {
   
         ...省略代码
   
       }
   
   ```

   - `@DistributedLock`解释说明
     - `namespace`
       - 命名空间，用于拼接key，建议取值为和操作的域对象相关，是个定值
     - `key`
       - 拼接Redis动态key，支持`SPEL`表达式
     - `timeout`
       - 获取锁，重试时间
      

4. 其他说明

   1. 获取分布式锁失败，抛出`DistributedLockUnGetException`异常。

   2. 在释放锁后，切面允许做一些通用操作，如有需要请声明为`DistributedLockUnObtainedException`类型的Bean,支持多个

      > 多个执行顺序参考Spring `Ordered`接口或者`@Order`注解

   3. `@DistributedLock`基于`Aspect`实现，使用约束请遵循`Aspect`的约束