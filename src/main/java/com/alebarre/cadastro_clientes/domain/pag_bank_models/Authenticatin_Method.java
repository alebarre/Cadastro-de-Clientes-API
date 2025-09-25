package com.alebarre.cadastro_clientes.domain.pag_bank_models;

public class Authenticatin_Method {
    public enum type {
        THREEDS,
        INAPP
    }
    public String cavv;
    public String eci;
    public String xid;
    public String version;
    public String dstrans_id;
    public String status;
}
