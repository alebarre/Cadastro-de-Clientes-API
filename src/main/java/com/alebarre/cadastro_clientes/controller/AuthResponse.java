package com.alebarre.cadastro_clientes.controller;

record AuthResponse(String token, long expiresIn, String username) {
}
