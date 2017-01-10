/*
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
 */
package br.com.uiltonsites.servletframework.utility;

import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public class SFMsisdn extends SFMyLogger {
    
    private String msisdn;
    private Long msisdnLong;
    private String msisdnOriginal;
    
    public SFMsisdn(String msisdn) {
        super();
        setFromString(msisdn);
    }
    
    public SFMsisdn(String msisdn, SFLogger logger) {
        super(logger);
        setFromString(msisdn);
    }
    
    public SFMsisdn(Long msisdn) {
        super();
        setFromLong(msisdn);
    }
    
    public SFMsisdn(Long msisdn, SFLogger logger) {
        super(logger);
        setFromLong(msisdn);
    }
    
    
    
    private void setFromLong(Long msisdn) {
        
        msisdnOriginal = String.valueOf(msisdn);
        
        if (msisdn != null && msisdn > 0) {
            
            setFromString(msisdn.toString());
            
        } else {
            
            this.msisdn = null;
            this.msisdnLong = 0L;
            
        }
    }
    private void setFromString(String msisdn) {
        
        msisdnOriginal = String.valueOf(msisdn);
        
        if (msisdn != null && !msisdn.isEmpty() && NumberUtils.isDigits(msisdn)) {
            
            String newMsisdn = msisdn;
            
            newMsisdn = newMsisdn.startsWith("00") ? newMsisdn.substring(2) : newMsisdn;
            newMsisdn = newMsisdn.startsWith("0") ? newMsisdn.substring(1) : newMsisdn;
            
            if (newMsisdn.length() == 11) {
                
                newMsisdn =  "55" + newMsisdn;
                
            } else if (newMsisdn.length() == 13 && newMsisdn.startsWith("55")) {
                // ok
            } else {
                
                this.msisdn = null;
                this.msisdnLong = 0L;
                return;
                
            }
            
            this.msisdn = newMsisdn;
            Long tmpLong;
            
            try {
                
                tmpLong = Long.parseLong(this.msisdn);
                
            } catch (NumberFormatException ex) {
                
                tmpLong = 0L;
            }
            
            this.msisdnLong = tmpLong;
            
        } else {
            this.msisdn = null;
            this.msisdnLong = 0L;
        }
    }
    
    

    /**
     * Retorna o msisdn formatado como String
     * @return 
     */
    @Override
    public String toString() {
        return msisdn;
    }
    
    /**
     * Retorna o msisdn formatado como Long
     * @return 
     */
    public long toLong() {
        return msisdnLong;
    }
    
    /**
     * Verifica se o msisdn eh valido (se tem 13 digitos apos ter sido formatado)
     * @return 
     */
    public boolean isValid() {
        return (this.msisdn != null && this.msisdn.length() == 13 );
    }
    
    /**
     * Retorna o msisdn original sem modificacao
     * @return 
     */
    public String getOriginal() {
        return msisdnOriginal;
    }
    
    /**
     * Retorna o prefixo do msisdn (primeiros 4 digitos)
     * @return 
     */
    public String getPrefix() {
        if (this.msisdn == null) {
            return null;
        }
        return this.msisdn.substring(0, this.msisdn.length()-4);
    }
    
    
    
}
