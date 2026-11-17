package com.quilate.util;

import com.quilate.exception.ReglaNegocioException;
import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ejecuta llamadas a la base de datos fuera del hilo de JavaFX para que la
 * ventana no se congele. Los callbacks se ejecutan de vuelta en el hilo de JavaFX.
 */
public final class Tareas {

    private Tareas() {
    }

    public static <T> void ejecutar(Supplier<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() {
                return trabajo.get();
            }
        };
        tarea.setOnSucceeded(e -> alTerminar.accept(tarea.getValue()));
        tarea.setOnFailed(e -> alFallar.accept(tarea.getException()));

        Thread hilo = new Thread(tarea, "quilate-bd");
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Variante para acciones del usuario: los errores se muestran en un Alert. */
    public static <T> void ejecutar(Supplier<T> trabajo, Consumer<T> alTerminar) {
        ejecutar(trabajo, alTerminar, Tareas::mostrarError);
    }

    /** Reglas de negocio como advertencia; cualquier otro error como error. */
    public static void mostrarError(Throwable e) {
        if (e instanceof ReglaNegocioException) {
            AlertUtil.advertencia(e.getMessage());
        } else {
            AlertUtil.error(e);
        }
    }

    /** Mensaje corto para mostrar en una etiqueta cuando falla la carga inicial. */
    public static String mensajeCorto(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName();
    }
}
