package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;

public class PagBank_Card {
    public String id;
    public String number;
    public String netwotk_token;
    public int exp_month;
    public int exp_year;
    public String secutiry_code;
    public boolean store;
    public String brand;
    public String product;
    public int first_digits;
    public List<Holder> holder;
}
