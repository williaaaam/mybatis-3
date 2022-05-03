package org.apache.ibatis.example;

import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * @author william
 * @title
 * @desc
 * @date 2022/4/30
 **/
public class ReferenceTest {

    @Test
    public void testPhantomReference() {
        Object obj = new Object();
        ReferenceQueue referenceQueue = new ReferenceQueue();
        PhantomReference<Object> phantomReference = new PhantomReference<>(obj, referenceQueue);
        obj = null;
        System.gc();
        // always return null
        System.out.println(referenceQueue.poll());
    }

    @Test
    public void testWeakReference() throws InterruptedException {
        Object obj = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
        PhantomReference<Object> weakReference = new PhantomReference<>(obj, referenceQueue);
        // 一直返回null，软和弱引用引用obj时则会输出Object实例
        System.out.println(weakReference.get());
        obj = null;
        System.gc();
        TimeUnit.SECONDS.sleep(3);
        // null
        System.out.println(weakReference.get());
        System.out.println(referenceQueue.poll());
    }

    @Test
    public void testFinalize() throws InterruptedException {
        Foo foo = new Foo();
//        ReferenceQueue<Foo> referenceQueue = new ReferenceQueue<>();
//        WeakReference<Foo> softReference = new WeakReference<>(foo, referenceQueue);
        foo = null;
        // active -> pending -> Finalizer.queue -> 执行finalize()方法
        System.gc();
        TimeUnit.SECONDS.sleep(3);
//        System.out.println(softReference.get());
//        System.out.println(referenceQueue.poll());
    }

    public class Foo {

        @Override
        public String toString() {
            return "Wow ! ";
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("执行Finalize");
        }
    }
}
