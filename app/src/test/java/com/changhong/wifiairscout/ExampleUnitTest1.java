package com.changhong.wifiairscout;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by fuheng on 2018/1/25.
 */

public class ExampleUnitTest1 {
    @Test
    public void addition_isCorrect() {
        new B();
    }

    class B extends A<String, Integer, Long> {
        public B() {
//            Type genType = this.getClass().getGenericSuperclass();
//            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
//            for (Type type : params)
//                System.out.println(type.getClass().getName());
        }
    }

    class A<T1, T2, T3> {
        public A() {
            Type genType = this.getClass().getGenericSuperclass();
//            System.out.println(genType.getClass().geta);
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
//            for (Type type : params)
                System.out.println(((Class<T1>)params[0]).getName());
                System.out.println(((Class<T2>)params[1]).getName());
                System.out.println(((Class<T3>)params[2]).getName());
        }
    }
}
