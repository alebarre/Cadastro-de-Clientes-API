package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.Date;
import java.util.List;

public class Qr_Codes {
    public Date expiration_date;
    public List<Amount> amount;
    public List<Splits> splits;
}
