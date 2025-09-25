package com.alebarre.cadastro_clientes.domain.pag_bank_models;

public class Recurring {
    public enum type {
        INITIAL,
        SUBSEQUENT,
        UNSCHEDULED,
        STANDING_ORDER
    }
    public String recurrence_id;
    public int original_amount;
}
