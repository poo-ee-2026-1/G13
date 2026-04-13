package com.voltmonitor.util;

public class Validador {

    public static boolean validarCPF(String cpf) {
        if (cpf == null) return false;
        String c = cpf.replaceAll("[^0-9]", "");
        if (c.length() != 11) return false;
        if (c.matches("(\\d)\\1{10}")) return false;

        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (c.charAt(i) - '0') * (10 - i);
        int r1 = 11 - (soma % 11);
        if (r1 >= 10) r1 = 0;
        if (r1 != (c.charAt(9) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += (c.charAt(i) - '0') * (11 - i);
        int r2 = 11 - (soma % 11);
        if (r2 >= 10) r2 = 0;
        return r2 == (c.charAt(10) - '0');
    }

    public static boolean validarCNPJ(String cnpj) {
        if (cnpj == null) return false;
        String c = cnpj.replaceAll("[^0-9]", "");
        if (c.length() != 14) return false;
        if (c.matches("(\\d)\\1{13}")) return false;

        int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (c.charAt(i) - '0') * peso1[i];
        int r1 = soma % 11;
        r1 = r1 < 2 ? 0 : 11 - r1;
        if (r1 != (c.charAt(12) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 13; i++) soma += (c.charAt(i) - '0') * peso2[i];
        int r2 = soma % 11;
        r2 = r2 < 2 ? 0 : 11 - r2;
        return r2 == (c.charAt(13) - '0');
    }

    public static boolean validarIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                       "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }

    public static boolean validarDDD(String ddd) {
        if (ddd == null) return false;
        String d = ddd.replaceAll("[^0-9]", "");
        return d.length() == 2;
    }

    public static boolean validarTelefone(String telefone) {
        if (telefone == null) return false;
        String t = telefone.replaceAll("[^0-9]", "");
        return t.length() == 9;
    }

    public static boolean validarTensaoNominal(double tensao) {
        return tensao >= 1 && tensao <= 1500;
    }

    public static String formatarCPF(String cpf) {
        String c = cpf.replaceAll("[^0-9]", "");
        if (c.length() != 11) return cpf;
        return c.substring(0, 3) + "." + c.substring(3, 6) + "." +
               c.substring(6, 9) + "-" + c.substring(9);
    }

    public static String formatarCNPJ(String cnpj) {
        String c = cnpj.replaceAll("[^0-9]", "");
        if (c.length() != 14) return cnpj;
        return c.substring(0, 2) + "." + c.substring(2, 5) + "." +
               c.substring(5, 8) + "/" + c.substring(8, 12) + "-" + c.substring(12);
    }

    public static String apenasNumeros(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9]", "");
    }
}
