/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.sql.Date;
import java.sql.Time;

public class ControlRegistro {

    private Date fecha;
    private String nombre_emp;
    private String documento_emp;
    private String cargo_emp;
    private String sede_emp;
    private Time hora_entrada;
    private Time hora_salida;
    private Boolean entradaMarcada;
    private Boolean salidaMarcada;

    public Date getFecha() {
        return fecha;
    }

    public Boolean getEntradaMarcada() {
        return entradaMarcada;
    }

    public void setEntradaMarcada(Boolean entradaMarcada) {
        this.entradaMarcada = entradaMarcada;
    }

    public Boolean getSalidaMarcada() {
        return salidaMarcada;
    }

    public void setSalidaMarcada(Boolean salidaMarcada) {
        this.salidaMarcada = salidaMarcada;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNombre_emp() {
        return nombre_emp;
    }

    public void setNombre_emp(String nombre_emp) {
        this.nombre_emp = nombre_emp;
    }

    public String getDocumento_emp() {
        return documento_emp;
    }

    public void setDocumento_emp(String documento_emp) {
        this.documento_emp = documento_emp;
    }

    public String getCargo_emp() {
        return cargo_emp;
    }

    public void setCargo_emp(String cargo_emp) {
        this.cargo_emp = cargo_emp;
    }

    public String getSede_emp() {
        return sede_emp;
    }

    public void setSede_emp(String sede_emp) {
        this.sede_emp = sede_emp;
    }

    public Time getHora_entrada() {
        return hora_entrada;
    }

    public void setHora_entrada(Time hora_entrada) {
        this.hora_entrada = hora_entrada;
    }

    public Time getHora_salida() {
        return hora_salida;
    }

    public void setHora_salida(Time hora_salida) {
        this.hora_salida = hora_salida;
    }
}
