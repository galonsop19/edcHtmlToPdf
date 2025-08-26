package com.credix.edcHtmlToPdf.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface EdcConstants {
    static final Integer CATEGORY  = 1;
    static final Integer SECTION  = 2;
    static final String TAG_P = "p";
    static final String TAG_DIV = "div";
    static final String TAG_TR = "tr";
    static final String MOD_SECTION = "section";
    static final String MOD_TABLE = "table";

    static final String FINAL_LINE = "de cuenta.";
    static final String ASPECTOS = "Aspectos destacados";
    static final String ABREVIACIONES = "Abreviaciones";

    static final Set<String> PRODUCTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Cuotas cero interés*",
            "Ampliar plazo cero interés",
            "Ampliar plazo cuoticas",
            "Cuoticas",
            "Crédito personal",
            "Crédito moto",
            "Plan liquidez - Refinanciamiento",
            "Plan apoyo - Refinanciamiento",
            "Plan solidario - Refinanciamiento"
    )));

    static final Set<String> SUMMARY_SECTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Movimientos de la tarjeta de crédito",
            "Detalle de pagos del periodo",
            "Detalle de transacciones",
            "Detalle de intereses",
            "Detalle de otros cargos",
            "Detalle de servicios de elección voluntaria",
            "Cargos por gestión de cobro",
            "Resumen de movimientos"
    )));

    static final Set<String> TOP_LEVEL_SECTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Otras líneas de financiamiento",
            "Resumen de tarjeta de crédito",
            "Detalle de pago",
            ASPECTOS,
            ABREVIACIONES
    )));

    static final Set<String> INTEREST_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Interés corriente",
            "Interés moratorio",
            "Reversión de intereses",
            "Interés de otras líneas de financiamiento",
            "Interés de periodos anteriores"
    )));

}
