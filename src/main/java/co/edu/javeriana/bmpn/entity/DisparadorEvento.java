package co.edu.javeriana.bmpn.entity;

public enum DisparadorEvento {
    NINGUNO,
    // Message Throw: el evento envia un mensaje a otro pool
    MENSAJE_ENVIO,
    // Message Catch: el evento espera un mensaje de otro pool
    MENSAJE_RECEPCION
}
