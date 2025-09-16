package com.alebarre.cadastro_clientes.DTO;

import com.alebarre.cadastro_clientes.domain.Endereco;


// EnderecoDTO.java
public record EnderecoDTO(
        Long id,
        String cidade,
        String logradouro,
        String uf,
        String bairro,
        String numero,
        String complemento,
        String cep,
        String pais
) {
    public static EnderecoDTO fromEntity(Endereco e) {
        return new EnderecoDTO(
                e.getId(),
                e.getCidade(),
                e.getLogradouro(),
                e.getUf(),
                e.getBairro(),
                e.getNumero(),
                e.getComplemento(),
                e.getCep(),
                e.getPais()
        );
    }

    public Endereco toEntity() {
        Endereco e = new Endereco();
        e.setId(this.id);
        e.setCep(this.cep);
        e.setLogradouro(this.logradouro);
        e.setNumero(this.numero);
        e.setComplemento(this.complemento);
        e.setBairro(this.bairro);
        e.setCidade(this.cidade);
        e.setUf(this.uf);
        e.setPais(this.pais);
        return e;
    }


}

