package com.alebarre.cadastro_clientes.domain.pag_bank_models;

import java.util.List;

public class Boleto {
    public String due_date;
    public List<Instruction_lines> instruction_lines;
    public List<Holder> holder;
}
