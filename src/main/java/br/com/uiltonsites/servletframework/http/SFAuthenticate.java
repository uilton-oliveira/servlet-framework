///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package br.com.uiltonsites.servletframework.http;
//
///**
// *
// * @author Uilton Oliveira - uilton.dev@gmail.com
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
