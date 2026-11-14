package pe.edu.usat.ecorutapac.ui.common;

public class UiState<T> {

    public enum Estado {
        LOADING,
        CONTENT,
        EMPTY,
        ERROR
    }

    private final Estado estado;
    private final T data;
    private final String mensaje;
    private final boolean desdeCache;

    private UiState(Estado estado, T data, String mensaje, boolean desdeCache) {
        this.estado = estado;
        this.data = data;
        this.mensaje = mensaje;
        this.desdeCache = desdeCache;
    }

    public static <T> UiState<T> loading() {
        return new UiState<>(Estado.LOADING, null, null, false);
    }

    public static <T> UiState<T> content(T data, boolean desdeCache) {
        return new UiState<>(Estado.CONTENT, data, null, desdeCache);
    }

    public static <T> UiState<T> empty() {
        return new UiState<>(Estado.EMPTY, null, null, false);
    }

    public static <T> UiState<T> error(String mensaje) {
        return new UiState<>(Estado.ERROR, null, mensaje, false);
    }

    public Estado getEstado() { return estado; }
    public T getData() { return data; }
    public String getMensaje() { return mensaje; }
    public boolean isDesdeCache() { return desdeCache; }
}
