package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;
import java.util.Map;

public class PagBanck_Amount {
    public int value;
    public String currency;
    public List<Summary> summary;
    public List<Payment_Response> payment_response;
    public List<Payment_Method> payment_methods;
    public List<Recurring> recurring;
    public List<Sub_Merchant> sub_merchant;
    public Map metadata;
    public List<Links> links;
    public List<Splits> splits;

}
