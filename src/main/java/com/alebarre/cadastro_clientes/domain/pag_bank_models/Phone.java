package com.alebarre.cadastro_clientes.domain.pag_bank_models;

public class Phone {

    public int country;
    public int area;
    public int number;
    public enum type {
        MOBILE,
        BUSINESS,
        HOME;
    }

}
