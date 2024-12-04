/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cardo
 */
public class Cargo {
    
  private String nombre;
  private ArrayList<Turno>turnos;

    public Cargo(String nombre, List<Turno> turnos) {
        this.nombre = nombre;
        this.turnos = new ArrayList<>(turnos); // Convierte la lista a ArrayList
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<Turno> getTurnos() {
        return turnos;
    }

    public void setTurnos(ArrayList<Turno> turnos) {
        this.turnos = turnos;
    }
  
  

    
}
