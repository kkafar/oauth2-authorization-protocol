package pl.edu.agh.dp.tkgk.oauth2server;

public abstract class BaseHandler implements Handler{
    protected Handler next;

    /**
     *
     * @param handler next handler in the pipeline
     * @return handler given as parameter
     */
    @Override
    public Handler setNext(Handler handler) {
        next = handler;
        return handler;
    }


}
