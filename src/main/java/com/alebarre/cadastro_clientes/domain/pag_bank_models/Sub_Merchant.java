package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;

public class Sub_Merchant {
    public String reference_id;
    public String name;
    public String tax_id;
    public String mcc;
    public List<PagBank_address> address;
    public List<Phone> phone;
}
