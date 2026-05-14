// ValidadorDocumento.java
package com.monitoramento.util;

public class ValidadorDocumento {
    
    /**
     * Valida um documento (CPF ou CNPJ) automaticamente baseado no tamanho
     * @param documento Documento a ser validado (pode conter pontos, traços, barras)
     * @return true se for um CPF ou CNPJ válido, false caso contrário
     */
    public static boolean validar(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }
        
        String docLimpo = documento.replaceAll("[^0-9]", "");
        
        if (docLimpo.length() == 11) {
            return validarCPF(docLimpo);
        } else if (docLimpo.length() == 14) {
            return validarCNPJ(docLimpo);
        }
        
        return false;
    }
    
    /**
     * Valida especificamente um CPF
     * @param cpf CPF a ser validado (deve conter apenas números)
     * @return true se o CPF é válido, false caso contrário
     */
    public static boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        
        if (cpf.length() != 11) return false;
        
        // Verifica se todos os dígitos são iguais
        boolean todosIguais = true;
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;
        
        // Calcula primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        
        // Calcula segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;
        
        return (cpf.charAt(9) - '0' == primeiroDigito) && 
               (cpf.charAt(10) - '0' == segundoDigito);
    }
    
    /**
     * Valida especificamente um CNPJ
     * @param cnpj CNPJ a ser validado (deve conter apenas números)
     * @return true se o CNPJ é válido, false caso contrário
     */
    public static boolean validarCNPJ(String cnpj) {
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
    
    /**
     * Formata um CPF no padrão XXX.XXX.XXX-XX
     * @param cpf CPF a ser formatado
     * @return CPF formatado ou string vazia se inválido
     */
    public static String formatarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        if (cpf.length() != 11) return cpf;
        
        return cpf.substring(0, 3) + "." + 
               cpf.substring(3, 6) + "." + 
               cpf.substring(6, 9) + "-" + 
               cpf.substring(9, 11);
    }
    
    /**
     * Formata um CNPJ no padrão XX.XXX.XXX/XXXX-XX
     * @param cnpj CNPJ a ser formatado
     * @return CNPJ formatado ou string vazia se inválido
     */
    public static String formatarCNPJ(String cnpj) {
        cnpj = cnpj.replaceAll("[^0-9]", "");
        if (cnpj.length() != 14) return cnpj;
        
        return cnpj.substring(0, 2) + "." + 
               cnpj.substring(2, 5) + "." + 
               cnpj.substring(5, 8) + "/" + 
               cnpj.substring(8, 12) + "-" + 
               cnpj.substring(12, 14);
    }
    
    /**
     * Formata automaticamente CPF ou CNPJ baseado no tamanho
     * @param documento Documento a ser formatado
     * @return Documento formatado
     */
    public static String formatar(String documento) {
        String docLimpo = documento.replaceAll("[^0-9]", "");
        
        if (docLimpo.length() == 11) {
            return formatarCPF(docLimpo);
        } else if (docLimpo.length() == 14) {
            return formatarCNPJ(docLimpo);
        }
        
        return documento;
    }
}