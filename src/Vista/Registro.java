/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

import Controlador.Controlador;
import Modelo.Cargo;
import Modelo.DB;
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
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author cardo
 */
public class Registro extends javax.swing.JFrame {

    Controlador controlador;
    private boolean lectorIniciado = false;

    public Registro() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            System.out.println("no se puede modificar el campo visual: " + e);
        }

        initComponents();

        controlador = new Controlador();
        setExtendedState(this.MAXIMIZED_BOTH);

        setLocationRelativeTo(this);

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
                        EnviarTexto("La huella ha sido capturada, repite el proceso hasta que se registre la huella por completo");
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
        lblImagenHuella.setIcon(new ImageIcon(
                image.getScaledInstance(lblImagenHuella.getWidth(),
                        lblImagenHuella.getHeight(),
                        Image.SCALE_DEFAULT)));

        repaint();
    }

    public void EstadoHuellas() {
        EnviarTexto("Muestra de huellas necesarias para guardar la plantilla: "
                + Reclutador.getFeaturesNeeded());

    }

    public void EnviarTexto(String string) {
        txtArea.append(string + "\n");
    }

    public void start() {
        lector.startCapture();
        EnviarTexto("Utilizando el lector de huella");
    }

    public void stop() {
        lector.stopCapture();
        EnviarTexto("No se esta usando el lector de huella");
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

        featuresinscripcion = extraerCarateristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

        feauturesverificacion = extraerCarateristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (featuresinscripcion != null) {
            try {
                System.out.println("Carcterísticas de la huella creadas");
                Reclutador.addFeatures(featuresinscripcion);

                Image image = crearImagenHuella(sample);
                dibujarHuella(image);

                btnRegistrarSegundaHuella.setEnabled(false);

            } catch (DPFPImageQualityException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            } finally {
                EstadoHuellas();

                switch (Reclutador.getTemplateStatus()) {
                    case TEMPLATE_STATUS_READY:
                        stop();
                        setTemplate(Reclutador.getTemplate());
                        EnviarTexto("La plantilla ha sido creada!");

                        jPanel2.setVisible(true);
                        encontrarUsuario();

                        if (lblTittle.getText().equalsIgnoreCase("Por favor pon la primer huella en el lector de huella digital")) {
                            btnRegistrarSegundaHuella.setEnabled(true);
                        }

                        BtnGuardar.setEnabled(true);

                        BtnGuardar.grabFocus();
                        break;

                    case TEMPLATE_STATUS_FAILED:
                        Reclutador.clear();
                        stop();
                        EstadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(Registro.this, "La plantilla no pudo crearse");
                        start();
                        break;
                }
            }
        }
    }


     public void encontrarUsuario() {

        Funcionario funcionario = encontrarFuncionarioPorHuella(feauturesverificacion, "huella");

        if (funcionario != null) {

            jLabel5.setText("Primer huella: ya se encuentra registrada para el usuario:");

            lblNombreFun1.setText(funcionario.getNombre());
            lblNombreFun2.setText(funcionario.getDocumento());
        } else {

            funcionario = encontrarFuncionarioPorHuella(feauturesverificacion, "huella2");

            if (funcionario != null) {
                jLabel5.setText("Segunda huella: ya se encuentra registrada para el usuario:");
                lblNombreFun1.setText(funcionario.getNombre());
                lblNombreFun2.setText(funcionario.getDocumento());
            } else {

                jLabel5.setText("Usuario sin huellas registradas, continúa con el registro...");
                labelTextFun3.setVisible(false);
                labelTextFun4.setVisible(false);
                lblNombreFun1.setVisible(false);
                lblNombreFun2.setVisible(false);
            }

        }

    }

    public String encontrarDocumentoFuncionarioPorHuella(DPFPFeatureSet huella, String huellabytes) {
        String documentoEncontrado = null;

        try (java.sql.Connection connection = new DB().getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM funcionarios"
        )) {

            try (ResultSet resultSet = statement.executeQuery()) {
                float menorDistancia = Float.MAX_VALUE;

                while (resultSet.next()) {
                    byte[] templateBuffer = resultSet.getBytes(huellabytes);

                    if (templateBuffer != null && templateBuffer.length > 0) {
                        DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);

                        DPFPVerificationResult result = verificador.verify(huella, referenceTemplate);

                        if (result.isVerified() && result.getFalseAcceptRate() < 0.001) {
                            float distancia = result.getFalseAcceptRate();
                            if (distancia < menorDistancia) {
                                menorDistancia = distancia;

                                documentoEncontrado = resultSet.getString("documento");
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documentoEncontrado;
    }


 public Funcionario encontrarFuncionarioPorHuella(DPFPFeatureSet huella, String huellabytes) {
    Funcionario funcionarioEncontrado = null;
    DB db = new DB();
    Connection connection = db.getConnection();

    if (connection != null) {
        String query = "SELECT f.documento, f.nombre, f.sede, c.nombre AS nombre_cargo, "
                     + "GROUP_CONCAT(t.`dias` SEPARATOR ', ') AS dias, "
                     + "GROUP_CONCAT(t.hora_ingreso SEPARATOR ', ') AS horasIngreso, "
                     + "GROUP_CONCAT(t.hora_salida SEPARATOR ', ') AS horasSalida, "
                     + "f." + huellabytes + " "
                     + "FROM funcionarios f "
                     + "JOIN cargos c ON f.CargoID = c.cargoID "
                     + "LEFT JOIN cargos_turnos ct ON c.cargoID = ct.CargoID "
                     + "LEFT JOIN turnos t ON ct.TurnoID = t.turnoID "
                     + "GROUP BY f.documento";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            DPFPVerificationResult mejorResultado = null;
            byte[] mejorTemplate = null;

            while (resultSet.next()) {
                byte[] templateBuffer = resultSet.getBytes(huellabytes);

                if (templateBuffer != null && templateBuffer.length > 0) {
                    DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);

                    // Verificar la coincidencia de huellas
                    DPFPVerificationResult result = verificador.verify(huella, referenceTemplate);

                    if (result.isVerified()) {
                        if (mejorResultado == null || result.getFalseAcceptRate() < mejorResultado.getFalseAcceptRate()) {
                            mejorResultado = result;
                            mejorTemplate = templateBuffer;

                            String documento = resultSet.getString("documento");
                            String nombre = resultSet.getString("nombre");
                            String sede = resultSet.getString("sede");

                            // Obtener los datos concatenados de los turnos
                            String diasConcatenados = resultSet.getString("dias");
                            String horasIngresoConcatenadas = resultSet.getString("horasIngreso");
                            String horasSalidaConcatenadas = resultSet.getString("horasSalida");

                            // Crear la lista de objetos Turno solo si hay turnos
                            List<Turno> turnos = new ArrayList<>();
                            if (diasConcatenados != null && horasIngresoConcatenadas != null && horasSalidaConcatenadas != null) {
                                // Dividir los datos concatenados en listas
                                List<String> listaDias = Arrays.asList(diasConcatenados.split(", "));
                                List<String> listaHorasIngreso = Arrays.asList(horasIngresoConcatenadas.split(", "));
                                List<String> listaHorasSalida = Arrays.asList(horasSalidaConcatenadas.split(", "));

                                for (int i = 0; i < listaDias.size(); i++) {
                                    Turno turno = new Turno(
                                            listaDias.get(i),
                                            LocalTime.parse(listaHorasIngreso.get(i)),
                                            LocalTime.parse(listaHorasSalida.get(i))
                                    );
                                    turnos.add(turno);
                                }
                            }

                            // Crear el objeto Cargo con la lista de turnos
                            Cargo cargo = new Cargo(resultSet.getString("nombre_cargo"), turnos);

                            // Crear el objeto Funcionario con la información obtenida
                            funcionarioEncontrado = new Funcionario(documento, nombre, cargo, sede);

                            // Salir del bucle si se encuentra una coincidencia suficientemente buena
                            if (mejorResultado.getFalseAcceptRate() < 0.0001) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.closeConnection();
        }
    }
    return funcionarioEncontrado;
}


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpanelHuella = new javax.swing.JPanel();
        lblImagenHuella = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();
        btnRegistrarSegundaHuella = new javax.swing.JButton();
        BtnGuardar = new javax.swing.JButton();
        lblTittle = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtDocumento = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        labelTextFun = new javax.swing.JLabel();
        lblNombreFun = new javax.swing.JLabel();
        labelTextFun1 = new javax.swing.JLabel();
        lblSede1 = new javax.swing.JLabel();
        labelTextFun2 = new javax.swing.JLabel();
        lblCargo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lblNombreFun1 = new javax.swing.JLabel();
        labelTextFun3 = new javax.swing.JLabel();
        labelTextFun4 = new javax.swing.JLabel();
        lblNombreFun2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(283647, 214737));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jpanelHuella.setForeground(new java.awt.Color(255, 255, 255));

        lblImagenHuella.setBackground(new java.awt.Color(0, 153, 204));
        lblImagenHuella.setForeground(new java.awt.Color(255, 255, 255));

        txtArea.setBackground(new java.awt.Color(255, 255, 255));
        txtArea.setColumns(20);
        txtArea.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        txtArea.setForeground(new java.awt.Color(51, 51, 51));
        txtArea.setRows(5);
        txtArea.setBorder(null);
        txtArea.setEnabled(false);
        jScrollPane1.setViewportView(txtArea);

        btnRegistrarSegundaHuella.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        btnRegistrarSegundaHuella.setText("Registrar segunda huella");
        btnRegistrarSegundaHuella.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarSegundaHuellaActionPerformed(evt);
            }
        });

        BtnGuardar.setBackground(new java.awt.Color(255, 0, 0));
        BtnGuardar.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        BtnGuardar.setText("Guardar huellas");
        BtnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGuardarActionPerformed(evt);
            }
        });

        lblTittle.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        lblTittle.setForeground(new java.awt.Color(255, 0, 0));
        lblTittle.setText("Por favor pon la primer huella en el lector de huella digital");

        javax.swing.GroupLayout jpanelHuellaLayout = new javax.swing.GroupLayout(jpanelHuella);
        jpanelHuella.setLayout(jpanelHuellaLayout);
        jpanelHuellaLayout.setHorizontalGroup(
            jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpanelHuellaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpanelHuellaLayout.createSequentialGroup()
                        .addGroup(jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jpanelHuellaLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(btnRegistrarSegundaHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(79, 79, 79)
                        .addGroup(jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 860, Short.MAX_VALUE)
                            .addComponent(BtnGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(lblTittle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpanelHuellaLayout.setVerticalGroup(
            jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpanelHuellaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTittle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jpanelHuellaLayout.createSequentialGroup()
                        .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addGroup(jpanelHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRegistrarSegundaHuella)
                            .addComponent(BtnGuardar))))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Dubai", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setText("Registro de huella FUNCIONARIO");

        jLabel3.setFont(new java.awt.Font("Dubai", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        jLabel3.setText("Buscar funcionario por número de documento:");

        txtDocumento.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N

        btnBuscar.setBackground(new java.awt.Color(255, 0, 0));
        btnBuscar.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        jButton1.setText("Finalizar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        labelTextFun.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        labelTextFun.setText("Nombre:");

        lblNombreFun.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        lblNombreFun.setText("nombre");

        labelTextFun1.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        labelTextFun1.setText("Sede:");

        lblSede1.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        lblSede1.setText("nombre");

        labelTextFun2.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        labelTextFun2.setText("Cargo:");

        lblCargo.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        lblCargo.setText("nombre");

        jLabel2.setFont(new java.awt.Font("Dubai", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 0, 0));
        jLabel2.setText("Datos del funcionario:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelTextFun1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelTextFun, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelTextFun2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCargo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNombreFun, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .addComponent(lblSede1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(70, 70, 70)))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTextFun)
                    .addComponent(lblNombreFun))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTextFun1)
                    .addComponent(lblSede1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTextFun2)
                    .addComponent(lblCargo))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jLabel5.setFont(new java.awt.Font("Dubai", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 0, 0));
        jLabel5.setText("Primer huella: ya se encuentra registrada para el usuario:");

        lblNombreFun1.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        lblNombreFun1.setText("nombre");

        labelTextFun3.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        labelTextFun3.setText("Nombre:");

        labelTextFun4.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        labelTextFun4.setText("C.C");

        lblNombreFun2.setFont(new java.awt.Font("Dubai", 0, 14)); // NOI18N
        lblNombreFun2.setText("nombre");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 71, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelTextFun4, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreFun2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelTextFun3, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNombreFun1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTextFun3)
                    .addComponent(lblNombreFun1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTextFun4)
                    .addComponent(lblNombreFun2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(117, 117, 117)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(36, 36, 36)
                        .addComponent(txtDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(61, 61, 61)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(577, 577, 577)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(132, 132, 132)
                .addComponent(jpanelHuella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jpanelHuella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

        labelTextFun.setVisible(false);
        lblNombreFun.setVisible(false);
        labelTextFun1.setVisible(false);
        lblCargo.setVisible(false);
        labelTextFun2.setVisible(false);
        lblSede1.setVisible(false);
        BtnGuardar.setEnabled(false);
        btnRegistrarSegundaHuella.setEnabled(false);
        jpanelHuella.setVisible(false);
        jPanel1.setVisible(false);
        jPanel2.setVisible(false);
    }//GEN-LAST:event_formWindowOpened

    public void reiniciarHuellero() {

        lblImagenHuella.setIcon(null);
        txtArea.setText("");
        jPanel2.setVisible(false);

        Reclutador.clear();

        EstadoHuellas();

        // Volver a iniciar el proceso de captura para la segunda huella
        start();

    }


    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed

        System.out.println(lectorIniciado);

        Funcionario funcionario = controlador.buscarFuncionarioPorDocumento(txtDocumento.getText());

        if (funcionario != null) {

            lblNombreFun.setText(funcionario.getNombre());
            lblCargo.setText(funcionario.getCargo().getNombre());
            lblSede1.setText(funcionario.getSede());

            jpanelHuella.setVisible(true);
            labelTextFun.setVisible(true);
            lblNombreFun.setVisible(true);
            labelTextFun1.setVisible(true);
            lblCargo.setVisible(true);
            labelTextFun2.setVisible(true);
            lblSede1.setVisible(true);
            jPanel1.setVisible(true);
            BtnGuardar.setVisible(false);

            lblImagenHuella.setIcon(null);
            txtArea.setText("");
            jPanel2.setVisible(false);

            lblTittle.setText("Por favor pon la primer huella en el lector de huella digital");

            BtnGuardar.setVisible(false);
            btnRegistrarSegundaHuella.setVisible(true);

            Reclutador.clear();

            EstadoHuellas();

            if (!lectorIniciado) {
                iniciar();
                start();
            } else {
                start();
            }

        } else {
            JOptionPane.showMessageDialog(null, "El funcionario no se encuentra registrado, debes registrarlo en la página de recursos humanos de CENDA");
        }

    }//GEN-LAST:event_btnBuscarActionPerformed

    private void BtnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGuardarActionPerformed

        String documento = txtDocumento.getText();
        DPFPFeatureSet huella = featuresinscripcion;
        controlador.actualizarHuella2Funcionario(documento, huella, template);

        JOptionPane.showMessageDialog(null, "Huellas registradas con éxito");

        BtnGuardar.setEnabled(false);


    }//GEN-LAST:event_BtnGuardarActionPerformed

    private void btnRegistrarSegundaHuellaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarSegundaHuellaActionPerformed

        BtnGuardar.setVisible(true);
        BtnGuardar.setEnabled(false);
        String documento = txtDocumento.getText();
        DPFPFeatureSet huella = featuresinscripcion;
        controlador.actualizarHuellaFuncionario(documento, huella, template);

        JOptionPane.showMessageDialog(null, "La primer huella ha sido registrada con éxito, registra la segunda");

        reiniciarHuellero();

        lblTittle.setText("Por favor pon la segunda huella en el lector de huella digital");

        btnRegistrarSegundaHuella.setVisible(false);

    }//GEN-LAST:event_btnRegistrarSegundaHuellaActionPerformed


    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        Reclutador.clear();
        stop();
        Control control = new Control();
        control.setVisible(true);
        this.setVisible(false);

    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(Registro.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Registro.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Registro.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Registro.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Registro().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JButton BtnGuardar;
    private javax.swing.JButton btnBuscar;
    public static javax.swing.JButton btnRegistrarSegundaHuella;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jpanelHuella;
    private javax.swing.JLabel labelTextFun;
    private javax.swing.JLabel labelTextFun1;
    private javax.swing.JLabel labelTextFun2;
    private javax.swing.JLabel labelTextFun3;
    private javax.swing.JLabel labelTextFun4;
    private javax.swing.JLabel lblCargo;
    public static javax.swing.JLabel lblImagenHuella;
    private javax.swing.JLabel lblNombreFun;
    private javax.swing.JLabel lblNombreFun1;
    private javax.swing.JLabel lblNombreFun2;
    private javax.swing.JLabel lblSede1;
    private javax.swing.JLabel lblTittle;
    public static javax.swing.JTextArea txtArea;
    public static javax.swing.JTextField txtDocumento;
    // End of variables declaration//GEN-END:variables
}
