package com.monitoramento.util;

public class ValidadorCNPJ {
    public static boolean validar(String cnpj) {
        cnpj = cnpj.replaceAll("[^0-9]", "");
        
        if (cnpj.length() != 14) return false;
        
        // Verifica se todos os dígitos são iguais
        boolean todosIguais = true;
        for (int i = 1; i < 14; i++) {
            if (cnpj.charAt(i) != cnpj.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;
        
        // Calcula primeiro dígito verificador
        int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += (cnpj.charAt(i) - '0') * peso1[i];
        }
        int primeiroDigito = soma % 11;
        if (primeiroDigito < 2) primeiroDigito = 0;
        else primeiroDigito = 11 - primeiroDigito;
        
        // Calcula segundo dígito verificador
        int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += (cnpj.charAt(i) - '0') * peso2[i];
        }
        int segundoDigito = soma % 11;
        if (segundoDigito < 2) segundoDigito = 0;
        else segundoDigito = 11 - segundoDigito;
        
        return (cnpj.charAt(12) - '0' == primeiroDigito) && 
               (cnpj.charAt(13) - '0' == segundoDigito);
    }
}