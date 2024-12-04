/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.time.LocalTime;

/**
 *
 * @author cardo
 */
public class Turno {

    private String dias;
    private LocalTime horaIngreso;
    private LocalTime horaSalida;

    public Turno(String dias, LocalTime horaIngreso, LocalTime horaSalida) {
        this.dias = dias;
        this.horaIngreso = horaIngreso;
        this.horaSalida = horaSalida;
    }

    public String getDias() {
        return dias;
    }

    public void setDias(String dias) {
        this.dias = dias;
    }

    public LocalTime getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(LocalTime horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    @Override
    public String toString() {
        return "Día/s: " + dias + ", Hora Ingreso: " + horaIngreso + ", Hora Salida: " + horaSalida;
    }

}
