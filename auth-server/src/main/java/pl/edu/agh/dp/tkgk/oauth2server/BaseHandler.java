package pl.edu.agh.dp.tkgk.oauth2server;

public abstract class BaseHandler<T, K> implements Handler<T, K> {
    protected Handler<K, ?> next;

    /**
     * @param handler next handler in the pipeline
     * @return given handler
     */

    @Override
    public <S> Handler<K, S> setNextAndGet(Handler<K, S> handler) {
        setNext(handler);
        return handler;
    }

    /**
     * @param handler next handler in the pipeline
     */

    public void setNext(Handler<K, ?> handler) {
        next = handler;
    }

}
