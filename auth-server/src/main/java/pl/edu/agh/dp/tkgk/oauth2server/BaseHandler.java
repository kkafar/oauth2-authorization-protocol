package pl.edu.agh.dp.tkgk.oauth2server;

import java.util.LinkedList;
import java.util.List;

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
    @Override
    public void setNext(Handler<K, ?> handler) {
        next = handler;
    }

    @Override
    public List<Handler<?,?>> getChain(){
        List<Handler<?,?>> chain;
        if(next != null) {
            chain = next.getChain();
        }else{
            chain = new LinkedList<>();
        }
        chain.add(0,this);
        return chain;
    }


}
