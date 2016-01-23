package com.zhai.example.rx;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextView = (TextView) findViewById(R.id.text);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void observer(View v) {
        //创建一个观察者
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onCompleted() {
                mTextView.setText(mTextView.getText() + "\nCompleted!!!");
            }

            @Override
            public void onError(Throwable e) {
                mTextView.setText(mTextView.getText() + "\nError:" + e.getMessage() + "!!!");
            }

            @Override
            public void onNext(String s) {
                mTextView.setText(mTextView.getText() + "\nNext:" + s);
            }
        };
        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onNext("Aloha");
                subscriber.onCompleted();
            }
        });
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void subscriber(View v) {
        //创建一个观察者 Observer是一个借口 RxJava内置一个Observer的抽象类，进行了一些扩展
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onStart() {
                super.onStart();
                // 事件未发送前调用，可以做一些装备工作
                mTextView.setText(mTextView.getText() + "\n我在做准备!!!");
            }

            @Override
            public void onCompleted() {
                mTextView.setText(mTextView.getText() + "\nCompleted!!!");
            }

            @Override
            public void onError(Throwable e) {
                mTextView.setText(mTextView.getText() + "\nError:" + e.getMessage() + "!!!");
            }

            @Override
            public void onNext(String s) {
                mTextView.setText(mTextView.getText() + "\nNext:" + s);
            }

        };
        // subscriber.isUnsubscribed();//是否订阅
        // subscriber.unsubscribe();//取消订阅
        // 快捷方式创建
        Observable observable = Observable.just("111", "222", "3333");
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
        /*
          public Subscription subscribe(Subscriber subscriber){
            subscriber.onStart();   // 开始传递事件
            onSubscribe.call(subscriber);   //
            return subscriber; // 返回subscriber 方便取消订阅
          }
         */

        // 使用数据创建
        // String[] events= {"one", "two", "three"};
        // Observable observable = Observable.from(events);
    }

    public void subscribe(View v) {
        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(mTextView.getText() + "\nNext:" + s);
            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                mTextView.setText(mTextView.getText() + "\nError:" + e.getMessage() + "!!!");
            }
        };

        Action0 onCompletedAction = new Action0() {
            @Override
            public void call() {
                mTextView.setText(mTextView.getText() + "\nCompleted!!!");
            }
        };
        Observable.just("999", "888", "777").observeOn(AndroidSchedulers.mainThread()).subscribe(onNextAction, onErrorAction, onCompletedAction);
    }

    public void clear(View v) {
        mTextView.setText("");
    }

    // Scheduler 线程控制
    /*
        Schedulers.immediate(); // 当前线程，默认
        Schedulers.newThread(); // 启动新线程
        Schedulers.io();    //I/O操作 内部实现了一个数量无上限的线程池，可以重用空闲的线程
        Schedulers.computation(); // 计算所使用的Scheduler CPU密集型计算
        AndroidSchedulers.mainThread(); // Android主线程
    */
    // subscribeOn() 指定被观察者线程
    // observeOn() 指定观察者线程

    public void change1(View v) {
        Observable.just(1, 2, 3, 4, 5, 6)
                .map(new Func1<Integer, String>() { // 转换
                    @Override
                    public String call(Integer integer) {
                        return integer.toString();
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(mTextView.getText() + "\nNext:" + s);
            }
        });
    }

    public void change2(View v) {
        Observable.just("China", "Japen", "England")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return Observable.from(new String[]{s, s.length() + ""});
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(mTextView.getText() + "\nNext:" + s);
            }
        });
    }

    public void throttle(View v) {
        Observable<String> observale = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext("1111");
                    Thread.sleep(300);
                    subscriber.onNext("2222");
                    Thread.sleep(300);
                    subscriber.onNext("3333");
                    Thread.sleep(300);
                    subscriber.onNext("4444");
                    subscriber.onCompleted();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        // 在接收到一个事件的300ms内不接收下一个事件
        observale.throttleFirst(310, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(mTextView.getText() + "\nNext:" + s);
            }
        });
    }

    public void lift(View v) {
        // 用一个Obervale接收一个Oberbale所有事件，并自己处理
        Observable.just(1, 2, 3, 4).lift(new Observable.Operator<String, Integer>() {
            @Override
            public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {
                return new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer s) {
                        // 做出了拦截，并发送给了原有的Subscribe，可以看做代理
                        subscriber.onNext(s.toString());
                        mTextView.setText(mTextView.getText() + "\nNext lift:" + s);
                    }
                };
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(mTextView.getText() + "\nNext subscribe:" + s);
            }
        });
    }

    public void compose(View v) {
        final Observable.Operator<String, Integer> operator1 = new Observable.Operator<String, Integer>() {
            @Override
            public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {
                return new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        subscriber.onNext(String.valueOf(integer));
                        mTextView.setText(String.format("%s\nOperator1:%d", mTextView.getText(), integer));
                    }
                };
            }
        };
        final Observable.Operator<Integer, String> operator2 = new Observable.Operator<Integer, String>() {
            @Override
            public Subscriber<? super String> call(final Subscriber<? super Integer> subscriber) {
                return new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        subscriber.onNext(Integer.valueOf(s));
                        mTextView.setText(String.format("%s\nOperator2:%s", mTextView.getText(), s));
                    }
                };
            }
        };
        // 耍一套杂技，int->string->int->string但没做变化
        Observable.just(1, 2, 3, 4).compose(new Observable.Transformer<Integer, String>() {
            @Override
            public Observable<String> call(Observable<Integer> integerObservable) {
                return integerObservable.lift(operator1).lift(operator2).lift(operator1);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(String.format("%s\nOnNext:%s", mTextView.getText(), s));
            }
        });
    }


}
