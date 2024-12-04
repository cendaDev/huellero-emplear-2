/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Cargo;
import Modelo.DB;
import Modelo.Funcionario;
import Modelo.Turno;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 *
 * @author cardo
 */
public class pruebas {

    private static Controlador controlador;

    public static void main(String[] args) {

        controlador = new Controlador();

        LocalTime hora = obtenerHora();
        LocalDate fecha = obtenerFecha();

        System.out.println("Hora: " + hora);
        System.out.println("Fecha: " + fecha);

        System.out.println("-----------------------------------");
        
        Funcionario funcionario = buscarFuncionarioPorDocumento("123456789");
        
        determinarEntradaOSalida(funcionario);

    }

    public static boolean determinarEntradaOSalida(Funcionario funcionario) {

        try (Connection connection = new DB().getConnection(); PreparedStatement pstmt = connection.prepareStatement(
                "SELECT entradaMarcada, salidaMarcada FROM `control-registros` WHERE documento_emp = ? AND fecha = ? ORDER BY ID DESC LIMIT 1")) {

            pstmt.setString(1, funcionario.getDocumento());
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = pstmt.executeQuery()) {
                boolean entradaMarcada = false;
                boolean salidaMarcada = false;

                if (resultSet.next()) {
                    entradaMarcada = resultSet.getBoolean("entradaMarcada");
                    salidaMarcada = resultSet.getBoolean("salidaMarcada");
                }

                LocalTime horaActual = obtenerHora();
                LocalDate fechaActual = obtenerFecha();

                if (salidaMarcada && !entradaMarcada) {
                    controlador.registrarEntrada(funcionario, horaActual, fechaActual);
                }

                if (!salidaMarcada && entradaMarcada) {
                    controlador.registrarSalida(funcionario, horaActual, fechaActual);
                }

                if (!salidaMarcada && !entradaMarcada) {
                    controlador.registrarEntrada(funcionario, horaActual, fechaActual);
                }

                if (salidaMarcada && entradaMarcada) {
                    controlador.registrarEntrada(funcionario, horaActual, fechaActual);
                }

                return true;

            } catch (SQLException e) {
                // Manejar adecuadamente la excepción (puedes imprimir o registrar de otra manera)
                e.printStackTrace();
            }

        } catch (SQLException e) {
            // Manejar adecuadamente la excepción (puedes imprimir o registrar de otra manera)
            e.printStackTrace();
        }

