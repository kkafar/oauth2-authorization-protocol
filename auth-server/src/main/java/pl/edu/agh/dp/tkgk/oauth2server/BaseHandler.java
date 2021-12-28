package pl.edu.agh.dp.tkgk.oauth2server;

public abstract class BaseHandler<T, K> implements Handler<T, K> {
    protected Handler<K, ?> next;

    /**
     * @param handler next handler in the pipeline
     * @return handler given as parameter
     */
    @Override
    public Handler<K, ?> setNext(Handler<K, ?> handler) {
        next = handler;
        return handler;
    }
}
