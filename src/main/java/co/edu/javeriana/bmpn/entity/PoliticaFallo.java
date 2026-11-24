package co.edu.javeriana.bmpn.entity;

// HU-26: que debe pasar en el modelo si falla una notificacion externa
public enum PoliticaFallo {
    CONTINUAR,
    IR_A_ACTIVIDAD_DE_ERROR,
    FINALIZAR_PROCESO
}
