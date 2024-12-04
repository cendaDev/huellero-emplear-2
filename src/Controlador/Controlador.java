/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Cargo;
import Modelo.ControlRegistro;
import Modelo.DB;
import Modelo.Funcionario;
import Modelo.Turno;
import Vista.Anotacion;
import static Vista.Control.jTable1;
import static Vista.Control.labelBienvenido;
import static Vista.Control.labelHuella;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPTemplate;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Controlador {

    private DB db;

    public Controlador() {
        this.db = new DB();
    }

    private boolean contarSegundos;

    public void editarLabelsSegundos(JLabel label, String text) {

        long tiempoInicio = System.currentTimeMillis();
        long segundosTranscurridos;

        while (contarSegundos) {

            segundosTranscurridos = (System.currentTimeMillis() - tiempoInicio) / 1000;

            if (segundosTranscurridos == 2) {
                contarSegundos = false;
                label.setText(text);
                labelHuella.setIcon(null);
            }

            try {
                Thread.sleep(1000); // Espera 1 segundo antes de actualizar nuevamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para obtener la lista de turnos desde el ResultSet
    public ArrayList<Turno> obtenerTurnosDesdeResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Turno> turnos = new ArrayList<>();

        // Obtener información sobre los turnos desde el ResultSet
        String dias = resultSet.getString("dias");
        Time horaIngreso = resultSet.getTime("horaIngreso");
        Time horaSalida = resultSet.getTime("horaSalida");

        // Verificar si hay información sobre los turnos
        if (dias != null && horaIngreso != null && horaSalida != null) {
            // Crear el objeto Turno y agregarlo a la lista
            Turno turno = new Turno(dias, horaIngreso.toLocalTime(), horaSalida.toLocalTime());
            turnos.add(turno);
        }

        return turnos;
    }


    
    
     public boolean determinarEntradaOSalida(Funcionario funcionario) {
        if (funcionario == null) {
            // Mostrar mensaje de error en la interfaz de usuario
            mostrarMensajeError("Error, Huella no registrada");
            return false;
        }

        try (Connection connection = new DB().getConnection(); PreparedStatement pstmt = connection.prepareStatement(
                "SELECT entrada_marcada, salida_marcada FROM `control_registro` WHERE documento_emp = ? AND fecha = ? ORDER BY ID DESC LIMIT 1")) {

            pstmt.setString(1, funcionario.getDocumento());
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            try (ResultSet resultSet = pstmt.executeQuery()) {
                boolean entradaMarcada = false;
                boolean salidaMarcada = false;

                if (resultSet.next()) {
                    entradaMarcada = resultSet.getBoolean("entrada_marcada");
                    salidaMarcada = resultSet.getBoolean("salida_marcada");
                }

                LocalTime horaActual = obtenerHora();
                LocalDate fechaActual = obtenerFecha();

                if (salidaMarcada && !entradaMarcada) {
                    registrarEntrada(funcionario, horaActual, fechaActual);
                }

                if (!salidaMarcada && entradaMarcada) {
                    registrarSalida(funcionario, horaActual, fechaActual);
                }

                if (!salidaMarcada && !entradaMarcada) {
                    registrarEntrada(funcionario, horaActual, fechaActual);
                }

                if (salidaMarcada && entradaMarcada) {
                    registrarEntrada(funcionario, horaActual, fechaActual);
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

    
    
    

// Método para mostrar mensajes de error en la interfaz de usuario
    public void mostrarMensajeError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            labelBienvenido.setForeground(Color.RED);
            labelBienvenido.setText(mensaje);

            contarSegundos = true;
            editarLabelsSegundos(labelBienvenido, "último ingreso fallido, huella no registrada");
        });
    }

    public void registrarEntrada(Funcionario funcionario, LocalTime horaComparacion, LocalDate fechaActual) {

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

                    if (horaComparacion.isAfter(horaIngresoTurno.minusMinutes(30)) && horaComparacion.isBefore(horaIngresoTurno.plusMinutes(1))) {
                        guardarRegistro(funcionario, horaComparacion, fechaActual);
                        labelBienvenido.setForeground(Color.black);
                        labelBienvenido.setText("Bienvenid@");

                        Thread timerThread = new Thread(() -> {
                            contarSegundos = true;
                            editarLabelsSegundos(labelBienvenido, "Último ingreso registrado");
                        });
                        registroExitoso = true;
                        timerThread.start();
                    } else {

                    }
                }
            } else if (turno.getDias().equalsIgnoreCase("Sábado")) {
                if (diaSemana == DayOfWeek.SATURDAY) {
                    LocalTime horaIngresoTurno = turno.getHoraIngreso();
                    if (horaComparacion.isAfter(horaIngresoTurno.minusMinutes(30)) && horaComparacion.isBefore(horaIngresoTurno.plusMinutes(1))) {
                        guardarRegistro(funcionario, horaComparacion, fechaActual);
                        labelBienvenido.setForeground(Color.black);
                        labelBienvenido.setText("Bienvenid@");
                        Thread timerThread = new Thread(() -> {
                            contarSegundos = true;
                            editarLabelsSegundos(labelBienvenido, "Último ingreso registrado");
                        });
                        timerThread.start();
                        registroExitoso = true;
                    } else {

                    }
                }
            } else if (turno.getDias().equalsIgnoreCase("Domingo")) {
                if (diaSemana == DayOfWeek.SUNDAY) {
                    LocalTime horaIngresoTurno = turno.getHoraIngreso();
                    if (horaComparacion.isAfter(horaIngresoTurno.minusMinutes(30)) && horaComparacion.isBefore(horaIngresoTurno.plusMinutes(1))) {
                        guardarRegistro(funcionario, horaComparacion, fechaActual);
                        labelBienvenido.setForeground(Color.black);
                        labelBienvenido.setText("Bienvenid@");

                        Thread timerThread = new Thread(() -> {
                            contarSegundos = true;
                            editarLabelsSegundos(labelBienvenido, "Último ingreso registrado");
                        });
                        timerThread.start();
                        registroExitoso = true;
                    } else {

                    }
                }
            }
        }

        if (!registroExitoso) {
            // Solo si el registro no fue exitoso

            Thread timer = new Thread(() -> {
                reproducirSonidoErrorEntrada();
            });

            timer.start();

            Thread timerThread = new Thread(() -> {

                contarSegundos = true;

                int opcion = JOptionPane.showConfirmDialog(
                        null,
                        "¿Deseas enviar una anotación?",
                        "Confirmación",
                        JOptionPane.YES_NO_OPTION);

                // Comprueba la opción seleccionada
                if (opcion == JOptionPane.YES_OPTION) {
                    String motivo = "Entrada no marcada";
                    Anotacion anotacion = new Anotacion(motivo, funcionario, horaComparacion, fechaActual);
                    anotacion.setVisible(true);

                } else {
                    editarLabelsSegundos(labelBienvenido, "Último ingreso fallido por falta de anotación");
                    labelBienvenido.setForeground(Color.red);
                }

            });
            timerThread.start();
        } else {
            reproducirSonidoEntrada();
        }
    }

    public void registrarSalida(Funcionario funcionario, LocalTime horaComparacion, LocalDate fechaActual) {

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
                        actualizarRegistroConHoraSalida(funcionario, horaComparacion, fechaActual);
                        labelBienvenido.setForeground(Color.black);
                        labelBienvenido.setText("Salida registrada con éxito");

                        Thread timerThread = new Thread(() -> {
                            contarSegundos = true;
                            editarLabelsSegundos(labelBienvenido, "Última salida registrada");
                        });

                        registroExitoso = true;
                        timerThread.start();
                    } else {

                    }
                }
            } else if (turno.getDias().equalsIgnoreCase("Sábado")) {
                if (diaSemana == DayOfWeek.SATURDAY) {
                    LocalTime horaSalidaTurno = turno.getHoraSalida();
                    if (horaComparacion.isAfter(horaSalidaTurno) && horaComparacion.isBefore(horaSalidaTurno.plusMinutes(30))) {
                        actualizarRegistroConHoraSalida(funcionario, horaComparacion, fechaActual);
                        labelBienvenido.setForeground(Color.black);
                        labelBienvenido.setText("Salida registrada con éxito");
                        Thread timerThread = new Thread(() -> {
                            contarSegundos = true;
                            editarLabelsSegundos(labelBienvenido, "Última salida registrada");
                        });
                        timerThread.start();
                        registroExitoso = true;
                    } else {

                    }
                }
            } else if (turno.getDias().equalsIgnoreCase("Domingo")) {
                if (diaSemana == DayOfWeek.SUNDAY) {
                    LocalTime horaSalidaTurno = turno.getHoraSalida();
                    if (horaComparacion.isAfter(horaSalidaTurno) && horaComparacion.isBefore(horaSalidaTurno.plusMinutes(30))) {
                        actualizarRegistroConHoraSalida(funcionario, horaComparacion, fechaActual);
                        labelBienvenido.setForeground(Color.black);
                        labelBienvenido.setText("Salida registrada con éxito");

                        Thread timerThread = new Thread(() -> {
                            contarSegundos = true;
                            editarLabelsSegundos(labelBienvenido, "Última salida registrada");
                        });
                        timerThread.start();
                        registroExitoso = true;
                    } else {

                    }
                }
            }
        }

        if (!registroExitoso) {
            // Solo si el registro no fue exitoso

            Thread timer = new Thread(() -> {
                reproducirSonidoErrorSalida();
            });

            timer.start();

            Thread timerThread = new Thread(() -> {

                contarSegundos = true;

                int opcion = JOptionPane.showConfirmDialog(
                        null,
                        "¿Deseas enviar una anotación?",
                        "Confirmación",
                        JOptionPane.YES_NO_OPTION);

                // Comprueba la opción seleccionada
                if (opcion == JOptionPane.YES_OPTION) {
                    String motivo = "Salida no marcada";

                    Anotacion anotacion = new Anotacion(motivo, funcionario, horaComparacion, fechaActual);
                    anotacion.setVisible(true);

                } else {
                    editarLabelsSegundos(labelBienvenido, "Última salida fallida por falta de anotación");
                    labelBienvenido.setForeground(Color.red);
                }

            });
            timerThread.start();
        } else {
            reproducirSonidoSalida();
        }
    }

    public Funcionario buscarFuncionarioPorDocumento(String documento) {
        Funcionario funcionario = null;
        DB db = new DB();
        Connection connection = db.getConnection();

        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                String query = "SELECT f.*, c.nombre AS nombre_cargo, GROUP_CONCAT(t.`dias` SEPARATOR ', ') AS dias, GROUP_CONCAT(t.hora_ingreso SEPARATOR ', ') AS horasIngreso, GROUP_CONCAT(t.hora_salida SEPARATOR ', ') AS horasSalida "
                        + "FROM funcionarios f "
                        + "JOIN cargos c ON f.CargoID = c.cargoID "
                        + "LEFT JOIN cargos_turnos ct ON c.cargoID = ct.CargoID "
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

    public List<ControlRegistro> buscarRegistrosDeHoy() {
        List<ControlRegistro> registros = new ArrayList<>();
        DB db = new DB();
        Connection connection = db.getConnection();

        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Obtener la fecha actual del sistema
                long millis = System.currentTimeMillis();
                java.util.Date utilDate = new java.util.Date(millis);
                java.sql.Date fechaActual = new java.sql.Date(utilDate.getTime());

                // Modifica la consulta para seleccionar registros sin una hora de salida (hora_salida IS NULL)
                String query = "SELECT nombre_emp, documento_emp, hora_entrada FROM `control_registro` WHERE fecha = ? AND hora_salida IS NULL AND sede = 'Armenia'";

                statement = connection.prepareStatement(query);
                statement.setDate(1, fechaActual);
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    ControlRegistro controlRegistro = new ControlRegistro();
                    controlRegistro.setNombre_emp(resultSet.getString("nombre_emp"));
                    controlRegistro.setDocumento_emp(resultSet.getString("documento_emp"));
                    controlRegistro.setHora_entrada(resultSet.getTime("hora_entrada"));
                    controlRegistro.setEntradaMarcada(null);
                    controlRegistro.setSalidaMarcada(null);

                    registros.add(controlRegistro);
                }
            } catch (SQLException e) {
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

        return registros;
    }

    public void guardarRegistro(Funcionario funcionario, LocalTime horaActual, LocalDate fechaActual) {

        String sede = "Armenia";

        try (Connection connection = new DB().getConnection(); PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO `control_registro` (cargo_emp, documento_emp, entrada_marcada, fecha, hora_entrada, hora_salida, hora_trabajadas, nombre_emp, salida_marcada, sede, sede_emp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            System.out.println(fechaActual);

              pstmt.setString(1, funcionario.getCargo().getNombre());
            pstmt.setString(2, funcionario.getDocumento());
            pstmt.setBoolean(3, true); // Marca la entrada como true
            pstmt.setDate(4, java.sql.Date.valueOf(fechaActual));
            pstmt.setTime(5, java.sql.Time.valueOf(horaActual));
            pstmt.setNull(6, java.sql.Types.TIME); // Establecer la hora de salida como NULL
            pstmt.setDouble(7, 0); // Establecer las horas trabajadas como 0
            pstmt.setString(8, funcionario.getNombre());
            pstmt.setBoolean(9, false); // Salida como false
            pstmt.setString(10, sede);
            pstmt.setString(11, funcionario.getSede());

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                // Puedes lanzar una excepción o registrar en un archivo de registro en lugar de imprimir en la consola.
                System.out.println("Datos insertados con éxito.");
            } else {
                // Puedes lanzar una excepción o registrar en un archivo de registro en lugar de imprimir en la consola.
                System.out.println("No se insertaron datos.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 
    public void actualizarRegistroConHoraSalida(Funcionario funcionario, LocalTime horaActual, LocalDate fechaActual) {
        DB db = new DB();
        Connection connection = db.getConnection();

        if (connection != null) {
            PreparedStatement pstmt = null;

            try {

                // Buscar el registro con hora de entrada pero sin hora de salida para la fecha actual
                String sql = "SELECT fecha, hora_entrada FROM `control_registro` WHERE documento_emp = ? AND fecha = ? AND hora_salida IS NULL";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, funcionario.getDocumento());
                pstmt.setDate(2, java.sql.Date.valueOf(fechaActual));

                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    java.sql.Time horaEntrada = resultSet.getTime("hora_entrada");
                    java.sql.Time horaSalida = java.sql.Time.valueOf(horaActual);

                    java.sql.Time horasTrabajadas = calcularDiferenciaHoras(horaEntrada, horaSalida);

                    // Actualizar las columnas entradaMarcada y salidaMarcada
                    sql = "UPDATE `control_registro` SET hora_salida = ?, hora_trabajadas = ?, entrada_marcada = ?, salida_marcada = ? WHERE documento_emp = ? AND fecha = ? AND hora_salida IS NULL";
                    pstmt = connection.prepareStatement(sql);
                    pstmt.setTime(1, horaSalida);
                    pstmt.setTime(2, horasTrabajadas);
                    pstmt.setBoolean(3, true); // entradaMarcada = true
                    pstmt.setBoolean(4, true); // salidaMarcada = true
                    pstmt.setString(5, funcionario.getDocumento());
                    pstmt.setDate(6, java.sql.Date.valueOf(fechaActual));

                    int filasAfectadas = pstmt.executeUpdate();
                    if (filasAfectadas > 0) {
                        System.out.println("Registro actualizado con éxito. Horas trabajadas: " + horasTrabajadas);
                    } else {
                        System.out.println("No se actualizó ningún registro para la fecha actual.");
                    }
                } else {
                    System.out.println("No se encontró un registro de entrada sin hora de salida para la fecha actual y el documento proporcionado.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (pstmt != null) {
                        pstmt.close();
                    }
                    db.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public java.sql.Time calcularDiferenciaHoras(java.sql.Time horaEntrada, java.sql.Time horaSalida) {
        long diferenciaMillis = horaSalida.getTime() - horaEntrada.getTime();
        long segundos = diferenciaMillis / 1000;
        long horas = segundos / 3600;
        segundos %= 3600;
        long minutos = segundos / 60;
        segundos %= 60;
        return java.sql.Time.valueOf(String.format("%02d:%02d:%02d", horas, minutos, segundos));
    }

    public void actualizarHuellaFuncionario(String documento, DPFPFeatureSet huella, DPFPTemplate template) {

        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer tamañoHuella = template.serialize().length;

        DB db = new DB();
        Connection connection = db.getConnection();

        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                String query = "UPDATE funcionarios SET huella = ? WHERE documento = ?";
                statement = connection.prepareStatement(query);
                statement.setBinaryStream(1, datosHuella, tamañoHuella);
                statement.setString(2, documento);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Huella actualizada exitosamente para el funcionario con documento: " + documento);
                } else {
                    System.out.println("No se encontró ningún funcionario con el documento: " + documento);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    db.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean registrarAnotacion(String documento, String anotacion, String nombreFuncionario, String motivo, LocalDate fechaActual, LocalTime horaActual) {
        try (Connection connection = new DB().getConnection(); PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO anotacion (anotacion, documento_funcionario, fecha, hora, motivo, nombre_funcionario) VALUES (?, ?, ?, ?, ?, ?)")) {

            pstmt.setString(1, anotacion);
             pstmt.setString(2, documento);
            pstmt.setDate(3, java.sql.Date.valueOf(fechaActual));
            pstmt.setTime(4, java.sql.Time.valueOf(horaActual));
            pstmt.setString(5, motivo);
            pstmt.setString(6, nombreFuncionario);
           
           

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {

                return true;
            } else {

                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void actualizarHuella2Funcionario(String documento, DPFPFeatureSet huella, DPFPTemplate template) {

        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer tamañoHuella = template.serialize().length;

        DB db = new DB();
        Connection connection = db.getConnection();

        if (connection != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                String query = "UPDATE funcionarios SET huella2 = ? WHERE documento = ?";
                statement = connection.prepareStatement(query);
                statement.setBinaryStream(1, datosHuella, tamañoHuella);
                statement.setString(2, documento);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Huella 2 actualizada exitosamente para el funcionario con documento: " + documento);
                } else {
                    System.out.println("No se encontró ningún funcionario con el documento: " + documento);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    db.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void reproducirSonidoSalida() {
        String mp3FilePath = "/Assets/Salida registrada co.mp3"; // Reemplaza con la ruta correcta de tu archivo MP3

        try {

            Player player = new Player(getClass().getResourceAsStream(mp3FilePath));
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    public void reproducirSonidoEntrada() {
        String mp3FilePath = "/Assets/Acceso registrado co.mp3"; // Reemplaza con la ruta correcta de tu archivo MP3

        try {

            Player player = new Player(getClass().getResourceAsStream(mp3FilePath));
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    public void reproducirSonidoErrorEntrada() {
        String mp3FilePath = "/Assets/Error no es hora de ingreso.mp3"; // Reemplaza con la ruta correcta de tu archivo MP3

        try {

            Player player = new Player(getClass().getResourceAsStream(mp3FilePath));
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    public void reproducirSonidoErrorSalida() {
        String mp3FilePath = "/Assets/Error no es hora de salida.mp3"; // Reemplaza con la ruta correcta de tu archivo MP3

        try {

            Player player = new Player(getClass().getResourceAsStream(mp3FilePath));
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    public void reproducirSonidoHuellaNoRegistrada() {
        String mp3FilePath = "/Assets/Error la huella no esta registrada.mp3"; // Reemplaza con la ruta correcta de tu archivo MP3

        try {

            Player player = new Player(getClass().getResourceAsStream(mp3FilePath));
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }



private LocalTime obtenerHora() {
    // Obtener la hora actual en la zona horaria de Colombia
    LocalTime horaActual = LocalTime.now(ZoneId.of("America/Bogota"));

    // Formatear la hora en formato HH:mm:ss
    DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);
    String horaFormateadaString = horaActual.format(formatoHora);
    LocalTime horaFormateada = LocalTime.parse(horaFormateadaString, formatoHora);

    return horaFormateada;
}

private LocalDate obtenerFecha() {
    // Obtener la fecha actual en la zona horaria de Colombia
    LocalDate fechaActual = LocalDate.now(ZoneId.of("America/Bogota"));

    // Formatear la fecha en formato dd-MM-yyyy
    DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
    String fechaFormateadaString = fechaActual.format(formatoFecha);
    LocalDate fechaFormateada = LocalDate.parse(fechaFormateadaString, formatoFecha);

    return fechaFormateada;
}

    public void actualizarRegistroEntrada() {
        List<ControlRegistro> registros = buscarRegistrosDeHoy();

        // Ordenar la lista de registros por la hora de entrada de manera descendente (más reciente primero)
        Collections.sort(registros, Comparator.comparing(ControlRegistro::getHora_entrada).reversed());

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        // Borra todas las filas existentes en la tabla
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        for (ControlRegistro controlRegistro : registros) {
            Object[] rowData = new Object[4];
            rowData[0] = controlRegistro.getNombre_emp();
            rowData[1] = controlRegistro.getDocumento_emp();
            rowData[2] = controlRegistro.getHora_entrada();

            model.addRow(rowData);
        }
    }

}
