package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;

public class Payment_Method {
    public enum type {
        CREDIT_CARD,
        BOLETO,
        PIX
    }
    public int installments;
    public boolean capture;
    public boolean capture_before;
    public String soft_descriptor;
    public List<PagBank_Card> card;
    public List<Token_Card> token_data;
    public List<Authenticatin_Method> authentication_method;
    public List<Boleto> boleto;
    public List<Pix> pix;
}
