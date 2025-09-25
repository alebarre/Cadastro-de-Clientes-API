package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Charges {
    public String id;
    public enum status {
        AUTHORIZED,
        PAID,
        IN_ANALYSIS,
        DECLINED,
        CANCELED,
        WAITING
    }
    public Date created_at;
    public Date paid_at;
    public String reference_id;
    public status description;
    public List<PagBanck_Amount> amount;
    public List<Payment_Response> payment_response;
    public List<Payment_Method> payment_method;
    public List<Recurring> recurring;
    public List<Sub_Merchant> sub_merchant;
    public Map metadata;
    public List<Links> links;
    public List<Splits> splits;
}
