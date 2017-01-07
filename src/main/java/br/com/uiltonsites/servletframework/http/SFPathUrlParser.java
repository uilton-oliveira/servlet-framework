/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.http;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
class PathParser {

    public int pathPos;
    public String prefix;
    public String suffix;
    public String var;

    public PathParser(int pathPos, String prefix, String suffix, String var) {
        this.pathPos = pathPos;
        this.prefix = prefix;
        this.suffix = suffix;
        this.var = var;
    }

}

public class SFPathUrlParser {

    private static List<PathParser> parsePathTemplate(String pathTemplate) {
        List<PathParser> pps = new ArrayList<PathParser>();

        if (pathTemplate.isEmpty()) {
            return pps;
        }

        int pos = 0;
        for (String s : pathTemplate.split("/")) {

            if (s.contains("{") && s.contains("}")) {
                String var = s.substring(s.indexOf("{") + 1, s.indexOf("}"));
                String prefix = s.substring(0, s.indexOf("{"));
                String suffix = s.substring(s.indexOf("}") + 1);

                pps.add(new PathParser(pos, prefix, suffix, var));
            } else {
                pps.add(new PathParser(pos, s, "", null));
            }

            pos++;
        }

        return pps;
    }

    private static String extractString(String str, String prefix, String suffix) {
        int start = str.indexOf(prefix) + prefix.length();
        int end = str.lastIndexOf(suffix);

        return str.substring(start, end);
    }

    public static List<SFPathValue> parse(String pathTemplate, List<String> pathUrl) {

        if (pathTemplate.startsWith("/")) {
            pathTemplate = pathTemplate.substring(1);
        }

        // remove last element if its empty ( url ends with / )
        if (!pathUrl.isEmpty()) {
            String last = pathUrl.get(pathUrl.size() - 1);
            if (last == null || last.isEmpty()) {
                pathUrl.remove(pathUrl.size() - 1);
            }
        }
        
        if (pathUrl.isEmpty() && pathTemplate.isEmpty()) {
            return new ArrayList<>();
        } else if (pathUrl.isEmpty() && !pathTemplate.isEmpty()) {
            return null;
        }
        
        List<PathParser> templates = parsePathTemplate(pathTemplate);
        List<SFPathValue> values = new ArrayList<>();

        if (pathUrl.size() != templates.size()) {
            return null;
        }

        int i = 0;
        for (String s : pathUrl) {

            PathParser t = templates.get(i);

            if (t.prefix != null && t.suffix != null && !t.prefix.isEmpty() && t.suffix.isEmpty() && t.var == null && !s.equals(t.prefix)) {
                return null;
            }

            if (s.startsWith(t.prefix) && s.endsWith(t.suffix)) {

                if (t.var != null) {
                    String value = extractString(s, t.prefix, t.suffix);
                    values.add(new SFPathValue(t.var, value));
                }
            } else {
                return null;
            }

            i++;
        }

        return values;

    }
}
