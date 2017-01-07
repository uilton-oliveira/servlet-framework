package br.com.uiltonsites.servletframework.utility;

import java.util.Random;

public final class SFToken {
	private final static int A_ASCII_CODE = 65;
	private final static  int Z_ASCII_CODE = 90;
	
	public static String getToken() {
		
            String token = "";
            Random random = new Random();

            for(int idx = 0; idx < 6;  idx++) {
                    token += (char)(random.nextInt(Z_ASCII_CODE - A_ASCII_CODE) + A_ASCII_CODE);
            }

            for(int idx = 0; idx < 4;  idx++) {
                    token += (random.nextInt(9));
            }

            return token;
	}

}
