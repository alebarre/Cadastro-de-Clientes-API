package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;

public class Splits {
    public enum method {
        FIXED,
        PERCENTAGE
    }
    public List<Receivers> receivers;
}
