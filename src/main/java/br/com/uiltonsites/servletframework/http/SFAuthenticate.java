///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package br.com.uiltonsites.servletframework.http;
//
///**
// *
// * @author Uilton Oliveira <uilton.dev@gmail.com>
// */
//public class SFAuthenticate {
//    /**
//     * <pre>
//     * Este metodo espera os seguintes metodos POST ou GET: username, password e redirect
//     *
//     * username = usuario
//     * password = senha
//     * redirect = url para redirecionar apos o login (opcional)
//     *
//     * Este metodo tambem ira guardar o objeto AcoUserInfo na sessao de atributo 'userinfo'
//     *
//     * </pre>
//     * @param c
//     * @param loginPage ex: /admin/login
//     * @param afterLoginPage ex: /admin/dashboard
//     */
//    public static void authenticate(SFServletContainer c, String loginPage, String afterLoginPage) {
//        String username = c.getParameter("username");
//        String password = c.getParameter("password");
//        String redirect = c.getParameter("redirect");
//        SFLDAP ldap = new SFLDAP();
//        AcoUserInfo ui = ldap.login(username, password);
//        if (ui != null) {
//            c.removeSessionAttribute("login_failed");
//            c.setSessionAttribute("userinfo", ui);
//            if (redirect != null) {
//                c.sendRedirect(redirect);
//            } else {
//                c.sendRedirect(afterLoginPage);
//            }
//        } else {
//            c.setSessionAttribute("login_failed", true);
//            c.sendRedirect(loginPage);
//        }
//    }
//}
