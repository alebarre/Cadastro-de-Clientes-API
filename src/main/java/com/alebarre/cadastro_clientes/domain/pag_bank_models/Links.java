package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;

public class Links {
    public enum rel {
        SELF
    }
    public String href;
    public String media;
    public enum type {
        GET,
        POST,
        DELETE,
        PUT
    }
    public List<Wallet> wallet;
}
