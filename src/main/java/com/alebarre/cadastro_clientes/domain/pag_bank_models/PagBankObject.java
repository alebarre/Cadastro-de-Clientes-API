package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.ArrayList;
import java.util.List;

public class PagBankObject {

    public static String id;
    public static String reference_id;
    public List<Customer> customer;
    public List<Items> items;
    public List<Shipping> shipping;
    public List<Qr_Codes> qr_codes;
    public ArrayList notification_urls;
    public List<Charges> charges;
    public List<Links> links;

}
