package com.tgithubc.messagebus;

/**
 * Created by tc :)
 */
public interface IObserver {

    interface ITest1Observer extends IObserver {

        void test11(String string);

        void test11(int string);

        void test12(String string);
    }

    interface ITest2Observer extends IObserver {

        void test21(String string);

        void test21(int string);
    }
}
