package com.alebarre.cadastro_clientes.domain.pag_bank_models;

public class Token_Card {
    public String requestor_id;
    public enum wallet {
        APPLE_PAY,
        GOOGLE_PAY,
        SAMSUNG_PAY,
        MERCHANT_TOKENIZATION_PROGRAM
    }
    public String cryptogram;
    public String ecommerce_domain;
    public int assurance_level;
}
