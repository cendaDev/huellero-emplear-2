package Modelo;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author desar
 */
public class UpdateChecker {
    
    
    
    // URL del archivo version.txt en tu repositorio GitHub
    private static final String VERSION_URL = "https://raw.githubusercontent.com/cendaDev/huellero/main/version.txt";
    // URL del archivo .jar en GitHub Releases
    private static final String JAR_URL = "https://github.com/cendaDev/huellero/releases/download/v1.0.0/App Huellero Armenia.jar";
    // Versión actual de tu aplicación
    public static final String CURRENT_VERSION = "1.0.0"; // Actualízalo según la versión de tu app

    /**
     * Verifica la versión más reciente disponible en GitHub.
     */
    public static String getLatestVersion() throws IOException {
        // Conexión a GitHub para obtener el archivo version.txt
        URL url = new URL(VERSION_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // Cerrar las conexiones
        in.close();
        conn.disconnect();

        // Retorna la versión más reciente desde el archivo version.txt
        return content.toString().trim();
    }

    /**
     * Descarga el archivo .jar de la nueva versión desde GitHub Releases.
     */
    public static void downloadUpdate(String latestVersion) {
        try {
            // Conexión para descargar el archivo .jar desde la URL de GitHub Releases
            URL url = new URL(JAR_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Crear el archivo local donde se descargará el .jar
            File jarFile = new File("tuApp-" + latestVersion + ".jar");
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fileOutputStream = new FileOutputStream(jarFile);

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }

            // Cerrar los flujos
            in.close();
            fileOutputStream.close();
            conn.disconnect();

            System.out.println("Nueva versión descargada: " + jarFile.getName());
        } catch (IOException e) {
            System.out.println("Error al descargar la actualización: " + e.getMessage());
        }
    }
    
}