        return false;
    }

    public static void registrarEntrada(Funcionario funcionario, LocalTime horaComparacion, LocalDate fechaActual) {

        DayOfWeek diaSemana = fechaActual.getDayOfWeek();
        boolean registroExitoso = false;

        for (Turno turno : funcionario.getCargo().getTurnos()) {

            if (registroExitoso) {
                // Si ya se hizo un registro exitoso, salir del bucle
                break;
            }

            if (turno.getDias().equalsIgnoreCase("Lunes - Viernes")) {
                if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {
                    LocalTime horaIngresoTurno = turno.getHoraIngreso();

                    if (horaComparacion.isAfter(horaIngresoTurno.minusMinutes(30))
                            && horaComparacion.isBefore(horaIngresoTurno.plusMinutes(5))) {
                        controlador.guardarRegistro(funcionario, horaComparacion, fechaActual);

                        registroExitoso = true;

                    } else {

                    }
                }
            } else if (turno.getDias().equalsIgnoreCase("Sábado")) {
                if (diaSemana == DayOfWeek.SATURDAY) {
                    LocalTime horaIngresoTurno = turno.getHoraIngreso();
                    if (horaComparacion.isAfter(horaIngresoTurno.minusMinutes(30))
                            && horaComparacion.isBefore(horaIngresoTurno.plusMinutes(5))) {
                        controlador.guardarRegistro(funcionario, horaComparacion, fechaActual);

                        registroExitoso = true;
                    } else {

                    }
                }
            }
        }

        if (!registroExitoso) {

            System.out.println("Algo falló");

        } else {
            System.out.println("Entra registrada!");
        }
    }

    public static void registrarSalida(Funcionario funcionario, LocalTime horaComparacion, LocalDate fechaActual) {

        DayOfWeek diaSemana = fechaActual.getDayOfWeek();
        boolean registroExitoso = false;

        for (Turno turno : funcionario.getCargo().getTurnos()) {
            if (registroExitoso) {
                // Si ya se hizo un registro exitoso, salir del bucle
                break;
            }

            if (turno.getDias().equalsIgnoreCase("Lunes - Viernes")) {
                if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {

                    LocalTime horaSalidaTurno = turno.getHoraSalida();

                    if (horaComparacion.isAfter(horaSalidaTurno) && horaComparacion.isBefore(horaSalidaTurno.plusMinutes(30))) {
                        System.out.println(horaSalidaTurno);
                        System.out.println(turno);
                        controlador.actualizarRegistroConHoraSalida(funcionario, horaComparacion, fechaActual);

                        registroExitoso = true;

                    } else {

                    }
                }
            }
        }

        if (!registroExitoso) {
            System.out.println("Algo falló");
        } else {
            System.out.println("Salida registrada!");
        }
    }

    private static LocalTime obtenerHora() {
        LocalTime horaFormateada = null;

        // URL del servicio web que proporciona la hora actual de Colombia
        String url = "https://worldtimeapi.org/api/timezone/America/Bogota";

        try {
            // Hacer una solicitud HTTP GET al servicio web
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Analizar la respuesta JSON para obtener la hora actual de Colombia
            String dateTimeString = response.toString().split("\"datetime\":\"")[1].split("\"")[0];

            // Parsear la fecha y hora utilizando un DateTimeFormatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.ENGLISH);
            LocalDateTime horaActual = LocalDateTime.parse(dateTimeString, formatter);

            // Obtener la hora local
            horaFormateada = horaActual.toLocalTime();

            // Formatear la hora en formato HH:mm:ss
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm:ss");
            String horaFormateadaString = horaFormateada.format(formato);
            horaFormateada = LocalTime.parse(horaFormateadaString);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return horaFormateada;
    }
    
    
      private static LocalDate obtenerFecha() {
        LocalDate fechaFormateada = null;

        // URL del servicio web que proporciona la hora actual de Colombia
        String url = "https://worldtimeapi.org/api/timezone/America/Bogota";

        try {
            // Hacer una solicitud HTTP GET al servicio web
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Analizar la respuesta JSON para obtener la fecha actual de Colombia
            String dateTimeString = response.toString().split("\"datetime\":\"")[1].split("\"")[0];

            // Parsear la fecha y hora utilizando un DateTimeFormatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.ENGLISH);
            LocalDateTime horaActual = LocalDateTime.parse(dateTimeString, formatter);

            // Obtener la fecha local
            fechaFormateada = horaActual.toLocalDate();

            // Formatear la fecha en formato dd-MM-yyyy
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String fechaFormateadaString = fechaFormateada.format(formato);
            fechaFormateada = LocalDate.parse(fechaFormateadaString, formato);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fechaFormateada;
    }

    public static Funcionario buscarFuncionarioPorDocumento(String documento) {
        Funcionario funcionario = null;
        DB db = new DB();
        Connection connection = db.getConnection();

        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                String query = "SELECT f.*, c.nombre AS nombre_cargo, GROUP_CONCAT(t.`Dia/s` SEPARATOR ', ') AS dias, GROUP_CONCAT(t.horaIngreso SEPARATOR ', ') AS horasIngreso, GROUP_CONCAT(t.horaSalida SEPARATOR ', ') AS horasSalida "
                        + "FROM funcionarios f "
                        + "JOIN cargos c ON f.CargoID = c.cargoID "
                        + "LEFT JOIN cargosTurnos ct ON c.cargoID = ct.CargoID "
                        + "LEFT JOIN turnos t ON ct.TurnoID = t.turnoID "
                        + "WHERE f.documento = ?";

                statement = connection.prepareStatement(query);
                statement.setString(1, documento);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String nombre = resultSet.getString("nombre");
                    String sede = resultSet.getString("sede");

                    // Obtener los datos concatenados de los turnos
                    String diasConcatenados = resultSet.getString("dias");
                    String horasIngresoConcatenadas = resultSet.getString("horasIngreso");
                    String horasSalidaConcatenadas = resultSet.getString("horasSalida");

                    // Verificar si los datos concatenados no son nulos antes de dividirlos
                    if (diasConcatenados != null) {
                        // Dividir los datos concatenados en listas
                        List<String> listaDias = Arrays.asList(diasConcatenados.split(", "));
                        List<String> listaHorasIngreso = Arrays.asList(horasIngresoConcatenadas.split(", "));
                        List<String> listaHorasSalida = Arrays.asList(horasSalidaConcatenadas.split(", "));

                        // Crear la lista de objetos Turno
                        List<Turno> turnos = new ArrayList<>();
                        for (int i = 0; i < listaDias.size(); i++) {
                            Turno turno = new Turno(
                                    listaDias.get(i),
                                    LocalTime.parse(listaHorasIngreso.get(i)),
                                    LocalTime.parse(listaHorasSalida.get(i))
                            );
                            turnos.add(turno);
                        }

                        // Crear el objeto Cargo con la lista de turnos
                        Cargo cargo = new Cargo(resultSet.getString("nombre_cargo"), turnos);

                        // Crear el objeto Funcionario con la información obtenida
                        funcionario = new Funcionario(documento, nombre, cargo, sede);
                    } else {
                        // Manejar el caso en el que los datos de los turnos son nulos
                        // Puedes imprimir un mensaje o manejar de otra forma según tus necesidades
                        System.out.println("Los datos de los turnos son nulos para este funcionario.");
                    }
                } else {
                    // No se encontró ningún funcionario, muestra un mensaje
                    JOptionPane.showMessageDialog(null, "No se encontró ningún funcionario con ese documento", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                // Manejo de la excepción (puedes imprimir o manejar de otra forma)
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    db.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return funcionario;
    }

}
