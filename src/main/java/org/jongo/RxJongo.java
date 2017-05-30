package org.jongo;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import org.jongo.function.Function;

public class RxJongo {

    public static <E> Function<MongoCursor<E>, Observable<E>> toObservable() {
        return new Function<MongoCursor<E>, Observable<E>>() {
            public Observable<E> apply(MongoCursor<E> mongoCursor) {
                return fromCursor(mongoCursor);
            }
        };
    }

    public static <E> Function<MongoCursor<E>, Flowable<E>> toFlowable() {
        return new Function<MongoCursor<E>, Flowable<E>>() {
            public Flowable<E> apply(MongoCursor<E> mongoCursor) {
                return fromCursor(mongoCursor).toFlowable(BackpressureStrategy.BUFFER);
            }
        };
    }

    private static <E> Observable<E> fromCursor(final MongoCursor<E> mongoCursor) {
        return Observable.create(
                new ObservableOnSubscribe<E>() {
                    public void subscribe(ObservableEmitter<E> e) throws Exception {
                        while (mongoCursor.hasNext() && !e.isDisposed()) {
                            e.onNext(mongoCursor.next());
                        }
                        e.onComplete();
                    }
                })
                .doOnComplete(new Action() {
                    public void run() throws Exception {
                        mongoCursor.close();
                    }
                });

    }

}
