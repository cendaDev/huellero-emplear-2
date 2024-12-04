/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import Modelo.UpdateChecker;
import Controlador.Controlador;
import Modelo.ControlRegistro;
import Modelo.Funcionario;
import Modelo.Turno;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import static com.digitalpersona.onetouch.processing.DPFPTemplateStatus.TEMPLATE_STATUS_FAILED;
import static com.digitalpersona.onetouch.processing.DPFPTemplateStatus.TEMPLATE_STATUS_READY;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import java.awt.Color;
import java.awt.Graphics;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import java.util.Collections;
import java.util.Comparator;

import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author cardo
 */
public class Control extends javax.swing.JFrame {

    /**
     * Creates new form Control
     */
    private boolean lectorIniciado = false;
    Controlador controlador;
    private boolean entradaMarcada = false;
    private boolean salidaMarcada = false;
    private Timer timer;
    private ImageIcon logoIcon;

    public Control() {
        initComponents();
        setLocationRelativeTo(this);
        controlador = new Controlador();
        actualizarFecha();
        iniciarTimer();
        controlador.actualizarRegistroEntrada();
        actualizarHora();
        labelBienvenido.setText("Huellero activo ...");
        setExtendedState(this.MAXIMIZED_BOTH);
        labelNombre.setVisible(false);
        jTable1.setRowHeight(30);
        iniciar();
        start();

        // Agregar un DocumentListener al JTextField para realizar el filtrado
        txtBuscarPorDocumento.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }

            private void filterTable() {

                DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();

                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                jTable1.setRowSorter(sorter);

                String text = txtBuscarPorDocumento.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1)); // Columna "Documento" (índice 1)
                }
            }
        }
        );

        // Agregar un DocumentListener al JTextField txtNombre para realizar el filtrado por nombre
        txtNombre.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTableByName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTableByName();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTableByName();
            }

            private void filterTableByName() {
                DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                jTable1.setRowSorter(sorter);

                String text = txtNombre.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0)); // Columna "Nombre" (índice 0)
                }
            }

        });
    }

    private void iniciarTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarHora();
            }
        });
        timer.start();
    }

    private void actualizarHora() {
        // Obtener la hora local ajustada a la zona horaria de Colombia
        LocalDateTime horaActual = LocalDateTime.now(ZoneId.of("America/Bogota"));

        // Formatear la hora actual incluyendo AM o PM
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("hh:mm:ss a", new Locale("es", "ES"));
        String horaFormateada = horaActual.format(formatoHora);

        // Actualizar el JLabel con la hora formateada en el hilo de la interfaz gráfica
        javax.swing.SwingUtilities.invokeLater(() -> labelHora.setText(horaFormateada));
    }

    private void actualizarFecha() {
        // Obtener la fecha local ajustada a la zona horaria de Colombia
        LocalDate fechaActual = LocalDate.now(ZoneId.of("America/Bogota"));

        // Obtener el nombre del día de la semana en español
        String diaSemanaTexto = fechaActual.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        // Formatear la fecha en el formato "día de la semana, dd de MMMM de yyyy"
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String fechaFormateada = fechaActual.format(formatoFecha);

        // Combinar el día de la semana con la fecha formateada
        String fechaFinal = diaSemanaTexto + ", " + fechaFormateada;

        // Actualizar el JLabel con la fecha formateada en el hilo de la interfaz gráfica
        javax.swing.SwingUtilities.invokeLater(() -> labelFecha.setText(fechaFinal));
    }

    private DPFPCapture lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    public DPFPVerification verificador = DPFPGlobal.getVerificationFactory().createVerification();
    public static DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";

    protected void iniciar() {

        lectorIniciado = true;

        lector.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        ProcesarCaptura(e.getSample());

                    }

                });
            }

        });
    }

    public DPFPFeatureSet featuresinscripcion;
    public DPFPFeatureSet feauturesverificacion;

    public DPFPFeatureSet extraerCarateristicas(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor
                = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();

        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }

    }

    public Image crearImagenHuella(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    public void dibujarHuella(Image image) {

        labelHuella.setIcon(new ImageIcon(
                image.getScaledInstance(labelHuella.getWidth(),
                        labelHuella.getHeight(),
                        Image.SCALE_DEFAULT)));
        repaint();
    }

    public void start() {
        lector.startCapture();
    }

    public void stop() {
        lector.stopCapture();
    }

    public DPFPTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }

    public void ProcesarCaptura(DPFPSample sample) {

        // Definir los componentes de la interfaz de usuario
        final JLabel labelEstadoLocal = labelEstado;
        final JLabel labelNombreLocal = labelNombre;
        final JPanel panelBienvenidoLocal = panelBienvenido;

        // Actualizar la interfaz de usuario para indicar que la huella ha sido capturada
        SwingUtilities.invokeLater(() -> {
            labelBienvenido.setForeground(Color.black);
            panelBienvenidoLocal.setVisible(true);
            labelEstadoLocal.setForeground(Color.red);
            labelEstadoLocal.setText("Huella capturada, espere por favor...");
            labelEstadoLocal.setVisible(true);
        });

        // Ejecutar el procesamiento de la huella en un hilo de fondo
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                featuresinscripcion = extraerCarateristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
                feauturesverificacion = extraerCarateristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

                if (featuresinscripcion != null) {
                    try {
                        Reclutador.addFeatures(featuresinscripcion);

                        Image image = crearImagenHuella(sample);
                        SwingUtilities.invokeLater(() -> dibujarHuella(image));

                        Registro registro = new Registro();

                        // Usar un array de un solo elemento para poder modificar el valor de funcionario
                        final Funcionario[] funcionarioRef = new Funcionario[1];
                        funcionarioRef[0] = registro.encontrarFuncionarioPorHuella(feauturesverificacion, "huella");

                        if (funcionarioRef[0] == null) {
                            funcionarioRef[0] = registro.encontrarFuncionarioPorHuella(feauturesverificacion, "huella2");
                        }

                        if (funcionarioRef[0] != null) {
                            // Determinar si es entrada o salida y proceder según corresponda
                            if (controlador.determinarEntradaOSalida(funcionarioRef[0])) {
                                SwingUtilities.invokeLater(() -> {
                                    labelNombreLocal.setVisible(true);
                                    labelNombreLocal.setText(funcionarioRef[0].getNombre());
                                });

                                // Ejecutar la actualización del registro en un hilo separado
                                SwingUtilities.invokeLater(() -> controlador.actualizarRegistroEntrada());
                            }
                        } else {
                            // Si ninguna huella coincide, mostrar error
                            SwingUtilities.invokeLater(() -> {
                                controlador.reproducirSonidoHuellaNoRegistrada();
                                labelBienvenido.setForeground(Color.red);
                                labelBienvenido.setText("Error, huella no registrada");
                                controlador.mostrarMensajeError("Error, huella no registrada");
                            });
                        }

                        Reclutador.clear();  // Limpiar el reclutador de huellas

                    } catch (DPFPImageQualityException e) {
                        e.printStackTrace();
                    } finally {
                        // Manejo de errores y liberación de recursos si es necesario
                    }

                    // Manejo del estado del template
                    SwingUtilities.invokeLater(() -> {
                        switch (Reclutador.getTemplateStatus()) {
                            case TEMPLATE_STATUS_READY:
                                stop();
                                setTemplate(Reclutador.getTemplate());
                                break;
                            case TEMPLATE_STATUS_FAILED:
                                Reclutador.clear();
                                stop();
                                setTemplate(null);
                                start();
                                break;
                        }
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                // Ocultar el texto de estado cuando el procesamiento haya terminado
                SwingUtilities.invokeLater(() -> labelEstadoLocal.setVisible(false));
            }
        }.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        btnRegistrarHuella = new javax.swing.JButton();
        panelBienvenido = new javax.swing.JPanel();
        labelBienvenido = new javax.swing.JLabel();
        labelNombre = new javax.swing.JLabel();
        labelHuella = new javax.swing.JLabel();
        labelEstado = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        labelFecha = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        labelHora = new javax.swing.JLabel();
        tableRegistros = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        txtBuscarPorDocumento = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel4.setBackground(new java.awt.Color(51, 51, 51));

        btnRegistrarHuella.setBackground(new java.awt.Color(255, 51, 51));
        btnRegistrarHuella.setFont(new java.awt.Font("Dubai", 0, 24)); // NOI18N
        btnRegistrarHuella.setText("Registrar nueva huella");
        btnRegistrarHuella.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarHuellaActionPerformed(evt);
            }
        });

        panelBienvenido.setBackground(new java.awt.Color(204, 204, 204));

        labelBienvenido.setFont(new java.awt.Font("Dubai", 1, 24)); // NOI18N
        labelBienvenido.setForeground(new java.awt.Color(51, 51, 51));
        labelBienvenido.setText("Bienvenid@, ");

        labelNombre.setFont(new java.awt.Font("Dubai", 1, 24)); // NOI18N
        labelNombre.setForeground(new java.awt.Color(0, 0, 0));
        labelNombre.setText("Nombre");

        labelEstado.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        labelEstado.setForeground(new java.awt.Color(0, 153, 255));

        javax.swing.GroupLayout panelBienvenidoLayout = new javax.swing.GroupLayout(panelBienvenido);
        panelBienvenido.setLayout(panelBienvenidoLayout);
        panelBienvenidoLayout.setHorizontalGroup(
            panelBienvenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBienvenidoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBienvenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBienvenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(labelBienvenido, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                        .addComponent(labelNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(labelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(labelHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBienvenidoLayout.setVerticalGroup(
            panelBienvenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBienvenidoLayout.createSequentialGroup()
                .addGroup(panelBienvenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBienvenidoLayout.createSequentialGroup()
                        .addComponent(labelBienvenido, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelEstado)
                        .addGap(0, 15, Short.MAX_VALUE))
                    .addComponent(labelHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));

        labelFecha.setBackground(new java.awt.Color(51, 51, 51));
        labelFecha.setFont(new java.awt.Font("Yu Gothic UI Light", 1, 48)); // NOI18N
        labelFecha.setForeground(new java.awt.Color(255, 51, 51));
        labelFecha.setText("Fecha");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(labelFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 671, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(labelFecha)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(51, 51, 51));
        jPanel6.setForeground(new java.awt.Color(255, 255, 255));

        labelHora.setBackground(new java.awt.Color(255, 255, 255));
        labelHora.setFont(new java.awt.Font("Yu Gothic UI Semibold", 3, 48)); // NOI18N
        labelHora.setForeground(new java.awt.Color(255, 51, 51));
        labelHora.setText("Hora");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(labelHora, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(100, 100, 100))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(labelHora, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        tableRegistros.setBackground(new java.awt.Color(51, 51, 51));

        jTable1.setBackground(new java.awt.Color(204, 204, 204));
        jTable1.setFont(new java.awt.Font("Dubai", 0, 18)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre", "Documento", "Hora de entrada"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel4.setFont(new java.awt.Font("Dubai", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 51, 51));
        jLabel4.setText("Registro de entradas de hoy");

        jLabel2.setFont(new java.awt.Font("Dubai", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Buscar por nombre");

        jLabel3.setFont(new java.awt.Font("Dubai", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Buscar por número de documento");

        javax.swing.GroupLayout tableRegistrosLayout = new javax.swing.GroupLayout(tableRegistros);
        tableRegistros.setLayout(tableRegistrosLayout);
        tableRegistrosLayout.setHorizontalGroup(
            tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableRegistrosLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tableRegistrosLayout.createSequentialGroup()
                        .addGroup(tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtBuscarPorDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 752, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        tableRegistrosLayout.setVerticalGroup(
            tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableRegistrosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tableRegistrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarPorDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43))
        );

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Rangos permitidos");

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Entrada: 30 minutos antes hasta la hora puntual");

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Software desarrollado por CENDA @2023 - 2024 todos los derechos reservados");

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Salida: hora puntual de salida y hasta 30 minutos después");

        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("v2");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(panelBienvenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRegistrarHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)))
                .addComponent(tableRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(121, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(146, 146, 146))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(panelBienvenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23)
                        .addComponent(btnRegistrarHuella)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel8))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tableRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 614, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));

        jPanel3.setBackground(new java.awt.Color(51, 51, 51));

        jLabel1.setFont(new java.awt.Font("Dubai", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Control de acceso y salida");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(561, 561, 561)
                .addComponent(jLabel1)
                .addContainerGap(1117, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegistrarHuellaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarHuellaActionPerformed

        Reclutador.clear();
        stop();
        Registro registro = new Registro();
        registro.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnRegistrarHuellaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Control.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Control.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Control.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Control.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Control().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRegistrarHuella;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    public static javax.swing.JTable jTable1;
    public static javax.swing.JLabel labelBienvenido;
    private javax.swing.JLabel labelEstado;
    private javax.swing.JLabel labelFecha;
    private javax.swing.JLabel labelHora;
    public static javax.swing.JLabel labelHuella;
    public static javax.swing.JLabel labelNombre;
    private javax.swing.JPanel panelBienvenido;
    private javax.swing.JPanel tableRegistros;
    private javax.swing.JTextField txtBuscarPorDocumento;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
